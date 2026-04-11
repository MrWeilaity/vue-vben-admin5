package com.vben.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.common.PageResult;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.params.DictDataParams;
import com.vben.system.dto.params.DictTypeParams;
import com.vben.system.dto.system.dict.*;
import com.vben.system.entity.SysDictData;
import com.vben.system.entity.SysDictType;
import com.vben.system.mapper.SysDictDataMapper;
import com.vben.system.mapper.SysDictTypeMapper;
import com.vben.system.service.system.ISysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字典服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictService extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictService {
    private static final String CACHE_PREFIX = "dict:data:";
    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 应用启动时预热字典到 Redis。
     */
    @PostConstruct
    public void warmupRedisCache() {
        rebuildRedisCache();
    }

    @Override
    public PageResult<DictTypeResponse> typeList(DictTypeParams params) {
        Page<SysDictType> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(StrUtil.isNotBlank(params.getName()), SysDictType::getName, params.getName())
                .like(StrUtil.isNotBlank(params.getCode()), SysDictType::getCode, params.getCode())
                .eq(params.getStatus() != null, SysDictType::getStatus, params.getStatus())
                .orderByAsc(SysDictType::getSortOrder)
                .orderByDesc(SysDictType::getId);
        Page<SysDictType> result = dictTypeMapper.selectPage(page, wrapper);
        List<DictTypeResponse> items = result.getRecords().stream().map(this::toTypeResponse).toList();
        return new PageResult<>(result.getTotal(), items);
    }

    @Override
    public List<DictTypeResponse> typeOptions() {
        return lambdaQuery()
                .eq(SysDictType::getStatus, 1)
                .orderByAsc(SysDictType::getSortOrder)
                .orderByDesc(SysDictType::getId)
                .list()
                .stream()
                .map(this::toTypeResponse)
                .toList();
    }

    @Override
    public void createType(DictTypeCreateRequest request) {
        String code = normalizeCode(request.getCode());
        ensureTypeCodeUnique(code, null);
        SysDictType type = new SysDictType();
        type.setName(normalizeName(request.getName()));
        type.setCode(code);
        type.setStatus(request.getStatus());
        type.setCacheEnabled(request.getCacheEnabled());
        type.setBuiltIn(0);
        type.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        type.setRemark(request.getRemark());
        dictTypeMapper.insert(type);
        afterCommit(() -> syncTypeCache(type.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(Long id, DictTypeUpdateRequest request) {
        SysDictType type = requireType(id);
        String oldCode = type.getCode();
        String code = normalizeCode(request.getCode());
        ensureTypeCodeUnique(code, id);
        type.setName(normalizeName(request.getName()));
        type.setCode(code);
        type.setStatus(request.getStatus());
        type.setCacheEnabled(request.getCacheEnabled());
        type.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        type.setRemark(request.getRemark());
        dictTypeMapper.updateById(type);

        // 保证冗余 type_code 与类型编码一致
        SysDictData updateEntity = new SysDictData();
        updateEntity.setTypeCode(code);
        dictDataMapper.update(updateEntity, new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getTypeId, id));
        afterCommit(() -> {
            syncTypeCache(oldCode);
            syncTypeCache(code);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTypeCode(Long id, String newCode) {
        SysDictType type = requireType(id);
        String oldCode = type.getCode();
        String normalized = normalizeCode(newCode);
        ensureTypeCodeUnique(normalized, id);
        type.setCode(normalized);
        dictTypeMapper.updateById(type);

        SysDictData updateEntity = new SysDictData();
        updateEntity.setTypeCode(normalized);
        dictDataMapper.update(updateEntity, new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getTypeId, id));
        afterCommit(() -> {
            evictCache(oldCode);
            syncTypeCache(normalized);
        });
    }

    @Override
    public void updateTypeStatus(Long id, Integer status) {
        SysDictType type = requireType(id);
        type.setStatus(status);
        dictTypeMapper.updateById(type);
        afterCommit(() -> syncTypeCache(type.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType type = requireType(id);
        if (type.getBuiltIn() != null && type.getBuiltIn() == 1) {
            throw new ServiceException("系统内置字典不允许删除");
        }
        Long itemCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getTypeId, id));
        if (itemCount != null && itemCount > 0) {
            throw new ServiceException("该字典下存在字典项，请先删除字典项后再删除类型");
        }
        dictTypeMapper.deleteById(id);
        afterCommit(() -> evictCache(type.getCode()));
    }

    @Override
    public PageResult<DictDataResponse> dataList(DictDataParams params) {
        Page<SysDictData> page = new Page<>(params.getPage(), params.getPageSize());
        String typeCode = params.getTypeCode();
        String normalizedTypeCode = StrUtil.isBlank(typeCode) ? null : normalizeCode(typeCode);
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(normalizedTypeCode != null, SysDictData::getTypeCode, normalizedTypeCode)
                .like(StrUtil.isNotBlank(params.getLabel()), SysDictData::getLabel, params.getLabel())
                .like(StrUtil.isNotBlank(params.getValue()), SysDictData::getValue, params.getValue())
                .eq(params.getStatus() != null, SysDictData::getStatus, params.getStatus())
                .orderByAsc(SysDictData::getSortOrder)
                .orderByDesc(SysDictData::getId);
        Page<SysDictData> result = dictDataMapper.selectPage(page, wrapper);
        List<DictDataResponse> items = result.getRecords().stream().map(this::toDataResponse).toList();
        return new PageResult<>(result.getTotal(), items);
    }

    @Override
    public List<DictDataResponse> dataByTypeCode(String typeCode, boolean onlyEnabled) {
        String normalized = normalizeCode(typeCode);

        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getCode, normalized));
        if (type == null) {
            return Collections.emptyList();
        }
        if (type.getStatus() == null || type.getStatus() != 1) {
            evictCache(normalized);
            return Collections.emptyList();
        }

        List<DictDataResponse> cached = getFromCache(normalized);
        if (cached != null) {
            return filterStatus(cached, onlyEnabled);
        }

        List<DictDataResponse> items = dictDataMapper.selectList(
                        new LambdaQueryWrapper<SysDictData>()
                                .eq(SysDictData::getTypeCode, normalized)
                                .orderByAsc(SysDictData::getSortOrder)
                                .orderByDesc(SysDictData::getId))
                .stream()
                .map(this::toDataResponse)
                .toList();

        if (type.getCacheEnabled() != null && type.getCacheEnabled() == 1) {
            putCache(normalized, items);
        }

        return filterStatus(items, onlyEnabled);
    }

    @Override
    public Map<String, List<DictDataResponse>> dataByTypeCodes(List<String> typeCodes, boolean onlyEnabled) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        return typeCodes.stream()
                .filter(StrUtil::isNotBlank)
                .map(this::normalizeCode)
                .distinct()
                .collect(Collectors.toMap(code -> code, code -> dataByTypeCode(code, onlyEnabled)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createData(DictDataCreateRequest request) {
        SysDictType type = requireTypeByCode(request.getTypeCode());
        ensureDataValueUnique(type.getCode(), request.getValue(), null);
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(type.getCode(), null);
        }

        SysDictData data = new SysDictData();
        data.setTypeId(type.getId());
        data.setTypeCode(type.getCode());
        data.setLabel(normalizeLabel(request.getLabel()));
        data.setValue(normalizeValue(request.getValue()));
        data.setStatus(request.getStatus());
        data.setIsDefault(request.getIsDefault());
        data.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        data.setTagType(request.getTagType());
        data.setTagClass(request.getTagClass());
        data.setCssStyle(request.getCssStyle());
        data.setExtJson(request.getExtJson());
        data.setRemark(request.getRemark());
        dictDataMapper.insert(data);
        afterCommit(() -> syncTypeCache(type.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateData(Long id, DictDataUpdateRequest request) {
        SysDictData data = requireData(id);
        SysDictType type = requireTypeByCode(request.getTypeCode());
        ensureDataValueUnique(type.getCode(), request.getValue(), id);

        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(type.getCode(), id);
        }

        String oldTypeCode = data.getTypeCode();
        data.setTypeId(type.getId());
        data.setTypeCode(type.getCode());
        data.setLabel(normalizeLabel(request.getLabel()));
        data.setValue(normalizeValue(request.getValue()));
        data.setStatus(request.getStatus());
        data.setIsDefault(request.getIsDefault());
        data.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        data.setTagType(request.getTagType());
        data.setTagClass(request.getTagClass());
        data.setCssStyle(request.getCssStyle());
        data.setExtJson(request.getExtJson());
        data.setRemark(request.getRemark());
        dictDataMapper.updateById(data);
        afterCommit(() -> {
            syncTypeCache(oldTypeCode);
            syncTypeCache(type.getCode());
        });
    }

    @Override
    public void updateDataStatus(Long id, Integer status) {
        SysDictData data = requireData(id);
        data.setStatus(status);
        dictDataMapper.updateById(data);
        afterCommit(() -> syncTypeCache(data.getTypeCode()));
    }

    @Override
    public void deleteData(Long id) {
        SysDictData data = requireData(id);
        dictDataMapper.deleteById(id);
        afterCommit(() -> syncTypeCache(data.getTypeCode()));
    }

    @Override
    public void refreshCache() {
        var keySet = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keySet != null && !keySet.isEmpty()) {
            List<String> keys = keySet.stream().toList();
            redisTemplate.delete(keys);
        }
        rebuildRedisCache();
    }

    private List<DictDataResponse> filterStatus(List<DictDataResponse> items, boolean onlyEnabled) {
        if (!onlyEnabled) {
            return items;
        }
        return items.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).toList();
    }

    private List<DictDataResponse> getFromCache(String typeCode) {
        String raw = redisTemplate.opsForValue().get(cacheKey(typeCode));
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("读取字典缓存失败，typeCode={}", typeCode, e);
            return null;
        }
    }

    private void putCache(String typeCode, List<DictDataResponse> items) {
        try {
            redisTemplate.opsForValue().set(cacheKey(typeCode), objectMapper.writeValueAsString(items), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入字典缓存失败，typeCode={}", typeCode, e);
        }
    }

    private void evictCache(String typeCode) {
        if (StrUtil.isBlank(typeCode)) {
            return;
        }
        try {
            redisTemplate.delete(cacheKey(typeCode));
        } catch (Exception e) {
            log.warn("删除字典缓存失败，typeCode={}", typeCode, e);
        }
    }

    private void rebuildRedisCache() {
        List<SysDictType> allTypes = dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCacheEnabled, 1)
                .eq(SysDictType::getStatus, 1));
        if (allTypes.isEmpty()) {
            return;
        }

        for (SysDictType type : allTypes) {
            List<DictDataResponse> items = dictDataMapper.selectList(
                            new LambdaQueryWrapper<SysDictData>()
                                    .eq(SysDictData::getTypeCode, type.getCode())
                                    .orderByAsc(SysDictData::getSortOrder)
                                    .orderByDesc(SysDictData::getId))
                    .stream()
                    .map(this::toDataResponse)
                    .toList();
            putCache(type.getCode(), items);
        }
        log.info("字典缓存预热完成，已加载 {} 个字典类型到Redis", allTypes.size());
    }

    private void syncTypeCache(String typeCode) {
        if (StrUtil.isBlank(typeCode)) {
            return;
        }
        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, typeCode));
        if (type == null
                || type.getCacheEnabled() == null || type.getCacheEnabled() != 1
                || type.getStatus() == null || type.getStatus() != 1) {
            evictCache(typeCode);
            return;
        }
        List<DictDataResponse> items = dictDataMapper.selectList(
                        new LambdaQueryWrapper<SysDictData>()
                                .eq(SysDictData::getTypeCode, typeCode)
                                .orderByAsc(SysDictData::getSortOrder)
                                .orderByDesc(SysDictData::getId))
                .stream()
                .map(this::toDataResponse)
                .toList();
        putCache(typeCode, items);
    }

    private void afterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runAfterCommitSafely(runnable);
                }
            });
            return;
        }
        runAfterCommitSafely(runnable);
    }

    private void runAfterCommitSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("字典缓存提交后同步失败", e);
        }
    }

    private void clearDefault(String typeCode, Long excludeId) {
        List<SysDictData> existingDefault = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getTypeCode, typeCode)
                .eq(SysDictData::getIsDefault, 1));
        if (existingDefault.isEmpty()) {
            return;
        }
        for (SysDictData item : existingDefault) {
            if (excludeId != null && Objects.equals(item.getId(), excludeId)) {
                continue;
            }
            item.setIsDefault(0);
            dictDataMapper.updateById(item);
        }
    }

    private void ensureTypeCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysDictType::getId, excludeId);
        }
        Long count = dictTypeMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new ServiceException("字典编码已存在");
        }
    }

    private void ensureDataValueUnique(String typeCode, String value, Long excludeId) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getTypeCode, typeCode)
                .eq(SysDictData::getValue, normalizeValue(value));
        if (excludeId != null) {
            wrapper.ne(SysDictData::getId, excludeId);
        }
        Long count = dictDataMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new ServiceException("同一字典类型下字典值不能重复");
        }
    }

    private SysDictType requireType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new ServiceException("字典类型不存在");
        }
        return type;
    }

    private SysDictType requireTypeByCode(String typeCode) {
        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, normalizeCode(typeCode)));
        if (type == null) {
            throw new ServiceException("字典类型不存在");
        }
        return type;
    }

    private SysDictData requireData(Long id) {
        SysDictData data = dictDataMapper.selectById(id);
        if (data == null) {
            throw new ServiceException("字典项不存在");
        }
        return data;
    }

    private String normalizeCode(String code) {
        if (StrUtil.isBlank(code)) {
            throw new ServiceException("字典编码不能为空");
        }
        String normalized = code.trim().toLowerCase();
        if (!normalized.matches("^[a-z][a-z0-9_:.]{1,63}$")) {
            throw new ServiceException("字典编码格式不合法，建议格式：biz_scope_status");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        if (StrUtil.isBlank(name)) {
            throw new ServiceException("字典名称不能为空");
        }
        return name.trim();
    }

    private String normalizeLabel(String label) {
        if (StrUtil.isBlank(label)) {
            throw new ServiceException("字典标签不能为空");
        }
        return label.trim();
    }

    private String normalizeValue(String value) {
        if (StrUtil.isBlank(value)) {
            throw new ServiceException("字典值不能为空");
        }
        return value.trim();
    }

    private String cacheKey(String typeCode) {
        return CACHE_PREFIX + typeCode;
    }

    private DictTypeResponse toTypeResponse(SysDictType type) {
        return DictTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .code(type.getCode())
                .status(type.getStatus())
                .cacheEnabled(type.getCacheEnabled())
                .builtIn(type.getBuiltIn())
                .sortOrder(type.getSortOrder())
                .remark(type.getRemark())
                .createTime(type.getCreateTime())
                .build();
    }

    private DictDataResponse toDataResponse(SysDictData data) {
        return DictDataResponse.builder()
                .id(data.getId())
                .typeId(data.getTypeId())
                .typeCode(data.getTypeCode())
                .label(data.getLabel())
                .value(data.getValue())
                .status(data.getStatus())
                .isDefault(data.getIsDefault())
                .sortOrder(data.getSortOrder())
                .tagType(data.getTagType())
                .tagClass(data.getTagClass())
                .cssStyle(data.getCssStyle())
                .extJson(data.getExtJson())
                .remark(data.getRemark())
                .createTime(data.getCreateTime())
                .build();
    }
}

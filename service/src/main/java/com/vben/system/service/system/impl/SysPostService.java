package com.vben.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.PageResult;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.params.PostParams;
import com.vben.system.dto.system.post.PostCreateRequest;
import com.vben.system.dto.system.post.PostResponse;
import com.vben.system.dto.system.post.PostUpdateRequest;
import com.vben.system.entity.SysPost;
import com.vben.system.entity.SysUserPost;
import com.vben.system.mapper.SysPostMapper;
import com.vben.system.mapper.SysUserPostMapper;
import com.vben.system.service.system.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 岗位业务服务，负责岗位增删改查与业务校验。
 */
@Service
@RequiredArgsConstructor
public class SysPostService extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    private final SysPostMapper postMapper;
    private final SysUserPostMapper userPostMapper;

    /**
     * 分页查询岗位列表。
     *
     * @param postParams 查询参数
     * @return 分页岗位数据
     */
    public PageResult<PostResponse> listForResponse(PostParams postParams) {
        validateDateRange(postParams.getStartTime(), postParams.getEndTime());

        Page<SysPost> page = new Page<>(postParams.getPage(), postParams.getPageSize());
        LambdaQueryChainWrapper<SysPost> query = lambdaQuery()
                .eq(postParams.getId() != null, SysPost::getId, postParams.getId())
                .eq(postParams.getStatus() != null, SysPost::getStatus, postParams.getStatus())
                .like(StrUtil.isNotBlank(postParams.getName()), SysPost::getName, postParams.getName())
                .like(StrUtil.isNotBlank(postParams.getRemark()), SysPost::getRemark, postParams.getRemark());

        LocalDate startTime = postParams.getStartTime();
        if (startTime != null) {
            query.ge(SysPost::getCreateTime, startTime.atStartOfDay());
        }

        LocalDate endTime = postParams.getEndTime();
        if (endTime != null) {
            query.lt(SysPost::getCreateTime, endTime.plusDays(1).atStartOfDay());
        }

        Page<SysPost> result = query.orderByDesc(SysPost::getId).page(page);
        List<PostResponse> items = result.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResult<>(result.getTotal(), items);
    }

    /**
     * 查询全部岗位列表。
     *
     * @return 岗位列表
     */
    public List<PostResponse> allList() {
        return lambdaQuery()
                .orderByDesc(SysPost::getId)
                .list()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 新增岗位。
     *
     * @param request 新增岗位数据
     */
    public void create(PostCreateRequest request) {
        ensurePostNameUnique(request.getName(), null);

        SysPost post = new SysPost();
        post.setName(request.getName().trim());
        post.setStatus(request.getStatus());
        post.setRemark(request.getRemark());
        postMapper.insert(post);
    }

    /**
     * 更新岗位。
     *
     * @param id      岗位 ID
     * @param request 岗位更新数据
     */
    public void update(Long id, PostUpdateRequest request) {
        SysPost post = requirePost(id);

        String name = normalizeName(request.getName());
        ensurePostNameUnique(name, id);

        post.setName(name);
        post.setStatus(request.getStatus());
        post.setRemark(request.getRemark());
        postMapper.updateById(post);
    }

    /**
     * 删除岗位。
     *
     * @param id 岗位 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePost(id);
        Long relationCount = userPostMapper.selectCount(
                new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getPostId, id)
        );
        if (relationCount != null && relationCount > 0) {
            throw new ServiceException("该岗位已分配给 " + relationCount + " 个用户，请先解除岗位关联后再删除");
        }

        postMapper.deleteById(id);
    }

    private void validateDateRange(LocalDate startTime, LocalDate endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new ServiceException("开始时间不能大于结束时间");
        }
    }

    private SysPost requirePost(Long id) {
        SysPost post = postMapper.selectById(id);
        if (post == null) {
            throw new ServiceException("岗位不存在");
        }
        return post;
    }

    private void ensurePostNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getName, normalizeName(name));
        if (excludeId != null) {
            queryWrapper.ne(SysPost::getId, excludeId);
        }
        Long count = postMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new ServiceException("岗位名称已存在");
        }
    }

    private String normalizeName(String name) {
        if (StrUtil.isBlank(name)) {
            throw new ServiceException("岗位名称不能为空");
        }
        return name.trim();
    }

    private PostResponse toResponse(SysPost post) {
        return PostResponse.builder()
                .id(post.getId())
                .name(post.getName())
                .status(post.getStatus())
                .remark(post.getRemark())
                .createTime(post.getCreateTime())
                .build();
    }

    /**
     * 更新岗位状态
     *
     * @param id     岗位id
     * @param status 岗位状态
     */
    public void updateStatus(Long id, Integer status) {
        SysPost post = requirePost(id);
        post.setStatus(status);
        postMapper.updateById(post);
    }
}

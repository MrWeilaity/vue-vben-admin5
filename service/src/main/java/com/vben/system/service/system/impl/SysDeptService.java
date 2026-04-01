package com.vben.system.service.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.system.dept.DeptCreateRequest;
import com.vben.system.dto.system.dept.DeptResponse;
import com.vben.system.dto.system.dept.DeptUpdateRequest;
import com.vben.system.entity.SysDept;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.service.system.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 部门业务服务，负责部门模块的查询与增删改逻辑。
 */
@Service
@RequiredArgsConstructor
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;

    /**
     * 查询部门列表（按 ID 升序）。平铺结果，不构建树形结构。
     *
     * @return 部门列表
     */
    public List<SysDept> list() {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getId));
    }

    public List<DeptResponse> treeDeptList() {
        List<SysDept> deptList = list();
        if (CollectionUtil.isEmpty(deptList)) {
            return new ArrayList<>();
        }
        Map<Long, DeptResponse> map = new HashMap<>(deptList.size());
        List<DeptResponse> roots = new ArrayList<>();

        for (SysDept dept : deptList) {
            DeptResponse node = DeptResponse.builder()
                    .id(dept.getId())
                    .pid(dept.getPid())
                    .name(dept.getName())
                    .status(dept.getStatus())
                    .remark(dept.getRemark())
                    .createTime(dept.getCreateTime())
                    .children(new ArrayList<>())
                    .build();
            map.put(node.getId(), node);
        }
        for (DeptResponse node : map.values()) {
            if (node.getPid() == null || node.getPid() == 0L) {
                roots.add(node);
            } else {
                DeptResponse parent = map.get(node.getPid());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }


    /**
     * 新增部门。
     *
     * @param request 新增部门实体
     */
    public void create(DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setPid(request.getPid());
        dept.setName(request.getName());
        dept.setStatus(request.getStatus());
        dept.setRemark(request.getRemark());
        deptMapper.insert(dept);
    }

    /**
     * 更新部门。
     *
     * @param id            部门 ID
     * @param updateRequest 编辑部门实体
     */
    public void update(Long id, DeptUpdateRequest updateRequest) {
        if (Objects.equals(id, updateRequest.getPid())) {
            throw new ServiceException("上级部门不能设置为自己的父部门");
        }
        SysDept dept = new SysDept();
        dept.setPid(updateRequest.getPid());
        dept.setName(updateRequest.getName());
        dept.setStatus(updateRequest.getStatus());
        dept.setRemark(updateRequest.getRemark());
        dept.setId(id);
        deptMapper.updateById(dept);
    }

    /**
     * 删除部门。
     *
     * @param id 部门 ID
     */
    public void delete(Long id) {
        deptMapper.deleteById(id);
    }
}

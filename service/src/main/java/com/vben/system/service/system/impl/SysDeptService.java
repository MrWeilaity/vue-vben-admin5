package com.vben.system.service.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.system.dept.DeptCreateRequest;
import com.vben.system.dto.system.dept.DeptResponse;
import com.vben.system.dto.system.dept.DeptUpdateRequest;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.service.system.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 部门业务服务，负责部门模块的查询与增删改逻辑。
 */
@Service
@RequiredArgsConstructor
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

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
        checkPidLegitimate(null, request.getPid());
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
        SysDept current = deptMapper.selectById(id);
        if (current == null) {
            throw new ServiceException("部门不存在");
        }

        Long pid = updateRequest.getPid();
        checkPidLegitimate(id, pid);

        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setPid(pid);
        dept.setName(updateRequest.getName());
        dept.setStatus(updateRequest.getStatus());
        dept.setRemark(updateRequest.getRemark());

        int updated = deptMapper.updateById(dept);
        if (updated <= 0) {
            throw new ServiceException("更新部门失败");
        }
    }


    private void checkPidLegitimate(Long id, Long pid) {
        if (pid == null) {
            return;
        }

        if (pid <= 0) {
            throw new ServiceException("请选择合适的上级部门");
        }

        if (Objects.equals(id, pid)) {
            throw new ServiceException("上级部门不能设置为自己");
        }

        SysDept parentDept = deptMapper.selectById(pid);
        if (parentDept == null) {
            throw new ServiceException("上级部门不存在");
        }

        if (isDescendant(pid, id)) {
            throw new ServiceException("上级部门不能设置为自己的子部门");
        }
    }

    private boolean isDescendant(Long pid, Long currentId) {
        Long parentId = pid;
        while (parentId != null) {
            if (Objects.equals(parentId, currentId)) {
                return true;
            }

            SysDept parent = deptMapper.selectById(parentId);
            if (parent == null) {
                break;
            }

            parentId = parent.getPid();
        }
        return false;
    }

    /**
     * 删除部门。
     *
     * @param id 部门 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysDept sysDept = deptMapper.selectById(id);
        if (sysDept == null) {
            throw new ServiceException("部门不存在或已被删除");
        }
        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getPid, id)
        );
        if (childCount != null && childCount > 0) {
            throw new ServiceException("该部门下仍有 " + childCount + " 个子部门，请先删除或迁移子部门后再删除");
        }

        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id)
        );
        if (userCount != null && userCount > 0) {
            throw new ServiceException("该部门下仍有 " + userCount + " 个用户，请先调整用户所属部门后再删除");
        }

        deptMapper.deleteById(id);
    }
}

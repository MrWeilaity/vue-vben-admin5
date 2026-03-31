package com.vben.system.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.entity.SysDept;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.service.system.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门业务服务，负责部门模块的查询与增删改逻辑。
 */
@Service
@RequiredArgsConstructor
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;

    /**
     * 查询部门列表（按 ID 升序）。
     *
     * @return 部门列表
     */
    public List<SysDept> list() {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getId));
    }

    /**
     * 新增部门。
     *
     * @param dept 部门实体
     */
    public void create(SysDept dept) {
        deptMapper.insert(dept);
    }

    /**
     * 更新部门。
     *
     * @param id   部门 ID
     * @param dept 部门实体
     */
    public void update(Long id, SysDept dept) {
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

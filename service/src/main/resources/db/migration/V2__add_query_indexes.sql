-- 查询性能优化索引
-- 只补充当前代码路径中明确使用到、且现有主键/唯一索引无法覆盖的查询方向。

-- 部门树与删除校验
CREATE INDEX IF NOT EXISTS idx_sys_dept_pid ON sys_dept (pid);

-- 菜单树与删除校验
CREATE INDEX IF NOT EXISTS idx_sys_menu_pid ON sys_menu (pid);

-- 用户部门关联校验
CREATE INDEX IF NOT EXISTS idx_sys_user_dept_id ON sys_user (dept_id);

-- 用户-角色关联：主键(user_id, role_id)已覆盖按user_id查询，这里补反向role_id查询
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role (role_id);

-- 角色-菜单关联：主键(role_id, menu_id)已覆盖按role_id查询，这里补反向menu_id查询
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_id ON sys_role_menu (menu_id);

-- 用户-岗位关联：主键(user_id, post_id)已覆盖按user_id查询，这里补反向post_id查询
CREATE INDEX IF NOT EXISTS idx_sys_user_post_post_id ON sys_user_post (post_id);

-- 操作日志分页筛选：success 等值 + occur_time 范围/排序
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_success_occur_time
    ON sys_operation_log (success, occur_time DESC);

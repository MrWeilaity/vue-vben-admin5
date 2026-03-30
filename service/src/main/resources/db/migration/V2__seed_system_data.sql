-- 初始化基础部门
INSERT INTO sys_dept (id, pid, name, status, remark)
VALUES (100, 0, '总部', 1, '系统初始化部门')
ON CONFLICT (id) DO NOTHING;

-- 初始化管理员角色
INSERT INTO sys_role (id, name, status, remark)
VALUES (100, '超级管理员', 1, '系统初始化角色')
ON CONFLICT (id) DO NOTHING;

-- 初始化管理员用户（用户名：admin，密码：123456）
INSERT INTO sys_user (id, username, nickname, password, dept_id, email, mobile, status, data_scope, remark)
VALUES (
    100,
    'admin',
    '系统管理员',
    '$2b$10$/p.16ZtZp5RgrtZ05S0.zOMTyNHmlSg3pAlA6bBBO1Q/.aRcTDn4.',
    100,
    'admin@vben.local',
    '13800000000',
    1,
    1,
    '系统初始化用户'
)
ON CONFLICT (id) DO NOTHING;

-- 建立管理员用户与角色关联
INSERT INTO sys_user_role (user_id, role_id)
VALUES (100, 100)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 初始化权限菜单（用于 /api/auth/codes 返回权限码）
INSERT INTO sys_menu (id, pid, name, path, type, component, auth_code, meta_json, status)
VALUES
    (10001, 0, '系统管理', '/system', 'CATALOG', '', NULL, '{}', 1),
    (10002, 10001, '用户管理', '/system/user', 'MENU', '/system/user/index', 'system:user:list', '{}', 1),
    (10003, 10001, '角色管理', '/system/role', 'MENU', '/system/role/index', 'system:role:list', '{}', 1),
    (10004, 10001, '菜单管理', '/system/menu', 'MENU', '/system/menu/index', 'system:menu:list', '{}', 1),
    (10005, 10001, '部门管理', '/system/dept', 'MENU', '/system/dept/index', 'system:dept:list', '{}', 1)
ON CONFLICT (id) DO NOTHING;

-- 建立角色与菜单关联
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
    (100, 10001),
    (100, 10002),
    (100, 10003),
    (100, 10004),
    (100, 10005)
ON CONFLICT (role_id, menu_id) DO NOTHING;

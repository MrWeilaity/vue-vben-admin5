-- PostgreSQL 初始化脚本（完整结构 + 注释 + 触发器 + 基础数据）

-- =========================
-- 1) 表结构定义
-- =========================

CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pid BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_dept_name UNIQUE (name)
);

COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '部门ID，主键，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_dept.pid IS '父部门ID，0表示根节点';
COMMENT ON COLUMN sys_dept.name IS '部门名称，唯一';
COMMENT ON COLUMN sys_dept.status IS '部门状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_dept.remark IS '部门备注';
COMMENT ON COLUMN sys_dept.create_time IS '创建时间，由数据库触发器写入';
COMMENT ON COLUMN sys_dept.update_time IS '更新时间，由数据库触发器维护';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    permissions JSONB,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_role_name UNIQUE (name)
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '角色ID，主键，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_role.name IS '角色名称，唯一';
COMMENT ON COLUMN sys_role.status IS '角色状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_role.permissions IS '权限ID集合(JSONB)';
COMMENT ON COLUMN sys_role.remark IS '角色备注';
COMMENT ON COLUMN sys_role.create_time IS '创建时间，由数据库触发器写入';
COMMENT ON COLUMN sys_role.update_time IS '更新时间，由数据库触发器维护';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pid BIGINT,
    name VARCHAR(64) NOT NULL,
    path VARCHAR(255),
    type VARCHAR(32) NOT NULL,
    component VARCHAR(255),
    auth_code VARCHAR(255),
    meta_json TEXT,
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_menu_path UNIQUE (path)
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '菜单ID，主键，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_menu.pid IS '父菜单ID，NULL表示根节点';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.path IS '前端路由路径，按钮可为空';
COMMENT ON COLUMN sys_menu.type IS '菜单类型：CATALOG=目录，MENU=菜单，BUTTON=按钮';
COMMENT ON COLUMN sys_menu.component IS '前端组件路径';
COMMENT ON COLUMN sys_menu.auth_code IS '权限标识码';
COMMENT ON COLUMN sys_menu.meta_json IS '菜单元信息JSON字符串';
COMMENT ON COLUMN sys_menu.status IS '菜单状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间，由数据库触发器写入';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间，由数据库触发器维护';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    dept_id BIGINT,
    email VARCHAR(128),
    mobile VARCHAR(32),
    status SMALLINT NOT NULL DEFAULT 1,
    data_scope SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(64),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT fk_sys_user_dept_id FOREIGN KEY (dept_id) REFERENCES sys_dept(id)
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.id IS '用户ID，主键，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_user.username IS '登录用户名，唯一';
COMMENT ON COLUMN sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN sys_user.password IS '密码（加密存储）';
COMMENT ON COLUMN sys_user.dept_id IS '所属部门ID，关联sys_dept.id';
COMMENT ON COLUMN sys_user.email IS '邮箱地址';
COMMENT ON COLUMN sys_user.mobile IS '手机号码';
COMMENT ON COLUMN sys_user.status IS '用户状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_user.data_scope IS '数据权限范围：1=本人数据，2=本部门数据，3=本部门及以下，4=全部数据';
COMMENT ON COLUMN sys_user.remark IS '用户备注';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.create_time IS '创建时间，由数据库触发器写入';
COMMENT ON COLUMN sys_user.update_time IS '更新时间，由数据库触发器维护';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID，关联sys_user.id';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID，关联sys_role.id';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_sys_role_menu_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu_id FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID，关联sys_role.id';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID，关联sys_menu.id';

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_post_name UNIQUE (name)
);

COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.id IS '岗位ID，主键，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_post.name IS '岗位名称，唯一';
COMMENT ON COLUMN sys_post.status IS '岗位状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_post.remark IS '岗位备注';
COMMENT ON COLUMN sys_post.create_time IS '创建时间，由数据库触发器写入';
COMMENT ON COLUMN sys_post.update_time IS '更新时间，由数据库触发器维护';

CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_sys_user_post_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_post_post_id FOREIGN KEY (post_id) REFERENCES sys_post(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_user_post IS '用户-岗位关联表';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID，关联sys_user.id';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID，关联sys_post.id';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occur_time TIMESTAMP NOT NULL,
    operator_user_id BIGINT,
    operator_username VARCHAR(64),
    operator_dept VARCHAR(128),
    module VARCHAR(128) NOT NULL,
    operation_desc VARCHAR(255) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    client_ip VARCHAR(64),
    request_method VARCHAR(16),
    request_url VARCHAR(512),
    request_params TEXT,
    biz_status_code INTEGER,
    http_status_code INTEGER,
    duration_ms BIGINT NOT NULL,
    success SMALLINT NOT NULL,
    error_message VARCHAR(500),
    ext_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    create_time TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_operation_log IS '操作日志表';
COMMENT ON COLUMN sys_operation_log.id IS '主键ID，数据库自动生成（GENERATED ALWAYS）';
COMMENT ON COLUMN sys_operation_log.occur_time IS '发生时间';
COMMENT ON COLUMN sys_operation_log.operator_user_id IS '操作人ID';
COMMENT ON COLUMN sys_operation_log.operator_username IS '操作人姓名';
COMMENT ON COLUMN sys_operation_log.operator_dept IS '所属部门';
COMMENT ON COLUMN sys_operation_log.module IS '操作模块（来自Controller类上的@Tag注解）';
COMMENT ON COLUMN sys_operation_log.operation_desc IS '具体操作描述（来自Controller方法上的@Operation.summary）';
COMMENT ON COLUMN sys_operation_log.action_type IS '动作类型：CREATE=新增，UPDATE=修改，DELETE=删除，IMPORT=导入，EXPORT=导出，OTHER=其他';
COMMENT ON COLUMN sys_operation_log.client_ip IS '客户端IP';
COMMENT ON COLUMN sys_operation_log.request_method IS '请求方法：GET/POST/PUT/PATCH/DELETE';
COMMENT ON COLUMN sys_operation_log.request_url IS '请求URL';
COMMENT ON COLUMN sys_operation_log.request_params IS '请求参数（TEXT，已裁剪且不含文件流）';
COMMENT ON COLUMN sys_operation_log.biz_status_code IS '业务状态码（ApiResponse.code，0=成功，非0=业务失败码）';
COMMENT ON COLUMN sys_operation_log.http_status_code IS 'HTTP状态码（例如200/400/401/403/500）';
COMMENT ON COLUMN sys_operation_log.duration_ms IS '执行耗时（毫秒）';
COMMENT ON COLUMN sys_operation_log.success IS '是否成功：1=成功，0=失败';
COMMENT ON COLUMN sys_operation_log.error_message IS '简短错误提示（不存堆栈）';
COMMENT ON COLUMN sys_operation_log.ext_data IS '兜底扩展字段（JSONB）';
COMMENT ON COLUMN sys_operation_log.create_time IS '日志入库时间';

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_occur_time ON sys_operation_log (occur_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operator_user_id ON sys_operation_log (operator_user_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_module_action ON sys_operation_log (module, action_type);

-- =========================
-- 2) 时间戳触发器函数
-- =========================

CREATE OR REPLACE FUNCTION fn_set_create_update_time()
RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.create_time := CURRENT_TIMESTAMP;
        IF to_jsonb(NEW) ? 'update_time' THEN
            NEW.update_time := CURRENT_TIMESTAMP;
        END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        NEW.create_time := OLD.create_time;
        IF to_jsonb(NEW) ? 'update_time' THEN
            NEW.update_time := CURRENT_TIMESTAMP;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_set_create_update_time() IS '统一维护create_time/update_time的触发器函数';

DROP TRIGGER IF EXISTS trg_sys_dept_set_time ON sys_dept;
CREATE TRIGGER trg_sys_dept_set_time
BEFORE INSERT OR UPDATE ON sys_dept
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

DROP TRIGGER IF EXISTS trg_sys_role_set_time ON sys_role;
CREATE TRIGGER trg_sys_role_set_time
BEFORE INSERT OR UPDATE ON sys_role
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

DROP TRIGGER IF EXISTS trg_sys_menu_set_time ON sys_menu;
CREATE TRIGGER trg_sys_menu_set_time
BEFORE INSERT OR UPDATE ON sys_menu
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

DROP TRIGGER IF EXISTS trg_sys_user_set_time ON sys_user;
CREATE TRIGGER trg_sys_user_set_time
BEFORE INSERT OR UPDATE ON sys_user
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

DROP TRIGGER IF EXISTS trg_sys_post_set_time ON sys_post;
CREATE TRIGGER trg_sys_post_set_time
BEFORE INSERT OR UPDATE ON sys_post
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();


DROP TRIGGER IF EXISTS trg_sys_operation_log_set_create_time ON sys_operation_log;
CREATE TRIGGER trg_sys_operation_log_set_create_time
BEFORE INSERT ON sys_operation_log
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

-- =========================
-- 3) 基础数据（不手动写入ID）
-- =========================

INSERT INTO sys_dept (pid, name, status, remark)
VALUES (0, '总部', 1, '系统初始化部门')
ON CONFLICT (name) DO NOTHING;

INSERT INTO sys_role (name, status, remark)
VALUES ('超级管理员', 1, '系统初始化角色')
ON CONFLICT (name) DO NOTHING;

INSERT INTO sys_post (name, status, remark)
VALUES ('系统管理员岗位', 1, '系统初始化岗位')
ON CONFLICT (name) DO NOTHING;

INSERT INTO sys_user (username, nickname, password, dept_id, email, mobile, status, data_scope, remark)
SELECT
    'admin',
    '系统管理员',
    '$2b$10$/p.16ZtZp5RgrtZ05S0.zOMTyNHmlSg3pAlA6bBBO1Q/.aRcTDn4.',
    d.id,
    'admin@vben.local',
    '13800000000',
    1,
    1,
    '系统初始化用户'
FROM sys_dept d
WHERE d.name = '总部'
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
VALUES (
    NULL,
    'System',
    '/system',
    'catalog',
    NULL,
    NULL,
    '{"icon":"carbon:settings","order":9997,"title":"system.title","badge":"new","badgeType":"normal","badgeVariants":"primary"}',
    1
)
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id, 'SystemMenu', '/system/menu', 'menu', '/system/menu/list', 'System:Menu:List', '{"icon":"carbon:menu","title":"system.menu.title"}', 1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id, 'SystemDept', '/system/dept', 'menu', '/system/dept/list', 'System:Dept:List', '{"icon":"carbon:container-services","title":"system.dept.title"}', 1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemMenuCreate', NULL, 'button', NULL, 'System:Menu:Create', '{"title":"common.create"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/menu'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Menu:Create');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemMenuEdit', NULL, 'button', NULL, 'System:Menu:Edit', '{"title":"common.edit"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/menu'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Menu:Edit');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemMenuDelete', NULL, 'button', NULL, 'System:Menu:Delete', '{"title":"common.delete"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/menu'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Menu:Delete');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDeptCreate', NULL, 'button', NULL, 'System:Dept:Create', '{"title":"common.create"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dept'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dept:Create');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDeptEdit', NULL, 'button', NULL, 'System:Dept:Edit', '{"title":"common.edit"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dept'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dept:Edit');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDeptDelete', NULL, 'button', NULL, 'System:Dept:Delete', '{"title":"common.delete"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dept'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dept:Delete');

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.name = '超级管理员'
WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.path IN ('/system', '/system/menu', '/system/dept')
   OR m.auth_code IN (
       'System:Menu:List',
       'System:Menu:Create',
       'System:Menu:Edit',
       'System:Menu:Delete',
       'System:Dept:List',
       'System:Dept:Create',
       'System:Dept:Edit',
       'System:Dept:Delete'
   )
WHERE r.name = '超级管理员'
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO sys_user_post (user_id, post_id)
SELECT u.id, p.id
FROM sys_user u
JOIN sys_post p ON p.name = '系统管理员岗位'
WHERE u.username = 'admin'
ON CONFLICT (user_id, post_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    login_ip VARCHAR(64) NOT NULL,
    login_address VARCHAR(255),
    browser VARCHAR(64),
    os VARCHAR(64),
    status SMALLINT NOT NULL,
    operation_msg VARCHAR(500),
    login_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_login_log IS '登录日志表';
COMMENT ON COLUMN sys_login_log.id IS '主键ID';
COMMENT ON COLUMN sys_login_log.username IS '登录名';
COMMENT ON COLUMN sys_login_log.login_ip IS '登录IP';
COMMENT ON COLUMN sys_login_log.login_address IS '登录地点';
COMMENT ON COLUMN sys_login_log.browser IS '浏览器';
COMMENT ON COLUMN sys_login_log.os IS '操作系统';
COMMENT ON COLUMN sys_login_log.status IS '登录状态：1成功 0失败';
COMMENT ON COLUMN sys_login_log.operation_msg IS '操作信息（成功=登录成功，失败=失败原因）';
COMMENT ON COLUMN sys_login_log.login_time IS '登录时间';
COMMENT ON COLUMN sys_login_log.create_time IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_sys_login_log_login_time ON sys_login_log (login_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_username ON sys_login_log (username);

DROP TRIGGER IF EXISTS trg_sys_login_log_set_create_time ON sys_login_log;
CREATE TRIGGER trg_sys_login_log_set_create_time
BEFORE INSERT ON sys_login_log
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id, 'SystemLoginLog', '/system/login-log', 'menu', '/system/login-log/list', 'System:LoginLog:List', '{"icon":"mdi:login-variant","title":"system.loginLog.title"}', 1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id, 'SystemOnline', '/system/online', 'menu', '/system/online/list', 'System:Online:List', '{"icon":"mdi:account-clock","title":"system.online.title"}', 1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemOnlineOffline', NULL, 'button', NULL, 'System:Online:Offline', '{"title":"system.online.offline"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/online'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Online:Offline');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.path IN ('/system/login-log', '/system/online')
   OR m.auth_code IN ('System:LoginLog:List', 'System:Online:List', 'System:Online:Offline')
WHERE r.name = 'super'
ON CONFLICT (role_id, menu_id) DO NOTHING;

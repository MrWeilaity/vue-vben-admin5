-- 字典管理模块：类型表 + 字典项表 + 索引 + 菜单权限初始化

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    cache_enabled SMALLINT NOT NULL DEFAULT 1,
    built_in SMALLINT NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_sys_dict_type_code UNIQUE (code)
);

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '主键ID';
COMMENT ON COLUMN sys_dict_type.name IS '字典名称（显示给管理员）';
COMMENT ON COLUMN sys_dict_type.code IS '字典编码（业务与前端按此读取）';
COMMENT ON COLUMN sys_dict_type.status IS '状态：1启用 0停用';
COMMENT ON COLUMN sys_dict_type.cache_enabled IS '是否启用缓存：1是 0否';
COMMENT ON COLUMN sys_dict_type.built_in IS '是否系统内置：1是 0否（内置默认不允许删除）';
COMMENT ON COLUMN sys_dict_type.sort_order IS '类型排序，升序';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_id BIGINT NOT NULL,
    type_code VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    is_default SMALLINT NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    tag_type VARCHAR(32),
    tag_class VARCHAR(64),
    css_style VARCHAR(255),
    ext_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_sys_dict_data_type_id FOREIGN KEY (type_id) REFERENCES sys_dict_type(id) ON DELETE CASCADE,
    CONSTRAINT uk_sys_dict_data_type_value UNIQUE (type_code, value)
);

COMMENT ON TABLE sys_dict_data IS '字典项表';
COMMENT ON COLUMN sys_dict_data.id IS '主键ID';
COMMENT ON COLUMN sys_dict_data.type_id IS '字典类型ID，关联sys_dict_type.id';
COMMENT ON COLUMN sys_dict_data.type_code IS '冗余字典编码，提升按编码查询性能';
COMMENT ON COLUMN sys_dict_data.label IS '展示文本';
COMMENT ON COLUMN sys_dict_data.value IS '字典值（业务表持久化该值）';
COMMENT ON COLUMN sys_dict_data.status IS '状态：1启用 0停用';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认项：1是 0否';
COMMENT ON COLUMN sys_dict_data.sort_order IS '排序，升序';
COMMENT ON COLUMN sys_dict_data.tag_type IS '前端标签类型（success/warning/error等）';
COMMENT ON COLUMN sys_dict_data.tag_class IS '前端标签class扩展';
COMMENT ON COLUMN sys_dict_data.css_style IS '前端行内样式扩展';
COMMENT ON COLUMN sys_dict_data.ext_json IS '扩展属性JSON';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_sys_dict_type_status_sort ON sys_dict_type (status, sort_order, id);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type_status_sort ON sys_dict_data (type_code, status, sort_order, id);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type_id ON sys_dict_data (type_id);

DROP TRIGGER IF EXISTS trg_sys_dict_type_set_time ON sys_dict_type;
CREATE TRIGGER trg_sys_dict_type_set_time
BEFORE INSERT OR UPDATE ON sys_dict_type
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

DROP TRIGGER IF EXISTS trg_sys_dict_data_set_time ON sys_dict_data;
CREATE TRIGGER trg_sys_dict_data_set_time
BEFORE INSERT OR UPDATE ON sys_dict_data
FOR EACH ROW
EXECUTE FUNCTION fn_set_create_update_time();

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id, 'SystemDict', '/system/dict', 'menu', '/system/dict/list', 'System:Dict:List', '{"icon":"mdi:book-settings","title":"system.dict.title"}', 1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDictCreate', NULL, 'button', NULL, 'System:Dict:Create', '{"title":"common.create"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dict'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dict:Create');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDictEdit', NULL, 'button', NULL, 'System:Dict:Edit', '{"title":"common.edit"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dict'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dict:Edit');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDictDelete', NULL, 'button', NULL, 'System:Dict:Delete', '{"title":"common.delete"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dict'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dict:Delete');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id, 'SystemDictRefresh', NULL, 'button', NULL, 'System:Dict:Refresh', '{"title":"system.dict.refreshCache"}', 1
FROM sys_menu parent
WHERE parent.path = '/system/dict'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Dict:Refresh');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.path IN ('/system/dict')
   OR m.auth_code IN ('System:Dict:List', 'System:Dict:Create', 'System:Dict:Edit', 'System:Dict:Delete', 'System:Dict:Refresh')
WHERE r.name = 'super'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 五种数据权限：1=全部数据，2=自定义数据，3=本部门数据，4=本部门及以下数据，5=仅本人数据

ALTER TABLE sys_user
    ALTER COLUMN data_scope SET DEFAULT 5;

COMMENT ON COLUMN sys_user.data_scope IS '数据权限范围：1=全部数据，2=自定义数据，3=本部门数据，4=本部门及以下数据，5=仅本人数据';

ALTER TABLE sys_role
    ADD COLUMN IF NOT EXISTS data_scope SMALLINT NOT NULL DEFAULT 5;

COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围：1=全部数据，2=自定义数据，3=本部门数据，4=本部门及以下数据，5=仅本人数据';

CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id),
    CONSTRAINT fk_sys_role_dept_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_dept_dept_id FOREIGN KEY (dept_id) REFERENCES sys_dept(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_role_dept IS '角色-数据权限部门关联表';
COMMENT ON COLUMN sys_role_dept.role_id IS '角色ID，关联sys_role.id';
COMMENT ON COLUMN sys_role_dept.dept_id IS '部门ID，关联sys_dept.id';

CREATE TABLE IF NOT EXISTS sys_user_dept (
    user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, dept_id),
    CONSTRAINT fk_sys_user_dept_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_dept_dept_id FOREIGN KEY (dept_id) REFERENCES sys_dept(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_user_dept IS '用户-数据权限部门关联表';
COMMENT ON COLUMN sys_user_dept.user_id IS '用户ID，关联sys_user.id';
COMMENT ON COLUMN sys_user_dept.dept_id IS '部门ID，关联sys_dept.id';

UPDATE sys_role
SET data_scope = 1
WHERE name = 'super';

CREATE INDEX IF NOT EXISTS idx_sys_role_dept_dept_id ON sys_role_dept (dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_dept_dept_id ON sys_user_dept (dept_id);

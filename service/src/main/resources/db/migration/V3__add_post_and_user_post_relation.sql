-- 初始化：岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.id IS '岗位ID';
COMMENT ON COLUMN sys_post.name IS '岗位名称';
COMMENT ON COLUMN sys_post.status IS '状态：1启用，0禁用';
COMMENT ON COLUMN sys_post.remark IS '备注';
COMMENT ON COLUMN sys_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_post.update_time IS '更新时间';

-- 初始化：用户岗位关联表
CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id)
);

COMMENT ON TABLE sys_user_post IS '用户-岗位关联表';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';

-- 初始化基础岗位
INSERT INTO sys_post (id, name, status, remark)
VALUES (100, '系统管理员岗位', 1, '系统初始化岗位')
ON CONFLICT (id) DO NOTHING;

-- 建立管理员用户与岗位关联
INSERT INTO sys_user_post (user_id, post_id)
VALUES (100, 100)
ON CONFLICT (user_id, post_id) DO NOTHING;

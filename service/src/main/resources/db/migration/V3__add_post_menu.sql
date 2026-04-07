INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT root.id,
       'SystemPost',
       '/system/post',
       'menu',
       '/system/post/list',
       'System:Post:List',
       '{"icon":"carbon:user-multiple","title":"system.post.title"}',
       1
FROM sys_menu root
WHERE root.path = '/system'
ON CONFLICT (path) DO NOTHING;

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id,
       'SystemPostCreate',
       NULL,
       'button',
       NULL,
       'System:Post:Create',
       '{"title":"common.create"}',
       1
FROM sys_menu parent
WHERE parent.path = '/system/post'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Post:Create');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id,
       'SystemPostEdit',
       NULL,
       'button',
       NULL,
       'System:Post:Edit',
       '{"title":"common.edit"}',
       1
FROM sys_menu parent
WHERE parent.path = '/system/post'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Post:Edit');

INSERT INTO sys_menu (pid, name, path, type, component, auth_code, meta_json, status)
SELECT parent.id,
       'SystemPostDelete',
       NULL,
       'button',
       NULL,
       'System:Post:Delete',
       '{"title":"common.delete"}',
       1
FROM sys_menu parent
WHERE parent.path = '/system/post'
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.auth_code = 'System:Post:Delete');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.path = '/system/post'
 OR m.auth_code IN (
     'System:Post:List',
     'System:Post:Create',
     'System:Post:Edit',
     'System:Post:Delete'
 )
WHERE r.name = '超级管理员'
ON CONFLICT (role_id, menu_id) DO NOTHING;

BEGIN;

-- 1. System user (environments NULL first)
INSERT INTO "user" (id, username,  current_environment, default_group_id)
VALUES ('00000000-0000-0000-0000-000000000000', 'system', NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- 2. Root nested group — owner is REQUIRED
INSERT INTO "nested_group" (id, name, parent_group_id, owner)
VALUES ('00000000-0000-0000-0000-000000000001',
        'Root System Group',
        NULL,
        '00000000-0000-0000-0000-000000000000')
ON CONFLICT (id) DO NOTHING;

-- 3. Point system user at the tenant
UPDATE "user"
SET current_environment = '00000000-0000-0000-0000-000000000001',
    default_group_id    = '00000000-0000-0000-0000-000000000001'
WHERE id = '00000000-0000-0000-0000-000000000000';

-- 4. Default registration fallback
INSERT INTO system_setting (key, value, description)
VALUES ('DEFAULT_REGISTRATION_GROUP_ID',
        '00000000-0000-0000-0000-000000000001',
        'Fallback group for users registering without an invite code')
ON CONFLICT (key) DO NOTHING;

-- 5. Action + system permission
INSERT INTO action (id, name)
VALUES ('a0000000-0000-0000-0000-000000000001', 'MANAGE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO permission (id, name, action_id, is_composite)
VALUES ('10000000-0000-0000-0000-000000000001',
        'MANAGE_SYSTEM_PERMISSIONS',
        'a0000000-0000-0000-0000-000000000001',
        false)
ON CONFLICT (id) DO NOTHING;



-- 7. System admins group inside the root tenant
INSERT INTO user_group (id, nested_group_id, name)
VALUES ('40000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'System Admins Group')
ON CONFLICT (id) DO NOTHING;

INSERT INTO "user" (id, username, current_environment, default_group_id)
VALUES ('68e571d5-3b66-41eb-a4ef-5db6992827b7',
        'a@a',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_info (user_id, email, name, password)
VALUES ('68e571d5-3b66-41eb-a4ef-5db6992827b7',
        'a@a',
        'a@a',
        '$2a$12$fXote57ba5iNKkETS9Z8vuw7wutBA8kLsuxCeQA8chAZNwgyn8lNu')
ON CONFLICT (user_id) DO NOTHING;

-- 8. Your real user joins that group
INSERT INTO group_member (nested_group_id, user_id, group_id)
VALUES ('00000000-0000-0000-0000-000000000001',
        '68e571d5-3b66-41eb-a4ef-5db6992827b7',
        '40000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- 9. Grant the permission to that group via group_permission
INSERT INTO group_permission
(nested_group_id, permission_id, owner_users_group, target_users_group)
VALUES
    ('00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     '40000000-0000-0000-0000-000000000001',
     NULL)
ON CONFLICT DO NOTHING;

INSERT INTO action (id, name) VALUES
                                  ('a0000000-0000-0000-0000-000000000010', 'CREATE'),
                                  ('a0000000-0000-0000-0000-000000000011', 'VIEW'),
                                  ('a0000000-0000-0000-0000-000000000012', 'UPDATE'),
                                  ('a0000000-0000-0000-0000-000000000013', 'DELETE'),
                                  ('a0000000-0000-0000-0000-000000000001', 'MANAGE')
ON CONFLICT (id) DO NOTHING;

COMMIT;
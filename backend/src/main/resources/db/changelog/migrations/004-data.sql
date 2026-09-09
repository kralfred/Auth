BEGIN;

-- 1. Create Default System User with NULL environments to bypass immediate FK checks
INSERT INTO public."user" (id, username, default_group_id, current_environment)
VALUES (
           '00000000-0000-0000-0000-000000000000',
           'system',
           NULL,
           NULL
       )
ON CONFLICT (id) DO NOTHING;

-- 2. Create Default Nested Group owned by the system user
INSERT INTO public."nested_group" (id, name, parent_group_id, owner)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Default Workspace',
           NULL,
           '00000000-0000-0000-0000-000000000000'
       )
ON CONFLICT (id) DO NOTHING;

-- 3. Link the System User to the created Default Workspace
UPDATE public."user"
SET current_environment = '00000000-0000-0000-0000-000000000001',
    default_group_id = '00000000-0000-0000-0000-000000000001'
WHERE id = '00000000-0000-0000-0000-000000000000';

-- 4. Seed the registration setting
INSERT INTO public.system_setting (key, value, description)
VALUES (
           'DEFAULT_REGISTRATION_GROUP_ID',
           '00000000-0000-0000-0000-000000000001',
           'The fallback group assigned to users registering without an invite code'
       )
ON CONFLICT (key) DO NOTHING;

COMMIT;


-- 1. Create the Root System Group (must match ROOT_SYSTEM_GROUP_ID)
INSERT INTO nested_group (id, name, parent_group_id)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Root System Group',
           NULL
       )
ON CONFLICT (id) DO NOTHING;

-- 2. Create the System Admin Action (e.g., 'MANAGE')
INSERT INTO action (id, name)
VALUES (
           'a0000000-0000-0000-0000-000000000001',
           'MANAGE'
       )
ON CONFLICT (id) DO NOTHING;

-- 3. Create the 'MANAGE_SYSTEM_PERMISSIONS' Permission
INSERT INTO permission (id, name, action_id, is_composite)
VALUES (
           '10000000-0000-0000-0000-000000000001',
           'MANAGE_SYSTEM_PERMISSIONS',
           'a0000000-0000-0000-0000-000000000001',
           false
       )
ON CONFLICT (id) DO NOTHING;



-- 5. Create a user group for the Root Group
INSERT INTO user_group (id, nested_group_id, name)
VALUES (
           '40000000-0000-0000-0000-000000000001',
           '00000000-0000-0000-0000-000000000001',
           'System Admins Group'
       )
ON CONFLICT (id) DO NOTHING;

-- 6. Add YOUR user to group_member in the Root Group
-- Replace 'YOUR_USER_UUID' with your actual currentUserId UUID
INSERT INTO group_member (group_id, nested_group_id, user_id)
VALUES (
           '40000000-0000-0000-0000-000000000001',
           '00000000-0000-0000-0000-000000000001',
           '68e571d5-3b66-41eb-a4ef-5db6992827b7'
       )
ON CONFLICT DO NOTHING;


SELECT
    gm.user_id,
    gm.nested_group_id AS gm_group_id,
    pa.nested_group_id AS pa_group_id,
    p.name AS permission_name,
    ng.id AS root_group_id
FROM group_member gm
         LEFT JOIN permission_attribute pa ON pa.nested_group_id = gm.nested_group_id
         LEFT JOIN permission p ON p.id = pa.permission_id
         LEFT JOIN nested_group ng ON ng.id = gm.nested_group_id
WHERE gm.user_id = '68e571d5-3b66-41eb-a4ef-5db6992827b7';


INSERT INTO "permission_attribute" (
    "nested_group_id",
    "permission_id",
    "targetable_attribute_id",
    "is_required",
    "auto_fill_value"
)
VALUES (
           '00000000-0000-0000-0000-000000000001', -- Root System Group
           '10000000-0000-0000-0000-000000000001', -- MANAGE_SYSTEM_PERMISSIONS
           NULL,                                  -- Action-only permission (no attribute target)
           false,
           ''
       )
ON CONFLICT DO NOTHING;

ALTER TABLE entity_type
    ADD CONSTRAINT uq_entity_type_name UNIQUE (name);
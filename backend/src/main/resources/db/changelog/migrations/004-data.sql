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

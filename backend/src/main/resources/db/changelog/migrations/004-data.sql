SELECT table_name
FROM information_schema.tables
WHERE table_name LIKE '%group_invite_target%';


SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_name IN ('group_invite', 'group_invite_target')
  AND column_name IN ('id', 'invite_id');

SELECT u.nested_group_id FROM group_invite_target u
                                  JOIN group_invite o ON u.invite_id = o.id
WHERE o.code = ?
  AND u.is_primary = true
LIMIT 1

BEGIN;

SET CONSTRAINTS ALL DEFERRED;

INSERT INTO public.nested_group (id, name, owner)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default Group', '00000000-0000-0000-0000-000000000000')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.user (id, username, current_environment)
VALUES ('00000000-0000-0000-0000-000000000000', 'Default User', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

COMMIT;


ALTER TABLE public.user_info
    ADD COLUMN custom_info_id UUID;

-- Run once in your PostgreSQL schema migration
ALTER TABLE public."user"
    DROP CONSTRAINT user_current_environment_fkey,
    ADD CONSTRAINT user_current_environment_fkey
        FOREIGN KEY (current_environment) REFERENCES public.nested_group(id)
            DEFERRABLE INITIALLY DEFERRED;


-- Seed the default group setting
INSERT INTO public.system_setting (key, value, description)
VALUES (
           'DEFAULT_REGISTRATION_GROUP_ID',
           '00000000-0000-0000-0000-000000000001',
           'The fallback group assigned to users registering without an invite code'
       )
ON CONFLICT (key) DO NOTHING;

-- 1. Insert system user with NULL environment to bypass FK check
INSERT INTO public."user" (id, username, current_environment, default_group_id)
VALUES ('00000000-0000-0000-0000-000000000000', 'system', NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert default group owned by system user (system user exists now!)
INSERT INTO public."nested_group" (id, name, parent_group_id, owner)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default Workspace', NULL, '00000000-0000-0000-0000-000000000000')
ON CONFLICT (id) DO NOTHING;

-- 3. Update system user to point to default group (default group exists now!)
UPDATE public."user"
SET current_environment = '00000000-0000-0000-0000-000000000001',
    default_group_id = '00000000-0000-0000-0000-000000000001'
WHERE id = '00000000-0000-0000-0000-000000000000';

SELECT pid, usename, state, query, age(clock_timestamp(), query_start)
FROM pg_stat_activity
WHERE state != 'idle' AND pid != pg_backend_pid();

SELECT * FROM user_info

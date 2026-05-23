CREATE OR REPLACE PROCEDURE sp_register_user(
    p_username VARCHAR,
    p_email VARCHAR,
    p_password VARCHAR
) LANGUAGE plpgsql AS $$
DECLARE
    v_user_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO app_user (id, username, email, password)
    VALUES (v_user_id, p_username, p_email, p_password);
END;
$$;

-- PROCEDURE: Adds a user to a group with a specific role and logs it
CREATE OR REPLACE PROCEDURE p_add_group_member(
    p_user_id UUID,
    p_group_id UUID,
    p_role_name VARCHAR
)
    LANGUAGE plpgsql AS $$
DECLARE
    v_role_id UUID;
BEGIN
    -- 1. Find the role ID based on the name
    SELECT id INTO v_role_id FROM group_roles WHERE name = p_role_name;

    -- 2. Insert or Update the membership
    INSERT INTO group_members (group_id, user_id, group_role_id)
    VALUES (p_group_id, p_user_id, v_role_id)
    ON CONFLICT (group_id, user_id) DO UPDATE SET group_role_id = v_role_id;

    -- 3. Automatically log the action
    INSERT INTO group_logs (group_id, message, severity)
    VALUES (p_group_id, 'User ' || p_user_id || ' assigned role ' || p_role_name, 'INFO');
END;
$$;


CREATE OR REPLACE FUNCTION fn_get_user_by_email(p_email TEXT)
    RETURNS SETOF app_user AS $$ -- This tells Postgres to return the table's structure
BEGIN
    RETURN QUERY
        SELECT * FROM app_user
        WHERE email = p_email
        LIMIT 1;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE sp_global_logout(
    p_user_id UUID,
    OUT p_count_revoked INTEGER
)
    LANGUAGE plpgsql
AS $$
BEGIN
    -- Count how many tokens exist for this owner
    SELECT COUNT(*) INTO p_count_revoked
    FROM user_token
    WHERE owner_id = p_user_id;

    -- Delete the tokens
    DELETE FROM user_token WHERE owner_id = p_user_id;

EXCEPTION WHEN OTHERS THEN
    p_count_revoked := 0;
    RAISE NOTICE 'Failed to log out all devices for user %', p_user_id;
-- In procedures, the transaction will automatically abort on unhandled exceptions.
END;
$$;
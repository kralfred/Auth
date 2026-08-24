CREATE OR REPLACE FUNCTION fn_verify_user_credentials(
    p_username VARCHAR
)
    RETURNS TABLE (
                      user_id UUID,
                      password_hash VARCHAR
                  ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            u.id AS user_id,
            ui.password,
            u.current_environment
        FROM "user" u
                 JOIN "user_info" ui ON u.id = ui.user_id
        WHERE u.username = p_username;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_register_user(
    p_username VARCHAR,
    p_email VARCHAR,
    p_name VARCHAR,
    p_password_hash VARCHAR
)
    RETURNS UUID AS $$
DECLARE
    v_user_id UUID;
BEGIN
    -- 1. Insert into "user" table (id auto-generates via gen_random_uuid())
    INSERT INTO "user" ("username")
    VALUES (p_username)
    RETURNING "id" INTO v_user_id;

    -- 2. Insert corresponding profile and hashed password into "user_info"
    INSERT INTO "user_info" ("user_id", "email", "name", "password")
    VALUES (v_user_id, p_email, p_name, p_password_hash);

    -- 3. Return the newly created user ID
    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_get_entity_access(
    p_user_id UUID,
    p_group_id UUID
)
    RETURNS TABLE (entity_type VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT DISTINCT e.name
        FROM "group_member" gm
                 JOIN "group_permission" gp ON gm.group_id = gp.owner_users_group
                 JOIN "permission" p ON gp.permission_id = p.id
                 JOIN "targetable_attribute" ta ON p.id = ta.id
                 JOIN "entity_type" e ON ta.entity_type_id = e.id
        WHERE gm.user_id = p_user_id
          AND gm.group_id = p_group_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_check_username_email(
    p_username UUID,
    p_email UUID
)
    RETURNS TABLE (entity_type VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT DISTINCT e.name
        FROM "group_member" gm
                 JOIN "group_permission" gp ON gm.group_id = gp.owner_users_group
                 JOIN "permission" p ON gp.permission_id = p.id
                 JOIN "targetable_attribute" ta ON p.id = ta.id
                 JOIN "entity_type" e ON ta.entity_type_id = e.id
        WHERE gm.user_id = p_user_id
          AND gm.group_id = p_group_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_validate_and_get_session(
    p_token_hash VARCHAR
)
    RETURNS TABLE (
                      session_id UUID,
                      user_id UUID,
                      username VARCHAR,
                      dpop_jkt VARCHAR,
                      is_valid BOOLEAN
                  ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            s.id AS session_id,
            s.user_id,
            u.username,
            s.dpop_jkt,
            (s.is_active AND NOT rt.is_revoked AND rt.expires_at > CURRENT_TIMESTAMP) AS is_valid
        FROM refresh_token rt
                 JOIN session s ON rt.session_id = s.id
                 JOIN "user" u ON s.user_id = u.id
        WHERE rt.token_hash = p_token_hash;
END;
$$ LANGUAGE plpgsql;
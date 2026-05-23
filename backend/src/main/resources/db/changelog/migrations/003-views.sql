CREATE OR REPLACE VIEW v_user_access_summary AS
SELECT
    pgm.user_id AS actor_id,
    u.id AS target_id,
    u.username AS target_username,
    -- Check if they have specific administrative permissions
    EXISTS (
        SELECT 1
        FROM permission_group_rules pgr
        WHERE pgr.permission_group_id = pgm.permission_group_id
          AND pgr.permission_name NOT IN ('view_user', 'VIEW')
    ) AS has_more_details
FROM app_user u
-- Join through the new relationship chain
         JOIN entity_group eg ON eg.name = u.username -- Or however you group your users
         JOIN permission_group_rules pgr ON pgr.target_entity_group_id = eg.id
         JOIN permission_group_members pgm ON pgm.permission_group_id = pgr.permission_group_id;


CREATE OR REPLACE VIEW v_nested_group_admin_view AS
SELECT
    ng.id,
    ng.name,
    ng.description,
    ng.parent_id,
    (SELECT array_agg(user_id) FROM nested_group_members WHERE nested_group_id = ng.id) as member_ids,
    (SELECT array_agg(id) FROM group_roles WHERE nested_group_id = ng.id) as role_ids
FROM nested_group ng;


CREATE OR REPLACE VIEW v_user_global_capabilities AS
SELECT
    u.id AS user_id,
    EXISTS (
        SELECT 1
        FROM permission_group_members pgm
                 JOIN permission_group_rules pgr ON pgm.permission_group_id = pgr.permission_group_id
        WHERE pgm.user_id = u.id
          AND pgr.permission_name = 'VIEW_USER_LIST'
    ) AS can_view_user_list
FROM app_user u;


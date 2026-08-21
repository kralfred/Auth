
CREATE OR REPLACE VIEW vw_entity_access AS
SELECT DISTINCT
    gm.user_id,
    e.name AS entity_type
FROM "group_member" gm
         JOIN "group_permission" gp ON gm.group_id = gp.owner_users_group
         JOIN "permission" p ON gp.permission_id = p.id
         JOIN "entity_type" e ON p.entity_type_id = e.id;

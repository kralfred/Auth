-- 1. Register the Permission Action in the Registry
-- Your schema requires this before the rule can be created
INSERT INTO permission_registry (name, description)
VALUES ('VIEW_ALL', 'Allows viewing of all entities within the assigned category');

-- 2. Create the Nested Group
INSERT INTO nested_group (id, name, description)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Project Alpha', 'Main project group');

-- 3. Create the Entity Group (Category: LOG)
INSERT INTO entity_group (id, nested_group_id, name)
VALUES ('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', 'LOG');

-- 4. Create a Permission Group
INSERT INTO permission_group (id, name, nested_group_id)
VALUES ('770e8400-e29b-41d4-a716-446655440000', 'Auditor Group', '550e8400-e29b-41d4-a716-446655440000');

-- 5. Add the Rule (Linking the Registry Action to the Entity Category)
INSERT INTO permission_group_rules (permission_group_id, permission_name, target_entity_group_id)
VALUES ('770e8400-e29b-41d4-a716-446655440000', 'VIEW_ALL', '660e8400-e29b-41d4-a716-446655440000');

-- 6. Add your User to the Permission Group
-- Replace :userId with your actual test user UUID
INSERT INTO permission_group_members (permission_group_id, user_id)
VALUES ('770e8400-e29b-41d4-a716-446655440000', '88b4ff64-f39c-4c3e-9bd0-af114dce7ef3');
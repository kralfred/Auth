
CREATE TABLE IF NOT EXISTS app_user (
                                        id UUID PRIMARY KEY,
                                        username VARCHAR(50) NOT NULL UNIQUE,
                                        email VARCHAR(100) NOT NULL UNIQUE,
                                        password VARCHAR(255) NOT NULL
);


CREATE TABLE IF NOT EXISTS nested_group (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            name VARCHAR(100) UNIQUE NOT NULL,
                                            parent_id UUID REFERENCES nested_group(id) ON DELETE CASCADE,
                                            description TEXT
);


CREATE TABLE IF NOT EXISTS permission_registry (
                                                   name VARCHAR(100) PRIMARY KEY,
                                                   description TEXT
);


CREATE TABLE IF NOT EXISTS entity_group (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            nested_group_id UUID REFERENCES nested_group(id) ON DELETE CASCADE,
                                            name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS nested_group_members (
                                                    nested_group_id UUID REFERENCES nested_group(id) ON DELETE CASCADE,
                                                    user_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
                                                    role_id UUID REFERENCES group_roles(id) ON DELETE SET NULL,
                                                    PRIMARY KEY (nested_group_id, user_id)
);

CREATE TABLE IF NOT EXISTS permission_group (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                nested_group_id UUID REFERENCES nested_group(id) ON DELETE CASCADE,
                                                name VARCHAR(100) NOT NULL,
                                                UNIQUE (nested_group_id, name)
);


CREATE TABLE IF NOT EXISTS permission_group_rules (
                                                      permission_group_id UUID REFERENCES permission_group(id) ON DELETE CASCADE,
                                                      permission_name VARCHAR(100) REFERENCES permission_registry(name),
                                                      target_entity_group_id UUID REFERENCES entity_group(id), -- Scopes the action to specific users
                                                      PRIMARY KEY (permission_group_id, permission_name, target_entity_group_id)
);


CREATE TABLE IF NOT EXISTS permission_group_members (
                                                        permission_group_id UUID REFERENCES permission_group(id) ON DELETE CASCADE,
                                                        user_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
                                                        PRIMARY KEY (permission_group_id, user_id)
);
CREATE TABLE IF NOT EXISTS group_roles (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           nested_group_id UUID REFERENCES nested_group(id) ON DELETE CASCADE,
                                           name VARCHAR(50) NOT NULL,
                                           UNIQUE (nested_group_id, name)
);

-- This table "packages" Permission Groups into a Role.
-- When a user gets this Role, they get all groups linked to it.
CREATE TABLE IF NOT EXISTS group_role_permission_groups (
                                                            role_id UUID REFERENCES group_roles(id) ON DELETE CASCADE,
                                                            permission_group_id UUID REFERENCES permission_group(id) ON DELETE CASCADE,
                                                            PRIMARY KEY (role_id, permission_group_id)
);

CREATE TABLE IF NOT EXISTS api_log (
                                       id UUID PRIMARY KEY,
                                       user_id UUID REFERENCES app_user(id),
                                       nested_group_id UUID REFERENCES nested_group(id),
                                       action TEXT,
                                       status TEXT,
                                       duration_ms BIGINT,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS jwt_token (
                                         id         uuid         not null
                                             primary key,
                                         expiration timestamp(6) not null,
                                         message    text,
                                         owner_id   uuid         not null
);

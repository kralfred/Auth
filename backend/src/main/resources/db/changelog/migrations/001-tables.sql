-- =========================================================================
-- EXTENSIONS
-- =========================================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- for gen_random_uuid() on PG < 13


-- =========================================================================
-- IDENTITY & USER
-- =========================================================================
CREATE TABLE IF NOT EXISTS "user" (
                        "id"                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        "username"            VARCHAR(100) NOT NULL,
                        "current_environment" UUID,
                        "default_group_id" UUID
);

CREATE TABLE IF NOT EXISTS "user_info" (
                             "user_id"  UUID PRIMARY KEY REFERENCES "user"("id") ON DELETE CASCADE,
                             "email"    VARCHAR(255) UNIQUE NOT NULL,
                             "name"     VARCHAR(100),
                             "password" VARCHAR(100)
);

-- Catalog of custom field definitions; per-user values live in user_custom_info.
CREATE TABLE IF NOT EXISTS "custom_info" (
                               "id"   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               "name" VARCHAR(255) UNIQUE NOT NULL,
                               "description" TEXT
);

CREATE TABLE IF NOT EXISTS "user_custom_info" (
                                    "user_id"        UUID NOT NULL REFERENCES "user_info"("user_id") ON DELETE CASCADE,
                                    "custom_info_id" UUID NOT NULL REFERENCES "custom_info"("id")     ON DELETE CASCADE,
                                    "value"          VARCHAR(255),
                                    PRIMARY KEY ("user_id", "custom_info_id")
);

-- External identity providers. Unique per (user, provider), not globally.
CREATE TABLE IF NOT EXISTS "user_identity" (
                                 "user_id"     UUID NOT NULL REFERENCES "user"("id") ON DELETE CASCADE,
                                 "provider"    VARCHAR(255) NOT NULL,
                                 "provider_id" VARCHAR(100),
                                 PRIMARY KEY ("user_id", "provider"),
                                 UNIQUE ("provider", "provider_id")
);

CREATE TABLE IF NOT EXISTS "system_setting" (
                                  "key"        VARCHAR(100) PRIMARY KEY,
                                  "value"      VARCHAR(255) NOT NULL,
                                  "description" TEXT,
                                  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =========================================================================
-- DEVICES & SESSIONS
-- =========================================================================
CREATE TABLE IF NOT EXISTS "device" (
                          "id"            VARCHAR(100) PRIMARY KEY,
                          "user_id"       UUID NOT NULL REFERENCES "user"("id") ON DELETE CASCADE,
                          "user_agent"    VARCHAR(512),
                          "last_login_at" TIMESTAMPTZ,
                          "created_at"    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "session" (
                           "id"         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           "user_id"    UUID NOT NULL REFERENCES "user"("id")   ON DELETE CASCADE,
                           "device_id"  VARCHAR(100) NOT NULL REFERENCES "device"("id") ON DELETE CASCADE,
                           "dpop_jkt"   VARCHAR(255),
                           "ip_address" VARCHAR(45),
                           "user_agent" VARCHAR(512),
                           "is_active"  BOOLEAN NOT NULL DEFAULT TRUE,
                           "created_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "refresh_token" (
                                 "id"         UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- == jti
                                 "session_id" UUID NOT NULL REFERENCES "session"("id") ON DELETE CASCADE,
                                 "token_hash" VARCHAR(64) NOT NULL UNIQUE,
                                 "expires_at" TIMESTAMPTZ NOT NULL,
                                 "is_revoked" BOOLEAN NOT NULL DEFAULT FALSE,
                                 "created_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX "idx_refresh_tokens_session" ON "refresh_token" ("session_id");


-- =========================================================================
-- GROUPS (TENANTS)
-- =========================================================================
CREATE TABLE IF NOT EXISTS "nested_group" (
                                "id"              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                "name"            VARCHAR(100) NOT NULL,
                                "parent_group_id" UUID REFERENCES "nested_group"("id"),
                                "owner"           UUID NOT NULL REFERENCES "user"("id")
);

CREATE INDEX "idx_nested_group_parent" ON "nested_group" ("parent_group_id");

CREATE TABLE IF NOT EXISTS "user_group" (
                              "id"              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              "nested_group_id" UUID NOT NULL REFERENCES "nested_group"("id") ON DELETE CASCADE,
                              "name"            VARCHAR(100) NOT NULL,
    -- Redundant key needed as the target of composite FKs below.
                              UNIQUE ("id", "nested_group_id")
);

CREATE INDEX "idx_user_group_nested" ON "user_group" ("nested_group_id");

CREATE TABLE IF NOT EXISTS "nested_group_configuration" (
                                              "nested_group_id"    UUID PRIMARY KEY REFERENCES "nested_group"("id") ON DELETE CASCADE,
                                              "default_user_group" UUID REFERENCES "user_group"("id"),
                                              "max_members"        INT,
                                              "base_url"           VARCHAR(100),
                                              "refresh_token_time" BIGINT,   -- seconds
                                              "access_token_time"  BIGINT    -- seconds
);

CREATE TABLE IF NOT EXISTS "group_member" (
                                "nested_group_id" UUID NOT NULL REFERENCES "nested_group"("id") ON DELETE CASCADE,
                                "user_id"         UUID NOT NULL REFERENCES "user"("id") ON DELETE CASCADE,
                                "group_id"        UUID NOT NULL,
                                PRIMARY KEY ("nested_group_id", "group_id", "user_id"),
    -- The group must belong to the same tenant as this membership row.
                                FOREIGN KEY ("group_id", "nested_group_id")
                                    REFERENCES "user_group" ("id", "nested_group_id") ON DELETE CASCADE
);

CREATE INDEX "idx_group_member_lookup"
    ON "group_member" ("nested_group_id", "user_id", "group_id");

CREATE TABLE IF NOT EXISTS "group_invite" (
                                "id"        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                "code"      VARCHAR(255) UNIQUE NOT NULL,
                                "is_active" BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS "group_invite_target" (
                                       "invite_id"       UUID NOT NULL REFERENCES "group_invite"("id") ON DELETE CASCADE,
                                       "nested_group_id" UUID NOT NULL REFERENCES "nested_group"("id") ON DELETE CASCADE,
                                       "is_primary"      BOOLEAN NOT NULL DEFAULT FALSE,
                                       PRIMARY KEY ("invite_id", "nested_group_id")
);


-- =========================================================================
-- ABAC CATALOG (SYSTEM ADMIN)
-- =========================================================================
CREATE TABLE IF NOT EXISTS "action" (
                          "id"   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          "name" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "entity_type" (
                               "id"   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               "name" VARCHAR(100) NOT NULL UNIQUE   -- e.g. 'api_log'
);

CREATE TABLE IF NOT EXISTS "targetable_attribute" (
                                        "id"             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        "entity_type_id" UUID NOT NULL REFERENCES "entity_type"("id") ON DELETE CASCADE,
                                        "name"           VARCHAR(100) NOT NULL,
                                        UNIQUE ("entity_type_id", "name")
);

CREATE INDEX "idx_targetable_attribute_entity"
    ON "targetable_attribute" ("entity_type_id");


-- =========================================================================
-- PERMISSIONS (GLOBAL TEMPLATES)
-- =========================================================================
CREATE TABLE IF NOT EXISTS "permission" (
                              "id"           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              "name"         VARCHAR(50) NOT NULL,
                              "action_id"    UUID NOT NULL REFERENCES "action"("id"),
                              "is_composite" BOOLEAN NOT NULL DEFAULT FALSE,
                              UNIQUE ("action_id", "name")
);

-- Which attributes this template touches. No tenant column here — templates
-- are global and reusable across tenants.
CREATE TABLE IF NOT EXISTS "permission_attribute" (
                                        "permission_id"           UUID NOT NULL REFERENCES "permission"("id")           ON DELETE CASCADE,
                                        "targetable_attribute_id" UUID NOT NULL REFERENCES "targetable_attribute"("id") ON DELETE CASCADE,
                                        "is_required"             BOOLEAN NOT NULL DEFAULT FALSE,
                                        "auto_fill_value"         VARCHAR(255),
                                        PRIMARY KEY ("permission_id", "targetable_attribute_id")
);

CREATE INDEX "idx_permission_attribute_attr"
    ON "permission_attribute" ("targetable_attribute_id");


-- =========================================================================
-- TENANT CEILING (SYSTEM ADMIN GRANTS)
-- =========================================================================
-- Which attributes a tenant is allowed to target. This is the "ceiling".
CREATE TABLE IF NOT EXISTS "group_attribute_grant" (
                                         "nested_group_id"         UUID NOT NULL REFERENCES "nested_group"("id")         ON DELETE CASCADE,
                                         "targetable_attribute_id" UUID NOT NULL REFERENCES "targetable_attribute"("id") ON DELETE CASCADE,
                                         PRIMARY KEY ("nested_group_id", "targetable_attribute_id")
);

CREATE INDEX "idx_group_attribute_grant_attr"
    ON "group_attribute_grant" ("targetable_attribute_id");


-- =========================================================================
-- TENANT BINDINGS
-- =========================================================================
CREATE TABLE IF NOT EXISTS "group_permission" (
                                    "id"                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    "nested_group_id"   UUID NOT NULL REFERENCES "nested_group"("id") ON DELETE CASCADE,
                                    "permission_id"     UUID NOT NULL REFERENCES "permission"("id")     ON DELETE CASCADE,
                                    "owner_users_group" UUID NOT NULL,
                                    "target_users_group" UUID,
    -- Both groups must belong to the same tenant.
                                    FOREIGN KEY ("owner_users_group", "nested_group_id")
                                        REFERENCES "user_group" ("id", "nested_group_id") ON DELETE CASCADE,
                                    FOREIGN KEY ("target_users_group", "nested_group_id")
                                        REFERENCES "user_group" ("id", "nested_group_id") ON DELETE CASCADE,
    -- NULLS NOT DISTINCT so rows with a NULL target are also de-duplicated.
    -- Requires PostgreSQL 15+. On older versions, replace with a sentinel UUID
    -- or a partial unique index for the NULL case.
                                    UNIQUE NULLS NOT DISTINCT
                                        ("nested_group_id", "owner_users_group", "target_users_group", "permission_id")
);

CREATE INDEX "idx_group_permission_lookup"
    ON "group_permission" ("nested_group_id", "owner_users_group", "permission_id");


-- =========================================================================
-- LOGGING
-- =========================================================================
CREATE TABLE IF NOT EXISTS "api_log" (
                           "id"              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           "nested_group_id" UUID REFERENCES "nested_group"("id") ON DELETE SET NULL,
                           "event_type"      VARCHAR(100) NOT NULL,
                           "method"          VARCHAR(10)  NOT NULL,
                           "path"            TEXT         NOT NULL,
                           "status"          INTEGER      NOT NULL,
                           "duration_ms"     BIGINT       NOT NULL,
                           "user_id"         UUID REFERENCES "user"("id") ON DELETE SET NULL,
                           "created_at"      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                           "error_details"   JSONB
);

CREATE INDEX "idx_api_log_tenant"
    ON "api_log" ("nested_group_id", "created_at" DESC);

CREATE INDEX "idx_api_log_user" ON "api_log" ("user_id");

CREATE INDEX "idx_api_log_error_details"
    ON "api_log" USING GIN ("error_details");


-- =========================================================================
-- LATE FKs (users point at nested_group, so added after nested_group exists)
-- =========================================================================
ALTER TABLE "user"
    ADD CONSTRAINT "user_current_environment_fk"
        FOREIGN KEY ("current_environment") REFERENCES "nested_group"("id") ON DELETE SET NULL;

ALTER TABLE "user"
    ADD CONSTRAINT "user_default_environment_fk"
        FOREIGN KEY ("default_environment") REFERENCES "nested_group"("id") ON DELETE SET NULL;
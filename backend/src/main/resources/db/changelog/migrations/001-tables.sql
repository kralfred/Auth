-- Ensure pgcrypto extension is active
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Global User Entities
CREATE TABLE IF NOT EXISTS "user" (
                                      "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      "username" VARCHAR(100) NOT NULL,
                                      "default_group_id" UUID,
                                      "current_environment" UUID
);

CREATE TABLE IF NOT EXISTS "user_info" (
                                           "user_id" UUID PRIMARY KEY,
                                           "email" VARCHAR(255) UNIQUE NOT NULL,
                                           "name" VARCHAR(100),
                                           "password" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "custom_info" (
                                             "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             "name" VARCHAR(255) UNIQUE,
                                             "value" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "system_setting" (
                                                "key" VARCHAR(100) PRIMARY KEY,
                                                "value" VARCHAR(255) NOT NULL,
                                                "description" TEXT,
                                                "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "user_identity" (
                                               "user_id" UUID NOT NULL,
                                               "provider" VARCHAR(255) NOT NULL,
                                               "provider_id" VARCHAR(100),
                                               PRIMARY KEY ("user_id", "provider")
);

CREATE TABLE IF NOT EXISTS "device" (
                                        "id" VARCHAR(100) PRIMARY KEY,
                                        "user_id" UUID NOT NULL,
                                        "user_agent" VARCHAR(512),
                                        "last_login_at" TIMESTAMP,
                                        "created_at" TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "session" (
                                         "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         "user_id" UUID NOT NULL,
                                         "device_id" VARCHAR(100) NOT NULL,
                                         "dpop_jkt" VARCHAR(255),
                                         "ip_address" VARCHAR(45),
                                         "user_agent" VARCHAR(512),
                                         "is_active" BOOLEAN DEFAULT TRUE,
                                         "created_at" TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "refresh_token" (
                                               "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               "session_id" UUID NOT NULL,
                                               "token_hash" VARCHAR(64) UNIQUE NOT NULL,
                                               "expires_at" TIMESTAMPTZ NOT NULL,
                                               "is_revoked" BOOLEAN NOT NULL DEFAULT FALSE,
                                               "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Group Architecture
CREATE TABLE IF NOT EXISTS "nested_group" (
                                              "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              "name" VARCHAR(100) NOT NULL,
                                              "parent_group_id" UUID,
                                              "owner" UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS "user_group" (
                                            "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            "nested_group_id" UUID NOT NULL,
                                            "name" VARCHAR(100) NOT NULL
);

-- Refactored: Added nested_group_id to group_member for fast direct tenant indexing
CREATE TABLE IF NOT EXISTS "group_member" (
                                              "nested_group_id" UUID NOT NULL,
                                              "user_id" UUID NOT NULL,
                                              "group_id" UUID NOT NULL,
                                              PRIMARY KEY ("nested_group_id", "group_id", "user_id")
);

CREATE TABLE IF NOT EXISTS "group_invite" (
                                              "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              "code" VARCHAR(255) UNIQUE,
                                              "is_active" BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS "group_invite_target" (
                                                     "invite_id" UUID NOT NULL,
                                                     "nested_group_id" UUID NOT NULL,
                                                     "is_primary" BOOLEAN NOT NULL,
                                                     PRIMARY KEY ("invite_id", "nested_group_id")
);

-- Global ABAC Metadata (Shared definitions)
CREATE TABLE IF NOT EXISTS "action" (
                                        "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        "name" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "permission" (
                                            "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            "name" VARCHAR(50) NOT NULL,
                                            "action_id" UUID NOT NULL,
                                            "is_composite" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "entity_type" (
                                             "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             "name" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "targetable_attribute" (
                                                      "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                      "entity_type_id" UUID NOT NULL,
                                                      "name" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "permission_attribute" (
                                                      "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                      "nested_group_id" UUID NOT NULL,
                                                      "permission_id" UUID NOT NULL,
                                                      "targetable_attribute_id" UUID,
                                                      "is_required" BOOLEAN DEFAULT FALSE,
                                                      "auto_fill_value" VARCHAR(255)
);

-- Refactored: Added nested_group_id to group_permission for direct lookup
CREATE TABLE IF NOT EXISTS "group_permission" (
                                                  "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  "nested_group_id" UUID NOT NULL,
                                                  "permission_id" UUID NOT NULL,
                                                  "owner_users_group" UUID NOT NULL,
                                                  "target_users_group" UUID NOT NULL
);

-- Logging (Added nested_group_id for tenant log isolation)
CREATE TABLE IF NOT EXISTS "api_log" (
                                         "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         "nested_group_id" UUID,
                                         "event_type" VARCHAR(100) NOT NULL,
                                         "method" VARCHAR(10) NOT NULL,
                                         "path" TEXT NOT NULL,
                                         "status" INTEGER NOT NULL,
                                         "duration_ms" BIGINT NOT NULL,
                                         "user_id" UUID,
                                         "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         "error_details" JSONB
);

-- =========================================================================
-- FOREIGN KEYS
-- =========================================================================

ALTER TABLE "user_info" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE;
ALTER TABLE "user_identity" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE;
ALTER TABLE "user" ADD FOREIGN KEY ("current_environment") REFERENCES "nested_group" ("id");
ALTER TABLE "user" ADD FOREIGN KEY ("default_group_id") REFERENCES "nested_group" ("id");

ALTER TABLE "session" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL;
ALTER TABLE "session" ADD FOREIGN KEY ("device_id") REFERENCES "device" ("id") ON DELETE SET NULL;
ALTER TABLE "device" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE;
ALTER TABLE "refresh_token" ADD FOREIGN KEY ("session_id") REFERENCES "session" ("id") ON DELETE CASCADE;

ALTER TABLE "nested_group" ADD FOREIGN KEY ("parent_group_id") REFERENCES "nested_group" ("id");
ALTER TABLE "nested_group" ADD FOREIGN KEY ("owner") REFERENCES "user" ("id");
ALTER TABLE "user_group" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE;

ALTER TABLE "group_member" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE;
ALTER TABLE "group_member" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE;
ALTER TABLE "group_member" ADD FOREIGN KEY ("group_id") REFERENCES "user_group" ("id") ON DELETE CASCADE;

ALTER TABLE "group_invite_target" ADD FOREIGN KEY ("invite_id") REFERENCES "group_invite" ("id") ON DELETE CASCADE;
ALTER TABLE "group_invite_target" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE;

ALTER TABLE "permission" ADD FOREIGN KEY ("action_id") REFERENCES "action" ("id");
ALTER TABLE "targetable_attribute" ADD FOREIGN KEY ("entity_type_id") REFERENCES "entity_type" ("id");

ALTER TABLE "permission_attribute" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE;
ALTER TABLE "permission_attribute" ADD FOREIGN KEY ("permission_id") REFERENCES "permission" ("id") ON DELETE CASCADE;
ALTER TABLE "permission_attribute" ADD FOREIGN KEY ("targetable_attribute_id") REFERENCES "targetable_attribute" ("id") ON DELETE CASCADE;

ALTER TABLE "group_permission" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("permission_id") REFERENCES "permission" ("id") ON DELETE CASCADE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("owner_users_group") REFERENCES "user_group" ("id") ON DELETE CASCADE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("target_users_group") REFERENCES "user_group" ("id") ON DELETE CASCADE;

ALTER TABLE "api_log" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE SET NULL;
ALTER TABLE "api_log" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id");

-- =========================================================================
-- COMPOSITE B-TREE INDEXES FOR OPTIMIZED TENANT LOOKUPS
-- =========================================================================

-- 1. Accelerates runtime authorization checks: (nested_group_id -> user -> group)
CREATE INDEX idx_group_member_lookup
    ON "group_member" ("nested_group_id", "user_id", "group_id");

-- 2. Accelerates group-permission resolving: (nested_group_id -> group -> permission)
CREATE INDEX idx_group_permission_lookup
    ON "group_permission" ("nested_group_id", "owner_users_group", "permission_id");

-- 3. Accelerates target attribute resolution: (nested_group_id -> permission)
CREATE INDEX idx_permission_attribute_lookup
    ON "permission_attribute" ("nested_group_id", "permission_id");

CREATE UNIQUE INDEX idx_permission_attribute_unique
    ON "permission_attribute" ("nested_group_id", "permission_id", "targetable_attribute_id")
    WHERE "targetable_attribute_id" IS NOT NULL;

-- Enforce uniqueness for global permissions (where attribute is NULL) to prevent duplicate global assignments
CREATE UNIQUE INDEX idx_permission_attribute_global_unique
    ON "permission_attribute" ("nested_group_id", "permission_id")
    WHERE "targetable_attribute_id" IS NULL;


-- 4. Accelerates tenant log queries
CREATE INDEX idx_api_log_tenant
    ON "api_log" ("nested_group_id", "created_at" DESC);

CREATE INDEX idx_api_log_error_details ON "api_log" USING GIN ("error_details");
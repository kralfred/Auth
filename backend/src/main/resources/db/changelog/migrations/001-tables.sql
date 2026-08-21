-- Ensure pgcrypto extension is active for gen_random_uuid() in older PG versions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS "user" (
                                      "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      "username" VARCHAR(100) NOT NULL,
                                      "current_environment" UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS "user_info" (
                                           "user_id" UUID PRIMARY KEY,
                                           "email" VARCHAR(255) UNIQUE NOT NULL,
                                           "name" VARCHAR(100) NOT NULL,
                                           "password" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "custom_info" (
                                             "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             "user_id" UUID NOT NULL,
                                             "name" VARCHAR(255),
                                             "value" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "device" (
                                        "id" VARCHAR(100) PRIMARY KEY,
                                        "user_id" UUID NOT NULL,
                                        "user_agent" VARCHAR(512),
                                        "last_login_at" TIMESTAMPTZ,
                                        "created_at" TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "session" (
                                         "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         "user_id" UUID NOT NULL,
                                         "device_id" VARCHAR(100) NOT NULL,
                                         "dpop_jkt" VARCHAR(255),
                                         "ip_address" INET,
                                         "user_agent" VARCHAR(512),
                                         "is_active" BOOLEAN DEFAULT true,
                                         "created_at" TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "refresh_token" (
                                               "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               "session_id" UUID NOT NULL,
                                               "token_hash" VARCHAR(64) UNIQUE NOT NULL,
                                               "expires_at" TIMESTAMPTZ NOT NULL,
                                               "is_revoked" BOOLEAN NOT NULL DEFAULT false,
                                               "created_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

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

CREATE TABLE IF NOT EXISTS "group_member" (
                                              "user_id" UUID NOT NULL,
                                              "group_id" UUID NOT NULL,
                                              PRIMARY KEY ("user_id", "group_id")
);

CREATE TABLE IF NOT EXISTS "action" (
                                        "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        "name" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "permission" (
                                            "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            "name" VARCHAR(50) NOT NULL,
                                            "action_id" UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS "entity_type" (
                                             "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             "name" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "targetable_attribute" (
                                                      "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                      "entity_type_id" UUID NOT NULL,
                                                      "name" VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "permission_attribute" (
                                                      "permission_id" UUID NOT NULL,
                                                      "targetable_attribute_id" UUID NOT NULL,
                                                      PRIMARY KEY ("permission_id", "targetable_attribute_id")
);

CREATE TABLE IF NOT EXISTS "group_permission" (
                                                  "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  "permission_id" UUID NOT NULL,
                                                  "owner_users_group" UUID NOT NULL,
                                                  "target_users_group" UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS "api_log" (
                                         "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         "event_type" VARCHAR(100) NOT NULL,
                                         "method" VARCHAR(10) NOT NULL,
                                         "path" TEXT NOT NULL,
                                         "status" INTEGER NOT NULL,
                                         "duration_ms" BIGINT NOT NULL,
                                         "user_id" UUID,
                                         "created_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX IF NOT EXISTS "idx_refresh_tokens_hash" ON "refresh_token" ("token_hash");
CREATE INDEX IF NOT EXISTS "idx_refresh_tokens_session" ON "refresh_token" ("session_id");
CREATE INDEX IF NOT EXISTS "idx_nested_group_parent" ON "nested_group" ("parent_group_id");
CREATE INDEX IF NOT EXISTS "idx_nested_group_owner" ON "nested_group" ("owner");
CREATE INDEX IF NOT EXISTS "idx_user_group_nested" ON "user_group" ("nested_group_id");
CREATE INDEX IF NOT EXISTS "idx_targetable_attribute_entity" ON "targetable_attribute" ("entity_type_id");
CREATE INDEX IF NOT EXISTS "idx_gp_permission" ON "group_permission" ("permission_id");
CREATE INDEX IF NOT EXISTS "idx_gp_owner" ON "group_permission" ("owner_users_group");
CREATE INDEX IF NOT EXISTS "idx_gp_target" ON "group_permission" ("target_users_group");
CREATE INDEX IF NOT EXISTS "idx_log_user" ON "api_log" ("user_id");

COMMENT ON COLUMN "refresh_token"."id" IS 'Token jti';

-- Foreign Keys
ALTER TABLE "user_info" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "custom_info" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "user" ADD FOREIGN KEY ("current_environment") REFERENCES "nested_group" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "session" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "session" ADD FOREIGN KEY ("device_id") REFERENCES "device" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "device" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "refresh_token" ADD FOREIGN KEY ("session_id") REFERENCES "session" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "nested_group" ADD FOREIGN KEY ("parent_group_id") REFERENCES "nested_group" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "nested_group" ADD FOREIGN KEY ("owner") REFERENCES "user" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "user_group" ADD FOREIGN KEY ("nested_group_id") REFERENCES "nested_group" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "group_member" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "group_member" ADD FOREIGN KEY ("group_id") REFERENCES "user_group" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "permission" ADD FOREIGN KEY ("action_id") REFERENCES "action" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "targetable_attribute" ADD FOREIGN KEY ("entity_type_id") REFERENCES "entity_type" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "permission_attribute" ADD FOREIGN KEY ("permission_id") REFERENCES "permission" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "permission_attribute" ADD FOREIGN KEY ("targetable_attribute_id") REFERENCES "targetable_attribute" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("permission_id") REFERENCES "permission" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("owner_users_group") REFERENCES "user_group" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "group_permission" ADD FOREIGN KEY ("target_users_group") REFERENCES "user_group" ("id") ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "api_log" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL DEFERRABLE INITIALLY IMMEDIATE;
package org.example.reservation_api.security;

import java.util.UUID;

public final class CurrentEnvironmentContext {

    private static final ThreadLocal<UUID> CURRENT_ENV = new ThreadLocal<>();

    private CurrentEnvironmentContext() {
        // Private constructor to prevent instantiation
    }

    public static void set(UUID nestedGroupId) {
        CURRENT_ENV.set(nestedGroupId);
    }

    public static UUID get() {
        return CURRENT_ENV.get();
    }

    public static void clear() {
        CURRENT_ENV.remove();
    }
}

package org.example.reservation_api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.repositories.SystemSettingRepository;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityFeatureFlags {

    private final SystemSettingRepository settingRepository;

    private volatile Map<String, String> cache;
    private volatile long loadedAt;

    private static final long CACHE_TTL_MS = 5_000;

    public enum Flag {
        DPOP_ENABLED            ("true"),
        DPOP_REQUIRE_JKT        ("true"),
        DPOP_REQUIRE_ATH        ("true"),
        DPOP_REQUIRE_HTM        ("true"),
        DPOP_REQUIRE_HTU        ("true"),
        DPOP_MAX_AGE_SECONDS    ("60"),
        DPOP_JTI_STORE_ENABLED  ("false"),
        JWT_EXPIRY_CHECK_ENABLED("true"),
        JWT_SIGNATURE_ENFORCED  ("true"),
        COOKIE_HTTP_ONLY        ("true");

        final String defaultValue;
        Flag(String defaultValue) { this.defaultValue = defaultValue; }
    }

    public boolean getBoolean(Flag flag) {
        return Boolean.parseBoolean(get(flag));
    }

    public long getLong(Flag flag) {
        try {
            return Long.parseLong(get(flag));
        } catch (NumberFormatException e) {
            return Long.parseLong(flag.defaultValue);
        }
    }

    public String get(Flag flag) {
        Map<String, String> snapshot = cache;
        if (snapshot == null || System.currentTimeMillis() - loadedAt > CACHE_TTL_MS) {
            snapshot = refresh();
        }
        return snapshot.getOrDefault(flag.name(), flag.defaultValue);
    }

    private synchronized Map<String, String> refresh() {
        Map<String, String> fresh = new HashMap<>();
        for (Flag f : Flag.values()) {
            settingRepository.findValueByKey(f.name())
                    .ifPresent(v -> fresh.put(f.name(), v));
        }
        cache = fresh;
        loadedAt = System.currentTimeMillis();
        return fresh;
    }

    public void set(Flag flag, String value) {
        settingRepository.upsert(flag.name(), value, "Security feature flag — practice mode");
        cache = null;   // force reload on next access
        log.warn("SECURITY FLAG CHANGED: {} = {}", flag.name(), value);
    }

    public Map<Flag, String> all() {
        Map<Flag, String> result = new EnumMap<>(Flag.class);
        for (Flag f : Flag.values()) {
            result.put(f, get(f));
        }
        return result;
    }
}
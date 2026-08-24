package org.example.reservation_api.entities;

import java.util.UUID;

public interface Identifiable {
    UUID id();

    static String getTableName(Class<?> clazz) {
        return clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }
}

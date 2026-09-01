package org.example.reservation_api.entities;

import java.util.UUID;

public interface BaseEntity extends Identifiable {
    UUID id();
}

package org.example.reservation_api.entities;

import java.util.UUID;

public record UserGroup(
        UUID id,
        UUID nestedGroupId,
        String name
) implements Identifiable {
    public UserGroup(UUID nestedGroupId, String name){
        this(UUID.randomUUID(),nestedGroupId,name);
    }

}
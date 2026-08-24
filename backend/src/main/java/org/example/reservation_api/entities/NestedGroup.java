package org.example.reservation_api.entities;


import java.util.UUID;

public record NestedGroup(
        UUID id,
        String name,
        UUID parentGroupId, // Nullable for root groups
        UUID owner
) implements Identifiable {

    public NestedGroup(String name, UUID parentGroup, UUID ownerId){
        this(UUID.randomUUID(),name,parentGroup,ownerId);
    }


}


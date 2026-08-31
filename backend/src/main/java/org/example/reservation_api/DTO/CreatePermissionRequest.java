package org.example.reservation_api.DTO;

public record CreatePermissionRequest(
        String targetTable,  // e.g., "user_logs", "nested_group", "user_info"
        String attribute,    // e.g., "*", "name", "email", "id"
        String actionType    // e.g., "CREATE", "READ", "UPDATE", "DELETE"
) {
    /**
     * Generates a standardized unique name for this permission.
     * Example: "DELETE:user_logs:id" or "UPDATE:nested_group:name"
     */
    public String toPermissionName() {
        return String.format("%s:%s:%s",
                actionType.toUpperCase(),
                targetTable.toLowerCase(),
                attribute.toLowerCase());
    }
}

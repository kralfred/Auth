import { BaseApiRepository } from "./BaseApiRepository.js";

export class ApiPermissionRepository extends BaseApiRepository {

    // Catalog — read-only, tenant-scoped
    async getActions() {
        return await this.request(`/api/v1/permissions/catalog/actions`, { method: "GET" });
    }

    async getEntities() {
        return await this.request(`/api/v1/permissions/catalog/entities`, { method: "GET" });
    }

    async getAttributes(entityId) {
        return await this.request(`/api/v1/permissions/catalog/entities/${entityId}/attributes`, { method: "GET" });
    }

    // Create — tenant-scoped
    async create(payload) {
        return await this.request(`/api/v1/permissions/create`, {
            method: "POST",
            body: JSON.stringify(payload)
        });
    }
}
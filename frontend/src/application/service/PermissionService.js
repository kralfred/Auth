// application/service/PermissionService.js
export class PermissionService {
    constructor(permissionRepository, appState) {
        this.permissionRepository = permissionRepository;
        this.appState = appState;
    }

    async loadFormData() {
        const [actions, entities] = await Promise.all([
            this.permissionRepository.getActions(),
            this.permissionRepository.getEntities()
        ]);
        return { actions, entities };
    }

    async loadAttributes(entityId) {
        return await this.permissionRepository.getAttributes(entityId);
    }

    async create({ name, actionId, entityId, attributeIds }) {
        const nestedGroupId = this.appState.getContext().currentEnvironmentId;
        if (!nestedGroupId) {
            throw new Error("No active environment — cannot create permission");
        }

        return await this.permissionRepository.create({
            name,
            actionId,
            nestedGroupId,
            targetableAttributeIds: attributeIds ?? []
        });
    }
}
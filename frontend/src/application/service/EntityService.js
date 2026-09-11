// application/services/EntityService.js
export class EntityService {
  constructor({ userRepository, entityRepository }) {
    this.userRepository = userRepository;
    this.entityRepository = entityRepository;
  }

  async getEntityList(entityType) {
    const key = entityType.toLowerCase();


    if (key === "users") {
      return await this.userRepository.getAllUsers();
    }

    // Generic REST call for standard entities (logs, permissions, rules, etc.)
    return await this.entityRepository.getAll(key);
  }

  async createPermissionAttribute(attributeName, entityTypeName) {
    return await this.entityRepository.createPermissionAttribute(attributeName, entityTypeName);
  }

  async getEntityById(entityType, id) {
    const key = entityType.toLowerCase();

    if (key === "users") {
      return await this.userRepository.getById(id);
    }

    return await this.entityRepository.getById(key, id);
  }
}
export class AdminService {
  constructor(adminRepository) {
    this.adminRepository = adminRepository;
    this.entities = [];
    this.selectedEntity = null;
    this.attributeCache = new Map();
  }

  getEntities() {
    return this.entities;
  }

  getSelectedEntity() {
    return this.selectedEntity;
  }

  setSelectedEntity(entity) {
    this.selectedEntity = entity;
  }

  async getAttributesForEntity(entityId) {
    if (!entityId) return [];
    if (this.attributeCache.has(entityId)) {
      return this.attributeCache.get(entityId);
    }
    const response = await this.adminRepository.getEntityAttributes(entityId);
    const attrList = Array.isArray(response) ? response : response.attributes || [];

    this.attributeCache.set(entityId, attrList);
    return attrList;
  }

  async loadSelectedEntityAttributes() {
    if (!this.selectedEntity) return [];
    return await this.getAttributesForEntity(this.selectedEntity.id);
  }

  async loadEntities() {
    const rawEntities = (await this.adminRepository.getAllEntities()) || [];
    this.entities = rawEntities.map((entity) => ({
      id: typeof entity === "string" ? entity : entity.id || entity.name,
      name: typeof entity === "string" ? entity : entity.name || entity.id
    }));

    return this.entities;
  }

  async createEntityType(name) {
    if (!name) throw new Error("Entity name is required.");
    const res = await this.adminRepository.addNewEntityType(name);
    
    const newEntity = {
      id: res.id,
      name: name
    }
    this.entities.push(newEntity);
    alert(res.message);

    return await this.loadEntities();
  }

  async createPermissionAttribute(attributeName) {
    if (!this.selectedEntity) throw new Error("No entity selected.");
    
    await this.adminRepository.addNewEntityAttribute(this.selectedEntity.id, attributeName);
    
    // CRITICAL FIX: Clear cache for this entity so the next fetch gets fresh API data
    this.attributeCache.delete(this.selectedEntity.id);
    
    return await this.loadSelectedEntityAttributes();
  }
}
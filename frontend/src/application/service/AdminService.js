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
      console.log(`Cache hit for entity: ${entityId}`);
      return this.attributeCache.get(entityId);
    }
    console.log(`Cache miss for entity: ${entityId}. Fetching from API...`);
    const response = await this.adminRepository.getEntityAttributes(entityId);
    const attrList = Array.isArray(response) ? response : response.attributes || [];

    this.attributeCache.set(entityId, attrList);

    return attrList;
  }

  async loadSelectedEntityAttributes() {
    if (!this.selectedEntity) return [];

    const entityId = typeof this.selectedEntity === "string"
      ? this.selectedEntity
      : this.selectedEntity.id || this.selectedEntity.name;

    return await this.getAttributesForEntity(entityId);
  }


  async loadEntities() {
    const rawEntities = (await this.adminRepository.getAllEntities()) || [];
    this.entities = rawEntities.map((entity) => ({
      id: typeof entity === "string" ? entity : entity.id || entity.name,
      name: typeof entity === "string" ? entity : entity.name || entity.id
    }));

    return this.entities;
  }

  async createEntityType(name){
    console.error("Service firing add entity:");
    await this.adminRepository.addNewEntityType(name);
  }
  async loadEntityAttributes(entityId){
    await this.adminRepository.getEntityAttributes(entityId);
  }
  async createPermissionAttribute(attributeName){
  }


  async createEntityType(name) {
    if (!name) throw new Error("Entity name is required.");
    
    await this.adminRepository.addNewEntityType(name);
    // Refresh entities list state automatically
    return await this.loadEntities();
  }
}
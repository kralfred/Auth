export class AdminService {
  constructor(adminRepository) {
    this.adminRepository = adminRepository;
    this.entities = [];
    this.selectedEntity = null;
    this.loadedEntities = [];
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


  async loadEntities() {
    const rawEntities = (await this.adminRepository.getAllEntities()) || [];

    // Normalize and store in service state
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


  async loadSelectedEntityAttributes() {
    if (!this.selectedEntity) return [];

    const entityId = this.selectedEntity.id || this.selectedEntity.name;
    const response = await this.adminRepository.getEntityAttributes(entityId);
    const attrList = Array.isArray(response) ? response : response.attributes || [];

    return attrList.map((attr) => ({
      id: typeof attr === "string" ? attr : attr.id || attr.attributeName,
      name: typeof attr === "string" ? attr : attr.name || attr.attributeName
    }));
  }

  async createEntityType(name) {
    if (!name) throw new Error("Entity name is required.");
    
    await this.adminRepository.addNewEntityType(name);
    // Refresh entities list state automatically
    return await this.loadEntities();
  }
}
export class AdminService {
  constructor( adminRepository ) {
    this.adminRepository = adminRepository;
  }


  async loadEntities(){
    console.error("Service firing:");
    return await this.adminRepository.getAllEntities();
  }

  async createEntityType(name){
    console.error("Service firing add entity:");
    await this.adminRepository.addNewEntityType(name);
  }
}
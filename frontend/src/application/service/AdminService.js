export class AdminService {
  constructor( adminRepository ) {
    this.adminRepository = adminRepository;
  }


  async loadEntities(){
    console.error("Service firing:");
    return await this.adminRepository.getAllEntities();
  }
}
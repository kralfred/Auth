import { BaseApiRepository } from "./BaseApiRepository.js";

export class ApiAdminRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
  }

async getAllEntities() {
    console.error("Repository firing:");
    return await this.request(`/api/admin/permissions/view/entities/all`, {
      method: "GET"
    });
  }

   async getEntityAttributes(string){
       return await this.request(`/api/admin/permissions/view/entities/all`, {
      method: "GET"
    });
  }


}
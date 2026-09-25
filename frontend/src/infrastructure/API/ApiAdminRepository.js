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

async addNewEntityType(name) {
  console.log("Sending entity name to backend:", name);
  
  if (!name) {
    throw new Error("Entity name cannot be empty");
  }

  return await this.request(`/api/admin/permissions/entity/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ name })
  });
}


}
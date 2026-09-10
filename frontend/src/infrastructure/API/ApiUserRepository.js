import { BaseApiRepository } from "./BaseApiRepository.js";




export class ApiUserRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
  }

  async getAllUsers() {
    return await this.request("/api/users", {
      method: "GET"
    });
  }

  async getById(userId) {
    return await this.request(`/api/users/${userId}`, {
      method: "GET"
    });
  }

  async getDetailedPermissions(userId) {
    return await this.request(`/api/users/${userId}/permissions`, {
      method: "GET"
    });
  }

  async updateSecureSettings(userId, updates) {
    return await this.request(`/api/users/${userId}/secure`, {
      method: "PUT",
      body: JSON.stringify(updates)
    });
  }

  async adminUpdateUser(userId, userData) {
    return await this.request(`/api/users/${userId}/admin`, {
      method: "PUT",
      body: JSON.stringify(userData)
    });
  }
}
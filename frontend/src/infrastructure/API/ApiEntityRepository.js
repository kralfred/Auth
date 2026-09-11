// infrastructure/API/ApiEntityRepository.js
import { BaseApiRepository } from "./BaseApiRepository.js";

export class ApiEntityRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
  }

  /**
   * Fetches a collection of entities
   * @param {string} endpoint - The resource path (e.g., "logs", "permissions")
   */
  async getAll(endpoint) {
    return await this.request(`/api/${endpoint}`, {
      method: "GET"
    });
  }

  /**
   * Fetches a single entity instance by ID
   * @param {string} endpoint - The resource path (e.g., "logs", "permissions")
   * @param {string|number} id - The entity identifier
   */
  async getById(endpoint, id) {
    return await this.request(`/api/${endpoint}/${id}`, {
      method: "GET"
    });
  }
}
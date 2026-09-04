import { BaseApiRepository } from "./BaseApiRepository.js";
import { User } from "../../domain/entities/User.js";
import { Token } from "../../domain/entities/Token.js";
import { DeviceUtils } from '../UI/utils/DeviceUtils.js';
import { DpopUtils } from '../UI/utils/DpopUtils.js';

export class ApiUserRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
  }

  async login(email, username, password) {
    const endpoint = "/api/auth/login";

    // 1. Generate DPoP proof header dynamically
    let headers = {};
    try {
      const dpopProof = await DpopUtils.generateProof("POST", endpoint);
      headers["DPoP"] = dpopProof;
    } catch (e) {
      console.warn("DPoP generation skipped or unsupported:", e);
    }

    // 2. Pass email along with username, password, and required device metadata
    const data = await this.request(endpoint, {
      method: "POST",
      headers: headers,
      body: JSON.stringify({ 
        email, 
        username, 
        password,
        deviceId: DeviceUtils.getDeviceId(),     // Satisfies @NotBlank deviceId requirement
        deviceName: DeviceUtils.getDeviceName(), // e.g., "Chrome Browser"
        deviceType: DeviceUtils.getDeviceType()  // e.g., "WEB"
      })
    });

    // Extract access token string and normalize format
    const tokenString = data.token?.token || data.token || data.accessToken;
    this.setToken(tokenString); 

    return {
      user: new User({
        id: data.id || data.userId,
        username: data.username,
        email: data.email,
        role: data.role,
        permissions: data.permissions || data.pageAccess
      }),
      token: new Token(tokenString, data.token?.expiresAt || data.expiresAt || data.expiration)
    };
  }

  async validateToken(tokenString) {
    const data = await this.request("/api/auth/validate", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${tokenString}`, 
        "Content-Type": "application/json"
      }
    });

    return {
      user: new User({
        id: data.id,
        username: data.username,
        email: data.email,
        role: data.role,
        permissions: data.permissions
      }),
      token: new Token(tokenString, data.token?.expiresAt || data.expiresAt)
    };
  }

  async getAllUsers() {
    return await this.request("/api/users", {
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

  async getById(userId) {
    return await this.request(`/api/users/${userId}`, {
      method: "GET"
    });
  }

  async adminUpdateUser(userId, userData) {
    return await this.request(`/api/users/${userId}/admin`, {
      method: "PUT",
      body: JSON.stringify(userData)
    });
  }

  async register(registrationData) {
    return await this.request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(registrationData)
    });
  }
}
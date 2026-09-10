// infrastructure/API/ApiAuthRepository.js
import { BaseApiRepository } from "./BaseApiRepository.js";
import { User } from "../../domain/entities/User.js";
import { Token } from "../../domain/entities/Token.js";
import { DeviceUtils } from '../UI/utils/DeviceUtils.js';
import { DpopUtils } from '../UI/utils/DpopUtils.js';

export class ApiAuthRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
  }

  async login(email, username, password) {
    const endpoint = "/api/auth/login";

    let headers = {};
    try {
      const dpopProof = await DpopUtils.generateProof("POST", endpoint);
      headers["DPoP"] = dpopProof;
    } catch (e) {
      console.warn("DPoP generation skipped or unsupported:", e);
    }

    const data = await this.request(endpoint, {
      method: "POST",
      headers: headers,
      body: JSON.stringify({ 
        email, 
        username, 
        password,
        deviceId: DeviceUtils.getDeviceId(),
        deviceName: DeviceUtils.getDeviceName(),
        deviceType: DeviceUtils.getDeviceType()
      })
    });

    const tokenString = data.token?.token || data.token || data.accessToken;
    this.setToken(tokenString); 

    return {
      user: new User({
        id: data.id || data.userId,
        username: data.username,
        permissions: data.permissions || data.pageAccess
      }),
      accessToken: new Token(tokenString, data.token?.expiresAt || data.expiresAt || data.expiration || data.expiresIn),
      refreshToken: new Token(data.refreshToken, data.refreshTokenExpiration)
    };
  }

  async register(registrationData) {
    return await this.request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(registrationData)
    });
  }
}
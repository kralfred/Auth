// infrastructure/API/ApiAuthRepository.js
import { BaseApiRepository } from "./BaseApiRepository.js";
import { User } from "../../domain/entities/User.js";
import { Token } from "../../domain/entities/Token.js";
import { DeviceUtils } from '../UI/utils/DeviceUtils.js';

export class ApiAuthRepository extends BaseApiRepository {
    constructor(client, deviceInfo) {
        super(client);
        this.deviceInfo = deviceInfo;
    }

  async login(email, username, password) {
    const data = await this.request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ 
        email, 
        username, 
        password,
        deviceId:   this.deviceInfo.getDeviceId(),
        deviceName: this.deviceInfo.getDeviceName(),
        deviceType: this.deviceInfo.getDeviceType()
      })
    });

    const tokenString = data.token?.token || data.token || data.accessToken;


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
// infrastructure/API/ApiTokenRepository.js
import { BaseApiRepository } from "./BaseApiRepository.js";
import { User } from "../../domain/entities/User.js";
import { Token } from "../../domain/entities/Token.js";

export class ApiTokenRepository extends BaseApiRepository {
  constructor(baseUrl) {
    super(baseUrl);
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
        id: data.userId,
        username: data.username,
        permissions: data.permissions
      })
    };
  }

  async refreshToken(refreshTokenString) {
    const data = await this.request("/api/auth/refresh", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${refreshTokenString}`, 
        "Content-Type": "application/json"
      }
    });

    const newTokenString = data.token?.token || data.token || data.accessToken;
    this.setToken(newTokenString);

    return {
      user: new User({
        id: data.id || data.userId,
        username: data.username,
        permissions: data.permissions || data.pageAccess
      }),
      accessToken: new Token(newTokenString, data.token?.expiresAt || data.expiresAt),
      refreshToken: new Token(data.refreshToken || refreshTokenString, data.refreshTokenExpiration)
    };
  }
}
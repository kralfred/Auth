import { TokenRepository } from "../../domain/irepositories/TokenRepository.js";
import { Token } from "../../domain/entities/Token.js";

export class CookieTokenRepository extends TokenRepository {

  // --- Private Generic Cookie Helpers ---

  _getCookie(cookieName) {
    const name = `${cookieName}=`;
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i].trim();
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length);
      }
    }
    return null;
  }

  _saveCookie(cookieName, tokenObject, defaultMaxAge = 1200) {
    const value = typeof tokenObject === 'object' ? tokenObject?.value : tokenObject;

    if (!value || value === "undefined") {
      console.error(`Attempted to save an invalid ${cookieName} string!`);
      return;
    }

    let maxAge = defaultMaxAge;
    if (tokenObject?.expiresAt) {
      const nowInSeconds = Math.floor(Date.now() / 1000);
      if (tokenObject.expiresAt > nowInSeconds) {
        maxAge = tokenObject.expiresAt - nowInSeconds;
      } else if (tokenObject.expiresAt < 86400 * 30) {
        maxAge = tokenObject.expiresAt;
      }
    }

    document.cookie = `${cookieName}=${value}; max-age=${maxAge}; path=/; SameSite=Lax`;
  }

  _clearCookie(cookieName) {
    document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Lax`;
  }

  // --- Domain Interface Implementation ---

  getSavedAccessToken() {
    const value = this._getCookie("access_token");
    return value ? new Token(value, null) : null;
  }

  getSavedRefreshToken() {
    const value = this._getCookie("refresh_token");
    return value ? new Token(value, null) : null;
  }

  saveAccessToken(accessToken) {
    this._saveCookie("access_token", accessToken, 1200); // 20 minutes default
  }

  saveRefreshToken(refreshToken) {
    this._saveCookie("refresh_token", refreshToken, 604800); // 7 days default
  }


  clearToken() {
    this._clearCookie("access_token");
    this._clearCookie("refresh_token");
  }
}
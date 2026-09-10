// infrastructure/UI/utils/JwtUtils.js
export class JwtUtils {
  static getExpiration(tokenString) {
    try {
      if (!tokenString || typeof tokenString !== 'string') return null;
      const base64Url = tokenString.split('.')[1];
      if (!base64Url) return null;
      
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      
      const parsed = JSON.parse(jsonPayload);
      return parsed.exp || null; // Returns Unix timestamp in seconds
    } catch (e) {
      console.warn("Failed to parse JWT exp claim:", e);
      return null;
    }
  }
}
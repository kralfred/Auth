// application/services/AuthService.js
import { Logger } from '../../infrastructure/UI/utils/Logger.js';

export class AuthService {
  constructor(userRepository, tokenService, appState) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
    this.appState = appState;
  }

  async login(email, username, password) {
    try {
      const result = await this.userRepository.login(email, username, password);
      await this.tokenService.applyTokensAndState(result.user, result.accessToken, result.refreshToken);

      const redirectUrl = this.appState.getRedirectUrl() || "/home";
      this.appState.setRedirectUrl(null);
      window.location.hash = redirectUrl;
    } catch (err) {
      if (err.message.includes("AUTH_001") || 
            err.message.includes("Invalid credentials") || 
            err.message.includes("401") || 
            err.message.includes("403")) {

        Logger.domainError("Login failed due to authentication issue.", err.message);
      } else {
        console.error("Unhandled System Error:", err);
      }
    }
  }

  async restoreSessionOrRedirect() {
    const restored = await this.tokenService.trySilentRefresh();
    if (!restored && window.location.hash !== "#/login" && window.location.hash !== "/login") {
      window.location.hash = "/login";
    }
    
    if(restored){
        const redirectUrl = "/home";
      this.appState.setRedirectUrl(redirectUrl);
      window.location.hash = redirectUrl;
    }
  }

  logout() {
    this.tokenService.applyTokensAndState(null, null, null);
    window.location.hash = "/login";
  }

  async register(registrationData) {
    return await this.userRepository.register(registrationData);
  }
}




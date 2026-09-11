// application/services/TokenService.js
export class TokenService {
  constructor(apiRepository, tokenRepository, appState) {
    this.apiRepository = apiRepository;
    this.tokenRepository = tokenRepository;
    this.appState = appState;
  }

  async applyTokensAndState(userObject, accessToken, refreshToken) {
    console.warn("Setting user state:", userObject?.username || userObject?.id);
    this.appState.setUser(userObject);

    if (accessToken) {
      const tokenString = typeof accessToken === 'object' ? accessToken.value : accessToken;
      this.apiRepository.setToken(tokenString);
      await this.tokenRepository.saveAccessToken(accessToken);
      
      if (refreshToken) {
        await this.tokenRepository.saveRefreshToken(refreshToken);
      }
    } else {
      await this.tokenRepository.clearToken();
      this.apiRepository.setToken(null);
    }
  }

  async trySilentRefresh() {
    // 1. Await stored tokens from storage
    const accessToken = await this.tokenRepository.getSavedAccessToken();
    const refreshToken = await this.tokenRepository.getSavedRefreshToken();

    // 2. Validate Access Token
    if (accessToken?.value) {
      try {
        const resp = await this.apiRepository.validateToken(accessToken.value);
        if (resp && resp.user) {
          console.warn("Access token validated successfully" + resp);
          this.appState.setUser(resp.user);
          return true;
        }
      } catch (e) {
        console.warn("Access token validation failed, attempting refresh:", e);
      }
    }

    // 3. Fallback to Refresh Token
    if (refreshToken?.value) {
      try {
        const resp = await this.apiRepository.refreshToken(refreshToken.value);
        if (resp && resp.user) {
          console.warn("Refresh token accepted, new session issued");
          await this.applyTokensAndState(resp.user, resp.accessToken, resp.refreshToken);
          return true;
        }
      } catch (e) {
        console.warn("Refresh token invalid or expired:", e);
      }
    }

    // 4. Clear state if all checks fail
    this.appState.setUser(null);
    await this.tokenRepository.clearToken();
    this.apiRepository.setToken(null);
    return false;
  }
}
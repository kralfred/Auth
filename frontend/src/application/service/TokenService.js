// application/services/TokenService.js
export class TokenService {
  constructor(apiRepository, tokenRepository, appState, repositories = []) {
    this.apiRepository = apiRepository;
    this.tokenRepository = tokenRepository;
    this.appState = appState;
    // Keep a list of all API repositories needing token synchronization
    this.repositories = Array.isArray(repositories) ? repositories : [apiRepository];
  }

  // Helper to set token across all registered API repositories
  _setTokenOnAllRepos(tokenString) {
    this.repositories.forEach(repo => {
      if (repo && typeof repo.setToken === 'function') {
        repo.setToken(tokenString);
      }
    });
  }

  async applyTokensAndState(userObject, accessToken, refreshToken) {
    console.warn("Setting user state:", userObject?.username || userObject?.id);
    this.appState.setUser(userObject);

    if (accessToken) {
      const tokenString = typeof accessToken === 'object' ? accessToken.value : accessToken;
      
      // Update token on ALL repositories
      this._setTokenOnAllRepos(tokenString);
      
      await this.tokenRepository.saveAccessToken(accessToken);
      
      if (refreshToken) {
        await this.tokenRepository.saveRefreshToken(refreshToken);
      }
    } else {
      await this.tokenRepository.clearToken();
      this._setTokenOnAllRepos(null);
    }
  }

  async trySilentRefresh() {
    const accessToken = await this.tokenRepository.getSavedAccessToken();
    const refreshToken = await this.tokenRepository.getSavedRefreshToken();

    // 1. Validate Access Token
    if (accessToken?.value) {
      try {
        const resp = await this.apiRepository.validateToken(accessToken.value);
        if (resp && resp.user) {
          console.warn("Access token validated successfully:", resp);
          // Set user AND propagate current access token to all repos
          await this.applyTokensAndState(resp.user, accessToken, refreshToken);
          return true;
        }
      } catch (e) {
        console.warn("Access token validation failed, attempting refresh:", e);
      }
    }

    // 2. Fallback to Refresh Token
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

    // 3. Clear state if all checks fail
    await this.applyTokensAndState(null, null, null);
    return false;
  }
}
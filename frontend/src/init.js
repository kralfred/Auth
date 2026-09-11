// main.js

import { CONFIG } from './config.js';
import { ApiUserRepository } from './infrastructure/API/ApiUserRepository.js'
import { MockApiUserRepository } from '../tests/mockDb/MockApiUserRepository.js'
import { App } from './application/state/AppState.js';
import { CookieTokenRepository } from './infrastructure/storage/CookieTokenRepository.js';
import { Router } from './infrastructure/routing/Router.js';
import { getRoutes } from './infrastructure/routing/routes.js';
import { ViewFactory } from './infrastructure/UI/views/ViewFactory.js';
import { NavigationDispatcher } from './application/navigation/NavigationDispatcher.js';
import { AuthService } from './application/service/AuthService.js';
import { TokenService } from './application/service/TokenService.js';
import { ApiAuthRepository } from './infrastructure/API/ApiAuthRepository.js';
import { ApiTokenRepository } from './infrastructure/API/ApiTokenRepository.js';
import { EntityService } from './application/service/EntityService.js';
import { ApiEntityRepository } from './infrastructure/API/ApiEntityRepository.js'


const isDevelopment = false;

const apiAuthRepo = new ApiAuthRepository(CONFIG.BACKEND_URL);
const apiTokenRepo = new ApiTokenRepository(CONFIG.BACKEND_URL);
const apiUserRepo = new ApiUserRepository(CONFIG.BACKEND_URL);
const apiEntityRepo = new ApiEntityRepository(CONFIG.BACKEND_URL);


const tokenRepo = new CookieTokenRepository();
const userRepo = isDevelopment 
  ? new MockApiUserRepository() 
  : new ApiUserRepository(CONFIG.BACKEND_URL);

const appState = new App();

// Services setup
const tokenService = new TokenService(apiTokenRepo, tokenRepo, appState);
const authService = new AuthService(apiAuthRepo, tokenService, appState);
const entityService = new EntityService({
  userRepository: apiUserRepo,
  entityRepository: apiEntityRepo
});


const viewFactory = new ViewFactory(authService, appState, userRepo, entityService);
const appRoutes = getRoutes(viewFactory);

const container = document.getElementById("app");
const dispatcher = new NavigationDispatcher(appState, viewFactory, container, authService);
const router = new Router(appRoutes, dispatcher);

dispatcher.setRouter(router); 
window.addEventListener('hashchange', () => router.handleRoute());

async function init() {
  console.log("1. App initializing...");
  await authService.restoreSessionOrRedirect();
  console.log("2. Session restored. Current State:", appState.getContext());
  router.handleRoute();
  console.log("3. Router handleRoute called.");
}

init();


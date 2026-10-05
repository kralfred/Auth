
import { CONFIG } from './config.js';


import { ApiClient } from './infrastructure/API/ApiClient.js';
import { DpopUtils } from './infrastructure/UI/utils/DpopUtils.js';
import { DeviceUtils } from './infrastructure/UI/utils/DeviceUtils.js';
import { ApiUserRepository } from './infrastructure/API/ApiUserRepository.js'
import { ApiAdminRepository } from './infrastructure/API/ApiAdminRepository.js'
import { MockApiUserRepository } from '../tests/mockDb/MockApiUserRepository.js'
import { App } from './application/state/AppState.js';
import { CookieTokenRepository } from './infrastructure/storage/CookieTokenRepository.js';
import { IndexedDbDpopKeyRepository } from './infrastructure/storage/IndexedDbDpopKeyRepository.js';
import { InMemoryDpopKeyRepository } from './infrastructure/storage/InMemoryDpopKeyRepository.js';
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
import { AdminService } from './application/service/AdminService.js';


const isDevelopment = false;
const useIndexedDb = true;

const keyRepo = useIndexedDb
  ? new IndexedDbDpopKeyRepository()
  : new InMemoryDpopKeyRepository();


const dpop = new DpopUtils(keyRepo);

const client = new ApiClient(CONFIG.BACKEND_URL, dpop);

const deviceInfo = new DeviceUtils();

const apiAuthRepo   = new ApiAuthRepository(client, deviceInfo);
const apiTokenRepo  = new ApiTokenRepository(client);
const apiUserRepo   = new ApiUserRepository(client);
const apiEntityRepo = new ApiEntityRepository(client);
const apiAdminRepo  = new ApiAdminRepository(client);


const tokenRepo = new CookieTokenRepository();
const userRepo = isDevelopment 
  ? new MockApiUserRepository() 
  : new ApiUserRepository(CONFIG.BACKEND_URL);

const appState = new App();

const tokenService = new TokenService(apiTokenRepo, tokenRepo, appState, client);
const authService = new AuthService(apiAuthRepo, tokenService, appState);
const entityService = new EntityService({
  userRepository: apiUserRepo,
  entityRepository: apiEntityRepo
});
const adminService = new AdminService(apiAdminRepo);

const viewFactory = new ViewFactory(authService, appState, userRepo, entityService, adminService);
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


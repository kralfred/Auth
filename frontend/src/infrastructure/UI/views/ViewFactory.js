
import { LoginView } from './LoginView.js'
import { RegisterView } from './RegisterView.js'
import { HomeView } from './HomeView.js'
import { UserManagementView } from './UserManagementView.js';
import { LogsView } from './LogsView.js'
import { PermissionView } from './PermissionView.js'

export class ViewFactory {
  constructor(authService, appState, userRepository, apiService, entityService) {
    this.authService = authService;
    this.appState = appState; 
    this.userRepository = userRepository;
    this.apiService = apiService;
    this.entityService = entityService;
  }

  getLoginView() {
    return new LoginView(this.authService);
  }

  getRegisterView() {
    return new RegisterView(this.authService);
  }

  getHomeView() {
    return new HomeView(this.appState);
  }
  getAdminUserView() {
    return new UserManagementView(this.appState, this.userRepository);
  }
  getLogsView(){
    return new LogsView(this.apiService, this.entityService);
  }
  getPermissionView(){
    return new PermissionView(this.entityService);
  }
 
}
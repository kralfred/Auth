import { PermissionView } from './PermissionView.js';
import { LoginView } from './LoginView.js'
import { RegisterView } from './RegisterView.js'
import { AdminPermissionView } from './AdminPermissionView.js'
import { HomeView } from './HomeView.js'
import { UserManagementView } from './UserManagementView.js';
import { LogsView } from './LogsView.js'

export class ViewFactory {
  constructor(authService, appState, userRepository, entityService, adminService) {
    this.authService = authService;
    this.appState = appState; 
    this.userRepository = userRepository;
    this.entityService = entityService;
    this.adminService = adminService;
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
    return new LogsView(this.entityService);
  }


  getAdminPermissionView(){
    return new AdminPermissionView(this.adminService, this.entityService);
  }

}
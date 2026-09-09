// infrastructure/ui/ViewFactory.js

import { LoginView } from './LoginView.js'
import { RegisterView } from './RegisterView.js'
import { HomeView } from './HomeView.js'
import { UserManagementView } from './UserManagementView.js';


export class ViewFactory {
  constructor(authService, appState, userRepository) {
    this.authService = authService;
    this.appState = appState; 
    this.userRepository = userRepository;
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
 
}
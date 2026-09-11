import { LoginView } from "../UI/views/LoginView.js";
import { RegisterView } from "../UI/views/RegisterView.js";
import { HomeView } from "../UI/views/HomeView.js";


export const getRoutes = (viewFactory) => ([ 
  {
    path: "/login",
    protected: false,
    createView: () => viewFactory.getLoginView()
  },
  {
    path: "/register",
    protected: false,
    createView: () => viewFactory.getRegisterView()
  },
  {
    path: "/app",
    protected: true,
    createView: () => viewFactory.getHomeView()
  },
  {
    path: "/home", 
    protected: true,
    createView: () => viewFactory.getHomeView() 
  },
  {
    path: "/admin/view/users",
    protected: true,
    requiredPermission: "can_view_users",
    createView: () => viewFactory.getAdminUserView()
  },
  {
    path: "/logs",
    protected: true,
    createView: () => viewFactory.getLogsView()
  },
  { 
      path: "/admin/permissions", 
      protected: true, 
      createView: () => viewFactory.getPermissionView() 
    }
  
]);
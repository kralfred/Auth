// infrastructure/UI/components/Navbar.js
export class Navbar {
  constructor(authService, appState, navigationDispatcher) {
    this.authService = authService;
    this.appState = appState;
    this.navigationDispatcher = navigationDispatcher;
  }

  render() {
    const nav = document.createElement("nav");
    nav.className = "navbar"; 
    Object.assign(nav.style, {
      display: "flex",
      justifyContent: "space-between", 
      alignItems: "center",
      padding: "10px 20px",
      backgroundColor: "#2c3e50",
      color: "white"
    });

    const user = this.appState.getUser();

    const brand = document.createElement("div");
    brand.textContent = "🏨 ReservationApp";
    brand.style.fontWeight = "bold";
    nav.appendChild(brand);

    const actions = document.createElement("div");
    actions.style.display = "flex";
    actions.style.alignItems = "center";

    if (user) {
      const userPermissions = user.permissions || [];

      // Map permission keys to link names and route paths
      const permissionRoutes = [
        { permission: "view_users", label: "Admin Panel", path: "/admin/view/users" },
        { permission: "string", label: "Logs", path: "/admin/view/logs" },
        { permission: "MANAGE_SYSTEM_PERMISSIONS", label: "Permissions", path: "/admin/permissions" },
        { permission: "view_permissions", label: "Permissions", path: "/admin/permissions" }
      ];

      // Render links for permissions present in user's permissions array
      permissionRoutes.forEach(item => {
        if (userPermissions.includes(item.permission)) {
          const navLink = document.createElement("a");
          navLink.textContent = item.label;
          navLink.href = "#";
          navLink.style.marginRight = "20px";
          navLink.style.color = "#ecf0f1";
          navLink.onclick = (e) => {
            e.preventDefault();
            this.navigationDispatcher.dispatch(item.path);
          };
          actions.appendChild(navLink);
        }
      });

      const userInfo = document.createElement("span");
      userInfo.innerHTML = `
        <small style="display:block; font-size: 0.7em; color: #bdc3c7;">${user.role || 'User'}</small>
        ${user.username}
      `;
      userInfo.style.marginRight = "15px";

      const logoutBtn = document.createElement("button");
      logoutBtn.textContent = "Logout";
      logoutBtn.style.padding = "5px 10px";
      logoutBtn.onclick = () => this.handleLogout();

      actions.append(userInfo, logoutBtn);
    }

    nav.appendChild(actions);
    return nav;
  }

  async handleLogout() {
    await this.authService.logout();
  }
}
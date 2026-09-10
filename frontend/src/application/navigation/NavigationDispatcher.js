
import { Navbar } from "../../infrastructure/UI/components/Navbar.js";

export class NavigationDispatcher {
  constructor(appState, viewFactory, container, authService) {
    this.appState = appState;
    this.viewFactory = viewFactory;
    this.container = container;
    this.authService = authService;
    this.router = null; 
    
    this.appState.subscribe(() => {
      this.handleStateChange();
    });
  }

  setRouter(router) {
    this.router = router;
  }


handleStateChange() {
    const isAuth = this.appState.isAuthenticated();
    const currentHash = window.location.hash || "#/home";
    const currentPath = currentHash.replace("#", "");


    if (!isAuth && currentPath === "/login") {
        return; 
    }

          console.log("Current path:  " + currentPath);
      console.log("Current hash:  " + currentHash);

    if (isAuth && (currentPath === "/login" || currentPath === "/register")) {

        window.location.hash = "/app";
    } else if (!isAuth) {
      this.appState.setRedirectUrl(currentPath);
        window.location.hash = "/login";
    }  
    else {
        this.reDispatch();
    }
}

  reDispatch() {
    if (this.router) {
      this.router.handleRoute();
    }
  }

  async dispatch(routeOrPath) {
    const context = this.appState.getContext();
    if (context === 'LOADING') return;


    let route = routeOrPath;
    if (typeof routeOrPath === 'string') {
      route = this.router.findRoute(routeOrPath);
    }

    if (!route) {
      console.error("No route found for:", routeOrPath);
      return;
    }

    const isAuth = this.appState.isAuthenticated();


    if (route.protected && !isAuth) {
      this.authService.handleUnauthorizedAccess(window.location.hash);
      return;
    }
   
    const view = route.createView(); 
    this.render(view);
  }

  render(view) {
    console.log("Dispatcher rendering view:", view);
  if (!view || typeof view.render !== 'function') {
    console.error("Dispatcher Error: Invalid view", view);
    return;
  }

    this.container.innerHTML = ""; 


    if (this.appState.isAuthenticated()) {
      const navbar = new Navbar(this.authService, this.appState, this);
      this.container.appendChild(navbar.render());
    }


    const pageElement = view.render();
  console.log("Page element generated:", pageElement);
  this.container.appendChild(pageElement);
  }
}
// application/services/AuthService.js
// application/services/AuthService.js
export class AuthService {
    constructor(userRepository, tokenRepository, appState) {
        this.userRepository = userRepository;    // The API Repo
        this.tokenRepository = tokenRepository;  // The LocalStorage/Cookie Repo
        this.appState = appState;                // The UI State
    }


async applyAuthentication(userObject, tokenObject) {
    if(this.appState.getRedirectUrl() != null){
     window.location.hash = this.appState.getRedirectUrl();
     console.warn("Token expired or invalid ", this.appState.getRedirectUrl());
    }
        this.appState.setUser(userObject);

        if (tokenObject) {
            const tokenString = typeof tokenObject === 'object' ? tokenObject.value : tokenObject;
            

            this.userRepository.setToken(tokenString);
            
            this.tokenRepository.saveToken(tokenObject);
        } else {
            this.tokenRepository.clearToken();
            this.userRepository.setToken(null);
        }
    }

    async login(email, username, password) {
    try {

        const result = await this.userRepository.login(email, username, password);

        await this.applyAuthentication(result.user, result.token);

        window.location.hash = "/home";
    } catch (error) {
        console.error("Login Error:", error.message);
        throw error;
    }
}


async handleUnauthorizedAccess() {
    const tokenObject = this.tokenRepository.getToken(); 
    
console.warn("Token " + JSON.stringify(tokenObject));
    if (tokenObject && tokenObject !== "undefined") { 
        try {
             
            const response = await this.userRepository.validateToken(tokenObject.value);
            if (response) {
                this.applyAuthentication(response.user, response.token);
                return;
            }
        } catch (e) {
            console.warn("Token expired or invalid", e);
        }
    }

    this.applyAuthentication(null, null);
    if (window.location.hash !== "#/login") {
        window.location.hash = "/login";
    }
}

    logout() {
        this.tokenRepository.clearToken();
        window.location.hash = "/login";
        this.applyAuthentication(null, null);
        
    }
    async register(registrationData) {
    try {
        const response = await this.userRepository.register(registrationData);
        return response;
    } catch (error) {
        console.error("Registration Error:", error.message);
        throw error;
    }
}
}
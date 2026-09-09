// application/services/AuthService.js
import { Logger } from '../../infrastructure/UI/utils/Logger.js';
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
    } catch (err) {
        // Updated condition to include AUTH_001 and 401/403 errors
        if (
            err.message.includes("AUTH_001") || 
            err.message.includes("Invalid credentials") || 
            err.message.includes("401") || 
            err.message.includes("403")
        ) {
            Logger.domainError("Login failed due to authentication issue.", err.message);
        } else {
            // Standard system error for unhandled runtime crashes or server errors
            console.error("Unhandled System Error:", err);
        }
    }
}


async handleUnauthorizedAccess() {
    const tokenObject = this.tokenRepository.getToken(); 
    
console.warn("Token " + JSON.stringify(tokenObject));
    if (tokenObject && tokenObject !== "undefined") { 
        console.warn("Trying to validate ", tokenObject);
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
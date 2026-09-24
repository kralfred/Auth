// infrastructure/API/BaseApiRepository.js
import { UserRepository } from "../../domain/irepositories/UserRepository.js";
import { DpopUtils } from "../UI/utils/DpopUtils.js";

export class BaseApiRepository extends UserRepository {
    constructor(baseUrl) {
        super(); 
        this.baseUrl = baseUrl;
        this.token = null;
    }

    setToken(token) {
        const tokenString = (token && typeof token === 'object') ? token.value : token;
        console.warn("BaseRepo: Storing raw token string: " + tokenString?.substring(0, 10) + "...");
        this.token = tokenString;
    }

    async request(path, options = {}) {
        const url = `${this.baseUrl}${path}`;
        const method = (options.method || "GET").toUpperCase();

        const headers = {
            "Content-Type": "application/json",
            ...options.headers
        };

        
        try {
            const dpopProof = await DpopUtils.generateProof(method, path);
            if (dpopProof) {
                headers["DPoP"] = dpopProof;
                console.warn("DPoP generation:", dpopProof);
            }
        } catch (e) {
            console.warn("DPoP generation skipped or unsupported:", e);
        }

        // Automatically attach Bearer token
        if (this.token) {
            headers["Authorization"] = `Bearer ${this.token}`;
        }

        console.log("Request URL:", url);
        console.log("Request Headers:", headers);
        const response = await fetch(url, {
            ...options,
            headers: headers
        });

        if (!response.ok) {
            const errorBody = await response.json().catch(() => ({}));
            throw new Error(errorBody.message || `API Error: ${response.status}`);
        }

        return response.json();
    }
}
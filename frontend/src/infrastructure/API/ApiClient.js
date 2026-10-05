// infrastructure/API/ApiClient.js

export class ApiClient {
    constructor(baseUrl, dpopUtils) {
        this.baseUrl = baseUrl;
        this.dpop = dpopUtils;
        this.token = null;
    }

    setToken(token) {
        this.token = (token && typeof token === 'object') ? token.value : token;
    }

    getToken() {
        return this.token;
    }

    async request(path, options = {}) {
    const url = `${this.baseUrl}${path}`;
    const method = (options.method || "GET").toUpperCase();

    // The one and only token for this request.
    const token = options.token ?? this.token;

    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };

    // Never let an options.headers.Authorization survive — replace it below
    // with the header derived from `token`.
    delete headers.Authorization;

    try {
        const proof = await this.dpop.generateProof(method, url, token);
        if (proof) headers["DPoP"] = proof;
    } catch (e) {
        console.warn("DPoP generation failed:", e);
    }

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        credentials: "include",
        ...options,
        headers
    });

    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.message || `API Error: ${response.status}`);
    }
    return response.json();
}
}
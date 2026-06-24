export const CONFIG = {
    BACKEND_URL: window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://amazing-api-460348586740.europe-central2.run.app"
};
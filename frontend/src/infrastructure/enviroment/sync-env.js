import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const envPath = path.resolve(__dirname, '../../../../secret.env'); 
const configPath = path.resolve(__dirname, '../../config.js');

/**
 * 1. Priority: Check if the variable is already set in the system environment 
 * (This is how Vercel/Docker/Production hosts provide variables)
 */
let backendUrl = process.env.BACKEND_URL;

/**
 * 2. Fallback: If not in environment, try to read from the local file
 */
if (!backendUrl) {
    try {
        if (fs.existsSync(envPath)) {
            const envContent = fs.readFileSync(envPath, 'utf8');
            const match = envContent.match(/BACKEND_URL=(.*)/);
            if (match) {
                backendUrl = match[1].trim();
                console.log("ℹ️ Using BACKEND_URL from secret.env file.");
            }
        }
    } catch (err) {
        console.warn("⚠️ Could not read secret.env file, checking final defaults.");
    }
} else {
    console.log("🚀 Using BACKEND_URL from system environment variables.");
}

/**
 * 3. Final Default: If both fail, use localhost
 */
const finalUrl = backendUrl || 'https://amazing-api-460348586740.europe-central2.run.app';

try {
    const fileContent = `export const CONFIG = {
    API_BASE_URL: "${finalUrl}"
};`;

    fs.writeFileSync(configPath, fileContent);
    console.log(`✅ config.js updated with: ${finalUrl}`);
} catch (err) {
    console.error("❌ Error writing config.js:", err.message);
}
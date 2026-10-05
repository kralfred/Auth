// infrastructure/device/BrowserDeviceInfoProvider.js
import { DeviceInfoProvider } from "./src/domain/irepositories/DeviceInfoProvider.js";

export class BrowserDeviceInfoProvider extends DeviceInfoProvider {
    getDeviceId() {
        let id = localStorage.getItem("device_id");
        if (!id) {
            id = crypto.randomUUID();
            localStorage.setItem("device_id", id);
        }
        return id;
    }

    getDeviceName() {
        return `${navigator.platform || "Browser"} on ${navigator.userAgentData?.platform || "Unknown"}`;
    }

    getDeviceType() {
        const ua = navigator.userAgent;
        if (/Mobi|Android|iPhone/i.test(ua)) return "mobile";
        if (/Tablet|iPad/i.test(ua)) return "tablet";
        return "desktop";
    }
}
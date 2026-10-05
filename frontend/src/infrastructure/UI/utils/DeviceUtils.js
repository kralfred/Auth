import { DeviceInfoProvider } from "../../../domain/irepositories/DeviceInfoProvider.js";

export class DeviceUtils extends DeviceInfoProvider {
  getDeviceId() {
    let deviceId = localStorage.getItem("device_id");
    if (!deviceId) {
      deviceId = crypto.randomUUID();
      localStorage.setItem("device_id", deviceId);
    }
    return deviceId;
  }

  getDeviceName() {
    const ua = navigator.userAgent;
    if (ua.includes("Chrome"))  return "Chrome Browser";
    if (ua.includes("Firefox")) return "Firefox Browser";
    if (ua.includes("Safari"))  return "Safari Browser";
    return "Web Client";
  }

  getDeviceType() {
    return /Mobi|Android/i.test(navigator.userAgent) ? "MOBILE" : "WEB";
  }
}
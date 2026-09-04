// infrastructure/utils/DeviceUtils.js

export class DeviceUtils {
  static getDeviceId() {
    let deviceId = localStorage.getItem("device_id");
    if (!deviceId) {
      deviceId = crypto.randomUUID(); // Native browser UUID generation
      localStorage.setItem("device_id", deviceId);
    }
    return deviceId;
  }

  static getDeviceName() {
    const userAgent = navigator.userAgent;
    if (userAgent.includes("Chrome")) return "Chrome Browser";
    if (userAgent.includes("Firefox")) return "Firefox Browser";
    if (userAgent.includes("Safari")) return "Safari Browser";
    return "Web Client";
  }

  static getDeviceType() {
    return /Mobi|Android/i.test(navigator.userAgent) ? "MOBILE" : "WEB";
  }
}
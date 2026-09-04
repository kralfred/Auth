// infrastructure/utils/DpopUtils.js

export class DpopUtils {
  static keyPair = null;

  static async getKeyPair() {
    if (!this.keyPair) {
      this.keyPair = await window.crypto.subtle.generateKey(
        {
          name: "RSASSA-PKCS1-v1_5", // Standard RSA algorithm for RS256
          modulusLength: 2048,
          publicExponent: new Uint8Array([0x01, 0x00, 0x01]), // 65537
          hash: { name: "SHA-256" }
        },
        false, // non-extractable private key
        ["sign", "verify"]
      );
    }
    return this.keyPair;
  }

  static async generateProof(htm, htu) {
    const { publicKey, privateKey } = await this.getKeyPair();
    const jwk = await window.crypto.subtle.exportKey("jwk", publicKey);

    const header = {
      typ: "dpop+jwt",
      alg: "RS256", // Matches RSA key type expected by Nimbus
      jwk: {
        kty: jwk.kty,
        n: jwk.n,
        e: jwk.e
      }
    };

    const payload = {
      jti: crypto.randomUUID(),
      htm: htm.toUpperCase(),
      htu: htu,
      iat: Math.floor(Date.now() / 1000)
    };

    const base64UrlEncode = (obj) =>
      btoa(JSON.stringify(obj))
        .replace(/=/g, "")
        .replace(/\+/g, "-")
        .replace(/\//g, "_");

    const unsignedToken = `${base64UrlEncode(header)}.${base64UrlEncode(payload)}`;

    const signature = await window.crypto.subtle.sign(
      { name: "RSASSA-PKCS1-v1_5" },
      privateKey,
      new TextEncoder().encode(unsignedToken)
    );

    const base64Signature = btoa(String.fromCharCode(...new Uint8Array(signature)))
      .replace(/=/g, "")
      .replace(/\+/g, "-")
      .replace(/\//g, "_");

    return `${unsignedToken}.${base64Signature}`;
  }
}
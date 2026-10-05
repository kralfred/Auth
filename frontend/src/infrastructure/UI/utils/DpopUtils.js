export class DpopUtils {
  constructor(keyRepository) {
    this.keyRepo = keyRepository;
  }

  async generateProof(htm, htu, token) {
    let pair = await this.keyRepo.getKeyPair();
    if (!pair) {
      pair = await this.#generateAndStore();
    }

    const jwk = await crypto.subtle.exportKey("jwk", pair.publicKey);
    const header = { typ: "dpop+jwt", alg: "RS256", jwk: { kty: jwk.kty, n: jwk.n, e: jwk.e } };

    const payload = {
      jti: crypto.randomUUID(),
      htm: htm.toUpperCase(),
      htu,
      iat: Math.floor(Date.now() / 1000),
      ...(token ? { ath: await this.#computeAth(token) } : {})
    };

    const unsigned = `${this.#encJson(header)}.${this.#encJson(payload)}`;
    const sig = await crypto.subtle.sign(
      { name: "RSASSA-PKCS1-v1_5" }, pair.privateKey,
      new TextEncoder().encode(unsigned));
    return `${unsigned}.${this.#encBytes(new Uint8Array(sig))}`;
  }

  async #generateAndStore() {
    const pair = await crypto.subtle.generateKey(
      { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048,
        publicExponent: new Uint8Array([1,0,1]), hash: "SHA-256" },
      false, ["sign", "verify"]);
    await this.keyRepo.saveKeyPair(pair);
    return pair;
  }

  async #computeAth(accessToken) {
    const hash = await crypto.subtle.digest(
      "SHA-256", new TextEncoder().encode(accessToken));
    return this.#encBytes(new Uint8Array(hash));
  }

  #encJson(obj) { return this.#encBytes(new TextEncoder().encode(JSON.stringify(obj))); }

  #encBytes(bytes) {
    let s = ""; for (const b of bytes) s += String.fromCharCode(b);
    return btoa(s).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  }
}
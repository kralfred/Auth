import { DpopKeyRepository } from "../../domain/irepositories/DpopKeyRepository.js";

export class InMemoryDpopKeyRepository extends DpopKeyRepository {
  #keyPair = null;

  async getKeyPair() {
    if (!this.#keyPair) {
      this.#keyPair = await crypto.subtle.generateKey(
        { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048,
          publicExponent: new Uint8Array([1,0,1]), hash: "SHA-256" },
        false, ["sign", "verify"]
      );
    }
    return this.#keyPair;
  }

  async saveKeyPair(pair) { this.#keyPair = pair; }
  async clearKeyPair()    { this.#keyPair = null; }
}
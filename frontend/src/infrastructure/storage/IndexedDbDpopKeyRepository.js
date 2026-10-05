import { DpopKeyRepository } from "../../domain/irepositories/DpopKeyRepository.js";

export class IndexedDbDpopKeyRepository extends DpopKeyRepository {

  constructor(dbName = "dpop", storeName = "keys") {
    super();
    this.dbName = dbName;
    this.storeName = storeName;
    this._dbPromise = null;
  }

  #open() {
    if (!this._dbPromise) {
      this._dbPromise = new Promise((resolve, reject) => {
        const req = indexedDB.open(this.dbName, 1);
        req.onupgradeneeded = () => req.result.createObjectStore(this.storeName);
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject(req.error);
      });
    }
    return this._dbPromise;
  }

  async getKeyPair() {
    const db = await this.#open();
    return new Promise((resolve, reject) => {
      const req = db.transaction(this.storeName, "readonly")
                    .objectStore(this.storeName)
                    .get("main");
      req.onsuccess = () => resolve(req.result || null);
      req.onerror = () => reject(req.error);
    });
  }

  async saveKeyPair(pair) {
    const db = await this.#open();
    return new Promise((resolve, reject) => {
      const tx = db.transaction(this.storeName, "readwrite");
      tx.objectStore(this.storeName).put(pair, "main");
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }

  async clearKeyPair() {
    const db = await this.#open();
    return new Promise((resolve, reject) => {
      const tx = db.transaction(this.storeName, "readwrite");
      tx.objectStore(this.storeName).delete("main");
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }
}
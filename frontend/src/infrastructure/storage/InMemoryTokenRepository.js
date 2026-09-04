// infrastructure/storage/InMemoryTokenRepository.js
import { TokenRepository } from "../../domain/irepositories/TokenRepository.js";
import { Token } from "../../domain/entities/Token.js";

export class InMemoryTokenRepository extends TokenRepository {
  constructor() {
    super();
    this.inMemoryToken = null;
  }

  getToken() {
    if (!this.inMemoryToken) return null;
    return new Token(this.inMemoryToken, null);
  }

  saveToken(tokenObject) {
    if (!tokenObject) {
      this.clearToken();
      return;
    }
    const value = typeof tokenObject === 'object' ? tokenObject.value : tokenObject;
    this.inMemoryToken = value;
  }

  clearToken() {
    this.inMemoryToken = null;
  }
}
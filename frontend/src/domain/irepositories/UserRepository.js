export class UserRepository {
/**
   * @param {string} token
   * @returns {Promise<User|null>}
   */

  async validateToken(token) {
    throw new Error("Not implemented")
  }
  async refreshToken(token) {
    throw new Error("Not implemented")
  }
  async login(email,username, password){
    throw new Error("Not implemented");
  }
  async findById(userId){
    throw new Error("Not implemented");
  }
  /**
   * @returns {Promise<User[]>} 
   */
  async getAllUsers(){
    throw new Error("Not implemented");
  }
  async register(){
    throw new Error("Not implemented");
  }
  async adminUpdateUser(){
    
  }
}
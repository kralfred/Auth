
export class User {
  constructor({ id, username, email, role = 'guest', permissions = [] }) {
    this.id = id;
    this.username = username;
    this.permissions = permissions;
  }


  can(permission) {
    return this.isAdmin() || this.permissions.includes(permission);
  }
}
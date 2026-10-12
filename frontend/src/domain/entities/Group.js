export class Group {
    constructor(raw) {
        this.id = raw.id;
        this.name = raw.name;
        this.config = new GroupConfig(raw);
    }
}

class GroupConfig {
    constructor(raw) {
        this.baseUrl = raw.baseUrl;
        this.maxMembers = raw.maxMembers;
        this.accessTokenTime = raw.accessTokenTime;
        this.refreshTokenTime = raw.refreshTokenTime;
    }
}
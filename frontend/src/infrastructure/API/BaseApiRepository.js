// infrastructure/API/BaseApiRepository.js
import { UserRepository } from "../../domain/irepositories/UserRepository.js";
import { DpopUtils } from "../UI/utils/DpopUtils.js";

export class BaseApiRepository {
    constructor(client) {
        this.client = client;
    }

    request(path, options) {
        return this.client.request(path, options);
    }
}
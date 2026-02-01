import { User } from "./User";

export interface LoginResponse {
    accessToken: string,
    tokenType: string,
    user: User
}

export interface LoginRequest {
    email: string,
    password: string,
}

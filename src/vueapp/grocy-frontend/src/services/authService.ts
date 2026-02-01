import api from "@/services/api";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import type { LoginRequest, LoginResponse } from "@/models/Login";

export const authService = {
	async login(payload: LoginRequest): Promise<LoginResponse> {
		const res = await api.post<LoginResponse>("/auth/login", payload);
		localStorage.setItem(ACCESS_TOKEN_KEY, res.data.accessToken);
		return res.data;
	}
};

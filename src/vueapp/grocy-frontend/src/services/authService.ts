import api from "@/services/api";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import type { LoginRequest, LoginResponse } from "@/models/Login";
import type { RegisterRequest, RegisterResponse } from "@/models/Register";

export const authService = {
	async register(payload: RegisterRequest): Promise<RegisterResponse> {
		const res = await api.post<RegisterResponse>("/auth/register", payload);
		return res.data;
	},

	async login(payload: LoginRequest): Promise<LoginResponse> {
		const res = await api.post<LoginResponse>("/auth/login", payload);
		localStorage.setItem(ACCESS_TOKEN_KEY, res.data.accessToken);
		return res.data;
	},

	async verifyEmail(token: string) {
		return api.post("/auth/verify-email", { token });
	},

	async resendVerification(email: string) {
		return api.post("/auth/resend-verification", { email });
	},

	async forgotPassword(email: string) {
		return api.post("/auth/forgot-password", { email });
	},

	async resetPassword(token: string, password: string) {
		return api.post("/auth/reset-password", { token, password });
	}
};

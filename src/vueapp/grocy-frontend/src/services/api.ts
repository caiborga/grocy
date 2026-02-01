import axios, {
    AxiosHeaders,
	type AxiosError,
	type AxiosInstance,
	type AxiosResponse,
	type InternalAxiosRequestConfig
} from "axios";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";

const api: AxiosInstance = axios.create({
	baseURL: "/api",
	headers: {
		"Content-Type": "application/json"
	},
	withCredentials: true
});

// Request interceptor: Bearer Token anhängen
api.interceptors.request.use(
	(config: InternalAxiosRequestConfig) => {
		const token = localStorage.getItem(ACCESS_TOKEN_KEY);

		if (token) {
			config.headers = AxiosHeaders.from(config.headers);
			config.headers.Authorization = `Bearer ${token}`;
		}

		return config;
	},
	(error: AxiosError) => Promise.reject(error)
);

// Response interceptor: nur data zurückgeben + Auth-Handling
api.interceptors.response.use(
	(response: AxiosResponse) => response,
	(error: AxiosError) => {
		const status = error.response?.status;
		const url = (error.config?.url ?? "") as string;

		if (url.startsWith("/auth")) {
			return Promise.reject(error);
		}

		if (
			(status === 401 || status === 403) &&
			!window.location.pathname.startsWith("/login")
		) {
			localStorage.removeItem(ACCESS_TOKEN_KEY);

			const redirect = window.location.pathname + window.location.search;
			window.location.href = `/login?redirect=${encodeURIComponent(
				redirect
			)}`;
		}

		return Promise.reject(error);
	}
);

export default api;

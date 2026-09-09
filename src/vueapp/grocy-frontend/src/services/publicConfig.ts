import { ref } from "vue";
import api from "./api";

const registrationEnabled = ref(false);
let loaded = false;

export async function loadPublicConfig() {
	if (loaded) {
		return;
	}
	try {
		const res = await api.get<{ registrationEnabled: boolean }>(
			"/public/config"
		);
		registrationEnabled.value = res.data.registrationEnabled === true;
	} catch {
		registrationEnabled.value = false;
	} finally {
		loaded = true;
	}
}

export function usePublicConfig() {
	return { registrationEnabled, loadPublicConfig };
}

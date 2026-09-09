<template>
	<div class="g-page">
		<div class="g-page-inner-narrow space-y-4">
			<section class="g-panel">
				<h1 class="g-page-title">Einstellungen</h1>
				<p class="g-page-sub">
					Account, Datenexport und Löschung.
				</p>
			</section>

			<section class="g-panel">
				<h2 class="font-display text-lg font-bold text-ink mb-2">
					Deine Daten
				</h2>
				<p class="g-page-sub mb-4">
					Du kannst eine Kopie deiner personenbezogenen und
					fachlichen Daten herunterladen.
				</p>
				<el-button type="primary" round :loading="exporting" @click="exportData">
					Daten exportieren
				</el-button>
			</section>

			<section class="g-panel">
				<h2 class="font-display text-lg font-bold text-ink mb-2">
					Account löschen
				</h2>
				<p class="g-page-sub mb-4">
					Gemeinsame Haushaltsdaten anderer Mitglieder bleiben
					erhalten. Wenn du alleiniger Eigentümer eines Haushalts mit
					weiteren Mitgliedern bist, musst du das Eigentum zuerst
					übertragen.
				</p>
				<el-button type="danger" round plain :loading="deleting" @click="deleteAccount">
					Account löschen
				</el-button>
			</section>

			<LegalFooter />
		</div>
	</div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { userService } from "@/services/userService";
import { useUserStore } from "@/stores/userStore";
import LegalFooter from "@/components/LegalFooter.vue";

const router = useRouter();
const userStore = useUserStore();
const exporting = ref(false);
const deleting = ref(false);

async function exportData() {
	exporting.value = true;
	try {
		const res = await userService.exportData();
		const blob = new Blob([JSON.stringify(res.data, null, 2)], {
			type: "application/json"
		});
		const url = URL.createObjectURL(blob);
		const link = document.createElement("a");
		link.href = url;
		link.download = "grocy-export.json";
		link.click();
		URL.revokeObjectURL(url);
		ElMessage.success("Export heruntergeladen");
	} catch (e) {
		console.error(e);
		ElMessage.error("Export fehlgeschlagen. Bitte später erneut versuchen.");
	} finally {
		exporting.value = false;
	}
}

async function deleteAccount() {
	try {
		await ElMessageBox.confirm(
			"Account wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
			"Account löschen",
			{
				confirmButtonText: "Löschen",
				cancelButtonText: "Abbrechen",
				type: "warning"
			}
		);
	} catch {
		return;
	}

	deleting.value = true;
	try {
		await userService.deleteAccount();
		await userStore.logout();
		ElMessage.success("Account gelöscht");
		router.push("/");
	} catch (e) {
		const code = e?.response?.data?.code;
		if (code === "ownership_transfer_required") {
			ElMessage.error(
				"Bitte übertrage zuerst das Eigentum an Haushalten mit weiteren Mitgliedern."
			);
			router.push("/households");
		} else {
			ElMessage.error("Account konnte nicht gelöscht werden.");
		}
	} finally {
		deleting.value = false;
	}
}
</script>

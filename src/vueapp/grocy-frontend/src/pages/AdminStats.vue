<template>
	<div class="g-page">
		<div class="g-page-inner-narrow">
			<section class="g-panel">
				<h1 class="g-page-title">Statistik</h1>
				<p class="g-page-sub mb-4">
					{{ stats?.activeUserDefinition }}
				</p>
				<div v-if="loading" class="g-page-sub">Lade Statistik…</div>
				<div v-else-if="error" class="g-page-sub">{{ error }}</div>
				<dl v-else class="stats-grid">
					<div>
						<dt>Nutzer insgesamt</dt>
						<dd>{{ stats.usersTotal }}</dd>
					</div>
					<div>
						<dt>Neue Nutzer 7 Tage</dt>
						<dd>{{ stats.usersCreatedLast7Days }}</dd>
					</div>
					<div>
						<dt>Neue Nutzer 30 Tage</dt>
						<dd>{{ stats.usersCreatedLast30Days }}</dd>
					</div>
					<div>
						<dt>Aktive Nutzer 7 Tage</dt>
						<dd>{{ stats.usersActiveLast7Days }}</dd>
					</div>
					<div>
						<dt>Aktive Nutzer 30 Tage</dt>
						<dd>{{ stats.usersActiveLast30Days }}</dd>
					</div>
					<div>
						<dt>Haushalte</dt>
						<dd>{{ stats.householdsTotal }}</dd>
					</div>
					<div>
						<dt>Aktive Einladungen</dt>
						<dd>{{ stats.invitesActive }}</dd>
					</div>
					<div>
						<dt>Accountlöschungen</dt>
						<dd>{{ stats.accountDeletionsTotal }}</dd>
					</div>
				</dl>
			</section>
		</div>
	</div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import api from "@/services/api";

const loading = ref(true);
const error = ref("");
const stats = ref(null);

onMounted(async () => {
	try {
		const res = await api.get("/admin/stats");
		stats.value = res.data;
	} catch (e) {
		error.value = "Statistik nicht verfügbar.";
	} finally {
		loading.value = false;
	}
});
</script>

<style scoped>
.stats-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
	gap: 12px;
	margin: 0;
}

.stats-grid div {
	padding: 14px;
	border-radius: 1rem;
	background: #f8fafc;
}

dt {
	font-size: 0.75rem;
	color: #64748b;
}

dd {
	margin: 6px 0 0;
	font-size: 1.4rem;
	font-weight: 700;
	color: #0f172a;
}
</style>

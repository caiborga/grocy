<template>
	<main class="landing-page">
		<header class="topbar">
			<div class="topbar-inner">
				<img :src="grocyLogo" alt="Grocy Logo" class="topbar-logo" />

				<nav class="topbar-nav">
					<router-link to="/login" class="topbar-link">
						Einloggen
					</router-link>

					<router-link
						to="/register"
						class="topbar-link topbar-link-primary"
					>
						Registrieren
					</router-link>
				</nav>
			</div>
		</header>

		<section class="hero-section">
			<div class="hero-copy">
				<h1>Schluss mit Zettelchaos beim Einkaufen.</h1>

				<p class="hero-text">
					Grocy hilft dir, Einkaufslisten übersichtlich zu
					organisieren, gemeinsam im Haushalt zu nutzen und beim
					Einkauf immer den Überblick zu behalten.
				</p>

				<div class="hero-actions">
					<el-button
						type="primary"
						size="large"
						round
						@click="scrollToFeatures"
					>
						Features ansehen
					</el-button>
				</div>
			</div>

			<div class="hero-visual">
				<img :src="comicImage" alt="Grocy Comic Vorschau" />
			</div>
		</section>

		<section ref="featuresSection" class="features-wrapper">
			<div class="section-heading">
				<el-tag type="primary" effect="plain" round>Features</el-tag>
				<h2>Alles, was dein Haushalt für den Einkauf braucht.</h2>
				<p>
					Von gemeinsamen Listen bis zu Rollen, Einladungen und
					Rezepten: Grocy bringt Ordnung in den Einkaufsalltag.
				</p>
			</div>

			<div class="feature-section">
				<el-card
					v-for="feature in features"
					:key="feature.title"
					shadow="never"
					class="feature-card"
				>
					<div class="feature-icon">
						<el-icon>
							<component :is="feature.icon" />
						</el-icon>
					</div>
					<h3>{{ feature.title }}</h3>
					<p>{{ feature.text }}</p>
				</el-card>
			</div>
		</section>

		<section class="cta-section">
			<h2>Bereit für einen aufgeräumten Einkauf?</h2>
			<p>
				Öffne Grocy, teile Listen mit deinem Haushalt und vergiss das
				Zettelchaos.
			</p>
			<el-button
				type="primary"
				size="large"
				round
				tag="router-link"
				to="/register"
			>
				Jetzt loslegen
			</el-button>
		</section>
	</main>
</template>

<script setup lang="ts">
import {
	List,
	User,
	Switch,
	Lock,
	House,
	Message,
	Star,
	Plus
} from "@element-plus/icons-vue";
import { ref } from "vue";

import comicImage from "/public/grocy-comic.png";
import grocyLogo from "/public/grocy.png";

const featuresSection = ref<HTMLElement | null>(null);

const scrollToFeatures = () => {
	featuresSection.value?.scrollIntoView({
		behavior: "smooth",
		block: "start"
	});
};

const features = [
	{
		icon: List,
		title: "Einkaufslisten verwalten",
		text: "Erstelle, bearbeite und lösche Einkaufslisten genau so, wie du sie im Alltag brauchst."
	},
	{
		icon: User,
		title: "Mehrere Nutzer pro Haushalt",
		text: "Organisiere Einkäufe gemeinsam mit allen Personen, die zu deinem Haushalt gehören."
	},
	{
		icon: Switch,
		title: "Gemeinsam nutzbare Listen",
		text: "Listen können von mehreren Nutzern geöffnet und bearbeitet werden."
	},
	{
		icon: Lock,
		title: "Login & Rollen",
		text: "Authentifizierung mit Rollen für Besitzer, Bearbeiter und Betrachter."
	},
	{
		icon: House,
		title: "Aktiver Haushalt",
		text: "Jeder Nutzer arbeitet immer im aktuell ausgewählten Haushalt."
	},
	{
		icon: Message,
		title: "Einladungen",
		text: "Lade andere Personen ein, um Listen gemeinsam anzusehen oder zu bearbeiten."
	},
	{
		icon: Plus,
		title: "Rezepte erstellen",
		text: "Erstelle eigene Rezepte, um Zutaten und Einkaufslisten noch smarter zu planen."
	},
	{
		icon: Star,
		title: "Default-Liste wählen",
		text: "Lege eine Standardliste fest, damit du noch schneller mit dem Einkauf starten kannst."
	}
];
</script>

<style scoped>
.landing-page {
	min-height: 100vh;
	background:
		radial-gradient(
			circle at top left,
			rgba(59, 130, 246, 0.16),
			transparent 32rem
		),
		linear-gradient(180deg, #f8fbff 0%, #eef5ff 46%, #f8fbff 100%);
	color: #172033;
}

.hero-section,
.features-wrapper,
.feature-section,
.cta-section {
	width: min(1180px, calc(100% - 32px));
	margin: 0 auto;
}

.topbar {
	position: sticky;
	top: 0;
	z-index: 20;
	width: 100%;
	border-bottom: 1px solid rgba(148, 163, 184, 0.16);
	background: rgba(255, 255, 255);
}

.topbar-inner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: min(1180px, calc(100% - 32px));
	height: 76px;
	margin: 0 auto;
}

.topbar-nav {
	display: flex;
	align-items: center;
	gap: 22px;
}

.topbar-link {
	color: #4b5563;
	text-decoration: none;
	font-size: 0.96rem;
	font-weight: 600;
	transition: color 0.2s ease;
}

.topbar-link:hover {
	color: #2563eb;
}

.topbar-link-primary {
	color: #2563eb;
}

/* old */
.topbar-inner {
	display: flex;
	align-items: center;
	width: min(1180px, calc(100% - 32px));
	height: 76px;
	margin: 0 auto;
}

.topbar-logo {
	width: 150px;
	object-fit: contain;
}

.hero-section {
	display: grid;
	grid-template-columns: 0.9fr 1.1fr;
	align-items: center;
	gap: 48px;
	padding: 72px 0 64px;
}

.hero-copy h1 {
	margin: 0;
	max-width: 620px;
	font-size: clamp(2.5rem, 6vw, 5.25rem);
	line-height: 0.95;
	letter-spacing: -0.07em;
}

.hero-text {
	max-width: 560px;
	margin: 24px 0 0;
	color: #526070;
	font-size: 1.15rem;
	line-height: 1.7;
}

.hero-actions {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 14px;
	margin-top: 34px;
}

.action-link {
	text-decoration: none;
}

.hero-actions .el-button {
	min-width: 180px;
}

.hero-actions .el-button--primary {
	box-shadow: 0 10px 25px rgba(37, 99, 235, 0.22);
}

.hero-actions .el-button:not(.el-button--primary) {
	border-color: rgba(59, 130, 246, 0.2);
	color: #2563eb;
	background: rgba(255, 255, 255, 0.82);
}

.hero-actions .el-button:not(.el-button--primary):hover {
	background: rgba(37, 99, 235, 0.06);
}

/* old */
.hero-actions {
	display: flex;
	flex-wrap: wrap;
	gap: 14px;
	margin-top: 34px;
}

.hero-visual {
	position: relative;
	padding: 12px;
	border-radius: 34px;
	background: rgba(255, 255, 255, 0.72);
	box-shadow: 0 24px 70px rgba(37, 99, 235, 0.16);
}

.hero-visual::before {
	content: "";
	position: absolute;
	inset: -18px;
	z-index: -1;
	border-radius: 46px;
	background: linear-gradient(
		135deg,
		rgba(59, 130, 246, 0.24),
		rgba(147, 197, 253, 0.05)
	);
	filter: blur(6px);
}

.hero-visual img {
	display: block;
	width: 100%;
	aspect-ratio: 16 / 10;
	object-fit: cover;
	border-radius: 24px;
}

.features-wrapper {
	padding: 42px 0;
}

.section-heading {
	max-width: 760px;
	margin: 0 auto 34px;
	text-align: center;
}

.section-heading h2,
.cta-section h2 {
	margin: 16px 0 0;
	font-size: clamp(2rem, 4vw, 3.5rem);
	line-height: 1;
	letter-spacing: -0.045em;
}

.section-heading p,
.cta-section p {
	margin: 18px auto 0;
	color: #64748b;
	font-size: 1.05rem;
	line-height: 1.7;
}

.feature-section {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 18px;
	padding: 0;
}

.feature-card {
	border-radius: 28px;
	border-color: rgba(148, 163, 184, 0.22);
	background: rgba(255, 255, 255, 0.76);
}

.feature-icon {
	display: grid;
	place-items: center;
	width: 52px;
	height: 52px;
	margin-bottom: 18px;
	border-radius: 18px;
	color: #2563eb;
	background: linear-gradient(
		135deg,
		rgba(59, 130, 246, 0.16),
		rgba(96, 165, 250, 0.1)
	);
	border: 1px solid rgba(59, 130, 246, 0.12);
	font-size: 1.25rem;
	font-weight: 700;
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.feature-card h3 {
	margin: 0;
	font-size: 1.15rem;
	letter-spacing: -0.02em;
}

.feature-card p {
	margin: 10px 0 0;
	color: #64748b;
	line-height: 1.6;
}

.cta-section {
	margin-top: 24px;
	margin-bottom: 70px;
	padding: 58px 24px;
	border-radius: 36px;
	text-align: center;
	background: linear-gradient(135deg, #1d4ed8, #60a5fa);
	color: #ffffff;
	box-shadow: 0 22px 55px rgba(37, 99, 235, 0.22);
}

.cta-section p {
	max-width: 620px;
	color: rgba(255, 255, 255, 0.82);
}

.cta-section .el-button {
	margin-top: 28px;
}

@media (max-width: 1100px) {
	.feature-section {
		grid-template-columns: repeat(3, 1fr);
	}
}

@media (max-width: 920px) {
	.hero-section {
		grid-template-columns: 1fr;
		padding-top: 56px;
	}

	.feature-section {
		grid-template-columns: 1fr 1fr;
	}
}

@media (max-width: 640px) {
	.topbar-inner,
	.hero-section,
	.features-wrapper,
	.feature-section,
	.cta-section {
		width: min(100% - 24px, 1180px);
	}

	.hero-section {
		gap: 30px;
	}

	.hero-copy h1 {
		font-size: 3rem;
	}

	.feature-section {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 420px) {
	.topbar-logo {
		width: 120px;
	}

	.topbar-nav {
		gap: 12px;
	}

	.topbar-link {
		font-size: 0.88rem;
	}

	.hero-copy h1 {
		font-size: 2.55rem;
	}
}
</style>

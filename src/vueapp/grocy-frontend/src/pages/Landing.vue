<template>
	<main class="landing">
		<header class="topbar">
			<div class="topbar-inner">
				<a href="#top" class="brand-link" aria-label="Grocy">
					<img class="topbar-logo" src="/grocy.png" alt="Grocy Logo" />
				</a>

				<nav class="topbar-nav">
					<router-link to="/login" class="topbar-link">
						Einloggen
					</router-link>
					<router-link to="/register" class="topbar-cta">
						Registrieren
					</router-link>
				</nav>
			</div>
		</header>

		<section id="top" class="hero">
			<div class="hero-media" aria-hidden="true">
				<div class="hero-gradient" />
				<div class="hero-pattern" />
				<div class="hero-orb hero-orb-a" />
				<div class="hero-orb hero-orb-b" />
				<img
					v-if="comicAvailable"
					:src="comicImage"
					alt=""
					class="hero-img"
					@error="comicAvailable = false"
				/>
			</div>

			<div class="hero-content">
				<img
					class="hero-brand-logo animate-in"
					src="/grocy.png"
					alt="Grocy"
				/>
				<h1 class="animate-in delay-1">
					Schluss mit Zettelchaos beim Einkaufen.
				</h1>
				<p class="hero-text animate-in delay-2">
					Gemeinsame Listen, klare Rollen und Rezepte — damit dein
					Haushalt beim Einkauf den Überblick behält.
				</p>
				<div class="hero-actions animate-in delay-3">
					<router-link to="/register" class="btn-primary">
						Jetzt loslegen
					</router-link>
					<button
						type="button"
						class="btn-ghost"
						@click="scrollToFeatures"
					>
						Features ansehen
					</button>
				</div>
			</div>
		</section>

		<section ref="featuresSection" class="features">
			<div class="section-heading">
				<span class="section-tag">Features</span>
				<h2>Alles für den Einkauf im Haushalt.</h2>
				<p>
					Listen teilen, Rollen vergeben, Rezepte in den Einkauf
					übernehmen — ohne Umwege.
				</p>
			</div>

			<div class="feature-grid">
				<article
					v-for="feature in features"
					:key="feature.title"
					class="feature-item"
				>
					<div class="feature-icon">
						<el-icon :size="22">
							<component :is="feature.icon" />
						</el-icon>
					</div>
					<h3>{{ feature.title }}</h3>
					<p>{{ feature.text }}</p>
				</article>
			</div>
		</section>

		<section class="cta">
			<div class="cta-inner">
				<h2>Bereit für einen aufgeräumten Einkauf?</h2>
				<p>
					Öffne Grocy, teile Listen mit deinem Haushalt und vergiss
					das Zettelchaos.
				</p>
				<router-link to="/register" class="btn-primary btn-primary--light">
					Jetzt loslegen
				</router-link>
			</div>
		</section>
	</main>
</template>

<script setup lang="ts">
import {
	List,
	User,
	Lock,
	House,
	Message,
	Star,
	Plus,
	Share
} from "@element-plus/icons-vue";
import { ref } from "vue";

const comicImage = "/grocy-comic.png";

const featuresSection = ref<HTMLElement | null>(null);
const comicAvailable = ref(true);

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
		text: "Erstelle und bearbeite Listen genau so, wie du sie im Alltag brauchst."
	},
	{
		icon: Share,
		title: "Gemeinsam einkaufen",
		text: "Listen können von mehreren Personen im Haushalt geöffnet und bearbeitet werden."
	},
	{
		icon: House,
		title: "Haushalte & Rollen",
		text: "Jeder arbeitet im aktiven Haushalt — mit klaren Rechten für Besitzer, Bearbeiter und Betrachter."
	},
	{
		icon: Message,
		title: "Einladungen",
		text: "Lade andere per Link ein, um Listen gemeinsam anzusehen oder zu bearbeiten."
	},
	{
		icon: Plus,
		title: "Rezepte nutzen",
		text: "Lege Rezepte an und übernimm Zutaten direkt in deine Einkaufsliste."
	},
	{
		icon: Star,
		title: "Standardliste",
		text: "Lege eine Standardliste fest und starte schneller mit dem Einkauf."
	},
	{
		icon: Lock,
		title: "Sicherer Login",
		text: "Accounts mit E-Mail-Bestätigung und Passwort-Zurücksetzen."
	},
	{
		icon: User,
		title: "Mehrere Nutzer",
		text: "Organisiere Einkäufe gemeinsam mit allen, die zu deinem Haushalt gehören."
	}
];
</script>

<style scoped>
.landing {
	min-height: 100vh;
	color: #0f172a;
	background: #f4f7fc;
}

.topbar {
	position: sticky;
	top: 0;
	z-index: 20;
	border-bottom: 1px solid rgba(15, 23, 42, 0.06);
	background: rgba(255, 255, 255, 0.86);
	backdrop-filter: blur(12px);
}

.topbar-inner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: min(1180px, calc(100% - 32px));
	height: 68px;
	margin: 0 auto;
}

.brand-link {
	display: inline-flex;
	align-items: center;
	gap: 10px;
	text-decoration: none;
}

.topbar-logo {
	display: block;
	height: 36px;
	width: auto;
	max-width: 150px;
	object-fit: contain;
}

.topbar-nav {
	display: flex;
	align-items: center;
	gap: 18px;
}

.topbar-link {
	color: #475569;
	font-weight: 600;
	text-decoration: none;
	transition: color 0.2s ease;
}

.topbar-link:hover {
	color: #2563eb;
}

.topbar-cta {
	padding: 8px 14px;
	border-radius: 9999px;
	background: #2563eb;
	color: #fff;
	font-weight: 600;
	text-decoration: none;
	box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22);
	transition: background 0.2s ease, transform 0.2s ease;
}

.topbar-cta:hover {
	background: #1d4ed8;
	transform: translateY(-1px);
}

.hero {
	position: relative;
	display: grid;
	min-height: calc(100vh - 68px);
	align-items: end;
	overflow: hidden;
}

.hero-media {
	position: absolute;
	inset: 0;
}

.hero-gradient {
	position: absolute;
	inset: 0;
	background:
		linear-gradient(
			180deg,
			rgba(15, 23, 42, 0.15) 0%,
			rgba(15, 23, 42, 0.55) 55%,
			rgba(15, 23, 42, 0.78) 100%
		),
		linear-gradient(135deg, #1d4ed8 0%, #3b82f6 45%, #93c5fd 100%);
}

.hero-pattern {
	position: absolute;
	inset: 0;
	opacity: 0.18;
	background-image: radial-gradient(
		rgba(255, 255, 255, 0.45) 1px,
		transparent 1px
	);
	background-size: 22px 22px;
	animation: drift 28s linear infinite;
}

.hero-orb {
	position: absolute;
	border-radius: 9999px;
	filter: blur(48px);
	pointer-events: none;
}

.hero-orb-a {
	top: 10%;
	left: 8%;
	width: 18rem;
	height: 18rem;
	background: rgba(255, 255, 255, 0.22);
	animation: float-a 9s ease-in-out infinite;
}

.hero-orb-b {
	right: 5%;
	bottom: 20%;
	width: 22rem;
	height: 22rem;
	background: rgba(147, 197, 253, 0.35);
	animation: float-b 11s ease-in-out infinite;
}

.hero-img {
	position: absolute;
	inset: 0;
	width: 100%;
	height: 100%;
	object-fit: cover;
	opacity: 0.35;
	mix-blend-mode: luminosity;
}

.hero-content {
	position: relative;
	z-index: 1;
	width: min(720px, calc(100% - 32px));
	margin: 0 auto 0 0;
	padding: 72px 0 80px;
	padding-left: max(16px, calc((100% - 1180px) / 2 + 16px));
	color: #fff;
}

.hero-brand-logo {
	display: block;
	width: min(220px, 55vw);
	height: auto;
	margin: 0 0 18px;
	filter: brightness(0) invert(1);
}

.hero-content h1 {
	margin: 0;
	max-width: 16ch;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: clamp(1.7rem, 4vw, 2.75rem);
	font-weight: 700;
	letter-spacing: -0.04em;
	line-height: 1.2;
}

.hero-text {
	max-width: 38rem;
	margin: 18px 0 0;
	font-size: 1.1rem;
	line-height: 1.65;
	color: rgba(255, 255, 255, 0.86);
}

.hero-actions {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
	margin-top: 28px;
}

.btn-primary,
.btn-ghost {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 160px;
	padding: 12px 20px;
	border-radius: 9999px;
	font-weight: 700;
	text-decoration: none;
	border: none;
	cursor: pointer;
	transition:
		transform 0.2s ease,
		background 0.2s ease,
		box-shadow 0.2s ease;
}

.btn-primary {
	background: #fff;
	color: #1d4ed8;
	box-shadow: 0 12px 28px rgba(15, 23, 42, 0.22);
}

.btn-primary:hover {
	transform: translateY(-1px);
}

.btn-primary--light {
	background: #fff;
	color: #1d4ed8;
}

.btn-ghost {
	background: rgba(255, 255, 255, 0.12);
	color: #fff;
	border: 1px solid rgba(255, 255, 255, 0.28);
	backdrop-filter: blur(6px);
}

.btn-ghost:hover {
	background: rgba(255, 255, 255, 0.2);
}

.features {
	width: min(1180px, calc(100% - 32px));
	margin: 0 auto;
	padding: 72px 0 40px;
}

.section-heading {
	max-width: 640px;
	margin: 0 auto 36px;
	text-align: center;
}

.section-tag {
	display: inline-flex;
	padding: 6px 12px;
	border-radius: 9999px;
	border: 1px solid rgba(37, 99, 235, 0.18);
	background: #eff6ff;
	color: #1d4ed8;
	font-size: 0.8rem;
	font-weight: 700;
}

.section-heading h2,
.cta h2 {
	margin: 14px 0 0;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: clamp(1.8rem, 3.5vw, 2.8rem);
	font-weight: 700;
	letter-spacing: -0.04em;
	line-height: 1.2;
}

.section-heading p,
.cta p {
	margin: 14px auto 0;
	color: #64748b;
	font-size: 1.05rem;
	line-height: 1.65;
}

.feature-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 18px;
}

.feature-item {
	padding: 22px 20px;
	border-radius: 1.35rem;
	border: 1px solid rgba(15, 23, 42, 0.06);
	background: rgba(255, 255, 255, 0.8);
	box-shadow: 0 8px 24px rgba(37, 99, 235, 0.05);
	transition:
		transform 0.2s ease,
		box-shadow 0.2s ease;
}

.feature-item:hover {
	transform: translateY(-2px);
	box-shadow: 0 14px 32px rgba(37, 99, 235, 0.1);
}

.feature-icon {
	display: grid;
	place-items: center;
	width: 48px;
	height: 48px;
	margin-bottom: 16px;
	border-radius: 14px;
	color: #2563eb;
	background: linear-gradient(
		135deg,
		rgba(37, 99, 235, 0.14),
		rgba(147, 197, 253, 0.18)
	);
	border: 1px solid rgba(37, 99, 235, 0.1);
}

.feature-item h3 {
	margin: 0;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: 1.1rem;
	letter-spacing: -0.02em;
}

.feature-item p {
	margin: 8px 0 0;
	color: #64748b;
	line-height: 1.55;
	font-size: 0.95rem;
}

.cta {
	width: min(1180px, calc(100% - 32px));
	margin: 24px auto 70px;
}

.cta-inner {
	padding: 56px 24px;
	border-radius: 2rem;
	text-align: center;
	color: #fff;
	background: linear-gradient(135deg, #1d4ed8, #60a5fa);
	box-shadow: 0 22px 55px rgba(37, 99, 235, 0.22);
}

.cta p {
	max-width: 560px;
	color: rgba(255, 255, 255, 0.84);
}

.cta .btn-primary {
	margin-top: 26px;
}

.animate-in {
	opacity: 0;
	transform: translateY(14px);
	animation: rise 0.7s ease forwards;
}

.delay-1 {
	animation-delay: 0.1s;
}

.delay-2 {
	animation-delay: 0.2s;
}

.delay-3 {
	animation-delay: 0.32s;
}

@keyframes rise {
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes float-a {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(20px);
	}
}

@keyframes float-b {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-18px);
	}
}

@keyframes drift {
	from {
		transform: translateY(0);
	}
	to {
		transform: translateY(22px);
	}
}

@media (max-width: 1100px) {
	.feature-grid {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 640px) {
	.hero-content {
		padding: 56px 16px 64px;
	}

	.feature-grid {
		grid-template-columns: 1fr;
	}

	.topbar-nav {
		gap: 10px;
	}

	.topbar-link {
		font-size: 0.9rem;
	}
}
</style>

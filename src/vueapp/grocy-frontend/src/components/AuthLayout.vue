<template>
	<div class="auth-shell">
		<div class="auth-orb auth-orb-a" aria-hidden="true" />
		<div class="auth-orb auth-orb-b" aria-hidden="true" />

		<div class="auth-frame">
			<RouterLink to="/" class="auth-brand" aria-label="Grocy Startseite">
				<img class="auth-logo" src="/grocy.png" alt="Grocy" />
			</RouterLink>

			<div class="auth-panel">
				<header v-if="title || subtitle" class="auth-header">
					<h1 v-if="title" class="auth-title">{{ title }}</h1>
					<p v-if="subtitle" class="auth-subtitle">{{ subtitle }}</p>
				</header>

				<slot />
			</div>

			<p v-if="footerHint" class="auth-hint">{{ footerHint }}</p>
		</div>
	</div>
</template>

<script setup>
defineProps({
	title: { type: String, default: "" },
	subtitle: { type: String, default: "" },
	footerHint: { type: String, default: "" }
});
</script>

<style scoped>
.auth-shell {
	position: relative;
	display: flex;
	min-height: 100vh;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	padding: 24px 16px;
	background:
		radial-gradient(
			ellipse 70% 50% at 10% 0%,
			rgba(37, 99, 235, 0.14),
			transparent 55%
		),
		radial-gradient(
			ellipse 50% 40% at 100% 20%,
			rgba(96, 165, 250, 0.16),
			transparent 50%
		),
		linear-gradient(180deg, #f8fbff 0%, #eef4ff 50%, #f4f7fc 100%);
}

.auth-orb {
	position: absolute;
	border-radius: 9999px;
	filter: blur(40px);
	pointer-events: none;
}

.auth-orb-a {
	top: 12%;
	left: -4rem;
	width: 16rem;
	height: 16rem;
	background: rgba(59, 130, 246, 0.18);
	animation: float-a 10s ease-in-out infinite;
}

.auth-orb-b {
	right: -3rem;
	bottom: 10%;
	width: 18rem;
	height: 18rem;
	background: rgba(147, 197, 253, 0.22);
	animation: float-b 12s ease-in-out infinite;
}

.auth-frame {
	position: relative;
	z-index: 1;
	width: min(100%, 420px);
}

.auth-brand {
	display: inline-flex;
	align-items: center;
	gap: 10px;
	margin-bottom: 20px;
	text-decoration: none;
	transition: opacity 0.2s ease;
}

.auth-brand:hover {
	opacity: 0.85;
}

.auth-logo {
	display: block;
	height: 40px;
	width: auto;
	max-width: 160px;
	object-fit: contain;
}

.auth-panel {
	padding: 28px 24px;
	border-radius: 1.5rem;
	border: 1px solid rgba(15, 23, 42, 0.08);
	background: #ffffff;
	box-shadow: 0 16px 40px rgba(37, 99, 235, 0.1);
}

.auth-header {
	margin-bottom: 22px;
	text-align: center;
}

.auth-title {
	margin: 0;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: 1.65rem;
	font-weight: 700;
	letter-spacing: -0.02em;
	line-height: 1.3;
	color: #0f172a;
}

.auth-subtitle {
	margin: 8px 0 0;
	font-size: 0.95rem;
	line-height: 1.5;
	color: #64748b;
}

.auth-hint {
	margin: 16px 0 0;
	text-align: center;
	font-size: 0.8rem;
	color: #94a3b8;
}

@keyframes float-a {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(18px);
	}
}

@keyframes float-b {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-16px);
	}
}
</style>

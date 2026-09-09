<template>
	<nav class="legal-footer" aria-label="Rechtliches">
		<RouterLink to="/impressum">Impressum</RouterLink>
		<span aria-hidden="true">·</span>
		<RouterLink to="/datenschutz">Datenschutz</RouterLink>
	</nav>
</template>

<style scoped>
.legal-footer {
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 10px;
	margin-top: 20px;
	font-size: 0.8rem;
}

.legal-footer a {
	color: #64748b;
	text-decoration: none;
}

.legal-footer a:hover,
.legal-footer a.router-link-active {
	color: #1d4ed8;
	text-decoration: underline;
}
</style>

<template>
	<div class="g-page">
		<div class="g-page-inner-narrow">
			<section class="g-panel legal-panel">
				<h1 class="g-page-title">Impressum</h1>

				<p class="g-page-sub">
					Angaben gemäß § 5 DDG und § 18 Abs. 1 MStV
				</p>

				<div class="legal-block">
					<h2>Diensteanbieter</h2>
					<p>
						Stephan Kleinhans<br />
						Lindwurmstraße 75<br />
						80337 München<br />
						Deutschland
					</p>
				</div>

				<div class="legal-block">
					<h2>Kontakt</h2>
					<p>
						E-Mail:
						<a href="mailto:info@sk-devlab.de">
							info@sk-devlab.de
						</a>
					</p>
				</div>
			</section>
		</div>
	</div>
</template>

<script setup></script>

<style scoped>
.legal-panel {
	display: grid;
	gap: 20px;
}

.legal-block h2 {
	margin: 0 0 8px;
	font-size: 1rem;
	font-weight: 700;
}

.legal-block p {
	margin: 0;
	color: #475569;
	line-height: 1.55;
}

.legal-block a {
	color: inherit;
}
</style>

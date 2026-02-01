/** @type {import('tailwindcss').Config} */
export default {
	content: [
		"./index.html",
		"./src/**/*.{vue,js,ts,jsx,tsx}" // Hier sucht Tailwind in all Ihren Vue-Komponenten
	],
	theme: {
		extend: {}
	},
	plugins: []
};

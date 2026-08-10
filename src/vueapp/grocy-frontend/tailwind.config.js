/** @type {import('tailwindcss').Config} */
export default {
	content: [
		"./index.html",
		"./src/**/*.{vue,js,ts,jsx,tsx}"
	],
	theme: {
		extend: {
			colors: {
				ink: "#0f172a",
				muted: "#64748b",
				surface: "#ffffff",
				bg: "#f4f7fc",
				primary: {
					DEFAULT: "#2563eb",
					hover: "#1d4ed8",
					soft: "#eff6ff",
					deep: "#1e40af"
				},
				border: "rgba(15, 23, 42, 0.08)",
				danger: "#dc2626",
				success: "#059669"
			},
			fontFamily: {
				display: ["Figtree", "ui-sans-serif", "system-ui", "sans-serif"],
				sans: ["Figtree", "ui-sans-serif", "system-ui", "sans-serif"]
			},
			boxShadow: {
				panel: "0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 24px rgba(37, 99, 235, 0.06)",
				nav: "0 10px 30px rgba(15, 23, 42, 0.1)",
				lift: "0 16px 40px rgba(37, 99, 235, 0.14)"
			},
			borderRadius: {
				panel: "1.25rem"
			}
		}
	},
	plugins: []
};

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        gray: {
          850: '#1f2937',
          950: '#030712',
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}

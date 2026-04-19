import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import 'highlight.js/styles/github-dark.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// THME-01: Ensure dark class is on html element
document.documentElement.classList.add('dark')

app.mount('#app')

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { registerAuthStore, registerRouter } from './services/api/client'
import './assets/styles/tailwind.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Register auth store and router accessors for the API client interceptors
import { useAuthStore } from './stores/auth'
registerAuthStore(() => useAuthStore())
registerRouter(() => router)

app.mount('#app')

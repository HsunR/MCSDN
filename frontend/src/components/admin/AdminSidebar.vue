<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <aside class="w-64 min-h-screen bg-gray-950 border-r border-gray-800 flex flex-col">
    <div class="p-6">
      <h1 class="text-xl font-bold text-gray-100">Blog Admin</h1>
    </div>

    <nav class="flex-1 px-4">
      <router-link
        to="/admin"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors"
        :class="route.path === '/admin' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        Dashboard
      </router-link>

      <router-link
        to="/admin/articles"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors"
        :class="route.path.startsWith('/admin/articles') ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        Articles
      </router-link>
    </nav>

    <div class="p-4 border-t border-gray-800">
      <button
        @click="handleLogout"
        class="w-full px-4 py-2 text-gray-400 hover:text-gray-100 hover:bg-gray-800 rounded-lg transition-colors text-left"
      >
        Logout
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { useCommentStore } from '../../stores/commentStore'
import { ref, onMounted } from 'vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const commentStore = useCommentStore()
const pendingCount = ref(0)

onMounted(() => {
  commentStore.fetchPendingCount()
  pendingCount.value = commentStore.pendingCount
})

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

      <router-link
        to="/admin/comments"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors relative"
        :class="route.path.startsWith('/admin/comments') ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        Comments
        <span
          v-if="commentStore.pendingCount > 0"
          class="absolute -top-1 -right-1 bg-red-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center"
        >
          {{ commentStore.pendingCount > 9 ? '9+' : commentStore.pendingCount }}
        </span>
      </router-link>

      <router-link
        to="/admin/categories"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors"
        :class="route.path.startsWith('/admin/categories') ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        Categories
      </router-link>

      <router-link
        to="/admin/tags"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors"
        :class="route.path.startsWith('/admin/tags') ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        Tags
      </router-link>

      <router-link
        to="/admin/csdn-sync"
        class="block px-4 py-2 mb-1 rounded-lg transition-colors"
        :class="route.path.startsWith('/admin/csdn-sync') ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'"
      >
        CSDN 同步
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

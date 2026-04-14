<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView } from 'vue-router'
import PublicHeader from './components/public/PublicHeader.vue'

const route = useRoute()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
</script>

<template>
  <div class="min-h-screen bg-gray-950">
    <PublicHeader v-if="!isAdminRoute" />
    <main>
      <RouterView />
    </main>
  </div>
</template>

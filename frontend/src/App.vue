<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView } from 'vue-router'
import Sidebar from './components/public/Sidebar.vue'
import EmailSidebar from './components/public/EmailSidebar.vue'

const route = useRoute()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const isArticleRoute = computed(() => route.path.startsWith('/article/'))
</script>

<template>
  <div class="app-container" :class="{ 'article-layout': isArticleRoute }">
    <template v-if="!isAdminRoute">
      <Sidebar v-if="!isArticleRoute" />
      <EmailSidebar v-if="!isArticleRoute" />
      <main class="main-content">
        <RouterView />
      </main>
    </template>
    <template v-else>
      <RouterView />
    </template>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: var(--navy);
}

.main-content {
  padding-left: 100px;
  padding-right: 40px;
  padding-top: 0;
  padding-bottom: 0;
  width: 100%;
  max-width: 100%;
}

.app-container.article-layout .main-content {
  padding-left: 50px;
}

@media (max-width: 768px) {
  .main-content {
    padding-left: 20px;
    padding-right: 20px;
  }
  
  .app-container.article-layout .main-content {
    padding-left: 20px;
  }
}
</style>

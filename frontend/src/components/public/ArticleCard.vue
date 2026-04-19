<script setup>
const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'short', day: 'numeric'
  })
}
</script>

<template>
  <article class="article-card border-l-2 border-blue-600 pl-4 py-2 hover:border-blue-400 transition-colors">
    <!-- D-02: Title + excerpt (2 lines) -->
    <h3 class="text-xl font-semibold text-gray-100 flex items-center gap-2">
      <!-- Preview button -->
      <button
        type="button"
        @click.stop.prevent="$emit('preview', article.id)"
        @mousedown.prevent
        class="p-1.5 rounded hover:bg-gray-700 text-gray-400 hover:text-blue-400 transition-colors flex-shrink-0"
        title="快速预览"
      >
        <!-- Eye icon (heroicons outline style) -->
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
        </svg>
      </button>
      <router-link
        :to="`/article/${article.id}-${article.slug}`"
        class="hover:text-blue-400 transition-colors"
      >
        {{ article.title }}
      </router-link>
    </h3>
    <p class="text-gray-400 mt-2 line-clamp-2">{{ article.excerpt }}</p>

    <!-- D-02: Tags + date + category badge -->
    <div class="flex flex-wrap items-center gap-2 mt-3">
      <!-- Category badge -->
      <router-link
        v-if="article.category"
        :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300 hover:bg-blue-800"
      >
        {{ article.category.name }}
      </router-link>

      <!-- Tags -->
      <router-link
        v-for="tag in article.tags"
        :key="tag.id"
        :to="`/tag/${tag.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300 hover:bg-gray-600"
      >
        {{ tag.name }}
      </router-link>

      <!-- Date -->
      <time class="text-sm text-gray-500 ml-auto">{{ formatDate(article.createdAt) }}</time>
    </div>
  </article>
</template>

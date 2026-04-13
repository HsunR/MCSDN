<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true }
})

const emit = defineEmits(['page-change'])

function goTo(page) {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('page-change', page)
  }
}

const pages = computed(() => {
  const result = []
  for (let i = 1; i <= props.totalPages; i++) {
    result.push(i)
  }
  return result
})
</script>

<template>
  <nav v-if="totalPages > 1" class="flex justify-center gap-1 mt-8">
    <!-- Previous -->
    <button
      @click="goTo(currentPage - 1)"
      :disabled="currentPage === 1"
      class="px-3 py-1 rounded bg-gray-800 text-gray-300 hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
    >
      &laquo;
    </button>

    <!-- Page numbers (D-09: numbered pages 1, 2, 3...) -->
    <button
      v-for="page in pages"
      :key="page"
      @click="goTo(page)"
      :class="[
        'px-3 py-1 rounded',
        page === currentPage
          ? 'bg-blue-600 text-white'
          : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
      ]"
    >
      {{ page }}
    </button>

    <!-- Next -->
    <button
      @click="goTo(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="px-3 py-1 rounded bg-gray-800 text-gray-300 hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
    >
      &raquo;
    </button>
  </nav>
</template>

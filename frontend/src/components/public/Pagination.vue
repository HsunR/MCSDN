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
  <nav v-if="totalPages > 1" class="pagination-nav">
    <button
      @click="goTo(currentPage - 1)"
      :disabled="currentPage === 1"
      class="pagination-button prev-button"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="m15 18-6-6 6-6"/>
      </svg>
      <span>Prev</span>
    </button>

    <div class="page-numbers">
      <button
        v-for="page in pages"
        :key="page"
        @click="goTo(page)"
        :class="[
          'page-button',
          { active: page === currentPage }
        ]"
      >
        {{ String(page).padStart(2, '0') }}
      </button>
    </div>

    <button
      @click="goTo(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="pagination-button next-button"
    >
      <span>Next</span>
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="m9 18 6-6-6-6"/>
      </svg>
    </button>
  </nav>
</template>

<style scoped>
.pagination-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 50px;
  padding: 30px 0;
}

.pagination-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background-color: transparent;
  border: 1px solid var(--green);
  border-radius: 4px;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled) {
  background-color: rgba(100, 255, 218, 0.1);
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 10px;
}

.page-button {
  padding: 8px 16px;
  background-color: transparent;
  border: 1px solid transparent;
  border-radius: 4px;
  color: var(--slate);
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-button:hover:not(.active) {
  color: var(--green);
  background-color: var(--navy-light);
}

.page-button.active {
  color: var(--green);
  background-color: var(--navy-light);
  border-color: var(--green);
}

@media (max-width: 768px) {
  .pagination-nav {
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .page-numbers {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>

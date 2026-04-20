<template>
  <div class="search-container">
    <div class="search-icon-wrapper">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="search-icon">
        <circle cx="11" cy="11" r="8"/>
        <path d="m21 21-4.3-4.3"/>
      </svg>
    </div>
    <form @submit.prevent="handleSearch" class="search-form">
      <input
        v-model="keyword"
        type="text"
        placeholder="Search articles..."
        class="search-input"
      />
      <button
        type="submit"
        class="search-button"
      >
        Search
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')

function handleSearch() {
  if (keyword.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(keyword.value.trim())}`)
    keyword.value = ''
  }
}
</script>

<style scoped>
.search-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.search-icon-wrapper {
  color: var(--green);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.search-icon {
  stroke: currentColor;
}

.search-form {
  flex: 1;
  display: flex;
  gap: 15px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  color: var(--lightest-slate);
  font-family: var(--font-sans);
  font-size: var(--fz-md);
  transition: all 0.3s ease;
}

.search-input::placeholder {
  color: var(--slate);
}

.search-input:focus {
  outline: none;
  border-color: var(--green);
  background-color: var(--lightest-navy);
}

.search-button {
  padding: 12px 24px;
  background-color: transparent;
  border: 1px solid var(--green);
  border-radius: 4px;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-button:hover {
  background-color: rgba(100, 255, 218, 0.1);
}
</style>

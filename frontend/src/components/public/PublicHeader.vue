<template>
  <header class="bg-gray-900 border-b border-gray-800">
    <div class="max-w-6xl mx-auto px-4 py-4">
      <div class="flex items-center justify-between">
        <router-link to="/" class="text-2xl font-bold text-gray-100 hover:text-blue-400">
          My Tech Blog
        </router-link>

        <nav class="flex items-center gap-6">
          <router-link to="/" class="text-gray-300 hover:text-blue-400">Home</router-link>
          <router-link to="/search" class="text-gray-300 hover:text-blue-400">Search</router-link>
        </nav>
      </div>
    </div>
  </header>
</template>

<script setup>
</script>

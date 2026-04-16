<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { useArticleStore } from '../../stores/articleStore'
import { getSyncConfig, saveSyncConfig, triggerSync } from '../../api/csdnSyncApi'
import axios from 'axios'

const articleStore = useArticleStore()
const http = axios.create({ baseURL: '/api' })

// JWT interceptor setup (same pattern as CategoryManagementView)
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const config = ref({ csdnUserId: '', categoryId: null, enabled: true })
const categories = ref([])
const syncing = ref(false)
const syncResult = ref(null)
const syncError = ref('')
const saveLoading = ref(false)

async function fetchConfig() {
  try {
    const res = await getSyncConfig()
    if (res.data) {
      config.value.csdnUserId = res.data.csdnUserId || ''
      config.value.categoryId = res.data.categoryId || null
      config.value.enabled = res.data.enabled !== false
    }
  } catch (e) {
    // Config not found — use defaults
  }
}

async function fetchCategories() {
  const res = await http.get('/admin/categories')
  categories.value = res.data
}

async function saveConfig() {
  saveLoading.value = true
  try {
    await saveSyncConfig({
      csdnUserId: config.value.csdnUserId,
      categoryId: config.value.categoryId,
      enabled: config.value.enabled
    })
  } finally {
    saveLoading.value = false
  }
}

async function handleSync() {
  syncing.value = true
  syncResult.value = null
  syncError.value = ''
  try {
    const res = await triggerSync()
    syncResult.value = res.data
    // D-05: Auto-refresh article list after sync
    articleStore.fetchArticles()
  } catch (e) {
    const errors = e.response?.data?.errors || []
    syncError.value = errors.length > 0 ? errors[0] : '同步失败'
  } finally {
    syncing.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchConfig(), fetchCategories()])
})
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">CSDN 同步</h1>

      <!-- Sync Config Card -->
      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-6">
        <h2 class="text-lg font-semibold text-gray-100 mb-4">同步配置</h2>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">CSDN User ID</label>
            <input
              v-model="config.csdnUserId"
              type="text"
              placeholder="2301_78723800"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">目标分类</label>
            <select
              v-model="config.categoryId"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100"
            >
              <option :value="null" disabled>Select category</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
        </div>
        <button
          @click="saveConfig"
          :disabled="saveLoading"
          class="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
        >
          {{ saveLoading ? '保存中...' : '保存配置' }}
        </button>
      </div>

      <!-- Sync Trigger Card -->
      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
        <button
          @click="handleSync"
          :disabled="syncing || !config.csdnUserId"
          class="px-6 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-semibold disabled:opacity-50"
        >
          {{ syncing ? '同步中...' : '同步' }}
        </button>

        <!-- Sync Results Panel (D-01 inline panel) -->
        <div v-if="syncResult" class="mt-4 p-4 bg-gray-700 rounded-lg border border-gray-600">
          <p class="text-gray-300">
            成功: <span class="text-green-400 font-bold">{{ syncResult.created }}</span> |
            更新: <span class="text-blue-400 font-bold">{{ syncResult.updated }}</span> |
            跳过: <span class="text-yellow-400 font-bold">{{ syncResult.skipped }}</span> |
            失败: <span class="text-red-400 font-bold">{{ syncResult.errors?.length || 0 }}</span>
          </p>
          <div v-if="syncResult.errors?.length" class="mt-2 text-red-400 text-sm">
            <p v-for="(err, idx) in syncResult.errors" :key="idx">{{ err }}</p>
          </div>
        </div>

        <!-- Sync Error Banner -->
        <div v-if="syncError" class="mt-4 p-4 bg-red-900 bg-opacity-30 border border-red-700 rounded text-red-400">
          {{ syncError }}
        </div>
      </div>
    </main>
  </div>
</template>

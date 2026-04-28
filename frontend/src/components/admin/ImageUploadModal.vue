<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../../stores/authStore'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'insert'])

const authStore = useAuthStore()

const fileInput = ref(null)
const uploading = ref(false)
const error = ref('')
const dragOver = ref(false)

function triggerFileInput() {
  fileInput.value.click()
}

async function handleFileSelect(event) {
  const file = event.target.files[0]
  if (file) await uploadFile(file)
}

async function handleDrop(event) {
  event.preventDefault()
  dragOver.value = false
  const file = event.dataTransfer.files[0]
  if (file) await uploadFile(file)
}

async function uploadFile(file) {
  // IMGE-06: Validate file type
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    error.value = 'Invalid file type. Allowed: jpg, png, gif, webp'
    return
  }

  // IMGE-05: Size validation (5MB)
  if (file.size > 5 * 1024 * 1024) {
    error.value = 'File too large. Maximum size is 5MB.'
    return
  }

  uploading.value = true
  error.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/api/admin/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${authStore.token}`
      }
    })

    // D-12: Image URL auto-inserted as ![alt](url) at cursor
    const url = response.data.url
    emit('insert', `![${file.name}](${url})`)
    emit('close')
  } catch (e) {
    error.value = e.response?.data?.error || 'Upload failed'
  } finally {
    uploading.value = false
  }
}

function handleClose() {
  error.value = ''
  emit('close')
}
</script>

<template>
  <div v-if="visible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700">
      <h3 class="text-lg font-semibold text-gray-100 mb-4">Upload Image</h3>

      <!-- D-13: Drag-drop as bonus UX -->
      <div
        @dragover.prevent="dragOver = true"
        @dragleave="dragOver = false"
        @drop="handleDrop"
        @click="triggerFileInput"
        :class="[
          'border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors',
          dragOver ? 'border-blue-500 bg-blue-900 bg-opacity-30' : 'border-gray-600 hover:border-gray-500'
        ]"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          class="hidden"
          @change="handleFileSelect"
        />
        <p class="text-gray-400">
          {{ uploading ? 'Uploading...' : 'Drag and drop or click to select' }}
        </p>
        <p class="text-gray-500 text-sm mt-2">jpg, png, gif, webp - max 5MB</p>
      </div>

      <p v-if="error" class="text-red-500 text-sm mt-3">{{ error }}</p>

      <div class="mt-4 flex justify-end">
        <button
          @click="handleClose"
          class="px-4 py-2 bg-gray-700 text-gray-300 rounded hover:bg-gray-600"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

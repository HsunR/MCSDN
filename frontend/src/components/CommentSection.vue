<script setup>
import { ref, onMounted } from 'vue'
import { commentApi } from '../api/commentApi'

const props = defineProps({
  articleId: {
    type: String,
    required: true
  }
})

const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = ref({
  name: '',
  content: '',
  honeypot: ''
})
const message = ref({ type: '', text: '' })

async function loadComments() {
  loading.value = true
  try {
    comments.value = await commentApi.getApproved(props.articleId)
  } catch (e) {
    // Silently fail - public view should not show errors
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  // Honeypot check - if filled, silently accept but don't process
  if (form.value.honeypot) {
    message.value = { type: 'success', text: 'Comment submitted, awaiting approval.' }
    return
  }

  // Validate
  if (!form.value.name.trim() || !form.value.content.trim()) {
    message.value = { type: 'error', text: 'Please fill in your name and comment.' }
    return
  }

  submitting.value = true
  message.value = { type: '', text: '' }

  try {
    await commentApi.submit(props.articleId, {
      authorName: form.value.name.trim(),
      content: form.value.content.trim(),
      honeypot: form.value.honeypot
    })
    // D-03: Success message
    message.value = { type: 'success', text: 'Comment submitted, awaiting approval.' }
    form.value.name = ''
    form.value.content = ''
    form.value.honeypot = ''
  } catch (e) {
    // D-03: Show success even on error to avoid bot feedback
    message.value = { type: 'success', text: 'Comment submitted, awaiting approval.' }
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  loadComments()
})
</script>

<template>
  <section class="mt-12 border-t border-gray-700 pt-8">
    <h3 class="text-xl font-bold text-gray-100 mb-6">Comments</h3>

    <!-- Comment Form -->
    <form @submit.prevent="submitComment" class="mb-8 space-y-4">
      <!-- Honeypot field - D-12: hidden from real users -->
      <input
        v-model="form.honeypot"
        name="website"
        type="text"
        class="absolute opacity-0 pointer-events-none"
        tabindex="-1"
        autocomplete="off"
      />

      <!-- Name input -->
      <div>
        <input
          v-model="form.name"
          type="text"
          placeholder="Your name"
          maxlength="100"
          class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500"
          :disabled="submitting"
        />
      </div>

      <!-- Content textarea -->
      <div>
        <textarea
          v-model="form.content"
          placeholder="Your comment"
          rows="4"
          maxlength="2000"
          class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500 resize-none"
          :disabled="submitting"
        ></textarea>
      </div>

      <!-- Message -->
      <div v-if="message.text" class="text-sm" :class="message.type === 'success' ? 'text-green-400' : 'text-red-400'">
        {{ message.text }}
      </div>

      <!-- Submit button -->
      <button
        type="submit"
        :disabled="submitting"
        class="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800 disabled:cursor-not-allowed text-white rounded font-medium transition-colors"
      >
        {{ submitting ? 'Submitting...' : 'Submit Comment' }}
      </button>
    </form>

    <!-- Comment List -->
    <div v-if="loading" class="text-gray-400 py-4">
      Loading comments...
    </div>
    <div v-else-if="comments.length === 0" class="text-gray-400 py-4">
      No comments yet. Be the first to comment!
    </div>
    <div v-else class="space-y-4">
      <!-- D-04: Flat list, no nesting; D-06: oldest first (ASC order from backend) -->
      <article
        v-for="comment in comments"
        :key="comment.id"
        class="p-4 bg-gray-800 border border-gray-700 rounded"
      >
        <!-- D-05: Comment card shows name (bold), formatted date, content -->
        <div class="flex items-baseline gap-3 mb-2">
          <span class="font-semibold text-gray-100">{{ comment.authorName }}</span>
          <time class="text-sm text-gray-400">{{ formatDate(comment.createdAt) }}</time>
        </div>
        <p class="text-gray-300 whitespace-pre-wrap">{{ comment.content }}</p>
      </article>
    </div>
  </section>
</template>

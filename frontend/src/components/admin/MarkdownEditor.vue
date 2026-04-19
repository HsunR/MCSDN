<script setup>
import { ref, computed, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import ImageUploadModal from './ImageUploadModal.vue'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch (__) {}
    }
    try {
      const result = hljs.highlightAuto(str)
      return '<pre class="hljs"><code>' + result.value + '</code></pre>'
    } catch (__) {}
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const props = defineProps({
  modelValue: String,
  placeholder: { type: String, default: 'Write your article in Markdown...' }
})

const emit = defineEmits(['update:modelValue'])

const showUploadModal = ref(false)

const content = computed({
  get: () => props.modelValue || '',
  set: (value) => emit('update:modelValue', value)
})

const renderedHtml = computed(() => {
  if (!content.value) return '<p class="text-gray-500">Preview will appear here...</p>'
  return md.render(content.value)
})
</script>

<template>
  <div>
    <!-- D-11: Toolbar with image upload button -->
    <div class="mb-4 flex items-center gap-2 border-b border-gray-700 pb-2">
      <span class="text-sm text-gray-400">Toolbar:</span>
      <button
        @click="showUploadModal = true"
        class="px-2 py-1 text-xs bg-gray-700 text-gray-300 rounded hover:bg-gray-600"
        title="Upload Image"
      >
        Upload Image
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 h-full">
    <!-- Editor pane -->
    <div class="flex flex-col">
      <label class="text-sm font-medium text-gray-300 mb-2">Markdown</label>
      <textarea
        v-model="content"
        :placeholder="placeholder"
        class="flex-1 w-full p-4 bg-gray-800 border border-gray-700 rounded-lg text-gray-100 font-mono text-sm resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        style="min-height: 400px;"
      ></textarea>
    </div>

    <!-- Preview pane -->
    <div class="flex flex-col">
      <label class="text-sm font-medium text-gray-300 mb-2">Preview</label>
      <div
        class="flex-1 p-4 bg-gray-800 border border-gray-700 rounded-lg overflow-auto"
        style="min-height: 400px;"
      >
        <!-- THME-02: Dark code highlighting styles -->
        <div
          class="prose prose-invert max-w-none"
          v-html="renderedHtml"
        ></div>
      </div>
    </div>
    </div>

    <!-- D-11, D-12: Image upload modal -->
    <ImageUploadModal
      :visible="showUploadModal"
      @close="showUploadModal = false"
      @insert="(markdown) => { content += '\n' + markdown }"
    />
  </div>
</template>

<style>
/* THME-02: Dark code highlighting theme (dracula-like) */
.hljs {
  background: #1e1e1e !important;
  color: #d4d4d4 !important;
  padding: 1em;
  border-radius: 0.5rem;
  overflow-x: auto;
}
.hljs-keyword { color: #569cd6; }
.hljs-string { color: #ce9178; }
.hljs-number { color: #b5cea8; }
.hljs-comment { color: #6a9955; }
.hljs-function { color: #dcdcaa; }
.hljs-class { color: #4ec9b0; }
.hljs-variable { color: #9cdcfe; }
.hljs-operator { color: #d4d4d4; }
.hljs-punctuation { color: #d4d4d4; }
.hljs-property { color: #9cdcfe; }
.hljs-attr { color: #9cdcfe; }
.hljs-tag { color: #569cd6; }
.hljs-attribute { color: #9cdcfe; }
.hljs-selector-class { color: #d7ba7d; }
.hljs-selector-id { color: #d7ba7d; }
.hljs-title { color: #dcdcaa; }
.hljs-built_in { color: #4ec9b0; }

/* Image styling in preview */
.prose img {
  max-width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin: 1rem 0;
}
</style>

# UI Design Contract: Phase 6 - Article Quick Preview

**Created:** 2026-04-19
**Phase:** 06-article-quick-preview
**Status:** Contract for implementation

---

## 1. Component Structure

### 1.1 ArticleCard Modifications

**File:** `frontend/src/components/public/ArticleCard.vue`

Add a preview button to the meta line (between tags and date).

```vue
<!-- Add to meta line, before <time> -->
<button
  @click="$emit('preview', article.id)"
  class="p-1.5 rounded hover:bg-gray-700 text-gray-400 hover:text-blue-400 transition-colors"
  title="Quick preview"
>
  <!-- Eye icon (heroicons style) -->
  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
  </svg>
</button>
```

**Props added:**
- No new props required (article already contains full data)

**Emits:**
- `preview` — emits `article.id` when button clicked

### 1.2 New ArticlePreviewModal Component

**File:** `frontend/src/components/public/ArticlePreviewModal.vue`

```
ArticlePreviewModal
├── Overlay (backdrop)
├── Modal Container
│   ├── Header
│   │   ├── Title + metadata
│   │   └── Close button (X icon)
│   └── Content (scrollable)
│       └── Markdown rendered content
```

---

## 2. Visual Design

### 2.1 Color Palette (Dark Theme)

| Token | Hex | Usage |
|-------|-----|-------|
| Background Primary | `#111827` (gray-900) | Modal background |
| Background Secondary | `#1f2937` (gray-800) | Modal header |
| Border | `#374151` (gray-700) | Modal borders |
| Text Primary | `#f3f4f6` (gray-100) | Titles, headings |
| Text Secondary | `#9ca3af` (gray-400) | Body text, icons |
| Text Muted | `#6b7280` (gray-500) | Dates, subtle text |
| Accent Blue | `#3b82f6` (blue-500) | Hover states |
| Accent Blue Dark | `#1d4ed8` (blue-700) | Category badges |
| Accent Blue Light | `#60a5fa` (blue-400) | Hover on badges |

### 2.2 Typography

| Element | Class | Font |
|---------|-------|------|
| Modal Title | `text-2xl font-bold text-gray-100` | System |
| Category Badge | `px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300` | System |
| Tag Badge | `px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300` | System |
| Date | `text-sm text-gray-500` | System |
| Article Body | `prose prose-invert max-w-none` | prose-invert |

### 2.3 Spacing System

| Element | Spacing |
|---------|---------|
| Modal padding | `p-6` (24px) |
| Gap between meta items | `gap-2` (8px) |
| Modal max-width | `max-w-4xl` (896px) |
| Modal max-height | `max-h-[90vh]` |
| Overlay padding | `p-4` |

### 2.4 Component Dimensions

```
Overlay:      fixed inset-0 (full viewport)
Modal:        max-w-4xl (896px), max-h-[90vh]
Content area: overflow-y-auto, p-6
```

---

## 3. Component Specifications

### 3.1 ArticlePreviewModal Props & Events

```typescript
Props:
  visible: Boolean          // Controls modal visibility
  articleId: Number/String  // ID of article to preview

Events:
  close: () => void         // Emitted when user closes modal
```

### 3.2 Modal Layout Structure

```html
<template>
  <!-- Overlay -->
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
    @click.self="handleClose"
  >
    <!-- Modal Container -->
    <div class="bg-gray-900 rounded-lg w-full max-w-4xl max-h-[90vh] overflow-hidden border border-gray-700 shadow-2xl flex flex-col">
      <!-- Header -->
      <div class="flex-shrink-0 border-b border-gray-700 bg-gray-800">
        <!-- Title + Meta Row -->
        <div class="p-6 pb-4">
          <h2 class="text-2xl font-bold text-gray-100 mb-3">{{ article?.title }}</h2>
          <!-- Meta line -->
          <div class="flex flex-wrap items-center gap-2">
            <router-link v-if="article?.category" ...>{{ article.category.name }}</router-link>
            <router-link v-for="tag in article?.tags" ...>{{ tag.name }}</router-link>
            <time class="ml-auto text-sm text-gray-500">{{ formatDate(article?.createdAt) }}</time>
          </div>
        </div>
        <!-- Close Button -->
        <button @click="handleClose" class="absolute top-4 right-4 ...">X</button>
      </div>

      <!-- Scrollable Content -->
      <div class="flex-1 overflow-y-auto">
        <div class="prose prose-invert max-w-none p-6" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>
```

### 3.3 Overlay Styling

```css
/* From ImageUploadModal.vue pattern */
fixed inset-0
bg-black bg-opacity-60       /* or bg-black/60 */
backdrop-blur-sm
z-50
flex items-center justify-center
p-4                           /* padding around modal */
```

### 3.4 Modal Container Styling

```css
/* Container */
bg-gray-900
rounded-lg
w-full
max-w-4xl                     /* 896px max width */
max-h-[90vh]                  /* 90% viewport height */
overflow-hidden
border border-gray-700
shadow-2xl
flex flex-col                 /* header + scrollable body */
```

### 3.5 Close Button Styling

```css
/* From ImageUploadModal.vue cancel button + custom positioning */
absolute top-4 right-4
p-2 rounded-full
hover:bg-gray-700
text-gray-400 hover:text-white
transition-colors
```

### 3.6 Eye Icon Button (ArticleCard)

```css
/* Add to ArticleCard meta line */
p-1.5 rounded
hover:bg-gray-700
text-gray-400 hover:text-blue-400
transition-colors
```

---

## 4. Interaction Flows

### 4.1 Open Preview Flow

```
User clicks eye icon on ArticleCard
    │
    ▼
ArticleCard emits: preview(article.id)
    │
    ▼
HomeView receives event, sets articleId state
    │
    ▼
ArticlePreviewModal opens (visible = true)
    │
    ▼
Modal calls store.fetchArticle(articleId)
    │
    ▼
Markdown content rendered with highlight.js
```

### 4.2 Close Preview Flow

```
User clicks:
  - Overlay background (outside modal), OR
  - X close button (top-right), OR
  - Presses Escape key
    │
    ▼
Modal emits: close event
    │
    ▼
HomeView sets articleId to null
    │
    ▼
Modal closes (visible = false)
    │
    ▼
User remains on article list at same page
```

### 4.3 Escape Key Handling

```javascript
// In ArticlePreviewModal setup
import { onMounted, onUnmounted } from 'vue'

function handleEscape(e) {
  if (e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

onMounted(() => document.addEventListener('keydown', handleEscape))
onUnmounted(() => document.removeEventListener('keydown', handleEscape))
```

---

## 5. Integration with HomeView

### 5.1 HomeView Changes

```vue
<!-- Add to script setup -->
import ArticlePreviewModal from '../components/public/ArticlePreviewModal.vue'
import { ref } from 'vue'

const previewArticleId = ref(null)

function handlePreview(articleId) {
  previewArticleId.value = articleId
}

function handleClosePreview() {
  previewArticleId.value = null
}
```

```vue
<!-- Add to template, after ArticleTimeline -->
<ArticleTimeline :articles="store.articles" @preview="handlePreview" />

<ArticlePreviewModal
  :visible="previewArticleId !== null"
  :articleId="previewArticleId"
  @close="handleClosePreview"
/>
```

---

## 6. Markdown Rendering

### 6.1 Reuse ArticleView Configuration

The modal MUST use the identical markdown-it + highlight.js configuration from `ArticleView.vue`:

```javascript
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const md = new MarkdownIt({
  html: false,
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
```

### 6.2 Rendered Content Styling

```html
<div
  class="prose prose-invert max-w-none"
  v-html="renderedContent"
></div>
```

---

## 7. State Management

### 7.1 Article Loading in Modal

```javascript
// In ArticlePreviewModal
import { computed, watch } from 'vue'
import { usePublicArticleStore } from '../../stores/publicArticleStore'

const props = defineProps({
  visible: Boolean,
  articleId: [Number, String]
})

const store = usePublicArticleStore()

const article = computed(() => store.currentArticle)
const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content)
})

// Fetch when modal opens with new articleId
watch(() => props.articleId, (newId) => {
  if (newId) {
    store.fetchArticle(newId)
  }
}, { immediate: true })
```

---

## 8. Accessibility

| Element | Requirement |
|---------|-------------|
| Close button | `title="Close preview"` attribute |
| Eye button | `title="Quick preview"` attribute |
| Modal | Should trap focus when open |
| Escape key | Must close modal |
| Overlay click | Must close modal |

---

## 9. CSS Classes Quick Reference

### From Existing Code

**ArticleCard meta line:**
```css
flex flex-wrap items-center gap-2 mt-3
```

**Category badge:**
```css
px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300 hover:bg-blue-800
```

**Tag badge:**
```css
px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300 hover:bg-gray-600
```

**Date:**
```css
text-sm text-gray-500 ml-auto
```

**ArticleView prose:**
```css
prose prose-invert max-w-none
```

**ImageUploadModal overlay:**
```css
fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50
```

**ImageUploadModal container:**
```css
bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700
```

---

## 10. File Checklist

- [ ] `frontend/src/components/public/ArticleCard.vue` — Add preview button
- [ ] `frontend/src/components/public/ArticlePreviewModal.vue` — New component
- [ ] `frontend/src/views/public/HomeView.vue` — Import modal, manage state

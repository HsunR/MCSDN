<template>
  <nav class="toc-sidebar" ref="tocSidebarRef">
    <div class="toc-container">
      <div class="toc-header">
        <span class="toc-label">目录</span>
      </div>
      <ul class="toc-list">
        <li
          v-for="heading in headings"
          :key="heading.id"
          class="toc-item"
          :class="{ active: activeHeading === heading.id, 'toc-h3': heading.level === 3 }"
        >
          <a
            :href="`#${heading.id}`"
            class="toc-link"
            @click="handleTocClick"
          >
            {{ heading.text }}
          </a>
        </li>
      </ul>
      <div v-if="headings.length === 0" class="toc-empty">
        <p class="text-slate text-xs">无目录</p>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const headings = ref([])
const activeHeading = ref('')
const tocSidebarRef = ref(null)

function parseHeadings() {
  headings.value = []
  
  nextTick(() => {
    const articleContent = document.querySelector('.article-content')
    if (!articleContent) return

    const headingElements = articleContent.querySelectorAll('h2, h3')
    const headingList = []

    headingElements.forEach((el, index) => {
      const level = el.tagName === 'H2' ? 2 : 3
      const text = el.textContent.trim()
      const id = `heading-${index}`
      
      if (!el.id) {
        el.id = id
        el.style.scrollMarginTop = '20px'
      }
      
      headingList.push({ id: el.id, text, level })
    })

    headings.value = headingList
    
    setTimeout(() => {
      handleScroll()
    }, 100)
  })
}

function handleTocClick(e) {
  e.preventDefault()
  const href = e.currentTarget.getAttribute('href')
  const targetId = href.replace('#', '')
  const targetEl = document.getElementById(targetId)
  if (targetEl) {
    targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeHeading.value = targetId
  }
}

function handleScroll() {
  if (headings.value.length === 0) return
  
  const headingElements = headings.value.map(h => document.getElementById(h.id)).filter(Boolean)
  let current = ''

  headingElements.forEach(el => {
    const rect = el.getBoundingClientRect()
    if (rect.top <= 100) {
      current = el.id
    }
  })

  if (current && current !== activeHeading.value) {
    activeHeading.value = current
  }
}

watch(() => props.content, () => {
  parseHeadings()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.toc-sidebar {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  z-index: 10;
}

.toc-container {
  padding: 12px 0;
  background-color: transparent;
}

.toc-header {
  padding: 0 16px 10px;
  border-bottom: 1px solid var(--lightest-navy);
  margin-bottom: 10px;
}

.toc-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--green);
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-width: 220px;
}

.toc-item {
  margin: 0;
  padding: 0;
}

.toc-link {
  display: block;
  padding: 6px 0;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--slate);
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-left: 16px;
  transition: all 0.3s ease;
}

.toc-item.toc-h3 .toc-link {
  padding-left: 28px;
}

.toc-link:hover {
  color: var(--green);
  opacity: 1;
}

.toc-link.active {
  color: var(--green);
}

.toc-empty {
  padding: 0 16px;
}
</style>

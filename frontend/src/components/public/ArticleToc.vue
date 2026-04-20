<template>
  <nav class="toc-sidebar" :class="{ collapsed: isCollapsed }" ref="tocSidebarRef">
    <button class="toc-toggle" @click="toggleToc" :title="isCollapsed ? '展开目录' : '收起目录'">
      <svg v-if="isCollapsed" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 12h18M3 6h18M3 18h18"/>
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 15l-6-6-6 6"/>
      </svg>
      <span>{{ isCollapsed ? '目录' : '收起' }}</span>
    </button>
    
    <Transition name="toc-slide">
      <div v-show="!isCollapsed" class="toc-container">
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
    </Transition>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  articleContainer: {
    type: Object,
    default: null
  }
})

const headings = ref([])
const activeHeading = ref('')
const tocSidebarRef = ref(null)
const isCollapsed = ref(false)
let resizeObserver = null
let manualCollapse = false

function toggleToc() {
  isCollapsed.value = !isCollapsed.value
  manualCollapse = !isCollapsed.value
}

function checkOverlap() {
  if (!tocSidebarRef.value || isCollapsed.value) return
  
  const tocRect = tocSidebarRef.value.getBoundingClientRect()
  const articleContent = document.querySelector('.article-content')
  
  if (!articleContent) return
  
  const contentRect = articleContent.getBoundingClientRect()
  
  if (tocRect.right > contentRect.left + 20 && !manualCollapse) {
    isCollapsed.value = true
  }
}

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
  window.addEventListener('resize', checkOverlap)
  
  resizeObserver = new ResizeObserver(checkOverlap)
  if (tocSidebarRef.value) {
    resizeObserver.observe(tocSidebarRef.value)
  }
  
  const articleContent = document.querySelector('.article-content')
  if (articleContent && resizeObserver) {
    resizeObserver.observe(articleContent)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', checkOverlap)
  
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})
</script>

<style scoped>
.toc-sidebar {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  z-index: 10;
  transition: all 0.3s ease;
}

.toc-sidebar.collapsed {
  transform: translateY(-50%) translateX(0);
}

.toc-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  color: var(--slate);
  font-family: var(--font-mono);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  margin-bottom: 12px;
}

.toc-toggle:hover {
  color: var(--green);
  border-color: var(--green);
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

.toc-slide-enter-active,
.toc-slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.toc-slide-enter-from,
.toc-slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.toc-slide-enter-to,
.toc-slide-leave-from {
  opacity: 1;
  max-height: 500px;
}
</style>

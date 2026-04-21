<template>
  <aside class="sidebar">
    <div class="sidebar-inner">
      <div class="logo">
        <router-link to="/" class="logo-link">
          <span class="logo-text">BC</span>
        </router-link>
      </div>

      <nav class="nav-links">
        <ol class="nav-list">
          <li class="nav-item" v-for="item in navItems" :key="item.name">
            <button 
              @click="handleNavClick(item)" 
              class="nav-link"
              :class="{ active: currentActive === item.name }"
            >
              <span class="nav-number">{{ item.number }}</span>
              <span class="nav-name">{{ item.name }}</span>
            </button>
          </li>
          <li class="nav-item">
            <button @click="navigateToAdmin" class="nav-link resume-link">
              <span class="nav-number"></span>
              <span class="resume-button">管理后台</span>
            </button>
          </li>
        </ol>
      </nav>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentActive = ref('About')

const navItems = [
  { name: 'About', number: '01.', sectionId: 'about', page: '/' },
  { name: '文章', number: '02.', sectionId: 'posts', page: '/' },
  { name: '分类标签', number: '03.', sectionId: 'categories', page: '/' },
]

function scrollToSection(sectionId) {
  setTimeout(() => {
    const element = document.getElementById(sectionId)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 200)
}

function handleNavClick(item) {
  currentActive.value = item.name
  
  if (route.path === item.page) {
    scrollToSection(item.sectionId)
  } else {
    router.push(item.page).then(() => {
      scrollToSection(item.sectionId)
    })
  }
}

function navigateToAdmin() {
  router.push('/admin')
}

function handleScroll() {
  if (route.path !== '/') return
  
  const sections = navItems.map(item => document.getElementById(item.sectionId)).filter(Boolean)
  const scrollPosition = window.scrollY + 300
  
  for (let i = sections.length - 1; i >= 0; i--) {
    const section = sections[i]
    if (section && section.offsetTop <= scrollPosition) {
      currentActive.value = navItems[i].name
      break
    }
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(() => route.path, (newPath) => {
  if (newPath === '/') {
    currentActive.value = 'About'
    setTimeout(() => handleScroll(), 300)
  } else if (newPath.startsWith('/category') || newPath.startsWith('/tag')) {
    currentActive.value = '分类标签'
  }
})
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100px;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.logo {
  margin-bottom: 20px;
}

.logo-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 2px solid var(--green);
  border-radius: 8px;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  transition: all 0.3s ease;
}

.logo-link:hover {
  background-color: rgba(100, 255, 218, 0.1);
  opacity: 1;
}

.nav-links {
  display: block;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
  counter-reset: item;
}

.nav-item {
  margin: 0;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.nav-item::after {
  content: '';
  display: block;
  width: 1px;
  height: 20px;
  background-color: var(--lightest-navy);
  margin: 10px 0;
}

.nav-item:last-child::after {
  display: none;
}

.nav-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 5px;
  background: none;
  border: none;
  cursor: pointer;
}

.nav-link:hover {
  opacity: 1;
}

.nav-link:hover .nav-name {
  color: var(--green);
}

.nav-link.active .nav-name {
  color: var(--green);
}

.nav-number {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--green);
  margin-bottom: 4px;
}

.nav-name {
  font-size: 12px;
  color: var(--light-slate);
  transition: all 0.3s ease;
  writing-mode: vertical-rl;
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.resume-link {
  margin-top: 10px;
}

.resume-button {
  padding: 12px 16px;
  border: 1px solid var(--green);
  border-radius: 4px;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: 12px;
  transition: all 0.3s ease;
  writing-mode: vertical-rl;
}

.resume-button:hover {
  background-color: rgba(100, 255, 218, 0.1);
  opacity: 1;
}
</style>

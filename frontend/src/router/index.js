import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const routes = [
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('../views/admin/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    name: 'Dashboard',
    component: () => import('../views/admin/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/articles',
    name: 'ArticleList',
    component: () => import('../views/admin/ArticleListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/articles/new',
    name: 'ArticleNew',
    component: () => import('../views/admin/ArticleEditorView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/articles/:id/edit',
    name: 'ArticleEdit',
    component: () => import('../views/admin/ArticleEditorView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/comments',
    name: 'AdminComments',
    component: () => import('../views/admin/AdminCommentsView.vue'),
    meta: { requiresAuth: true }
  },

  // NEW: Public routes (no auth required per D-03, D-04, D-05, D-06)
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/public/HomeView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/article/:idSlug',
    name: 'Article',
    component: () => import('../views/public/ArticleView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/category/:name',
    name: 'Category',
    component: () => import('../views/public/CategoryView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/tag/:name',
    name: 'Tag',
    component: () => import('../views/public/TagView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/public/SearchView.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for auth
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.token) {
    next('/admin/login')
  } else if (to.path === '/admin/login' && authStore.token) {
    next('/admin')
  } else {
    next()
  }
})

export default router

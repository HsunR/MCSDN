---
phase: 02-public-blog-image-upload
plan: 03
subsystem: ui
tags: [vue3, vue-router, pinia, markdown-it, highlight.js, image-upload]

# Dependency graph
requires:
  - phase: 01-admin-backend-dark-theme
    provides: JWT auth, admin dark theme, MarkdownEditor
provides:
  - Public blog frontend with timeline, article detail, category/tag filtering, search
  - ImageUploadModal with drag-drop support integrated into MarkdownEditor
affects: [02-public-blog-image-upload]

# Tech tracking
tech-stack:
  added: [striptags@3.2.0]
  patterns:
    - Public API client (publicApi.js) with paginated endpoints
    - Pinia store for public article state management
    - Vue Router hash mode public routes
    - Timeline layout with date dividers using computed grouping

key-files:
  created:
    - frontend/src/api/publicApi.js
    - frontend/src/stores/publicArticleStore.js
    - frontend/src/components/public/ArticleTimeline.vue
    - frontend/src/components/public/ArticleCard.vue
    - frontend/src/components/public/Pagination.vue
    - frontend/src/views/public/HomeView.vue
    - frontend/src/views/public/ArticleView.vue
    - frontend/src/views/public/CategoryView.vue
    - frontend/src/views/public/TagView.vue
    - frontend/src/views/public/SearchView.vue
    - frontend/src/components/admin/ImageUploadModal.vue
  modified:
    - frontend/src/router/index.js
    - frontend/src/components/admin/MarkdownEditor.vue

key-decisions:
  - "striptags for excerpt generation (150 chars, stripped markdown)"
  - "Timeline grouping via computed property on createdAt date"
  - "ImageUploadModal emits markdown string, MarkdownEditor appends to content"
  - "All public routes requireAuth: false in router meta"

patterns-established:
  - "Public API layer separates concerns from admin API (no JWT interceptor on publicApi)"
  - "Pinia store handles all public article state (list, detail, filters, search)"
  - "Date dividers via zh-CN localeDateString formatting"

requirements-completed: [CTGY-01, CTGY-02, CTGY-03, CTGY-04, CTGY-05, PUBL-01, PUBL-02, PUBL-03, PUBL-04, PUBL-05, PUBL-06, IMGE-01, IMGE-02, IMGE-03, IMGE-04, IMGE-05, IMGE-06]

# Metrics
duration: 5min
completed: 2026-04-13
---

# Phase 02-03: Public Blog Frontend Summary

**Vue 3 public blog with article timeline, category/tag filters, search, and ImageUploadModal for MarkdownEditor**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-13T13:15:00Z
- **Completed:** 2026-04-13T13:20:00Z
- **Tasks:** 4
- **Files modified:** 14

## Accomplishments
- Public article API layer with striptags@3.2.0 for excerpt generation
- ArticleTimeline with date dividers, ArticleCard with full metadata, Pagination with numbered pages
- Public views: HomeView, ArticleView, CategoryView, TagView, SearchView with Vue Router hash mode
- ImageUploadModal with drag-drop support integrated into MarkdownEditor toolbar

## Task Commits

Each task was committed atomically:

1. **Task 1: Install striptags and create public API layer** - `6723b19` (feat)
2. **Task 2: Create public Vue components (ArticleTimeline, ArticleCard, Pagination)** - `78a54b2` (feat)
3. **Task 3: Create public views (HomeView, ArticleView, CategoryView, TagView, SearchView)** - `a11579b` (feat)
4. **Task 4: Update Vue Router with public routes and create ImageUploadModal** - `7765a31` (feat)

## Files Created/Modified

- `frontend/src/api/publicApi.js` - Public article API client (getArticles, getArticle, getByCategory, getByTag, search)
- `frontend/src/stores/publicArticleStore.js` - Pinia store for public article state management
- `frontend/src/components/public/ArticleTimeline.vue` - Timeline grouping with date dividers (D-01)
- `frontend/src/components/public/ArticleCard.vue` - Article card with title, excerpt, tags, date, category badge (D-02)
- `frontend/src/components/public/Pagination.vue` - Numbered page buttons at 10 per page (D-09)
- `frontend/src/views/public/HomeView.vue` - Article timeline with pagination
- `frontend/src/views/public/ArticleView.vue` - Article detail with markdown rendering (D-03: #/article/:id-slug)
- `frontend/src/views/public/CategoryView.vue` - Category filter (D-04: #/category/:name)
- `frontend/src/views/public/TagView.vue` - Tag filter (D-05: #/tag/:name)
- `frontend/src/views/public/SearchView.vue` - Search results (D-06: #/search?q=keyword)
- `frontend/src/router/index.js` - Added public routes (Home, Article, Category, Tag, Search)
- `frontend/src/components/admin/ImageUploadModal.vue` - Image upload modal with drag-drop (D-13)
- `frontend/src/components/admin/MarkdownEditor.vue` - Added upload toolbar button (D-11), inserts ![alt](url) (D-12)
- `frontend/package.json` - Added striptags@3.2.0

## Decisions Made

- striptags used for excerpt generation (150 chars, strips markdown symbols)
- Timeline grouping via computed property using zh-CN localeDateString
- ImageUploadModal emits markdown string, MarkdownEditor appends to content
- All public routes have `requiresAuth: false` in router meta

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Next Phase Readiness

Public blog frontend complete. Ready for Phase 3 (Comments) which will add comment functionality to ArticleView.

## Self-Check

**PASSED** - All 14 files created, all 4 task commits found.

---
*Phase: 02-public-blog-image-upload*
*Completed: 2026-04-13*

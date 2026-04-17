# Milestones

## v1.1 CSDN 文章同步 (Shipped: 2026-04-17)

**Phases completed:** 2 phases, 12 plans, 20 tasks

**Key accomplishments:**

- CSDN sync infrastructure: Jsoup dependency added, csdn_sync_config table and article source fields created via Flyway migrations
- CSDN article fetcher with RestClient + Jsoup, HTML parser with XSS sanitization, and HTML-to-Markdown converter with CSDN image URL replacement
- CSDN sync orchestrator with MD5 dedup: config persistence, article dedup by csdn_article_id, source marking, and continue-on-error sync
- CsdnSyncController
- Gap:
- One-liner:
- CsdnSyncService.syncArticles() now calls ImageDownloadService.downloadAndReplaceImages() to replace CSDN image URLs with local storage paths during sync, with continue-on-error handling
- Blocking warning modal in ArticleEditorView for CSDN-synced articles
- RestClient.create() replaces null-injected RestClient.Builder, fixing all-zero CSDN sync counts
- CSDN article list fetch now uses JSON API with pagination — HTML scraping removed
- RestClient.create() static factory replaces null-injected RestClient.Builder, enabling MD5 deduplication

---

## v1.0 MVP (Shipped: 2026-04-15)

**Phases completed:** 3 phases, 9 plans, 29 tasks

**Key accomplishments:**

- JWT auth with BCrypt password hashing, rate-limited login, and Spring Security filter chain
- Article CRUD backend with Flyway migrations, MyBatis mappers, JWT auth, and protected /api/admin/articles endpoints
- Vue 3 admin frontend scaffold with Tailwind dark theme, markdown-it + highlight.js code highlighting, and article management UI
- PublicArticleController
- 1. [Rule 1 - Bug] Fixed PublicArticleResponse.generateSlug() compilation error
- Vue 3 public blog with article timeline, category/tag filters, search, and ImageUploadModal for MarkdownEditor
- 1. [Rule 3 - Blocking Issue] CommentRequest DTO created in Task 3 instead of Task 4

---

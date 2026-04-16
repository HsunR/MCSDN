# Phase 5: Image Handling & Sync UI - Context

**Gathered:** 2026-04-16
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 5 delivers two capabilities:
1. **Image handling** — Download CSDN images to local filesystem, replace URLs in Markdown content with local paths, deduplicate by URL hash (MD5)
2. **Sync management UI** — Admin sidebar page for CSDN sync configuration, sync trigger button, results display, and edit warning for synced articles

UI components from Phase 4 (REST API for config/sync) are extended here. Phase 4 covered backend sync infrastructure; Phase 5 covers image download + admin UI.

</domain>

<decisions>
## Implementation Decisions

### Sync Results Display
- **D-01:** Inline alert/panel below button — after clicking sync, results panel appears below the button showing created/updated/skipped counts. No toast, no modal, no navigation.

### Sync UI Location
- **D-02:** Sidebar menu item — "CSDN Sync" appears as a new entry in the admin sidebar. Dedicated page for sync config and trigger, separate from article list. Scales if more sync options added later.

### Synced Article Edit Warning
- **D-03:** Blocking modal dialog — when an article with `source: CSDN` is opened in the editor, a modal immediately blocks entry: "This article was synced from CSDN. Editing is not recommended as changes will be overwritten on next sync." User must acknowledge before proceeding.

### Image Download Failure Handling
- **D-04:** Retry with backoff — retry each failed image 3 times with exponential backoff (1s, 2s, 4s) before giving up. After max retries, fall back to broken image placeholder.

### Post-Sync Behavior
- **D-05:** Auto-refresh article list — after sync completes and results are displayed, the article list automatically reloads to show new/updated articles.

### Technical Constraints (from prior phases)
- Image storage path: `/uploads/{year}/{month}/{uuid}.{ext}` (from v1.0 constraints)
- Image deduplication: by URL hash (MD5), not re-downloading duplicates
- Sync runs synchronously (D-05 from Phase 4)
- Jsoup 1.18.x for HTML parsing (Phase 4)
- Spring RestClient for HTTP (Phase 4)

</decisions>

<specifics>
## Specific Ideas

- "同步" button label — use Chinese label matching the rest of the admin UI
- Sync results panel should show counts with labels: "成功: X | 更新: Y | 跳过: Z | 失败: W"
- Modal dialog should have a clear "我已知悉" (I understand) button to dismiss

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### v1.1 Requirements
- `.planning/REQUIREMENTS.md` — SYNC-07, SYNC-09, SYNC-10, SYNC-11, SYNC-12, SYNC-13 define the phase scope

### v1.1 Phase 4 Context
- `.planning/phases/04-core-sync-infrastructure/04-CONTEXT.md` — prior decisions (Jsoup 1.18.x, Spring RestClient, synchronous sync, MD5 dedup, continue-on-error)

### v1.0 Patterns
- `frontend/src/views/admin/DashboardView.vue` — admin page structure, sidebar integration
- `frontend/src/views/admin/ArticleEditorView.vue` — existing editor with route params, modal patterns
- `frontend/src/stores/articleStore.js` — Pinia store pattern for API calls
- `backend/src/main/java/com/blog/controller/CsdnSyncController.java` — existing backend API endpoints (GET/POST /api/admin/csdn-sync/config, POST /api/admin/csdn-sync/sync)

### Project Constraints
- `.planning/PROJECT.md` — Core value: 快速搭建一个属于自己的技术博客空间，专注于内容创作，无需操心复杂功能

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- ArticleEditorView.vue modal pattern — existing route-based editor with modal for warnings
- DashboardView.vue admin layout — card-based stats display in dark theme
- Pinia store pattern — articleStore.js with fetchArticle, updateArticle methods
- AdminSidebar.vue — existing sidebar navigation structure

### Established Patterns
- Dark theme Tailwind classes: `bg-gray-900`, `border-gray-700`, `text-gray-400`
- Card components: `bg-gray-800 rounded-lg p-6 border border-gray-700`
- Modal pattern: confirm dialogs with dual buttons
- API calls: axios with `/api` baseURL, JWT interceptor

### Integration Points
- CsdnSyncController.java endpoints: GET/POST /api/admin/csdn-sync/config, POST /api/admin/csdn-sync/sync
- SyncResultResponse.java: existing response DTO with created/updated/skipped/error counts
- Article entity: already has `source` and `csdnArticleId` fields (Phase 4)

</codebase_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 05-image-handling-sync-ui*
*Context gathered: 2026-04-16*

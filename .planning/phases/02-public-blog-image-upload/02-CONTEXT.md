# Phase 2: Public Blog & Image Upload - Context

**Gathered:** 2026-04-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Public blog frontend for visitors (article list, detail, category/tag filtering, search) + admin category/tag management + image upload to local filesystem. Depends on Phase 1 (admin auth, article CRUD, dark theme).

</domain>

<decisions>
## Implementation Decisions

### Public Blog — Article List Layout
- **D-01:** Timeline layout with date dividers — articles grouped by date
- **D-02:** Per-article metadata: title + excerpt (2 lines) + tags + date + category badge

### Public Blog — URL Structure (Hash Mode)
- **D-03:** Article detail: `#/article/:id-slug` (e.g., `#/article/42-how-to-use-vue3`)
- **D-04:** Category filter: `#/category/:name` (slugified, e.g., `#/category/backend`)
- **D-05:** Tag filter: `#/tag/:name` (slugified, e.g., `#/tag/vue`)
- **D-06:** Search results: `#/search?q=keyword`

### Public Blog — Search
- **D-07:** MySQL FULLTEXT index with `MATCH() AGAINST()` in NATURAL LANGUAGE MODE
- **D-08:** Elasticsearch deferred to Phase 4 (infrastructure expansion, not within local-deploy constraint)

### Public Blog — Pagination
- **D-09:** Numbered pages (1, 2, 3...) — 10 articles per page (PUBL-01)

### Admin — Category/Tag Management
- **D-10:** Dedicated admin page for category/tag CRUD (CTGY-01, CTGY-02)

### Image Upload
- **D-11:** Toolbar button in MarkdownEditor opens upload modal
- **D-12:** Image URL auto-inserted as `![alt](url)` at cursor position after upload success
- **D-13:** Drag-drop as bonus UX if time permits, not blocking

### Implementation Notes
- Category/tag name uniqueness enforced at DB level (UNIQUE constraint already in V2 migration)
- Excerpt auto-generated from content (strip Markdown, first 150 chars) — no manual excerpt field
- Published articles only shown on public blog (DRAFT filtered out)
- Slug auto-generated from title (lowercase, spaces to hyphens, special chars removed)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Specs
- `.planning/ROADMAP.md` — Phase 2 goal and success criteria
- `.planning/REQUIREMENTS.md` — CTGY-01 to CTGY-05, PUBL-01 to PUBL-06, IMGE-01 to IMGE-06
- `.planning/PROJECT.md` — Core value, constraints (local deploy, MySQL only)
- `.planning/STATE.md` — Current phase state

### Phase 1 Context (already implemented)
- `backend/src/main/resources/db/migration/V2__articles_categories_tags.sql` — Existing schema for categories, tags, articles, article_tags
- `frontend/src/router/index.js` — Hash-mode router, existing admin routes
- `frontend/src/views/admin/ArticleListView.vue` — Existing admin article list pattern
- `frontend/src/components/admin/MarkdownEditor.vue` — Existing editor component (image upload hooks here)

### Tech Constraints
- MySQL FULLTEXT only (no Elasticsearch in Phase 2)
- Local filesystem storage: `/uploads/{year}/{month}/{uuid}.{ext}`
- Max image size: 5MB; allowed types: jpg, jpeg, png, gif, webp
- Image served via Spring Boot static handler at `/uploads/**`

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- `MarkdownEditor.vue` — Image upload toolbar button should integrate here; existing preview pane can display uploaded images
- `ArticleListView.vue` (admin) — Can reference for list rendering patterns, but public timeline is different layout
- Dark theme CSS variables — Already defined in Tailwind config, apply to all public views

### Established Patterns
- Hash-mode router — All public routes must use `#/` prefix
- Vue 3 Composition API with `<script setup>` — Continue pattern
- Pinia store for auth token — Consider a public article store for list/detail state
- Axios for HTTP — Continue pattern; public API needs no auth header

### Integration Points
- Backend public article controller — New `@RestController` at `/api/articles` (public, no auth required)
- Backend category/tag controllers — New admin controllers at `/api/admin/categories`, `/api/admin/tags`
- Image upload controller — New at `/api/admin/upload` (auth required)
- Frontend router — Add public routes alongside existing admin routes
- Article entity already has `categoryId` and `tags` — no schema changes needed for Phase 2

</codebase_context>

<specifics>
## Specific Ideas

No specific reference sites or "I want it like X" moments — standard blog patterns accepted.

</specifics>

<deferred>
## Deferred Ideas

### Elasticsearch (Phase 4)
- **Elasticsearch for search** — More capable full-text search with typo tolerance and ranking. Not within Phase 2 scope; deferred to Phase 4 when infrastructure can expand beyond MySQL-only local deploy.

### Nice-to-Haves Not in Scope
- Drag-drop image upload (mentioned as bonus UX)
- Real-time search preview/typeahead
- Social sharing meta tags (Open Graph) — Phase 2 SEO deferred

</deferred>

---

*Phase: 02-public-blog-image-upload*
*Context gathered: 2026-04-13*

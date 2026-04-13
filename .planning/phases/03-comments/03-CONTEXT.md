# Phase 3: Comments - Context

**Gathered:** 2026-04-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Public visitors can submit comments on articles (name + content, no auth required). Comments default to PENDING status and are not publicly visible until admin approves them. Admin can view pending comments, approve/reject them, and delete any comment. Approved comments display below the article content. Depends on Phase 2 (public blog, article views).

</domain>

<decisions>
## Implementation Decisions

### Comment Form UI
- **D-01:** Comment form appears below article content, before the comment list
- **D-02:** Form fields: name (text input) + content (textarea). No email/URL in v1.
- **D-03:** Submit button with loading state. Success: "Comment submitted, awaiting approval." Error: inline error message.

### Comment Display
- **D-04:** Approved comments displayed below article in a flat list (no threading/nesting in v1)
- **D-05:** Comment card shows: commenter name, formatted date, comment content
- **D-06:** Comments ordered by creation date (oldest first) — standard blog convention

### Comment Status (Admin)
- **D-07:** Admin comment management via dedicated `/admin/comments` page
- **D-08:** Comment list shows all comments (PENDING/APPROVED/REJECTED) with status filter tabs
- **D-09:** Admin actions per comment: Approve (PENDING→APPROVED), Reject (PENDING→REJECTED), Delete

### Comment Data Model
- **D-10:** Comment entity: id, article_id, author_name, content, status (PENDING/APPROVED/REJECTED), created_at
- **D-11:** Status defaults to PENDING on creation (CMNT-02, CMNT-03 — already locked in STATE.md)

### Spam Protection
- **D-12:** Basic honeypot field (hidden CSS field, bot-filled = reject silently)
- **D-13:** No captcha in v1 (keeps comment friction low for real visitors)

### Backend API
- **D-14:** Public POST `/api/articles/{id}/comments` — submit comment (no auth)
- **D-15:** Public GET `/api/articles/{id}/comments` — list approved comments only (CMNT-05)
- **D-16:** Admin GET `/api/admin/comments` — list all comments with status filter
- **D-17:** Admin PATCH `/api/admin/comments/{id}/approve` — approve comment
- **D-18:** Admin PATCH `/api/admin/comments/{id}/reject` — reject comment
- **D-19:** Admin DELETE `/api/admin/comments/{id}` — delete comment

### Frontend Routes
- **D-20:** Admin comments page: `#/admin/comments`
- **D-21:** Comments section integrated into ArticleView (public) below content

### Integration Points
- **D-22:** ArticleView.vue — add `<CommentSection>` below `<div class="prose">`
- **D-23:** Admin sidebar — add "Comments" nav link (shows pending count badge)
- **D-24:** publicArticleStore — add fetchComments(articleId) method
- **D-25:** CommentService — handles all comment business logic

### Claude's Discretion
- Exact styling of comment cards (colors, spacing, typography)
- Comment form validation error messages
- Empty state messages (no comments yet)
- Loading skeleton for comment list

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Specs
- `.planning/ROADMAP.md` — Phase 3 goal and success criteria
- `.planning/REQUIREMENTS.md` — CMNT-01 to CMNT-05
- `.planning/PROJECT.md` — Core value, constraints (local deploy, MySQL only)
- `.planning/STATE.md` — "Phase 3: Comments default to PENDING, admin must approve before public" (already locked)
- `.planning/phases/02-public-blog-image-upload/02-CONTEXT.md` — Prior decisions on public blog

### Phase 1/2 Codebase
- `backend/src/main/resources/db/migration/V2__articles_categories_tags.sql` — Existing schema (comment table needs new migration)
- `frontend/src/views/public/ArticleView.vue` — Where comment section integrates
- `frontend/src/views/admin/DashboardView.vue` — Admin sidebar pattern
- `frontend/src/stores/publicArticleStore.js` — Where comment fetching methods go
- `frontend/src/router/index.js` — Hash-mode router, admin route guards
- `backend/src/main/java/com/blog/entity/Article.java` — Existing entity (add comments relationship)

### Tech Constraints
- No external services (local deploy only)
- MySQL database (existing connection)
- Hash-mode Vue Router (all routes use `#/` prefix)
- Dark theme Tailwind CSS (already in place)

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- `ArticleView.vue` — Comment section component will slot below rendered markdown content
- `publicArticleStore.js` — fetchComments(articleId) follows existing fetchArticle pattern
- Admin sidebar from DashboardView.vue — reuse for pending count badge on Comments nav

### Established Patterns
- Hash-mode router — all new routes use `#/` prefix
- Vue 3 Composition API with `<script setup>` — continue pattern
- Pinia store for state management — commentStore for admin, fetch in publicArticleStore
- Axios for HTTP — continue pattern
- Flyway migrations for DB schema changes — new V3__comments.sql

### Integration Points
- New Comment entity: `backend/src/main/java/com/blog/entity/Comment.java`
- New CommentMapper + CommentMapper.xml for DB access
- New CommentService + CommentController for API
- New CommentSection.vue component in public views
- New AdminCommentsView.vue in admin views
- Admin route added to router: `#/admin/comments`

</codebase_context>

<specifics>
## Specific Ideas

No specific reference sites or "I want it like X" moments — standard blog comment patterns accepted.

</specifics>

<deferred>
## Deferred Ideas

### Phase 4 or later
- Email/push notifications when admin approves comment
- Threaded/nested comment replies
- Comment upvotes or reactions
- Anti-spam service integration (Akismet, etc.)
- Commenter email field (for notification on reply)

</deferred>

---

*Phase: 03-comments*
*Context gathered: 2026-04-13 (auto mode)*

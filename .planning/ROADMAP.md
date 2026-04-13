# Roadmap: Personal Blog

## Overview

A personal blog for a programmer, featuring a dark-themed admin backend for content management and a public-facing blog for readers. Three phases deliver: (1) admin auth + article CRUD + dark theme, (2) public blog frontend + categories/tags + image upload, (3) comment system with moderation.

## Phases

- [x] **Phase 1: Admin Backend & Dark Theme** - JWT auth, article management, dark UI
- [x] **Phase 2: Public Blog & Image Upload** - Public frontend, categories/tags, image storage
- [ ] **Phase 3: Comments** - Public comments with admin moderation

## Phase Details

### Phase 1: Admin Backend & Dark Theme
**Goal**: Admin can securely manage blog content with a dark-themed interface
**Depends on**: Nothing (first phase)
**Requirements**: AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, ARTL-01, ARTL-02, ARTL-03, ARTL-04, ARTL-05, ARTL-06, THME-01, THME-02, THME-03, THME-04
**Success Criteria** (what must be TRUE):
  1. Admin can log in with username/password and receive JWT token (AUTH-01, AUTH-02)
  2. JWT token validates on all admin API endpoints (AUTH-03)
  3. Login rate limited to 5 attempts per minute per IP (AUTH-04)
  4. Admin can log out (token invalidated client-side) (AUTH-05)
  5. Admin can create/edit/delete articles with title, Markdown content, category, and tags (ARTL-01, ARTL-02, ARTL-03)
  6. Articles render Markdown with code syntax highlighting for JS/TS/Python/Go/Java/SQL (ARTL-04)
  7. Articles have published/draft status and auto-populated created_at/updated_at timestamps (ARTL-05, ARTL-06)
  8. Dark theme applied to entire admin interface with dark code highlighting (THME-01, THME-02, THME-03, THME-04)
**Plans**: 01-01, 01-02, 01-03 (all complete)

### Phase 2: Public Blog & Image Upload
**Goal**: Public visitors can browse articles by category/tag/search; admin can manage categories/tags and upload images
**Depends on**: Phase 1
**Requirements**: CTGY-01, CTGY-02, CTGY-03, CTGY-04, CTGY-05, PUBL-01, PUBL-02, PUBL-03, PUBL-04, PUBL-05, PUBL-06, IMGE-01, IMGE-02, IMGE-03, IMGE-04, IMGE-05, IMGE-06
**Success Criteria** (what must be TRUE):
  1. Public can view paginated article list (10 per page) showing title, date, category, excerpt (PUBL-01, PUBL-06)
  2. Public can view full article with rendered Markdown content (PUBL-02)
  3. Public can filter article list by category (PUBL-03, CTGY-04)
  4. Public can filter article list by tag (PUBL-04, CTGY-05)
  5. Public can search articles by keyword using MySQL full-text search (PUBL-05)
  6. Admin can create/edit/delete categories and tags (CTGY-01, CTGY-02)
  7. Articles can have one category and multiple tags (CTGY-03)
  8. Admin can upload images via API (max 5MB, jpg/jpeg/png/gif/webp) (IMGE-01, IMGE-05, IMGE-06)
  9. Images stored on local filesystem at /uploads/{year}/{month}/{uuid}.{ext} and served correctly (IMGE-02, IMGE-03)
  10. Image URLs insertable into Markdown as ![alt](/uploads/...) (IMGE-04)
**Plans**: 02-01, 02-02, 02-03 (all complete)

### Phase 3: Comments
**Goal**: Public visitors can comment on articles; admin moderates through approve/reject workflow
**Depends on**: Phase 2
**Requirements**: CMNT-01, CMNT-02, CMNT-03, CMNT-04, CMNT-05
**Success Criteria** (what must be TRUE):
  1. Public visitors can submit comments on articles (name + content, no auth required) (CMNT-01)
  2. New comments default to PENDING status and are not publicly visible (CMNT-02)
  3. Admin can view and approve/reject pending comments (CMNT-03)
  4. Admin can delete any comment (CMNT-04)
  5. Approved comments display on the article page (CMNT-05)
**Plans**:
- [ ] 03-01-PLAN.md — Database + Backend API (Flyway migration, entity, mapper, service, controllers)
- [ ] 03-02-PLAN.md — Admin Frontend (AdminCommentsView, commentStore, adminCommentApi, router, sidebar)
- [ ] 03-03-PLAN.md — Public Frontend (CommentSection, commentApi, ArticleView integration)

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Admin Backend & Dark Theme | 3/3 | ✅ Complete | 2026-04-13 |
| 2. Public Blog & Image Upload | 3/3 | ✅ Complete | 2026-04-13 |
| 3. Comments | 0/3 | Ready to execute | - |

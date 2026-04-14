---
phase: 02-public-blog-image-upload
plan: "01"
type: execute
subsystem: backend
tags:
  - public-api
  - rest
  - mybatis
  - pagination
  - fulltext-search
dependency_graph:
  requires: []
  provides:
    - PublicArticleController: REST API endpoints for public blog
    - PublicArticleService: Business logic for public article access
  affects:
    - frontend: Consumes public REST API
tech_stack:
  added:
    - Spring Boot REST Controller
    - MyBatis XML mapper queries
    - DTOs with validation
  patterns:
    - Pagination with limit/offset
    - MySQL FULLTEXT search (NATURAL LANGUAGE MODE)
    - Slug generation from title
    - Excerpt extraction from markdown
key_files:
  created:
    - backend/src/main/java/com/blog/dto/ArticleListRequest.java
    - backend/src/main/java/com/blog/dto/ArticleListResponse.java
    - backend/src/main/java/com/blog/dto/PublicArticleResponse.java
    - backend/src/main/java/com/blog/service/PublicArticleService.java
    - backend/src/main/java/com/blog/service/impl/PublicArticleServiceImpl.java
    - backend/src/main/java/com/blog/controller/PublicArticleController.java
    - backend/src/main/java/com/blog/controller/GlobalExceptionHandler.java
  modified:
    - backend/src/main/java/com/blog/entity/Article.java (added category field)
    - backend/src/main/java/com/blog/mapper/ArticleMapper.java (added 8 new methods)
    - backend/src/main/resources/mapper/ArticleMapper.xml (added public queries)
decisions:
  - Use case-insensitive category/tag name matching via LOWER() in SQL
  - MySQL FULLTEXT MATCH() AGAINST() in NATURAL LANGUAGE MODE for search
  - 404 response for non-published articles (information disclosure mitigation)
  - Category/Tag as inner DTOs within PublicArticleResponse
metrics:
  duration_minutes: "~5"
  completed_date: "2026-04-13"
---

# Phase 02 Plan 01 Summary: Public Blog Backend API

## One-liner

Backend REST API for public blog with paginated article listing, detail view, category/tag filtering, and MySQL FULLTEXT search.

## Completed Tasks

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Create DTOs for public article API | `fc5360b` | ArticleListRequest.java, ArticleListResponse.java, PublicArticleResponse.java |
| 2 | Add MyBatis mapper queries for public articles | `da3f420` | ArticleMapper.xml, ArticleMapper.java, Article.java |
| 3 | Create PublicArticleController and Service | `30358b8` | PublicArticleController.java, PublicArticleService.java, PublicArticleServiceImpl.java, GlobalExceptionHandler.java |

## What Was Built

**PublicArticleController** exposes:
- `GET /api/articles` - paginated article list (page, pageSize params)
- `GET /api/articles/{id}` - article detail with content, excerpt, category, tags
- `GET /api/articles/category/{categorySlug}` - filter by category (case-insensitive)
- `GET /api/articles/tag/{tagSlug}` - filter by tag (case-insensitive)
- `GET /api/articles/search?q=keyword` - FULLTEXT search with relevance ranking

**PublicArticleResponse** includes:
- `generateExcerpt(markdown)` - strips markdown syntax, truncates 150 chars
- `generateSlug(title)` - lowercase, spaces-to-hyphens, special-chars-removed
- Inner `CategoryDTO` and `TagDTO` with id, name, slug fields

**Pagination**: All list endpoints return `{ articles[], page, pageSize, total, totalPages }`

**Security**: Only `PUBLISHED` articles are returned; drafts return 404.

## Deviations from Plan

None - plan executed exactly as written.

## Threat Surface

| Flag | File | Description |
|------|------|-------------|
| N/A | - | No new threat surface beyond plan's threat_model |

## Known Stubs

None.

## Verification

Backend compiles without errors:
```bash
cd backend && mvn compile -q
```

## Self-Check

All files exist, all commits verified, compilation successful.

## Self-Check: PASSED

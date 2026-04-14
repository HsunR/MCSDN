---
phase: 02-public-blog-image-upload
plan: "02"
subsystem: backend
tags: [category, tag, upload, image, admin, rest-api]
dependency_graph:
  requires: []
  provides:
    - id: CTGY-01
      description: Admin can create/edit/delete categories via dedicated admin page
    - id: CTGY-02
      description: Admin can create/delete tags via dedicated admin page
    - id: CTGY-03
      description: Articles can have one category and multiple tags
    - id: IMGE-01
      description: Admin can upload images via toolbar button in MarkdownEditor
    - id: IMGE-02
      description: Images stored at /uploads/{year}/{month}/{uuid}.{ext} on local filesystem
    - id: IMGE-03
      description: Uploaded images served at /uploads/** via Spring Boot static handler
    - id: IMGE-04
      description: Image URL returned as ![alt](url) for Markdown insertion
    - id: IMGE-05
      description: Max file size 5MB enforced
    - id: IMGE-06
      description: Allowed types: jpg, jpeg, png, gif, webp
  affects:
    - backend: CategoryController, TagController, UploadController, WebConfig
    - database: categories, tags tables (already existed)
tech_stack:
  added:
    - SlugUtils: URL slug generation utility
  patterns:
    - Spring Boot REST controllers with @Autowired services
    - MyBatis @Mapper interfaces with XML mappers
    - MultipartFile handling for image uploads
    - WebMvcConfigurer for static resource serving
key_files:
  created:
    - backend/src/main/java/com/blog/util/SlugUtils.java
    - backend/src/main/java/com/blog/service/CategoryService.java
    - backend/src/main/java/com/blog/service/TagService.java
    - backend/src/main/java/com/blog/service/impl/CategoryServiceImpl.java
    - backend/src/main/java/com/blog/service/impl/TagServiceImpl.java
    - backend/src/main/java/com/blog/controller/CategoryController.java
    - backend/src/main/java/com/blog/controller/TagController.java
    - backend/src/main/java/com/blog/controller/UploadController.java
    - backend/src/main/java/com/blog/config/WebConfig.java
  modified:
    - backend/src/main/java/com/blog/mapper/CategoryMapper.java
    - backend/src/main/java/com/blog/mapper/TagMapper.java
    - backend/src/main/resources/mapper/CategoryMapper.xml
    - backend/src/main/resources/mapper/TagMapper.xml
    - backend/src/main/resources/application.yml
    - backend/src/main/java/com/blog/dto/PublicArticleResponse.java
decisions:
  - Use SlugUtils.slugify() for URL-safe slug generation (5-line custom regex, zero dependencies)
  - Case-insensitive name matching via LOWER() in SQL for category/tag uniqueness
  - UUID filenames prevent path traversal and file overwriting
  - Service-layer uniqueness checks complement DB UNIQUE constraints
metrics:
  duration: ~2 minutes
  completed: "2026-04-13T13:14:00Z"
  tasks_completed: 4
---

# Phase 02 Plan 02 Summary: Admin CRUD + Image Upload Backend

## One-liner

Backend admin CRUD for categories/tags plus image upload with local filesystem storage and Spring Boot static serving.

## What Was Built

Implemented backend infrastructure for admin category/tag management and image upload:

1. **SlugUtils** - URL-safe slug generation utility with regex-based approach
2. **CategoryService/TagService** - Full CRUD with case-insensitive uniqueness enforcement
3. **CategoryController/TagController** - Admin REST endpoints at `/api/admin/categories` and `/api/admin/tags`
4. **UploadController** - Image upload at `/api/admin/upload` with type/size validation
5. **WebConfig** - Static resource handler for `/uploads/**`

## Commits

| Hash | Message |
|------|---------|
| 6ea17fa | feat(02-02): add SlugUtils and mapper findByName/countByName methods |
| 4a8f8e2 | feat(02-02): create CategoryService and TagService with CRUD operations |
| bf1a170 | feat(02-02): create CategoryController, TagController, and UploadController |
| 7384efc | feat(02-02): create WebConfig for static uploads serving |

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed PublicArticleResponse.generateSlug() compilation error**
- **Found during:** Task 1 verification
- **Issue:** Incorrect use of `Pattern.matcher().replaceAll("")` which doesn't compile
- **Fix:** Simplified to direct String operations with proper regex
- **Files modified:** backend/src/main/java/com/blog/dto/PublicArticleResponse.java
- **Commit:** 6ea17fa

## Threat Flags

None - this plan's components were already modeled in the threat_model section of the plan.

## Known Stubs

None identified.

## Self-Check

- [x] All 4 tasks executed
- [x] Each task committed individually
- [x] Backend compiles without errors
- [x] All files created/modified match plan artifacts
- [x] No stubs identified

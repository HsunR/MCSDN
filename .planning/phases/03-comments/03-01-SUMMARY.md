---
phase: "03"
plan: "01"
subsystem: backend
tags: [comments, backend, mybatis, spring-boot]
dependency_graph:
  requires: []
  provides:
    - comment_entity
    - comment_mapper
    - comment_service
    - comment_controllers
  affects:
    - frontend_comment_section
tech_stack:
  added:
    - MyBatis CommentMapper
    - Spring @RestController x2
    - Flyway V3__comments.sql
    - SpamException (honeypot rejection)
  patterns:
    - MyBatis #{ } parameter binding only
    - Constructor injection in controllers
    - RuntimeException for spam rejection
    - ResponseEntity for REST responses
key_files:
  created:
    - backend/src/main/resources/db/migration/V3__comments.sql
    - backend/src/main/java/com/blog/entity/Comment.java
    - backend/src/main/java/com/blog/mapper/CommentMapper.java
    - backend/src/main/resources/mapper/CommentMapper.xml
    - backend/src/main/java/com/blog/service/CommentService.java
    - backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java
    - backend/src/main/java/com/blog/service/impl/SpamException.java
    - backend/src/main/java/com/blog/dto/CommentRequest.java
    - backend/src/main/java/com/blog/controller/PublicCommentController.java
    - backend/src/main/java/com/blog/controller/AdminCommentController.java
decisions:
  - "Comments default to PENDING on creation (database DEFAULT + service layer)"
  - "Public GET returns only APPROVED comments (findByArticleIdAndStatus with 'APPROVED')"
  - "Honeypot check: if honeypot field is non-empty, throw SpamException silently"
  - "SpamException caught in controller returns 200 with success message (no error signal to bots)"
  - "CommentRequest DTO auto-created in Task 3 to unblock compilation (Rule 3 deviation)"
metrics:
  duration: "~2 min"
  completed: "2026-04-13T13:56:00Z"
  tasks: "4/4"
---

# Phase 03 Plan 01: Comments Backend Summary

## One-liner

Comment backend: Flyway migration, MyBatis mapper, service with honeypot spam protection, and REST controllers for public submission and admin moderation.

## Completed Tasks

| Task | Name | Commit | Files |
| ---- | ---- | ------ | ----- |
| 1 | Flyway migration and Comment entity | `3a62a4b` | V3__comments.sql, Comment.java |
| 2 | CommentMapper interface and XML | `51ac86f` | CommentMapper.java, CommentMapper.xml |
| 3 | CommentService, SpamException, CommentRequest | `13aa37e` | CommentService.java, CommentServiceImpl.java, SpamException.java, CommentRequest.java |
| 4 | Both REST controllers | `07981ed` | PublicCommentController.java, AdminCommentController.java |

## Commits

- `3a62a4b` feat(03-01): add comment table migration and entity
- `51ac86f` feat(03-01): add CommentMapper interface and XML
- `13aa37e` feat(03-01): add CommentService, SpamException, and CommentRequest DTO
- `07981ed` feat(03-01): add PublicCommentController and AdminCommentController

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking Issue] CommentRequest DTO created in Task 3 instead of Task 4**
- **Found during:** Task 3 compilation
- **Issue:** `CommentService` and `CommentServiceImpl` import `CommentRequest` from `com.blog.dto`, but the plan placed `CommentRequest.java` creation in Task 4. Compilation failed.
- **Fix:** Created `CommentRequest.java` in the same commit as `CommentService` (Task 3). This was the correct call since the service fundamentally depended on it.
- **Files modified:** `backend/src/main/java/com/blog/dto/CommentRequest.java` (new)
- **Commit:** `13aa37e`
- **Impact:** No impact. Task 4 still creates both controllers as planned. The DTO was simply moved earlier.

## Verification Results

### Compilation
```
cd backend && mvn compile -q
BUILD SUCCESS
```

### Acceptance Criteria

All acceptance criteria across 4 tasks passed:

- V3__comments.sql: `CREATE TABLE comments`, `PENDING` default, `CASCADE FK` verified
- Comment.java: all fields (id, articleId, authorName, content, status, createdAt) verified
- CommentMapper.java: interface with all 8 methods verified
- CommentMapper.xml: namespace, resultMap, all SQL statements, `#{ }` binding only (no `${}`) verified
- CommentService.java: interface with 7 methods verified
- CommentServiceImpl.java: honeypot check, "APPROVED" constant, SpamException usage verified
- SpamException.java: RuntimeException subclass verified
- CommentRequest.java: @NotBlank, @Size, honeypot field verified
- PublicCommentController.java: GET/{id}/comments, POST/{id}/comments verified
- AdminCommentController.java: GET/, PATCH/{id}/approve, PATCH/{id}/reject, DELETE/{id} verified

## Threat Surface Scan

No new security surface introduced beyond what was modeled in the plan's threat register:

| Flag | File | Description |
|------|------|-------------|
| N/A | All new files | All user inputs use MyBatis `#{ }` binding (T-03-01 mitigated). Public GET returns only APPROVED comments (T-03-04 mitigated). Honeypot handles bots (T-03-03 mitigated). Admin endpoints require JWT via existing /api/admin/** rule (T-03-05 accepted). |

## Known Stubs

None - all stubs from the plan were implemented with real data wiring.

## Self-Check

- [x] `backend/src/main/resources/db/migration/V3__comments.sql` exists
- [x] `backend/src/main/java/com/blog/entity/Comment.java` exists
- [x] `backend/src/main/java/com/blog/mapper/CommentMapper.java` exists
- [x] `backend/src/main/resources/mapper/CommentMapper.xml` exists
- [x] `backend/src/main/java/com/blog/service/CommentService.java` exists
- [x] `backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java` exists
- [x] `backend/src/main/java/com/blog/service/impl/SpamException.java` exists
- [x] `backend/src/main/java/com/blog/dto/CommentRequest.java` exists
- [x] `backend/src/main/java/com/blog/controller/PublicCommentController.java` exists
- [x] `backend/src/main/java/com/blog/controller/AdminCommentController.java` exists
- [x] `git log --oneline | grep -q "3a62a4b"` (commit exists)
- [x] `git log --oneline | grep -q "51ac86f"` (commit exists)
- [x] `git log --oneline | grep -q "13aa37e"` (commit exists)
- [x] `git log --oneline | grep -q "07981ed"` (commit exists)

## Self-Check: PASSED

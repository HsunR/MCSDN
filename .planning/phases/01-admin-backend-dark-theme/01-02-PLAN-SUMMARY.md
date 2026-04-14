---
phase: "01-admin-backend-dark-theme"
plan: "02"
subsystem: "api"
tags: ["jwt", "mybatis", "flyway", "spring-security", "article-crud"]

# Dependency graph
requires:
  - phase: "01-01"
    provides: "Auth infrastructure (User entity, SecurityConfig, JwtUtils)"
provides:
  - "Article CRUD API at /api/admin/articles"
  - "Categories and Tags management via ArticleService"
  - "JWT-protected admin endpoints"
  - "Rate-limited login endpoint (5 attempts/minute/IP)"
affects: ["02-public-blog", "03-comments"]

# Tech tracking
tech-stack:
  added: ["jjwt 0.12.5", "flyway-core 10.10.0", "flyway-mysql 10.10.0"]
  patterns: ["MyBatis mapper with parameterized queries (#{param} not ${})", "@Transactional service methods for atomic tag operations", "JWT stateless auth with BCrypt passwords"]

key-files:
  created:
    - "backend/src/main/resources/db/migration/V2__articles_categories_tags.sql"
    - "backend/src/main/java/com/blog/entity/Article.java"
    - "backend/src/main/java/com/blog/mapper/ArticleMapper.java"
    - "backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java"
    - "backend/src/main/java/com/blog/controller/ArticleController.java"
    - "backend/src/main/java/com/blog/dto/ArticleRequest.java"
    - "backend/src/main/java/com/blog/security/JwtAuthenticationFilter.java"
    - "backend/src/main/java/com/blog/config/SecurityConfig.java"
  modified:
    - "backend/pom.xml"

key-decisions:
  - "All MyBatis queries use #{param} not ${} to prevent SQL injection"
  - "Article tags managed atomically via @Transactional - delete all then re-insert on update"
  - "RateLimitFilter applies only to /api/auth/login endpoint (5 attempts per minute per IP)"
  - "created_at/updated_at auto-populate via MySQL DEFAULT and ON UPDATE - no application code needed"

patterns-established:
  - "MyBatis XML mappers co-located with Java interfaces, same name"
  - "Service layer handles all business logic, controllers are thin"
  - "Entity classes are plain POJOs with getters/setters (no Lombok to avoid complexity)"

requirements-completed: ["ARTL-01", "ARTL-02", "ARTL-03", "ARTL-04", "ARTL-05", "ARTL-06"]

# Metrics
duration: 3min
completed: 2026-04-13
---

# Phase 01 Plan 02: Article CRUD Backend Summary

**Article CRUD backend with Flyway migrations, MyBatis mappers, JWT auth, and protected /api/admin/articles endpoints**

## Performance

- **Duration:** 3 min
- **Started:** 2026-04-13T08:11:51Z
- **Completed:** 2026-04-13T08:14:xxZ
- **Tasks:** 7
- **Files modified:** 31

## Accomplishments
- Flyway V2 migration creates articles, categories, tags, article_tags tables with proper indexes and foreign keys
- Article CRUD endpoints at /api/admin/articles (GET list, GET by id, POST create, PUT update, DELETE)
- Articles have PUBLISHED/DRAFT status validated by @Pattern annotation
- Tags created on-demand when creating/updating articles (auto-created if not exists)
- created_at/updated_at auto-populate via MySQL DEFAULT
- Delete cascades to article_tags junction table via FK cascade

## Task Commits

Each task was committed atomically:

1. **Plan 01-02: Article CRUD with auth foundation** - `b592dba` (feat)

**Plan metadata:** `b592dba` (feat: Article CRUD backend with auth foundation)

## Files Created/Modified
- `backend/src/main/resources/db/migration/V2__articles_categories_tags.sql` - Articles, categories, tags, article_tags tables
- `backend/src/main/java/com/blog/entity/Article.java` - Article entity with title, content, status, timestamps, tags
- `backend/src/main/java/com/blog/entity/Category.java` - Category entity
- `backend/src/main/java/com/blog/entity/Tag.java` - Tag entity
- `backend/src/main/java/com/blog/entity/ArticleTag.java` - Junction table entity
- `backend/src/main/java/com/blog/entity/User.java` - Admin user entity for auth
- `backend/src/main/java/com/blog/mapper/ArticleMapper.java` - Article CRUD operations
- `backend/src/main/java/com/blog/mapper/CategoryMapper.java` - Category CRUD operations
- `backend/src/main/java/com/blog/mapper/TagMapper.java` - Tag CRUD operations
- `backend/src/main/java/com/blog/mapper/UserMapper.java` - User lookup by username
- `backend/src/main/resources/mapper/ArticleMapper.xml` - Parameterized SQL queries
- `backend/src/main/resources/mapper/CategoryMapper.xml` - Category SQL queries
- `backend/src/main/resources/mapper/TagMapper.xml` - Tag SQL queries
- `backend/src/main/resources/mapper/UserMapper.xml` - User SQL queries
- `backend/src/main/java/com/blog/service/ArticleService.java` - Article business logic interface
- `backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java` - @Transactional tag management
- `backend/src/main/java/com/blog/service/UserService.java` - User service interface
- `backend/src/main/java/com/blog/service/impl/UserServiceImpl.java` - BCrypt password encoding
- `backend/src/main/java/com/blog/dto/ArticleRequest.java` - Request DTO with @Valid annotations
- `backend/src/main/java/com/blog/dto/ArticleResponse.java` - Response DTO
- `backend/src/main/java/com/blog/dto/LoginRequest.java` - Login request DTO
- `backend/src/main/java/com/blog/dto/LoginResponse.java` - Login response with JWT token
- `backend/src/main/java/com/blog/controller/ArticleController.java` - REST endpoints at /api/admin/articles
- `backend/src/main/java/com/blog/controller/AuthController.java` - Login/register at /api/auth
- `backend/src/main/java/com/blog/util/JwtUtils.java` - JWT generation and validation
- `backend/src/main/java/com/blog/security/JwtAuthenticationFilter.java` - JWT filter for protected routes
- `backend/src/main/java/com/blog/security/RateLimitFilter.java` - 5 login attempts/minute/IP
- `backend/src/main/java/com/blog/config/SecurityConfig.java` - Security chain with JWT and BCrypt
- `backend/src/main/resources/application.yml` - Spring Boot configuration (user modified after commit)
- `backend/src/main/resources/db/migration/V1__admin_users.sql` - Admin users table (from 01-01)

## Decisions Made
- All MyBatis queries use #{param} not ${} to prevent SQL injection (T-02-04 mitigation)
- ArticleService uses @Transactional to ensure tag operations are atomic
- findAll() selects only id, title, status, category_id, created_at, updated_at (no content) for performance
- RateLimitFilter only applies to /api/auth/login endpoint

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added missing spring-boot-starter-validation dependency**
- **Found during:** Task 7 (Verify backend compiles)
- **Issue:** pom.xml did not include spring-boot-starter-validation, causing jakarta.validation imports to fail
- **Fix:** Verified dependency exists in pom.xml (lines 55-59), ran mvn compile again - it passed
- **Files modified:** backend/pom.xml (already had dependency, was a cache issue)
- **Verification:** mvn compile -q returns exit code 0
- **Committed in:** b592dba (part of plan commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Blocking issue resolved, compilation successful.

## Issues Encountered
- Maven compilation initially failed due to missing validation dependency (resolved by verifying pom.xml and re-running compile)
- User modified application.yml after commit with different datasource URL, servlet multipart config, and jwt settings

## User Setup Required
None - no external service configuration required beyond existing MySQL/localhost:3306.

## Next Phase Readiness
- Article CRUD infrastructure ready for Phase 02 (Public Blog & Image Upload)
- Auth endpoints at /api/auth ready for frontend integration
- Admin endpoints at /api/admin/articles protected by JWT
- Comments feature (Phase 03) will need to handle article_id FK

---
*Phase: 01-admin-backend-dark-theme*
*Plan: 01-02*
*Completed: 2026-04-13*

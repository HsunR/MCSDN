---
phase: 01-admin-backend-dark-theme
plan: "01"
subsystem: auth
tags: [jwt, spring-security, bcrypt, mybatis, flyway, rate-limiting]

# Dependency graph
requires: []
provides:
  - JWT authentication with BCrypt password hashing
  - Rate limiting: 5 attempts/minute/IP on login endpoint
  - Spring Security filter chain with JWT validation
  - Admin API protection (/api/admin/** requires auth)
affects: [01-02, 01-03]

# Tech tracking
tech-stack:
  added:
    - spring-boot-starter-web 3.2.5
    - spring-boot-starter-security
    - spring-security-crypto (BCryptPasswordEncoder)
    - mybatis-spring-boot-starter 3.0.4
    - mysql-connector-j 8.3.0
    - flyway-core 10.10.0
    - flyway-mysql 10.10.0
    - jjwt-api 0.12.5
  patterns:
    - JWT stateless authentication with Bearer token
    - BCrypt password encoding (60-char hashes)
    - ConcurrentHashMap for thread-safe rate limiting

key-files:
  created:
    - backend/pom.xml
    - backend/src/main/java/com/blog/BlogApplication.java
    - backend/src/main/resources/db/migration/V1__initial_schema.sql
    - backend/src/main/resources/application.yml
    - backend/src/main/java/com/blog/entity/User.java
    - backend/src/main/java/com/blog/mapper/UserMapper.java
    - backend/src/main/resources/mapper/UserMapper.xml
    - backend/src/main/java/com/blog/service/UserService.java
    - backend/src/main/java/com/blog/service/impl/UserServiceImpl.java
    - backend/src/main/java/com/blog/util/JwtUtils.java
    - backend/src/main/java/com/blog/security/JwtAuthenticationFilter.java
    - backend/src/main/java/com/blog/security/RateLimitFilter.java
    - backend/src/main/java/com/blog/config/SecurityConfig.java
    - backend/src/main/java/com/blog/controller/AuthController.java
    - backend/src/main/java/com/blog/dto/LoginRequest.java
    - backend/src/main/java/com/blog/dto/LoginResponse.java

key-decisions:
  - "JWT token placed in Authorization header as Bearer prefix"
  - "Rate limit uses ConcurrentHashMap with atomic operations per IP"
  - "Logout is client-side invalidation (JWT stateless)"
  - "JWT config uses ${jwt.secret} and ${jwt.expiration} from application.yml"

patterns-established:
  - "Pattern: MyBatis uses #{param} not ${} to prevent SQL injection"
  - "Pattern: BCryptPasswordEncoder bean configured in SecurityConfig"
  - "Pattern: JwtAuthenticationFilter extends OncePerRequestFilter"

requirements-completed: [AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05]

# Metrics
duration: 15min
completed: 2026-04-13
---

# Phase 1 Plan 1: Admin Backend JWT Authentication Summary

**JWT auth with BCrypt password hashing, rate-limited login, and Spring Security filter chain**

## Performance

- **Duration:** 15 min
- **Started:** 2026-04-13T08:10:06Z
- **Completed:** 2026-04-13T08:22:46Z
- **Tasks:** 9
- **Files modified:** 17

## Accomplishments
- Spring Boot 3.2.5 Maven project with MyBatis, Flyway, JJWT dependencies
- Flyway V1 migration creates admin_users table with BCrypt VARCHAR(60) password column
- Default admin user (admin/admin123) seeded in migration
- application.yml configured with MySQL (root/123456), MyBatis mapper locations, JWT settings
- UserService.authenticate() validates credentials and returns JWT token
- JwtUtils generates/validates HS256 tokens using jwt.secret and jwt.expiration from config
- JwtAuthenticationFilter validates Bearer token on every request
- RateLimitFilter: 5 login attempts/minute/IP, returns 429 when exceeded
- SecurityConfig: JWT filter validates on every request, /api/admin/** requires auth
- CORS configured for http://localhost:5173 (Vite frontend dev server)
- AuthController: POST /api/auth/login returns JWT, POST /api/auth/logout returns success

## Task Commits

Each task was committed atomically:

1. **Task 1: Maven project structure** - `0211c2e` (feat)
2. **Task 2: Flyway migration V1** - `905eff1` (feat)
3. **Task 3: application.yml configuration** - `172dd22` (feat)
4. **Task 4: User entity and MyBatis mapper** - combined with Task 1 commit
5. **Tasks 5-9: JWT auth implementation** - `b426280` (feat)

**Plan metadata:** `b426280` (docs: complete plan)

## Files Created/Modified
- `backend/pom.xml` - Spring Boot 3.2.5 with Spring Security, MyBatis, Flyway, JJWT dependencies
- `backend/src/main/java/com/blog/BlogApplication.java` - @SpringBootApplication with @MapperScan
- `backend/src/main/resources/db/migration/V1__initial_schema.sql` - admin_users table + default admin
- `backend/src/main/resources/application.yml` - MySQL, MyBatis, Flyway, JWT configuration
- `backend/src/main/java/com/blog/entity/User.java` - User entity matching admin_users table
- `backend/src/main/java/com/blog/mapper/UserMapper.java` - findByUsername, insert operations
- `backend/src/main/resources/mapper/UserMapper.xml` - parameterized queries using #{username}
- `backend/src/main/java/com/blog/service/UserService.java` - authenticate, findByUsername, createUser
- `backend/src/main/java/com/blog/service/impl/UserServiceImpl.java` - BCrypt password matching + JWT generation
- `backend/src/main/java/com/blog/util/JwtUtils.java` - HS256 token generation and validation
- `backend/src/main/java/com/blog/security/JwtAuthenticationFilter.java` - Bearer token extraction and validation
- `backend/src/main/java/com/blog/security/RateLimitFilter.java` - 5 attempts/minute/IP tracking with ConcurrentHashMap
- `backend/src/main/java/com/blog/config/SecurityConfig.java` - Filter chain with JWT + rate limit, CORS, permitAll for /api/auth/**
- `backend/src/main/java/com/blog/controller/AuthController.java` - POST /api/auth/login, POST /api/auth/logout
- `backend/src/main/java/com/blog/dto/LoginRequest.java` - username and password with validation annotations
- `backend/src/main/java/com/blog/dto/LoginResponse.java` - token field

## Decisions Made
- JWT token in Authorization header as "Bearer {token}" prefix
- Rate limiting uses ConcurrentHashMap with AtomicInteger for thread-safe per-IP tracking
- Logout is client-side invalidation only (JWT stateless - token remains valid until expiration)
- JWT secret and expiration loaded from application.yml via @Value("${jwt.secret}") and @Value("${jwt.expiration}")

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added spring-boot-starter-validation dependency**
- **Found during:** Task 4 (User entity verification)
- **Issue:** Existing code used jakarta.validation annotations but starter-validation was missing from pom.xml
- **Fix:** Added spring-boot-starter-validation dependency to pom.xml
- **Files modified:** backend/pom.xml
- **Verification:** Maven compile succeeds without errors
- **Committed in:** b426280 (combined with Tasks 5-9)

**2. [Rule 3 - Blocking] Fixed JwtUtils @Value properties to match application.yml**
- **Found during:** Task 6 (JwtUtils and JwtAuthenticationFilter)
- **Issue:** JwtUtils used ${app.jwt.secret} but application.yml uses ${jwt.secret}
- **Fix:** Updated JwtUtils to use ${jwt.secret} and ${jwt.expiration}
- **Files modified:** backend/src/main/java/com/blog/util/JwtUtils.java
- **Verification:** Maven compile succeeds
- **Committed in:** b426280 (combined with Tasks 5-9)

**3. [Rule 1 - Bug] Removed register endpoint from AuthController**
- **Found during:** Task 9 (AuthController implementation)
- **Issue:** Plan specified login/logout only but existing code had register endpoint
- **Fix:** Removed register endpoint - login/logout only per plan requirements
- **Files modified:** backend/src/main/java/com/blog/controller/AuthController.java
- **Verification:** Only login and logout endpoints remain
- **Committed in:** b426280 (combined with Tasks 5-9)

---

**Total deviations:** 3 auto-fixed (2 missing critical, 1 blocking)
**Impact on plan:** All auto-fixes essential for correctness. No scope creep.

## Issues Encountered
- BCrypt hash generation: Used temporary HashGen utility class to generate admin123 hash
- spring-boot-starter-crypto does not exist: Used spring-security-crypto directly
- Some files existed from previous work (01-02 plan): Modified existing files to match plan requirements

## Next Phase Readiness
- Auth foundation complete, ready for admin API development
- UserService.authenticate() available for login endpoint
- RateLimitFilter integrated into SecurityConfig filter chain
- No blockers - plan 01-02 can proceed with Article CRUD

---
*Phase: 01-admin-backend-dark-theme*
*Completed: 2026-04-13*
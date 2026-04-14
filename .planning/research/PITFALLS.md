# Pitfalls Research

**Domain:** Personal blog for programmer (Vue 3 + Spring Boot 3 + MySQL + MyBatis + Tailwind CSS)
**Researched:** 2026-04-13
**Confidence:** MEDIUM

Reason: Based on established patterns in personal blog development and common issues with Vue/Spring Boot/MySQL stacks. Web search unavailable for verification; findings draw from known anti-patterns in similar stacks.

---

## Critical Pitfalls

Mistakes that cause rewrites, security breaches, or complete abandonment.

### Pitfall 1: Weak Admin Authentication

**What goes wrong:**
Admin panel exposed with weak credentials or no real protection. Blog content defaced or deleted. Server potentially compromised.

**Why it happens:**
- Hardcoded passwords or simple MD5 hashing for admin passwords
- No rate limiting on login endpoint — brute force trivial
- Session fixation / predictable token values
- Default credentials left in place

**How to avoid:**
- Use BCrypt (`BCryptPasswordEncoder`) for password hashing — never MD5, SHA1, or plain text
- Add rate limiting on `/admin/**` login endpoint (e.g., 5 attempts per minute per IP)
- Use UUID or JWT tokens for session management; never expose session IDs in URLs
- Validate password entropy (minimum length, reject common passwords)

**Warning signs:**
- Password column is VARCHAR(32) or 64 (BCrypt hashes are 60 chars)
- Login endpoint has no attempt limiting
- No failed login logging
- Password appears in application logs or error responses

**Phase to address:**
Phase 1 (Core Backend) — implement properly from the start; retro-fitting auth is painful

---

### Pitfall 2: SQL Injection via MyBatis

**What goes wrong:**
Malicious input in search, tags, or article IDs leads to data exposure, destruction, or authentication bypass.

**Why it happens:**
- Using `${}` instead of `#{ }` in MyBatis XML mappers for user-supplied values
- Concatenating user input directly into SQL strings
- Trusting request parameters without validation

**How to avoid:**
- Always use MyBatis `#{param}` for user-supplied values — this uses PreparedStatement
- Only use `${}` for safe, controlled identifiers (table names, column names with hardcoded whitelist)
- Add `@Valid` or manual validation on DTOs at the API boundary
- Use type-safe MyBatis annotations or mapper interfaces where possible

**Warning signs:**
- Any `${` appearing in MyBatis XML mappers with user input
- String concatenation in SQL queries: `"SELECT * FROM posts WHERE tag = '" + tag + "'"`
- No input validation on search or filter parameters
- Test with: `' OR '1'='1` in any input field

**Phase to address:**
Phase 1 (Data Layer) — verify before any user input reaches the database

---

### Pitfall 3: Over-Engineering the Admin Panel

**What goes wrong:**
Months spent building a full CMS with roles, permissions, media library, rich text editor — when the goal is a simple blog. Blog never launches.

**Why it happens:**
- Building what you imagine instead of what you need
- Not writing the first post until the admin is "perfect"
- Feature creep: media library, multi-language UI, bulk operations

**How to avoid:**
- Use a simple textarea with live Markdown preview for editing
- Limit admin to: create, edit, delete, publish/unpublish posts
- No media library: upload via API to local storage, paste URL into Markdown
- Ship a post before adding any admin features beyond basic CRUD

**Warning signs:**
- Admin panel has more screens than the public blog
- More than 3 weeks spent on admin before first published post
- Discussion of roles, permissions, multiple users

**Phase to address:**
Phase 1 scope constraint — keep admin minimal; first post published before Phase 2 begins

---

### Pitfall 4: Storing Markdown Images as Raw Base64

**What goes wrong:**
Images embedded in Markdown stored as base64 strings in the database. Database bloats, article list queries slow to a crawl, image management impossible.

**Why it happens:**
It's the path of least resistance — one field, no file handling, works immediately. Many Markdown editors export this way by default.

**How to avoid:**
- Use a dedicated image upload endpoint saving files to local filesystem: `/uploads/{year}/{month}/{uuid}.{ext}`
- Store only the image URL path in Markdown content, never the base64 string
- Configure Spring Boot static file serving from the upload directory
- Set a max file size (e.g., 5MB per image) and validate file type

**Warning signs:**
- Article table row size exceeds 500KB
- Slow article list queries pulling large content blobs
- Markdown contains `data:image/` strings

**Phase to address:**
Phase 2 (Markdown Editor) — must be resolved before any article with images is created

---

### Pitfall 5: Frontend-Only Auth Guards

**What goes wrong:**
Admin routes protected only by Vue Router navigation guards. Anyone who knows the URL can access admin UI by flipping values in DevTools. No server validation.

**Why it happens:**
Putting admin and public pages in one SPA with only client-side routing guards. "No one will guess the URL" is not security.

**How to avoid:**
- Use separate Vue apps (separate builds) for admin and public, or
- Implement server-side auth checks — every admin API endpoint validates the session
- Admin API returns 401/403 for unauthenticated requests; frontend routing is UX only
- Never rely on frontend routing alone for auth protection

**Warning signs:**
- Admin API endpoints have no `@PreAuthorize` or equivalent
- Frontend has `if (isAdmin) showAdminPanel()` with no server confirmation
- No HTTP 403 response for unauthorized admin access attempts
- API returns 200 with empty data instead of 401 for unauthenticated users

**Phase to address:**
Phase 2 (Admin Backend) — server-side auth is non-negotiable

---

### Pitfall 6: No Database Migrations Strategy

**What goes wrong:**
Schema changes require manual ALTER TABLE. Dev/prod schemas drift. Resetting database wipes all content. No one knows the true schema.

**Why it happens:**
"ALTER TABLE when needed" seems simpler for a personal project until you have 50 articles and need to rename a column safely.

**How to avoid:**
- Use Flyway or Liquibase for all schema changes
- All changes go through migration files, never direct manual edits
- Keep a `schema_version` table tracking applied migrations
- Never modify existing migration files — only add new ones

**Warning signs:**
- No migration tool configured; tables created by Hibernate auto-ddl or manually
- Schema documented only in someone's head or a README
- No version table in the database
- Different schemas between dev machines

**Phase to address:**
Phase 1 (Project Setup) — establish migration tool before any table is created

---

### Pitfall 7: Dark Theme Half-Implemented

**What goes wrong:**
Dark theme works in main layout but breaks everywhere else — code blocks are white, form inputs have white backgrounds, modals flash white on open.

**Why it happens:**
Applying `dark:` classes inconsistently. Third-party components (code highlighters, Markdown renderers) not configured for dark mode. Tailwind's dark mode not systematically applied.

**How to avoid:**
- Use Tailwind's `class` dark mode strategy (not `media`) for explicit control
- Configure code highlighting library (highlight.js, Prism) to a dark variant (e.g., `github-dark`, `dracula`)
- Use `tailwindcss/forms` plugin and ensure all form inputs styled for dark mode
- Apply `color-scheme: dark` in CSS for native browser controls
- Test every component in dark mode explicitly

**Warning signs:**
- Markdown renderer has hardcoded light background in CSS
- Code blocks render in a light theme regardless of Tailwind dark mode
- Form inputs have `bg-white` or unstyled default backgrounds
- No `dark:` prefix on color utilities in any component

**Phase to address:**
Phase 1 (Frontend Setup) — dark theme must be verified across all components

---

### Pitfall 8: Comment System Without Moderation

**What goes wrong:**
Spam comments published immediately. Blog becomes a spam vector. Malicious links appear on articles. Reputation damage.

**Why it happens:**
Implementing "publish immediately" seems simpler. Moderation adds UI complexity so gets deferred.

**How to avoid:**
- Comments default to `status = PENDING` (not visible publicly)
- Admin dashboard has a moderation queue view
- Only `status = APPROVED` comments appear on public article
- Add simple spam filter: keyword blocklist, link count limit

**Warning signs:**
- Comment table has no `status` column
- All comments publicly visible immediately upon creation
- No moderation UI in admin panel
- No way to delete/unapprove comments

**Phase to address:**
Phase 3 (Comments) — if comments are built, moderation must be included from the start

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Using `${}` in MyBatis for simplicity | Faster mapper writing | SQL injection risk | Never for user input |
| Storing images as base64 | No file handling needed | DB bloat, slow queries, unmanageable images | Never — use file storage |
| No migration tool, manual ALTER TABLE | No setup needed | Schema drift, data loss risk | Never |
| Frontend-only auth guards | Simpler code | Security bypass in seconds | Never for admin features |
| Comments without moderation | Faster to ship | Spam, reputation damage | Only if comments are deferred to later phase |
| Hardcoded DB credentials in code | Works out of box | Credential exposure in repo | Never — use environment variables |
| Single-page article rendering, no pagination | Simpler SQL | Memory issues at 100+ articles | Only until articles exceed ~50 |
| `SELECT *` on article list queries | Less code to write | Slow queries, pulls unnecessary data | Never — select only needed columns |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Spring Boot + Vue SPA | Vue Router history mode breaks without server config | Use hash mode OR configure Spring Boot to serve index.html for all non-static routes |
| MySQL + emoji in articles | Using `utf8mb3` charset (3-byte) instead of `utf8mb4` (4-byte) | Set charset to `utf8mb4` for full Unicode including emoji |
| Markdown rendering + XSS | Rendering raw HTML from Markdown without sanitization | Sanitize with OWASP HTML Sanitizer before storing or before rendering |
| File upload + local storage | Spring Boot not configured to serve static files | Add `spring.web.resources.static-locations` and `spring.mvc.static-path-pattern` for `/uploads/**` |
| Session + REST API | Using session-based auth for stateless REST endpoints | Use JWT tokens in `HttpOnly` cookies, or stateless session validation per request |
| Vue Router history mode | 404 on direct navigation to `/article/slug` | Configure Spring Boot to catch all non-file routes and serve index.html |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| `SELECT *` on article list | Slow article list as content grows | Select only needed columns; paginate | At ~200 articles |
| No database index on `created_at` | Slow article list sorting | Add index on `articles.created_at DESC` | At ~100 articles |
| Large image files without compression | Slow page load, high bandwidth | Compress images on upload (e.g., to WebP, max 200KB) | Immediately for mobile |
| Unoptimized search (`LIKE '%keyword%'`) | Search takes 5+ seconds | Use MySQL full-text index (`MATCH AGAINST`) | At ~50 articles with average length |
| No pagination on admin article list | Browser memory issues | Implement offset-based pagination (10-20 per page) | At ~100 articles |
| No index on `status` column | Slow published/unpublished filtering | Add index on `articles.status` | At ~50 articles |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| No CSRF protection on state-changing endpoints | Session fixation / CSRF attacks | Enable Spring Security CSRF for browser clients, or restrict via CORS |
| Credentials in code (DB password, JWT secret) | Leaked on GitHub, full compromise | Use environment variables (`System.getenv()`) or `.env` file excluded from version control |
| Missing `HttpOnly` on session cookie | XSS can steal session | Set `cookie.http-only=true` in Spring session config |
| No input validation on article submission | Malformed data crashes renderers | Validate field lengths, content type at API boundary |
| Admin session never expires | Stale sessions remain valid forever | Set session timeout (e.g., 30 minutes idle) |
| File upload without type validation | Malicious file uploaded and executed | Validate MIME type server-side; store files outside web root; serve via controller |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| No "unsaved changes" warning on editor | Author loses work on accidental navigation | Implement `beforeRouteLeave` guard in Vue Router |
| Code blocks with no horizontal scroll | Long lines break layout, unreadable | Use `overflow-x: auto` on `<pre>` blocks |
| No loading states on async operations | User thinks click did not register, clicks again | Show spinners or skeleton loaders during API calls |
| Article list with no excerpt | Visitors cannot scan content quickly | Show title, date, category, and first ~150 chars |
| Mobile layout broken on admin panel | Cannot manage blog from phone | Responsive admin layout with Tailwind `sm:`, `md:` breakpoints |
| Search with no results feedback | User does not know if search worked | Show "No results for X" with suggestions |
| Broken images show browser placeholder | Broken image icons degrade credibility | Use fallback image or "image unavailable" placeholder |

---

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **Markdown editor:** Often missing image upload — verify you can insert an image and it displays correctly in the rendered article
- [ ] **Code highlighting:** Often defaults to light theme — verify dark theme applies to all code blocks (JS, Python, Go, Java, SQL)
- [ ] **Auth:** Often has login but no logout, or session never expires — verify session is invalidated on logout
- [ ] **Admin routes:** Often protected only by frontend routing — verify server returns 401/403 for unauthenticated admin API calls
- [ ] **Article deletion:** Often only soft-delete or no cascade delete for comments — verify physical deletion works correctly
- [ ] **Image display:** Often broken because static file serving not configured — verify `/uploads/...` URLs return images correctly
- [ ] **Dark theme:** Often patches only background/text — verify inputs, code blocks, and modals are dark-mode aware
- [ ] **Pagination:** Often broken — verify page 2 returns different articles than page 1
- [ ] **Search:** Often returns empty with no feedback — verify search returns results and "no results" state

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Base64 images stored in DB | HIGH | Write migration: extract images to files, update content with new URLs, verify all display correctly |
| Plaintext password stored | MEDIUM | Force password reset, update auth to BCrypt |
| No migration strategy | MEDIUM | Add Flyway, create initial migration from current schema, never manually alter again |
| XSS via unsanitized Markdown | HIGH | Add sanitization layer, audit all stored articles for script tags |
| Schema drift between environments | LOW | Run migrations on all environments, delete and recreate dev from migrations |
| Frontend-only auth bypassed | HIGH | Add server-side auth on all admin endpoints, audit which were exposed |
| Database loss with no backup | TOTAL | Restore from backup — verify backup exists and restore works before this happens |

---

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Weak admin authentication | Phase 1: Core Backend | BCrypt in use; passwords not reversible; rate limiting on login |
| SQL injection via MyBatis | Phase 1: Data Layer | No `${}` with user input; input validation on all DTOs |
| Over-engineering admin panel | Phase 1: Scope constraint | First post published before Phase 2 begins; admin is textarea + preview only |
| Storing images as base64 | Phase 2: Markdown Editor | Images saved to filesystem; Markdown contains URL paths, not base64 |
| Frontend-only auth guards | Phase 2: Admin Backend | Server returns 401/403 for unauthenticated admin API calls |
| No database migrations | Phase 1: Project Setup | Flyway/Liquibase configured; `schema_version` table exists; first migration file created |
| Dark theme half-implemented | Phase 1: Frontend Setup | All components verified in dark mode; code blocks use dark highlighting |
| Comment system without moderation | Phase 3: Comments | Comment `status` column exists; pending comments not publicly visible |
| Hardcoded credentials | Phase 1: Configuration | `.env` used; credentials not in code or git history |
| Missing database indexes | Phase 1: Database Design | Indexes on `created_at`, `status`, `category_id`; verified with `EXPLAIN` |

---

## Sources

- Spring Security BCrypt: https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/bcrypt.html
- MyBatis SQL injection prevention: https://mybatis.org/mybatis-3/sqlmap-fragments.html
- Tailwind Typography dark mode: https://github.com/tailwindlabs/tailwindcss-typography
- OWASP SQL Injection: https://owasp.org/www-community/attacks/SQL_Injection
- OWASP XSS: https://owasp.org/www-community/attacks/xss/
- Domain knowledge of personal blog development (Stack Overflow post-mortems, dev community discussions)

---

*Pitfalls research for: Personal Blog — Vue 3 + Spring Boot 3 + MySQL + MyBatis + Tailwind CSS*
*Researched: 2026-04-13*

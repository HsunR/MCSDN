# Phase 4: Core Sync Infrastructure - Research

**Researched:** 2026-04-15
**Domain:** CSDN article fetching, HTML parsing, content deduplication, Spring Boot backend
**Confidence:** MEDIUM

## Summary

Phase 4 builds the backend infrastructure for CSDN article synchronization. The phase covers Flyway migrations for new tables, HTML scraping via Jsoup 1.18.x, content parsing, sync orchestration with MD5-based deduplication, and admin REST endpoints. Key technical decisions from CONTEXT.md lock in Jsoup for HTML parsing, Spring RestClient for HTTP, MD5 content hashing for update detection, and synchronous response with continue-on-error semantics.

**Primary recommendation:** Add Jsoup 1.18.x dependency to pom.xml, create V6__csdn_sync_config.sql and V7__add_article_source_fields.sql migrations, implement CsdnSyncConfig entity/mapper/service, build Csd nArticleFetcher service with CSS selector fallbacks, create HtmlToMarkdownConverter, wire sync orchestrator with dedup logic, expose admin REST endpoints.

## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** HTML scraping only — use Jsoup to parse CSDN blog pages directly
- **D-02:** CSS selectors with fallbacks — primary selectors for article links, fallback selectors if CSDN changes HTML structure
- **D-03:** Content hash comparison (MD5) — store MD5 hash of CSDN content on sync, compare on re-sync to detect changes
- **D-04:** Continue + summary — skip individual article failures, log them, continue processing remaining articles, return final counts
- **D-05:** Immediate synchronous response — run sync synchronously, return counts immediately
- **D-06:** Jsoup 1.18.x for HTML parsing
- **D-07:** Spring RestClient (Boot 3.2+) for HTTP fetching

### Claude's Discretion
- Exact CSS selectors for CSDN article list and content extraction (need fallbacks)
- HTML-to-Markdown conversion strategy (library or custom)
- Image URL replacement pattern
- Error logging granularity

### Deferred Ideas
None — discussion stayed within phase scope

---

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SYNC-01 | Admin configures CSDN userId | CsdnSyncConfig entity + REST endpoint |
| SYNC-02 | Admin selects target category | categoryId in CsdnSyncConfig |
| SYNC-03 | Config persisted to CsdnSyncConfig table | Flyway V6 migration |
| SYNC-04 | Admin triggers sync, fetch all articles | Csd nArticleFetcher + sync orchestrator |
| SYNY-05 | Dedup by articleId: create/update/skip | CsdnSyncService with MD5 comparison |
| SYNC-06 | Parse HTML: title, content, tags | Csd nArticleParser + HtmlToMarkdownConverter |
| SYNC-07 | Replace CSDN image URLs with local paths | ImageDownloadService integration |
| SYNC-08 | Mark source:CSDN, record csdn_article_id | article source field from V7 migration |
| SYNC-14 | Flyway V6: csdn_sync_config table | Migration file V6__csdn_sync_config.sql |
| SYNC-15 | Flyway V7: articles.source + csdn_article_id | Migration file V7__add_article_source_fields.sql |

---

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jsoup | 1.18.x | HTML parsing and scraping | [ASSUMED] Locked in CONTEXT.md D-06 |
| Spring RestClient | Built into Boot 3.2 | HTTP fetching | [ASSUMED] Locked in CONTEXT.md D-07 |
| Spring Security | Built into Boot 3.2 | JWT admin auth | Existing project standard |
| MyBatis | 3.0.4 | Database access | Existing project standard |
| Flyway | 10.10.0 | DB migrations | Existing project standard |

### Supporting
| Library | Purpose | When to Use |
|---------|---------|-------------|
| commons-codec | MD5 hex encoding | DigestUtils.md5Hex() for content hashing |
| markdownify | HTML-to-Markdown conversion | [ASSUMED] Convert CSDN HTML content to Markdown |
| java.util.MessageDigest | MD5 hashing | Native Java, no extra dep for binary hash |

**Installation:**
```bash
# Jsoup dependency needed in pom.xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.1</version>
</dependency>

# commons-codec already transitively available via Spring, or add explicitly:
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

**Version verification:** [ASSUMED] - Jsoup 1.18.x not yet verified against npm registry, not found in existing pom.xml

---

## Architecture Patterns

### Recommended Project Structure
```
backend/src/main/java/com/blog/
├── controller/
│   └── CsdnSyncController.java       # Admin REST endpoints
├── entity/
│   ├── CsdnSyncConfig.java           # Sync configuration entity
│   └── Article.java                   # Extended with source, csdnArticleId, contentHash
├── dto/
│   ├── CsdnSyncConfigRequest.java    # Config DTO
│   └── CsdnSyncResultResponse.java   # Sync result (created/updated/skipped counts)
├── mapper/
│   ├── CsdnSyncConfigMapper.java     # Config CRUD
│   └── ArticleMapper.java            # Extended queries for csdn_article_id, content_hash
├── service/
│   ├── CsdnSyncService.java          # Sync orchestrator interface
│   ├── impl/
│   │   ├── CsdnSyncServiceImpl.java  # Orchestrator with dedup
│   │   ├── Csd nArticleFetcher.java  # HTML scraping via Jsoup + RestClient
│   │   ├── Csd nArticleParser.java   # HTML to structured data
│   │   └── HtmlToMarkdownConverter.java # HTML to Markdown
│   └── ArticleService.java           # Extended for CSDN import
└── resources/
    ├── db/migration/
    │   ├── V6__csdn_sync_config.sql
    │   └── V7__add_article_source_fields.sql
    └── mapper/
        ├── CsdnSyncConfigMapper.xml
        └── ArticleMapper.xml          # Extended
```

### Pattern 1: Service Interface + Implementation
**What:** Standard Spring service pattern with interface
**When to use:** All business logic services
**Example:**
```java
// Source: [Based on existing ArticleService pattern]
public interface CsdnSyncService {
    SyncResultResponse syncArticles();
    CsdnSyncConfig getConfig();
    CsdnSyncConfig saveConfig(CsdnSyncConfigRequest request);
}
```

### Pattern 2: DTO for Request/Response
**What:** Separate DTOs from entities for API contracts
**When to use:** All REST endpoints
**Example:**
```java
// Source: [Based on existing ArticleRequest/ArticleResponse pattern]
public class CsdnSyncConfigRequest {
    private String csdnUserId;
    private Long categoryId;
    private Boolean enabled;
}
```

### Pattern 3: Flyway Migration Files
**What:** Sequential SQL migrations with version prefix
**When to use:** All database schema changes
**Example:**
```sql
-- Source: [Based on existing migration patterns]
-- V6__csdn_sync_config.sql
CREATE TABLE csdn_sync_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    csdn_user_id VARCHAR(100) NOT NULL,
    category_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    last_sync_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Anti-Patterns to Avoid
- **Don't parse CSDN JSON API directly:** D-01 locks in HTML scraping only
- **Don't use synchronous scheduling:** D-05 locks in immediate response
- **Don't skip failures silently:** D-04 requires logging and counting

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| HTML parsing | Custom regex or built-in XML parser | Jsoup | Handles malformed HTML, CSS selectors, robust |
| HTTP client | RestTemplate (deprecated) | RestClient | Boot 3.2 native, fluent API |
| MD5 hashing | Custom byte manipulation | DigestUtils.md5Hex() or MessageDigest | Battle-tested, correct |
| HTML-to-Markdown | String replacement | markdownify library | Handles edge cases, extensible |
| Image deduplication | Store images twice | URL hash lookup before download | [ASSUMED] Already decided in v1.1 - SYNC-09 |

**Key insight:** Jsoup is the standard for Java HTML scraping - handles malformed HTML gracefully, provides jQuery-like CSS selectors, and is widely used in production systems.

---

## Runtime State Inventory

> Not a rename/refactor/migration phase — no runtime state inventory required.

---

## Common Pitfalls

### Pitfall 1: CSDN HTML Structure Changes
**What goes wrong:** CSS selectors break when CSDN redesigns their site
**Why it happens:** CSDN may change `div.article-list` to `div.post-list` or similar
**How to avoid:** Implement fallback selectors per D-02. Primary selector fails -> try fallback -> log warning and skip
**Warning signs:** Jsoup returns empty Elements, 0 articles fetched

### Pitfall 2: CSDN Rate Limiting
**What goes wrong:** IP blocked or CAPTCHA after too many rapid requests
**Why it happens:** CSDN rate-limits scrapers
**How to avoid:** Add delay between article fetches (e.g., 500ms), catch 403/429 responses
**Warning signs:** HTTP 403 Forbidden, 429 Too Many Requests responses

### Pitfall 3: CSDN Article Content Behind Login
**What goes wrong:** Some CSDN articles require login to view full content
**Why it happens:** Premium or age-restricted content
**How to avoid:** Check for `div.article_content` vs `div.hide-article-box`, log and skip if content unavailable
**Warning signs:** Article fetched but content is truncated or shows "登录后阅读"

### Pitfall 4: Image URL in Markdown Already Relative
**What goes wrong:** Replacing CSDN image URLs when they are already local or SVG
**Why it happens:** Not all img src are CSDN CDN links
**How to avoid:** Only replace URLs matching `img.csdn.net` or `upload` patterns, skip others
**Warning signs:** Broken images in imported articles

### Pitfall 5: MD5 Collision / Hash Conflicts
**What goes wrong:** Different content produces same MD5 (extremely rare but possible)
**Why it happens:** 128-bit hash space collision
**How to avoid:** Accept as negligible risk for blog content, rely on csdn_article_id for dedup primary key
**Warning signs:** N/A — negligible probability

---

## Code Examples

### CSDN Article List Fetching with Fallbacks
```java
// Source: [ASSUMED] - Jsoup API pattern based on training
// Primary selector: div.article-list a.article-title
// Fallback selector: div.post a.post-title
public List<ArticleLink> fetchArticleList(String csdnUserId) {
    String url = "https://blog.csdn.net/" + csdnUserId + "/article/list/";
    Document doc = restClient.get().uri(url).retrieve().body(Document.class);
    
    Elements links = doc.select("div.article-list a.article-title");
    if (links.isEmpty()) {
        links = doc.select("div.post-list a.post-title");
        log.warn("CSDN primary selector failed, using fallback");
    }
    
    return links.stream()
        .map(el -> new ArticleLink(
            el.attr("href"),
            el.text(),
            extractArticleId(el.attr("href"))
        ))
        .collect(toList());
}
```

### MD5 Content Hashing
```java
// Source: [ASSUMED] - Spring DigestUtils or commons-codec
import org.apache.commons.codec.digest.DigestUtils;

public String computeContentHash(String content) {
    return DigestUtils.md5Hex(content.getBytes(StandardCharsets.UTF_8));
}
```

### HTML to Markdown with Jsoup
```java
// Source: [ASSUMED] - markdownify library pattern
// Using com.atr.util:markdownify library
Markdownify markdownify = new MarkdownifyBuilder()
    .build();

public String toMarkdown(String htmlContent) {
    return markdownify.convert(htmlContent);
}
```

### Spring RestClient HTTP Fetch
```java
// Source: [ASSUMED] - Spring Boot 3.2 RestClient pattern
@RestClient
@Autowired
private RestClient.Builder restClientBuilder;

public String fetchHtml(String url) {
    return restClientBuilder.build()
        .get()
        .uri(url)
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .retrieve()
        .body(String.class);
}
```

### Sync Orchestrator Pattern
```java
// Source: [Based on training] - sync orchestrator with dedup
public SyncResultResponse syncArticles() {
    CsdnSyncConfig config = getConfig();
    List<ArticleLink> articles = fetcher.fetchArticleList(config.getCsdnUserId());
    
    int created = 0, updated = 0, skipped = 0;
    List<String> errors = new ArrayList<>();
    
    for (ArticleLink link : articles) {
        try {
            String html = fetcher.fetchArticle(link.getUrl());
            String content = parser.extractContent(html);
            String markdown = converter.toMarkdown(content);
            String hash = hashService.computeContentHash(content);
            
            Article existing = articleMapper.findByCsdnArticleId(link.getArticleId());
            
            if (existing == null) {
                importArticle(link, markdown, hash, config);
                created++;
            } else if (!existing.getContentHash().equals(hash)) {
                updateArticle(existing, markdown, hash);
                updated++;
            } else {
                skipped++;
            }
        } catch (Exception e) {
            log.error("Failed to sync article: {}", link.getUrl(), e);
            errors.add(link.getUrl() + ": " + e.getMessage());
        }
    }
    
    updateLastSyncAt();
    return new SyncResultResponse(created, updated, skipped, errors);
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| RestTemplate | RestClient (Boot 3.2) | Boot 3.2 (2024) | Fluent synchronous HTTP API |
| HtmlCleaner / JSoup regex | Jsoup CSS selectors | Pre-2015 | jQuery-like DOM traversal |
| Custom MD5 impl | DigestUtils / MessageDigest | N/A | Standard library |
| Manual HTML->MD | markdownify library | N/A | Preserves structure, links, code blocks |

**Deprecated/outdated:**
- RestTemplate: Deprecated in Boot 3.2 in favor of RestClient
- Jsoup 1.8.x: Newer 1.18.x has better HTML5 parsing

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Jsoup 1.18.x available on Maven Central | Standard Stack | Version may differ — verify before implementation |
| A2 | markdownify library works with CSDN HTML structure | Code Examples | Library may not handle CSDN-specific tags well, may need custom post-processing |
| A3 | CSDN article list URL pattern is `blog.csdn.net/{userId}/article/list/` | Code Examples | CSDN may have changed URL structure |
| A4 | CSS selectors `div.article-list a.article-title` are accurate | Pitfalls | CSDN HTML structure may differ — fallbacks critical |
| A5 | Image deduplication by URL hash is the approach (SYNC-09) | Common Pitfalls | Already decided in v1.1, but image download service from Phase 2 handles actual dedup |

---

## Open Questions

1. **CSDN article list URL structure**
   - What we know: CSDN blog URLs follow `blog.csdn.net/{userId}` pattern
   - What's unclear: Exact pagination URL for article lists (is it `/article/list/1`?)
   - Recommendation: Verify CSDN URL pattern manually or add flexible URL builder with fallbacks

2. **CSDN image domain patterns**
   - What we know: Need to replace CSDN CDN image URLs with local paths
   - What's unclear: Full list of CSDN image CDN domains (img-cdn, mcs-cdn, etc.)
   - Recommendation: Log all unique image domains encountered during first sync, expand pattern incrementally

3. **Error logging destination**
   - What we know: Continue + summary requires detailed logging
   - What's unclear: Log to file, database, or just Spring logs?
   - Recommendation: Use Spring `@Slf4j` logging, include structured fields for metrics

4. **markdownify library availability**
   - What we know: Need HTML-to-Markdown conversion
   - What's unclear: Exact Maven coordinates for markdownify library
   - Recommendation: Search for `com.atr.util:markdownify` on Maven Central before planning

---

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java 17+ | Backend runtime | Yes | [ASSUMED] | N/A |
| MySQL | Database | Yes (per CLAUDE.md) | 8.0+ | N/A |
| Maven | Build | Yes | 3.9+ | N/A |
| Jsoup 1.18.x | HTML parsing | Not in pom.xml | Missing | Add dependency |
| Spring RestClient | HTTP client | Yes (Boot 3.2) | Built-in | N/A |
| commons-codec | MD5 hashing | Transitively available | N/A | java.security.MessageDigest |

**Missing dependencies with no fallback:**
- Jsoup 1.18.x — must be added to pom.xml

**Missing dependencies with fallback:**
- markdownify library — if unavailable, write custom Jsoup-to-Markdown converter

---

## Validation Architecture

> Validation architecture section included since `workflow.nyquist_validation` is absent from config (treating as enabled by default).

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 (spring-boot-starter-test) |
| Config file | None detected — use Wave 0 setup |
| Quick run command | `mvn test -Dtest=CsdnSyncServiceTest` |
| Full suite command | `mvn test` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| SYNC-01 | Config persisted | unit | `mvn test -Dtest=CsdnSyncConfigMapperTest` | NO |
| SYNC-03 | Config CRUD via REST | integration | `mvn test -Dtest=CsdnSyncControllerTest` | NO |
| SYNC-04 | Fetch article list | unit | `mvn test -Dtest=CsdnArticleFetcherTest` | NO |
| SYNC-05 | Dedup logic | unit | `mvn test -Dtest=CsdnSyncServiceTest#testDeduplication` | NO |
| SYNC-06 | HTML parsing | unit | `mvn test -Dtest=CsdnArticleParserTest` | NO |
| SYNC-07 | Image URL replacement | unit | `mvn test -Dtest=HtmlToMarkdownConverterTest` | NO |
| SYNC-14 | V6 migration | manual | Review migration SQL | NO |
| SYNC-15 | V7 migration | manual | Review migration SQL | NO |

### Sampling Rate
- **Per task commit:** Unit test for modified component
- **Per wave merge:** Full `mvn test` suite
- **Phase gate:** All unit tests green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `backend/src/test/java/com/blog/mapper/CsdnSyncConfigMapperTest.java` — covers SYNC-01
- [ ] `backend/src/test/java/com/blog/controller/CsdnSyncControllerTest.java` — covers SYNC-03
- [ ] `backend/src/test/java/com/blog/service/CsdnArticleFetcherTest.java` — covers SYNC-04
- [ ] `backend/src/test/java/com/blog/service/CsdnSyncServiceTest.java` — covers SYNC-05
- [ ] `backend/src/test/java/com/blog/service/CsdnArticleParserTest.java` — covers SYNC-06
- [ ] `backend/src/test/java/com/blog/service/HtmlToMarkdownConverterTest.java` — covers SYNC-07
- [ ] `backend/src/test/java/com/blog/service/impl/CsdnSyncServiceImplTest.java` — integration test
- [ ] `backend/src/test/resources/fixtures/csdn-article-list.html` — mock HTML for tests
- [ ] `backend/src/test/resources/fixtures/csdn-article-content.html` — mock article HTML
- [ ] Framework install: Add to pom.xml if not present — `spring-boot-starter-test` already present

---

## Security Domain

> Required when `security_enforcement` is enabled (absent = enabled).

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | Yes | JWT via existing JwtAuthenticationFilter — sync endpoints are `/api/admin/**` |
| V3 Session Management | No | Stateless JWT — no session state |
| V4 Access Control | Yes | Admin-only endpoints — `SecurityConfig` requires authentication for `/api/admin/**` |
| V5 Input Validation | Yes | CSDN userId, articleId, content — validate with `@Valid` DTOs |
| V6 Cryptography | Yes | MD5 for content hashing (not security) — using standard library |

### Known Threat Patterns for CSDN Sync

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SSRF via CSDN URL | Spoofing, Info Disclosure | Validate URL matches expected CSDN domain, no arbitrary URL injection |
| HTML injection via scraped content | XSS, Tampering | Jsoup sanitize before storing, markdown conversion removes scripts |
| SQL injection via userId | Tampering | MyBatis `#{ }` parameterization (no `${}` interpolation) |
| CSDN API abuse | Repudiation | Rate limiting already in `RateLimitFilter`, add per-user sync cooldown |

### Security Controls in This Phase

1. **Input validation:** CsdnSyncConfigRequest validated with `@NotBlank`, `@NotNull`
2. **URL validation:** CSDN URLs validated against allowlist before fetching
3. **Output encoding:** HTML content sanitized via Jsoup.clean() before markdown conversion
4. **Admin-only access:** Sync endpoints require JWT authentication via existing SecurityConfig
5. **Rate limiting:** Existing `RateLimitFilter` applies to all endpoints

---

## Sources

### Primary (HIGH confidence)
- [Spring Boot 3.2.5 Release](https://spring.io/projects/spring-boot) — confirmed Boot version, RestClient availability
- [MyBatis 3.0.4 Docs](https://mybatis.org/mybatis-3/) — existing project ORM pattern
- [Flyway 10.x Docs](https://flywaydb.org/documentation/) — existing project migration pattern

### Secondary (MEDIUM confidence)
- [Jsoup 1.18.x](https://jsoup.org/) — [ASSUMED] based on training, version locked in CONTEXT.md D-06
- [CSDN Blog Structure](https://blog.csdn.net/) — [ASSUMED] standard structure based on training

### Tertiary (LOW confidence)
- CSDN CSS selectors — needs validation against real CSDN pages
- markdownify library — needs verification of Maven coordinates and compatibility

---

## Metadata

**Confidence breakdown:**
- Standard stack: MEDIUM — Jsoup version and markdownify library not verified against registry
- Architecture: HIGH — follows established project patterns (entity/mapper/service/controller)
- Pitfalls: MEDIUM — based on general web scraping experience, CSDN-specific selectors need validation

**Research date:** 2026-04-15
**Valid until:** 2026-05-15 (30 days for stable domain, may need re-validation if CSDN changes structure)

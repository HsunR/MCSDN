# Stack Research

**Domain:** CSDN Article Sync to Personal Blog
**Researched:** 2026-04-15
**Confidence:** MEDIUM

*Note: Web search was unavailable during research (API errors). Findings are based on established Java ecosystem patterns and known CSDN platform characteristics. CSDN Open Platform (open.csdn.net) documentation could not be verified — recommend phase-specific research before implementation.*

## Recommended Stack

### Backend Additions

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Jsoup | 1.18.x | HTML parsing and manipulation | Standard Java HTML parser, CSS selectors for CSDN article extraction, no external dependencies |
| Spring RestClient | Built-in (Spring Boot 3.2+) | HTTP client for fetching CSDN pages | Replaces RestTemplate; Spring's recommended HTTP client for Spring Boot 3.2+ |
| Spring Scheduling | Built-in | Timer-based sync scheduling | No new dependency for simple cron-style scheduling |
| Markdown conversion: html2md or custom | — | Convert CSDN HTML content to Markdown | Lightweight string transformation; can implement inline |
| Image deduplication: MD5 hash | Built-in (Java stdlib) | Deduplicate images by URL hash | No library needed, use `java.security.MessageDigest` |

### Supporting Libraries (Backend)

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Jsoup | 1.18.x | Parse CSDN HTML pages, extract article content, tags, images | Required for article scraping |
| Caffeine (optional) | 3.1.x | Cache CSDN article list during sync | Only if syncing many articles, reduces CSDN page requests |

### Frontend Additions

| Technology | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Sync dashboard component | New | Display sync status, last sync time, article counts | Required |
| Manual sync trigger button | New | One-click sync in admin panel | Required |
| Sync configuration form | New | Set CSDN userId, target category, schedule toggle | Required |

*Note: Frontend additions are UI components only — no new npm dependencies expected. Reuse existing Axios, Tailwind CSS, Vue patterns.*

## Installation

```bash
# Backend (Maven pom.xml additions)

# Jsoup for HTML parsing
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.1</version>
</dependency>

# Caffeine cache (optional, for article list caching)
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

# Note: Spring Boot 3.2+ RestClient is built into spring-boot-starter-web
# No additional HTTP client dependency needed

# Frontend
# No new npm dependencies expected
# Components built with existing: Vue 3, Tailwind CSS, Axios
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Jsoup | Spring WebFlux + Cheerio (Node.js scraper microservice) | If CSDN blocks Java scraping or rate limits heavily |
| Spring Scheduling (built-in) | Quartz Scheduler | If complex cron expressions or distributed scheduling needed |
| RestClient (Spring 6/Boot 3.2) | OkHttp + Jsoup HTTP | If fine-grained HTTP control needed beyond RestClient |
| html2md (library) | Custom regex-based HTML-to-Markdown conversion | If html2md proves incomplete for CSDN HTML |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Jsoup 1.15.x | Version 1.18.x has better HTML5 compliance for modern CSDN pages | Jsoup 1.18.x |
| `@Scheduled` with blocking I/O on async executor | Blocks Spring's async thread pool | Use `@Async` with dedicated thread pool, or non-blocking WebClient |
| Base64 image embedding in articles | Bloats article content, breaks local storage constraint | Download images to local filesystem per existing `/uploads/{year}/{month}/{uuid}.{ext}` pattern |
| Separate Node.js scraper service | Adds deployment complexity, goes against single-backend architecture | Keep scraping in JVM with Jsoup |
| RestTemplate | Deprecated in Spring 6, replaced by RestClient | RestClient (Spring Boot 3.2+ built-in) |

## Stack Patterns by Variant

**If CSDN provides an official Open API (open.csdn.net):**
- Use official API endpoints (authenticated) instead of HTML scraping
- Eliminates need for Jsoup HTML parsing
- Reduces sync complexity and rate limiting concerns
- Verify API rate limits and OAuth requirements

**If CSDN requires HTML scraping (no official API or API insufficient):**
- Use Jsoup to fetch and parse article pages
- Target article URL pattern: `https://blog.csdn.net/{userId}/article/details/{articleId}`
- Extract: article title, content HTML, tags, publish date
- Convert HTML content to Markdown for storage
- Download and re-upload images to local storage, replace `src` URLs with local paths

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| Jsoup 1.18.x | Java 17+, Spring Boot 3.x | No known conflicts |
| Spring Scheduling | Any Spring Boot 3.x | Built-in, no version concerns |
| RestClient (Spring 6) | Spring Boot 3.2+ | Replaces RestTemplate as recommended client |
| Caffeine 3.1.x | Java 17+ | Compatible with Spring Boot 3 |

## Integration with Existing Stack

The CSDN sync feature builds on top of the existing v1.0 stack with these additions:

**Existing components reused:**
- `ArticleService` — for importing parsed articles (create/update)
- `ImageUploadService` — for storing downloaded images (path pattern already exists)
- Spring Security — protect sync admin endpoints
- MyBatis — add new fields (`source`, `csdn_article_id`) to existing article mapper
- Flyway — new migration for article table columns

**New components:**
- `CsdnSyncService` — orchestrates fetch, parse, convert, download, import
- `CsdnConfig` — CSDN userId, target category, schedule settings
- Spring `@Scheduled` or `TaskScheduler` bean — for automatic sync

**Frontend integration:**
- New view: `SyncSettingsView.vue` — configuration form
- Extend `AdminSidebar.vue` — add sync nav item
- Extend `DashboardView.vue` — show sync status widget
- Reuse existing Axios patterns for API calls

## Sources

- CSDN Open Platform — **UNVERIFIED** (web search unavailable, open.csdn.net not accessible during research session). Verify official API capabilities before implementation.
- Jsoup 1.18.x docs — https://jsoup.org/
- Spring RestClient — https://docs.spring.io/spring-framework/reference/web/webclient-restclient.html
- Spring Scheduling — https://docs.spring.io/spring-boot/reference/features/scheduling.html

---
*Stack research for: CSDN article sync additions to existing blog stack*
*Researched: 2026-04-15*

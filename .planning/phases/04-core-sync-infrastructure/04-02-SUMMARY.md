---
phase: 04-core-sync-infrastructure
plan: '02'
subsystem: infra
tags: [csdn, jsoup, restclient, html, markdown, image-processing]

# Dependency graph
requires:
  - phase: 04-01
    provides: Jsoup 1.18.1 dependency, HtmlToMarkdownConverter already created first to break circular dependency with parser
provides:
  - CsdnArticleFetcher — fetches CSDN article list and article HTML via RestClient + Jsoup
  - CsdnArticleParser — parses CSDN HTML to extract title, content (Markdown), tags with XSS sanitization
  - HtmlToMarkdownConverter — converts HTML to Markdown with CSDN image URL replacement
  - CsdnArticleDto — structured DTO for parsed CSDN article data
affects: [04-03, 04-04]

# Tech tracking
tech-stack:
  added: [Jsoup 1.18.1, Spring RestClient (Boot 3.2 native)]
  patterns: [HTML scraping with CSS selectors, HTML-to-Markdown conversion, URL hashing for image deduplication]

key-files:
  created:
    - backend/src/main/java/com/blog/dto/CsdnArticleDto.java
    - backend/src/main/java/com/blog/service/CsdnArticleFetcher.java
    - backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java
    - backend/src/main/java/com/blog/service/CsdnArticleParser.java
    - backend/src/main/java/com/blog/service/impl/CsdnArticleParserImpl.java
    - backend/src/main/java/com/blog/service/HtmlToMarkdownConverter.java
    - backend/src/main/java/com/blog/service/impl/HtmlToMarkdownConverterImpl.java
  modified: []

key-decisions:
  - "Created HtmlToMarkdownConverter before CsdnArticleParser to avoid circular dependency (parser depends on converter)"
  - "Used Spring RestClient.Builder with @Autowired for HTTP fetching (Boot 3.2 native, not RestTemplate)"
  - "Applied Jsoup.clean(Safelist.none()) for XSS mitigation before parsing external CSDN HTML"
  - "CSDN image URLs replaced with MD5-hashed local paths: /uploads/csdn/{hash}.{ext}"
  - "Primary CSS selector: div.article-list a.article-title with fallback div.post-list a.post-title"

patterns-established:
  - "Pattern: CSS selector with fallback chain — try primary selector, log warning, fall back to alternative"
  - "Pattern: HTML sanitization before parsing untrusted external content"
  - "Pattern: URL-to-local-path conversion using MD5 hash for deterministic image deduplication"

requirements-completed: [SYNC-04, SYNC-06, SYNC-07]

# Metrics
duration: 8min
completed: 2026-04-15
---

# Phase 4 Plan 2: CSDN Article Fetching, Parsing, and HTML-to-Markdown Conversion Summary

**CSDN article fetcher with RestClient + Jsoup, HTML parser with XSS sanitization, and HTML-to-Markdown converter with CSDN image URL replacement**

## Performance

- **Duration:** 8 min
- **Started:** 2026-04-15T11:52:40Z
- **Completed:** 2026-04-15T12:00:XXZ
- **Tasks:** 4
- **Files modified:** 7

## Accomplishments
- CsdnArticleFetcher retrieves CSDN article list and article HTML via Spring RestClient + Jsoup with primary/fallback CSS selectors
- CsdnArticleParser extracts structured article data (title, content, tags) with HTML sanitization against XSS
- HtmlToMarkdownConverter converts HTML to Markdown and replaces CSDN image URLs with MD5-hashed local placeholders
- CsdnArticleDto provides structured container for parsed CSDN article data
- Maven compile succeeds with no errors

## Task Commits

Each task was committed atomically:

1. **Task 1: CsdnArticleDto** - `70c77fe` (feat)
2. **Task 2: CsdnArticleFetcher** - `982d62f` (feat)
3. **Task 4: HtmlToMarkdownConverter** - `b8f98d6` (feat)
4. **Task 3: CsdnArticleParser** - `61c8d17` (feat)

## Files Created/Modified

- `backend/src/main/java/com/blog/dto/CsdnArticleDto.java` — DTO with articleId, title, content, tags, url fields
- `backend/src/main/java/com/blog/service/CsdnArticleFetcher.java` — Interface for fetchArticleList() and fetchArticleHtml()
- `backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java` — RestClient + Jsoup implementation with primary/fallback selectors
- `backend/src/main/java/com/blog/service/CsdnArticleParser.java` — Interface for parseArticle(html, url)
- `backend/src/main/java/com/blog/service/impl/CsdnArticleParserImpl.java` — Parser with XSS sanitization via Jsoup.clean()
- `backend/src/main/java/com/blog/service/HtmlToMarkdownConverter.java` — Interface for HTML-to-Markdown conversion
- `backend/src/main/java/com/blog/service/impl/HtmlToMarkdownConverterImpl.java` — Converter supporting p/h1-h6/ul/ol/li/pre/code/blockquote/a/img/strong/em/br, CSDN image URL replacement via MD5 hash

## Decisions Made

- Created HtmlToMarkdownConverter before CsdnArticleParser to break circular dependency (parser @Autowired converter; converter has no parser dependency)
- Used Spring RestClient.Builder with @Autowired rather than RestTemplate (Boot 3.2 native, D-07 decision)
- Applied Jsoup.clean(Safelist.none()) before parsing — strips all HTML tags, preventing XSS from untrusted CSDN content (T-04-04 mitigation)
- CSDN image URLs replaced with local placeholders using MD5 hash: `/uploads/csdn/{16-char-hash}.{ext}` for deterministic deduplication
- Primary CSS selector `div.article-list a.article-title` with fallback `div.post-list a.post-title` per D-02

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None — all tasks completed without problems.

## Next Phase Readiness

- All services compile successfully with Maven
- CsdnArticleFetcher, CsdnArticleParser, and HtmlToMarkdownConverter are ready for integration with sync configuration (04-03) and sync execution (04-04)
- No blockers for subsequent plans

---
*Phase: 04-core-sync-infrastructure*
*Completed: 2026-04-15*

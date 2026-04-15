# Project Research Summary

**Project:** CSDN Article Sync (v1.1 milestone)
**Domain:** Content migration/sync feature for personal blog
**Researched:** 2026-04-15
**Confidence:** MEDIUM

## Executive Summary

This research covers adding CSDN (Chinese Software Developer Network) article synchronization to an existing personal blog system. CSDN provides no public export API, so the only viable approach is HTML web scraping -- parsing article pages directly. This is the industry standard for Chinese blog platforms but introduces fragility: CSDN periodically redesigns their page structure, breaking scrapers without warning.

The recommended approach is incremental implementation: start with manual single-article sync, validate HTML parsing against real CSDN pages, then build toward scheduled auto-sync. The core value chain is fetch -> parse HTML -> convert to Markdown -> download images to local storage -> import as blog article. Each step depends on the previous and must be tested in isolation.

Key risks are CSDN anti-scraping blocking (403/429 errors), fragile CSS selectors that break when CSDN redesigns, and image URL expiration (CSDN uses signed URLs that expire). Mitigation requires validating parsed content immediately, implementing retry logic with exponential backoff, and downloading images immediately rather than on-demand.

## Key Findings

### Recommended Stack

CSDN sync builds on the existing v1.0 blog stack (Vue 3, Spring Boot 3, MyBatis, MySQL, Tailwind CSS) with minimal additions.

**New backend dependencies:**
- **Jsoup 1.18.x** -- HTML parsing and CSS selector extraction. Standard Java HTML parser, no external dependencies. Required for scraping CSDN article pages.
- **Spring RestClient** (built-in Spring Boot 3.2+) -- HTTP client for fetching CSDN pages. Replaces deprecated RestTemplate; Spring's recommended client for Boot 3.2+.
- **Spring Scheduling** (built-in) -- Timer-based sync scheduling. No new dependency needed for simple cron-style scheduling.
- **Caffeine 3.1.x** (optional) -- Cache CSDN article list during bulk sync. Only needed when syncing many articles.

**Frontend additions are UI components only** -- no new npm dependencies. Reuse existing Vue 3, Tailwind CSS, Axios, and Pinia patterns.

**Key decision:** If CSDN Open Platform (open.csdn.net) has a usable API, use it instead of scraping. Official API eliminates selector fragility and reduces rate limiting concerns. This needs verification during Phase 1.

### Expected Features

**Must have (table stakes):**
- Manual one-click sync for single CSDN article URL -- user wants control over when sync happens
- HTML extraction and content parsing -- core value prop, must extract actual article text
- Image local download and URL replacement -- CSDN images will break if not downloaded
- CSDN article ID tracking -- enables deduplication, prevents duplicate articles
- Edit warning for synced articles -- prevents accidentally overwriting synced content

**Should have (competitive differentiators):**
- Scheduled auto-sync -- set and forget, new CSDN articles appear automatically
- Sync status dashboard -- show last sync time, articles synced, errors
- Tag/category mapping -- CSDN tags don't map directly to local categories

**Defer to v2+:**
- Bidirectional sync (push edits back to CSDN) -- CSDN doesn't support this
- Comment importing -- comments are platform-specific, spam-prone
- Real-time webhook sync -- CSDN has no webhook API
- Selective article sync (choose which to import) -- adds complexity to v1.1

### Architecture Approach

The sync feature follows a pull-based external content sync pattern: periodically pull content from CSDN into the local database using a deduplication key (CSDN article ID). New articles are inserted, existing articles are updated only if CSDN version is newer.

**Major new components:**
1. **CsdnSyncService** -- Orchestrates the full sync flow: fetch article list, parse, convert, download images, import
2. **CsdnPageParser** -- Fetches CSDN HTML via RestClient, extracts content with Jsoup CSS selectors
3. **HtmlToMarkdownConverter** -- Converts CSDN HTML content to Markdown for storage
4. **ImageDownloaderService** -- Downloads images to local `/uploads/{year}/{month}/{uuid}.{ext}`, replaces URLs in Markdown
5. **CsdnSyncConfig** entity + mapper -- Stores sync configuration (CSDN userId, target category, schedule settings)

**Data model changes:**
- Articles table: add `source` (VARCHAR) and `csdn_article_id` (VARCHAR with unique index)
- New `csdn_sync_config` table: stores userId, target category, schedule enabled, last sync timestamp

**Frontend additions:**
- SyncSettingsView.vue -- configuration form for CSDN userId, target category, schedule toggle
- SyncStatusWidget.vue -- dashboard widget showing last sync time and article count
- csdnApi.js -- Axios client for sync endpoints

### Critical Pitfalls

1. **CSDN anti-scraping blocking (403/429)** -- CSDN uses aggressive bot detection. Mitigation: set realistic User-Agent/Accept-Language headers, add random delays between requests (500ms-2s), implement exponential backoff on failures, consider headless browser if needed.

2. **Fragile HTML selectors** -- CSDN periodically redesigns page structure, breaking CSS selectors like `.article-content` or `#content_views`. Mitigation: validate parsed content immediately (check title not empty, content length > X), use multiple selector strategies with fallbacks, log selector failures.

3. **Image URL expiration** -- CSDN uses signed/expiring URLs for images. Mitigation: download images immediately during sync, never store CSDN image URLs in Markdown, always replace with local paths.

4. **Duplicate articles from no deduplication** -- Sync without checking `csdn_article_id` first creates duplicates. Mitigation: unique constraint on `csdn_article_id`, check existence BEFORE fetching content, use upsert pattern.

5. **Full sync blocking on manual trigger** -- No pagination or limit means 500 articles download synchronously, timing out. Mitigation: limit to last N articles for manual sync, implement async/background processing, show progress.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Core Sync Infrastructure
**Rationale:** All sync features depend on this. Must establish data model, API endpoints, and basic scraping before any sync UI or scheduling.
**Delivers:** Flyway migration V2 (add source columns + csdn_sync_config table), CsdnSyncConfig entity and mapper, CsdnApiController with config/status endpoints, manual single-article sync working end-to-end.
**Addresses:** Manual sync trigger, HTML extraction, HTML-to-Markdown conversion, CSDN article ID deduplication, sync config API
**Avoids:** No deduplication pitfall (unique constraint on csdn_article_id from start), fragile selectors (validate content immediately after parsing)

### Phase 2: Image Handling and Sync UI
**Rationale:** Image downloading is critical for content integrity and must work before scheduled sync runs unattended. Sync UI needed for admin to configure and trigger sync.
**Delivers:** ImageDownloaderService (download + local storage + URL replacement), SyncSettingsView.vue (config form), sync status display, edit warning for synced articles.
**Addresses:** Image local download, source attribution, edit warning, sync configuration form
**Avoids:** Image URL expiration pitfall (downloaded immediately), base64 embedding pitfall (uses local filesystem pattern from v1.0)

### Phase 3: Scheduled Auto-Sync
**Rationale:** Manual sync must work reliably first. Scheduling adds complexity (cron config, background execution, error handling) that should not block initial validation.
**Delivers:** Spring @Scheduled job with configurable cron, enable/disable toggle, background async processing, last_sync_at tracking.
**Addresses:** Scheduled auto-sync, sync status dashboard
**Avoids:** Full sync blocking (async processing from start), local edit overwrite (timestamp tracking per article)

### Phase 4 (Future): Bulk Sync and Optimization
**Rationale:** Once basic sync works, add article list fetching and selective sync for users with large CSDN histories. Rate limiting and scaling concerns become relevant here.
**Delivers:** CSDN article list fetching, bulk sync with checkboxes, rate limiting, concurrent image downloads.
**Addresses:** Selective sync, tag/category mapping UI, sync history logging

### Phase Ordering Rationale

- **Phase 1 before 2:** Data model and scraping must work before UI can test it
- **Phase 2 before 3:** Image handling is required for content integrity; UI needed to validate config
- **Phase 3 before 4:** Scheduling adds complexity that should not block initial validation
- **Dependency chain:** HTML extraction -> Markdown conversion -> Image download -> Article import -> Scheduling -> Bulk operations

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1:** CSDN Open Platform API verification -- if official API exists and is usable, architecture changes significantly (no Jsoup scraping needed)
- **Phase 1:** CSDN HTML selector validation -- need to test against real CSDN pages to confirm selector paths work
- **Phase 4:** CSDN rate limiting behavior -- bulk sync at scale (500+ articles) needs empirical testing

Phases with standard patterns (skip research-phase):
- **Phase 1:** Flyway migration pattern -- well-established, same as v1.0 setup
- **Phase 2:** Image upload to local filesystem -- already exists in v1.0, reuse pattern
- **Phase 3:** Spring @Scheduled -- documented in Spring Boot official docs

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM | Jsoup, RestClient, Spring Scheduling are well-documented. CSDN Open Platform API is unverified (web search unavailable). |
| Features | MEDIUM | Feature landscape is clear; CSDN scraping approach is industry standard. Prioritization is reasonable but user feedback may shift priorities. |
| Architecture | MEDIUM | Patterns are sound (pull-sync, deduplication key, image locality). Component breakdown is logical. Build order is well-reasoned. |
| Pitfalls | MEDIUM | General blog pitfalls are well-established. CSDN-specific pitfalls (anti-scraping, selector fragility) are known risks but severity depends on CSDN's behavior. |

**Overall confidence:** MEDIUM

### Gaps to Address

- **CSDN Open Platform API:** Could eliminate need for HTML scraping if available. Must verify before Phase 1 implementation.
- **CSDN HTML selector paths:** Actual CSS selectors need validation against real CSDN article pages. May need adjustment during implementation.
- **Rate limiting behavior:** How quickly does CSDN block after how many requests? Empirical testing needed before Phase 4 planning.

## Sources

### Primary (HIGH confidence)
- Jsoup 1.18.x docs (https://jsoup.org/) -- HTML parsing library, well-documented
- Spring RestClient docs (https://docs.spring.io/spring-framework/reference/web/webclient-restclient.html) -- Official Spring documentation
- Spring Scheduling docs (https://docs.spring.io/spring-boot/reference/features/scheduling.html) -- Official Spring Boot documentation

### Secondary (MEDIUM confidence)
- CSDN Open Platform (open.csdn.net) -- **UNVERIFIED**, not accessible during research session
- jsoup HTML parsing for CSDN article pages -- standard web scraping approach, works but fragile
- markdown-it (already in project) -- frontend Markdown conversion, existing pattern reuse

### Tertiary (LOW confidence)
- CSDN HTML selector paths -- approximate selectors based on training data, need validation against live CSDN pages
- CSDN rate limit thresholds -- inferred from general bot detection behavior, not measured

---
*Research completed: 2026-04-15*
*Ready for roadmap: yes*

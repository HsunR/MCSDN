# Feature Research: CSDN Article Sync

**Domain:** Blog migration/sync from CSDN (Chinese Software Developer Network)
**Researched:** 2026-04-15
**Confidence:** MEDIUM

> **Research note:** Web search APIs failed during research. Findings based on training data and web fetches to CSDN/jsoup documentation. CSDN does not appear to have a public API for article export. Recommend validating HTML selector paths during implementation as CSDN periodically changes page structure.

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = product feels broken.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Manual one-click sync | User wants control over when sync happens | MEDIUM | Requires backend job execution + frontend trigger |
| Article content extraction | Core value prop - must get the actual article text | MEDIUM | HTML parsing + HTML-to-Markdown conversion |
| Image local download | CSDN images will break if not downloaded | MEDIUM | Reuse existing image upload logic |
| Source attribution | Users want to know article came from CSDN | LOW | Add `source: CSDN` field to article |
| Deduplication | Don't create duplicate articles on re-sync | LOW | Track CSDN article ID, check before creating |

### Differentiators (Competitive Advantage)

Features that set the product apart. Not required, but valuable.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Scheduled auto-sync | Set and forget - new articles appear automatically | HIGH | Requires scheduler (Spring @Scheduled or Quartz), configuration UI |
| Tag/category mapping | CSDN tags don't match your categories - map them | MEDIUM | Need mapping configuration per CSDN tag -> local category |
| Edit warning for synced articles | Prevent accidentally overwriting synced content | LOW | Simple UI warning dialog |
| Selective sync (choose which articles) | User may not want ALL CSDN articles | MEDIUM | List view with checkboxes, bulk sync |
| Sync status dashboard | Show last sync time, articles synced, errors | LOW | Simple status panel in admin UI |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Real-time sync (webhook) | "Sync immediately when I publish on CSDN" | CSDN has no webhook API; would require polling which is wasteful | Manual sync button is sufficient |
| Bidirectional sync | "Edit on my blog and push back to CSDN" | CSDN doesn't support this; violates CSDN ToS likely | One-way import is the standard approach |
| Sync comments | "Bring over CSDN comments too" | Comments are platform-specific, often spam, CSDN may block scraping | Skip comments - self-contained comment system is better |
| Auto-categorization | "Automatically put articles in right categories" | NLP/categorization is complex, error-prone | Manual tag/category mapping is good enough |

## Feature Dependencies

```
[HTML Content Extraction]
    └──requires──> [HTML-to-Markdown Conversion]
                          └──requires──> [Image URL Replacement]

[Image Download]
    └──requires──> [Image Local Storage] (already exists in v1.0)

[Scheduled Sync]
    └──requires──> [Manual Sync] (must work first)
    └──requires──> [Sync Configuration API]

[Sync Dashboard]
    └──requires──> [Sync Status Tracking]
```

### Dependency Notes

- **HTML extraction requires HTML-to-Markdown conversion:** You cannot store what you extract without converting it to the blog's format
- **Image download builds on existing local storage:** v1.0 image upload/download is directly reusable
- **Scheduled sync requires manual sync working first:** Don't add scheduler until basic sync is tested
- **Sync dashboard needs status tracking:** Need to store last_sync_time, sync_status somewhere

## MVP Definition

### Launch With (v1.1)

Minimum viable product - what's needed to validate the concept.

- [ ] **Manual sync trigger** - Backend endpoint + admin button to trigger sync for one CSDN article URL
- [ ] **HTML extraction** - Parse CSDN article page, extract title, content HTML, publish date, tags
- [ ] **HTML-to-Markdown conversion** - Convert extracted HTML to Markdown for storage
- [ ] **Image download + URL replacement** - Download CSDN images to local storage, update markdown image URLs
- [ ] **CSDN article ID tracking** - Store `csdn_article_id` to enable deduplication
- [ ] **Edit warning** - When editing a synced article, show "This is a synced article" warning
- [ ] **Sync config API** - Store CSDN user ID, target default category

### Add After Validation (v1.x)

Features to add once core is working.

- [ ] **Scheduled auto-sync** - Add `@Scheduled` job with cron config, enable/disable toggle
- [ ] **Sync status dashboard** - Show last sync time, count, errors
- [ ] **Bulk sync** - Fetch CSDN user article list, show as checklist, sync selected

### Future Consideration (v2+)

Features to defer until product-market fit is established.

- [ ] **Tag mapping UI** - Visual mapping of CSDN tags to local categories
- [ ] **Selective article sync** - Choose which CSDN articles to sync instead of all
- [ ] **Sync history** - Log each sync operation with details

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Manual one-article sync | HIGH | MEDIUM | P1 |
| HTML-to-Markdown conversion | HIGH | MEDIUM | P1 |
| Image download + URL rewrite | HIGH | MEDIUM | P1 |
| CSDN article ID deduplication | HIGH | LOW | P1 |
| Edit warning for synced | MEDIUM | LOW | P1 |
| Sync config (userId, category) | MEDIUM | LOW | P1 |
| Scheduled auto-sync | MEDIUM | HIGH | P2 |
| Sync status dashboard | MEDIUM | LOW | P2 |
| Bulk sync (list + select) | MEDIUM | HIGH | P3 |
| Tag mapping UI | LOW | MEDIUM | P3 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Technical Approach (How CSDN Sync Works)

### The Problem

CSDN does not have a public API for article export or sync. The only viable approach is **web scraping** - parsing the HTML of CSDN article pages.

### Standard Web Scraping Approach

```
1. Fetch CSDN article page URL
   └─> HTTP GET to https://blog.csdn.net/{userId}/article/details/{articleId}

2. Parse HTML response with jsoup
   └─> Extract: title, content div, publish date, tags, view count

3. Convert HTML content to Markdown
   └─> Use Html2Markdown or similar library
   └─> Must preserve code blocks, images, links

4. Process images in markdown
   └─> Regex find all image URLs: ![](url)
   └─> For each URL: download to local /uploads/{year}/{month}/{uuid}.{ext}
   └─> Replace URL in markdown with local path

5. Create article in local blog
   └─> POST /api/articles with: title, markdown, category, tags, source=CSDN, csdnArticleId=xxx
   └─> Check if csdnArticleId exists -> update or skip (deduplication)

6. Store sync metadata
   └─> Track: last_sync_time, sync_status per article
```

### CSDN Page Structure (Approximate)

CSDN article pages typically have:
- Title in `<h1 class="article-title">` or similar
- Content in `<div class="article-content">` or `#content_views`
- Tags in `<div class="article-tag">` or similar
- Publish date in `<span class="time">` or similar

**Note:** CSDN changes their HTML structure periodically. This is a known risk with scraping. Validate selector paths during implementation.

### Key Libraries for Java/Spring Boot

| Library | Purpose | Notes |
|---------|---------|-------|
| **jsoup** | HTML parsing and extraction | Java HTML parser, CSS selectors |
| **commonmark** | Markdown parsing | Or use markdown-it on frontend after API response |
| **Html2Markdown** | HTML to Markdown conversion | Several Java libraries exist |
| **Spring WebClient** | HTTP client for fetching | Reactive, built into Spring WebFlux |

**Simpler approach:** Send raw HTML to frontend, use existing `markdown-it` (already in project) to convert HTML -> Markdown on the frontend, then send Markdown back to backend. This reuses existing tech and avoids adding another Java library.

### Image Deduplication Strategy

```
1. Compute SHA-256 hash of image URL
2. Check if file exists: /uploads/images/{hash}.{ext}
3. If exists -> reuse existing URL
4. If not -> download and save with hash as filename
```

### Sync Deduplication Strategy

```
On sync request for CSDN article {articleId}:
1. Query: SELECT * FROM article WHERE csdn_article_id = {articleId}
2. If found:
   - If content changed (compare hash or timestamp) -> UPDATE
   - If content same -> SKIP (log info)
3. If not found -> INSERT new article
```

## Competitor Feature Analysis

| Feature | CSDN (source) | Juejin | cnblogs | Our Approach |
|---------|---------------|--------|---------|--------------|
| Export API | NONE | NONE | NONE | Web scraping only option |
| Markdown support | Proprietary HTML | Markdown native | Markdown native | Our blog is Markdown-native (good) |
| Image handling | CSDN CDN | Juejin CDN | cnblogs CDN | We download and self-host (more reliable) |
| Tags | Supported | Supported | Supported | Extract and import as our tags |

**Key insight:** No Chinese blog platform provides export APIs. Web scraping is the industry standard approach. It works but is fragile (HTML structure changes break scrapers).

## Sources

- [jsoup documentation](https://jsoup.org/) - Java HTML parsing library
- [markdown-it](https://markdown-it.github.io/) - Markdown parser (already in project)
- [CSDN Blog](https://blog.csdn.net/) - Source platform (no API available)
- [Commonmark](https://commonmark.org/) - Markdown spec reference
- [Spring WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html) - Reactive HTTP client

---
*Feature research for: CSDN Article Sync*
*Researched: 2026-04-15*

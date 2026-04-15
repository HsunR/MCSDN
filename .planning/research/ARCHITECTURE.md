# Architecture Research: CSDN Article Sync

**Domain:** CSDN Article Sync to Personal Blog (v1.1 milestone)
**Researched:** 2026/04/15
**Confidence:** MEDIUM

*Note: Web search was unavailable during research (API errors). Findings are based on established Java ecosystem patterns and known CSDN platform characteristics. CSDN Open Platform (open.csdn.net) capabilities are unverified -- phase-specific research recommended before implementation.*

## Standard Architecture for CSDN Sync

### System Overview (Delta from v1.0)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Presentation Layer (v1.1)                       │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌───────────────────┐  ┌────────────────────┐  │
│  │   Blog UI        │  │   Admin UI        │  │  Sync Settings UI  │  │
│  │   (unchanged)     │  │   (unchanged)     │  │  (NEW - v1.1)     │  │
│  └──────────────────┘  └───────────────────┘  └────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   API Layer (v1.1 additions)                        │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌───────────────────┐  ┌────────────────┐  │
│  │ BlogApi          │  │  AdminApi          │  │ CsdnApi        │  │
│  │ (unchanged)      │  │  (unchanged)       │  │ Controller     │  │
│  │                  │  │                   │  │ (NEW - v1.1)  │  │
│  └──────────────────┘  └───────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   Service Layer (v1.1 additions)                     │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────┐  │
│  │ BlogService      │  │  CsdnSyncService   │  │ ImageDownloader  │  │
│  │ (reused)         │  │  (NEW - v1.1)      │  │ Service (NEW)   │  │
│  └──────────────────┘  └───────────────────┘  └──────────────────┘  │
│  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────┐  │
│  │ ArticleService   │  │  HtmlToMarkdown    │  │ CsdnPageParser   │  │
│  │ (reused)         │  │  Converter (NEW)  │  │ (NEW - v1.1)     │  │
│  └──────────────────┘  └───────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   Data Access Layer (v1.1 changes)                  │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌────────────────────────────────────────┐   │
│  │ ArticleMapper    │  │  CsdnSyncConfigMapper (NEW - v1.1)      │   │
│  │ (modified: add   │  │                                        │   │
│  │  source fields)  │  └────────────────────────────────────────┘   │
│  └──────────────────┘                                               │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                        MySQL Database                                │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌────────────────────────────────────────┐   │
│  │ articles table   │  │  csdn_sync_config table (NEW - v1.1)    │   │
│  │ (modified: add  │  │  - csdn_user_id                         │   │
│  │  source,        │  │  - target_category_id                    │   │
│  │  csdn_article_id│  │  - schedule_enabled                     │   │
│  │  columns)       │  │  - schedule_cron                         │   │
│  └──────────────────┘  │  - last_sync_at                        │   │
│                        │  - last_sync_article_count              │   │
│                        └────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Implementation |
|-----------|----------------|----------------|
| `CsdnApi Controller` | Exposes sync trigger, config save, sync status endpoints | New Spring `@RestController` at `/api/admin/sync` |
| `CsdnSyncService` | Orchestrates: fetch article list -> parse -> convert -> download images -> import | New `@Service`, calls other sync components |
| `CsdnPageParser` | Fetches CSDN article HTML, extracts title/content/tags/publish date | New component using `RestClient` + `Jsoup` |
| `HtmlToMarkdownConverter` | Converts CSDN HTML content to Markdown | New component, string transformation or html2md lib |
| `ImageDownloaderService` | Downloads images from CSDN article to local storage, deduplicates by URL hash | Reuses existing upload path pattern (`/uploads/{year}/{month}/{uuid}.{ext}`) |
| `CsdnSyncConfig` | Entity for sync settings (userId, target category, schedule) | New JPA entity or MyBatis-mapped POJO |
| `CsdnSyncConfigMapper` | Persist and retrieve sync configuration | New MyBatis mapper |
| `SyncSettingsView.vue` | UI form for CSDN userId, target category, schedule toggle | New Vue view in admin panel |
| `AdminSidebar.vue` (modified) | Add "CSDN Sync" nav item | Extend existing sidebar |
| `DashboardView.vue` (modified) | Add sync status widget (last sync time, article count) | Extend existing dashboard |

## Integration Points: New vs Modified

### Modified (v1.0 components that change)

| Component | What Changes | Migration Needed |
|-----------|-------------|------------------|
| `Article` entity | Add `source` (VARCHAR), `csdnArticleId` (VARCHAR) fields | Flyway migration V2 |
| `ArticleMapper` | Add `findByCsdnArticleId()`, `updateByCsdnArticleId()` | MyBatis XML update |
| `ArticleService` | Optionally reuse `createArticle()` / `updateArticle()` for sync import | No structural change |
| `SecurityConfig` | Confirm `/api/admin/sync/**` is protected by JWT filter | Verify (likely already covered by `/api/admin/**` rule) |
| `AdminSidebar.vue` | Add nav item for CSDN sync | Minor extension |
| `DashboardView.vue` | Add sync status widget | Minor extension |

### New Components

| Component | Package | Purpose |
|-----------|---------|---------|
| `CsdnSyncConfig` | `entity/` | Stores: csdnUserId, targetCategoryId, scheduleEnabled, scheduleCron, lastSyncAt, lastSyncArticleCount |
| `CsdnSyncConfigMapper` | `mapper/` | CRUD for sync config |
| `CsdnApiController` | `controller/` | `POST /api/admin/sync/trigger` (manual), `GET /api/admin/sync/status`, `PUT /api/admin/sync/config` |
| `CsdnSyncService` | `service/` | Orchestration logic |
| `CsdnPageParser` | `service/` | HTTP fetch + HTML extraction with Jsoup |
| `HtmlToMarkdownConverter` | `service/` | HTML string -> Markdown string |
| `ImageDownloaderService` | `service/` | URL download + local storage + deduplication |
| `SyncSettingsView.vue` | `views/admin/` | Config form UI |
| `CsdnApi` (frontend) | `api/` | Axios client for `/api/admin/sync/**` endpoints |
| `syncStore.js` | `stores/` | Pinia store for sync state (optional) |

### Unchanged (v1.0 components that work as-is)

| Component | Reason |
|-----------|--------|
| `ArticleService` | Synced articles use same `createArticle`/`updateArticle` flow |
| `ImageUploadService` (backend) | `ImageDownloaderService` implements same local storage pattern |
| `PublicArticleController` | No changes needed; synced articles appear as normal articles |
| `PublicArticleService` | No changes needed |
| `CommentService` | No changes needed |
| `TagService` / `CategoryService` | CSDN tags mapped to existing tag system |

## Data Flow: CSDN Sync

### Manual Sync Flow

```
[Admin clicks "Sync Now" button]
    ↓
[SyncSettingsView] → [csdnApi.triggerSync()]
    ↓
[CsdnApiController POST /api/admin/sync/trigger]
    ↓
[CsdnSyncService.syncNow()]
    ├→ [CsdnPageParser.fetchArticleList(csdnUserId)]
    │       ↓
    │   [RestClient GET https://blog.csdn.net/{userId}]
    │       ↓
    │   [Jsoup parses HTML, extracts article IDs and URLs]
    │
    ├→ [For each article not in local DB]
    │   │
    │   ├→ [CsdnPageParser.fetchArticle(csdnArticleId)]
    │   │       ↓
    │   │   [RestClient GET article URL]
    │   │   ↓
    │   │   [Jsoup extracts: title, content HTML, tags, publish date]
    │   │
    │   ├→ [HtmlToMarkdownConverter.convert(contentHtml)]
    │   │       ↓
    │   │   [Returns Markdown string]
    │   │
    │   ├→ [ImageDownloaderService.downloadAndReplaceImages(markdown)]
    │   │       ↓
    │   │   [Jsoup extracts <img src="...">]
    │   │   ↓
    │   │   [For each image URL, check URL hash in local store]
    │   │   ↓
    │   │   [If not cached: RestClient download -> save to /uploads/{year}/{month}/{uuid}.{ext}]
    │   │   ↓
    │   │   [Replace original URL with local path in Markdown]
    │   │
    │   └→ [ArticleService.createArticle(articleRequest)]
    │           ↓
    │       [ArticleMapper.insert(article)]
    │           ↓
    │       [TagService.mapTags(csdnTags)] → creates or finds tags
    │
    └→ [CsdnSyncConfigMapper.updateLastSync(time, count)]
            ↓
        [Return sync result: imported N, updated M, skipped K]
            ↓
[CsdnApiController returns SyncResult]
    ↓
[Frontend shows toast: "Synced 5 articles"]
```

### Automatic Scheduled Sync Flow

```
[Spring @Scheduled on cron expression]
    ↓
[Check CsdnSyncConfig.scheduleEnabled == true]
    ↓
[Same flow as manual sync, triggered by scheduler]
    ↓
[No frontend involvement]
```

## Project Structure (v1.1 additions)

```
backend/src/main/java/com/blog/
├── controller/
│   ├── CsdnSyncController.java     # NEW: sync trigger, config, status
│   └── ... (existing)
├── service/
│   ├── CsdnSyncService.java         # NEW: orchestration
│   ├── CsdnPageParser.java          # NEW: HTTP + Jsoup extraction
│   ├── HtmlToMarkdownConverter.java # NEW: HTML -> MD conversion
│   ├── ImageDownloaderService.java   # NEW: download + local storage + dedup
│   └── ... (existing)
├── entity/
│   ├── CsdnSyncConfig.java          # NEW: sync configuration entity
│   └── ... (existing)
├── mapper/
│   ├── CsdnSyncConfigMapper.java    # NEW: MyBatis mapper for config
│   └── ... (existing)
└── dto/
    ├── CsdnSyncConfigDto.java       # NEW: API request/response DTO
    ├── CsdnSyncResultDto.java       # NEW: sync result response
    └── ... (existing)

frontend/src/
├── api/
│   └── csdnApi.js                   # NEW: Axios client for sync endpoints
├── views/admin/
│   └── SyncSettingsView.vue        # NEW: sync config UI
├── stores/
│   └── syncStore.js                # NEW (optional): sync state
├── components/admin/
│   └── SyncStatusWidget.vue        # NEW: dashboard widget
└── router/
    └── index.js                     # MODIFIED: add /admin/sync route
```

## Database Schema Changes (Flyway Migration)

### V2__add_csdn_sync_fields.sql

```sql
-- Articles table: add source tracking
ALTER TABLE articles
  ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT 'Article source: CSDN, MANUAL, etc.',
  ADD COLUMN csdn_article_id VARCHAR(50) DEFAULT NULL COMMENT 'CSDN article ID if source=CSDN',
  ADD UNIQUE INDEX idx_csdn_article_id (csdn_article_id);

-- Sync configuration table
CREATE TABLE csdn_sync_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  csdn_user_id VARCHAR(50) NOT NULL COMMENT 'CSDN user ID to sync from',
  target_category_id BIGINT NOT NULL COMMENT 'Default category for synced articles',
  schedule_enabled BOOLEAN DEFAULT FALSE COMMENT 'Enable automatic sync',
  schedule_cron VARCHAR(50) DEFAULT '0 0 2 * * ?' COMMENT 'Cron expression for auto sync',
  last_sync_at DATETIME DEFAULT NULL COMMENT 'Last sync timestamp',
  last_sync_article_count INT DEFAULT 0 COMMENT 'Articles synced in last run',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_csdn_category FOREIGN KEY (target_category_id) REFERENCES categories(id)
);
```

## Architectural Patterns

### Pattern: External Content Sync (Pull Model)

**What:** Periodically pull content from external platform (CSDN) into local database.
**When to use:** When you control the destination but not the source; source has no webhooks.
**Trade-offs:** Delay between source publish and destination update; rate limiting concerns.

```java
// CsdnSyncService.syncNow()
public SyncResultDto syncNow() {
    String userId = config.getCsdnUserId();
    List<String> articleIds = csdnPageParser.fetchArticleIds(userId);

    int imported = 0, updated = 0, skipped = 0;
    for (String csdnId : articleIds) {
        Article existing = articleMapper.findByCsdnArticleId(csdnId);
        if (existing != null) {
            // Incremental update: check if CSDN article was updated after local sync
            if (isUpdatedOnCSDN(existing, csdnId)) {
                csdnPageParser.updateLocalArticle(existing, csdnId);
                updated++;
            } else {
                skipped++;
            }
        } else {
            // New import
            Article article = csdnPageParser.parseArticle(csdnId);
            articleService.createArticle(toRequest(article));
            imported++;
        }
    }
    return new SyncResultDto(imported, updated, skipped);
}
```

### Pattern: Image Locality via URL Rewriting

**What:** Download external images to local storage, replace src URLs in Markdown.
**When to use:** Articles reference external images that may disappear.
**Trade-offs:** Storage growth; image download can be slow for articles with many images.

```java
// ImageDownloaderService.downloadAndReplaceImages(markdown, baseUrl)
public String downloadAndReplaceImages(String markdown, String articleBaseUrl) {
    Document doc = Jsoup.parse(markdown);
    Elements images = doc.select("img[src]");

    for (Element img : images) {
        String originalUrl = img.attr("src");

        // Deduplication: check URL hash
        String urlHash = md5(originalUrl);
        Path cachedPath = imageCacheDir.resolve(urlHash);
        if (Files.exists(cachedPath)) {
            img.attr("src", "/uploads/" + cachedPath.getFileName());
            continue;
        }

        // Download
        byte[] imageBytes = restClient.get().uri(originalUrl).retrieve().bodyAsBytes();
        String localPath = saveToUploads(imageBytes, originalUrl);
        img.attr("src", localPath);

        // Cache hash for next time
        Files.write(cachedPath, originalUrl.getBytes());
    }
    return doc.body().html();
}
```

### Pattern: Optimistic Sync with Deduplication Key

**What:** Use CSDN article ID as unique key to determine new vs. existing.
**When to use:** When source has stable, unique article identifiers.
**Trade-offs:** If CSDN article ID scheme changes, migration needed.

```java
// In ArticleMapper
Article findByCsdnArticleId(@Param("csdnArticleId") String csdnArticleId);

// In sync logic
Article existing = articleMapper.findByCsdnArticleId(csdnId);
if (existing == null) {
    // INSERT new article with source='CSDN', csdn_article_id=csdnId
} else {
    // Check if update needed (compare updated_at with CSDN publish date)
}
```

## Scalability Considerations

| Scale | Concern | Approach |
|-------|---------|----------|
| 0-50 CSDN articles | No scaling issues | Sync on demand or daily |
| 50-500 CSDN articles | Image download time | Add concurrent image downloads (ThreadPoolTaskExecutor) |
| 500+ CSDN articles | CSDN rate limiting, sync time | Paginated sync, incremental only, or CSDN Open API |

### Scaling Priorities

1. **First concern:** CSDN rate limiting -- add `User-Agent` rotation, respect `robots.txt`, add delays between requests
2. **Second concern:** Image download bottleneck -- use async image downloads with ` CompletableFuture`
3. **Third concern:** Large sync blocking admin operations -- run sync in `@Async` thread pool separate from request thread

## Anti-Patterns

### Anti-Pattern 1: Syncing All Images on Every Sync

**What:** Re-download all images every time even if already cached locally.
**Why bad:** Wastes bandwidth, slows sync dramatically, increases CSDN server load.
**Instead:** URL hash deduplication -- check if image URL was already downloaded before fetching.

### Anti-Pattern 2: Storing CSDN Image URLs Directly

**What:** Leaving `src="https://img-blog.csdn.net/..."` URLs in imported Markdown.
**Why bad:** CSDN images may disappear or block hotlinking; articles look broken.
**Instead:** Always download to local storage; never leave external image URLs in imported content.

### Anti-Pattern 3: Blocking HTTP Requests on Main Thread

**What:** Using synchronous RestClient calls in the request thread for manual sync.
**Why bad:** Admin panel hangs until all CSDN articles are fetched and parsed.
**Instead:** Use `@Async` or `CompletableFuture` for sync operations; return immediately to frontend.

### Anti-Pattern 4: No Edit Warning for Synced Articles

**What:** Editing a CSDN-synced article overwrites local changes on next sync.
**Why bad:** Author edits are lost when scheduled sync re-imports from CSDN.
**Instead:** Show prominent warning when editing synced articles; consider adding `sync_locked` flag.

## Build Order

```
1. Flyway migration V2 (add source columns + create csdn_sync_config table)
   ↓
2. CsdnSyncConfig entity + CsdnSyncConfigMapper
   ↓
3. CsdnSyncController (config + status endpoints only)
   ↓
4. Frontend SyncSettingsView (config form + status display)
   ↓
5. CsdnPageParser (fetch + extract -- test with one article)
   ↓
6. HtmlToMarkdownConverter (HTML -> MD)
   ↓
7. ImageDownloaderService (download + local storage + deduplication)
   ↓
8. CsdnSyncService orchestration (new + update + skip logic)
   ↓
9. CsdnSyncController POST /api/admin/sync/trigger (manual sync)
   ↓
10. Sync result display in frontend
   ↓
11. Spring @Scheduled for automatic sync
```

**Rationale:** Start with data layer (migration + config persistence), then UI, then the complex sync logic last. Each step is testable independently.

## Sources

- Jsoup HTML parsing: https://jsoup.org/
- Spring RestClient: https://docs.spring.io/spring-framework/reference/web/webclient-restclient.html
- Spring Scheduling: https://docs.spring.io/spring-boot/reference/features/scheduling.html
- CSDN Open Platform: **UNVERIFIED** -- open.csdn.net not accessible during research. Verify official API capabilities before implementation.
- CSDN article URL pattern: `https://blog.csdn.net/{userId}/article/details/{articleId}` (observed pattern, not officially documented)

---
*Architecture research for: CSDN Article Sync (v1.1 milestone)*
*Researched: 2026/04/15*

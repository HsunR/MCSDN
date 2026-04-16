# Phase 5: Image Handling & Sync UI - Research

**Researched:** 2026-04-16
**Domain:** CSDN image downloading + admin sync management UI
**Confidence:** HIGH

## Summary

Phase 5 implements two independent capabilities on top of Phase 4's backend sync infrastructure:
1. **Image handling** — backend service that downloads CSDN images to local filesystem, replaces URLs in Markdown, and deduplicates by URL MD5 hash
2. **Sync management UI** — admin sidebar page with config form, sync trigger, and results display; plus blocking modal warning when editing synced articles

The backend image download builds on the existing `UploadController` storage pattern (`/uploads/{year}/{month}/{uuid}.{ext}`). The frontend uses existing Pinia store patterns and Vue Router conventions.

## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Inline alert/panel below button for sync results (no toast, no modal, no navigation)
- **D-02:** Sidebar menu item "CSDN Sync" — dedicated page, not inline in article list
- **D-03:** Blocking modal dialog when editing synced article (`source: CSDN`), must acknowledge before proceeding
- **D-04:** Retry with exponential backoff (1s, 2s, 4s) per failed image, then fall back to placeholder
- **D-05:** Auto-refresh article list after sync completes and results displayed
- Image storage path: `/uploads/{year}/{month}/{uuid}.{ext}` (v1.0 constraint)
- Image deduplication: by URL hash (MD5), already downloaded images not re-downloaded
- Sync runs synchronously (Phase 4 D-05)
- Jsoup 1.18.x for HTML parsing (Phase 4 D-06)
- Spring RestClient for HTTP (Phase 4 D-07)

### Specific Ideas

- "同步" button label in Chinese
- Sync results panel labels: "成功: X | 更新: Y | 跳过: Z | 失败: W"
- Modal has "我已知悉" button

### Deferred Ideas

None — discussion stayed within phase scope.

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SYNC-09 | Image dedup by URL hash (MD5) | Backend `ImageDownloadService` with MD5 cache, phase 4 already stores `source` and `csdnArticleId` on Article |
| SYNC-10 | Sync config form display | `CsdnSyncView.vue` reuses `CsdnSyncConfigRequest` DTO and `CsdnSyncConfigService` from Phase 4 |
| SYNC-11 | Sync trigger button | POST `/api/admin/csdn-sync/sync` already implemented in `CsdnSyncController` |
| SYNC-12 | Sync results display | `SyncResultResponse` DTO already exists with `created/updated/skipped/errors` fields |
| SYNC-13 | Edit warning for synced articles | `ArticleEditorView.vue` already fetches article; check `source` field, show blocking modal |

## Standard Stack

No new dependencies required. Phase 4 already established Jsoup 1.18.x and Spring RestClient. Frontend uses existing Vue 3, Pinia, Tailwind, and Axios.

## Architecture Patterns

### Backend: Image Download Service

```
src/main/java/com/blog/service/
├── ImageDownloadService.java       # Interface
└── impl/ImageDownloadServiceImpl.java  # MD5 dedup, retry, placeholder
```

**Storage flow:**
1. Compute MD5 hash of image URL
2. Check `image_url_hash` column in DB (new) — if exists, skip download
3. If not exists, download via Spring RestClient with retry (3 attempts, exponential backoff)
4. Save to `{upload.path}/{year}/{month}/{uuid}.{ext}` (same as `UploadController`)
5. Record URL hash in `downloaded_images` table (new Flyway V8)

**Interface design:**
```java
// Source: existing pattern from CsdnSyncService.java
public interface ImageDownloadService {
    /**
     * Downloads and replaces CSDN image URLs in Markdown content.
     * Uses MD5 hash of URL for deduplication.
     * @param content Markdown content with CSDN image URLs
     * @return Content with replaced local paths
     */
    String downloadAndReplaceImages(String content);
}
```

### Backend: Image Deduplication Table

New Flyway V8 migration:
```sql
-- V8__downloaded_images.sql
CREATE TABLE downloaded_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_hash VARCHAR(32) NOT NULL UNIQUE COMMENT 'MD5 hash of original URL',
    local_path VARCHAR(500) NOT NULL COMMENT 'Path relative to upload.path',
    original_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_url_hash (url_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Backend: Image Replacement Integration

`CsdnSyncServiceImpl.syncArticles()` must call `imageDownloadService.downloadAndReplaceImages()` on parsed content before storing — update line ~72-73 in `CsdnSyncServiceImpl.java`:
```java
// Current (line 72):
CsdnArticleDto articleDto = csdnArticleParser.parseArticle(html, articleUrl);

// After (add image replacement):
String contentWithLocalImages = imageDownloadService.downloadAndReplaceImages(articleDto.getContent());
articleDto.setContent(contentWithLocalImages);
```

### Backend: Fallback Image Placeholder

For images that fail after 3 retries, replace the URL with a data URI placeholder:
```html
<img src="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 60'><rect fill='%23dc2626' width='400' height='60'/><text x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='white' font-family='sans-serif' font-size='14'>Image unavailable</text></svg>" alt="Image unavailable">
```
Or in Markdown: `![Image unavailable](data:image/svg+xml,...)` — though markdown-it may not render data URI images. Better approach: keep original URL but log failure, or show a clearly broken image reference.

**Recommendation:** Keep original CSDN URL for failed images (so content still renders if CSDN is accessible) and log a warning. Do not inject a data URI SVG.

### Frontend: CsdnSyncView

```
frontend/src/views/admin/CsdnSyncView.vue    # New - sync management page
frontend/src/api/csdnSyncApi.js               # New - frontend API calls
frontend/src/stores/syncStore.js              # Optional - Pinia store for sync state
```

**Layout:** Single admin page with sidebar, card-based form layout matching `DashboardView.vue` and `CategoryManagementView.vue`.

**Structure:**
- Card 1: CSDN userId input + target category dropdown (GET/POST `/api/admin/csdn-sync/config`)
- Card 2: "同步" button + loading state + results panel below button
- Results panel shows: `成功: X | 更新: Y | 跳过: Z | 失败: W` plus error list if any

### Frontend: Route Registration

Add to `frontend/src/router/index.js`:
```javascript
{
  path: '/admin/csdn-sync',
  name: 'CsdnSync',
  component: () => import('../views/admin/CsdnSyncView.vue'),
  meta: { requiresAuth: true }
}
```

### Frontend: AdminSidebar Integration

Add new `router-link` to `AdminSidebar.vue` after Tags entry:
```html
<router-link
  to="/admin/csdn-sync"
  class="block px-4 py-2 mb-1 rounded-lg ..."
  :class="route.path.startsWith('/admin/csdn-sync') ? 'bg-blue-600 text-white' : '...'">
  CSDN 同步
</router-link>
```

### Frontend: ArticleEditorView Modal for Synced Articles

On mount in `ArticleEditorView.vue`, after fetching article:
```javascript
// Add to existing onMounted after article is loaded:
const showSyncWarning = ref(false)
if (isEditing.value && article.value?.source === 'CSDN') {
  showSyncWarning.value = true
}
```

Warning modal uses the same dark theme overlay pattern as `ImageUploadModal.vue`:
```html
<!-- Modal overlay: v-if="showSyncWarning" -->
<!-- Full-screen backdrop: bg-black bg-opacity-50 -->
<!-- Centered card: bg-gray-800 border border-gray-700 -->
<!-- Warning text: "该文章为同步文章，不建议编辑" -->
<!-- "我已知悉" button dismisses modal -->
```

**Important:** The modal is blocking — user must click "我已知悉" to dismiss. No other action available until acknowledged.

### Frontend: Auto-Refresh After Sync

In `CsdnSyncView.vue`, after sync button handler completes:
```javascript
async function handleSync() {
  syncing.value = true
  try {
    const res = await csdnSyncApi.sync()
    syncResult.value = res.data
    // D-05: Auto-refresh article list
    articleStore.fetchArticles()
  } finally {
    syncing.value = false
  }
}
```

## Common Pitfalls

### Pitfall 1: Markdown Image URL Replacement Regex
**What goes wrong:** Naive string replace replaces all instances of a URL, even if it appears in code blocks or links.
**How to avoid:** Only replace image URLs in Markdown format `![alt](url)` — use regex with negative lookbehind to avoid matching inside code fences, or use markdown-it AST parsing.
**Regex pattern:** `!\[([^\]]*)\]\((https?://[^\s)]+\.(?:jpg|jpeg|png|gif|webp)(?:\?[^\)]*)?)\)` — replace the URL part only.

### Pitfall 2: Image Extension Detection from CSDN URLs
**What goes wrong:** CSDN URLs may have query parameters (`?x-oss-process=style/...`) or no extension at all.
**How to avoid:** Default to `.jpg` for URLs with no clear extension, or use `Content-Type` header from HTTP response to determine actual type.

### Pitfall 3: Spring RestClient Timeout on Large Images
**What goes wrong:** Default RestClient timeout may be too short for large images.
**How to avoid:** Configure `setConnectTimeout` and `setReadTimeout` on RestClient — 10s connect, 30s read.

### Pitfall 4: Blocking UI During Sync
**What goes wrong:** Sync is synchronous on backend (D-05 from Phase 4), but if called via Axios without timeout config, frontend may time out.
**How to avoid:** Axios default timeout is 0 (no timeout) — fine since backend runs sync synchronously. Do not set a low timeout.

### Pitfall 5: CSRF Token Not Sent for Multipart/File Download
**What goes wrong:** Not applicable here — image download from CSDN is a server-to-server call, not a browser-initiated upload.

## Code Examples

### Backend: ImageDownloadServiceImpl

```java
// Source: Pattern from CsdnSyncServiceImpl.java + UploadController.java
@Service
public class ImageDownloadServiceImpl implements ImageDownloadService {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private RestClient restClient;

    @Autowired
    private DownloadedImageMapper downloadedImageMapper;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final int MAX_RETRIES = 3;

    @Override
    public String downloadAndReplaceImages(String content) {
        // Find all markdown image URLs: ![alt](url)
        Pattern pattern = Pattern.compile("!\\[([^]]*)\\]\\((https?://[^)]+\\.(?:jpg|jpeg|png|gif|webp)(?:\\?[^)]*)?)\\)");
        Matcher matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group(2);
            String localPath = downloadImage(url);
            matcher.appendReplacement(sb, localPath != null
                ? "![" + matcher.group(1) + "](" + localPath + ")"
                : matcher.group(0)); // keep original on failure
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String downloadImage(String url) {
        String urlHash = computeMd5Hash(url);

        // Check dedup cache
        DownloadedImage existing = downloadedImageMapper.findByUrlHash(urlHash);
        if (existing != null) {
            return existing.getLocalPath();
        }

        // Download with retry
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String localPath = downloadWithBackoff(url, urlHash);
                if (localPath != null) {
                    // Record in DB
                    DownloadedImage record = new DownloadedImage();
                    record.setUrlHash(urlHash);
                    record.setLocalPath(localPath);
                    record.setOriginalUrl(url);
                    downloadedImageMapper.insert(record);
                    return localPath;
                }
            } catch (Exception e) {
                log.warn("Image download attempt {} failed for {}: {}", attempt + 1, url, e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try { Thread.sleep((long) Math.pow(2, attempt) * 1000); } catch (InterruptedException ie) { }
                }
            }
        }

        log.error("All {} retries exhausted for image: {}", MAX_RETRIES, url);
        return null; // caller keeps original URL
    }

    private String downloadWithBackoff(String url, String urlHash) throws IOException {
        byte[] imageBytes = restClient.get()
            .uri(url)
            .retrieve()
            .body(byte[].class);

        if (imageBytes == null || imageBytes.length == 0) return null;

        // Determine extension from Content-Type header or URL
        String ext = inferExtension(url);
        String filename = urlHash + "." + ext;

        LocalDate now = LocalDate.now();
        Path targetDir = Paths.get(uploadPath,
            String.valueOf(now.getYear()),
            String.format("%02d", now.getMonthValue()));
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(filename);
        Files.write(targetPath, imageBytes);

        return "/uploads/" + now.getYear() + "/" +
               String.format("%02d", now.getMonthValue()) + "/" + filename;
    }
}
```

### Frontend: CsdnSyncView.vue

```html
<!-- Source: DashboardView.vue + ArticleListView.vue patterns -->
<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { useArticleStore } from '../../stores/articleStore'
import axios from 'axios'

const articleStore = useArticleStore()
const http = axios.create({ baseURL: '/api' })

const config = ref({ csdnUserId: '', categoryId: null, enabled: true })
const categories = ref([])
const syncing = ref(false)
const syncResult = ref(null)
const syncError = ref('')

async function fetchConfig() { /* GET /api/admin/csdn-sync/config */ }
async function fetchCategories() { /* GET /api/admin/categories */ }
async function saveConfig() { /* POST /api/admin/csdn-sync/config */ }

async function handleSync() {
  syncing.value = true
  syncResult.value = null
  syncError.value = ''
  try {
    const res = await http.post('/api/admin/csdn-sync/sync')
    syncResult.value = res.data
    articleStore.fetchArticles() // D-05: auto-refresh
  } catch (e) {
    syncError.value = e.response?.data?.errors?.[0] || 'Sync failed'
  } finally {
    syncing.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchConfig(), fetchCategories()])
})
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />
    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">CSDN 同步</h1>

      <!-- Config Card -->
      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-6">
        <h2 class="text-lg font-semibold text-gray-100 mb-4">同步配置</h2>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">CSDN User ID</label>
            <input v-model="config.csdnUserId" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100" placeholder="2301_78723800" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">目标分类</label>
            <select v-model="config.categoryId" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100">
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
        </div>
        <button @click="saveConfig" class="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg">保存配置</button>
      </div>

      <!-- Sync Button + Results -->
      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
        <button
          @click="handleSync"
          :disabled="syncing || !config.csdnUserId"
          class="px-6 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-semibold disabled:opacity-50"
        >
          {{ syncing ? '同步中...' : '同步' }}
        </button>

        <!-- D-01: Inline results panel below button -->
        <div v-if="syncResult" class="mt-4 p-4 bg-gray-700 rounded-lg border border-gray-600">
          <div class="text-gray-300">
            成功: <span class="text-green-400 font-bold">{{ syncResult.created }}</span> |
            更新: <span class="text-blue-400 font-bold">{{ syncResult.updated }}</span> |
            跳过: <span class="text-yellow-400 font-bold">{{ syncResult.skipped }}</span> |
            失败: <span class="text-red-400 font-bold">{{ syncResult.errors?.length || 0 }}</span>
          </div>
          <div v-if="syncResult.errors?.length" class="mt-2 text-red-400 text-sm">
            <div v-for="(err, i) in syncResult.errors" :key="i">{{ err }}</div>
          </div>
        </div>

        <div v-if="syncError" class="mt-4 p-4 bg-red-900 bg-opacity-30 border border-red-700 rounded text-red-400">
          {{ syncError }}
        </div>
      </div>
    </main>
  </div>
</template>
```

### Frontend: ArticleEditorView Synced Warning Modal

```javascript
// Add to existing ArticleEditorView.vue script setup
const showSyncWarning = ref(false)

onMounted(async () => {
  await fetchCategories()
  if (isEditing.value) {
    await articleStore.fetchArticle(articleId.value)
    const article = articleStore.currentArticle
    if (article) {
      title.value = article.title
      content.value = article.content
      status.value = article.status
      categoryId.value = article.categoryId
      tags.value = article.tags?.map(t => t.name) || []
      // D-03: Check if synced article
      if (article.source === 'CSDN') {
        showSyncWarning.value = true
      }
    }
  }
})
```

```html
<!-- Add to template after <div class="flex min-h-screen bg-gray-900"> -->
<div v-if="showSyncWarning" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
  <div class="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700">
    <h3 class="text-lg font-semibold text-yellow-400 mb-4">编辑警告</h3>
    <p class="text-gray-300 mb-6">该文章为同步文章，不建议编辑。更改将在下次同步时被覆盖。</p>
    <button
      @click="showSyncWarning = false"
      class="w-full px-4 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded-lg"
    >
      我已知悉
    </button>
  </div>
</div>
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| MD5 hashing | Custom hash utility | `java.security.MessageDigest` | Already in JDK, trivial to use |
| Image MIME detection | File extension parsing | HTTP Content-Type header | More reliable than URL extension |
| Exponential backoff | Custom sleep loop | `Thread.sleep` in retry loop | Simple enough, no library needed |
| Vue modal overlay | Custom component | Existing `ImageUploadModal.vue` pattern | Already in codebase |

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | CSDN images use standard image extensions (jpg/png/gif/webp) in URL | Backend image replacement | May miss some image formats — can add extension check |
| A2 | `RestClient` in Phase 4 is configured with timeouts | Backend image download | RestClient created without explicit timeout config — may hang on slow images |
| A3 | `source` field on Article entity is set to `"CSDN"` (uppercase) | Frontend ArticleEditorView | Checks `article.source === 'CSDN'` — must match value set in CsdnSyncServiceImpl line 29 |

**If A2 is wrong:** RestClient may hang indefinitely on slow CSDN image hosts. Should add `setConnectTimeout(10_000)` and `setReadTimeout(30_000)`.

## Open Questions

1. **Failed image strategy**: Should we keep the original CSDN URL for failed images (content still renders if CSDN accessible) or replace with a placeholder that clearly indicates failure?
   - Recommendation: Keep original URL. Log failure. Show warning in sync results if any images failed.

2. **Image download during article update**: Should `updateContentAndHash` also re-download images if content changed? Currently it only updates content and hash.
   - Recommendation: Yes — when content changes, re-run image download on new content. Existing images already cached by MD5 dedup.

3. **Image dedup table existence check**: Should `downloadImage` use a DB query or in-memory cache?
   - Recommendation: DB query via `DownloadedImageMapper` — images persist across restarts, avoids re-downloading on restart.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None detected — no test framework in project |
| Config file | N/A |
| Quick run command | N/A |
| Full suite command | N/A |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Command | File Exists? |
|--------|----------|-----------|---------|--------------|
| SYNC-09 | MD5 dedup skips already-downloaded images | Manual | Run sync twice, verify second sync skips image downloads | N/A |
| SYNC-10 | Config form pre-fills and saves correctly | Manual | Fill form, reload, verify values persist | N/A |
| SYNC-11 | Sync button triggers POST and shows loading | Manual | Click button, verify spinner and /sync endpoint called | N/A |
| SYNC-12 | Results panel shows correct counts | Manual | Run sync with known article set, verify counts | N/A |
| SYNC-13 | Modal blocks entry for CSDN-sourced article | Manual | Edit article with `source: CSDN`, verify modal appears | N/A |

**Wave 0 Gaps:** No test infrastructure exists in project. All testing is manual. Consider adding a test framework (JUnit + MockMvc for backend, Vitest for frontend) in a future phase.

## Sources

### Primary (HIGH confidence)
- `backend/src/main/java/com/blog/service/impl/CsdnSyncServiceImpl.java` — existing sync logic, source field value
- `backend/src/main/java/com/blog/controller/CsdnSyncController.java` — existing API endpoints
- `backend/src/main/java/com/blog/dto/SyncResultResponse.java` — response DTO structure
- `backend/src/main/java/com/blog/controller/UploadController.java` — existing image storage pattern
- `backend/src/main/resources/application.yml` — upload path config
- `frontend/src/views/admin/DashboardView.vue` — admin page structure pattern
- `frontend/src/views/admin/ArticleEditorView.vue` — modal and editor pattern
- `frontend/src/components/admin/ImageUploadModal.vue` — modal overlay pattern
- `frontend/src/components/admin/AdminSidebar.vue` — sidebar navigation pattern
- `frontend/src/stores/articleStore.js` — Pinia store pattern
- `frontend/src/router/index.js` — route registration pattern

### Secondary (MEDIUM confidence)
- Spring Boot documentation for `RestClient` timeout configuration
- markdown-it image syntax regex patterns (standard Markdown spec)

### Tertiary (LOW confidence)
- CSDN image URL format — may vary, regex may need adjustment based on real URLs

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies, all patterns from existing codebase
- Architecture: HIGH — patterns clearly established in existing code
- Pitfalls: MEDIUM — regex and retry logic are standard, but CSDN URL format unverified

**Research date:** 2026-04-16
**Valid until:** 2026-05-16 (30 days — technology stack is stable)

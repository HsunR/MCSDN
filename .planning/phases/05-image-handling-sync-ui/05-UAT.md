---
status: partial
phase: 05-image-handling-sync-ui
source:
  - 05-01-SUMMARY.md
  - 05-02-SUMMARY.md
  - 05-03-SUMMARY.md
  - 05-04-SUMMARY.md
  - 05-05-SUMMARY.md
started: 2026-04-16T04:30:00Z
updated: 2026-04-16T04:45:00Z
---

## Current Test

[testing paused — gap diagnosed, fix plan needed]

## Tests

### 1. CSDN Sync Page Navigation
expected: |
  Open http://localhost:5173/admin/csdn-sync (or navigate via sidebar).
  You should see the CSDN sync management page with:
  - A heading or title labeled "CSDN 同步"
  - A form with a CSDN user ID text input field
  - A category dropdown/select field
  - A "保存配置" (Save Config) button
  - A prominent "同步" or "开始同步" (Start Sync) button
  The page should use a dark theme (bg-gray-900) consistent with the admin panel.
result: pass

### 2. CSDN Sync Sidebar Nav Item
expected: |
  Clicking the "CSDN 同步" nav item in the admin sidebar navigates to
  /admin/csdn-sync and the nav item shows an active/highlighted state
  (e.g., bg-blue-600) when on that route.
result: pass

### 3. Save Sync Config
expected: |
  Enter a CSDN user ID and select a category, then click "保存配置" (Save Config).
  The save operation completes without error and the configured values are
  retained when you reload the page or re-open the form.
result: pass

### 4. Trigger Sync with Loading State
expected: |
  With valid config saved, click the sync button ("同步" or "开始同步").
  The button text changes to "同步中..." and appears disabled (grayed out or
  showing a spinner/loading indicator) while the sync operation is in progress.
result: pass

### 5. Sync Returns Non-Zero Counts
expected: |
  After sync completes, the results panel shows non-zero counts:
  - created (green), updated (yellow), skipped (gray), error (red)
  At least one of these should be non-zero if there are articles to sync.
  If all are 0, it means no articles were fetched from CSDN.
  The response body should show: {"created":N, "updated":N, "skipped":N, "errors":[]}
  where at least one of N > 0.
result: issue
reported: "仍然无法正常同步文章，返回200但所有计数都是0"
severity: blocker

### 6. Article List Auto-Refresh
expected: |
  After a successful sync (created > 0 or updated > 0), the article list in
  the admin panel automatically updates to show any new or modified articles
  from CSDN — without requiring a manual page refresh or reload.
result: blocked
blocked_by: prior-phase
reason: "同步无法获取文章，无法测试自动刷新"

### 7. CSDN Article Warning Modal
expected: |
  Open an existing article that was synced from CSDN (source = "CSDN") in the
  Article Editor (/admin/article/edit/{id}). Before the editor is accessible,
  a blocking modal appears with a warning message. The modal has an
  acknowledgment button labeled "我已知悉" (or similar). Clicking the button
  dismisses the modal and reveals the article editor beneath it.
result: blocked
blocked_by: prior-phase
reason: "同步无法获取文章，无法测试此功能"

### 8. Image Download in Sync
expected: |
  When syncing an article that contains images hosted on CSDN (external image URLs
  in the markdown), those images are downloaded to the local /uploads/ directory
  and the article content is updated to reference the local paths instead of
  the original CSDN URLs. You can verify this by checking the article markdown
  content — CSDN image URLs should be replaced with /uploads/... paths.
result: blocked
blocked_by: prior-phase
reason: "同步无法获取文章，无法测试图片下载"

## Summary

total: 8
passed: 4
issues: 1
pending: 0
skipped: 0
blocked: 3

## Gaps

- truth: "Sync operation returns non-zero counts when articles are fetched"
  status: failed
  reason: "User reported: 仍然无法正常同步文章，返回200但所有计数都是0"
  severity: blocker
  test: 5
  root_cause: "CsdnArticleFetcherImpl.fetchArticleList() uses HTML scraping on CSDN blog listing page (https://blog.csdn.net/{userId}/article/list/) which no longer works — CSDN changed their page structure and the article links are not rendered in the HTML anymore. Reference repo (CSDNBlogBackup) successfully uses a JSON API: https://blog.csdn.net/community/home-api/v1/get-business-list with params: username, page, size, businessType=blog, orderby=createtime, noHtml=1"
  artifacts:
    - path: "backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java"
      issue: "fetchArticleList() scrapes HTML from /article/list/ page which returns empty article list"
  missing:
    - "Replace HTML scraping with JSON API call to /community/home-api/v1/get-business-list"
    - "Parse JSON response to extract article URLs"
    - "Handle pagination if API returns paginated results"
  debug_session: ""

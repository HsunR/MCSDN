---
status: diagnosed
phase: 05-image-handling-sync-ui
source:
  - 05-01-SUMMARY.md
  - 05-02-SUMMARY.md
  - 05-03-SUMMARY.md
  - 05-04-SUMMARY.md
started: 2026-04-16T00:00:00Z
updated: 2026-04-16T00:00:00Z
---

## Current Test

[testing complete]

## Tests

### 1. CSDN Sync Page Navigation
expected: Open http://localhost:5173/admin/csdn-sync (or navigate via sidebar). You should see the CSDN sync management page with: - A heading or title labeled "CSDN 同步" - A form with a CSDN user ID text input field - A category dropdown/select field - A "保存配置" (Save Config) button - A prominent "同步" or "开始同步" (Start Sync) button The page should use a dark theme (bg-gray-900) consistent with the admin panel.
result: pass

### 2. CSDN Sync Sidebar Nav Item
expected: Clicking the "CSDN 同步" nav item in the admin sidebar navigates to /admin/csdn-sync and the nav item shows an active/highlighted state (e.g., bg-blue-600) when on that route.
result: pass

### 3. Save Sync Config
expected: Enter a CSDN user ID and select a category, then click "保存配置" (Save Config). The save operation completes without error and the configured values are retained when you reload the page or re-open the form.
result: pass

### 4. Trigger Sync with Loading State
expected: With valid config saved, click the sync button ("同步" or "开始同步"). The button text changes to "同步中..." and appears disabled (grayed out or showing a spinner/loading indicator) while the sync operation is in progress.
result: pass

### 5. Sync Results Display
expected: After sync completes, an inline results panel appears showing color-coded counts for each result type: green text for success count, yellow/amber for update count, gray for skip count, and red for error count. The counts reflect the actual outcome of the sync operation.
result: issue
reported: "返回200但所有计数都是0: {\"created\":0,\"updated\":0,\"skipped\":0,\"errors\":[]} 无法正常同步"
severity: major

### 6. Article List Auto-Refresh
expected: After a successful sync, the article list in the admin panel (or the page you navigated from) automatically updates to show any new or modified articles from CSDN — without requiring a manual page refresh or reload.
result: pass

### 7. CSDN Article Warning Modal
expected: Open an existing article that was synced from CSDN (source = "CSDN") in the Article Editor (/admin/article/edit/{id}). Before the editor is accessible, a blocking modal appears with a warning message. The modal has an acknowledgment button labeled "我已知悉" (or similar). Clicking the button dismisses the modal and reveals the article editor beneath it.
result: blocked
blocked_by: prior-phase
reason: "无法同步文章，因此无法测试此功能"

## Summary

total: 7
passed: 4
issues: 1
pending: 0
skipped: 0
blocked: 1

## Gaps

- truth: "After sync completes, an inline results panel appears showing color-coded counts for each result type: green text for success count, yellow/amber for update count, gray for skip count, and red for error count. The counts reflect the actual outcome of the sync operation."
  status: failed
  reason: "User reported: 返回200但所有计数都是0: {\"created\":0,\"updated\":0,\"skipped\":0,\"errors\":[]} 无法正常同步"
  severity: major
  test: 5
  root_cause: "CsdnArticleFetcherImpl uses @Autowired RestClient.Builder but no RestClient.Builder bean is defined in Spring context. restClientBuilder is null, causing NPE on every fetchHtml() call. All article fetches fail silently in the continue-on-error loop, resulting in empty article list and all-zero counts."
  artifacts:
    - path: "backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java"
      issue: "RestClient.Builder restClientBuilder is null — no such bean exists"
    - path: "backend/src/main/java/com/blog/config/WebConfig.java"
      issue: "Missing RestClient.Builder bean definition"
  missing:
    - "Add RestClient.Builder bean to Spring config (WebConfig or RestClientConfig)"
    - "OR change CsdnArticleFetcherImpl to use RestClient.create() instead of injection"
  debug_session: ".planning/debug/csdn-sync-returns-all-zeros.md"

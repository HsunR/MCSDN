---
phase: '05'
plan: '01'
subsystem: backend-image-download
tags: [image-download, dedup, retry, flyway, mybatis]
dependency_graph:
  requires: []
  provides: [ImageDownloadService, downloaded_images-table, DownloadedImageMapper]
  affects: [CsdnSyncServiceImpl]
tech_stack:
  added: [Spring RestClient, SimpleClientHttpRequestFactory, MessageDigest MD5, HexFormat]
  patterns: [MD5-URL-dedup, exponential-backoff-retry, SSRF-scheme-validation]
key_files:
  created:
    - backend/src/main/resources/db/migration/V8__downloaded_images.sql
    - backend/src/main/java/com/blog/entity/DownloadedImage.java
    - backend/src/main/java/com/blog/mapper/DownloadedImageMapper.java
    - backend/src/main/resources/mapper/DownloadedImageMapper.xml
    - backend/src/main/java/com/blog/service/ImageDownloadService.java
    - backend/src/main/java/com/blog/service/impl/ImageDownloadServiceImpl.java
  modified: []
key_decisions:
  - MD5 of URL (not content) used as dedup key — stable across re-downloads, URL is the identity
  - Backoff formula is 2^(attempt-1) giving sleeps of 1s/2s/4s before retries 1/2/3 (4 total attempts)
  - Extension inferred from Content-Type header first, URL extension second, default jpg
  - RestClient configured per-instance via PostConstruct to set timeouts on SimpleClientHttpRequestFactory
metrics:
  duration: 3 min
  completed_date: '2026-04-16'
  tasks_completed: 4
  files_created: 6
  files_modified: 0
---

# Phase 05 Plan 01: Image Download Infrastructure Summary

**One-liner:** Backend image download service with MD5 URL deduplication, exponential backoff retry, and SSRF mitigation for CSDN image localization.

## What Was Built

Created the complete backend image download infrastructure needed to localize CSDN images into the blog's local filesystem:

1. **Flyway V8 migration** — `downloaded_images` table with `url_hash UNIQUE` index for deduplication.
2. **DownloadedImage entity** — Plain POJO following `CsdnSyncConfig` pattern; MyBatis `map-underscore-to-camel-case` handles column mapping.
3. **DownloadedImageMapper** — Interface with `findByUrlHash` and `insert`; XML with explicit `DownloadedImageResultMap`.
4. **ImageDownloadService** — Interface defining `downloadAndReplaceImages(String content)`.
5. **ImageDownloadServiceImpl** — Full implementation with:
   - Markdown image regex: `!\[...\](https?://...\.{ext})`
   - MD5 hash of URL for dedup key (server-computed, not user-supplied)
   - DB cache hit check via `findByUrlHash` before any HTTP call
   - `RestClient` with 10s connect / 30s read timeout via `SimpleClientHttpRequestFactory`
   - Retry loop: 4 total attempts (initial + 3 retries), sleeps 1s / 2s / 4s before retries
   - Saves to `{uploadPath}/{year}/{month}/{urlHash}.{ext}` using `Files.write()`
   - Returns `/uploads/{year}/{month}/{urlHash}.{ext}` as the replacement path
   - Logs error and returns `null` (original URL preserved) on all retries exhausted

## Tasks Completed

| Task | Description | Commit |
|------|-------------|--------|
| 1 | Flyway V8 migration: downloaded_images table | d15bc6e |
| 2 | DownloadedImage entity | b783529 |
| 3 | DownloadedImageMapper interface + XML | 79479e9 |
| 4 | ImageDownloadService interface + implementation | b3a073a |

## Security Controls Applied

All threat model mitigations from the plan were implemented:

| Threat ID | Mitigation | Implementation |
|-----------|-----------|----------------|
| T-05-01 | SSRF: URL scheme validation | `startsWith("http://") || startsWith("https://")` before any processing |
| T-05-02 | Path traversal: server-controlled filename | Filename = MD5 hash, path = config + year + month (no user input) |
| T-05-03 | SQL injection: parameterized queries | `#{urlHash}` in `DownloadedImageMapper.xml` |
| T-05-05 | DoS: HTTP timeouts | `setConnectTimeout(10_000)` + `setReadTimeout(30_000)` |

## Deviations from Plan

None - plan executed exactly as written.

## Known Stubs

None - all code paths are fully wired. The service is not called by `CsdnSyncServiceImpl` yet (that is plan 05-02's responsibility).

## Threat Flags

None — no new security surface introduced beyond what was planned in the threat model.

## Self-Check: PASSED

Files created:
- FOUND: backend/src/main/resources/db/migration/V8__downloaded_images.sql
- FOUND: backend/src/main/java/com/blog/entity/DownloadedImage.java
- FOUND: backend/src/main/java/com/blog/mapper/DownloadedImageMapper.java
- FOUND: backend/src/main/resources/mapper/DownloadedImageMapper.xml
- FOUND: backend/src/main/java/com/blog/service/ImageDownloadService.java
- FOUND: backend/src/main/java/com/blog/service/impl/ImageDownloadServiceImpl.java

Commits verified:
- d15bc6e: chore(05-01): add Flyway V8 migration for downloaded_images table
- b783529: feat(05-01): add DownloadedImage entity
- 79479e9: feat(05-01): add DownloadedImageMapper interface and XML
- b3a073a: feat(05-01): add ImageDownloadService with MD5 dedup and retry backoff

Maven compile: PASSED (mvn compile -q returned no errors)

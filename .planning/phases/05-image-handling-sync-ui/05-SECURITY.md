# Phase 05 Security Verification

**Phase:** 05 — image-handling-sync-ui
**ASVS Level:** 2
**block_on:** open

---

## Threat Verification

### Closed (10/10)

| Threat ID | Category | Disposition | Evidence |
|-----------|----------|-------------|----------|
| T-05-01 | S | mitigate | ImageDownloadServiceImpl.java:88 — `startsWith("http://") \|\| startsWith("https://")` |
| T-05-02 | T | mitigate | ImageDownloadServiceImpl.java:151-152 — `Paths.get(uploadPath, year, month, filename)` with MD5 hash filename (no user input) |
| T-05-03 | D | mitigate | DownloadedImageMapper.xml:13 — `WHERE url_hash = #{urlHash}` (parameterized) |
| T-05-04 | I | accept | RestClient download writes content to disk with server-controlled extension inferred from Content-Type header |
| T-05-05 | R | mitigate | ImageDownloadServiceImpl.java:54-55 — `setConnectTimeout(10_000)` + `setReadTimeout(30_000)` |
| T-05-06 | S | mitigate | Deferred to T-05-01; URL scheme validation applies to all image downloads including CSDN sync |
| T-05-07 | D | accept | Spring @Autowired fail-fast — container fails at startup if ImageDownloadService dependency is unavailable |
| T-05-08 | S | accept | CsdnSyncView.vue calls JWT-protected /api/admin/** backend endpoint; JWT validation handled by existing SecurityConfig |
| T-05-09 | R | accept | Sync button triggers non-destructive read from CSDN; no data deleted by sync operation |
| T-05-10 | I | accept | ArticleEditorView.vue blocking modal is UX warning only per D-03; does not prevent edit if user bypasses modal (non-security control) |

---

## Accepted Risks Log

| Threat ID | Rationale |
|-----------|-----------|
| T-05-04 | External image content written to disk with server-controlled extension; Content-Type header determines actual format; no user-supplied extension used |
| T-05-07 | Spring @Autowired fail-fast behavior ensures missing dependency is caught at application startup, not at runtime |
| T-05-08 | Existing JWT authentication on /api/admin/** endpoints provides access control; no new auth mechanism introduced |
| T-05-09 | CSDN sync is a read operation from external source; no destructive operations performed |
| T-05-10 | Modal is UX advisory (D-03) for CSDN-synced articles; not an access control mechanism; backend API enforces JWT auth for article updates |

---

## Unregistered Flags

None — no new attack surface detected during implementation.

---

## Summary

**Threats Closed:** 10/10
**ASVS Level:** 2
**Result:** SECURED

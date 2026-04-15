# Phase 4: Core Sync Infrastructure - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-15
**Phase:** 04-core-sync-infrastructure
**Areas discussed:** Article List Fetching, Update Detection, Error Handling, Sync Response

---

## Article List Fetching

| Option | Description | Selected |
|--------|-------------|----------|
| Hybrid: API first, scrape fallback | Try official CSDN Open Platform API first — if it fails or isn't available, fall back to HTML scraping | |
| HTML scraping only | Parse CSDN blog page directly with Jsoup. Simpler but fragile if CSDN changes HTML structure. | ✓ |
| Official CSDN API only | Use CSDN Open Platform API exclusively. Most reliable if API is stable, but may require authentication tokens. | |

**User's choice:** HTML scraping only
**Notes:** Prefer simplicity over robustness. Accepts risk of CSDN HTML changes.

---

## Selector Robustness

| Option | Description | Selected |
|--------|-------------|----------|
| CSS selectors with fallbacks | Use CSS selectors to find article links, but include fallback selectors in case CSDN changes their HTML. More robust. | ✓ |
| Exact attribute matching only | Match exact HTML attributes (id, data-*). Faster but very fragile. | |

**User's choice:** CSS selectors with fallbacks
**Notes:** Balances simplicity with some resilience.

---

## Update Detection

| Option | Description | Selected |
|--------|-------------|----------|
| Content hash comparison (MD5) | Store MD5 hash of CSDN content when syncing. On re-sync, compare hashes — if different, update. Most reliable. | ✓ |
| Always overwrite on match | If csdn_article_id matches, always import fresh from CSDN. Simplest. | |
| CSDN updatedAt timestamp | Only update if remote updatedAt > local updatedAt. Fast but relies on CSDN timestamps. | |

**User's choice:** Content hash comparison (MD5)
**Notes:** Most reliable approach for detecting actual content changes.

---

## Error Handling

| Option | Description | Selected |
|--------|-------------|----------|
| Continue + summary | Skip individual article failures, log them, continue processing remaining articles. Return final counts at end. | ✓ |
| Fail fast with rollback | Stop immediately on any failure. Roll back any DB changes. | |
| Retry 3 times then skip | Retry each failing article up to 3 times with exponential backoff. | |

**User's choice:** Continue + summary
**Notes:** Resilient — one bad article shouldn't block entire sync.

---

## Sync Response

| Option | Description | Selected |
|--------|-------------|----------|
| Immediate summary | Run sync synchronously, return immediately with counts. Simple and immediate feedback. | ✓ |
| Async job with status | Return job ID immediately, process in background. Admin polls or waits for callback. | |
| Store history in database | Save each sync run to a csdn_sync_history table. | |

**User's choice:** Immediate synchronous response
**Notes:** Keep it simple for v1 — no need for async complexity yet.

---

## Claude's Discretion

- Jsoup version (1.18.x) — already decided in STATE.md
- Spring RestClient — already decided in STATE.md
- Specific CSS selector paths — will research and determine during implementation
- Fallback selector strategy — will define during implementation


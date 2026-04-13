---
phase: 02
slug: public-blog-image-upload
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-13
---

# Phase 02 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Playwright (existing from Phase 1 E2E tests) |
| **Config file** | frontend/playwright.config.js |
| **Quick run command** | `cd frontend && npx playwright test --grep "PUBL\|CTGY\|IMGE" --reporter=line` |
| **Full suite command** | `cd frontend && npx playwright test --reporter=line` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run quick run command for affected features
- **After every plan wave:** Run full suite for the wave
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | PUBL-01/Paginated article list | T-02-02 | page >= 1 enforced | e2e | `playwright test --grep "PUBL-01"` | ✅ | ⬜ pending |
| 02-01-02 | 01 | 1 | PUBL-02/Article detail | T-02-03 | 404 for drafts | e2e | `playwright test --grep "PUBL-02"` | ✅ | ⬜ pending |
| 02-01-03 | 01 | 1 | PUBL-03/Category filter | T-02-01 | MyBatis #{ } param | e2e | `playwright test --grep "PUBL-03"` | ✅ | ⬜ pending |
| 02-01-04 | 01 | 1 | PUBL-04/Tag filter | T-02-01 | MyBatis #{ } param | e2e | `playwright test --grep "PUBL-04"` | ✅ | ⬜ pending |
| 02-01-05 | 01 | 1 | PUBL-05/Search FULLTEXT | T-02-01 | MyBatis #{ } param | e2e | `playwright test --grep "PUBL-05"` | ✅ | ⬜ pending |
| 02-02-01 | 02 | 2 | CTGY-01/Category CRUD | T-02-07 | UNIQUE + countByName | e2e | `playwright test --grep "CTGY-01"` | ✅ | ⬜ pending |
| 02-02-02 | 02 | 2 | CTGY-02/Tag CRUD | T-02-08 | UNIQUE + countByName | e2e | `playwright test --grep "CTGY-02"` | ✅ | ⬜ pending |
| 02-02-03 | 02 | 2 | IMGE-01/Upload endpoint | T-02-04/05/06 | Content type whitelist | e2e | `playwright test --grep "IMGE-01"` | ✅ | ⬜ pending |
| 02-03-01 | 03 | 3 | PUBL-01/Timeline layout | — | N/A | e2e | `playwright test --grep "PUBL-01"` | ✅ | ⬜ pending |
| 02-03-02 | 03 | 3 | D-01/Date dividers | — | N/A | e2e | visual check | N/A | ⬜ pending |
| 02-03-03 | 03 | 3 | D-03/Article route | — | N/A | e2e | `playwright test --grep "PUBL-02"` | ✅ | ⬜ pending |
| 02-03-04 | 03 | 3 | IMGE-04/URL insert | — | N/A | e2e | `playwright test --grep "IMGE-04"` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `frontend/e2e/blog.spec.js` — existing Playwright test file covers all phase requirements
- [ ] `frontend/playwright.config.js` — existing Playwright config (headed: false)
- [ ] `npm install striptags@3.2.0` — for excerpt generation in publicArticleStore

*No Wave 0 gaps — existing Playwright E2E infrastructure covers all phase requirements.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Date divider visual grouping | D-01 | Visual layout requires human confirmation | Run frontend, navigate to public blog, verify articles grouped by date with dividers |
| Category/tag slug URL matching | D-04/D-05 | URL slug ↔ DB name case sensitivity | Create category "Backend Dev", verify #/category/backend-dev works |

*All other phase behaviors have automated E2E verification.*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending

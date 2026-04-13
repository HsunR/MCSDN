---
phase: 03
slug: comments
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-13
---

# Phase 03 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 (backend) + Vitest (frontend) |
| **Config file** | backend/src/test/resources/application.yml |
| **Quick run command (backend)** | `cd backend && mvn test -Dtest=CommentServiceTest,CommentMapperTest` |
| **Quick run command (frontend)** | `cd frontend && npx vitest run --grep "Comment"` |
| **Full suite command** | `cd backend && mvn test && cd ../frontend && npx vitest run` |
| **Estimated runtime** | ~60 seconds |

---

## Sampling Rate

- **After every task commit:** Run relevant test subset (backend unit tests)
- **After every plan wave:** Full suite
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 03-01-01 | 01 | 1 | CMNT-02 | T-03-03 | Status defaults to PENDING | unit | `mvn test -Dtest=CommentMapperTest` | ❌ W0 | ⬜ pending |
| 03-01-02 | 01 | 1 | CMNT-01 | T-03-01 | Honeypot spam check | unit | `mvn test -Dtest=CommentServiceTest` | ❌ W0 | ⬜ pending |
| 03-02-01 | 02 | 2 | CMNT-05 | T-03-03 | Only APPROVED returned | unit | `mvn test -Dtest=CommentServiceTest` | ❌ W0 | ⬜ pending |
| 03-02-02 | 02 | 2 | CMNT-03 | T-03-02 | Admin approve/reject | unit | `mvn test -Dtest=AdminCommentControllerTest` | ❌ W0 | ⬜ pending |
| 03-02-03 | 02 | 2 | CMNT-04 | T-03-02 | Admin delete | unit | `mvn test -Dtest=AdminCommentControllerTest` | ❌ W0 | ⬜ pending |
| 03-03-01 | 03 | 3 | CMNT-01 | T-03-01 | Public form + honeypot | e2e | `npx playwright test --grep "CMNT-01"` | ❌ W0 | ⬜ pending |
| 03-03-02 | 03 | 3 | CMNT-05 | T-03-03 | Approved comments display | e2e | `npx playwright test --grep "CMNT-05"` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `backend/src/test/java/com/blog/service/CommentServiceTest.java` — covers CMNT-01 (honeypot), CMNT-02, CMNT-05
- [ ] `backend/src/test/java/com/blog/controller/AdminCommentControllerTest.java` — covers CMNT-03, CMNT-04
- [ ] `backend/src/test/java/com/blog/mapper/CommentMapperTest.java` — covers CMNT-02
- [ ] `backend/src/test/java/com/blog/dto/CommentRequestTest.java` — covers input validation
- [ ] `frontend/src/__tests__/CommentSection.spec.js` — covers CMNT-01, CMNT-05
- [ ] `backend/src/test/resources/application.yml` — H2 test datasource config
- [ ] H2 dependency in backend/pom.xml (test scope)

*Wave 0 tests need to be written before execution begins.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Honeypot blocks bots | CMNT-01 | Requires bot simulation | Submit form with bot-filled honeypot, verify success returned but comment NOT stored |
| Admin badge shows pending count | CMNT-03 | Visual UI state | Navigate to admin dashboard, verify "Comments" badge shows correct pending count |

*All other phase behaviors have automated E2E/unit verification.*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending

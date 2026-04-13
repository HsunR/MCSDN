# Phase 3: Comments - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the analysis.

**Date:** 2026-04-13
**Phase:** 03-comments
**Mode:** auto (assumptions mode — codebase-first analysis)

---

## Auto-Resolved Decisions

All decisions below were auto-resolved in `--auto` mode via assumptions-first analysis.

| Decision | Auto-Selected | Rationale |
|----------|-------------|-----------|
| D-01: Comment form position | Below article content | Standard blog pattern; natural flow |
| D-02: Form fields | name + content only | v1 MVP — no email/URL overhead |
| D-03: Submit UX | Inline success/error messages | Clean, no modal interruption |
| D-04: Comment display | Flat list | Threading deferred to future phase |
| D-05: Comment card fields | name + date + content | Minimal sufficient |
| D-06: Comment order | Oldest first | Standard blog convention |
| D-07: Admin management | Dedicated /admin/comments page | Clean separation from articles |
| D-08: Admin filter | Status tabs (PENDING/APPROVED/REJECTED) | Clear, actionable |
| D-09: Admin actions | Approve/Reject/Delete | Per CMNT-03, CMNT-04 |
| D-10: Comment model | id, article_id, author_name, content, status, created_at | Minimal for v1 |
| D-11: Status default | PENDING | Already locked in STATE.md |
| D-12: Spam protection | Honeypot field | Zero-friction for real users |
| D-13: Captcha | None in v1 | Keeps friction low |
| D-14: Public POST | /api/articles/{id}/comments | RESTful, clear |
| D-15: Public GET | /api/articles/{id}/comments (approved only) | CMNT-05 |
| D-16: Admin list | /api/admin/comments with filter | CMNT-03 |
| D-17: Admin approve | PATCH /api/admin/comments/{id}/approve | CMNT-03 |
| D-18: Admin reject | PATCH /api/admin/comments/{id}/reject | CMNT-03 |
| D-19: Admin delete | DELETE /api/admin/comments/{id} | CMNT-04 |
| D-20: Admin route | #/admin/comments | Consistent with other admin routes |
| D-21: Public integration | ArticleView below content | D-01 |
| D-22: ArticleView slot | Below prose div | Natural location |
| D-23: Admin nav | Comments link with pending badge | Dashboard pattern |
| D-24: Store method | publicArticleStore.fetchComments() | Existing pattern |
| D-25: Service layer | CommentService | Standard layered architecture |

## Gray Areas Not Applicable to Phase 3

These topics were considered but belong to other phases:
- Email notifications → Phase 4 (NOTF-01, NOTF-02 in REQUIREMENTS.md deferred)
- Threaded comments → future phase
- Comment reactions/votes → future phase
- Anti-spam service → Phase 4+

---

*Phase: 03-comments*
*Discussion mode: auto (assumptions)*

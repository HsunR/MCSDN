# Phase 2: Public Blog & Image Upload - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-13
**Phase:** 02-public-blog-image-upload
**Areas discussed:** Article list layout, Public URL structure, Search implementation

---

## Article List Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Card grid (2-3 columns) | Modern blog style, visual cards with title/excerpt/date | |
| Single-column list | Dense, text-focused like HN — simpler | |
| Timeline with date dividers | Articles grouped by date header | ✓ |

**User's choice:** Timeline with date dividers
**Notes:** Grouped by date — chronological feel for the blog

| Option | Description | Selected |
|--------|-------------|----------|
| Title + date + category badge only | Minimal — title prominent | |
| Title + excerpt (2 lines) + date + category | Medium density | ✓ |
| Title + excerpt + tags + date + category | Full metadata | |

**User's choice:** Title + excerpt (2 lines) + tags + date + category
**Notes:** Most informative, shows tags for filtering context

---

## Public URL Structure

| Option | Description | Selected |
|--------|-------------|----------|
| ID only (#/article/:id) | Simple, always works | |
| ID + slug (#/article/:id-title) | More readable URLs | ✓ |

**User's choice:** ID + slug
**Notes:** SEO-friendly, more readable

| Option | Description | Selected |
|--------|-------------|----------|
| ID-based (#/category/1, #/tag/5) | Simple, no collision risk | |
| Name-based (#/category/python, #/tag/backend) | More readable, slugified | ✓ |

**User's choice:** Name-based slugs
**Notes:** More readable URLs

---

## Search Implementation

| Option | Description | Selected |
|--------|-------------|----------|
| MySQL FULLTEXT (natural language) | Fast, proper ranking — but 4-char min word length | ✓ |
| LIKE %keyword% | Simple, works for any term | |
| Elasticsearch | More capable, but adds significant Phase 2 scope | deferred |

**User's choice:** MySQL FULLTEXT
**Notes:** Kept within constraints (local deploy, MySQL-only). Elasticsearch noted for Phase 4 backlog.

---

## Not Discussed (Standard Approaches Applied)

- **Pagination** — Numbered pages, 10/page (PUBL-01)
- **Category/tag admin UI** — Dedicated admin page for CTGY-01/CTGY-02
- **Image upload UX** — Toolbar button + upload modal; URL auto-inserted as `![alt](url)` at cursor

---

## Deferred Ideas

- **Elasticsearch** — Deferred to Phase 4. Phase 2 stays with MySQL FULLTEXT.
- **Drag-drop image upload** — Nice-to-have, not blocking
- **Open Graph / social sharing meta tags** — Phase 2 SEO deferred

---

*Phase: 02-public-blog-image-upload*
*Discussion log: 2026-04-13*

# Phase 4: Core Sync Infrastructure - Context

**Gathered:** 2026-04-15
**Status:** Ready for planning

<domain>
## Phase Boundary

Backend service for CSDN article fetching, parsing, and import with deduplication. This phase covers:
- Flyway migrations for `csdn_sync_config` table and article table extensions
- CSDN article fetching via HTML scraping
- HTML → Markdown parsing with Jsoup
- Sync orchestrator with dedup logic
- REST API for sync configuration and trigger

UI components (sync button, config form, sync results display) are Phase 5.

</domain>

<decisions>
## Implementation Decisions

### Article List Fetching
- **D-01:** HTML scraping only — use Jsoup to parse CSDN blog pages directly
- **D-02:** CSS selectors with fallbacks — primary selectors for article links, fallback selectors if CSDN changes HTML structure

### Update Detection
- **D-03:** Content hash comparison (MD5) — store MD5 hash of CSDN content on sync, compare on re-sync to detect changes

### Error Handling
- **D-04:** Continue + summary — skip individual article failures, log them, continue processing remaining articles, return final counts (created/updated/skipped)

### Sync Response
- **D-05:** Immediate synchronous response — run sync synchronously, return counts immediately

### Technical Stack
- **D-06:** Jsoup 1.18.x for HTML parsing (per v1.1 decisions)
- **D-07:** Spring RestClient (Boot 3.2+) for HTTP fetching (per v1.1 decisions)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### v1.1 Requirements
- `.planning/REQUIREMENTS.md` — SYNC-01 through SYNC-08 and SYNC-14, SYNC-15 define the phase scope

### v1.0 Patterns
- `backend/src/main/java/com/blog/entity/Article.java` — existing Article entity, will be extended with source and csdn_article_id fields
- `backend/src/main/java/com/blog/service/ArticleService.java` — existing service pattern to follow
- `backend/src/main/java/com/blog/mapper/ArticleMapper.java` — existing mapper pattern to follow
- `backend/src/main/resources/db/migration/V1__initial_schema.sql` through `V5__add_tag_slug.sql` — existing Flyway migration patterns

### v1.1 Decisions
- STATE.md — Jsoup 1.18.x, Spring RestClient, image deduplication by URL hash decisions already made

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Article entity/mapper/service pattern — follow existing conventions for new CsdnSyncConfig entity
- Flyway migration pattern — sequential versioning (current: V5, new: V6, V7)
- REST controller pattern — @RestController, @RequestMapping, JWT-secured endpoints

### Established Patterns
- DTO pattern: Request/Response DTOs separate from entities
- Service interface + implementation pattern
- MyBatis mapper with XML or annotations

### Integration Points
- Articles table: needs `source` (varchar) and `csdn_article_id` (varchar, unique) columns
- Need new `csdn_sync_config` table for configuration storage
- JWT-secured admin endpoints — sync endpoints should be admin-only

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 04-core-sync-infrastructure*
*Context gathered: 2026-04-15*

---
# Roadmap: Personal Blog

## Milestones

- ✅ **v1.0 MVP** — Phases 1-3 (shipped 2026-04-15)
- 📋 **v1.1** — CSDN 文章同步 (planning)

## Phases

<details>
<summary>✅ v1.0 MVP (Phases 1-3) — SHIPPED 2026-04-15</summary>

- [x] Phase 1: Admin Backend & Dark Theme (3/3 plans) — completed 2026-04-13
- [x] Phase 2: Public Blog & Image Upload (3/3 plans) — completed 2026-04-13
- [x] Phase 3: Comments (3/3 plans) — completed 2026-04-14

_Archived:_ `.planning/milestones/v1.0-ROADMAP.md`

</details>

### v1.1: CSDN 文章同步

- [ ] **Phase 4: Core Sync Infrastructure** - Backend service, DB migrations, CSDN article parsing and import
- [ ] **Phase 5: Image Handling & Sync UI** - Image download/dedupe, admin sync management interface

---

## Phase Details

### Phase 4: Core Sync Infrastructure

**Goal:** Backend can fetch, parse, and import CSDN articles with deduplication

**Depends on:** Phase 3 (v1.0)

**Requirements:** SYNC-01, SYNC-02, SYNC-03, SYNC-04, SYNC-05, SYNC-06, SYNC-07, SYNC-08, SYNC-14, SYNC-15

**Success Criteria** (what must be TRUE):
1. Admin can configure CSDN userId via backend API and persist to database
2. Admin can select target category for synced articles via backend API
3. Admin can trigger manual sync that fetches articles from CSDN and imports them as published posts
4. Duplicate CSDN articles are detected by csdn_article_id and skipped or updated (not duplicated)
5. Parsed articles have `source: CSDN` and `csdn_article_id` recorded in database
6. Flyway V6 migration creates `csdn_sync_config` table
7. Flyway V7 migration adds `source` and `csdn_article_id` columns to articles table

**Plans:** 5 plans
- [x] .planning/phases/04-core-sync-infrastructure/04-01-PLAN.md — Jsoup dependency + Flyway V6/V7 migrations (Wave 1)
- [x] .planning/phases/04-core-sync-infrastructure/04-02-PLAN.md — CSDN Article Fetcher + Parser + Converter (Wave 2)
- [x] .planning/phases/04-core-sync-infrastructure/04-03-PLAN.md — Sync Config entity/mapper/service + Sync Orchestrator (Wave 2)
- [x] .planning/phases/04-core-sync-infrastructure/04-04-PLAN.md — Admin REST Endpoints for sync (Wave 3)
- [x] .planning/phases/04-core-sync-infrastructure/04-05-PLAN.md — Gap closure: fix ArticleMapper.insert (Wave 4)

**UI hint:** yes

---

### Phase 5: Image Handling & Sync UI

**Goal:** CSDN images are downloaded locally and admin has sync management interface

**Depends on:** Phase 4

**Requirements:** SYNC-07, SYNC-09, SYNC-10, SYNC-11, SYNC-12, SYNC-13

**Success Criteria** (what must be TRUE):
1. CSDN images in article content are downloaded to local filesystem storage
2. CSDN image URLs in Markdown content are replaced with local storage paths
3. Images with duplicate URLs (same URL hash/MD5) are not re-downloaded
4. Admin can view and edit sync configuration (CSDN userId, target category) in backend UI
5. Admin can click "同步" button to trigger sync and see result counts (success/fail/skip)
6. Admin receives warning dialog when editing an article that was synced from CSDN

**Plans:** 7 plans
- [ ] .planning/phases/05-image-handling-sync-ui/05-01-PLAN.md — Flyway V8 + DownloadedImage entity/mapper + ImageDownloadService (Wave 1)
- [ ] .planning/phases/05-image-handling-sync-ui/05-02-PLAN.md — Integrate ImageDownloadService into CsdnSyncService (Wave 1)
- [ ] .planning/phases/05-image-handling-sync-ui/05-03-PLAN.md — CsdnSyncView page + router + sidebar nav (Wave 2)
- [ ] .planning/phases/05-image-handling-sync-ui/05-04-PLAN.md — ArticleEditorView CSDN warning modal (Wave 2)
- [x] .planning/phases/05-image-handling-sync-ui/05-05-PLAN.md — Fix RestClient.Builder NPE in CsdnArticleFetcherImpl (Wave 3)
- [x] .planning/phases/05-image-handling-sync-ui/05-06-PLAN.md — Fix HTML scraping: use CSDN JSON API (Wave 1)
- [ ] .planning/phases/05-image-handling-sync-ui/05-07-PLAN.md — Fix RestClient.Builder NPE in ImageDownloadServiceImpl (Wave 1) [GAP CLOSURE]

**UI hint:** yes

---

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Admin Backend & Dark Theme | 3/3 | ✅ Complete | 2026-04-13 |
| 2. Public Blog & Image Upload | 3/3 | ✅ Complete | 2026-04-13 |
| 3. Comments | 3/3 | ✅ Complete | 2026-04-14 |
| 4. Core Sync Infrastructure | 5/5 | ✅ Complete | 2026-04-16 |
| 5. Image Handling & Sync UI | 2/7 | Gap closure | - |

---
_See `.planning/milestones/v1.0-ROADMAP.md` for full v1.0 phase details._

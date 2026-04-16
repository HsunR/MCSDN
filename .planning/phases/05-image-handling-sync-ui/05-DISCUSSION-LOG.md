# Phase 5: Image Handling & Sync UI - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-16
**Phase:** 05-image-handling-sync-ui
**Areas discussed:** Results UI, UI Location, Edit Warning, Image Errors, Post-sync Behavior

---

## Results UI

| Option | Description | Selected |
|--------|-------------|----------|
| Inline alert/panel below button | Shows created/updated/skipped counts immediately after clicking — simple, no navigation | ✓ |
| Toast notification | Brief popup in corner — less intrusive but easy to miss | |
| Dedicated results panel | Persistent results section with full detail — more information but adds UI complexity | |

**User's choice:** Inline alert/panel below button
**Notes:** Straightforward feedback directly tied to the action

---

## UI Location

| Option | Description | Selected |
|--------|-------------|----------|
| Dashboard widget | Add sync stats/widget to main dashboard alongside article counts | |
| Sidebar menu item | New 'CSDN Sync' entry in admin sidebar with dedicated page | ✓ |
| Article list toolbar | Sync button in article list toolbar + config in article editor settings | |

**User's choice:** Sidebar menu item
**Notes:** Clean separation, can grow with more sync options

---

## Edit Warning

| Option | Description | Selected |
|--------|-------------|----------|
| Blocking modal dialog | Modal pops up immediately on load — user must acknowledge before editing | ✓ |
| Non-blocking warning banner | Warning banner at top of editor — visible but doesn't prevent editing | |
| Soft warning on save only | No warning on entry — warn only if user tries to save changes | |

**User's choice:** Blocking modal dialog
**Notes:** Clearest protection against accidental edits

---

## Image Errors

| Option | Description | Selected |
|--------|-------------|----------|
| Skip article on any image failure | If any CSDN image fails to download, skip the entire article | |
| Retry failed images with backoff | Retry each failed image 3 times with delays before giving up | ✓ |
| Proceed with broken image markers | Download what we can, replace failed URLs with a placeholder | |

**User's choice:** Retry failed images with backoff
**Notes:** Most robust, retries before giving up

---

## Post-sync Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Auto-refresh article list | After sync completes, automatically reload article list | ✓ |
| Manual refresh only | User clicks refresh or navigates to see new articles | |

**User's choice:** Auto-refresh article list
**Notes:** Seamless workflow, no extra clicks needed

---

## Claude's Discretion

The following were left to implementation judgment:
- Exact retry backoff timing (1s, 2s, 4s)
- Broken image placeholder path
- Exact layout of sync results panel (card vs list)
- Exact modal styling and button labels

---

*Discussion log for Phase 05-image-handling-sync-ui — 2026-04-16*

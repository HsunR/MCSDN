# Research Summary — Personal Blog

**Project:** Personal Blog | **Date:** 2026/04/13 | **Confidence:** HIGH

---

## Key Findings

### Stack

**Backend:** Spring Boot 3.2 + MyBatis 3.0 + MySQL 8 + Flyway migrations + JWT auth
**Frontend:** Vue 3 + Vite 5 + Tailwind CSS 3.4 + Pinia + markdown-it + highlight.js
**Image Storage:** Local filesystem (`/uploads/{year}/{month}/{uuid}.{ext}`)

---

### Table Stakes (Must Have)

1. **Article CRUD** — publish/edit/delete with Markdown content
2. **Markdown Editor** — code highlighting (JS/TS/Python/Go/Java/SQL), image upload
3. **Admin Auth** — BCrypt + JWT, rate-limited login
4. **Public Article List** — read/view blog posts

---

### Differentiators (Competitive Edge)

1. **Categories + Tags** — navigable blog structure
2. **Comments** — with moderation queue (pending/approved/rejected)
3. **Search** — MySQL full-text index, not LIKE
4. **Image Upload in Editor** — local file storage, not base64

---

### Watch Out For (Critical Pitfalls)

| # | Pitfall | Phase | Prevention |
|---|---------|-------|------------|
| 1 | **Weak admin auth** | Phase 1 | BCrypt + rate limiting on login |
| 2 | **SQL injection via MyBatis** | Phase 1 | Use `#{ }` only, never `${}` with user input |
| 3 | **Over-engineering admin** | Phase 1 | Textarea + preview only, ship first post before Phase 2 |
| 4 | **Base64 images** | Phase 2 | File storage from day one |
| 5 | **Frontend-only auth** | Phase 2 | Server validates every admin request |
| 6 | **No DB migrations** | Phase 1 | Flyway configured before any table created |
| 7 | **Dark theme half-done** | Phase 1 | Systematic `dark:` classes, dark code highlighting |
| 8 | **Comments without moderation** | Phase 3 | Default PENDING, admin approves before public |

---

### Architecture

- **Pattern:** REST API (Backend-for-Frontend)
- **Build order:** DB schema → Backend core → Frontend scaffold → API clients → Views
- **Auth:** JWT stateless tokens via `Authorization: Bearer` header
- **State:** Pinia stores on frontend, Spring Security filter chain on backend

---

### Anti-Features (Do NOT Build)

- Social login, multi-user accounts, analytics, email notifications, RSS, SEO tooling, theme toggle, mobile apps, third-party comments (Disqus), content versioning, scheduled publishing

---

### Files

| File | Purpose |
|------|---------|
| `research/STACK.md` | Technology choices with versions and rationale |
| `research/FEATURES.md` | Feature categories and dependency map |
| `research/ARCHITECTURE.md` | System structure, data flow, build order |
| `research/PITFALLS.md` | 12 domain pitfalls with prevention and phase mapping |

---
*Research synthesized: 2026/04/13*

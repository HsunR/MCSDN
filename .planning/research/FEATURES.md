# Feature Dimensions — Personal Blog

## Context

- **Type**: Personal blog for a programmer
- **Theme**: Dark mode
- **Stack**: Vue 3 + Spring Boot 3 + MySQL + MyBatis + Tailwind CSS
- **Scope**: Local deployment only (no public hosting / CDN dependency)
- **Users**: Admin (author) + public readers

---

## Table Stakes

Features without which the product fails its core purpose or users leave immediately.

| Feature | Complexity | Notes |
|---|---|---|
| Article CRUD (publish/edit/delete) | Medium | Core loop. Edit and delete must be protected behind auth. |
| Markdown editor with code highlighting | Medium | Code blocks are table stakes for a dev blog. Must support JS/TS/Python/Go/Java/SQL. |
| Admin login authentication | Medium | Without auth there is no admin/protected backend at all. |
| Public article list (read/view) | Low | The primary read path for visitors. |

**Rationale**: A dev blog where the author cannot publish/edit/delete posts and readers cannot read them is not a blog. Authentication gates the entire admin side.

---

## Differentiators

Features that provide competitive advantage or meaningfully elevate the product beyond a generic CMS.

| Feature | Complexity | Dependencies |
|---|---|---|
| Categories and tags | Low | None |
| Comments | Medium | Requires auth or anonymous posting; moderation surfaces here |
| Search | Medium | Requires indexing strategy (DB LIKE vs full-text) |
| Image upload in markdown editor | Low-Medium | Must store locally for local deployment; needs file serving route |
| Separate admin backend and public frontend | Low | Structurally distinct but straightforward SPA routing |

**Rationale**:
- **Categories/tags** make a blog navigable and are expected, but the way they are designed (hierarchical? multi-tag?) is a differentiator.
- **Comments** turn a blog from broadcast into a community. For a personal dev blog, the quality of discourse is the differentiator.
- **Search** is a strong differentiator for a content-heavy blog; most personal blogs skip it.
- **Image upload in-editor** removes friction for the author and is not universal across blogging platforms.
- **Separate admin/reader frontends** allow distinct UX per audience — a clean, fast public face and a functional admin panel.

---

## Anti-Features

Things to deliberately NOT build. Each represents scope that dilutes focus, adds maintenance burden, or conflicts with the local-only constraint.

| Feature | Why Not |
|---|---|
| Social login (OAuth) | Local deployment makes OAuth providers awkward. Password auth is sufficient for a single-admin blog. |
| Public user registration / multi-user accounts | Single-admin blog. Complexity is not justified. |
| Analytics / visitor tracking | Local-only deployment. External analytics conflict with privacy-first stance. Self-hosted analytics could be a later differentiator, but not v1. |
| Email notifications (comments, etc.) | Adds SMTP dependency. Local deploy means no guaranteed email relay. Skip for v1. |
| RSS/Atom feeds | Could be a differentiator, but adds feed generation and maintenance. Not critical for v1. |
| SEO tooling / sitemap generation | Local deployment, low traffic. Not worth the complexity. |
| Dark/light theme toggle | Explicitly specified as dark-only. Building a toggle adds UI complexity for no value in this project. |
| Mobile native apps | Out of scope for a personal blog. |
| Third-party comment systems (Disqus, etc.) | Privacy concern. Local deployment favors self-hosted comments. |
| Content versioning / revision history | Adds significant complexity. Not needed for a personal blog. |
| Scheduled publishing | Adds scheduler infrastructure. Publish-now is sufficient for personal use. |

---

## Dependency Map

```
Article CRUD
  ├── Markdown editor (code highlighting + image upload)
  │     └── Image upload → local file storage + static file serving route
  ├── Categories and tags
  └── Comments
        └── Auth (admin moderation requires login)

Auth (admin login)
  └── Protects: Article CRUD, Comments moderation, Categories/Tags management

Search
  └── Depends on: Article content exists (no hard dependency, but post-CRD)

Admin backend
  └── Depends on: Auth
        └── Protects: all admin features

Public frontend
  └── Depends on: Articles exist, Categories/Tags exist
```

---

## Quality Gate Checklist

- [x] Categories are clear (table stakes vs differentiators vs anti-features)
- [x] Complexity noted for each feature
- [x] Dependencies between features identified

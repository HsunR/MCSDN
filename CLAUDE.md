<!-- GSD:project-start source:PROJECT.md -->
## Project

**个人博客系统**

一个面向程序员的个人博客网站，采用前后端分离架构。前台展示博客文章，支持 Markdown 渲染、代码高亮、图片展示；后台独立部署用于文章管理和登录认证。

**Core Value:** 快速搭建一个属于自己的技术博客空间，专注于内容创作，无需操心复杂功能。

### Constraints

- **数据库**：MySQL 本地安装，端口 3306，密码 123456
- **存储**：图片存储在服务器本地文件系统
- **部署**：仅开发阶段本地运行，不考虑生产部署
<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->
## Technology Stack

## Tech Stack
### Backend
| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| Framework | Spring Boot | 3.2.x | Java 17+ required, native AOT support |
| Language | Java | 17+ | LTS, Spring Boot 3 requires 17+ |
| ORM | MyBatis | 3.0.x | SQL explicit, avoids JPA complexity |
| Database | MySQL | 8.0+ | Local install, utf8mb4 charset |
| Auth | Spring Security + JWT | — | Stateless JWT, BCrypt password encoding |
| Migrations | Flyway | 10.x | Version-controlled schema changes |
| Build | Maven | 3.9+ | Standard Java build tool |
### Frontend
| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| Framework | Vue | 3.4+ | Composition API, `<script setup>` |
| Build | Vite | 5.x | Fast dev server, HMR |
| Router | Vue Router | 4.x | SPA routing, history mode |
| State | Pinia | 2.x | Lightweight, TypeScript support |
| CSS | Tailwind CSS | 3.4+ | Utility-first, dark mode via `class` strategy |
| Markdown | markdown-it | 12.x | Plugin ecosystem, syntax highlighting |
| Code Highlight | highlight.js | 11.x | 190+ languages, auto-detection |
| HTTP | Axios | 1.x | Interceptors, request/response transforms |
### Infrastructure
| Component | Technology | Notes |
|-----------|-----------|-------|
| Image Storage | Local filesystem | `/uploads/{year}/{month}/{uuid}.{ext}` |
| Static Serving | Spring Boot | Configure `spring.web.resources.static-locations` |
| Port (backend) | 8080 | Default Spring Boot |
| Port (frontend) | 5173 | Default Vite dev server |
## What NOT to Use
| Avoid | Reason |
|-------|--------|
| JPA/Hibernate | Overkill for simple blog; MyBatis gives SQL visibility |
| MySQL `${}` interpolation | SQL injection risk — always use `#{ }` |
| Base64 images | Database bloat; use file storage |
| Session-based auth | Stateless REST calls for; JWT is idiomatic |
| Component library (Element Plus etc.) | Conflicts with custom dark theme; Tailwind only |
## Key Libraries
## Version Verification
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, or `.github/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->

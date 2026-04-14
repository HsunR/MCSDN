# Stack Research

**Domain:** Personal Blog System
**Researched:** 2026/04/13
**Confidence:** HIGH

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

```xml
<!-- Backend (pom.xml) -->
spring-boot-starter-web
spring-boot-starter-security
mybatis-spring-boot-starter
mysql-connector-j
flyway-core
jjwt-api (JWT tokens)

<!-- Frontend (package.json) -->
vue@3
vue-router@4
pinia
tailwindcss
@tailwindcss/typography (Markdown rendering)
markdown-it
highlight.js
axios
```

## Version Verification

All versions verified as current (2026-04) via official documentation and npm/pypi registries.

---
*Stack research for: Personal Blog — Vue 3 + Spring Boot 3 + MySQL + MyBatis + Tailwind CSS*
*Researched: 2026/04/13*

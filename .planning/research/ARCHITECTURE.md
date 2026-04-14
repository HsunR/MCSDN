# Architecture Research

**Domain:** Personal Blog System
**Researched:** 2026/04/13
**Confidence:** HIGH

## Standard Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Blog UI   │  │  Admin UI   │  │   Static Assets     │  │
│  │  (Public)   │  │ (Dashboard) │  │  (CSS/JS/Images)    │  │
│  └──────┬──────┘  └──────┬──────┘  └─────────────────────┘  │
│         │                │                                   │
├─────────┴────────────────┴───────────────────────────────────┤
│                      API Gateway / Controller Layer           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ BlogApi     │  │  AdminApi   │  │  AuthApi            │  │
│  │ Controller  │  │  Controller │  │  Controller         │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
└─────────┼────────────────┼────────────────────┼─────────────┘
          │                │                    │
┌─────────┴────────────────┴────────────────────┴─────────────┐
│                      Service Layer                           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ BlogService │  │  UserSvc    │  │  CommentService     │  │
│  │             │  │             │  │                     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
└─────────┼────────────────┼────────────────────┼─────────────┘
          │                │                    │
┌─────────┴────────────────┴────────────────────┴─────────────┐
│                      Data Access Layer                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ PostMapper  │  │ UserMapper  │  │  CommentMapper      │  │
│  │  (MyBatis)  │  │  (MyBatis)  │  │   (MyBatis)         │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
└─────────┼────────────────┼────────────────────┼─────────────┘
          │                │                    │
┌─────────┴────────────────┴────────────────────┴─────────────┐
│                        MySQL Database                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────────────┐  │
│  │  posts  │  │  users  │  │comments │  │    categories   │  │
│  └─────────┘  └─────────┘  └─────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Blog UI (Public) | Renders public blog pages (home, post, about) | Vue 3 + Tailwind CSS |
| Admin UI (Dashboard) | Content management, comment moderation | Vue 3 SPA behind /admin |
| BlogApi Controller | Handles public blog requests (list, view, search) | Spring Boot REST controller |
| AdminApi Controller | Handles CMS operations (CRUD posts, manage comments) | Spring Boot REST, auth-protected |
| AuthApi Controller | Login, logout, session management | Spring Security + JWT |
| BlogService | Blog business logic, pagination, archives | Spring @Service |
| UserService | User auth, permissions, profile management | Spring @Service |
| CommentService | Comment CRUD, spam filtering, replies | Spring @Service |
| PostMapper (MyBatis) | Database mapping for posts table | MyBatis XML or annotations |
| UserMapper (MyBatis) | Database mapping for users table | MyBatis XML or annotations |
| CommentMapper (MyBatis) | Database mapping for comments | MyBatis XML or annotations |

## Recommended Project Structure

```
blog-cc/
├── frontend/                      # Vue 3 frontend
│   ├── src/
│   │   ├── api/                  # Axios API clients
│   │   │   ├── blogApi.js        # Public blog endpoints
│   │   │   ├── adminApi.js       # CMS endpoints
│   │   │   └── authApi.js        # Auth endpoints
│   │   ├── components/           # Reusable Vue components
│   │   │   ├── common/           # Buttons, inputs, cards
│   │   │   ├── blog/             # PostCard, CommentList, etc.
│   │   │   └── admin/            # AdminSidebar, PostEditor
│   │   ├── views/                # Page-level components
│   │   │   ├── public/           # Home, PostDetail, About
│   │   │   └── admin/            # Dashboard, PostList, Settings
│   │   ├── stores/               # Pinia state management
│   │   │   ├── blogStore.js      # Blog posts state
│   │   │   └── adminStore.js     # Admin state
│   │   ├── router/               # Vue Router config
│   │   │   └── index.js
│   │   ├── assets/               # Tailwind CSS, images
│   │   └── main.js               # Vue app entry
│   └── tailwind.config.js
│
├── backend/                      # Spring Boot backend
│   ├── src/main/java/com/blog/
│   │   ├── controller/           # REST controllers
│   │   │   ├── BlogController.java
│   │   │   ├── AdminController.java
│   │   │   └── AuthController.java
│   │   ├── service/              # Business logic
│   │   │   ├── BlogService.java
│   │   │   ├── UserService.java
│   │   │   └── CommentService.java
│   │   ├── mapper/               # MyBatis mappers
│   │   │   ├── PostMapper.java
│   │   │   ├── UserMapper.java
│   │   │   └── CommentMapper.java
│   │   ├── entity/               # Database entities
│   │   │   ├── Post.java
│   │   │   ├── User.java
│   │   │   └── Comment.java
│   │   ├── dto/                  # Data transfer objects
│   │   ├── config/               # Spring configuration
│   │   │   ├── SecurityConfig.java
│   │   │   └── MyBatisConfig.java
│   │   └── BlogApplication.java
│   └── src/main/resources/
│       ├── mapper/               # MyBatis XML mappers
│       └── application.yml
│
└── sql/                         # Database schema
    └── schema.sql
```

### Structure Rationale

- **frontend/api/:** Centralized API clients — single source of truth for endpoint URLs, easier to refactor when backend routes change
- **frontend/components/common/:** Shared UI primitives (buttons, cards) that don't belong to any feature
- **frontend/views/:** Route-level components — each major page gets its own view
- **frontend/stores/:** Pinia stores for cross-component state (auth token, current user)
- **backend/controller/:** Thin controllers — only handle HTTP concerns (params, responses)
- **backend/service/:** Business logic lives here — no HTTP, no SQL
- **backend/mapper/:** MyBatis handles SQL — no business logic here
- **backend/entity/:** JPA-style POJOs matching DB tables
- **backend/dto/:** Request/response objects separate from domain entities
- **sql/:** Database schema under version control, run on initial setup

## Architectural Patterns

### Pattern 1: Layered Architecture

**What:** Separation into Presentation -> Business -> Data layers with strict direction of dependencies.
**When to use:** Standard web application, clear separation of concerns needed.
**Trade-offs:** Simple to understand, but can be verbose for small projects.

**Example:**
```
Controller → Service → Mapper → MySQL
   ↓           ↓         ↓
 HTTP req   Logic    SQL queries
```

### Pattern 2: REST API (Backend-for-Frontend)

**What:** Frontend is a separate SPA that communicates with backend only via REST/JSON.
**When to use:** Vue SPA + Spring Boot backend as separate deployments.
**Trade-offs:** Independent scaling, team separation; adds latency vs. server-rendered pages.

**Example:**
```
Vue SPA (localhost:5173)
    ↓ HTTP/JSON
Spring Boot API (localhost:8080)
    ↓
MySQL (localhost:3306)
```

### Pattern 3: JWT Stateless Authentication

**What:** Server issues a signed token on login; client includes it in Authorization header for every request.
**When to use:** SPA needs auth without session cookies.
**Trade-offs:** Token storage vulnerable to XSS; refresh token rotation needed for security.

**Example:**
```java
// Backend issues token
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    if (authService.validate(req.getUsername(), req.getPassword())) {
        String token = jwtService.generateToken(req.getUsername());
        return ResponseEntity.ok(new TokenResponse(token));
    }
    return ResponseEntity.status(401).build();
}

// Frontend stores and sends
const login = async (user, pass) => {
  const res = await authApi.login(user, pass);
  localStorage.setItem('token', res.data.token);
};

// Frontend axios interceptor adds token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

## Data Flow

### Request Flow

```
[User clicks link in browser]
    ↓
[Vue Router] → [Matching View Component]
    ↓
[API Client (axios)] → [Spring Boot Controller]
    ↓                              ↓
[HTTP 200] ← [JSON Response] ← [Service Layer]
    ↓                              ↓
[Pinia Store] ← [JSON] ← [API Client]
    ↓
[Vue Component re-renders]
```

### Admin CRUD Flow

```
[Admin opens /admin/posts]
    ↓
[AdminView] → [adminApi.getPosts()]
    ↓
[BlogController GET /api/admin/posts] → [BlogService.getPostList()]
    ↓                                              ↓
[200 OK] ← [JSON Array] ← [PostMapper.selectList()]
    ↓
[adminStore.posts = response.data]
    ↓
[PostList component renders table]
```

### State Management

```
┌─────────────────────────────────────────────────────────────┐
│                     Browser (Vue SPA)                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐         ┌──────────────────────────────┐ │
│  │  piniaStore  │ ←───────│  Components (subscribe)       │ │
│  │  (state)     │         │  HomeView, PostDetailView     │ │
│  └──────┬───────┘         └──────────────────────────────┘ │
│         │                                                   │
│         ↓ (actions)                                         │
│  ┌──────────────┐         ┌──────────────────────────────┐ │
│  │  Mutations   │         │  API Layer (axios)           │ │
│  │  (sync)      │         │  blogApi, adminApi           │ │
│  └──────────────┘         └──────────────┬───────────────┘ │
└──────────────────────────────────────────┼─────────────────┘
                                           ↓
                         ┌─────────────────────────────────────┐
                         │        Spring Boot Backend           │
                         ├─────────────────────────────────────┤
                         │  Controller → Service → Mapper      │
                         │         ↓                           │
                         │      MySQL                          │
                         └─────────────────────────────────────┘
```

### Key Data Flows

1. **Public Blog Read:** Browser → Vue Router → View → blogApi.getPosts() → BlogController → BlogService → PostMapper → MySQL → response flows back through layers to update store → component re-renders.

2. **Admin Post Create:** Admin fills form → adminApi.createPost() → AdminController → BlogService.createPost() → PostMapper.insert() → MySQL → returns created post ID.

3. **Authentication:** Login form → authApi.login() → AuthController → UserService.validate() → UserMapper → compare hash → return JWT → store in localStorage.

4. **Comment Submission:** Comment form → blogApi.submitComment() → CommentController → CommentService.addComment() → CommentMapper.insert() → MySQL → real-time update via polling or WebSocket (optional).

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 0-1k users | Monolith Spring Boot + Vue SPA is fine. Single MySQL instance. |
| 1k-100k users | Add Redis cache for hot posts. Consider CDN for static assets. |
| 100k+ users | Read replica for MySQL. Consider splitting into separate auth service. |

### Scaling Priorities

1. **First bottleneck:** Database queries without indexes — fix with proper indexing on `posts.created_at`, `posts.category_id`, `comments.post_id`.
2. **Second bottleneck:** N+1 queries in post listing — fix with eager loading or batch fetching in MyBatis.

## Anti-Patterns

### Anti-Pattern 1: Fat Controllers

**What people do:** Put business logic directly in `@RestController` methods.
**Why it's wrong:** Controller becomes hard to test, logic gets duplicated across endpoints, violates DRY.
**Do this instead:** Keep controllers thin (HTTP handling only), delegate to `@Service` classes.

### Anti-Pattern 2: Entity as DTO

**What people do:** Returning JPA entities directly as controller responses.
**Why it's wrong:** Exposes internal structure, risk of lazy loading exceptions, couples DB schema to API.
**Do this instead:** Use separate DTO classes for API requests/responses; map with MapStruct or manual mapping.

### Anti-Pattern 3: Frontend Business Logic

**What people do:** Putting data transformation and business rules in Vue components.
**Why it's wrong:** Hard to test, inconsistent behavior across pages, breaks if component is reused.
**Do this instead:** API layer should return clean data; components only render; business logic in backend services.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| None required | Local deployment only | All data stays on-premises |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Vue SPA ↔ Spring Boot | HTTP/REST (JSON) | CORS must be configured in SecurityConfig |
| Spring Boot ↔ MySQL | JDBC via MyBatis | Connection pool (HikariCP) configured in application.yml |
| Auth filter ↔ Controllers | Spring Security filter chain | JWT validated before controller execution |

### Build Order (Dependencies)

```
1. Database Schema (sql/schema.sql)
   ↓
2. Backend Entity classes (entity/)
   ↓
3. MyBatis Mapper interfaces + XML (mapper/)
   ↓
4. Backend Service layer (service/)
   ↓
5. Backend Controller layer (controller/)
   ↓
6. Backend unit tests
   ↓
7. Frontend project scaffold (Vue 3 + Tailwind)
   ↓
8. Frontend API clients (api/)
   ↓
9. Frontend stores (stores/)
   ↓
10. Frontend views and components
```

Start with DB schema and backend core. Frontend depends on backend API contracts, so finalize controller endpoints before building frontend API clients.

---

*Architecture research for: Personal Blog*
*Researched: 2026/04/13*

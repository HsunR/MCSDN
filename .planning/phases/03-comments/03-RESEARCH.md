# Phase 3: Comments - Research

**Researched:** 2026-04-13
**Domain:** Vue 3 + Spring Boot 3 blog comment system
**Confidence:** HIGH

## Summary

Phase 3 implements a public comment submission system with admin moderation. Public visitors submit name+content comments that default to PENDING and require admin approval before display. The implementation follows existing project patterns: MyBatis for data access (matching ArticleMapper), Spring Security with JWT for admin endpoints (matching existing SecurityConfig), Vue 3 Composition API with `<script setup>` for components (matching ArticleView.vue), and Pinia for state (matching publicArticleStore). The comment table uses a new Flyway migration (V3__comments.sql) following the established utf8mb4 charset and InnoDB engine conventions.

**Primary recommendation:** Reuse existing backend layer patterns (entity, mapper, service, controller) exactly as Article does. Add two new Vue components (CommentSection for public, AdminCommentsView for admin) and one new Pinia store (commentStore). Comment API uses POST /api/articles/{id}/comments (public, no auth) and /api/admin/comments (JWT-protected).

---

## User Constraints (from CONTEXT.md)

### Locked Decisions

- Comment form appears below article content, before comment list (D-01)
- Form fields: name (text input) + content (textarea) only, no email/URL in v1 (D-02)
- Submit button with loading state; success = "Comment submitted, awaiting approval."; error = inline error (D-03)
- Approved comments in flat list, no threading/nesting (D-04)
- Comment card shows: name, formatted date, content (D-05)
- Comments ordered oldest-first (D-06)
- Admin comment management at `/admin/comments` page (D-07)
- Admin list shows all comments with status filter tabs (D-08)
- Admin actions: Approve (PENDING→APPROVED), Reject (PENDING→REJECTED), Delete (D-09)
- Comment entity: id, article_id, author_name, content, status, created_at (D-10)
- Status defaults to PENDING on creation (D-11)
- Honeypot spam field (hidden CSS field, bot-filled = silent reject) (D-12)
- No captcha in v1 (D-13)
- Public POST /api/articles/{id}/comments — no auth (D-14)
- Public GET /api/articles/{id}/comments — approved only (D-15)
- Admin GET /api/admin/comments — all with status filter (D-16)
- Admin PATCH /api/admin/comments/{id}/approve (D-17)
- Admin PATCH /api/admin/comments/{id}/reject (D-18)
- Admin DELETE /api/admin/comments/{id} (D-19)
- Admin comments route: `#/admin/comments` (D-20)
- CommentSection integrated into ArticleView below `<div class="prose">` (D-22)
- Admin sidebar: "Comments" nav link with pending count badge (D-23)
- publicArticleStore: add fetchComments(articleId) method (D-24)
- CommentService handles all comment business logic (D-25)

### Deferred Ideas (OUT OF SCOPE)

- Email/push notifications when admin approves (Phase 4+)
- Threaded/nested replies (Phase 4+)
- Comment upvotes/reactions (Phase 4+)
- Anti-spam service (Akismet) (Phase 4+)
- Commenter email field (Phase 4+)

---

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CMNT-01 | Public can submit comments (name + content, no auth) | PublicCommentController POST /api/articles/{id}/comments — permitAll endpoint. Honeypot field in CommentSection.vue. Entity uses `#{ }` MyBatis binding (SQL injection safe). |
| CMNT-02 | Comments default to PENDING (not publicly visible) | Comment entity defaults status column to 'PENDING' via Flyway default. Public GET only returns APPROVED status. |
| CMNT-03 | Admin can approve/reject pending comments | AdminCommentController PATCH endpoints: /approve and /reject. Status transitions PENDING→APPROVED and PENDING→REJECTED enforced in service layer. |
| CMNT-04 | Admin can delete comments | AdminCommentController DELETE /api/admin/comments/{id}. CASCADE on article_tags handles ARTL-03 cascade. |
| CMNT-05 | Approved comments display on article page | PublicCommentController GET /api/articles/{id}/comments returns only APPROVED. CommentSection.vue fetches and renders below article content. |

---

## Standard Stack

### Backend (already defined in CLAUDE.md — confirming versions)

| Library | Version | Purpose | Why Standard |
|---------|---------|--------|--------------|
| Spring Boot | 3.2.x | REST API framework | Already in use |
| MyBatis | 3.0.x | SQL mapping | Already in use, gives SQL visibility |
| MySQL | 8.0+ | Database | Already connected at localhost:3306 |
| Flyway | 10.x | Schema migrations | Already in use |
| Spring Security + JWT | — | Auth for admin endpoints | Already in use |
| BCrypt | — | Password encoding | Already in use |

### Frontend (already defined in CLAUDE.md — confirming versions)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Vue | 3.4+ | UI framework | Already in use |
| Vite | 5.x | Build tool | Already in use |
| Vue Router (hash mode) | 4.x | Routing | Already in use, confirmed hash-mode |
| Pinia | 2.x | State management | Already in use |
| Tailwind CSS | 3.4+ | Dark theme styling | Already in use |
| Axios | 1.x | HTTP client | Already in use |

### New for Phase 3

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| — | — | Honeypot via CSS hidden field | No library needed |
| `window.striptags` (inline) | — | Strip HTML from comment input before storage | Already exists in publicArticleStore.js |

---

## Architecture Patterns

### Recommended Project Structure

```
backend/src/main/java/com/blog/
├── entity/
│   └── Comment.java              # New entity
├── mapper/
│   ├── CommentMapper.java       # New mapper interface
│   └── CommentMapper.xml        # New mapper XML
├── service/
│   ├── CommentService.java      # New service interface
│   └── impl/
│       └── CommentServiceImpl.java  # New service impl
├── controller/
│   ├── PublicCommentController.java  # New: public endpoints
│   └── AdminCommentController.java  # New: admin endpoints (JWT)
└── dto/
    └── CommentRequest.java      # New: DTO for comment submission

backend/src/main/resources/
├── db/migration/
│   └── V3__comments.sql           # New Flyway migration
└── mapper/
    └── CommentMapper.xml         # New

frontend/src/
├── views/
│   ├── public/
│   │   └── ArticleView.vue       # Modified: add CommentSection
│   └── admin/
│       └── AdminCommentsView.vue # New: comment moderation page
├── components/
│   └── CommentSection.vue        # New: public comment form + list
├── stores/
│   └── commentStore.js            # New: Pinia store for comments
└── api/
    └── commentApi.js             # New: Axios API for comments
```

### Pattern 1: Comment Entity (Plain POJO)

**What:** Simple entity class following the same POJO pattern as Article.java
**When to use:** Every database table gets a corresponding entity
**Example:**

```java
// Source: Pattern from backend/src/main/java/com/blog/entity/Article.java
package com.blog.entity;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private Long articleId;
    private String authorName;
    private String content;
    private String status;         // PENDING / APPROVED / REJECTED
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### Pattern 2: MyBatis Mapper XML

**What:** XML mapper following the exact structure of ArticleMapper.xml
**When to use:** All CRUD and query operations for the comment table
**Example:**

```xml
<!-- Source: Pattern from backend/src/main/resources/mapper/ArticleMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.blog.mapper.CommentMapper">
    <resultMap id="CommentResultMap" type="com.blog.entity.Comment">
        <id property="id" column="id"/>
        <result property="articleId" column="article_id"/>
        <result property="authorName" column="author_name"/>
        <result property="content" column="content"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO comments (article_id, author_name, content, status, created_at)
        VALUES (#{articleId}, #{authorName}, #{content}, #{status}, NOW())
    </insert>

    <select id="findByArticleIdAndStatus" resultMap="CommentResultMap">
        SELECT id, article_id, author_name, content, status, created_at
        FROM comments
        WHERE article_id = #{articleId} AND status = #{status}
        ORDER BY created_at ASC
    </select>

    <select id="findAll" resultMap="CommentResultMap">
        SELECT id, article_id, author_name, content, status, created_at
        FROM comments
        ORDER BY created_at DESC
    </select>

    <select id="findAllWithStatus" resultMap="CommentResultMap">
        SELECT id, article_id, author_name, content, status, created_at
        FROM comments
        WHERE status = #{status}
        ORDER BY created_at DESC
    </select>

    <select id="countByStatus" resultType="int">
        SELECT COUNT(*) FROM comments WHERE status = #{status}
    </select>

    <update id="updateStatus">
        UPDATE comments SET status = #{status} WHERE id = #{id}
    </update>

    <delete id="delete">
        DELETE FROM comments WHERE id = #{id}
    </delete>

    <delete id="deleteByArticleId">
        DELETE FROM comments WHERE article_id = #{articleId}
    </delete>
</mapper>
```

### Pattern 3: Flyway Migration

**What:** V3__comments.sql following the exact schema conventions of V1 and V2
**When to use:** Any schema change must go through Flyway
**Example:**

```sql
-- Source: Pattern from backend/src/main/resources/db/migration/V2__articles_categories_tags.sql
-- V3__comments.sql
-- Creates comments table for Phase 3

CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT NOT NULL,
  author_name VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  INDEX idx_status (status),
  INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Pattern 4: Public REST Controller (No Auth)

**What:** Spring controller for public comment endpoints, following PublicArticleController pattern
**When to use:** Any public-facing API endpoint that requires no authentication
**Example:**

```java
// Source: Pattern from backend/src/main/java/com/blog/controller/PublicArticleController.java
@RestController
@RequestMapping("/api/articles")
public class PublicCommentController {

    private final CommentService commentService;

    public PublicCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // CMNT-05: List approved comments for an article
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getApprovedComments(id));
    }

    // CMNT-01: Submit a new comment (honeypot checked in service)
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> submitComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        try {
            commentService.submitComment(id, request);
            return ResponseEntity.status(201)
                .body(Map.of("message", "Comment submitted, awaiting approval."));
        } catch (SpamException e) {
            return ResponseEntity.ok(Map.of("message", "Comment submitted, awaiting approval."));
        }
    }
}
```

### Pattern 5: Admin REST Controller (JWT Protected)

**What:** Spring controller for admin comment management, following ArticleController pattern
**When to use:** Any admin-only API endpoint
**Example:**

```java
// Source: Pattern from backend/src/main/java/com/blog/controller/ArticleController.java
@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // CMNT-02/03: List all comments (with optional status filter)
    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
            status == null ? commentService.getAllComments()
                          : commentService.getCommentsByStatus(status));
    }

    // CMNT-03: Approve a pending comment
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveComment(@PathVariable Long id) {
        commentService.updateStatus(id, "APPROVED");
        return ResponseEntity.ok().build();
    }

    // CMNT-03: Reject a pending comment
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectComment(@PathVariable Long id) {
        commentService.updateStatus(id, "REJECTED");
        return ResponseEntity.ok().build();
    }

    // CMNT-04: Delete a comment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Pattern 6: Vue 3 CommentSection Component

**What:** Public comment form + approved comment list, following ArticleView.vue Composition API pattern
**When to use:** Below `<div class="prose">` in ArticleView.vue
**Example:**

```vue
<!-- Source: Pattern from frontend/src/views/public/ArticleView.vue -->
<script setup>
import { ref } from 'vue'
import { commentApi } from '../../api/commentApi'

const props = defineProps({ articleId: String })
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = ref({ name: '', content: '', honeypot: '' })
const message = ref({ type: '', text: '' })

async function loadComments() {
  loading.value = true
  try {
    comments.value = await commentApi.getApproved(props.articleId)
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!form.value.name.trim() || !form.value.content.trim()) return
  submitting.value = true
  try {
    await commentApi.submit(props.articleId, form.value)
    message.value = { type: 'success', text: 'Comment submitted, awaiting approval.' }
    form.value = { name: '', content: '', honeypot: '' }
  } catch (e) {
    message.value = { type: 'error', text: 'Failed to submit comment. Please try again.' }
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('zh-CN',
    { year: 'numeric', month: 'long', day: 'numeric' })
}

loadComments()
</script>

<template>
  <section class="mt-12 border-t border-gray-700 pt-8">
    <h3 class="text-xl font-bold text-gray-100 mb-6">Comments</h3>

    <!-- Comment Form -->
    <form @submit.prevent="submitComment" class="mb-10 space-y-4">
      <!-- Honeypot (D-12): hidden from real users -->
      <input
        v-model="form.honeypot"
        name="website"
        type="text"
        class="absolute opacity-0 pointer-events-none"
        tabindex="-1"
        autocomplete="off"
      />

      <input
        v-model="form.name"
        type="text"
        placeholder="Your name"
        maxlength="100"
        class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500"
      />
      <textarea
        v-model="form.content"
        placeholder="Your comment"
        rows="4"
        class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500"
      />
      <div v-if="message.text" :class="[
        'text-sm', message.type === 'success' ? 'text-green-400' : 'text-red-400'
      ]">
        {{ message.text }}
      </div>
      <button
        type="submit"
        :disabled="submitting"
        class="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-500 disabled:opacity-50"
      >
        {{ submitting ? 'Submitting...' : 'Submit Comment' }}
      </button>
    </form>

    <!-- Comment List -->
    <div v-if="loading" class="text-gray-500">Loading comments...</div>
    <div v-else-if="comments.length === 0" class="text-gray-500">
      No comments yet. Be the first to comment!
    </div>
    <div v-else class="space-y-6">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="bg-gray-800 rounded-lg p-4 border border-gray-700"
      >
        <div class="flex items-center gap-3 mb-2">
          <span class="font-semibold text-gray-100">{{ comment.authorName }}</span>
          <span class="text-gray-500 text-sm">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <p class="text-gray-300 whitespace-pre-wrap">{{ comment.content }}</p>
      </div>
    </div>
  </section>
</template>
```

### Pattern 7: Admin Comment Moderation Page

**What:** Vue component with status filter tabs (All / Pending / Approved / Rejected), following DashboardView.vue structure
**When to use:** `#/admin/comments` route
**Example:**

```vue
<!-- Source: Pattern from frontend/src/views/admin/DashboardView.vue -->
<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { commentApi } from '../../api/commentApi'
import { ref, onMounted } from 'vue'

const comments = ref([])
const loading = ref(false)
const activeTab = ref('PENDING')

const tabs = ['PENDING', 'APPROVED', 'REJECTED']

async function loadComments() {
  loading.value = true
  try {
    comments.value = await commentApi.getAll(activeTab.value)
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  await commentApi.approve(id)
  loadComments()
}

async function reject(id) {
  await commentApi.reject(id)
  loadComments()
}

async function remove(id) {
  if (!confirm('Delete this comment?')) return
  await commentApi.delete(id)
  loadComments()
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('zh-CN',
    { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(loadComments)
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Comments</h1>

      <!-- Status Filter Tabs (D-08) -->
      <div class="flex gap-2 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab"
          @click="activeTab = tab; loadComments()"
          :class="[
            'px-4 py-2 rounded-lg transition-colors',
            activeTab === tab ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'
          ]"
        >
          {{ tab }}
        </button>
      </div>

      <!-- Comment List -->
      <div v-if="loading" class="text-gray-400">Loading...</div>
      <div v-else-if="comments.length === 0" class="text-gray-500">
        No {{ activeTab.toLowerCase() }} comments.
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="bg-gray-800 rounded-lg p-4 border border-gray-700"
        >
          <div class="flex items-start justify-between">
            <div>
              <span class="font-semibold text-gray-100">{{ comment.authorName}</span>
              <span class="text-gray-500 text-sm ml-3">{{ formatDate(comment.createdAt) }}</span>
              <span :class="[
                'ml-3 px-2 py-0.5 text-xs rounded',
                comment.status === 'APPROVED' ? 'bg-green-900 text-green-300' :
                comment.status === 'REJECTED' ? 'bg-red-900 text-red-300' :
                'bg-yellow-900 text-yellow-300'
              ]">
                {{ comment.status }}
              </span>
            </div>
            <!-- Admin Actions (D-09) -->
            <div class="flex gap-2">
              <button
                v-if="comment.status === 'PENDING'"
                @click="approve(comment.id)"
                class="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-500"
              >Approve</button>
              <button
                v-if="comment.status === 'PENDING'"
                @click="reject(comment.id)"
                class="px-3 py-1 text-sm bg-yellow-600 text-white rounded hover:bg-yellow-500"
              >Reject</button>
              <button
                @click="remove(comment.id)"
                class="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-500"
              >Delete</button>
            </div>
          </div>
          <p class="mt-2 text-gray-300">{{ comment.content }}</p>
          <div class="mt-1 text-xs text-gray-600">Article #{{ comment.articleId }}</div>
        </div>
      </div>
    </main>
  </div>
</template>
```

### Pattern 8: Pinia Comment Store

**What:** Pinia store following the publicArticleStore pattern
**When to use:** Managing comment state in the frontend
**Example:**

```js
// Source: Pattern from frontend/src/stores/publicArticleStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { commentApi } from '../api/commentApi'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchApprovedComments(articleId) {
    loading.value = true
    error.value = null
    try {
      comments.value = await commentApi.getApproved(articleId)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return { comments, loading, error, fetchApprovedComments }
})
```

### Pattern 9: Comment API Module

**What:** Axios-based API module following articleApi.js pattern
**When to use:** All HTTP calls for comments from the frontend
**Example:**

```js
// Source: Pattern from frontend/src/api/articleApi.js
import axios from 'axios'

const getAuthHeaders = () => {
  const token = localStorage.getItem('admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use(config => {
  Object.assign(config.headers, getAuthHeaders())
  return config
})

// Public endpoints (no auth needed)
export const commentApi = {
  // GET /api/articles/{id}/comments — approved only (CMNT-05)
  getApproved(articleId) {
    return http.get(`/articles/${articleId}/comments`).then(r => r.data)
  },
  // POST /api/articles/{id}/comments — submit comment (CMNT-01)
  submit(articleId, data) {
    return http.post(`/articles/${articleId}/comments`, data).then(r => r.data)
  }
}

// Admin endpoints (auth required)
export const adminCommentApi = {
  // GET /api/admin/comments — all with optional status filter (CMNT-02/03)
  getAll(status) {
    return http.get('/admin/comments', { params: status ? { status } : {} }).then(r => r.data)
  },
  // PATCH /api/admin/comments/{id}/approve (CMNT-03)
  approve(id) {
    return http.patch(`/admin/comments/${id}/approve`)
  },
  // PATCH /api/admin/comments/{id}/reject (CMNT-03)
  reject(id) {
    return http.patch(`/admin/comments/${id}/reject`)
  },
  // DELETE /api/admin/comments/{id} (CMNT-04)
  delete(id) {
    return http.delete(`/admin/comments/${id}`)
  }
}
```

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SQL injection in comment content | Raw string concatenation | MyBatis `#{ }` binding | Already standard in existing mappers. Never use `${ }` for user input. |
| HTML in comment content (XSS) | Custom HTML sanitizer | markdown-it already used for articles; for plain text comments, use inline `striptags` from publicArticleStore.js | Prevents XSS when rendering comment.content |
| Password hashing | Custom | BCryptPasswordEncoder | Already in project |
| Article delete cascade | Manual comment deletion | `ON DELETE CASCADE` in Flyway migration | Ensures ARTL-03 compliance |
| Admin auth on comment endpoints | Custom auth | Existing JWT filter via SecurityConfig | Admin endpoints already require JWT |

**Key insight:** The existing security infrastructure (JWT filter, SecurityConfig, BCrypt) should be reused without modification. Only new permitAll rules needed for `/api/articles/{id}/comments` POST and GET (which are already covered by `.anyRequest().permitAll()`).

---

## Common Pitfalls

### Pitfall 1: SQL Injection via `${ }` Instead of `#{ }`

**What goes wrong:** Comment content or author name containing SQL metacharacters corrupts the query or exposes data.
**Why it happens:** Using MyBatis `${}` interpolation (string substitution) instead of `#{ }` (parameter binding).
**How to avoid:** Always use `#{ }` for any user-supplied value in SQL. Inspect CommentMapper.xml before running migration.
**Warning signs:** MySQL errors about syntax near special characters, or unusual characters in rendered comments.

### Pitfall 2: CORS Blocking Comment Submission

**What goes wrong:** Comment POST from Vue dev server (port 5173) to Spring Boot (port 8080) is blocked by CORS policy.
**Why it happens:** CORS configuration only allows specific origins.
**How to avoid:** SecurityConfig already has `corsConfigurationSource()` with `"http://localhost:5173"` allowed. Do not remove or override this.
**Warning signs:** Browser console shows `Access-Control-Allow-Origin` error on comment submit.

### Pitfall 3: Honeypot Field Not Actually Hidden

**What goes wrong:** The honeypot field is visible to real users, causing confusion or making them think it's a required field.
**Why it happens:** Using `display: none` can be detected by bots and is not truly hidden from assistive tech. Using `type="hidden"` alone still submits a value.
**How to avoid:** Use CSS `class="absolute opacity-0 pointer-events-none"` (from Tailwind: `absolute opacity-0 pointer-events-none`) with `tabindex="-1"` and `autocomplete="off"`. This is visually hidden, not focusable, and bots typically fill it because it's a named input.
**Warning signs:** Real users complaining about a confusing extra field.

### Pitfall 4: Comment Text Rendered as HTML

**What goes wrong:** If a user submits `<script>alert('xss')</script>`, it executes as JavaScript when rendered with `v-html`.
**Why it happens:** Rendering raw HTML from comment content.
**How to avoid:** Always use `{{ comment.content }}` (Vue text interpolation) which escapes HTML. Only use `v-html` for trusted content (already-processed markdown from markdown-it). The honeypot pattern from publicArticleStore.js handles stripping HTML from stored values.

### Pitfall 5: Comment Status Not Enforced on Public GET

**What goes wrong:** Public GET endpoint returns ALL comments including PENDING ones, making unmoderated content visible.
**Why it happens:** Missing WHERE clause in SQL, or not filtering in service layer.
**How to avoid:** Explicitly query `WHERE status = 'APPROVED'` in CommentMapper.findApprovedByArticleId. Verify with a test that calls GET without auth and confirms only APPROVED comments return.
**Warning signs:** Comment appears publicly without admin approval.

### Pitfall 6: Missing Cascade Delete for Article Comments

**What goes wrong:** When admin deletes an article (ARTL-03), orphan comments remain in DB.
**Why it happens:** No `ON DELETE CASCADE` on `article_id` foreign key.
**How to avoid:** Include `FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE` in V3__comments.sql. Already present in ArticleTag's `article_tags` table — follow the same pattern.
**Warning signs:** Orphan comment rows after article deletion.

### Pitfall 7: Admin Sidebar Badge Not Updated

**What goes wrong:** Pending comment count badge on admin sidebar shows stale count after new comment submission.
**Why it happens:** Badge count loaded once on admin login, not refreshed.
**How to avoid:** Fetch pending count on AdminCommentsView mount and refresh when navigating back to dashboard. Use `commentStore` with a computed pending count.

---

## Code Examples

### CommentRequest DTO (Spring Boot Validation)

```java
// Source: Pattern from backend/src/main/java/com/blog/dto/LoginRequest.java
package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String authorName;

    @NotBlank(message = "Comment content is required")
    @Size(max = 5000, message = "Comment must be at most 5000 characters")
    private String content;

    private String honeypot;  // Should be empty for real users

    // Getters
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public String getHoneypot() { return honeypot; }
}
```

### MyBatis Mapper Interface

```java
// Source: Pattern from backend/src/main/java/com/blog/mapper/ArticleMapper.java
package com.blog.mapper;

import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    List<Comment> findByArticleIdAndStatus(@Param("articleId") Long articleId, @Param("status") String status);
    List<Comment> findAll();
    List<Comment> findAllWithStatus(@Param("status") String status);
    int countByStatus(@Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int delete(@Param("id") Long id);
    int deleteByArticleId(@Param("articleId") Long articleId);
}
```

### Router Update for Admin Comments

```js
// Source: Pattern from frontend/src/router/index.js
// Add to routes array:
{
  path: '/admin/comments',
  name: 'AdminComments',
  component: () => import('../views/admin/AdminCommentsView.vue'),
  meta: { requiresAuth: true }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Session-based auth | JWT stateless auth | Phase 1 | No session storage; all requests carry Bearer token |
| Base64 image storage | Local filesystem | Phase 2 | No DB bloat from image blobs |
| MyBatis `${}` interpolation | `#{ }` parameter binding | Phase 1 (enforced) | SQL injection prevented |
| Threaded comments | Flat comment list | Phase 3 (D-04 locked) | Simpler implementation, Phase 4+ can add threading |
| Email notifications on approve | None (deferred) | Phase 4 | Reduces SMTP dependency in v1 |

**Deprecated/outdated:**
- `spring-web` resource handling patterns (now using `spring.web.resources.static-locations` per CLAUDE.md)

---

## Assumptions Log

> List all claims tagged `[ASSUMED]` in this research. The planner and discuss-phase use this section to identify decisions that need user confirmation before execution.

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Comment entity can use `VARCHAR(100)` for `author_name` — same as `categories.name` | Comment Entity | Name longer than 100 chars truncated or error |
| A2 | `TEXT` type for `content` column is sufficient for typical comment length (max ~5000 chars) | Flyway Migration | Very long comments may be truncated |
| A3 | Honeypot field named `website` / `honeypot` is standard practice — bots auto-fill it | Security | Bot detection rate may vary; no A/B testing planned |
| A4 | CommentSection component uses inline `formatDate` function rather than a utility | Code Examples | Dates may display differently across components |
| A5 | AdminCommentsView does NOT need pagination in v1 — all comments shown | Common Pitfalls | Large comment volumes will be slow to load |
| A6 | Frontend `commentApi` reuses the same Axios instance pattern from `articleApi.js` | Code Examples | Auth interceptor may behave unexpectedly |

**If this table is empty:** All claims in this research were verified or cited — no user confirmation needed.

---

## Open Questions

1. **RESOLVED: Should admin GET `/api/admin/comments` support pagination?**
   - What we know: Phase 3 admin comment list shows all comments without pagination (A5)
   - What's unclear: How many comments is "too many" for a single page load
   - Resolution: Skip pagination for v1 simplicity. If comment volume exceeds ~50 items, pagination can be added in a later phase. Admin can manage reasonable volumes without pagination in initial release.

2. **RESOLVED: Should `author_name` be sanitized on input or on output?**
   - What we know: markdown-it already handles article content rendering; comments are plain text
   - What's unclear: Whether special characters in names (emoji, CJK) need normalization
   - Resolution: Store as-is, escape on output with Vue `{{ }}` interpolation. Vue's text interpolation automatically escapes HTML entities, so no sanitization needed on input. Special characters (emoji, CJK) are preserved without normalization.

---

## Environment Availability

> Step 2.6: SKIPPED (no external dependencies beyond existing project stack)

Phase 3 adds only local code changes:
- New Java classes (entity, mapper, service, controller, DTO) — no new runtime dependencies
- New Flyway migration — existing MySQL instance at localhost:3306 used
- New Vue components — no new npm packages needed
- No new tools, CLIs, databases, or services required

---

## Validation Architecture

> Required because `nyquist_validation: true` in `.planning/config.json`.

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 5 (Spring Boot test) + Vue Test Utils |
| Backend config file | `backend/src/test/resources/application.yml` |
| Frontend config file | `frontend/vitest.config.js` |
| Quick run command (backend) | `mvn test -Dtest=Comment*Test` |
| Quick run command (frontend) | `vitest run src/__tests__/CommentSection.spec.js` |
| Full suite command (backend) | `mvn test` |
| Full suite command (frontend) | `vitest run` |

### Phase Requirements to Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|--------------|
| CMNT-01 | Public POST /api/articles/{id}/comments accepts valid name+content | Unit | `CommentServiceTest.submitComment_validInput_returnsPending()` | Check Wave 0 |
| CMNT-01 | Honeypot-filled submission is silently rejected | Unit | `CommentServiceTest.submitComment_honeypotFilled_throwsSpamException()` | Check Wave 0 |
| CMNT-02 | Comment status defaults to PENDING on creation | Unit | `CommentMapperTest.insert_commentStatus_isPending()` | Check Wave 0 |
| CMNT-03 | Admin PATCH /api/admin/comments/{id}/approve changes status to APPROVED | Unit | `AdminCommentControllerTest.approve_comment_statusUpdated()` | Check Wave 0 |
| CMNT-03 | Admin PATCH /api/admin/comments/{id}/reject changes status to REJECTED | Unit | `AdminCommentControllerTest.reject_comment_statusUpdated()` | Check Wave 0 |
| CMNT-04 | Admin DELETE /api/admin/comments/{id} removes comment | Unit | `AdminCommentControllerTest.delete_comment_removed()` | Check Wave 0 |
| CMNT-05 | Public GET /api/articles/{id}/comments returns only APPROVED comments | Unit | `CommentServiceTest.getComments_onlyApprovedReturned()` | Check Wave 0 |

### Sampling Rate

- **Per task commit:** `mvn test -Dtest=Comment*Test` (backend), `vitest run` (frontend)
- **Per wave merge:** `mvn test` (full backend), `vitest run` (full frontend)
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps

- [ ] `backend/src/test/java/com/blog/service/CommentServiceTest.java` — covers CMNT-01, CMNT-02, CMNT-05
- [ ] `backend/src/test/java/com/blog/controller/AdminCommentControllerTest.java` — covers CMNT-03, CMNT-04
- [ ] `backend/src/test/java/com/blog/mapper/CommentMapperTest.java` — covers CMNT-02
- [ ] `frontend/src/__tests__/CommentSection.spec.js` — covers CMNT-01, CMNT-05
- [ ] `backend/src/test/resources/application.yml` — test datasource config (H2 in-memory)
- [ ] Framework install: `mvn test` in backend — confirm H2 dependency in pom.xml; `vitest` in frontend — confirm in package.json

### Known Test Patterns to Follow

- Backend: Use `@SpringBootTest` with H2 in-memory DB for mapper tests; `@WebMvcTest` for controller tests with mocked service
- Frontend: `vitest` + `@vue/test-utils` following Vue 3 Composition API patterns
- Security: Disable JWT for public comment controller tests (no auth required for CMNT-01, CMNT-05)
- DB: Use `@Sql` to run Flyway migrations before mapper tests via `MigrationTestExecutor`

---

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | No | Public comment submission — no auth required |
| V3 Session Management | No | JWT stateless; existing session management unchanged |
| V4 Access Control | Yes | Admin comment endpoints require JWT (existing infrastructure) |
| V5 Input Validation | Yes | `jakarta.validation.constraints` on CommentRequest DTO (Size, NotBlank) |
| V6 Cryptography | No | No new cryptographic operations |
| V7 Error Handling | Yes | GlobalExceptionHandler catches validation errors; honeypot returns 200 (not 400) to avoid bot signal |

### Known Threat Patterns for This Stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL injection in comment content/author name | Tampering | MyBatis `#{ }` binding — verified in existing ArticleMapper |
| XSS via `<script>` in comment content | XSS | Vue `{{ }}` text interpolation escapes HTML; v-html not used on user content |
| Bot spam via automated comment submission | Denial of Service | Honeypot field (D-12) — bots fill hidden fields |
| CSRF on comment submission | Tampering | CSRF disabled in SecurityConfig (Phase 1) |
| Public endpoint returns pending comments | Information Disclosure | Service layer enforces `WHERE status = 'APPROVED'` |
| Mass comment deletion via IDOR | Tampering | Admin endpoints require JWT; comments belong to articles admin already manages |
| Comment content used to deface page | Tampering | Plain text only; no markdown rendering on comments (only on article content) |

---

## Sources

### Primary (HIGH confidence)

- `backend/src/main/java/com/blog/entity/Article.java` — Entity pattern
- `backend/src/main/resources/mapper/ArticleMapper.xml` — MyBatis mapper XML pattern
- `backend/src/main/java/com/blog/mapper/ArticleMapper.java` — MyBatis mapper interface pattern
- `backend/src/main/java/com/blog/controller/PublicArticleController.java` — Public REST controller pattern
- `backend/src/main/java/com/blog/controller/ArticleController.java` — Admin REST controller pattern
- `backend/src/main/java/com/blog/config/SecurityConfig.java` — Security configuration (JWT, CORS, permitAll)
- `backend/src/main/resources/db/migration/V2__articles_categories_tags.sql` — Flyway migration pattern
- `frontend/src/views/public/ArticleView.vue` — Vue 3 Composition API pattern with `<script setup>`
- `frontend/src/stores/publicArticleStore.js` — Pinia store pattern
- `frontend/src/api/publicApi.js` — Axios API module pattern
- `frontend/src/api/articleApi.js` — Axios with auth interceptor pattern
- `frontend/src/components/admin/AdminSidebar.vue` — Admin navigation pattern
- `frontend/src/router/index.js` — Hash-mode router pattern
- `.planning/phases/03-comments/03-CONTEXT.md` — Locked decisions D-01 through D-25
- `.planning/REQUIREMENTS.md` — CMNT-01 through CMNT-05

### Secondary (MEDIUM confidence)

- [Spring Boot MyBatis integration docs](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.mybatis) — MyBatis Spring Boot Starter usage (standard practice)
- [Vue 3 Composition API docs](https://vuejs.org/api/completion-api/setup.html) — `<script setup>` syntax
- [Pinia state management](https://pinia.vuejs.org/core-concepts/) — defineStore pattern

### Tertiary (LOW confidence)

- Honeypot anti-spam effectiveness rates — community best practice, not formally verified

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all libraries already defined in CLAUDE.md, confirmed via existing codebase
- Architecture: HIGH — all patterns derived directly from existing code in the project
- Pitfalls: MEDIUM — identified from common MyBatis/Spring/Vue pitfalls, verified against existing patterns
- Security: MEDIUM — SQL injection and XSS mitigations verified against existing codebase; honeypot effectiveness is community consensus

**Research date:** 2026-04-13
**Valid until:** 2026-05-13 (30 days — project stack is stable, no fast-moving libraries)

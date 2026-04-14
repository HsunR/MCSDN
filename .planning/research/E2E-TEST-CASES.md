# End-to-End Test Cases — Personal Blog System

> **Date:** 2026/04/14
> **Project:** Personal Blog (blog-cc)
> **Scope:** Full-stack E2E coverage — Frontend (Vue 3) + Backend (Spring Boot)

***

## 1. Overview

### 1.1 Test Strategy

| Layer          | Tool                         | Scope                                                  |
| -------------- | ---------------------------- | ------------------------------------------------------ |
| E2E (frontend) | <br />                       | Full browser flows — auth, navigation, forms, comments |
| API (backend)  | cURL / Postman / REST Client | All REST endpoints with/without auth                   |
| Database       | MySQL queries                | Verify data persistence, integrity                     |

### 1.2 Test Environment

| Component     | Value                                   |
| ------------- | --------------------------------------- |
| Backend URL   | `http://localhost:8080`                 |
| Frontend URL  | `http://localhost:5173`                 |
| MySQL         | `localhost:3306`, password `123456`     |
| Default Admin | username: `admin`, password: `admin123` |
| Browser       | Chromium                                |

### 1.3 Test Categories

- **Auth**: Login, logout, JWT, route guards
- **Public Browsing**: Homepage, article detail, category/tag filter, search
- **Comments**: Public submission, honeypot, moderation
- **Admin Articles**: CRUD, markdown editor, image upload
- **Admin Categories & Tags**: CRUD
- **Admin Comments**: Approve/reject/delete, filter tabs
- **Navigation & Guards**: Redirects, unauthorized access
- **Error & Edge Cases**: Validation, file size, file type

***

## 2. Authentication Tests

### 2.1 Login

| ID      | Test Case                     | Steps                                                              | Expected Result                                             | Priority |
| ------- | ----------------------------- | ------------------------------------------------------------------ | ----------------------------------------------------------- | -------- |
| AUTH-01 | Login with valid credentials  | 1. Open `/admin/login`2. Enter `admin` / `admin123`3. Click login  | JWT stored, redirected to `/admin`, sidebar visible         | P0       |
| AUTH-02 | Login with invalid password   | 1. Open `/admin/login`2. Enter `admin` / `wrongpass`3. Click login | Error message "Invalid username or password", stay on login | P0       |
| AUTH-03 | Login with non-existent user  | 1. Open `/admin/login`2. Enter `nouser` / `anypass`3. Click login  | Error message "Invalid username or password"                | P0       |
| AUTH-04 | Login with empty fields       | 1. Open `/admin/login`2. Leave fields empty3. Click login          | Browser-native validation prevents submission               | P1       |
| AUTH-05 | JWT token persists on refresh | 1. Login successfully2. Refresh page                               | Stay on `/admin`, no redirect to login                      | P0       |

### 2.2 Logout

| ID      | Test Case                    | Steps                                                            | Expected Result                           | Priority |
| ------- | ---------------------------- | ---------------------------------------------------------------- | ----------------------------------------- | -------- |
| AUTH-10 | Logout clears token          | 1. Login as admin2. Click logout in sidebar3. Check localStorage | JWT removed, redirected to `/admin/login` | P0       |
| AUTH-11 | Logout prevents admin access | 1. Logout2. Navigate to `/admin`                                 | Redirected to `/admin/login`              | P0       |

### 2.3 Route Guards

| ID      | Test Case                                   | Steps                                               | Expected Result                | Priority |
| ------- | ------------------------------------------- | --------------------------------------------------- | ------------------------------ | -------- |
| AUTH-20 | Unauthenticated access to `/admin`          | 1. Clear all tokens2. Navigate to `/admin`          | Redirect to `/admin/login`     | P0       |
| AUTH-21 | Unauthenticated access to `/admin/articles` | 1. Clear all tokens2. Navigate to `/admin/articles` | Redirect to `/admin/login`     | P0       |
| AUTH-22 | Authenticated user on login page            | 1. Login as admin2. Navigate to `/admin/login`      | Redirect to `/admin/dashboard` | P1       |
| AUTH-23 | Public pages accessible without auth        | 1. No token2. Navigate to `/`                       | Homepage loads normally        | P0       |

### 2.4 API Security

| ID      | Test Case                           | Steps                                                   | Expected Result        | Priority |
| ------- | ----------------------------------- | ------------------------------------------------------- | ---------------------- | -------- |
| AUTH-30 | Admin API without token             | 1. GET `/api/admin/articles` (no Authorization header)  | HTTP 401               | P0       |
| AUTH-31 | Admin API with invalid token        | 1. GET `/api/admin/articles` with `Bearer invalidtoken` | HTTP 401               | P0       |
| AUTH-32 | Public API accessible without token | 1. GET `/api/articles`                                  | HTTP 200, article list | P0       |

***

## 3. Public Browsing Tests

### 3.1 Homepage

| ID     | Test Case                       | Steps                          | Expected Result                                       | Priority |
| ------ | ------------------------------- | ------------------------------ | ----------------------------------------------------- | -------- |
| PUB-01 | Homepage loads article timeline | 1. Open `/`                    | Timeline shows published articles grouped by date     | P0       |
| PUB-02 | Homepage shows pagination       | 1. Open `/`2. Check pagination | Page numbers or prev/next visible if >10 articles     | P1       |
| PUB-03 | Homepage pagination works       | 1. Click page 2                | URL updates to `?page=2`, articles update             | P1       |
| PUB-04 | Article cards show correct data | 1. On homepage                 | Each card shows: title, excerpt, tags, date, category | P0       |

### 3.2 Article Detail

| ID     | Test Case                   | Steps                                                               | Expected Result                                          | Priority |
| ------ | --------------------------- | ------------------------------------------------------------------- | -------------------------------------------------------- | -------- |
| PUB-10 | Article detail loads        | 1. Click an article card                                            | Navigate to `/article/:id-slug`, article content renders | P0       |
| PUB-11 | Markdown renders correctly  | 1. View article with markdown content                               | Headings, lists, bold, italic render as HTML             | P0       |
| PUB-12 | Code blocks highlight       | 1. View article with code blocks (`js, `  java, etc.)               | Syntax highlighting applied, language label shown        | P0       |
| PUB-13 | Images display inline       | 1. View article with inline images                                  | Images load and display at correct size                  | P1       |
| PUB-14 | Article shows category link | 1. View article detail                                              | Category name clickable, links to category page          | P1       |
| PUB-15 | Article shows tag links     | 1. View article detail                                              | Tags clickable, link to tag pages                        | P1       |
| PUB-16 | Comment section visible     | 1. View article detail                                              | CommentSection component visible below article           | P0       |
| PUB-17 | Approved comments displayed | 1. View article with approved comments                              | Comment list shows approved comments                     | P0       |
| PUB-18 | Pending comments not shown  | 1. Submit a comment (approved later)2. View article before approval | Comment NOT visible until approved                       | P0       |

### 3.3 Category Filter

| ID     | Test Case                            | Steps                                                     | Expected Result                         | Priority |
| ------ | ------------------------------------ | --------------------------------------------------------- | --------------------------------------- | -------- |
| PUB-20 | Category page loads                  | 1. Navigate to `/category/:name` or click category link   | Filtered article list for that category | P0       |
| PUB-21 | Category page shows correct articles | 1. Visit category page                                    | All articles belong to that category    | P0       |
| PUB-22 | Category page pagination             | 1. Visit category with many articles2. Navigate to page 2 | Correct subset of articles shown        | P1       |

### 3.4 Tag Filter

| ID     | Test Case                       | Steps                                         | Expected Result                    | Priority |
| ------ | ------------------------------- | --------------------------------------------- | ---------------------------------- | -------- |
| PUB-30 | Tag page loads                  | 1. Navigate to `/tag/:name` or click tag link | Filtered article list for that tag | P0       |
| PUB-31 | Tag page shows correct articles | 1. Visit tag page                             | All articles have that tag         | P0       |

### 3.5 Search

| ID     | Test Case                 | Steps                                                         | Expected Result                     | Priority |
| ------ | ------------------------- | ------------------------------------------------------------- | ----------------------------------- | -------- |
| PUB-40 | Search returns results    | 1. Open `/search?q=keyword`2. Use keyword present in articles | Matching articles displayed         | P0       |
| PUB-41 | Search shows no results   | 1. Open `/search?q=nonexistentkeyword12345`                   | Empty state or "No results" message | P0       |
| PUB-42 | Search highlights keyword | 1. Search with valid keyword2. Check results                  | Results contain the keyword         | P1       |
| PUB-43 | Search pagination         | 1. Search with many results2. Navigate pages                  | Correct subset of results           | P1       |

***

## 4. Comment System Tests

### 4.1 Public Comment Submission

| ID    | Test Case                         | Steps                                                        | Expected Result                                        | Priority |
| ----- | --------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------ | -------- |
| CM-01 | Submit valid comment              | 1. On article page2. Fill name + content3. Submit            | Success message, form clears, comment pending approval | P0       |
| CM-02 | Submit empty name                 | 1. On article page2. Leave name empty, fill content3. Submit | Validation error "Name is required"                    | P0       |
| CM-03 | Submit empty content              | 1. On article page2. Fill name, leave content empty3. Submit | Validation error "Content is required"                 | P0       |
| CM-04 | Honeypot field filled (bot)       | 1. Fill honeypot field (hidden)2. Submit valid form          | Silent success (200), comment NOT stored               | P0       |
| CM-05 | Comment appears after approval    | 1. Submit comment2. Admin approves3. View article            | Comment visible in list                                | P0       |
| CM-06 | Multiple comments on same article | 1. Submit 3 comments on same article2. Approve all           | All 3 comments display in chronological order          | P1       |

### 4.2 Comment Data Integrity

| ID    | Test Case                          | Steps                                              | Expected Result                     | Priority |
| ----- | ---------------------------------- | -------------------------------------------------- | ----------------------------------- | -------- |
| CM-10 | Comment stores correct article\_id | 1. Submit comment on article ID 52. Query database | `article_id = 5` in comments table  | P0       |
| CM-11 | Comment stores correct timestamps  | 1. Submit comment2. Query database                 | `created_at` set to submission time | P1       |

***

## 5. Admin Article Tests

### 5.1 Article List

| ID       | Test Case              | Steps                                    | Expected Result                                               | Priority |
| -------- | ---------------------- | ---------------------------------------- | ------------------------------------------------------------- | -------- |
| ADM-A-01 | Article list loads     | 1. Login2. Navigate to `/admin/articles` | All articles listed with title, status, date                  | P0       |
| ADM-A-02 | Status badges visible  | 1. On article list                       | Draft articles show "DRAFT" badge, published show "PUBLISHED" | P0       |
| ADM-A-03 | Delete article button  | 1. On article list2. Click delete icon   | Confirmation dialog appears                                   | P0       |
| ADM-A-04 | Confirm delete article | 1. Click delete2. Confirm                | Article removed from list, DB deleted                         | P0       |
| ADM-A-05 | Cancel delete article  | 1. Click delete2. Cancel                 | Article remains in list                                       | P1       |

### 5.2 Create Article

| ID       | Test Case                      | Steps                                                | Expected Result                                              | Priority |
| -------- | ------------------------------ | ---------------------------------------------------- | ------------------------------------------------------------ | -------- |
| ADM-A-10 | Create article page loads      | 1. Navigate to `/admin/articles/new`                 | Editor form visible with title, content, status, tags fields | P0       |
| ADM-A-11 | Create published article       | 1. Fill title, content, select "PUBLISHED"2. Submit  | Article created, redirect to list, visible on public site    | P0       |
| ADM-A-12 | Create draft article           | 1. Fill title, content, select "DRAFT"2. Submit      | Article created with DRAFT status, not on public site        | P0       |
| ADM-A-13 | Create article without title   | 1. Leave title empty2. Submit                        | Validation error "Title is required"                         | P0       |
| ADM-A-14 | Create article without content | 1. Fill title, leave content empty2. Submit          | Validation error "Content is required"                       | P0       |
| ADM-A-15 | Markdown live preview works    | 1. Type markdown in editor pane2. Check preview pane | Preview updates in real-time                                 | P1       |
| ADM-A-16 | Insert image via toolbar       | 1. Click image insert2. Upload image3. Click insert  | Markdown image syntax inserted at cursor                     | P1       |

### 5.3 Edit Article

| ID       | Test Case                 | Steps                                                          | Expected Result                      | Priority |
| -------- | ------------------------- | -------------------------------------------------------------- | ------------------------------------ | -------- |
| ADM-A-20 | Edit article page loads   | 1. Click edit on existing article                              | Editor pre-filled with existing data | P0       |
| ADM-A-21 | Update article            | 1. Change title2. Save                                         | Changes persisted, redirect to list  | P0       |
| ADM-A-22 | Toggle draft to published | 1. Edit article with DRAFT status2. Change to PUBLISHED3. Save | Now visible on public site           | P0       |

### 5.4 Image Upload

| ID       | Test Case                | Steps                                                | Expected Result                                  | Priority |
| -------- | ------------------------ | ---------------------------------------------------- | ------------------------------------------------ | -------- |
| ADM-A-30 | Upload valid image       | 1. In article editor2. Upload jpg/png/gif/webp < 5MB | Upload success, markdown image inserted          | P0       |
| ADM-A-31 | Upload invalid file type | 1. Upload .pdf or .exe                               | Error message "Only jpg, png, gif, webp allowed" | P0       |
| ADM-A-32 | Upload oversized file    | 1. Upload file > 5MB                                 | Error message "File size exceeds 5MB"            | P0       |
| ADM-A-33 | Drag-and-drop upload     | 1. Drag image file onto upload area                  | Same result as click-to-upload                   | P1       |
| ADM-A-34 | Uploaded image URL works | 1. Insert uploaded image2. View article public page  | Image renders correctly                          | P0       |

***

## 6. Admin Category Tests

| ID         | Test Case                     | Steps                                             | Expected Result                                   | Priority |
| ---------- | ----------------------------- | ------------------------------------------------- | ------------------------------------------------- | -------- |
| ADM-CAT-01 | List categories               | 1. Login2. Navigate to articles/new or categories | Existing categories shown                         | P0       |
| ADM-CAT-02 | Create category               | 1. Create category "DevOps"                       | Category created, appears in list                 | P0       |
| ADM-CAT-03 | Create duplicate category     | 1. Create category with existing name             | Error "Category already exists"                   | P0       |
| ADM-CAT-04 | Update category               | 1. Edit category name                             | Name updated                                      | P0       |
| ADM-CAT-05 | Delete unused category        | 1. Delete category with no articles               | Category deleted                                  | P0       |
| ADM-CAT-06 | Delete category with articles | 1. Delete category that has articles              | Error or articles reassigned (per implementation) | P1       |

***

## 7. Admin Tag Tests

| ID         | Test Case             | Steps                                      | Expected Result                  | Priority |
| ---------- | --------------------- | ------------------------------------------ | -------------------------------- | -------- |
| ADM-TAG-01 | List tags             | 1. Login2. Navigate to article editor      | Existing tags shown              | P0       |
| ADM-TAG-02 | Create tag            | 1. Create tag "Kubernetes"                 | Tag created, appears in tag list | P0       |
| ADM-TAG-03 | Create duplicate tag  | 1. Create tag with existing name           | Error "Tag already exists"       | P0       |
| ADM-TAG-04 | Delete unused tag     | 1. Delete tag with no articles             | Tag deleted                      | P0       |
| ADM-TAG-05 | Assign tag to article | 1. Create/Edit article2. Select tag3. Save | Tag associated with article      | P0       |

***

## 8. Admin Comment Moderation Tests

| ID        | Test Case                          | Steps                                            | Expected Result                                  | Priority |
| --------- | ---------------------------------- | ------------------------------------------------ | ------------------------------------------------ | -------- |
| ADM-CM-01 | Comment moderation page loads      | 1. Login2. Navigate to `/admin/comments`         | All comments shown with status tabs              | P0       |
| ADM-CM-02 | Pending tab shows pending comments | 1. Submit new comment2. Open admin comments      | New comment in PENDING tab                       | P0       |
| ADM-CM-03 | Approve comment                    | 1. Click approve on pending comment              | Comment status → APPROVED, moves to APPROVED tab | P0       |
| ADM-CM-04 | Reject comment                     | 1. Click reject on pending comment               | Comment status → REJECTED, moves to REJECTED tab | P0       |
| ADM-CM-05 | Delete comment                     | 1. Click delete on any comment                   | Comment permanently removed                      | P0       |
| ADM-CM-06 | Pending badge count                | 1. Have 3 pending comments2. Check sidebar badge | Badge shows "3"                                  | P0       |
| ADM-CM-07 | Filter by APPROVED tab             | 1. Click APPROVED tab                            | Only APPROVED comments shown                     | P0       |
| ADM-CM-08 | Filter by REJECTED tab             | 1. Click REJECTED tab                            | Only REJECTED comments shown                     | P0       |

***

## 9. Dashboard Tests

| ID       | Test Case                           | Steps                                          | Expected Result                   | Priority |
| -------- | ----------------------------------- | ---------------------------------------------- | --------------------------------- | -------- |
| ADM-D-01 | Dashboard loads                     | 1. Login → `/admin`                            | Dashboard view with stats         | P0       |
| ADM-D-02 | Stats are accurate                  | 1. Check article count2. Compare with DB       | Counts match actual data          | P1       |
| ADM-D-03 | Recent articles listed              | 1. On dashboard                                | Recent articles listed            | P1       |
| ADM-D-04 | Sidebar shows pending comment count | 1. Login with pending comments2. Check sidebar | Badge shows correct pending count | P0       |

***

## 10. Navigation Tests

| ID     | Test Case                | Steps                                        | Expected Result                | Priority |
| ------ | ------------------------ | -------------------------------------------- | ------------------------------ | -------- |
| NAV-01 | Admin sidebar links work | 1. Click each sidebar link                   | Correct page loads             | P0       |
| NAV-02 | Breadcrumb navigation    | 1. On article edit page2. Click breadcrumb   | Navigate to list               | P1       |
| NAV-03 | Browser back/forward     | 1. Navigate between pages2. Use browser back | Correct previous page restored | P1       |
| NAV-04 | 404 page                 | 1. Navigate to `/nonexistent-route`          | 404 page or redirect           | P1       |

***

## 11. Error & Edge Cases

| ID     | Test Case                     | Steps                                            | Expected Result                                      | Priority |
| ------ | ----------------------------- | ------------------------------------------------ | ---------------------------------------------------- | -------- |
| ERR-01 | Article not found             | 1. Navigate to `/article/99999`                  | 404 page or "Article not found" message              | P0       |
| ERR-02 | Network error on page load    | 1. Go offline2. Reload page                      | Error message or fallback UI                         | P2       |
| ERR-03 | API timeout                   | 1. Slow network simulation2. Submit form         | Timeout error after threshold                        | P2       |
| ERR-04 | Concurrent comment submission | 1. Submit same comment twice quickly             | Both processed (no deduplication unless implemented) | P2       |
| ERR-05 | Very long article title       | 1. Create article with 500-char title            | Truncated in list, full in detail                    | P2       |
| ERR-06 | XSS in comment                | 1. Submit `<script>alert(1)</script>` as comment | Script NOT executed (sanitized)                      | P0       |
| ERR-07 | SQL injection in search       | 1. Search with `' OR 1=1 --`                     | Handled safely, no SQL error exposed                 | P0       |

***

## 12. Test Data Prerequisites

Before running E2E tests, ensure:

```sql
-- Admin user exists
INSERT INTO admin_users (username, password) VALUES
('admin', '$2a$10$...'); -- BCrypt hash of 'admin123'

-- Categories exist
INSERT INTO categories (name) VALUES ('Backend'), ('Frontend'), ('DevOps');

-- Tags exist
INSERT INTO tags (name) VALUES ('Java'), ('Vue'), ('Spring Boot'), ('Docker');

-- At least one published article for public browsing tests
INSERT INTO articles (title, content, status, category_id) VALUES
('Welcome to My Blog', '# Welcome\n\nThis is my first article.', 'PUBLISHED', 1);
```

***

## 14. Coverage Summary

| Category          | P0 Tests | P1 Tests | P2 Tests | Total  |
| ----------------- | -------- | -------- | -------- | ------ |
| Authentication    | 7        | 2        | 0        | 9      |
| Public Browsing   | 11       | 7        | 0        | 18     |
| Comments (Public) | 6        | 0        | 0        | 6      |
| Admin Articles    | 8        | 4        | 2        | 14     |
| Admin Categories  | 2        | 2        | 0        | 4      |
| Admin Tags        | 2        | 1        | 0        | 3      |
| Admin Comments    | 5        | 0        | 0        | 5      |
| Dashboard         | 2        | 2        | 0        | 4      |
| Navigation        | 0        | 4        | 0        | 4      |
| Error/Edge Cases  | 4        | 0        | 3        | 7      |
| **Total**         | **47**   | **22**   | **5**    | **74** |


# Phase 03: Comments — E2E Test Cases

**Created:** 2026-04-14
**Phase:** 03-comments
**Framework:** Playwright (existing from Phase 1)

---

## Test Setup

### Prerequisites
```bash
# Backend running on port 8080
cd backend && mvn spring-boot:run

# Frontend running on port 5173
cd frontend && npm run dev

# Install Playwright browsers (once)
cd frontend && npx playwright install
```

### Auth (for admin tests)
```javascript
// Login as admin before running admin tests
const loginAsAdmin = async (page) => {
  await page.goto('http://localhost:5173/#/admin/login')
  await page.fill('input[name="username"]', 'admin')
  await page.fill('input[name="password"]', 'admin123')
  await page.click('button[type="submit"]')
  await page.waitForURL('**/#/admin')
}
```

---

## Test Cases

### PUBLISH-01: Public Comment Submission

**Feature:** Public visitors can submit comments on articles without authentication.

**Preconditions:**
- Backend running on port 8080
- Frontend running on port 5173
- At least one PUBLISHED article exists in the database
- A published article with ID `1` is assumed for these tests

**Test Steps:**
1. Navigate to `http://localhost:5173/#/article/1`
2. Wait for page to load and markdown to render
3. Scroll to comment section (below article content)
4. Fill in name: `TestUser`
5. Fill in comment: `This is a test comment.`
6. Click "Submit Comment" button
7. Verify success message: "Comment submitted, awaiting approval."

**Expected Result:** Comment form accepts input, submits successfully, shows approval message. Comment does NOT appear immediately in the list (pending moderation).

**Acceptance Criteria:**
- [ ] Form fields accept text input
- [ ] Submit button shows loading state during submission
- [ ] Success message appears after submission
- [ ] Form clears after successful submission
- [ ] Comment is NOT visible in the approved list immediately after

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "PUBLISH-01"
```

---

### PUBLISH-02: Approved Comments Display on Article Page

**Feature:** Only APPROVED comments are displayed publicly.

**Preconditions:**
- A comment exists in DB with status `APPROVED` for article ID `1`
- Admin has approved the comment via admin panel

**Test Steps:**
1. Navigate to `http://localhost:5173/#/article/1`
2. Scroll to comment section
3. Verify approved comments appear with: author name, date, content
4. Verify NO comments with status `PENDING` or `REJECTED` are displayed

**Expected Result:** Only APPROVED comments visible. Each shows:
- Author name (bold)
- Date formatted as "YYYY年MM月DD日"
- Comment content (plain text, no HTML rendering)

**Acceptance Criteria:**
- [ ] Comment author name displayed
- [ ] Comment date displayed
- [ ] Comment content displayed as plain text
- [ ] PENDING comments NOT shown
- [ ] REJECTED comments NOT shown

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "PUBLISH-02"
```

---

### PUBLISH-03: Comment Form Validation

**Feature:** Comment form validates required fields.

**Test Steps:**
1. Navigate to `http://localhost:5173/#/article/1`
2. Leave name and comment fields empty
3. Click "Submit Comment"
3. Attempt to submit with only name filled
4. Attempt to submit with only comment filled

**Expected Result:**
- Empty submission: form does not submit, browser native validation or inline error
- Name-only submission: shows error "Name is required" or similar
- Comment-only submission: shows error "Comment is required" or similar

**Acceptance Criteria:**
- [ ] Empty form cannot be submitted
- [ ] Name field is required
- [ ] Comment content field is required
- [ ] Clear error messages displayed

---

### ADMIN-01: Admin Comment List

**Feature:** Admin can view all comments with status filtering.

**Preconditions:** Admin is logged in (use `loginAsAdmin` helper)

**Test Steps:**
1. Navigate to `http://localhost:5173/#/admin/comments`
2. Verify page loads with "Comments" heading
3. Verify three status tabs: PENDING, APPROVED, REJECTED
4. Click each tab and verify list updates
5. Verify each comment shows: author name, date, status badge, content, article ID

**Expected Result:**
- PENDING tab shows only PENDING comments (yellow badge)
- APPROVED tab shows only APPROVED comments (green badge)
- REJECTED tab shows only REJECTED comments (red badge)
- Empty state message when no comments in a category

**Acceptance Criteria:**
- [ ] Page loads without errors
- [ ] All three tabs visible and clickable
- [ ] Tab shows correct filtered results
- [ ] Comment cards display all metadata
- [ ] Status badges have correct colors

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "ADMIN-01"
```

---

### ADMIN-02: Approve Comment

**Feature:** Admin can approve a PENDING comment.

**Preconditions:** Admin logged in, at least one PENDING comment exists

**Test Steps:**
1. Navigate to `http://localhost:5173/#/admin/comments`
2. Click PENDING tab
3. Find a PENDING comment card
4. Click "Approve" button on that card
5. Verify comment moves from PENDING to APPROVED tab

**Expected Result:**
- Approve button is visible and clickable on PENDING comments
- After clicking, comment disappears from PENDING list
- Comment appears in APPROVED tab
- No error messages

**Acceptance Criteria:**
- [ ] Approve button exists on PENDING comments
- [ ] Clicking Approve sends PATCH to `/api/admin/comments/{id}/approve`
- [ ] Comment status changes to APPROVED in database
- [ ] Comment moves to APPROVED tab

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "ADMIN-02"
```

---

### ADMIN-03: Reject Comment

**Feature:** Admin can reject a PENDING comment.

**Preconditions:** Admin logged in, at least one PENDING comment exists

**Test Steps:**
1. Navigate to `http://localhost:5173/#/admin/comments`
2. Click PENDING tab
3. Find a PENDING comment card
4. Click "Reject" button on that card
5. Verify comment moves from PENDING to REJECTED tab

**Expected Result:**
- Reject button is visible and clickable on PENDING comments
- After clicking, comment disappears from PENDING list
- Comment appears in REJECTED tab
- No error messages

**Acceptance Criteria:**
- [ ] Reject button exists on PENDING comments
- [ ] Clicking Reject sends PATCH to `/api/admin/comments/{id}/reject`
- [ ] Comment status changes to REJECTED in database
- [ ] Comment moves to REJECTED tab

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "ADMIN-03"
```

---

### ADMIN-04: Delete Comment

**Feature:** Admin can delete any comment regardless of status.

**Preconditions:** Admin logged in

**Test Steps:**
1. Navigate to `http://localhost:5173/#/admin/comments`
2. Find any comment (any status tab)
3. Click "Delete" button
4. Confirm deletion in browser dialog (if present)
5. Verify comment is removed from all lists

**Expected Result:**
- Delete button visible on all comments regardless of status
- After clicking, comment is permanently removed
- Comment does not appear in any tab

**Acceptance Criteria:**
- [ ] Delete button exists on all comments
- [ ] Clicking Delete sends DELETE to `/api/admin/comments/{id}`
- [ ] Comment removed from database
- [ ] Comment removed from UI immediately

**Automated Command:**
```bash
cd frontend && npx playwright test --grep "ADMIN-04"
```

---

### ADMIN-05: Admin Sidebar Badge

**Feature:** Admin sidebar shows pending comment count badge.

**Preconditions:** Admin logged in

**Test Steps:**
1. Login as admin
2. Navigate to any admin page (e.g., `#/admin`)
3. Look at sidebar navigation
4. Verify "Comments" nav item has a badge
5. Navigate to comments page
6. Note the PENDING count in the PENDING tab
7. Return to dashboard
8. Verify badge count matches PENDING count

**Expected Result:**
- Comments nav item has a badge showing pending count
- Badge updates when pending count changes
- Badge only visible to authenticated admin users

**Acceptance Criteria:**
- [ ] Badge visible on Comments nav item
- [ ] Badge shows numeric count
- [ ] Count matches actual PENDING comments in database
- [ ] Badge only visible when logged in as admin

---

### SECURITY-01: Honeypot Blocks Bot Submissions

**Feature:** Hidden honeypot field rejects automated/bot submissions.

**Test Steps:**
1. Navigate to `http://localhost:5173/#/article/1`
2. Inspect the comment form HTML
3. Find the hidden honeypot field (class `absolute opacity-0 pointer-events-none` or similar)
4. Fill in ALL form fields including the honeypot
5. Submit the form

**Expected Result:**
- Form submission appears to succeed (no error)
- But comment is NOT stored in database
- Or comment is stored but with special flag indicating spam

**Note:** This is a negative test — actual bot blocking cannot be fully tested via E2E.

---

### SECURITY-02: Public Cannot Access Admin Endpoints

**Feature:** Admin comment endpoints require authentication.

**Test Steps:**
1. WITHOUT logging in, try to access admin comment endpoints directly:
   - GET `http://localhost:8080/api/admin/comments`
   - PATCH `http://localhost:8080/api/admin/comments/1/approve`
   - DELETE `http://localhost:8080/api/admin/comments/1`

**Expected Result:**
- All requests return 401 Unauthorized or 403 Forbidden
- No comments are modified or deleted

**Acceptance Criteria:**
- [ ] Unauthenticated GET returns 401/403
- [ ] Unauthenticated PATCH returns 401/403
- [ ] Unauthenticated DELETE returns 401/403

---

### SECURITY-03: Public GET Returns Only Approved Comments

**Feature:** Public comment list endpoint never returns PENDING or REJECTED comments.

**Test Steps:**
1. Create a PENDING comment via API or admin
2. As anonymous user, call: GET `http://localhost:8080/api/articles/1/comments`
3. Verify response contains only APPROVED comments

**Expected Result:**
- Response array contains zero PENDING comments
- Response array contains zero REJECTED comments
- Only APPROVED comments are returned

**Acceptance Criteria:**
- [ ] PENDING comments NOT in public API response
- [ ] REJECTED comments NOT in public API response
- [ ] Only APPROVED comments visible to public

---

## Run All Tests

### Full Suite
```bash
cd frontend && npx playwright test --reporter=line
```

### Comment-Specific Tests Only
```bash
cd frontend && npx playwright test --grep "PUBLISH|ADMIN|SECURITY" --reporter=line
```

### Quick Smoke Test (CMNT-01 through CMNT-05)
```bash
cd frontend && npx playwright test --grep "PUBLISH-01|PUBLISH-02|ADMIN-01|ADMIN-02|ADMIN-03|ADMIN-04" --reporter=line
```

---

## Test Data Setup

### Create Published Article with Comments
```sql
-- Insert test article (if not exists)
INSERT INTO articles (title, content, status, category_id) VALUES
('Test Article', '# Hello\n\nThis is a test article.', 'PUBLISHED', 1);

-- Get the article ID
SELECT id FROM articles WHERE title = 'Test Article' LIMIT 1;

-- Insert test comments
INSERT INTO comments (article_id, author_name, content, status) VALUES
(1, 'John Doe', 'Great article!', 'APPROVED'),
(1, 'Jane Smith', 'Very helpful, thanks.', 'PENDING'),
(1, 'Bot User', 'Buy cheap meds now!', 'REJECTED');
```

### Clean Up Test Data
```sql
DELETE FROM comments WHERE author_name IN ('John Doe', 'Jane Smith', 'Bot User', 'TestUser');
DELETE FROM articles WHERE title = 'Test Article';
```

---

## Browser Support

| Browser | Status |
|---------|--------|
| Chromium (Chrome) | Tested |
| Firefox | Should work |
| WebKit (Safari) | Should work |

Run on all browsers:
```bash
npx playwright test --browser=chromium,firefox,webkit
```

---

## Debugging Failed Tests

### View Test Trace
```bash
npx playwright show-trace test-results/
```

### Run with UI
```bash
npx playwright test --headed
```

### Run Single Test
```bash
npx playwright test --grep "PUBLISH-01" --headed
```

### Check Console Errors
```javascript
// Add to test:
page.on('console', msg => {
  if (msg.type() === 'error') console.log('CONSOLE ERROR:', msg.text())
})
```

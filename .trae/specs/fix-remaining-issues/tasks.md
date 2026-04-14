# Tasks

- [x] Task 1: Fix search functionality in ArticleMapper.xml
  - [x] SubTask 1.1: Rewrite search query with proper MyBatis conditional logic
  - [x] SubTask 1.2: Test search with various keywords (empty, short, normal)

- [x] Task 2: Fix TagCloud API path
  - [x] SubTask 2.1: Change TagCloud.vue to use correct API path
  - [x] SubTask 2.2: Test tag display on homepage

- [x] Task 3: Fix mobile responsive layout in HomeView
  - [x] SubTask 3.1: Reorder grid so sidebar appears above articles on mobile
  - [x] SubTask 3.2: Test responsive layout

- [x] Task 4: Add public tags API endpoint
  - [x] SubTask 4.1: Add public endpoint for tags list in backend
  - [x] SubTask 4.2: Update frontend API to use public endpoint

- [x] Task 5: Add admin category management
  - [x] SubTask 5.1: Create CategoryManagementView component
  - [x] SubTask 5.2: Add route for category management
  - [x] SubTask 5.3: Integrate into AdminSidebar navigation

- [x] Task 6: Add admin tag management
  - [x] SubTask 6.1: Create TagManagementView component
  - [x] SubTask 6.2: Add route for tag management
  - [x] SubTask 6.3: Integrate into AdminSidebar navigation

# Task Dependencies

- [Task 2] depends on [Task 4]
- [Task 5] and [Task 6] can be done in parallel after [Task 1, 2, 3] are complete
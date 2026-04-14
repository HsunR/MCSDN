# Tasks

- [x] Task 1: Add slug field to Category entity and generate slug from name
  - [x] SubTask 1.1: Modify Category.java to add slug field
  - [x] SubTask 1.2: Create migration V4 to add slug column to categories table
  - [x] SubTask 1.3: Update CategoryService to generate slug when creating category

- [x] Task 2: Add slug field to Tag entity and generate slug from name
  - [x] SubTask 2.1: Modify Tag.java to add slug field
  - [x] SubTask 2.2: Create migration V5 to add slug column to tags table
  - [x] SubTask 2.3: Update TagService to generate slug when creating tag

- [x] Task 3: Fix search functionality
  - [x] SubTask 3.1: Modify ArticleMapper.xml search query to handle empty/short keywords
  - [x] SubTask 3.2: Add fallback to LIKE search when fulltext search returns no results
  - [x] SubTask 3.3: Test search with various keywords

- [x] Task 4: Separate admin and public navigation in App.vue
  - [x] SubTask 4.1: Modify App.vue to check route path
  - [x] SubTask 4.2: Conditionally render AdminSidebar or PublicHeader based on route
  - [x] SubTask 4.3: Test admin pages show correct navigation

# Task Dependencies

- [Task 1] and [Task 2] can be done in parallel
- [Task 3] depends on database migrations being applied
- [Task 4] can be done independently
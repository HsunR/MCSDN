# Checklist

- [x] Category entity has slug field
- [x] Tag entity has slug field
- [x] Database migration V4 adds slug column to categories
- [x] Database migration V5 adds slug column to tags
- [x] CategoryService generates slug when creating category
- [x] TagService generates slug when creating tag
- [x] Search returns empty results for empty keyword
- [x] Search returns empty results for keyword shorter than 2 characters
- [x] Search uses LIKE fallback when fulltext returns no results
- [x] App.vue shows AdminSidebar for /admin/* routes (via individual admin views)
- [x] App.vue shows PublicHeader for non-admin routes
- [ ] Admin pages have correct navigation (verified by running app)

**Note:** Database migrations V4 and V5 must be run for slug columns to be added to existing data. Restart the backend after migrations.
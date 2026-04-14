# Fix Category, Tag, Search and Admin Header Spec

## Why

当前存在以下问题：
1. 分类和标签功能不完善 - Category和Tag实体缺少slug字段，导致前端无法正确构建slug
2. 搜索功能有bug - 任意搜索词都返回全部文章，因为MySQL全文搜索在keyword为空或短词时行为异常
3. 后台顶部栏和前台一样 - Admin页面应该使用独立的AdminSidebar，而不是显示PublicHeader

## What Changes

### 1. 修复Category和Tag实体
- Category实体添加slug字段，自动从name生成
- Tag实体添加slug字段，自动从name生成
- 数据库迁移脚本添加slug列

### 2. 修复搜索功能
- 当keyword为空时返回空结果，而不是全部文章
- 当keyword过短时返回空结果或提示用户
- 使用LIKE进行模糊搜索作为后备方案

### 3. 分离后台顶部栏
- App.vue检测路由是否为/admin开头
- /admin路由使用AdminSidebar
- 非/admin路由使用PublicHeader

## Impact

- Affected specs: 分类浏览、标签浏览、搜索功能、管理后台
- Affected code:
  - `backend/src/main/java/com/blog/entity/Category.java`
  - `backend/src/main/java/com/blog/entity/Tag.java`
  - `backend/src/main/resources/mapper/ArticleMapper.xml`
  - `backend/src/main/resources/db/migration/V4__add_slugs.sql`
  - `frontend/src/App.vue`

## ADDED Requirements

### Requirement: Category Slug
分类应该有一个slug字段，用于URL生成。

### Requirement: Tag Slug
标签应该有一个slug字段，用于URL生成。

### Requirement: Fixed Search
搜索功能应该在keyword为空或过短时返回空结果，而不是全部文章。

## MODIFIED Requirements

### Requirement: App.vue Routing
App.vue应该根据路由前缀决定使用哪个导航组件：
- `/admin/*` 路由使用AdminSidebar
- 其他路由使用PublicHeader

## REMOVED Requirements

None.
# Fix Remaining Issues Spec

## Why

当前存在以下未解决的问题：
1. 搜索功能仍有问题 - 任意搜索返回全部文章（MyBatis if test 条件判断在SQL中不正确）
2. Tags显示"No tags"但数据库有tags（API路径错误，使用了/admin/tags）
3. 移动端响应式布局问题 - 侧边栏（分类、标签）在文章列表下方，应该在上方
4. 需要后台添加分类管理和标签管理功能

## What Changes

### 1. 修复搜索功能
- 修复 ArticleMapper.xml 中的 MyBatis if test 条件判断
- 使用正确的 MyBatis XML 语法处理空关键词和短关键词

### 2. 修复 Tags 显示问题
- TagCloud.vue 使用正确的 API 路径 `/api/admin/tags`

### 3. 修复移动端布局
- 调整 HomeView.vue 的 grid 布局顺序
- 在小屏幕上让侧边栏（分类、标签）显示在文章列表上方

### 4. 添加后台分类和标签管理
- 创建 TagController 的公开访问端点（或修改现有端点）
- 后台添加分类管理页面
- 后台添加标签管理页面

## Impact

- Affected specs: 搜索功能、标签显示、响应式布局、管理后台
- Affected code:
  - `backend/src/main/resources/mapper/ArticleMapper.xml`
  - `frontend/src/components/public/TagCloud.vue`
  - `frontend/src/views/public/HomeView.vue`
  - `backend/src/main/java/com/blog/controller/TagController.java`
  - `backend/src/main/java/com/blog/controller/CategoryController.java`
  - New admin views for category and tag management

## ADDED Requirements

### Requirement: Public Tags API
系统应该提供公开的标签列表 API，供前台页面使用。

### Requirement: Admin Category Management
后台应该提供分类管理功能，包括列表、添加、编辑、删除分类。

### Requirement: Admin Tag Management
后台应该提供标签管理功能，包括列表、添加、编辑、删除标签。

## MODIFIED Requirements

### Requirement: Fixed Search
搜索功能在关键词为空或过短时必须返回空结果，而不是全部文章。

### Requirement: Mobile Layout
在移动端（屏幕宽度小于 lg），分类和标签应该显示在文章列表上方。
# Fix Admin Article Issues Spec

## Why

当前存在以下问题需要修复：
1. 文章编辑页面 `/admin/articles/new` 没有分类选择功能
2. 图片上传后不能正确显示（Markdown预览中的图片URL问题）
3. 分类管理和标签管理页面的删除功能使用alert弹窗，不够友好
4. 文章列表页面 `/admin/articles` 需要添加根据分类、标签筛选和搜索功能

## What Changes

### 1. 添加文章编辑页面的分类选择
- 在 ArticleEditorView 中添加分类下拉选择
- 从 API 获取分类列表

### 2. 修复图片上传显示问题
- 检查 MarkdownEditor 的预览渲染逻辑
- 确保上传后的图片 URL 正确解析和显示

### 3. 替换 alert 弹窗为更友好的确认方式
- 使用内联确认按钮或确认模态框替代 alert

### 4. 文章列表添加筛选和搜索功能
- 添加分类筛选下拉框
- 添加标签筛选下拉框
- 添加搜索框
- 实现组合筛选逻辑

## Impact

- Affected specs: 文章编辑、文章列表、分类管理、标签管理
- Affected code:
  - `frontend/src/views/admin/ArticleEditorView.vue`
  - `frontend/src/components/admin/MarkdownEditor.vue`
  - `frontend/src/views/admin/ArticleListView.vue`
  - `frontend/src/views/admin/CategoryManagementView.vue`
  - `frontend/src/views/admin/TagManagementView.vue`

## ADDED Requirements

### Requirement: Category Selection in Article Editor
文章编辑页面必须提供分类选择下拉框，从API获取分类列表。

### Requirement: Inline Confirmation
删除操作使用内联确认按钮，而不是浏览器 alert 弹窗。

### Requirement: Article List Filtering
文章列表页面应支持按分类、标签筛选和关键词搜索。

## MODIFIED Requirements

### Requirement: Image Upload Display
上传的图片应能正确在Markdown预览中显示。
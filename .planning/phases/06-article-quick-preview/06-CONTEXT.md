# Phase 6: Article Quick Preview - Context

**Gathered:** 2026-04-19
**Status:** Ready for planning
**Mode:** Auto-generated (discuss skipped via workflow.skip_discuss)

<domain>
## Phase Boundary

在文章列表页（HomeView.vue）每个 ArticleCard 上添加"快速预览"按钮，点击后打开全屏 Modal 显示完整文章内容（Markdown 渲染）。用户无需跳转详情页即可预览。

</domain>

<decisions>
## Implementation Decisions

### Modal vs Inline Expand
全屏 Modal 方式实现，无需担心破坏列表布局。

### Preview Button Position
在文章卡片的 meta 信息行（标签、日期旁边）添加眼睛图标按钮。

### Markdown Rendering
复用 ArticleView.vue 中已有的 markdown-it + highlight.js 配置，确保代码高亮一致。

### State Management
在 ArticleCard 中通过 props 接收 article，Modal 组件内部使用 store.fetchArticle(id) 获取完整内容。

### Closing Behavior
点击遮罩背景或右上角关闭按钮关闭 Modal，回到列表保持分页位置。

### Claude's Discretion
所有实现细节（组件结构、样式、动画）由 Claude 自行决定。

</decisions>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- ArticleView.vue: Markdown 渲染配置（markdown-it + highlight.js）可直接复用
- store: publicArticleStore.js 的 fetchArticle(id) 方法
- Modal pattern: admin 端 ImageUploadModal.vue 有 Modal 参考

### Established Patterns
- Tailwind dark theme: `bg-gray-900`, `text-gray-100`, `border-gray-700`
- 图标组件使用内联 SVG（heroicons 风格）
- Modal 遮罩: `fixed inset-0 bg-black/50 backdrop-blur-sm`

### Integration Points
- HomeView.vue: 需要导入 QuickPreviewModal 组件
- ArticleCard.vue: 需要添加预览按钮和 Modal 组件

</codebase_context>

<specifics>
## Specific Ideas

- 按钮样式：眼睛图标，hover 时变蓝，位置在文章标题下方 meta 行
- Modal 宽度：max-w-4xl，居中，padding 4
- Modal 内容：标题 + 元信息 + Markdown 内容（prose-invert）
- 关闭按钮：右上角 X 图标，hover 变亮

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>
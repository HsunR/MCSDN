# 个人博客系统

## What This Is

一个面向程序员的个人博客网站，采用前后端分离架构。前台展示博客文章，支持 Markdown 渲染、代码高亮、图片展示；后台独立部署用于文章管理和登录认证。v1.0 MVP 已完成，包含完整的管理后台和公开博客。

## Core Value

快速搭建一个属于自己的技术博客空间，专注于内容创作，无需操心复杂功能。

## Requirements

### Validated

- ✓ JWT 认证与 BCrypt 密码 hashing — v1.0
- ✓ Spring Security 过滤链保护管理 API — v1.0
- ✓ 登录限流（5次/分钟/IP）— v1.0
- ✓ 文章 CRUD（标题、Markdown 内容、分类、标签）— v1.0
- ✓ Markdown 代码高亮（JS/TS/Python/Go/Java/SQL）— v1.0
- ✓ 文章发布/草稿状态，自动时间戳 — v1.0
- ✓ 全站 Dark Theme — v1.0
- ✓ 公开博客：文章列表（分页）、详情、分类/标签筛选、全文搜索 — v1.0
- ✓ 分类/标签管理 — v1.0
- ✓ 图片上传（本地存储，5MB，jpg/png/gif/webp）— v1.0
- ✓ 评论系统（待审核流程）— v1.0

### Active

_(None yet — start v1.1 with `/gsd-new-milestone`)_

### Out of Scope

| Feature | Reason |
|---------|--------|
| 社交登录 (OAuth) | 本地部署，单管理员，密码认证足够 |
| 多用户/权限管理 | 单人运营，不需复杂权限 |
| 访问统计/Analytics | 本地部署，隐私优先 |
| 邮件通知 | SMTP 依赖，本地部署中继问题 |
| RSS/Atom | 个人博客非必需品 |
| 主题切换 (dark/light) | 明确 dark-only，无需切换 |
| 移动原生 app | 个人博客不需要 |
| 第三方评论 (Disqus) | 隐私问题，自托管优先 |
| 内容版本管理 | 复杂度高，暂不需要 |
| 定时发布 | 现有 publish-now 足够 |

## Context

**技术栈（已确定）：**
- 前端：Vue 3 + Tailwind CSS + Vue Router + Pinia + Axios
- 后端：Spring Boot 3 + MyBatis + Spring Security + JWT
- 数据库：MySQL（本地，utf8mb4）
- Markdown：markdown-it + highlight.js
- 存储：本地文件系统 `/uploads/{year}/{month}/{uuid}.{ext}`
- 迁移：Flyway

**部署阶段：** 开发阶段本地部署，端口 8080 (后端) / 5173 (前端)

**v1.0 交付：**
- 3 phases, 9 plans, 29 tasks
- 150 files changed, 18,852 lines added
- 2 天开发 (2026-04-13 → 2026-04-15)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Vue + Spring Boot 分离架构 | 主流前后端分离方案，便于开发和扩展 | ✅ 验证可行 |
| Tailwind CSS | 原子化 CSS，快速实现 dark 程序员风格 | ✅ 验证可行 |
| MyBatis 代替 JPA | SQL 直视可控，适合业务简单的博客 | ✅ 验证可行 |
| JWT 无状态认证 | 适合前后端分离的 REST API | ✅ 验证可行 |
| 本地文件系统存储图片 | 简化部署，避免数据库膨胀 | ✅ 验证可行 |
| 评论默认 PENDING，管理员审核 | 避免垃圾评论，self-hosted 方案 | ✅ 验证可行 |
| Flyway 数据库迁移 | 版本控制的 schema 变更 | ✅ 验证可行 |
| Dark theme only | 程序员风格，无需 light 切换 | ✅ 验证可行 |

## Current Milestone: v1.1 CSDN 文章同步

**Goal:** 让用户把 CSDN 博客文章一键同步到本博客系统，包含图片本地化和标签同步

**Target features:**
- CSDN 同步工具增强 — 解析 CSDN 文章页提取 tags，复用图片下载逻辑
- 同步配置 API — 后台配置 CSDN userId + 目标分类 + 定时开关
- 文章解析导入 — 解析 HTML → Markdown，提取标题/内容/标签，下载图片到本地
- 手动同步 — 后台点击"同步"按钮立即执行
- 定时自动同步 — 可开启，每天按配置时间自动拉取新文章
- 同步去重 + 增量更新 — 根据 CSDN articleId 判断：新增/增量更新/跳过
- 图片去重 — 根据 URL hash 去重，已存在的图片不重复下载
- 同步文章标记 — `source: CSDN`, `csdn_article_id: xxx`
- 编辑警告 — 编辑同步文章时弹窗提醒"该文章为同步文章，不建议编辑"

## Constraints

- **数据库**：MySQL 本地安装，端口 3306，密码 123456
- **存储**：图片存储在服务器本地文件系统
- **部署**：仅开发阶段本地运行，不考虑生产部署

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-16 after phase 05 (image-handling-sync-ui) complete — v1.1 CSDN sync milestone finished*

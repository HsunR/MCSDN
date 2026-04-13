# 个人博客系统

## What This Is

一个面向程序员的个人博客网站，采用前后端分离架构。前台展示博客文章，支持 Markdown 渲染、代码高亮、图片展示；后台独立部署用于文章管理和登录认证。

## Core Value

快速搭建一个属于自己的技术博客空间，专注于内容创作，无需操心复杂功能。

## Requirements

### Active

- [ ] 博客前台：文章列表、分类标签、评论、搜索
- [ ] Markdown 编辑器：支持代码高亮、图片插入
- [ ] 管理后台：文章 CRUD、分类标签管理、评论管理
- [ ] 用户认证：后台登录保护
- [ ] 公开访问：无需登录即可浏览博客

### Out of Scope

- 多用户/权限管理（单人运营）
- 评论通知/邮件提醒
- 访问统计/Analytics
- 生产环境部署/容器化

## Context

**技术栈（已确定）：**
- 前端：Vue 3 + Tailwind CSS + Vue Router
- 后端：Spring Boot 3 + MyBatis
- 数据库：MySQL（本地，密码 123456）
- Markdown：支持代码高亮（JS/TS/Python/Go/Java/SQL 等）
- 图片：本地服务器存储

**部署阶段：** 开发阶段本地部署，仅局域网访问

## Constraints

- **数据库**：MySQL 本地安装，端口 3306，密码 123456
- **存储**：图片存储在服务器本地文件系统
- **部署**：仅开发阶段本地运行，不考虑生产部署

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Vue + Spring Boot 分离架构 | 主流前后端分离方案，便于开发和扩展 | — Pending |
| Tailwind CSS | 原子化 CSS，快速实现 dark 程序员风格 | — Pending |
| MyBatis 代替 JPA | SQL 直观可控，适合业务简单的博客 | — Pending |
| Markdown 存数据库 | 便于编辑和渲染，图片路径统一管理 | — Pending |

---
*Last updated: 2026-04-13 after initialization*

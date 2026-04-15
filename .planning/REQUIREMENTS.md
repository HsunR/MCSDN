# Requirements: 个人博客系统 v1.1

**Defined:** 2026-04-15
**Core Value:** 快速搭建一个属于自己的技术博客空间，专注于内容创作，无需操心复杂功能。

## v1 Requirements

### CSDN 同步配置

- [ ] **SYNC-01**: 管理员可在后台配置 CSDN userId（如 `2301_78723800`）
- [ ] **SYNC-02**: 管理员可指定同步文章的目标分类（下拉选择已有分类）
- [ ] **SYNC-03**: 同步配置持久化到数据库（CsdnSyncConfig 表）

### CSDN 文章同步

- [ ] **SYNC-04**: 管理员点击"同步"按钮，立即从 CSDN 拉取配置用户的所有文章列表
- [ ] **SYNC-05**: 根据 CSDN articleId 判断文章是否存在（去重）：
  - 不存在 → 新增导入（发布状态）
  - 存在且远程更新过 → 增量更新
  - 存在且未更新 → 跳过
- [ ] **SYNC-06**: 解析 CSDN 文章 HTML，提取标题、内容、标签（tags）
- [ ] **SYNC-07**: HTML 内容中的 CSDN 图片 URL 替换为本地存储路径
- [ ] **SYNC-08**: 同步后的文章标记 `source: CSDN`，记录 `csdn_article_id`
- [ ] **SYNC-09**: 图片根据 URL hash（MD5）去重，已存在的图片不重复下载

### 同步管理界面

- [ ] **SYNC-10**: 后台显示同步配置表单（CSDN userId 输入框 + 目标分类下拉）
- [ ] **SYNC-11**: 后台显示"同步"按钮，点击触发同步
- [ ] **SYNC-12**: 后台显示同步结果（成功/失败/跳过数量）
- [ ] **SYNC-13**: 编辑同步文章时，弹窗警告"该文章为同步文章，不建议编辑"

### 数据库迁移

- [ ] **SYNC-14**: Flyway V2 — 新增 `csdn_sync_config` 表（user_id, category_id, enabled, last_sync_at）
- [ ] **SYNC-15**: Flyway V3 — 文章表新增 `source` 字段（varchar，nullable）和 `csdn_article_id` 字段（varchar，nullable，unique）

## v2 Requirements

（定时自动同步，移至 v1.2）

### 定时同步

- **SYNC-16**: 开启/关闭定时同步开关
- **SYNC-17**: 配置定时同步 cron 表达式
- **SYNC-18**: 定时任务按配置时间自动执行同步
- **SYNC-19**: 定时同步异步执行，不阻塞主线程

## Out of Scope

| Feature | Reason |
|---------|--------|
| CSDN OAuth 登录 | 单人运营，不需要打通 CSDN 账号体系 |
| 同步文章删除 | 保留本地文章，CSDN 删除不影响 |
| 按标签选择性同步 | v1 只做全量同步，按标签筛选 v2 再做 |
| CSDN 评论同步 | 评论系统独立，不从 CSDN 拉取 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| SYNC-01 | Phase 1 | Pending |
| SYNC-02 | Phase 1 | Pending |
| SYNC-03 | Phase 1 | Pending |
| SYNC-04 | Phase 1 | Pending |
| SYNC-05 | Phase 1 | Pending |
| SYNC-06 | Phase 1 | Pending |
| SYNC-07 | Phase 1 | Pending |
| SYNC-08 | Phase 1 | Pending |
| SYNC-09 | Phase 2 | Pending |
| SYNC-10 | Phase 2 | Pending |
| SYNC-11 | Phase 2 | Pending |
| SYNC-12 | Phase 2 | Pending |
| SYNC-13 | Phase 2 | Pending |
| SYNC-14 | Phase 1 | Pending |
| SYNC-15 | Phase 1 | Pending |
| SYNC-16 | v1.2 | Pending |
| SYNC-17 | v1.2 | Pending |
| SYNC-18 | v1.2 | Pending |
| SYNC-19 | v1.2 | Pending |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-15*
*Last updated: 2026-04-15 after initial definition*

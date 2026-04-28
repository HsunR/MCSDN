# Redis 缓存集成规格文档

## Why
当前博客系统每次请求都需要查询数据库，对于文章列表、文章详情等读多写少的场景，性能开销较大。通过引入 Redis 缓存，可以显著减少数据库压力，提高系统响应速度，改善用户体验。

## What Changes
- 添加 Spring Data Redis 依赖
- 配置 Redis 连接（host: localhost, port: 6379, 无密码）
- 创建 Redis 配置类和工具类
- 为文章相关接口添加缓存支持：
  - 文章列表缓存（带分页和分类/标签筛选）
  - 文章详情缓存
  - 搜索结果的短期缓存
- 实现缓存失效策略：文章创建、更新、删除时清除相关缓存

## Impact
- 受影响模块：ArticleService, PublicArticleService 及相关实现类
- 新增文件：RedisConfig.java, RedisCacheUtil.java
- 配置文件更新：application.yml 添加 Redis 配置
- 构建文件更新：pom.xml 添加 Redis 依赖

## ADDED Requirements

### Requirement: Redis 基础配置
系统 SHALL 提供 Redis 连接配置和基础工具类

#### Scenario: Redis 连接成功
- **GIVEN** Redis 服务运行在 localhost:6379
- **WHEN** 应用启动
- **THEN** 成功建立 Redis 连接

### Requirement: 文章列表缓存
系统 SHALL 缓存文章列表查询结果

#### Scenario: 获取文章列表命中缓存
- **GIVEN** 用户已请求过相同参数的文章列表
- **WHEN** 再次请求相同参数的文章列表
- **THEN** 从 Redis 缓存返回结果，不查询数据库

#### Scenario: 文章列表缓存失效
- **GIVEN** 文章列表已缓存
- **WHEN** 有新文章发布或文章状态变更
- **THEN** 清除相关缓存，下次请求重新从数据库加载

### Requirement: 文章详情缓存
系统 SHALL 缓存已发布文章的详情

#### Scenario: 获取文章详情命中缓存
- **GIVEN** 用户已访问过某篇文章详情
- **WHEN** 再次访问同一篇文章
- **THEN** 从 Redis 缓存返回结果

#### Scenario: 文章更新时清除缓存
- **GIVEN** 某篇文章详情已缓存
- **WHEN** 该文章被更新或删除
- **THEN** 清除该文章的缓存

### Requirement: 搜索结果缓存
系统 SHALL 短期缓存搜索结果

#### Scenario: 搜索请求命中缓存
- **GIVEN** 用户在短时间内使用相同关键词搜索
- **WHEN** 再次使用相同关键词搜索
- **THEN** 从缓存返回搜索结果（缓存时间 5 分钟）

## MODIFIED Requirements
无

## REMOVED Requirements
无

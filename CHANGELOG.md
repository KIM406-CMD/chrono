# Changelog

本文件记录 chrono-bbs 项目的所有重要变更。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### Added
- 项目初始化：Spring Boot 3.4.5 + Java 21 后端骨架
- Maven 多模块结构：`common` / `module-mapper` / `module-service` / `module-api` / `module-start`
- 公共模块：统一返回体 `Result<T>` / `PageResult<T>`、业务异常、错误码枚举、JWT 工具类、用户上下文
- 数据访问层：19 张表的 Entity、Mapper 接口及 XML 映射文件
- 业务层：用户认证、商品、分类、购物车、订单、支付、退款、商家、管理员、评论、搜索、聊天等 Service 接口与实现
- API 层：12 个 Controller + 全局异常处理器
- 启动模块：Spring Security 配置、Knife4j 接口文档、JWT 拦截器、Web MVC 配置
- 配置文件：`application.yaml` / `application-dev.yml` / `application-prod.yml`
- Docker Compose 编排：MySQL 8.0、Redis 7、Elasticsearch 7.17
- 数据库初始化脚本：`initial/schema.sql`（建表）、`initial/data.sql`（初始数据）
- 文档：PRD、需求分析、可行性分析、服务端/客户端设计、数据库设计、详细设计、测试规范、API 文档
- UML 图：用例图、组件图、ER 图、时序图、活动图（PlantUML）
- 单元测试与集成测试：JWT 工具、认证 Service、认证接口 E2E
- Git 提交规范文档与 AI 助手规则（AGENTS.md）

---

## [0.1.0] - 2026-10-26

### Added
- 首次提交：项目基础骨架与文档

---

## 版本类型说明

| 类型 | 说明 |
|------|------|
| `Added` | 新增功能 |
| `Changed` | 对现有功能的变更 |
| `Deprecated` | 即将废弃的功能 |
| `Removed` | 已移除的功能 |
| `Fixed` | Bug 修复 |
| `Security` | 安全相关修复 |

---

## 维护规范

- 每次合并到 `dev` 或 `main` 时，在 `Unreleased` 下追加对应条目
- 发布新版本时，将 `Unreleased` 内容移入对应版本号，并补充日期
- 条目使用简体中文，格式：`- 简短描述`
- 同一版本内按 Added → Changed → Fixed → Removed → Security 顺序排列
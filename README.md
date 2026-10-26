# 🛒 网上超市系统 (Chrono BBS)

> 课程设计项目 — 基于 Spring Boot + Vue 3 的全栈电商系统

---

## 📑 目录

- [项目简介](#-项目简介)
- [团队信息](#-团队信息)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速启动](#-快速启动)
- [文档索引](#-文档索引)
- [UML 图索引](#-uml-图索引)
- [核心功能模块](#-核心功能模块)
- [安全设计](#-安全设计)
- [项目进度](#-项目进度)
- [贡献指南](#-贡献指南)
- [许可证](#-许可证)

---

## 📋 项目简介

网上超市系统是一个 B2B2C 多商家电商平台，支持游客浏览商品、注册用户购物下单、商家管理商品与订单、管理员全局管理及销售报表分析。

**原始题目描述**：

> 用户通过浏览器访问网上超市系统，系统以分类的形式显示所有商品，帮助用户逐步找到所要的商品；系统也提供关键词检索功能；用户在浏览商品目录时可以点击查看商品的具体信息和价格；如果满意，用户可以将商品暂时放入"购物车"；也可以随时从"购物车"中取出商品。当用户选完后可以进行付款处理，这时用户输入信用卡号，系统联系对应的银行支付系统，开始支付。最后系统应能够向管理员提供查询界面和各类报表，统计商品的销售情况。

---

## 🧑‍💻 团队信息

| 角色 | 人数 | 主要职责 |
|------|------|----------|
| 后端开发 | 1 人 | 用户/认证/鉴权、商品/分类/SKU、购物车/订单/支付/退款、销售报表、ES 集成、WebSocket |
| 前端开发 | 1 人 | Vue 3 前端全部页面、Axios 封装、WebSocket |
| 测试人员 | 1 人 | 测试用例编写、单元测试、集成测试、E2E 测试、缺陷跟踪 |

- **开发周期**：6 周（约 1.5 个月）
- **演示方式**：个人笔记本本地运行，Docker 编排中间件

---

## 🛠 技术栈

| 层级 | 技术 | 版本/说明 |
|------|------|-----------|
| **后端框架** | Spring Boot | 3.4.5 |
| **开发语言** | Java / TypeScript | Java 21 + TS 5.x |
| **ORM** | MyBatis | 3.0.4 |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis | 7.x |
| **搜索引擎** | Elasticsearch | 7.17.x |
| **前端框架** | Vue 3 + Vite | Composition API |
| **UI 组件库** | Element Plus | Vue 3 官方推荐 |
| **状态管理** | Pinia | Vue 3 官方推荐 |
| **构建工具** | Vite | 开发热更新 + Rollup 打包 |
| **认证鉴权** | JWT + Redis + Spring AOP | RBAC 权限控制 |
| **即时通讯** | WebSocket (JSR 356) | 用户与商家实时聊天 |
| **接口文档** | Knife4j (OpenAPI 3.0) | 自动生成 API 文档 |
| **容器化** | Docker + Docker Compose | 中间件一键编排 |
| **部署** | Nginx | 前后端分离 + 反向代理 |

---

## 📁 项目结构

```
chrono-bbs/
├── chrono-bbs-server/                # 后端（Maven 多模块）
│   ├── common/                       # 公共模块（Result / 异常 / 工具类）
│   ├── module-mapper/                # 数据访问（Entity + Mapper + XML）
│   ├── module-service/               # 业务逻辑（Service 接口 + Impl + DTO）
│   ├── module-api/                   # API 层（Controller + VO + 拦截器）
│   ├── module-start/                 # 启动模块（配置 + 定时任务）
│   └── pom.xml                       # 父 POM
│
├── chrono-bbs-client/                # 前端（Vue 3 + Vite + TS + Element Plus）
│
├── docs/                             # 📄 文档目录
│   ├── prd.md                        # 产品需求文档
│   ├── requirements-analysis.md      # 需求分析
│   ├── feasibility-analysis.md       # 可行性分析
│   ├── server-design.md              # 服务端概要设计
│   ├── client-design.md              # 客户端概要设计
│   ├── db-design.md                  # 数据库设计
│   ├── detailed-design.md            # 详细设计（含流程图）
│   ├── api-doc.md                    # API 接口文档
│   ├── test-specification.md         # 测试规范
│   └── git-commit-convention.md      # Git 提交规范
│
├── uml/                              # 📐 UML 图目录
│   ├── uml-usecase.puml              # 用例图
│   ├── uml-component.puml            # 组件图
│   ├── uml-er.puml                   # ER 图
│   ├── uml-sequence.puml             # 时序图
│   └── uml-activity.puml             # 活动图
│
├── docker/                           # 🐳 Docker 编排
│   └── docker-compose.yml            # MySQL + Redis + ES
│
├── initial/                          # 🗄 数据库初始化脚本
│   ├── schema.sql                    # 建表语句
│   └── data.sql                      # 初始数据
│
├── AGENTS.md                         # 🤖 AI 助手行为规范
├── CHANGELOG.md                      # 📝 变更日志
└── README.md                         # 本文件
```

---

## 🚀 快速启动

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Node.js | 18+ |
| Docker Desktop | 最新版 |
| Maven | 3.8+ |

### 1. 启动中间件

```bash
cd docker
docker-compose up -d
```

启动 MySQL、Redis、Elasticsearch。

### 2. 初始化数据库

```bash
# 执行建表语句
mysql -u root -p < initial/schema.sql
# 导入初始数据
mysql -u root -p < initial/data.sql
```

### 3. 启动后端

```bash
cd chrono-bbs-server
mvn clean install -DskipTests
cd module-start
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，Knife4j 文档在 `http://localhost:8080/doc.html`。

### 4. 启动前端

```bash
cd chrono-bbs-client
npm install
npm run dev
```

前端运行在 `http://localhost:5173`。

### 5. 默认测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |

> 普通用户可通过前端注册功能自行创建。

---

## 📖 文档索引

| 文档 | 内容 |
|------|------|
| [prd.md](./docs/prd.md) | 产品需求文档 — 功能模块、角色、业务流程 |
| [requirements-analysis.md](./docs/requirements-analysis.md) | 需求分析 — 47 项功能需求 + 优先级排序 |
| [feasibility-analysis.md](./docs/feasibility-analysis.md) | 可行性分析 — 技术/进度/经济/运行四维度 |
| [server-design.md](./docs/server-design.md) | 服务端概要设计 — Maven 模块 / 认证 / 异常体系 |
| [client-design.md](./docs/client-design.md) | 客户端概要设计 — 目录结构 / 路由 / 状态管理 |
| [db-design.md](./docs/db-design.md) | 数据库设计 — 表结构 / 字段 / 索引 |
| [detailed-design.md](./docs/detailed-design.md) | 详细设计 — Mermaid 流程图 / 时序图 / 部署架构 |
| [api-doc.md](./docs/api-doc.md) | API 接口文档 — 接口列表与请求响应示例 |
| [test-specification.md](./docs/test-specification.md) | 测试规范 — 单元/集成/E2E 测试要求 |
| [git-commit-convention.md](./docs/git-commit-convention.md) | Git 提交规范 — Conventional Commits + 分支模型 |
| [CHANGELOG.md](./CHANGELOG.md) | 变更日志 — 版本发布记录 |
| [AGENTS.md](./AGENTS.md) | AI 助手规范 — 编码与提交行为约束 |

## 📐 UML 图索引

| 图 | 工具 | 内容 |
|----|------|------|
| [uml-usecase.puml](./uml/uml-usecase.puml) | PlantUML | 用例图 — 游客/用户/商家/管理员 4 角色 |
| [uml-component.puml](./uml/uml-component.puml) | PlantUML | 组件图 — Maven 模块 + 外部依赖 |
| [uml-er.puml](./uml/uml-er.puml) | PlantUML | ER 图 — 14 张表关联关系 |
| [uml-sequence.puml](./uml/uml-sequence.puml) | PlantUML | 时序图 — 注册登录 / 购物下单 / 支付 / WebSocket |
| [uml-activity.puml](./uml/uml-activity.puml) | PlantUML | 活动图 — 订单状态流转 / 商家入驻 / 退款退货 |

---

## 🧩 核心功能模块

| 模块 | 功能描述 |
|------|----------|
| 👤 用户模块 | 注册、登录（JWT + Redis）、个人信息、收货地址管理 |
| 🏪 商品模块 | 三级分类浏览、商品详情、多规格 SKU |
| 🔍 搜索模块 | Elasticsearch 全文检索、搜索建议、多条件筛选排序 |
| 🛒 购物车 | 登录态购物车（数据库存储）、数量修改、库存校验 |
| 📦 订单模块 | 下单（防重 Token + 分布式锁）、订单状态流转 |
| 💳 支付模块 | 模拟支付（Luhn 算法卡号校验 + 二维码支付） |
| 🔄 退款/退货 | 申请 → 审核 → 退货寄回 → 退款 → 恢复库存 |
| 🏬 商家后台 | 入驻审核、商品/库存管理、发货、退款审核、销售统计 |
| 🛡 管理员后台 | 用户管理、商家审核、分类管理、全局报表（6 类） |
| 💬 即时通讯 | WebSocket 用户与商家实时聊天（低优先级） |

---

## 🔒 安全设计

- 密码 BCrypt 加密存储
- JWT + Redis 双校验认证（支持主动踢人）
- Spring AOP + `@RequireRole` RBAC 权限控制
- 下单防重 Token（Redis 原子 DEL）
- 库存扣减乐观锁（`WHERE stock >= ?`）
- 支付幂等（基于 `order_id` 去重）
- MyBatis 参数化查询防 SQL 注入

---

## 📊 项目进度

| 阶段 | 时间 | 任务 |
|------|------|------|
| 设计 + 环境搭建 | 第 1-2 周 | 需求确认、DB 设计、接口定义、Docker 环境、ES 调研 |
| 核心功能开发 | 第 3-5 周 | 用户/商品/购物车/订单/支付 CRUD、商家/管理员后台、ES 搜索 |
| 收尾 + 答辩 | 第 6 周 | 销售报表、退款/退货、联调测试、Bug 修复、答辩 PPT |

---

## 🤝 贡献指南

本项目为课程设计团队项目，开发前请务必阅读以下规范：

- [Git 提交规范](./docs/git-commit-convention.md) — 提交信息格式与分支模型
- [AGENTS.md](./AGENTS.md) — AI 助手行为规范（编码风格与提交约束）

开发流程：从 `dev` 分支拉取 → 创建 `feature/*` 分支 → 开发 → PR 合并到 `dev` → 稳定后合并到 `main`。

---

## 📄 许可证

本项目为课程设计作业，**All Rights Reserved**，未经授权不得商用或再分发。
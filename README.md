# 🛒 网上超市系统 (Chrono BBS)

> [English](./README.en.md) | 简体中文

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
| **搜索引擎** | Elasticsearch | 8.10.x |
| **前端框架** | Vue 3 + Vite | Composition API |
| **UI 组件库** | Element Plus | Vue 3 官方推荐 |
| **状态管理** | Pinia | Vue 3 官方推荐 |
| **构建工具** | Vite | 开发热更新 + Rollup 打包 |
| **认证鉴权** | JWT + Redis | 拦截器认证 + Service 层角色/数据归属校验 |
| **即时通讯** | WebSocket (JSR 356) | 用户与商家实时聊天 |
| **接口文档** | springdoc-openapi + knife4j-ui | 自动生成 API 文档 |
| **图片存储** | 本地目录 / 阿里云 OSS + Thumbnailator | 开发默认本地，支持 OSS，800px 压缩 |
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
│   ├── zh/                           # 中文文档（需求/设计/API/测试/HMI 等）
│   └── en/                           # English documentation
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
│   ├── schema.sql                    # 建表语句（含最新字段）
│   ├── data.sql                      # 初始数据
│   ├── migration-2026-11-09.sql      # 已有库增量迁移（可重复执行）
│   └── demo-data.sql                 # 演示商家/商品/订单/评论（可重复执行）
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
| Maven | 3.8+ |
| Docker Desktop | 最新版（仅 Docker 模式需要） |

### 1. 一键启动（推荐）

Windows（cmd / PowerShell）：

```bat
script\start-all.bat            :: Docker 中间件（MySQL 3307）
script\start-all.bat --local    :: 本机中间件（MySQL 3306）
```

Bash / Git Bash / macOS / Linux：

```bash
./script/start-all.sh           # Docker 中间件（MySQL 3307）
./script/start-all.sh --local   # 本机中间件（MySQL 3306）
```

脚本会依次启动中间件、后端与前端：

| 服务 | 地址 |
|------|------|
| 前端 | <http://localhost:5173> |
| 后端 | <http://localhost:8080> |
| Knife4j 文档 | <http://localhost:8080/doc.html> |

> 若前端使用 Docker Nginx 容器（不启动 Vite），执行：`script\start-all.bat --docker-frontend --skip-build`（Bash：`./script/start-all.sh --docker-frontend --skip-build`），前端地址为 <http://localhost:3000>。
> Windows 会为后端和前端各打开一个新命令行窗口，关闭窗口即停止服务。
> Bash 下后端在后台运行，日志写入 `logs/backend.log`，`Ctrl+C` 同时停止前后端。
> 更多参数（`--skip-build`、`--install`、`--volumes`）见 [script/README.md](script/README.md)。

### 2. 分步启动（可选）

| 步骤 | Windows | Bash |
|------|---------|------|
| 启动中间件 | `script\start-middleware.bat` | `./script/start-middleware.sh` |
| 启动后端 | `script\start-backend.bat` | `./script/start-backend.sh` |
| 启动前端 | `script\start-frontend.bat` | `./script/start-frontend.sh` |
| 停止中间件 | `script\stop-middleware.bat` | `./script/stop-middleware.sh` |

### 3. Docker 部署前端（可选）

前端提供多阶段镜像（Node 构建 + Nginx 托管），后端仍运行在宿主机 `8080`：

```bash
cd docker
docker compose --profile frontend up -d --build nginx
```

| 服务 | 地址 |
|------|------|
| 前端（Nginx 容器） | <http://localhost:3000> |

> 默认映射宿主机 `3000`，可用 `FRONTEND_PORT=3001` 覆盖；若本机已有 Nginx 占用 3000，请先停止本机 Nginx。
> 容器通过 `host.docker.internal:8080` 访问后端；后端纳入 Compose 后可将 `deploy/nginx.docker.conf` 中的地址改为后端服务名。

> 默认使用 Docker 中间件（MySQL 映射到宿主机 `3307`）；追加 `--local` 使用本机已启动的 MySQL / Redis / Elasticsearch（MySQL 按 `application-dev.yml` 使用 `3306`）。

### 4. 本机中间件数据库初始化（可选）

使用 `--local` 且首次运行时，先初始化数据库：

```bash
mysql -u root -p < initial/schema.sql
mysql -u root -p < initial/data.sql
mysql -u root -p < initial/demo-data.sql
```

脚本位于 `initial/`，创建 `chrono_bbs_dev` 库；默认管理员 `admin` / `admin123`，演示商家与用户见下方测试账号。

> 已有数据库升级：执行 `mysql -u root -p chrono_bbs_dev < initial/migration-2026-11-09.sql`（可重复执行）；需要演示数据时再执行 `mysql -u root -p chrono_bbs_dev < initial/demo-data.sql`（可重复执行）。
> Docker 模式首次启动会按 `schema.sql → data.sql → migration-2026-11-09.sql → demo-data.sql` 顺序自动执行，无需手动导入。修改初始化脚本后需执行 `./script/stop-middleware.sh --volumes`（Windows：`script\stop-middleware.bat --volumes`）删除数据卷，重新启动后才会再次执行。
> 开发环境默认将图片写入后端本地 `uploads/` 目录（`upload.mode=local`），无需配置 OSS 即可上传；需要对象存储时，复制 `script/.env.example` 为根目录 `.env` 填写密钥并设置 `UPLOAD_MODE=oss`。

### 5. 默认测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 初始管理员 |
| 商家 | `merchant1` | `admin123` | 鲜果直供旗舰店（已审核） |
| 商家 | `merchant2` | `admin123` | 数码优选专营店（已审核） |
| 普通用户 | `user1` | `admin123` | 含地址、购物车、订单、退款与评论 |

> 演示账号由 `initial/demo-data.sql` 创建；普通用户也可通过前端注册功能自行创建。

### 6. 生产部署（Nginx）

```bash
cd chrono-bbs-client
npm run build
# 将 dist/ 下的内容部署到 Nginx 静态目录
```

将 `deploy/nginx.conf` 放入 Nginx 配置目录并重新加载。配置已包含：

- History 路由回退 `try_files $uri $uri/ /index.html`；
- `/api/` 反向代理到后端 `8080`；
- `client_max_body_size 10m`，满足商品图片上传；
- `/assets/` 静态资源长缓存与 gzip。

> 若 Nginx 运行在容器中，请把 `proxy_pass` 中的 `127.0.0.1` 改为后端服务名或 `host.docker.internal`。

生产环境启动后端前必须配置：`DB_PASSWORD`（数据库密码）、`JWT_SECRET`（至少 32 字节随机字符串）以及 OSS 密钥（如启用 OSS）。仓库中的 `application-dev.yml`、`application-test.yml` 和 `docker-compose.yml` 中的数据库口令仅用于本地开发/测试，请勿用于生产。

---

## 📖 文档索引

> 中文文档位于 `docs/zh/`，英文文档位于 `docs/en/`；每篇文档顶部提供语言切换。

| 文档 | 内容 |
|------|------|
| [requirements-analysis.md](./docs/zh/requirements-analysis.md) | 需求分析 — 业务背景、核心业务规则、48 项功能需求（F-01~F-48）、优先级、业务流程与数据需求 |
| [object-oriented-development.md](./docs/zh/object-oriented-development.md) | 面向对象开发 — 需求与用例、领域分析、职责分配、架构设计与详细设计步骤 |
| [feasibility-analysis.md](./docs/zh/feasibility-analysis.md) | 可行性分析 — 技术/进度/经济/运行四维度 |
| [server-design.md](./docs/zh/server-design.md) | 服务端概要设计 — Maven 模块 / 认证 / 异常体系 |
| [client-design.md](./docs/zh/client-design.md) | 客户端概要设计 — 目录结构 / 路由 / 状态管理 |
| [db-design.md](./docs/zh/db-design.md) | 数据库设计 — 表结构 / 字段 / 索引 |
| [detailed-design.md](./docs/zh/detailed-design.md) | 详细设计 — Mermaid 流程图 / 时序图 / 部署架构 |
| [api-doc.md](./docs/zh/api-doc.md) | API 接口文档 — 接口列表与请求响应示例 |
| [test-specification.md](./docs/zh/test-specification.md) | 测试规范 — 单元/集成/E2E 测试要求 |
| [hmi-design.md](./docs/zh/hmi-design.md) | 人机界面设计 — 页面原型与交互 |
| [git-commit-convention.md](./docs/zh/git-commit-convention.md) | Git 提交规范 — Conventional Commits + 分支模型 |
| [CHANGELOG.md](./CHANGELOG.md) | 变更日志 — 版本发布记录 |
| [AGENTS.md](./AGENTS.md) | AI 助手规范 — 编码与提交行为约束 |

## 📐 UML 图索引

| 图 | 工具 | 内容 |
|----|------|------|
| [uml-usecase.puml](./uml/uml-usecase.puml) | PlantUML | 用例图 — 游客/用户/商家/管理员 4 角色 |
| [uml-component.puml](./uml/uml-component.puml) | PlantUML | 组件图 — Maven 模块 + 外部依赖 |
| [uml-er.puml](./uml/uml-er.puml) | PlantUML | ER 图 — 20 张表关联关系 |
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
| 🎟 秒杀券 | 管理员创建/编辑/删除券，用户抢领（Redis 防超卖）、我的券、下单优惠计算 |
| 🔄 退款/退货 | 申请 → 审核 → 退货寄回 → 退款 → 恢复库存 |
| 🏬 商家后台 | 入驻审核、商品/库存管理、发货、退款审核、销售统计 |
| 🛡 管理员后台 | 用户管理、商家审核、分类管理、全局报表（6 类） |
| 💬 即时通讯 | WebSocket 用户与商家实时聊天（低优先级） |

---

## 🔒 安全设计

- 密码 BCrypt 加密存储
- JWT + Redis 双校验认证（支持主动踢人）
- Service 层角色与数据归属校验（管理员角色、商家资质、商品/订单/退款归属）
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

- [Git 提交规范](./docs/zh/git-commit-convention.md) — 提交信息格式与分支模型
- [AGENTS.md](./AGENTS.md) — AI 助手行为规范（编码风格与提交约束）

开发流程：从 `dev` 分支拉取 → 创建 `feature/*` 分支 → 开发 → PR 合并到 `dev` → 稳定后合并到 `main`。

---

## 📄 许可证

本项目为课程设计作业，**All Rights Reserved**，未经授权不得商用或再分发。
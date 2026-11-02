# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

Chrono BBS 是一个 B2B2C 网上超市系统（课程设计项目），前后端分离：

- **后端**：Spring Boot 3.4.5 + MyBatis + MySQL 8 + Redis + Elasticsearch，Java 21，Maven 多模块
- **前端**：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router
- 仓库根目录为 `D:\Project\chrono-bbs`，子项目分别在 `chrono-bbs-server/` 和 `chrono-bbs-client/`

## 常用命令

### 中间件（MySQL / Redis / Elasticsearch）

```bash
cd docker && docker-compose up -d    # 启动中间件；down 停止
```

数据库初始化脚本在 `initial/schema.sql`（建表）与 `initial/data.sql`（初始数据，含管理员账号 `admin` / `admin123`）。

### 后端（chrono-bbs-server，Maven 多模块）

```bash
cd chrono-bbs-server
mvn clean install -DskipTests        # 编译安装全部模块
mvn test                             # 运行全部测试
# 运行单个测试类（-am 会连同依赖模块一起构建）
mvn -pl module-service -am test -Dtest=AuthServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl module-start spring-boot:run # 启动应用（需先 install），默认端口 8080
```

启动后 Knife4j API 文档在 `http://localhost:8080/doc.html`。默认激活 `dev` profile，配置见 `module-start/src/main/resources/application-dev.yml`（本地 MySQL `chrono_bbs_dev`、Redis 6379、ES 9200）。

### 前端（chrono-bbs-client）

```bash
cd chrono-bbs-client
npm install
npm run dev        # 开发服务器，端口 5173
npm run build      # 类型检查（vue-tsc -b）后 vite 打包
npm run preview    # 预览构建产物
```

前端没有配置 lint / test 脚本。请求代理与 Mock 数据由 `vite.config.ts` 中的 `vite-plugin-mock` 处理，Mock 源在 `mock/` 目录。

## 架构

### 后端模块分层（chrono-bbs-server）

Maven 模块依赖方向：`module-start → module-api → module-service → module-mapper → common`。每个模块的包都位于 `com.ncist` 下：

| 模块 | 职责 | 关键内容 |
|------|------|----------|
| `common` | 无依赖的基础设施 | `Result<T>` / `PageResult<T>`（统一返回包装）、`BusinessException`、`enums`（`ErrorCode` / `CommonErrorCode` / `AuthErrorCode` / `UserRoleEnum`）、`UserContext`（ThreadLocal 用户上下文）、`JwtUtil` |
| `module-mapper` | 数据访问 | `entity/`（实体）+ `mapper/`（Mapper 接口）+ `src/main/resources/mapper/*.xml`（SQL） |
| `module-service` | 业务逻辑 | `dto/`（入参）、`vo/`（出参）、`service/`（接口）+ `service/impl/`（实现） |
| `module-api` | 对外接口 | `controller/` + `handler/GlobalExceptionHandler` |
| `module-start` | 启动与装配 | `MyApplication`、`config/`（`WebMvcConfig`、`SecurityConfig`、`Knife4jConfig`）、`interceptor/JwtInterceptor` |

MyBatis 配置在 `application.yaml`：`map-underscore-to-camel-case: true`，Mapper XML 通过 `classpath*:mapper/*.xml` 定位。

### 认证与鉴权流程

1. `JwtInterceptor` 拦截所有 `/api/**` 请求（`WebMvcConfig` 白名单：captcha / register / login / forgot-code / reset-password / swagger 相关路径）。
2. 从 `Authorization: Bearer <token>` 取 JWT → `JwtUtil.parseToken` → 用 Redis `token:{userId}` 二次校验（登出/改密/重置密码/禁用后 token 立即失效）。
3. 校验通过后将 `userId` / `username` / `role` 写入 `UserContext`（ThreadLocal），`afterCompletion` 中 `clear()`。
4. `GlobalExceptionHandler`（`@RestControllerAdvice`）统一把 `BusinessException`、参数校验异常、其他异常转为 `Result.error(...)`。

所有 Controller 统一返回 `Result<T>`（`{code, message, data}`，成功 code=200）。新增接口时遵循此约定并更新 `docs/api-doc.md`。

### 前端结构（chrono-bbs-client/src）

- `api/`：Axios 接口封装（`index.ts` 为统一实例，baseURL `/api`、15s 超时、Bearer Token 拦截器），按业务域拆分（user / product / order / cart / …）
- `stores/`：Pinia 状态，`useUserStore` 将 token + userInfo 持久化到 localStorage，角色枚举 `USER=1 / MERCHANT=2 / ADMIN=3`
- `router/`：History 模式 + 全局前置守卫（登录校验 + 角色权限）
- `views/`：页面组件（商城 / 商家后台 / 管理员后台三套布局）
- `types/`：TS 类型定义；`utils/`：工具；`@` 别名指向 `src`

## 开发约定

项目的强制行为规范在 `AGENTS.md`（已随仓库签入，Claude Code 会自动加载），要点：

- **提交信息**：Conventional Commits，`<type>(<scope>): <subject>`，subject 用简体中文、动词开头、≤50 字符、不加句号。
- **提交前更新 `CHANGELOG.md`**：在 `## [Unreleased]` 下按 `Added` / `Changed` / `Fixed` / `Removed` 追加对应条目。
- **分支**：日常在 `dev` 或 `feature/<模块>-<描述>` 上开发，禁止直接向 `main` 提交。
- **后端风格**：Lombok、Service 接口与实现分离、统一 `Result<T>` 返回。
- **前端风格**：Composition API、`<script setup lang="ts">`、接口统一经 `src/api/` 封装。

## 关键文档

设计文档集中在 `docs/`：`db-design.md`（14 张表）、`server-design.md`、`client-design.md`、`api-doc.md`、`git-commit-convention.md`、`test-specification.md`。UML 图在 `uml/*.puml`（PlantUML）。

测试分层：`common` 的 `JwtUtilTest`（单元）、`module-service` 的 `AuthServiceImplTest`（Mockito 单元，25 用例）、`module-start` 的 `AuthServiceIntegrationTest` 与 `AuthControllerE2ETest`（`@SpringBootTest`，配合 test 作用域的 H2）。

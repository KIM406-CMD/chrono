# AGENTS.md — AI 助手行为规范

本文件为 AI 编码助手（Cursor、Trae、GitHub Copilot 等）提供项目级行为约束，所有自动生成的代码与提交必须遵守。

---

## 1. Git 提交规范

**提交信息必须遵循 `docs/git-commit-convention.md` 中定义的 Conventional Commits 格式：**

```
<type>(<scope>): <subject>

<body>

<footer>
```

- `type` 必须从以下集合中选择：`feat`、`fix`、`docs`、`style`、`refactor`、`perf`、`test`、`build`、`ci`、`chore`、`revert`
- `subject` 使用简体中文，不超过 50 个字符，以动词开头，不加句号
- 一个提交只做一件事，不要把不相关的改动合并到一个 commit
- 禁止使用 `update`、`fix bug`、`wip` 等无意义的提交信息

**提交示例：**

```
feat(cart): 新增购物车数量调整接口
```

```
fix(auth): 修复 token 过期未清理 Redis 的问题

JwtInterceptor 在 token 过期后未删除 Redis 中的会话记录，
导致用户无法重新登录。现在校验失败时主动 DEL 对应 key。

Closes #42
```

---

## 2. 分支操作

- 日常开发在 `dev` 分支或 `feature/*` 分支上进行，**禁止直接向 `main` 分支提交**
- 创建功能分支命名：`feature/<模块>-<简短描述>`
- 紧急修复分支命名：`hotfix/<模块>-<简短描述>`

---

## 3. 代码风格

- **后端（Java）**：遵循项目现有风格，使用 Lombok 简化样板代码，Service 层接口与实现分离，统一返回 `Result<T>` 包装
- **前端（TypeScript/Vue）**：使用 Composition API，组件用 `<script setup lang="ts">`，接口请求统一通过 `src/api/` 封装
- 不添加无意义的注释；仅在业务逻辑复杂、容易误解处添加注释
- 不在代码中硬编码密钥、密码、Token

---

## 4. 文件忽略

以下内容不得提交到仓库（已由 `.gitignore` 管控）：

- `target/`、`node_modules/`、`dist/` 等构建产物
- `.idea/`、`.vscode/`、`*.iml` 等 IDE 配置
- `.env`、`.env.local` 等环境变量文件

---

## 5. 参考文档

- Git 提交规范：`docs/git-commit-convention.md`
- 数据库设计：`docs/db-design.md`
- 服务端设计：`docs/server-design.md`
- 客户端设计：`docs/client-design.md`
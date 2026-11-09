# AGENTS.md — AI 助手行为规范

本文件为 AI 编码助手（Cursor、Trae、GitHub Copilot 等）提供项目级行为约束，所有自动生成的代码与提交必须遵守。

---

## 1. Git 提交规范

**提交信息必须遵循 `docs/zh/git-commit-convention.md` 中定义的 Conventional Commits 格式：**

```
<type>(<scope>): <subject>

<body>

<footer>
```

- `type` 必须从以下集合中选择：`feat`、`fix`、`docs`、`style`、`refactor`、`perf`、`test`、`build`、`ci`、`chore`、`revert`
- `subject` 使用简体中文，不超过 50 个字符，以动词开头，不加句号
- 一个提交只做一件事，不要把不相关的改动合并到一个 commit
- 禁止使用 `update`、`fix bug`、`wip` 等无意义的提交信息
- **提交前必须更新 `CHANGELOG.md`**：每次提交代码前，需在 `CHANGELOG.md` 的 `## [Unreleased]` 下追加对应的变更记录。根据变更类型归入对应小节：新增功能→`### Added`、修改→`### Changed`、修复→`### Fixed`、删除→`### Removed`。描述使用简体中文，简洁明了，确保日志与代码同步

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

### 提交工作流（每次提交必须遵循以下顺序）

1. **创建新分支**：`git checkout -b feature/<模块>-<简短描述>`
2. **提交**：`git add` + `git commit`（遵循 Conventional Commits 格式，提交前更新 `CHANGELOG.md`）
3. **合并到本地 dev**：`git checkout dev` && `git merge <分支名>`
4. **上传**：`git push origin dev`

---

## 3. 代码风格

- **后端（Java）**：遵循项目现有风格，使用 Lombok 简化样板代码，Service 层接口与实现分离，统一返回 `Result<T>` 包装
- **前端（TypeScript/Vue）**：使用 Composition API，组件用 `<script setup lang="ts">`，接口请求统一通过 `src/api/` 封装
- 不添加无意义的注释；仅在业务逻辑复杂、容易误解处添加注释
- 不在代码中硬编码密钥、密码、Token

### 3.1 后端 Javadoc 规范

**所有后端 public 类、接口、枚举与 public 方法必须编写 Javadoc**，并遵守以下要求：

- 使用简体中文，保持与现有代码风格一致
- 类/接口注释说明职责、使用场景与关键约束
- 方法注释说明业务语义；使用 `@param` 描述每个参数的取值范围或业务含义，使用 `@return` 描述返回值，使用 `@throws` 描述业务异常及触发条件
- DTO / VO / Entity 的非直观字段必须有字段注释，说明单位、枚举取值、可空性或业务含义；Lombok 自动生成的方法无需重复注释
- 不要写“`@param id 主键`”这类无信息量的注释；应说明约束与用途，例如“用户 ID，必须为当前登录用户”
- Controller 的 Javadoc 与 `@Operation` / `@Parameter` 注解互补：注解面向接口文档，Javadoc 面向代码维护者
- 复杂私有方法应使用 Javadoc 或块注释说明算法、状态流转或并发处理
- 修改方法签名、参数含义、返回值或异常时，必须同步更新 Javadoc

**示例：**

```java
/**
 * 用户认证服务。
 *
 * <p>负责验证码、注册、登录、登出与密码重置等认证流程。</p>
 */
public interface AuthService {

    /**
     * 校验登录凭证并生成登录态。
     *
     * @param dto 登录参数，包含账号、密码与验证码
     * @return 登录结果，包含 token 与用户信息
     * @throws BusinessException 账号不存在、密码错误或账号被禁用时抛出
     */
    LoginVO login(LoginDTO dto);
}
```

---

## 4. 文件忽略

以下内容不得提交到仓库（已由 `.gitignore` 管控）：

- `target/`、`node_modules/`、`dist/` 等构建产物
- `.idea/`、`.vscode/`、`*.iml` 等 IDE 配置
- `.env`、`.env.local` 等环境变量文件

---

## 5. 参考文档

- Git 提交规范：`docs/zh/git-commit-convention.md`
- 产品需求：`docs/zh/requirements-analysis.md`
- 数据库设计：`docs/zh/db-design.md`
- 服务端设计：`docs/zh/server-design.md`
- 客户端设计：`docs/zh/client-design.md`
- 接口文档：`docs/zh/api-doc.md`
- 详细设计：`docs/zh/detailed-design.md`
- 测试规范：`docs/zh/test-specification.md`
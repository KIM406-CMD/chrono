# Git 提交规范

本文档规定了 chrono-bbs 项目的 Git 提交信息格式与分支管理规范，所有团队成员必须遵守。

---

## 1. 提交信息格式

采用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/v1.0.0/) 规范，提交信息由 **Header**、**Body**、**Footer** 三部分组成：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 1.1 Header（必填，单行）

```
<type>(<scope>): <subject>
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `type` | ✅ | 提交类型，见下表 |
| `scope` | ❌ | 影响范围，如 `auth`、`cart`、`order`、`client` |
| `subject` | ✅ | 简短描述，不超过 50 个字符，以动词开头，不加句号 |

**type 类型清单：**

| type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(cart): 新增购物车数量调整接口` |
| `fix` | 修复 Bug | `fix(auth): 修复 token 过期未清理 Redis 的问题` |
| `docs` | 文档变更 | `docs: 更新 README 启动说明` |
| `style` | 代码格式（不影响逻辑） | `style: 统一缩进为 4 空格` |
| `refactor` | 重构（非新功能、非修 Bug） | `refactor(order): 提取订单状态机为独立类` |
| `perf` | 性能优化 | `perf(search): ES 查询增加缓存` |
| `test` | 测试相关 | `test(auth): 补充登录失败用例` |
| `build` | 构建/依赖变更 | `build: 升级 MyBatis 至 3.0.4` |
| `ci` | CI/CD 配置 | `ci: 配置 GitHub Actions 自动测试` |
| `chore` | 杂项（不属于以上类型） | `chore: 调整 .gitignore` |
| `revert` | 回滚提交 | `revert: 回滚 feat(cart) 的提交` |

### 1.2 Body（可选，多行）

详细描述本次提交的动机、实现方式、与之前行为的差异。每行不超过 72 字符。

```
购物车接口原来直接扣减数据库库存，
现改为先校验 Redis 库存再异步扣减，
避免高并发下出现超卖。
```

### 1.3 Footer（可选）

- **不兼容变更**：以 `BREAKING CHANGE:` 开头，说明破坏性改动及迁移方案。
- **关联 Issue**：`Closes #123` 或 `Refs #456`。

```
BREAKING CHANGE: 购物车接口响应字段 cartItems 改为 items

Closes #88
```

---

## 2. 完整示例

### 2.1 简单提交

```
feat(order): 新增订单防重 Token 校验
```

### 2.2 带 Body 和 Footer 的提交

```
fix(payment): 修复重复支付导致的金额错误

支付回调未做幂等校验，同一订单多次回调会重复更新支付状态。
现基于 order_id 做幂等去重，已支付订单直接返回成功。

Closes #42
```

### 2.3 破坏性变更

```
refactor(auth): 重构 JWT 工具类，拆分签名与校验逻辑

BREAKING CHANGE: JwtUtil.generate() 方法签名变更，
调用方需传入 UserInfo 对象而非 userId 字符串。

Refs #15
```

---

## 3. 分支管理规范

### 3.1 分支模型

采用 `main` + `dev` 双分支模型：

| 分支 | 说明 | 保护规则 |
|------|------|----------|
| `main` | 生产/可发布分支，只接受 PR 合并 | 禁止直接 push |
| `dev` | 日常开发集成分支 | 禁止直接 push（团队协作时） |
| `feature/*` | 功能开发分支 | 从 `dev` 切出，完成后合并回 `dev` |
| `hotfix/*` | 紧急修复分支 | 从 `main` 切出，修复后同时合并 `main` 和 `dev` |

### 3.2 分支命名

```
feature/<模块>-<简短描述>
hotfix/<模块>-<简短描述>
```

示例：

```
feature/cart-coupon
feature/order-refund
hotfix/auth-token-expire
```

### 3.3 开发流程

```bash
# 1. 从 dev 拉取最新代码
git checkout dev
git pull origin dev

# 2. 创建功能分支
git checkout -b feature/cart-coupon

# 3. 开发并频繁提交
git add .
git commit -m "feat(cart): 新增优惠券计算逻辑"

# 4. 推送分支
git push -u origin feature/cart-coupon

# 5. 在 GitHub 发起 Pull Request: feature/cart-coupon -> dev

# 6. 合并后删除本地与远程分支
git branch -d feature/cart-coupon
git push origin --delete feature/cart-coupon
```

---

## 4. 提交粒度要求

- **一个提交只做一件事**：不要把多个不相关的改动塞进一个 commit。
- **可编译、可运行**：每个 commit 都应保证项目能通过编译，不要提交半成品。
- **先自测再提交**：提交前确认相关测试通过。
- **提交信息用中文**：subject 和 body 使用简体中文，便于团队阅读。

---

## 5. 禁止事项

- ❌ 禁止使用 `git commit -m "update"`、`git commit -m "fix bug"` 等无意义信息。
- ❌ 禁止直接向 `main` 分支 push。
- ❌ 禁止在 `dev` 分支上直接开发新功能，必须切 `feature/*` 分支。
- ❌ 禁止提交 `target/`、`node_modules/`、`.idea/`、`.iml` 等构建产物与 IDE 配置（已由 `.gitignore` 管控）。
- ❌ 禁止提交包含密钥、密码、Token 的文件。

---

## 6. 快速参考

```
feat:     新功能
fix:      修 Bug
docs:     文档
style:    格式
refactor: 重构
perf:     性能
test:     测试
build:    构建/依赖
ci:       CI/CD
chore:    杂项
revert:   回滚
```
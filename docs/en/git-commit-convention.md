# Git Commit Convention

> [简体中文](../zh/git-commit-convention.md) | English

This document specifies the Git commit message format and branch management conventions for the chrono-bbs project. All team members must follow them.

---

## 1. Commit Message Format

Adopt the [Conventional Commits](https://www.conventionalcommits.org/zh-hans/v1.0.0/) specification. A commit message consists of three parts: **Header**, **Body**, and **Footer**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 1.1 Header (Required, Single Line)

```
<type>(<scope>): <subject>
```

| Field | Required | Description |
|------|------|------|
| `type` | ✅ | Commit type, see the table below |
| `scope` | ❌ | Scope of impact, e.g. `auth`, `cart`, `order`, `client` |
| `subject` | ✅ | Short description, no more than 50 characters, starts with a verb, no period |

**type list:**

| type | Description | Example |
|------|------|------|
| `feat` | New feature | `feat(cart): add cart quantity adjustment API` |
| `fix` | Bug fix | `fix(auth): clear expired token from Redis` |
| `docs` | Documentation changes | `docs: update README startup instructions` |
| `style` | Code formatting (no logic impact) | `style: unify indentation to 4 spaces` |
| `refactor` | Refactoring (neither a new feature nor a bug fix) | `refactor(order): extract order state machine into a separate class` |
| `perf` | Performance optimization | `perf(search): add cache to ES queries` |
| `test` | Test-related | `test(auth): add login failure test cases` |
| `build` | Build/dependency changes | `build: upgrade MyBatis to 3.0.4` |
| `ci` | CI/CD configuration | `ci: configure GitHub Actions automated tests` |
| `chore` | Miscellaneous (none of the above) | `chore: adjust .gitignore` |
| `revert` | Revert a commit | `revert: revert the feat(cart) commit` |

### 1.2 Body (Optional, Multiple Lines)

Describe in detail the motivation for this commit, the implementation approach, and the differences from previous behavior. Each line must not exceed 72 characters.

```
The cart API used to deduct database stock directly;
it now validates Redis stock first and deducts asynchronously,
avoiding overselling under high concurrency.
```

### 1.3 Footer (Optional)

- **Breaking Changes**: Start with `BREAKING CHANGE:`, explaining the breaking change and the migration plan.
- **Related Issue**: `Closes #123` or `Refs #456`.

```
BREAKING CHANGE: the cart API response field cartItems is renamed to items

Closes #88
```

---

## 2. Complete Examples

### 2.1 Simple Commit

```
feat(order): add order duplicate-submission-prevention Token validation
```

### 2.2 Commit with Body and Footer

```
fix(payment): fix amount error caused by duplicate payment

The payment callback lacked Idempotency validation, so multiple callbacks
for the same order updated the payment status repeatedly.
Idempotent deduplication based on order_id is now applied; already-paid
orders return success directly.

Closes #42
```

### 2.3 Breaking Change

```
refactor(auth): refactor JWT utility class, split signing and validation logic

BREAKING CHANGE: the signature of JwtUtil.generate() has changed;
callers must pass a UserInfo object instead of a userId string.

Refs #15
```

---

## 3. Branch Management Convention

### 3.1 Branch Model

Adopt the `main` + `dev` two-branch model:

| Branch | Description | Protection Rule |
|------|------|----------|
| `main` | Production/release branch, only accepts PR merges | Direct push prohibited |
| `dev` | Daily development integration branch | Direct push prohibited (during team collaboration) |
| `feature/*` | Feature development branch | Branched from `dev`, merged back to `dev` when complete |
| `hotfix/*` | Emergency fix branch | Branched from `main`, merged into both `main` and `dev` after the fix |

### 3.2 Branch Naming

```
feature/<module>-<short-description>
hotfix/<module>-<short-description>
```

Examples:

```
feature/cart-coupon
feature/order-refund
hotfix/auth-token-expire
```

### 3.3 Development Workflow

```bash
# 1. Pull the latest code from dev
git checkout dev
git pull origin dev

# 2. Create a feature branch
git checkout -b feature/cart-coupon

# 3. Develop and commit frequently
git add .
git commit -m "feat(cart): add coupon calculation logic"

# 4. Push the branch
git push -u origin feature/cart-coupon

# 5. Open a Pull Request on GitHub: feature/cart-coupon -> dev

# 6. Delete the local and remote branches after merging
git branch -d feature/cart-coupon
git push origin --delete feature/cart-coupon
```

---

## 4. Commit Granularity Requirements

- **One commit does one thing**: do not cram multiple unrelated changes into a single commit.
- **Compilable and runnable**: every commit should ensure the project compiles; do not commit half-finished work.
- **Self-test before committing**: confirm the relevant tests pass before committing.
- **Commit messages in Chinese**: the subject and body use Simplified Chinese for team readability.

---

## 5. Prohibitions

- ❌ Do not use meaningless messages such as `git commit -m "update"` or `git commit -m "fix bug"`.
- ❌ Do not push directly to the `main` branch.
- ❌ Do not develop new features directly on the `dev` branch; always create a `feature/*` branch.
- ❌ Do not commit build artifacts or IDE configurations such as `target/`, `node_modules/`, `.idea/`, `.iml` (already managed by `.gitignore`).
- ❌ Do not commit files containing secrets, passwords, or tokens.

---

## 6. Quick Reference

```
feat:     New feature
fix:      Bug fix
docs:     Documentation
style:    Formatting
refactor: Refactoring
perf:     Performance
test:     Testing
build:    Build/dependency
ci:       CI/CD
chore:    Miscellaneous
revert:   Revert
```

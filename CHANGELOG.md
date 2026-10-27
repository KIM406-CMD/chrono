# Changelog

本文件记录 chrono-bbs 项目的所有重要变更。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### Added
- 前端项目骨架：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- Mock 数据层：基于 vite-plugin-mock，覆盖用户、商品、分类、购物车、订单、支付、地址、商家、管理员、退款、聊天、上传 12 个模块
- API 封装层：`src/api/` 下 11 个接口模块，统一 Axios 实例（baseURL=/api，超时 15s，Bearer Token 拦截器）
- 类型定义：`src/types/` 下 user、product、order、cart、address、common 6 个模块
- 三套独立布局：商城布局（顶部导航+分类栏+内容区+页脚）、商家后台布局、管理员后台布局
- 用户页面：登录、注册、忘记密码、个人信息、收货地址管理
- 商品页面：首页商品列表、商品详情、搜索结果
- 购物车页面：列表展示、数量调整、选中/全选、删除、去结算
- 订单页面：结算、订单列表、订单详情、支付（卡号/二维码）
- 商家后台：入驻申请、商品管理、订单管理、退款审核、销售统计
- 管理员后台：用户管理、商家审核、分类管理、全平台商品/订单管理、销售报表
- 聊天页面、帮助中心、404 页面
- 状态管理：`useUserStore`（token + userInfo 持久化到 localStorage，角色枚举 USER=1/MERCHANT=2/ADMIN=3）
- 样式系统：CSS 变量设计令牌（京东红主色 `#E2231A`、间距、圆角、阴影、过渡）
- 路由系统：History 模式 + 全局前置守卫（登录校验 + 角色权限控制）
- 文档：人机界面设计文档（hmi-design.md）

### Changed
- 客户端概要设计文档（client-design.md）：目录结构、技术选型、Pinia 实现、路由角色名与实际代码对齐
- AGENTS.md：新增"提交前必须更新 CHANGELOG.md"规则

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
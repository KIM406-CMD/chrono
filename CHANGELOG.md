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
- 后端认证模块补全 6 个接口：获取用户信息、修改个人信息、修改密码、退出登录、发送重置密码验证码、重置密码（`/api/user` 组 9 接口齐备）
- JWT+Redis 双校验：登录/注册后 token 存入 Redis（2h），拦截器校验服务端 token 一致性，支持登出/改密/重置密码后强制下线及单设备登录
- UserMapper 新增 selectById、selectByAccount、updatePassword、updateProfile 四个数据访问方法
- AuthErrorCode 新增 ACCOUNT_NOT_FOUND（404）
- ForgotCodeVO 新增 code 字段（开发阶段模拟短信/邮件返回验证码）
- 秒杀模块：Mock 数据层（seckill.ts），覆盖秒杀活动、秒杀商品、优惠券管理
- 搜索模块：Mock 数据层（search.ts）与 API 层（search.ts）
- 评论模块：评论 API 层（comment.ts）与类型定义（comment.ts）
- 上传模块：上传 API 层（upload.ts）
- 用户设置页面：SettingsView.vue，支持个人信息修改和安全设置
- 工具模块：settings.ts 设置工具类
- 秒杀优惠券前端：管理员秒杀券管理页面（CouponManageView）与用户领券中心（CouponCenterView），API 层（seckill.ts）与类型定义（coupon.ts）
- 分类管理：树形展示与新增/编辑/删除，支持子分类与三级层级限制
- 商品管理：新增/编辑弹窗、上架/下架切换与删除

### Changed
- 客户端概要设计文档（client-design.md）：目录结构、技术选型、Pinia 实现、路由角色名与实际代码对齐
- AGENTS.md：新增"提交前必须更新 CHANGELOG.md"规则
- Mock 层全面增强：address、admin、cart、category、chat、comment、merchant、order、payment、product、refund、upload、user 模块数据更新
- API 层增强：admin、cart、chat、merchant、product、refund、user 接口扩展
- 视图层优化：商城布局、聊天、首页、商家审核、订单管理、商品管理、销售统计、商品详情、搜索结果、登录、注册、忘记密码页面更新
- 类型定义扩展：product.ts、user.ts 补充新字段
- 后端优化：AuthController、WebMvcConfig、JwtInterceptor 逻辑增强
- 数据库脚本更新：data.sql、schema.sql
- 设计文档更新：api-doc、db-design、prd、requirements-analysis、server-design
- UML 用例图更新
- 类型定义 common.ts：枚举改为常量对象 + 类型（适配 erasableSyntaxOnly），order/product/user 类型导入改为 import type
- 清理多处未使用变量与导入

### Fixed
- 统一用户 status 语义为 api-doc 规范（1=正常，0=禁用）：修正注册默认值、登录校验逻辑、data.sql 管理员初始数据及关联单元测试断言
- 修复忘记密码接口被 JWT 拦截器误拦的问题：WebMvcConfig 白名单补充 forgot-code、reset-password
- 修正 AuthServiceImplTest 中 5 个用例的 status mock 值语义混写，单元测试 25/25 通过
- 修复商家审核/退款审核传参错误（approved 布尔值、reason 字段名）
- 修复商品销量字段 totalSales → sales 导致销量不显示
- 修复 admin.ts 缺失 MerchantInfo 导入

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
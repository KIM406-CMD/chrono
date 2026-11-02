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
- 搜索模块前端接入：搜索建议（自动补全）、热搜榜、搜索历史（含清空）
- CLAUDE.md：新增 Claude Code 项目指引
- 分类管理 2 个接口：GET /api/category/tree（全量分类树，Redis 缓存 `category:tree` 30min，缓存故障降级查库）、GET /api/category/children（按父分类查子分类）
- 商品管理 6 个接口：商品列表（分页+分类筛选，仅展示上架 status=1，批量 MIN(price) 组装最低价）、商品详情（含 SKU 列表/图片/店铺名）、商家发布商品（需已审核商户，默认下架）、修改商品（属主校验）、上架/下架、删除（FK 引用保护，被购物车等引用时返回 409）
- `@Public` 注解：公开接口免登录机制，JwtInterceptor 扫描 HandlerMethod 注解放行，替代通配白名单
- CategoryMapper（3 方法）、ProductMapper（8 方法）、ProductSkuMapper（4 方法）、MerchantMapper（2 方法），全注解 SQL
- 新增 VO/DTO：CategoryTreeVO（递归 children）、CategoryVO、ProductVO、ProductDetailVO、ProductSkuVO；ProductCreateDTO、ProductSkuDTO、ProductStatusDTO
- 新增错误码：CategoryErrorCode（CATEGORY_NOT_FOUND）、ProductErrorCode（9 个：NOT_FOUND/NOT_MERCHANT/MERCHANT_NOT_APPROVED/NO_PERMISSION_OPERATE/SKU 校验三项/STATUS_ILLEGAL/PRODUCT_REFERENCED）
- 分类/商品单元测试 18 个用例（4+14），全模块单测 43/43 通过
- 秒杀券管理 8 个接口：券列表/详情（公开）、抢领（Redis DECR 防超卖 + INCR 领取计数防超领 + Redis 故障降级数据库原子扣减 + MySQL 失败补偿回滚）、我的优惠券（联查分页）、创建/编辑/删除（商家或管理员，编辑自动同步 remain_count=±增量，仅未开始可删）、下单优惠计算（满减券/折扣券，校验未使用、未过期、最低消费门槛）
- SeckillCouponMapper（10 方法）、UserCouponMapper（6 方法），含原子扣减库存 `UPDATE ... WHERE remain_count > 0` 与状态流转 SQL
- 秒杀券状态定时器：每 30 秒刷新 未开始→进行中→已结束，未使用的用户券随券结束自动过期（@EnableScheduling）
- 新增 VO/DTO：SeckillCouponVO、SeckillClaimVO、MyCouponVO、SeckillCalculateVO；SeckillCouponDTO、SeckillCalculateDTO（时间格式 yyyy-MM-dd HH:mm:ss）
- SeckillErrorCode 14 个错误码（券不存在/已抢光/已领取/未开始或已结束/不满足门槛/已过期或已使用等）
- 秒杀券单元测试 25 个用例，全模块单测 68/68 通过；E2E 16 项检查全部通过
- 购物车 7 个接口：列表（联查商品/SKU/商户，首图提取，同时提供文档命名 specText/price/stock 与前端命名 skuSpecText/skuPrice/skuStock）、加入（同 SKU 累加数量，校验上架状态与库存）、修改数量（库存校验）、选中/取消选中（兼容文档单个 cartItemId+整型与前端批量 cartItemIds+布尔，缺省视为全部）、全选、删除、清空（前端扩展接口）
- 收货地址 6 个接口：列表（默认地址在前）、详情、新增（上限 20 条，首个地址自动设默认）、修改（设默认时自动清除原默认）、设为默认、删除；全部带属主校验
- CartItemMapper（9 方法）、AddressMapper（9 方法，动态 SET 更新）、ProductSkuMapper 新增 selectById
- 新增 VO/DTO：CartItemVO、AddressVO；CartAddDTO、CartUpdateDTO、CartSelectDTO（兼容布尔/整型 selected）、SelectAllDTO、AddressDTO
- 新增错误码：CartErrorCode（6 个：SKU 不存在/商品下架/库存不足/项不存在/越权/selected 非法）、AddressErrorCode（3 个：不存在/越权/超上限）
- 购物车/地址单元测试 30 个用例（17+13），全模块单测 98/98 通过；E2E 38 项检查全部通过
- 结算页优惠券抵扣：加载可用优惠券、选券后调 `/api/seckill/calculate` 重算优惠、下单请求体新增 `userCouponId` 字段、侧边栏展示优惠与折后应付

### Changed
- 接口文档（api-doc.md）与购物车/地址实际实现对齐：购物车列表响应补充前端命名兼容字段（skuSpecText/skuPrice/skuStock）、选中接口补充批量 cartItemIds+布尔与缺省视为全部的兼容说明、新增 5.7 清空购物车（前端扩展）、地址模块补充上限 20 条/首地址自动默认/设默认清除原默认/默认地址排序等行为说明；错误码说明章节（现为第 18 章）替换为按模块 ErrorCode 枚举整理的真实 HTTP 语义错误码
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
- 移除「进店逛逛」链接（后端无店铺主页接口）
- 移除购物车 clearCart 接口（后端无清空接口）
- 文档同步：client-design.md（目录结构/搜索走后端/路由总览）、api-doc.md（补秒杀券接口）、AGENTS.md（补参考文档与提交工作流）、UML 图（补秒杀券模块、修组件图/用例图错误、搜索时序走后端）
- README.md：核心功能模块补秒杀券、文档索引补 hmi-design.md、技术栈更新（ES 8.10.x、springdoc+knife4j-ui）
- 接口文档依赖重构：knife4j-openapi3-jakarta-spring-boot-starter 替换为 springdoc-openapi-starter-webmvc-ui 2.8.9（官方实现，适配 Boot 3.4）+ knife4j-openapi3-ui 4.5.0（纯静态 doc.html 界面）
- application.yaml：springdoc 改为全局包扫描 com.ncist.controller（移除 group-configs 分组配置）
- 前端开发配置：vite.config.ts 新增 /api 代理到后端 8080，关闭 Mock 切换到真实后端联调
- hmi-design.md：补全商家后台（订单/退款/统计/入驻）、管理员后台（7 页）、领券中心、账户设置、帮助中心及订单详情页
- api-doc.md：6.2 创建订单参数表新增 `userCouponId`（可选），与前端契约对齐

### Fixed
- 统一用户 status 语义为 api-doc 规范（1=正常，0=禁用）：修正注册默认值、登录校验逻辑、data.sql 管理员初始数据及关联单元测试断言
- 修复忘记密码接口被 JWT 拦截器误拦的问题：WebMvcConfig 白名单补充 forgot-code、reset-password
- 修正 AuthServiceImplTest 中 5 个用例的 status mock 值语义混写，单元测试 25/25 通过
- 修复商家审核/退款审核传参错误（approved 布尔值、reason 字段名）
- 修复商品销量字段 totalSales → sales 导致销量不显示
- 修复 admin.ts 缺失 MerchantInfo 导入
- 对齐聊天会话字段与后端 API（merchantId/merchantName/merchantAvatar）
- 对齐评论星级与头像字段（star/avatar）
- 修复扫码支付误调卡号支付的问题（改走二维码接口）
- 修复「立即购买」跳结算页被忽略的问题（先加入购物车）
- 修复创建订单缺失防重复提交 token
- 修复清空购物车调用不存在的接口（改循环删除）
- 修复商品详情库存 fallback 用销量的语义错误
- 修复 Knife4j 文档页（doc.html）接口列表为空的问题：knife4j 4.5.0 传递的 springdoc 2.2.0 与 Spring Boot 3.4.5 二进制不兼容（`ControllerAdviceBean(Object)` 构造器被 Spring 6.2 移除，/v3/api-docs 抛 NoSuchMethodError），升级 springdoc 2.8.9 后 17 个接口全部可见
- 修复 data.sql 管理员初始数据两处错误：role=1 应为 3（管理员），原 BCrypt 哈希与注释密码 admin123 不匹配导致 admin 无法登录，重新生成正确哈希
- 修正 api-doc/UML 秒杀券模块标注：待实现→已实现、权限仅管理员→商家/管理员、错误码 7001-7006 → HTTP 状态码（与后端 SeckillController 实际实现对齐）

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
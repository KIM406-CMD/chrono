# Changelog

本文件记录 chrono-bbs 项目的所有重要变更。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### Changed
- 文档与注释同步：移除“后端未实现/Mock 扩展”等过期描述，RBAC 说明改为实际的 Service 层角色与数据归属校验，补充本地图片存储与演示数据说明

### Fixed
- “联系客服”会话创建：商品详情新增 `merchantUserId`，前端使用商家用户 ID 创建会话，修复传入商家表主键导致“商家不存在”的问题
- 商品删除引用校验：数据库未建外键，改为删除前显式统计购物车与订单明细引用，避免商家/管理员误删已售商品产生孤儿数据

### Added
- 测试补齐：新增 `ChatServiceImplTest`、`MerchantServiceImplTest`、`UploadServiceImplTest`，并填充 `AuthControllerE2ETest` 的未登录与公开接口用例
- 本地上传兜底：新增 `upload.mode` 配置，开发环境默认写入 `uploads/` 并通过 `/uploads/**` 访问，无需 OSS 密钥即可上传；生产可设置 `UPLOAD_MODE=oss` 切回对象存储，Vite 与 Nginx 已补充代理
- 演示种子数据：新增 `initial/demo-data.sql`（幂等），内置 `merchant1` / `merchant2` / `user1` 演示账号，以及 12 个商品、13 个 SKU、购物车、3 笔订单、支付流水、退款申请与商品评论；Docker 初始化自动执行
- 管理员商品操作接口：新增 `PUT /api/admin/products/{id}/status` 与 `DELETE /api/admin/products/{id}`，支持全平台商品上下架/删除并同步搜索索引
- 支付记录接口：新增 `GET /api/payment/records`，按当前用户分页返回支付方式、脱敏卡号、订单金额、状态与支付时间
- 退款审核列表接口：新增 `GET /api/merchant/refunds`（商家仅见自己商品）与 `GET /api/admin/refunds`（管理员见全平台），支持状态筛选与分页
- 聊天前后端打通：新增 `utils/chatSocket.ts` 连接后端 WebSocket，聊天页实时收发、断线指数退避重连、未读角标实时刷新；Vite 与 Nginx 增加 `/chat/` WebSocket 代理
- 前端工程化：新增 ESLint（`npm run lint`）、Vitest 单元测试（`npm run test`，覆盖 Luhn 与富文本清洗）和 Playwright E2E 冒烟测试（`npm run test:e2e`）
- 构建优化：Element Plus 改为按需引入，主包由约 833 kB 降至约 87 kB；ECharts 保持报表页懒加载
- 退货寄回确认：退款单新增 `merchant_id`、退货快递/物流单号、寄回与收货时间字段及 3/4/5 状态；买家可填写退货物流，商家/管理员可确认收货并完成退款
- 管理员编辑商品：全平台商品列表新增编辑弹窗，支持修改基础信息与 SKU 价格/库存，后端提供 `PUT /api/admin/products/{id}`
- 评论图片：`comment` 增加 `images` 字段，发表评价支持上传最多 4 张图片，商品详情评论列表展示图片
- 支付记录页：新增 `/payments` 页面，展示支付方式、脱敏卡号、金额、状态与支付时间，后端提供 `GET /api/payment/records`
- 报表订单量趋势：管理员销售报表新增按日下单量/支付量/完成量折线图，后端 `AdminReportVO` 增加 `orderTrend` 并补充聚合 SQL
- 分类图标渲染：商城顶部导航与首页分类侧边栏优先展示 `category.icon`，无图标时回退 📦
- 搜索结果排序新增“上架时间”（`time_desc`）
- 聊天功能启用：顶部消息入口与未读角标、会话列表/历史消息/发送/已读、商品详情“联系客服”创建会话；后端提供 WebSocket 收发、会话去重与未读计数
- 退款审核：商家后台与管理员后台复用同一审核页，支持状态筛选、同意/拒绝及审核意见；后端新增商家/管理员退款列表接口
- 管理员商品操作：全平台商品列表支持上架/下架与删除，后端新增对应接口
- 搜索接口接入：关键词检索、搜索建议、历史、热搜改为调用 `/api/search` 系列接口，接口不可用时自动降级本地过滤
- 商家商品多规格 SKU：新增规格组编辑与笛卡尔积组合生成，SKU 表格支持逐行设置价格/库存，编辑时从 `specText` 反推规格组
- 销售报表图表：引入 ECharts 6 并封装 `BaseChart`，管理员报表与商家统计新增销售趋势折线图、热销商品柱状图、分类销售饼图、商家销售柱状图
- 卡号支付前端校验：新增 `utils/luhn.ts`，收银台自动格式化卡号/有效期并在提交前执行 Luhn、MM/YY、CVV 校验
- 搜索筛选：搜索结果页新增级联分类、品牌、价格区间筛选、分类路径展示与重置
- 三级分类导航：顶部导航与首页分类侧边栏新增 hover 浮层，可直接选择二级/三级分类
- 商品详情增强：详情标签页渲染清洗后的富文本描述，规格参数标签页展示品牌/分类/店铺/销量及全部 SKU 价格与库存
- 首页商品分页：“猜你喜欢”支持分页，“热销推荐”按销量排序
- 前端 Mock 调试模式：新增 `npm run dev:mock`（`vite --mode mock`），仅该模式启用 Mock，普通 `npm run dev` 仍通过代理连接后端，无需来回修改 `vite.config.ts`
- 开发启动脚本：`script/` 下提供 Bash 与 Windows 批处理两套脚本，支持 Docker / 本机两种中间件模式，可分别或一键启动中间件、后端与前端；后端脚本自动适配 Docker MySQL 3307 端口并支持根目录 `.env` 注入 OSS 密钥；README 快速启动改为脚本方式
- 游客访问支持：搜索结果页降级为公开商品列表本地搜索（本地建议/热搜/历史），领券中心开放游客浏览（领券仍需登录），商品详情评论对游客展示登录提示
- 领券中心新增优惠券详情弹窗，接入 `GET /api/seckill/detail/{id}`，展示门槛、数量、限领与活动时间
- 管理员商家审核升级为全状态管理：支持全部/待审核/已通过/已拒绝筛选、分页与审核意见展示，`AdminMerchant` 类型对齐后端返回
- 商家后台新增评论管理页：按商品查看评论、追评与回复记录，支持商家回复（`POST /api/comment/reply`）
- 商家商品表单接入图片上传（最多 4 张、单张 ≤2MB，随创建/编辑提交 `images`）；分类管理支持图标上传与树节点展示；USER 菜单新增商家入驻入口并移至商城布局；`/profile` 重定向账户设置
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
- 订单 7 个接口：获取防重复提交 Token（Redis 绑定用户 5 分钟有效、DELETE 影响 1 行保证一次性消费，兼容低版本 Redis 无 GETDEL）、创建订单（校验地址属主/购物车项上架与库存 → `UPDATE WHERE stock>=?` 原子锁库存 → 复用秒杀券 calculate 折扣 → 地址 JSON 快照 + 订单号（时间戳+6 位随机去重）→ 明细价格快照批量写入 → 清理购物车 → 核销 `userCouponId`（状态条件防并发重复核销））、我的订单分页（状态筛选）、详情（联查明细含商品名/首图/SKU 规格）、取消（仅待支付：回补库存+按 `used_order_id` 回退优惠券）、确认收货（仅已发货）、删除（仅已完成/已取消，连带删明细）
- 支付 3 个接口（模拟支付）：卡号支付（16-19 位数字 + Luhn 校验，卡号脱敏存后 4 位）、获取支付二维码（qrserver 生成，payload 含订单号与金额）、确认二维码支付；均校验订单属主与待支付状态，状态条件更新防并发重复支付
- OrderMapper（9 方法）、OrderItemMapper（5 方法含批量插入与联查）、PaymentMapper、ProductSkuMapper 新增 deductStock/restoreStock、UserCouponMapper 新增 markUsed/revokeByOrderId、CartItemMapper 新增 selectDetailByIds/deleteByIds
- 新增 VO/DTO：SubmitTokenVO、CreateOrderVO、OrderListItemVO、OrderDetailVO、OrderItemVO、PayResultVO、PaymentQrVO；CreateOrderDTO（token+addressId+cartItemIds+remark+userCouponId）、CancelOrderDTO、CardPayDTO（JSR-303 校验 MM/YY、CVV）、OrderIdDTO
- 新增错误码：OrderErrorCode（6 个：不存在/越权/状态非法/Token 无效/购物车项为空/订单号生成失败）、PaymentErrorCode（2 个：不可支付/Luhn 非法）
- `order` 表新增 `remark` 列（schema.sql 同步，存量库需执行 `ALTER TABLE order ADD COLUMN remark VARCHAR(500)`）
- 订单/支付单元测试 30 个用例（Order 22 + Payment 8），全模块单测 128 个全部通过；E2E 22 项检查全部通过（下单锁库存/购物车清理/token 一次性/重复支付 409/取消回补库存/状态机守卫/越权 403）
- 商家管理 6 个接口：入驻申请（同用户防重复申请）、商家信息、商家商品分页（需审核通过）、商家订单分页（订单商品全属于该商家才可见，addressSnapshot JSON 解析收货地址）、发货（订单商品归属校验 + `UPDATE WHERE status=2` 状态条件防并发，已支付→已发货）、销售统计（总销售额/订单数/Top10 商品/按分类/按日期趋势，仅统计已支付及以后状态订单）
- 管理后台 10 个接口：用户分页（keyword/role/status 筛选）、启禁用（仅 0/1，管理员账号不可禁用）、商家申请列表、商家审核（status=0 条件防重复审核，通过时申请人角色升级为商家，同事务）、分类增（层级 1-3 校验，父分类必须存在且层级连续，一级分类强制 parentId=0）/改（仅名称/排序/图标）/删（有子分类或商品引用时拒绝）、全平台商品分页（四条件筛选）、全平台订单分页、全局销售报表（day/week/month 分组趋势 + 状态分布五键）
- 文件上传 1 个接口：图片上传（非空/≤2MB/扩展名白名单 jpg jpeg png webp → ImageIO 真实图片内容校验 → 宽>800px 等比缩放 → 阿里云 OSS），OSS 客户端懒加载 + @PreDestroy 关闭，objectKey 规则 product/yyyyMMdd/uuid.ext
- MerchantMapper 重写（+10 方法含统计 SQL）、OrderMapper（+markShipped/selectAdminPage/countAdmin/报表 8 条 XML SQL）、UserMapper（+updateRole/selectAdminPage/countAdmin/updateStatus）、CategoryMapper（+insert/update/deleteById/countChildren）、ProductMapper（+selectAdminPage/countAdmin/countByCategoryId）
- 新增 VO/DTO：MerchantInfoVO、MerchantOrderVO、MerchantStatsVO；AdminUserVO、AdminMerchantVO、AdminReportVO；UploadResult 以 Map 返回；MerchantApplyDTO、MerchantReviewDTO、CategoryUpsertDTO
- 新增错误码：MerchantErrorCode（7 个 4041-4047）、AdminErrorCode（6 个 4051-4056）、UploadErrorCode（4 个 4061-4064）
- application.yaml：新增 spring.servlet.multipart 限制（单文件 2MB / 请求 10MB）
- module-service 新增依赖：aliyun-sdk-oss 3.17.4、thumbnailator 0.4.20
- 文档：`db-design.md` 补齐 `seckill_coupon`/`user_coupon` 两张表及索引、Redis 键设计；`detailed-design.md` 新增秒杀券模块（14.1-14.4）并按实际实现更新接口清单与数据库表；`server-design.md` 新增秒杀券模块设计（Redis 预扣减、降级补偿、定时任务）
- 文档：`hmi-design.md` 补充商家评论管理页、管理员商家审核全状态筛选与审核意见、优惠券详情弹窗；`client-design.md` 补充 `/merchant/comments` 路由与游客领券中心说明
- 文档：`requirements-analysis.md` 合并原 PRD 的项目概述、原始题目描述与核心业务规则（分类/商品属性/购物车/模拟支付），作为唯一需求文档
- 文档：全部设计文档提供中英文版本，中文位于 `docs/zh/`、英文位于 `docs/en/`，每篇文档顶部提供语言切换；根目录新增 `README.en.md`
- 文档：新增《面向对象开发步骤与设计分析》，梳理需求与用例、领域分析、职责分配、架构设计与详细设计步骤（中文 `docs/zh/object-oriented-development.md`，英文 `docs/en/object-oriented-development.md`）
- 部署：新增 `deploy/nginx.conf`（History 回退、`/api` 反向代理、上传大小限制、静态缓存与 gzip），README 补充生产部署步骤
- Docker：`docker-compose.yml` 增加编排说明，直接挂载 `initial/schema.sql` 与 `initial/data.sql` 自动初始化；两个脚本统一为 `chrono_bbs_dev` 库
- 即时通讯 5 个接口：会话列表（联查店铺名/头像/关联商品名/未读数）、历史消息分页（从新到旧，进入即标记对方消息已读并清零该侧未读数）、创建或获取会话（校验商家资质、唯一索引兜底并发）、标记已读、未读总数
- ConversationMapper/MessageMapper 全注解 SQL：会话联查、按用户查会话、未读计数、消息分页、批量已读、离线未读查询
- 新增 ConversationDetail/MessageDetail 联查实体，ConversationVO/MessageVO，ConversationCreateDTO/SendMessageDTO；ChatErrorCode（8 个）
- WebSocket 通信：JSR 356 原生端点 `/chat/{userId}?token=`（JWT + Redis token 双校验、防冒连），上线写 `online:{userId}` 并补推离线未读，消息落库后实时推送对方并回显发送方，断开清理在线状态
- RedisOnlineService：在线状态读写与握手 token 一致性校验；WebSocketConfig 注册 ServerEndpointExporter 并注入端点依赖
- 商品搜索 5 个接口：全文检索（多字段 AND 匹配 + 分类/品牌/价格过滤 + 综合/销量/价格/时间排序 + 分页）、搜索建议（`match_bool_prefix` 自动补全）、搜索历史（最近 20 条，同关键词去重）、清空搜索历史、近 7 天热搜榜
- ES 索引 `chrono_product`：启动时自动创建 mapping（`name`/`description` 分词，`brand`/`categoryId`/`status` 过滤，`minPrice`/`sales`/`createdAt` 排序）；索引为空时自动全量同步上架商品（按 id 覆盖写，可重复执行）
- 商品增量同步：`ProductSearchIndexService` 在商品修改/上下架/删除时同步 ES，异常仅记日志、不影响商品主业务（下架商品重复删除的 404 不告警，bulk 失败项输出原因日志）；`ProductServiceImpl` 注入该服务
- SearchHistoryMapper（5 方法，全注解 SQL：新增、同关键词去重、最近 20 条、按用户清空、近 7 天热搜聚合）；新增 SearchItemVO/ProductDoc、SearchErrorCode（SEARCH_UNAVAILABLE 503）
- JwtInterceptor：`@Public` 接口支持可选登录（携带有效 token 时填充 UserContext，无效按游客处理），用于登录用户搜索时记录历史；搜索历史写入失败不影响检索

### Changed
- `docker/docker-compose.yml` 挂载 `initial/migration-2026-11-09.sql` 为 `03-migration.sql`；增量脚本改为幂等（列/索引已存在则跳过），Docker 首次初始化按 schema → data → migration 执行
- 新增 `initial/migration-2026-11-09.sql`；已有开发库需执行该增量脚本（评论图片、退款退货字段），全新初始化直接使用 `schema.sql`
- 退款状态扩展为 0-5，退货退款审核通过后进入“待寄回”，订单保持退款中直至商家确认收货
- 功能开关 `CHAT_ENABLED` 与 `REFUND_AUDIT_ENABLED` 调整为开启；正式后端已实现对应接口，`dev:mock` 下由 `mock/` 提供数据
- 管理员商品列表数据源改为复用 Mock 商品数据，状态/删除操作实时反映到商品列表与搜索
- Mock 商品接口支持多 SKU 创建/编辑与上架、下架、删除同步更新；管理员销售报表字段改为与后端 `AdminReportVO` 一致，商家统计补充分类与热销数据
- 前端依赖新增 `echarts`（按需引入，报表页懒加载）
- 后端 module-service 模块：为 DTO、VO、Service 接口/实现、定时任务与配置补充简体中文 Javadoc
- 后端 module-api 模块：为 Controller 与全局异常处理器补充简体中文 Javadoc
- 后端 module-mapper 模块：为实体类、Mapper 接口与字段补充简体中文 Javadoc
- 文档：英文 HMI 设计文档原型图对齐与细节完善
- 后端 module-start 模块：为启动类、配置类与 JWT 拦截器补充简体中文 Javadoc
- 后端 common 模块：为 public 类、接口、枚举与 public 方法补充简体中文 Javadoc
- 开发规范：后端 public 类、接口与 public 方法统一要求编写简体中文 Javadoc，并在 AGENTS.md / CLAUDE.md 中补充 `@param`、`@return`、`@throws` 与字段注释要求
- 管理后台用户、订单、商品列表新增条件筛选：关键词、角色、状态、订单号，支持查询与重置
- 管理后台分类编辑支持修改排序值；分类树接口不返回排序值时按父级子分类补齐
- 聊天、商家退款审核接口未就绪时显示“功能开发中”并隐藏入口，避免请求不存在的接口与展示错误数据
- 移除商品详情页无实际行为的收藏/分享占位按钮，避免点击无反馈
- 站点标题与语言本地化为中文；首页分类/商品加载失败时增加错误提示；移除未使用的个人信息占位页
- 全局异常处理：新增 HttpMessageNotReadableException 处理器，JSON 请求体格式错误（类型不匹配如 cartItemIds 传标量、语法错误）由 500「服务器内部错误」改为 400「请求体格式错误」
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
- README.md / CLAUDE.md：数据库表数量由 14 更新为 20；README 补充 Docker 自动初始化与 3307 端口说明；CLAUDE.md 测试说明由 H2 更正为 `chrono_bbs_test` MySQL + 本地 Redis
- api-doc.md：文末更新日期改为 2026-11-09，版本标记为 v0.2.0-dev（Unreleased）
- test-specification.md：集成测试数据库地址由 `chrono_bbs` 更正为 `chrono_bbs_test`，与 `application-test.yml` 一致
- 搜索模块文档与实现对齐：api-doc §15 补模块说明、§18 错误码表补 SearchErrorCode、§19 权限总览细化游客/登录用户差异；db-design §13 索引由 `product_index` 改为 `chrono_product` 并补全 11 个字段类型；server-design §9 同步策略改为"同步调用 + 异常隔离"（未引入 MQ）；detailed-design §13 五个流程图改为 GET 接口、AND 匹配、直接返回 ES 文档字段（不回查 MySQL、无聚合）

### Removed
- 文档：移除 `chrono-bbs-server/docs/` 下与根 `docs/` 重复的 10 份 Markdown 与 2 份 Word 副本，统一以根 `docs/` 为准
- 文档：删除 `docs/prd.md`，其独有内容已合并至 `requirements-analysis.md`，并同步清理 README、AGENTS.md、db-design.md 中的引用
- Docker：移除 `docker/sql/` 重复副本，统一复用仓库根目录 `initial/` 下的初始化脚本

### Fixed
- 修复注册角色越权：注册接口忽略客户端传入的 `role`，固定创建普通用户，商家角色仅能由入驻审核升级
- 修正 `db-design.md` 中用户角色/状态错误（`role 1/2/3`、`status 1正常/0禁用`），并标注 Redis/ES/规格模板为规划或预留
- 修复三级分类浮层未做悬浮控制导致所有一级分类面板同时展开、互相遮挡：改为同一时间只展示当前悬浮分类
- 修复 Mock 商品详情价格与库存显示 0：SKU 字段由 `skus` 改为前端/后端约定的 `skuList`，并补齐 `minPrice`、`maxPrice`、`shopName`、`categoryName`
- 修复昵称显示乱码或角色过期：应用启动时调用 `/api/user/info` 刷新用户信息并更新 Pinia/localStorage，不再一直使用登录时缓存的旧数据；Mock 的 `/api/user/info` 同步返回当前登录用户
- 修复首页商城公告点击无响应：公告改为可点击查看详情弹窗，活动类公告提供“去领券”跳转
- 修复 Docker 中间件初始化后中文乱码：`initial/schema.sql` 与 `initial/data.sql` 增加 `SET NAMES utf8mb4`，避免容器内 `mysql` 客户端默认 latin1 字符集把 UTF-8 种子数据存成双重编码
- 修正 `User` 实体 `status` 注释为 `1-正常 / 0-禁用`，与后端实际语义和数据库脚本一致
- 商家评论管理商品下拉支持加载全部分页商品，避免商品超过 100 个时下拉缺项
- 商品详情评论对游客开放查看：游客可浏览评价，登录后才可发表评价
- 首页左侧分类支持点击跳转分类搜索；商城头部购物车角标显示实际商品数量
- 管理员/商家订单状态补充“退款中/已退款”映射，避免退款订单显示“未知”；商家订单筛选同步增加退款状态
- 管理员用户管理状态语义修正：按后端 `1-正常 / 0-禁用` 显示标签与操作文案，避免把正常用户显示为禁用
- 登录/注册/忘记密码页验证码图片使用 `cover` 裁剪导致首尾字符显示不全，改为按原始尺寸 `contain` 展示并统一宽度
- 后台全平台商品上下架按钮改为禁用并提示“后端暂未提供管理员上下架接口”，移除仅弹成功提示的伪操作
- 订单详情新增退款申请（已支付/已发货/已完成订单按明细提交，金额不超过小计），订单状态补充退款中/已退款展示；新增「退款/售后」页面与导航入口，api-doc 10.1-10.3 同步 `RefundVO` 字段
- 评论展示适配后端嵌套结构（`append`/`replies`），商品详情移除必然失败的评论表单，评价入口改到订单详情（仅已完成订单、按订单明细提交）；api-doc 9.1 同步 `CommentVO` 字段
- 商家销售统计与管理员销售报表按后端 VO 字段重写（`orderCount`/`salesTrend`/`productName`/`salesCount`/`totalAmount`、`avgOrderAmount`、`salesByCategory`/`salesByMerchant`），修复指标空白与 NaN；商家审核改用 `applicantUsername`，商家订单商品列改用 `items` 首项，api-doc 11.4/12.2 同步返回字段
- 首页商品卡片改用商品列表接口的 `image` 字段展示首图，`maxPrice` 缺失时隐藏原价；商家/后台商品列表价格与店铺名缺失时显示占位，修复 NaN 与空白列；api-doc 11.3/12.4 同步实际返回字段
- 商品详情页改用后端返回的 `skuList`/`shopName` 字段，修复 SKU 不显示、无法加购及店铺名空白；商品图片字段统一为字符串数组，接口文档 4.1/4.2 补充实际响应字段并与前端类型同步
- 修复搜索相关性误命中：multi_match 由默认 OR 改为 `operator=AND`（"可乐"不再命中"乐事薯片"）
- 修复显式 `sort=sales_desc` 被默认综合排序覆盖、关键词相关性未参与综合排序：现已按关键词时 `_score` 优先、否则销量兜底
- 修复深分页（`(page-1)*size ≥ 10000`）触发 ES `max_result_window` 报错返回 503：改为返回空分页
- 修复下架商品重复同步索引时 ES 404 被当作异常打印告警日志
- 清理 SearchHistoryMapper 未使用的 selectById 方法与多余导入、JwtInterceptor 重复注释、SearchController 误用的 @Parameter
- 修复 MerchantController 遗漏 `@GetMapping("/stats")` 导致 11.6 销售统计端点不暴露的问题；同时修正 stats 参数注解顺序（@Parameter 内移到 @RequestParam 之后）
- 修复 AdminServiceImpl 四处：分类修改三字段全空时 MyBatis `<set>` 渲染为空导致 SQL 语法错误（提前 400 拦截）；createCategory 补 level 取值 1-3 校验；一级分类强制 parentId=0 防脏数据；updateUserStatus 补 status ∈ {0,1} 校验
- 修复上传文件超 multipart 限制时 MaxUploadSizeExceededException 被全局兜底成 500 的问题：新增专用异常处理器映射为业务错误码 4062
- 修复 webp 上传无内容校验的安全缺口：改用 RIFF/WEBP 魔数头校验文件真实性
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
- 修正 `initial/schema.sql` 中用户 `role`/`status` 注释与默认值：角色改为 1-普通用户/2-商家/3-管理员，状态改为 1-正常/0-禁用，与 `UserRoleEnum` 及后端注册逻辑一致

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
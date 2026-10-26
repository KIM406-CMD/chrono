# 网上超市系统 - 数据库设计文档

---

## 一、设计原则

| 原则 | 说明 |
|------|------|
| 删除策略 | 物理删除（直接删除记录） |
| 图片存储 | 存 URL 路径字符串，每商品最多 4 张 |
| 商品快照 | 不做快照，订单关联 product_id / sku_id，查询时实时获取最新信息 |
| 退款粒度 | 支持部分退款，关联到 order_item |
| 地址存储 | 省/市/区存纯文本（用户手动输入） |

---

## 二、用户与商家（分表设计）

### `user` 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密 |
| nickname | VARCHAR(50) | 昵称 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar | VARCHAR(500) | 头像 URL |
| role | TINYINT | 0-普通用户, 1-管理员 |
| status | TINYINT | 0-正常, 1-禁用 |
| created_at | DATETIME | 注册时间 |
| updated_at | DATETIME | 更新时间 |

### `merchant` 商家表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联 user 表 |
| shop_name | VARCHAR(100) | 店铺名称 |
| business_license | VARCHAR(500) | 营业执照 URL |
| contact_phone | VARCHAR(20) | 联系电话 |
| status | TINYINT | 0-待审核, 1-已通过, 2-已拒绝 |
| review_reason | VARCHAR(500) | 审核意见 |
| reviewed_at | DATETIME | 审核时间 |
| created_at | DATETIME | 申请时间 |
| updated_at | DATETIME | 更新时间 |

---

## 三、分类与商品

### `category` 分类表（三级）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 分类名称 |
| parent_id | BIGINT | 父级分类 ID，0 表示一级 |
| level | TINYINT | 层级：1/2/3 |
| sort_order | INT | 排序序号 |
| icon | VARCHAR(500) | 分类图标 URL |
| created_at | DATETIME | 创建时间 |

### `spec_template` 规格模板表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 规格名（如"颜色"、"尺码"） |
| created_at | DATETIME | 创建时间 |

### `spec_value` 规格值表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| template_id | BIGINT FK | 关联 spec_template |
| value | VARCHAR(50) | 规格值（如"红色"、"XL"） |
| sort_order | INT | 排序序号 |

### `product` 商品表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(200) | 商品名称 |
| description | TEXT | 商品描述（富文本） |
| images | VARCHAR(2000) | 图片 URL 数组（JSON，最多 4 张） |
| brand | VARCHAR(100) | 品牌 |
| category_id | BIGINT FK | 关联三级分类 |
| merchant_id | BIGINT FK | 关联 merchant 表 |
| status | TINYINT | 0-下架, 1-上架 |
| sales | INT DEFAULT 0 | 累计销量 |
| created_at | DATETIME | 创建/上架时间 |
| updated_at | DATETIME | 更新时间 |

> **说明**：商品基础信息只存描述性字段，价格和库存下沉到 SKU。`sales` 字段用 Redis 累加计数，定时同步回 MySQL，避免高频写库。

### `product_sku` SKU 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| product_id | BIGINT FK | 关联 product 表 |
| spec_ids | VARCHAR(200) | 规格值 ID 组合（JSON 数组，如 `[1,5]` 表示"红色 + XL"） |
| spec_text | VARCHAR(200) | 规格文本（如"红色/XL"），便于展示 |
| price | INT | 价格（单位：分） |
| stock | INT | 库存数量 |
| status | TINYINT | 0-禁用, 1-启用 |
| created_at | DATETIME | 创建时间 |

> **说明**：商品发布时商家选择规格模板及对应值，组合生成 SKU。Redis 中可缓存热门商品 SKU 的库存，下单时先扣 Redis 库存，异步同步 MySQL。

---

## 四、购物车

### `cart_item` 购物车表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联 user 表 |
| product_id | BIGINT FK | 关联 product 表 |
| sku_id | BIGINT FK | 关联 product_sku 表 |
| quantity | INT | 数量 |
| selected | TINYINT | 0-未选中, 1-已选中 |
| created_at | DATETIME | 加入时间 |

> **说明**：同一用户 + 同一 SKU 不可重复添加，重复时合并数量。结算时校验 sku_id 对应的实时库存。

---

## 五、订单与支付

### `order` 订单表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_no | VARCHAR(32) | 订单编号，唯一 |
| user_id | BIGINT FK | 关联 user 表 |
| address_snapshot | VARCHAR(500) | 收货地址快照（JSON：姓名、电话、省市区、详细地址） |
| total_amount | INT | 实付金额（单位：分） |
| status | TINYINT | 状态码（见 PRD 3.5.2 节） |
| cancel_reason | VARCHAR(500) | 取消原因 |
| paid_at | DATETIME | 支付时间 |
| shipped_at | DATETIME | 发货时间 |
| received_at | DATETIME | 收货时间 |
| completed_at | DATETIME | 完成时间 |
| created_at | DATETIME | 下单时间 |
| updated_at | DATETIME | 更新时间 |

> **说明**：`address_snapshot` 下单时快照保存，防止用户后续修改地址影响历史订单。

### `order_item` 订单明细表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_id | BIGINT FK | 关联 order 表 |
| product_id | BIGINT FK | 关联 product 表（不设外键约束，允许商品后被删除） |
| sku_id | BIGINT FK | 关联 product_sku 表 |
| quantity | INT | 购买数量 |
| unit_price | INT | 下单时单价（分） |
| subtotal | INT | 小计（分） |

> **说明**：商品名称、图片等展示信息通过 `product_id` / `sku_id` 实时关联查询。`unit_price` 存下单时的价格，即使后续改价，历史记录不变。

### `payment` 支付记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_id | BIGINT FK | 关联 order 表 |
| pay_method | TINYINT | 支付方式：1-卡号支付, 2-二维码支付 |
| card_no | VARCHAR(20) | 卡号（脱敏或明文） |
| status | TINYINT | 0-处理中, 1-成功, 2-失败 |
| paid_at | DATETIME | 支付成功时间 |
| created_at | DATETIME | 创建时间 |

### `refund` 退款/退货表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_id | BIGINT FK | 关联 order 表 |
| order_item_id | BIGINT FK | 关联 order_item（支持部分退款） |
| user_id | BIGINT FK | 申请人 |
| type | TINYINT | 1-退款, 2-退货 |
| reason | VARCHAR(500) | 申请原因 |
| amount | INT | 退款金额（分） |
| status | TINYINT | 0-待审核, 1-已通过, 2-已拒绝 |
| reviewer_id | BIGINT | 审核人（管理员或商家） |
| review_reason | VARCHAR(500) | 审核意见 |
| reviewed_at | DATETIME | 审核时间 |
| created_at | DATETIME | 申请时间 |

---

## 六、收货地址

### `address` 收货地址表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联 user 表 |
| receiver_name | VARCHAR(50) | 收货人姓名 |
| phone | VARCHAR(20) | 联系电话 |
| province | VARCHAR(50) | 省（纯文本） |
| city | VARCHAR(50) | 市（纯文本） |
| district | VARCHAR(50) | 区（纯文本） |
| detail | VARCHAR(200) | 详细地址 |
| is_default | TINYINT | 0-否, 1-默认地址 |
| created_at | DATETIME | 创建时间 |

---

## 七、商品评论

### `comment` 评论表（多级嵌套）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| product_id | BIGINT FK | 关联 product 表 |
| user_id | BIGINT FK | 关联 user 表 |
| order_item_id | BIGINT | 关联 order_item（购买后评价时有值，可追溯来源） |
| parent_id | BIGINT | 父评论 ID，NULL 表示一级评论，非 NULL 表示回复 |
| content | VARCHAR(1000) | 评论内容 |
| star | TINYINT | 星级评分（1-5），仅一级评论有值，回复为 NULL |
| is_append | TINYINT | 0-首次评价, 1-追评 |
| status | TINYINT | 0-正常, 1-已删除（管理员/商家删除） |
| created_at | DATETIME | 评论时间 |

> **整体设计思路**：
> - 所有登录用户均可发表一级评论和回复。
> - 用户确认收货后弹出星级评分窗口，提交后生成 `star + is_append=0` 的评论；之后用户可再次评论同一商品，此时 `is_append=1`，实现追评。
> - 回复采用 `parent_id` 递归实现无限级嵌套，查询时用 MySQL 递归 CTE 或应用层递归组装。
> - Redis 缓存热门商品的评论总数、平均星级等聚合数据；新增评论时异步更新 Redis 并批量同步 MySQL。
> - 商家和管理员可删除/回复任何评论，删除为物理删除。

---

## 八、搜索历史

### `search_history` 搜索历史表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联 user 表 |
| keyword | VARCHAR(100) | 搜索关键词 |
| searched_at | DATETIME | 搜索时间 |

---

## 九、即时通讯（WebSocket 聊天）

### `conversation` 会话表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 普通用户 ID，关联 user 表 |
| merchant_id | BIGINT | 商家用户 ID，关联 user 表 |
| product_id | BIGINT | 关联的商品 ID（可选，标记从哪个商品发起的对话） |
| last_message | VARCHAR(500) | 最后一条消息摘要 |
| last_message_time | DATETIME | 最后一条消息时间 |
| user_unread | INT DEFAULT 0 | 用户未读数 |
| merchant_unread | INT DEFAULT 0 | 商家未读数 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

> **说明**：同一用户和同一商家之间只有一条会话。通过 `(user_id, merchant_id)` 唯一约束防止重复创建。用户从商品详情页点击"联系卖家"时传入 `product_id`，若会话已存在则忽略此字段。

### `message` 消息表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| conversation_id | BIGINT FK | 关联 conversation 表 |
| sender_id | BIGINT | 发送者 ID，关联 user 表 |
| sender_role | TINYINT | 发送者角色：0-用户, 1-商家 |
| content | VARCHAR(2000) | 消息内容 |
| type | TINYINT | 消息类型：1-文本, 2-图片 |
| is_read | TINYINT | 0-未读, 1-已读 |
| created_at | DATETIME | 发送时间 |

> **说明**：离线消息：发送方发送时写入数据库，接收方上线后从数据库拉取未读消息并通过 WebSocket 推送。定时任务每天凌晨清理 `created_at < 30天前` 的消息。

---

## 十、操作日志

### `operation_log` 操作日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 操作人 ID |
| username | VARCHAR(50) | 操作人用户名 |
| ip | VARCHAR(50) | 操作 IP |
| module | VARCHAR(50) | 模块（如"管理员"、"订单"） |
| action | VARCHAR(200) | 操作描述 |
| params | TEXT | 请求参数（JSON） |
| result | VARCHAR(50) | 结果（成功/失败） |
| cost_time | BIGINT | 耗时（ms） |
| created_at | DATETIME | 操作时间 |

> 通过 AOP 切面自动记录，Service 层方法添加 `@OperationLog` 注解即可触发。

### operation_log 索引

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_user_id` | INDEX | user_id | 查某用户的操作记录 |
| `idx_module` | INDEX | module | 按模块筛选 |
| `idx_created_at` | INDEX | created_at | 按时间排序/清理 |

---

## 十一、MySQL 索引设计

> 目的：加速高频查询、避免全表扫描，提升 SQL 执行效率。

### 9.1 user 用户表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `uk_username` | UNIQUE | username | 登录/注册时校验用户名唯一 |
| `idx_phone` | INDEX | phone | 手机号登录查询 |
| `idx_email` | INDEX | email | 邮箱登录查询 |
| `idx_role` | INDEX | role | 管理员按角色筛选用户 |

### 9.2 merchant 商家表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_user_id` | INDEX | user_id | 通过用户查商家信息 |
| `idx_status` | INDEX | status | 管理员筛选待审核商家 |

### 9.3 category 分类表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_parent_id` | INDEX | parent_id | 查询子分类 |
| `idx_level` | INDEX | level | 按层级筛选 |

### 9.4 spec_template / spec_value 规格表

| 表 | 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|------|
| spec_value | `idx_template_id` | INDEX | template_id | 查某个规格模板下的所有值 |

### 9.5 product 商品表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_category_id` | INDEX | category_id | 按分类浏览商品 |
| `idx_merchant_id` | INDEX | merchant_id | 商家查自己的商品 |
| `idx_status` | INDEX | status | 筛选上架商品 |
| `idx_category_status` | **COMPOSITE** | (category_id, status) | **高频**：分类页筛选上架商品，覆盖索引 |
| `idx_status_sales` | **COMPOSITE** | (status, sales) | 热门商品排行 |
| `idx_created_at` | INDEX | created_at | 按上架时间排序 |
| `idx_brand` | INDEX | brand | 按品牌筛选 |
| `ft_product` | **FULLTEXT** | (name, description, brand) | MySQL 全文检索兜底（ES 不可用时） |

### 9.6 product_sku SKU 表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_product_id` | INDEX | product_id | 查询某商品所有 SKU |
| `idx_status` | INDEX | status | 筛选启用的 SKU |

### 9.7 cart_item 购物车表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `uk_user_sku` | **UNIQUE** | (user_id, sku_id) | 防止重复添加，同一用户同一 SKU 唯一 |
| `idx_user_id` | INDEX | user_id | 查询用户购物车列表 |

### 9.8 order 订单表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `uk_order_no` | UNIQUE | order_no | 订单编号唯一，精确查询 |
| `idx_user_id` | INDEX | user_id | 用户查看自己的订单 |
| `idx_status` | INDEX | status | 管理员/商家按状态筛选 |
| `idx_user_status` | **COMPOSITE** | (user_id, status) | **高频**：用户按状态查订单（如"我的待支付订单"） |
| `idx_created_at` | INDEX | created_at | 按时间排序/筛选 |

### 9.9 order_item 订单明细表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_order_id` | INDEX | order_id | 查某订单的所有明细 |
| `idx_product_id` | INDEX | product_id | 反向查某商品被哪些订单购买 |
| `idx_sku_id` | INDEX | sku_id | 按 SKU 统计销量 |

### 9.10 payment 支付记录表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_order_id` | INDEX | order_id | 查某订单的支付记录 |

### 9.11 refund 退款/退货表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_order_id` | INDEX | order_id | 查某订单的退款记录 |
| `idx_order_item_id` | INDEX | order_item_id | 部分退款时定位具体商品 |
| `idx_user_id` | INDEX | user_id | 用户查自己的退款记录 |
| `idx_status` | INDEX | status | 管理员/商家筛选待审核退款 |

### 9.12 address 收货地址表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_user_id` | INDEX | user_id | 查用户的所有地址 |

### 9.13 comment 评论表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_product_id` | INDEX | product_id | 查某商品的所有评论 |
| `idx_product_status` | **COMPOSITE** | (product_id, status) | **高频**：查某商品的正常评论（排除已删除） |
| `idx_user_id` | INDEX | user_id | 查某用户的历史评论 |
| `idx_parent_id` | INDEX | parent_id | 查某条评论的回复列表 |
| `idx_order_item_id` | INDEX | order_item_id | 判断是否已评价过 |

### 9.14 search_history 搜索历史表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_user_id` | INDEX | user_id | 查用户的搜索历史 |
| `idx_searched_at` | INDEX | searched_at | 按时间排序，定期清理旧记录 |

### 9.15 conversation 会话表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `uk_user_merchant` | **UNIQUE** | (user_id, merchant_id) | 防止同一对用户和商家重复创建会话 |
| `idx_user_id` | INDEX | user_id | 查某用户的所有会话列表 |
| `idx_merchant_id` | INDEX | merchant_id | 查某商家的所有会话列表 |
| `idx_updated_at` | INDEX | updated_at | 按最后消息时间排序 |

### 9.16 message 消息表

| 索引名 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `idx_conversation_id` | INDEX | conversation_id | 查某会话的所有消息 |
| `idx_conversation_read` | **COMPOSITE** | (conversation_id, is_read) | 查某会话的未读消息 |
| `idx_created_at` | INDEX | created_at | 按时间排序 + 定时清理 30 天前消息 |

### 9.17 索引设计总结

| 类型 | 数量 | 说明 |
|------|------|------|
| UNIQUE 索引 | 4 | username、user+sku、order_no、user+merchant |
| 普通 INDEX | 26 | 高频查询字段 |
| COMPOSITE 索引 | 5 | 多条件组合高频查询，利用覆盖索引减少回表 |
| FULLTEXT 索引 | 1 | MySQL 全文检索兜底 |
| **合计** | **36** | |

> **注意**：复合索引遵循**最左前缀原则**，`WHERE` 条件中必须包含复合索引的第一列才能命中。例如 `idx_category_status(category_id, status)` 支持「只查 category_id」和「同时查 category_id + status」，但不支持「只查 status」。

---

## 十二、Redis 缓存设计

| 缓存 Key 示例 | 内容 | 数据结构 | 过期策略 |
|------|------|------|------|
| `token:{userId}` | 用户登录态 | String | 设置过期时间 |
| `hot:products` | 首页热门商品列表 | ZSet（按销量排序） | 定期刷新 |
| `category:tree` | 三级分类树 | String（JSON） | 分类变更时主动更新 |
| `hot:search` | 搜索热词排行 | ZSet | 定期刷新 |
| `verify:{uuid}` | 图形验证码 | String | 5 分钟过期 |
| `product:{id}:stock` | 商品 SKU 库存 | Hash | 下单时扣减，定时同步 MySQL |
| `product:{id}:sales` | 商品累计销量 | Hash | 下单时自增，定时同步 MySQL |
| `product:{id}:comments` | 评论统计（总数 + 平均星级） | Hash | 评论变更时更新 |
| `sku:{id}` | 热门 SKU 缓存 | String（JSON） | 设置过期时间，变更时失效 |

---

## 十三、搜索引擎（Elasticsearch）

| 索引 | 同步字段 | 说明 |
|------|------|------|
| `product_index` | id, name, description, brand, category_id, min_price, sales, created_at | 商品上架时写入，下架/删除时移除，更新时异步同步 |
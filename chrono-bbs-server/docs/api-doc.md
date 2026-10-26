# 网上超市系统 — API 接口文档

> 基础路径：`http://localhost:8080`
>
> 在线调试：启动项目后访问 `http://localhost:8080/doc.html`（Knife4j）
>
> **通用说明**：除公开接口外，所有请求需在 Header 中携带 Token：
> ```
> Authorization: Bearer <token>
> ```

---

## 目录

- [1. 通用规范](#1-通用规范)
- [2. 用户认证](#2-用户认证-apiuser)
- [3. 分类管理](#3-分类管理-apicategory)
- [4. 商品管理](#4-商品管理-apiproduct)
- [5. 购物车](#5-购物车-apicart)
- [6. 订单管理](#6-订单管理-apiorder)
- [7. 支付管理](#7-支付管理-apipayment)
- [8. 收货地址](#8-收货地址-apiaddress)
- [9. 评论管理](#9-评论管理-apicomment)
- [10. 退款管理](#10-退款管理-apirefund)
- [11. 商家管理](#11-商家管理-apimerchant)
- [12. 管理后台](#12-管理后台-apiadmin)
- [13. 文件上传](#13-文件上传-apiupload)
- [14. 即时通讯](#14-即时通讯-apichat)
- [15. 商品搜索](#15-商品搜索-apisearch)
- [16. WebSocket 通信](#16-websocket-通信)
- [17. 错误码说明](#17-错误码说明)
- [18. 接口权限总览](#18-接口权限总览)

---

## 1. 通用规范

### 1.1 统一返回格式 `Result<T>`

所有接口返回以下 JSON 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1700000000000
}
```

### 1.2 分页返回格式 `PageResult<T>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "list": [ ... ]
  },
  "timestamp": 1700000000000
}
```

### 1.3 请求/响应统一后缀

| 类别 | 后缀 | 说明 | 所在模块 |
|------|------|------|----------|
| 入参 | `DTO` | 前端传给后端的参数 | `module-api` 或 `module-service` |
| 出参 | `VO` | 后端返回给前端的数据 | `module-api` |

### 1.4 认证方式

采用 **JWT + Redis 双校验**：
1. 登录成功后返回 `token`
2. 后续请求在 Header 中携带：`Authorization: Bearer <token>`
3. 后端拦截器解析 JWT → 从 Redis 校验 Token 有效性 → 放行/拒绝

### 1.5 角色说明

| 角色 | 值 | 说明 |
|------|-----|------|
| 游客 | — | 未登录，仅可浏览商品 |
| 注册用户 | `1` | 已登录，可加购、下单、支付 |
| 商家 | `2` | 可管理自己的商品和订单 |
| 管理员 | `3` | 全局管理 |

---

## 2. 用户认证 `/api/user`

**Controller**：`AuthController`  
**Swagger 分组**：用户认证

### 2.1 发送验证码

> 生成图形验证码，返回验证码图片的 UUID

```
GET /api/user/captcha
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| — | — | — | 无参数 |

**返回**：

```json
{
  "code": 200,
  "data": {
    "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "image": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

---

### 2.2 用户注册

```
POST /api/user/register
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，长度 3-20 |
| password | String | 是 | 密码，长度 6-20 |
| phone | String | 是 | 手机号，格式 `1[3-9]\d{9}` |
| email | String | 否 | 邮箱 |
| role | Integer | 是 | 角色：1=普通用户，2=商家 |
| captchaUuid | String | 是 | 验证码 UUID（来自 2.1） |
| captchaCode | String | 是 | 验证码输入值 |

**返回**：

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 2,
      "username": "testuser",
      "nickname": "测试用户",
      "phone": "13800138000",
      "role": 1
    }
  }
}
```

---

### 2.3 用户登录

```
POST /api/user/login
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| captchaUuid | String | 是 | 验证码 UUID |
| captchaCode | String | 是 | 验证码输入值 |

**返回**：同注册，包含 `token` + `userInfo`

---

### 2.4 获取当前用户信息

```
GET /api/user/info
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 无 | — | — | 用户 ID 从 Header `Authorization: Bearer <token>` 的 JWT 中解析，无需传入参数 |

**返回**：

```json
{
  "code": 200,
  "data": {
    "id": 2,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": "https://oss.example.com/avatar/uuid.jpg",
    "role": 1,
    "status": 1
  }
}
```

---

### 2.5 修改个人信息

```
PUT /api/user/profile
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 昵称 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像 URL（先调上传接口获取） |

---

### 2.6 修改密码

```
PUT /api/user/password
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码，长度 6-20 |

---

### 2.7 退出登录

```
POST /api/user/logout
```

无请求体。后端删除 Redis 中的 Token，使当前 Token 立即失效。

---

### 2.8 发送重置密码验证码

> 用户忘记密码时，输入注册时的手机号或邮箱，后端发送短信/邮件验证码

```
POST /api/user/forgot-code
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 手机号 或 邮箱 |
| captchaUuid | String | 是 | 图形验证码 UUID（来自 2.1） |
| captchaCode | String | 是 | 图形验证码输入值 |

**返回**：

```json
{
  "code": 200,
  "data": {
    "uuid": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "maskAccount": "138****8000"
  }
}
```

> 验证码有效期 5 分钟，存入 Redis：`reset:code:{uuid}`
>
> 课程设计阶段，验证码直接返回在响应中（模拟短信/邮件），生产环境应真正发送

---

### 2.9 忘记密码 — 重置

> 用户输入验证码和新密码，完成密码重置

```
POST /api/user/reset-password
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uuid | String | 是 | 验证码 UUID（来自 2.8） |
| code | String | 是 | 短信/邮箱验证码 |
| newPassword | String | 是 | 新密码，长度 6-20 |

**返回**：

```json
{
  "code": 200,
  "message": "密码重置成功，请重新登录"
}
```

> 验证码校验通过后立即删除 Redis 中的验证码，保证一次性有效


## 3. 分类管理 `/api/category`

**Controller**：`CategoryController`  
**Swagger 分组**：分类管理  
**权限**：公开

### 3.1 获取分类树（三级）

```
GET /api/category/tree
```

**返回**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "level": 1,
      "children": [
        {
          "id": 2,
          "name": "手机通讯",
          "level": 2,
          "children": [
            { "id": 3, "name": "智能手机", "level": 3 },
            { "id": 4, "name": "功能手机", "level": 3 }
          ]
        }
      ]
    }
  ]
}
```

---

### 3.2 获取子分类列表

```
GET /api/category/children?parentId=0
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 否 | 父分类 ID，默认 0（查一级分类） |

---

## 4. 商品管理 `/api/product`

**Controller**：`ProductController`  
**Swagger 分组**：商品管理

### 4.1 分页查询商品列表

> 公开接口

```
GET /api/product/list
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 分类 ID |
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页大小，默认 20 |

**返回**：`PageResult<ProductVO>`，每条包含商品名、图片、最低价、销量

---

### 4.2 商品详情

> 公开接口

```
GET /api/product/{id}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 商品 ID（路径参数） |

**返回**：商品全量信息 + SKU 列表 + 规格列表

---

### 4.3 添加商品

> 权限：商家

```
POST /api/product
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 商品名称 |
| description | String | 否 | 商品描述 |
| images | String | 否 | 商品图片（JSON 数组字符串，最多 4 张） |
| brand | String | 否 | 品牌 |
| categoryId | Long | 是 | 三级分类 ID |
| skuList | List | 是 | SKU 列表 |

**SKU 项**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| specIds | String | 是 | 规格 ID 组合，如 `"1,3"` |
| specText | String | 是 | 规格文本，如 `"红色 / 64GB"` |
| price | Integer | 是 | 价格（分） |
| stock | Integer | 是 | 库存数量 |

---

### 4.4 修改商品

```
PUT /api/product/{id}
```

参数同 4.3。

---

### 4.5 商品上下架

```
PUT /api/product/{id}/status
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 1=上架，0=下架 |

---

### 4.6 删除商品

```
DELETE /api/product/{id}
```

---

## 5. 购物车 `/api/cart`

**Controller**：`CartController`  
**Swagger 分组**：购物车  
**权限**：需登录

### 5.1 获取购物车列表

```
GET /api/cart/list
```

**返回**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "productId": 100,
      "productName": "iPhone 15",
      "productImage": "https://oss.example.com/p1.jpg",
      "skuId": 10,
      "specText": "红色 / 64GB",
      "price": 599900,
      "quantity": 2,
      "stock": 99,
      "selected": 1
    }
  ]
}
```

---

### 5.2 加入购物车

```
POST /api/cart/add
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | Long | 是 | SKU ID |
| quantity | Integer | 是 | 数量，≥1 |

> 若同一 SKU 已在购物车中，则累加数量

---

### 5.3 修改数量

```
PUT /api/cart/update
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cartItemId | Long | 是 | 购物车项 ID |
| quantity | Integer | 是 | 新数量 |

---

### 5.4 选中/取消选中

```
PUT /api/cart/select
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cartItemId | Long | 是 | 购物车项 ID |
| selected | Integer | 是 | 1=选中，0=取消 |

---

### 5.5 删除购物车项

```
DELETE /api/cart/{id}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 购物车项 ID（路径参数） |

---

### 5.6 全选/取消全选

```
PUT /api/cart/select-all
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| selected | Integer | 是 | 1=全选，0=取消全选 |

---

## 6. 订单管理 `/api/order`

**Controller**：`OrderController`  
**Swagger 分组**：订单管理  
**权限**：需登录

### 6.1 获取防重复提交 Token

> 下单前必须先调此接口

```
GET /api/order/submit-token
```

**返回**：

```json
{
  "code": 200,
  "data": { "token": "uuid-string-5min-valid" }
}
```

> Token 有效期 5 分钟，使用后立即删除，防止重复提交

---

### 6.2 创建订单

```
POST /api/order/create
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | 是 | 防重复提交 Token（来自 6.1） |
| addressId | Long | 是 | 收货地址 ID |
| cartItemIds | List\<Long\> | 是 | 购物车选中项的 ID 列表 |
| remark | String | 否 | 订单备注 |

**返回**：

```json
{
  "code": 200,
  "data": {
    "orderId": 20241022001,
    "orderNo": "202410221530001234",
    "totalAmount": 1199800,
    "status": 1,
    "createdAt": "2024-10-22T15:30:00"
  }
}
```

---

### 6.3 订单列表

```
GET /api/order/list
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 订单状态（见下方），不传查全部 |
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页大小，默认 10 |

**订单状态枚举**：

| 状态码 | 含义 |
|--------|------|
| 1 | 待支付 |
| 2 | 已支付（待发货） |
| 3 | 已发货（待收货） |
| 4 | 已收货（待评价） / 已完成 |
| 5 | 已取消 |
| 6 | 退款中 |
| 7 | 已退款 |

---

### 6.4 订单详情

```
GET /api/order/{id}
```

**返回**：订单全量信息 + 订单项列表 + 物流信息（如有）

---

### 6.5 取消订单

```
PUT /api/order/{id}/cancel
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单 ID（路径参数） |
| reason | String | 否 | 取消原因 |

> 仅「待支付」状态可取消，取消后恢复库存

---

### 6.6 确认收货

```
PUT /api/order/{id}/receive
```

> 仅「已发货」状态可确认收货

---

### 6.7 删除订单

```
DELETE /api/order/{id}
```

> 仅「已完成」或「已取消」状态可删除

---

## 7. 支付管理 `/api/payment`

**Controller**：`PaymentController`（或在 OrderController 中）  
**Swagger 分组**：支付管理  
**权限**：需登录

### 7.1 卡号支付

```
POST /api/payment/card
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 订单 ID |
| cardNo | String | 是 | 卡号（16-19 位数字） |
| expireDate | String | 是 | 有效期，格式 `MM/YY` |
| cvv | String | 是 | 安全码，3-4 位 |

> 前端先做 Luhn 算法校验，后端模拟支付成功

**返回**：

```json
{
  "code": 200,
  "data": { "status": 2, "paidAt": "2024-10-22T15:32:00" }
}
```

---

### 7.2 获取支付二维码

```
POST /api/payment/qrcode
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 订单 ID |

**返回**：

```json
{
  "code": 200,
  "data": {
    "orderId": 20241022001,
    "totalAmount": 1199800,
    "qrcodeUrl": "https://api.qrserver.com/v1/create-qr-code/?data=..."
  }
}
```

---

### 7.3 确认二维码支付

```
POST /api/payment/confirm-qrcode
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 订单 ID |

---

## 8. 收货地址 `/api/address`

**Controller**：`AddressController`  
**Swagger 分组**：收货地址  
**权限**：需登录

### 8.1 地址列表

```
GET /api/address/list
```

---

### 8.2 地址详情

```
GET /api/address/{id}
```

---

### 8.3 新增地址

```
POST /api/address
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverName | String | 是 | 收货人姓名 |
| phone | String | 是 | 手机号 |
| province | String | 是 | 省 |
| city | String | 是 | 市 |
| district | String | 是 | 区 |
| detail | String | 是 | 详细地址 |
| isDefault | Integer | 否 | 1=默认地址 |

---

### 8.4 修改地址

```
PUT /api/address/{id}
```

参数同 8.3。

---

### 8.5 设置默认地址

```
PUT /api/address/{id}/default
```

---

### 8.6 删除地址

```
DELETE /api/address/{id}
```

---

## 9. 评论管理 `/api/comment`

**Controller**：`CommentController`  
**Swagger 分组**：评论管理

### 9.1 商品评论列表（公开）

```
GET /api/comment/list/{productId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品 ID（路径参数） |
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页大小，默认 10 |
| star | Integer | 否 | 按星级筛选（1-5） |

**返回**：含评论内容、星级、用户信息、追评内容

---

### 9.2 发表评论

> 权限：需登录，仅已收货用户可评价

```
POST /api/comment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品 ID |
| orderItemId | Long | 是 | 订单明细 ID |
| content | String | 是 | 评论内容 |
| star | Integer | 是 | 星级（1-5） |

---

### 9.3 追加评论

```
POST /api/comment/append
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 原评论 ID |
| content | String | 是 | 追评内容 |

---

### 9.4 商家回复

> 权限：商家（仅可回复自己商品的评论）

```
POST /api/comment/reply
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 是 | 被回复的评论 ID |
| content | String | 是 | 回复内容 |

---

## 10. 退款管理 `/api/refund`

**Controller**：`RefundController`  
**Swagger 分组**：退款管理  
**权限**：需登录

### 10.1 申请退款/退货

```
POST /api/refund
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderItemId | Long | 是 | 订单明细 ID |
| type | Integer | 是 | 1=仅退款，2=退款+退货 |
| reason | String | 是 | 退款原因 |
| amount | Integer | 是 | 退款金额（分） |

---

### 10.2 退款列表（我的）

```
GET /api/refund/my
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |

---

### 10.3 退款详情

```
GET /api/refund/{id}
```

---

### 10.4 审核退款/退货

> 权限：商家或管理员

```
PUT /api/refund/{id}/review
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 退款单 ID（路径参数） |
| approved | Integer | 是 | 1=同意，0=拒绝 |
| reviewReason | String | 否 | 审核意见 |

---

## 11. 商家管理 `/api/merchant`

**Controller**：`MerchantController`  
**Swagger 分组**：商家管理  
**权限**：需登录（部分接口需商家角色）

### 11.1 商家入驻申请

```
POST /api/merchant/apply
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shopName | String | 是 | 店铺名称 |
| businessLicense | String | 是 | 营业执照图片 URL |
| contactPhone | String | 是 | 联系电话 |

---

### 11.2 获取商家信息

```
GET /api/merchant/info
```

---

### 11.3 商家商品列表

```
GET /api/merchant/products
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |
| status | Integer | 否 | 状态筛选 |

---

### 11.4 商家订单列表

```
GET /api/merchant/orders
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 订单状态筛选 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |

---

### 11.5 发货

```
PUT /api/merchant/ship/{orderId}
```

---

### 11.6 商家销售统计

```
GET /api/merchant/stats
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 `yyyy-MM-dd` |
| endDate | String | 否 | 结束日期 `yyyy-MM-dd` |

**返回**：

```json
{
  "code": 200,
  "data": {
    "totalSales": 5000000,
    "orderCount": 120,
    "topProducts": [ ... ],
    "salesByCategory": [ ... ],
    "salesTrend": [ ... ]
  }
}
```

---

## 12. 管理后台 `/api/admin`

**Controller**：`AdminController`  
**Swagger 分组**：管理后台  
**权限**：仅管理员

### 12.1 用户管理

#### 用户列表

```
GET /api/admin/users
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索用户名/手机号 |
| role | Integer | 否 | 按角色筛选 |
| status | Integer | 否 | 按状态筛选 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |

#### 启用/禁用用户

```
PUT /api/admin/users/{id}/status
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 1=启用，0=禁用 |

---

### 12.2 商家审核

#### 商家申请列表

```
GET /api/admin/merchants
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 0=待审核，1=已通过，2=已拒绝 |

#### 审核商家

```
PUT /api/admin/merchants/{id}/review
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Integer | 是 | 1=通过，0=拒绝 |
| reviewReason | String | 否 | 审核意见 |

---

### 12.3 分类管理

#### 新增分类

```
POST /api/admin/categories
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 分类名称 |
| parentId | Long | 否 | 父分类 ID（一级分类不传或传 0） |
| level | Integer | 是 | 层级：1/2/3 |
| sortOrder | Integer | 否 | 排序值 |
| icon | String | 否 | 图标 |

#### 修改分类

```
PUT /api/admin/categories/{id}
```

#### 删除分类

```
DELETE /api/admin/categories/{id}
```

---

### 12.4 全平台商品管理

```
GET /api/admin/products
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索 |
| categoryId | Long | 否 | 按分类筛选 |
| merchantId | Long | 否 | 按商家筛选 |
| status | Integer | 否 | 0=下架，1=上架 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |

---

### 12.5 全平台订单管理

```
GET /api/admin/orders
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | String | 否 | 订单号搜索 |
| status | Integer | 否 | 状态筛选 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |

---

### 12.6 全局销售报表

```
GET /api/admin/report
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| groupBy | String | 否 | `day` / `week` / `month` |

**返回**：

```json
{
  "code": 200,
  "data": {
    "totalSales": 50000000,
    "totalOrders": 1200,
    "avgOrderAmount": 41666,
    "topProducts": [
      { "productId": 1, "productName": "...", "salesCount": 200, "totalAmount": 8000000 }
    ],
    "salesByCategory": [
      { "categoryId": 1, "categoryName": "电子产品", "totalAmount": 20000000 }
    ],
    "salesByMerchant": [
      { "merchantId": 1, "shopName": "官方旗舰店", "totalAmount": 30000000 }
    ],
    "salesTrend": [
      { "date": "2024-10-01", "amount": 1500000 },
      { "date": "2024-10-02", "amount": 2300000 }
    ],
    "orderStatusDistribution": {
      "pending": 50,
      "paid": 30,
      "shipped": 200,
      "completed": 900,
      "cancelled": 20
    }
  }
}
```

---

## 13. 文件上传 `/api/upload`

**Controller**：`UploadController`  
**Swagger 分组**：文件上传  
**权限**：需登录

### 13.1 上传图片

```
POST /api/upload/image
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 图片文件（form-data） |

**校验规则**：

| 规则 | 值 |
|------|-----|
| 允许格式 | `jpg`、`jpeg`、`png`、`webp` |
| 单张最大 | 2MB |
| 一次最多 | 4 张 |
| 压缩宽度 | 800px（等比缩放） |

**返回**：

```json
{
  "code": 200,
  "data": { "url": "https://oss.example.com/product/20241022/uuid.jpg" }
}
```

---

## 14. 即时通讯 `/api/chat`

**Controller**：`ChatController`  
**Swagger 分组**：即时通讯  
**权限**：需登录

### 14.1 会话列表

```
GET /api/chat/conversations
```

**返回**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 2,
      "merchantId": 5,
      "merchantName": "数码旗舰店",
      "merchantAvatar": "https://...",
      "productId": 100,
      "productName": "iPhone 15",
      "lastMessage": "好的，今天发货",
      "lastMessageTime": "2024-10-22T15:00:00",
      "unreadCount": 2
    }
  ]
}
```

---

### 14.2 历史消息

```
GET /api/chat/history/{conversationId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | Long | 是 | 会话 ID（路径参数） |
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页大小，默认 20 |

**返回**：消息列表（从新到旧），每条含发送者信息、内容、类型、时间、已读状态

---

### 14.3 创建/获取会话

```
POST /api/chat/conversation
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchantId | Long | 是 | 商家 ID |
| productId | Long | 否 | 商品 ID（关联商品） |

> 若已有同用户+同商家的会话则直接返回，否则新建

---

### 14.4 标记已读

```
PUT /api/chat/read/{conversationId}
```

---

### 14.5 未读消息总数

```
GET /api/chat/unread-count
```

**返回**：

```json
{
  "code": 200,
  "data": { "count": 5 }
}
```

---

## 15. 商品搜索 `/api/search`

**Controller**：`SearchController`  
**Swagger 分组**：商品搜索  
**权限**：公开

### 15.1 全文检索

```
GET /api/search
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| categoryId | Long | 否 | 分类筛选 |
| brand | String | 否 | 品牌筛选 |
| minPrice | Integer | 否 | 最低价（分） |
| maxPrice | Integer | 否 | 最高价（分） |
| sort | String | 否 | `sales_desc` / `price_asc` / `price_desc` / `time_desc` |
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页大小，默认 20 |

> 前端在 Vite 开发环境下通过代理直连 ES（`/es` → `localhost:9200`），也可以是后端中转。本文档按后端中转路径设计，方便统一。

**返回**：

```json
{
  "code": 200,
  "data": {
    "total": 156,
    "page": 1,
    "size": 20,
    "list": [
      {
        "id": 1,
        "name": "iPhone 15",
        "description": "...",
        "brand": "Apple",
        "categoryId": 1,
        "minPrice": 599900,
        "sales": 2000,
        "image": "https://..."
      }
    ]
  }
}
```

---

### 15.2 搜索建议（自动补全）

```
GET /api/search/suggest?keyword=iph
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 前缀关键词 |

**返回**：

```json
{
  "code": 200,
  "data": ["iPhone 15", "iPhone 15 Pro", "iPhone 14", "iPhone充电器"]
}
```

---

### 15.3 搜索历史

```
GET /api/search/history
```

> 仅登录用户，返回最近 20 条

---

### 15.4 清除搜索历史

```
DELETE /api/search/history
```

---

### 15.5 热搜榜

```
GET /api/search/hot
```

> 公开接口，基于近 7 天搜索频率

---

## 16. WebSocket 通信

**端点**：`ws://localhost:8080/chat/{userId}?token=<jwt_token>`  
**协议**：JSR 356 原生 WebSocket（后端 `@OnOpen` / `@OnMessage` / `@OnClose`）

### 16.1 连接

```typescript
const ws = new WebSocket(`ws://localhost:8080/chat/${userId}?token=${jwtToken}`)
```

### 16.2 发送消息（JSON 格式）

```json
{
  "type": "TEXT",
  "conversationId": 123,
  "content": "你好，这个商品有货吗？",
  "productId": 456,
  "timestamp": 1700000000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | `TEXT`（文本） |
| conversationId | Long | 会话 ID |
| content | String | 消息内容 |
| productId | Long | 关联商品 ID（可选） |
| timestamp | Long | 客户端时间戳（毫秒） |

### 16.3 接收消息

```json
{
  "type": "TEXT",
  "conversationId": 123,
  "senderId": 5,
  "senderRole": 2,
  "content": "有货的，今天可以发出",
  "productId": 456,
  "isRead": 0,
  "timestamp": 1700000010000
}
```

### 16.4 连接生命周期

| 事件 | `@OnOpen` | 校验 JWT Token，记录 `online:{userId}` 到 Redis |
|------|-----------|------|
| | | 推送离线期间未读消息 |
| 事件 | `@OnClose` | 删除 `online:{userId}` |
| 事件 | `@OnMessage` | 校验权限 → 存入 MySQL → 推送给接收方 |
| 事件 | `@OnError` | 记录日志 → 关闭连接 |

---

## 17. 错误码说明

| 错误码 | 含义 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 系统内部异常 |
| 2001 | 用户不存在 |
| 2002 | 密码错误 |
| 2003 | 用户名已存在 |
| 3001 | 商品不存在 |
| 3002 | 库存不足 |
| 3003 | 商品已下架 |
| 4001 | 订单不存在 |
| 4002 | 订单状态异常 |
| 4003 | 订单不可取消 |
| 5001 | 支付失败 |
| 5002 | 卡号无效（Luhn 校验不通过） |
| 6001 | 商家未审核通过 |
| 6002 | 已提交过入驻申请 |

---

## 18. 接口权限总览

| 模块 | 路径前缀 | 游客 | 注册用户 | 商家 | 管理员 |
|------|----------|------|----------|------|--------|
| 用户认证 | `/api/user` | 注册/登录/验证码 | 信息/修改密码/登出 | ← | ← |
| 分类管理 | `/api/category` | ✅ 查看 | ✅ | ✅ | ✅ |
| 商品管理 | `/api/product` | ✅ 查看 | ✅ 查看 | ✅ 管理自己的 | ✅ 全部管理 |
| 购物车 | `/api/cart` | ❌ | ✅ | ✅ | — |
| 订单管理 | `/api/order` | ❌ | ✅ 自己的 | ✅ 自己商品的 | ✅ 全部 |
| 支付管理 | `/api/payment` | ❌ | ✅ | ✅ | — |
| 收货地址 | `/api/address` | ❌ | ✅ | ✅ | — |
| 评论管理 | `/api/comment` | ✅ 查看 | ✅ 发表/追评 | ✅ 回复 | ✅ 管理 |
| 退款管理 | `/api/refund` | ❌ | ✅ 申请 | ✅ 审核 | ✅ 审核 |
| 商家管理 | `/api/merchant` | ❌ | ✅ 入驻 | ✅ 商品/订单/统计 | — |
| 管理后台 | `/api/admin` | ❌ | ❌ | ❌ | ✅ 全部 |
| 文件上传 | `/api/upload` | ❌ | ✅ | ✅ | ✅ |
| 即时通讯 | `/api/chat` | ❌ | ✅ | ✅ | — |
| 商品搜索 | `/api/search` | ✅ | ✅ | ✅ | ✅ |
| WebSocket | `ws://.../chat/` | ❌ | ✅ | ✅ | — |

> ✅ = 可访问，❌ = 不可访问，← = 同左

---

> **文档更新日期**：2024-10-22  
> **对应版本**：v1.0.0
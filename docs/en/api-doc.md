# Online Supermarket System — API Documentation

> [简体中文](../zh/api-doc.md) | English

> Base path: `http://localhost:8080`
>
> Online debugging: after starting the project, visit `http://localhost:8080/doc.html` (Knife4j)
>
> **General note**: Except for public endpoints, all requests must carry a Token in the Header:
> ```
> Authorization: Bearer <token>
> ```

---

## Table of Contents

- [1. General Conventions](#1-general-conventions)
- [2. User Authentication](#2-user-authentication-apiuser)
- [3. Category Management](#3-category-management-apicategory)
- [4. Product Management](#4-product-management-apiproduct)
- [5. Cart](#5-cart-apicart)
- [6. Order Management](#6-order-management-apiorder)
- [7. Payment Management](#7-payment-management-apipayment)
- [8. Shipping Address](#8-shipping-address-apiaddress)
- [9. Comment Management](#9-comment-management-apicomment)
- [10. Refund Management](#10-refund-management-apirefund)
- [11. Merchant Management](#11-merchant-management-apimerchant)
- [12. Admin Backend](#12-admin-backend-apiadmin)
- [13. File Upload](#13-file-upload-apiupload)
- [14. Instant Messaging](#14-instant-messaging-apichat)
- [15. Product Search](#15-product-search-apisearch)
- [16. Flash-sale Coupons](#16-flash-sale-coupons-apiseckill)
- [17. WebSocket Communication](#17-websocket-communication)
- [18. Error Code Reference](#18-error-code-reference)
- [19. API Permission Overview](#19-api-permission-overview)

---

## 1. General Conventions

### 1.1 Unified Response Format `Result<T>`

All APIs return the following JSON structure:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1700000000000
}
```

### 1.2 Paginated Response Format `PageResult<T>`

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

### 1.3 Unified Request/Response Suffix

| Category | Suffix | Description | Module |
|------|------|------|----------|
| Input | `DTO` | Parameters passed from the frontend to the backend | `module-api` or `module-service` |
| Output | `VO` | Data returned from the backend to the frontend | `module-api` |

### 1.4 Authentication Method

Use **JWT + Redis double validation**:
1. Return a `token` after a successful login
2. Subsequent requests carry it in the Header: `Authorization: Bearer <token>`
3. The backend interceptor parses the JWT → validates Token validity from Redis → allows/rejects

### 1.5 Roles

| Role | Value | Description |
|------|-----|------|
| Guest | — | Not logged in; can only browse products |
| Registered User | `1` | Logged in; can add to cart, place orders, and pay |
| Merchant | `2` | Can manage own products and orders |
| Administrator | `3` | Global management |

---

## 2. User Authentication `/api/user`

**Controller**: `AuthController`  
**Swagger Group**: User Authentication

### 2.1 Send Captcha

> Generate a graphical captcha and return the UUID of the captcha image

```
GET /api/user/captcha
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| — | — | — | No parameters |

**Return**:

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

### 2.2 User Registration

```
POST /api/user/register
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| username | String | Yes | Username, length 3-20 |
| password | String | Yes | Password, length 6-20 |
| phone | String | Yes | Phone number, format `1[3-9]\d{9}` |
| email | String | No | Email |
| role | Integer | No | Reserved field; registration always creates a regular user (1). Merchant role is granted only after merchant approval |
| captchaUuid | String | Yes | Captcha UUID (from 2.1) |
| captchaCode | String | Yes | Captcha input value |

**Return**:

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

### 2.3 User Login

```
POST /api/user/login
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| username | String | Yes | Username |
| password | String | Yes | Password |
| captchaUuid | String | Yes | Captcha UUID |
| captchaCode | String | Yes | Captcha input value |

**Return**: Same as registration, including `token` + `userInfo`

---

### 2.4 Get Current User Info

```
GET /api/user/info
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| None | — | — | The user ID is parsed from the JWT in the Header `Authorization: Bearer <token>`; no parameter is needed |

**Return**:

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

### 2.5 Update Personal Info

```
PUT /api/user/profile
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| nickname | String | No | Nickname |
| phone | String | No | Phone number |
| email | String | No | Email |
| avatar | String | No | Avatar URL (obtain it via the upload API first) |

---

### 2.6 Change Password

```
PUT /api/user/password
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| oldPassword | String | Yes | Old password |
| newPassword | String | Yes | New password, length 6-20 |

---

### 2.7 Logout

```
POST /api/user/logout
```

No request body. The backend deletes the Token from Redis, invalidating the current Token immediately.

---

### 2.8 Send Password Reset Captcha

> When a user forgets their password, they enter the phone number or email used during registration, and the backend sends an SMS/email captcha

```
POST /api/user/forgot-code
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| account | String | Yes | Phone number or email |
| captchaUuid | String | Yes | Graphical captcha UUID (from 2.1) |
| captchaCode | String | Yes | Graphical captcha input value |

**Return**:

```json
{
  "code": 200,
  "data": {
    "uuid": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "maskAccount": "138****8000"
  }
}
```

> The captcha is valid for 5 minutes and is stored in Redis: `reset:code:{uuid}`
>
> During the course design phase, the captcha is returned directly in the response (simulating SMS/email); in production it should actually be sent

---

### 2.9 Forgot Password — Reset

> The user enters the captcha and a new password to complete the password reset

```
POST /api/user/reset-password
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| uuid | String | Yes | Captcha UUID (from 2.8) |
| code | String | Yes | SMS/email captcha |
| newPassword | String | Yes | New password, length 6-20 |

**Return**:

```json
{
  "code": 200,
  "message": "密码重置成功，请重新登录"
}
```

> After the captcha is validated successfully, it is immediately deleted from Redis to ensure one-time use


## 3. Category Management `/api/category`

**Controller**: `CategoryController`  
**Swagger Group**: Category Management  
**Permission**: Public

### 3.1 Get Category Tree (Three Levels)

```
GET /api/category/tree
```

**Return**:

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

### 3.2 Get Subcategory List

```
GET /api/category/children?parentId=0
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| parentId | Long | No | Parent category ID, default 0 (query first-level categories) |

---

## 4. Product Management `/api/product`

**Controller**: `ProductController`  
**Swagger Group**: Product Management

### 4.1 Paged Product List

> Public endpoint

```
GET /api/product/list
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| categoryId | Long | No | Category ID |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 20 |

**Return**: `PageResult<ProductVO>`

| Field | Type | Description |
|------|------|------|
| id | Long | Product ID |
| name | String | Product name |
| image | String | URL of the product's first image (single image) |
| brand | String | Brand |
| categoryId | Long | Category ID |
| minPrice | Integer | Minimum SKU price (cents) |
| sales | Integer | Sales volume |

---

### 4.2 Product Details

> Public endpoint

```
GET /api/product/{id}
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Product ID (path parameter) |

**Return**: `ProductDetailVO`

| Field | Type | Description |
|------|------|------|
| id | Long | Product ID |
| name | String | Product name |
| description | String | Product description |
| images | List&lt;String&gt; | Product image URL array |
| brand | String | Brand |
| categoryId | Long | Category ID |
| categoryName | String | Category name |
| merchantId | Long | Merchant table primary key, used for admin filtering |
| merchantUserId | Long | Merchant user ID, used when creating a chat conversation |
| shopName | String | Shop name |
| status | Integer | Status: 0-Unlisted, 1-Listed |
| sales | Integer | Sales volume |
| skuList | List&lt;ProductSkuVO&gt; | SKU list |

**SKU item (ProductSkuVO)**:

| Field | Type | Description |
|------|------|------|
| id | Long | SKU ID |
| specIds | String | Specification ID combination, e.g. `"1,3"` |
| specText | String | Specification text, e.g. `"Red / 64GB"` |
| price | Integer | Price (cents) |
| stock | Integer | Stock quantity |

---

### 4.3 Add Product

> Permission: Merchant

```
POST /api/product
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| name | String | Yes | Product name |
| description | String | No | Product description |
| images | List&lt;String&gt; | No | Product image URL array (up to 4 images), e.g. `["https://.../a.jpg"]`. Note: implemented as a JSON array rather than a string |
| brand | String | No | Brand |
| categoryId | Long | Yes | Third-level category ID |
| skuList | List | Yes | SKU list, at least one item; when updating, a non-empty list replaces all existing SKUs |

**SKU item**:

| Field | Type | Required | Description |
|------|------|------|------|
| specIds | String | No | Specification value IDs as a JSON array string, e.g. `"[1,3]"`; generated by the frontend in specification order |
| specText | String | Yes | Specification text, levels separated by `/`, e.g. `"Red/64GB"` |
| price | Integer | Yes | Price (cents), must be greater than 0 |
| stock | Integer | Yes | Stock quantity, must not be negative |

---

### 4.4 Update Product

```
PUT /api/product/{id}
```

Parameters are the same as 4.3.

---

### 4.5 List/Unlist Product

```
PUT /api/product/{id}/status
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| status | Integer | Yes | 1=Listed, 0=Unlisted |

---

### 4.6 Delete Product

> Only the owning merchant can delete; returns 409 when the product is referenced by carts or orders, in which case taking it off shelf is recommended.

```
DELETE /api/product/{id}
```

---

## 5. Cart `/api/cart`

**Controller**: `CartController`  
**Swagger Group**: Cart  
**Permission**: Login required

### 5.1 Get Cart List

```
GET /api/cart/list
```

**Return**:

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
      "selected": 1,
      "skuSpecText": "红色 / 64GB",
      "skuPrice": 599900,
      "skuStock": 99
    }
  ]
}
```

> `specText/price/stock` are the documentation names, while `skuSpecText/skuPrice/skuStock` are the frontend names; the two have identical values; `productImage` takes the product's first image.

---

### 5.2 Add to Cart

```
POST /api/cart/add
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| skuId | Long | Yes | SKU ID |
| quantity | Integer | Yes | Quantity, ≥1 |

> If the same SKU is already in the cart, the quantities are added together

---

### 5.3 Update Quantity

```
PUT /api/cart/update
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| cartItemId | Long | Yes | Cart item ID |
| quantity | Integer | Yes | New quantity |

---

### 5.4 Select/Deselect

```
PUT /api/cart/select
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| cartItemId | Long | No | Cart item ID (documentation name, single operation) |
| selected | Integer \| Boolean | No | 1/0 or true/false |
| cartItemIds | List\<Long\> | No | Cart item ID list (frontend name, batch operation) |

> The two parameter forms are compatible: the `cartItemIds` batch form takes precedence, then the single `cartItemId` form; if neither is provided, the operation applies to all cart items of the current user.

---

### 5.5 Delete Cart Item

```
DELETE /api/cart/{id}
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Cart item ID (path parameter) |

---

### 5.6 Select All/Deselect All

```
PUT /api/cart/select-all
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| selected | Integer | Yes | 1=Select all, 0=Deselect all |

---

### 5.7 Clear Cart (Frontend Extension)

```
DELETE /api/cart/clear
```

> Deletes all cart items of the current user; no parameters.

---

## 6. Order Management `/api/order`

**Controller**: `OrderController`  
**Swagger Group**: Order Management  
**Permission**: Login required

### 6.1 Get Duplicate Submission Prevention Token

> This API must be called before placing an order

```
GET /api/order/submit-token
```

**Return**:

```json
{
  "code": 200,
  "data": { "token": "uuid-string-5min-valid" }
}
```

> The Token is valid for 5 minutes and is deleted immediately after use to prevent duplicate submission

---

### 6.2 Create Order

```
POST /api/order/create
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| token | String | Yes | Duplicate submission prevention Token (from 6.1) |
| addressId | Long | Yes | Shipping Address ID |
| cartItemIds | List\<Long\> | Yes | ID list of the selected cart items |
| remark | String | No | Order remark |
| userCouponId | Long | No | User coupon ID (the user coupon record ID, i.e. the `userCouponId` input of `/api/seckill/calculate`); if omitted, no coupon is used |

**Return**:

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

### 6.3 Order List

```
GET /api/order/list
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| status | Integer | No | Order status (see below); all statuses if omitted |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |

**Order Status Enumeration**:

| Status Code | Meaning |
|--------|------|
| 1 | Pending Payment |
| 2 | Paid (Pending Shipment) |
| 3 | Shipped (Pending Receipt) |
| 4 | Received (Pending Review) / Completed |
| 5 | Cancelled |
| 6 | Refunding |
| 7 | Refunded |

---

### 6.4 Order Details

```
GET /api/order/{id}
```

**Return**: Full order information + order item list + logistics information (if any)

---

### 6.5 Cancel Order

```
PUT /api/order/{id}/cancel
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Order ID (path parameter) |
| reason | String | No | Cancellation reason |

> Only the "Pending Payment" status can be cancelled; stock is restored after cancellation

---

### 6.6 Confirm Receipt

```
PUT /api/order/{id}/receive
```

> Only the "Shipped" status can be confirmed as received

---

### 6.7 Delete Order

```
DELETE /api/order/{id}
```

> Only "Completed" or "Cancelled" orders can be deleted

---

## 7. Payment Management `/api/payment`

**Controller**: `PaymentController` (or in OrderController)  
**Swagger Group**: Payment Management  
**Permission**: Login required

### 7.1 Card Payment

```
POST /api/payment/card
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| orderId | Long | Yes | Order ID |
| cardNo | String | Yes | Card number (16-19 digits) |
| expireDate | String | Yes | Expiry date, format `MM/YY` |
| cvv | String | Yes | Security code, 3-4 digits |

> The frontend performs Luhn algorithm validation first; the backend simulates a successful payment

**Return**:

```json
{
  "code": 200,
  "data": { "status": 2, "paidAt": "2024-10-22T15:32:00" }
}
```

---

### 7.2 Get Payment QR Code

```
POST /api/payment/qrcode
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| orderId | Long | Yes | Order ID |

**Return**:

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

### 7.3 Confirm QR Code Payment

```
POST /api/payment/confirm-qrcode
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| orderId | Long | Yes | Order ID |

---

### 7.4 My Payment Records

> Permission: logged-in user. Returns only payment records for the current user's orders, newest first.

```
GET /api/payment/records
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |

**Return**: `PageResult<PaymentRecord>` with `id`, `orderId`, `orderNo`, `payMethod` (1=card, 2=QR code), `cardNo` (masked), `amount` (cents), `status` (0=processing, 1=success, 2=failed), `paidAt`, `createdAt`.

---

## 8. Shipping Address `/api/address`

**Controller**: `AddressController`  
**Swagger Group**: Shipping Address  
**Permission**: Login required

### 8.1 Address List

```
GET /api/address/list
```

> The default address is listed first, and the rest are sorted by creation time in descending order.

---

### 8.2 Address Details

```
GET /api/address/{id}
```

---

### 8.3 Add Address

```
POST /api/address
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| receiverName | String | Yes | Recipient name |
| phone | String | Yes | Phone number |
| province | String | Yes | Province |
| city | String | Yes | City |
| district | String | Yes | District |
| detail | String | Yes | Detailed address |
| isDefault | Integer | No | 1=Default address |

> Each user can have at most 20 addresses; exceeding the limit returns an error. If no default is explicitly specified, the first address is automatically set as default.

---

### 8.4 Update Address

```
PUT /api/address/{id}
```

Parameters are the same as 8.3.

> Setting an address as default automatically clears the previous default address; all fields are subject to ownership validation.

---

### 8.5 Set Default Address

```
PUT /api/address/{id}/default
```

---

### 8.6 Delete Address

```
DELETE /api/address/{id}
```

---

## 9. Comment Management `/api/comment`

**Controller**: `CommentController`  
**Swagger Group**: Comment Management

> Note: This module is implemented. Comment data model: top-level comment (`parent_id IS NULL` and `is_append=0`, including star rating), follow-up review (`parent_id`=top-level comment ID and `is_append=1`), merchant reply (`parent_id`=top-level comment ID and `is_append=0`). Rules: comments can only be made when the order status = Received (4); each order item allows only one initial review; each top-level comment allows only one follow-up review; merchants can only reply to comments on their own products.

### 9.1 Product Comment List (Public)

```
GET /api/comment/list/{productId}
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| productId | Long | Yes | Product ID (path parameter) |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| star | Integer | No | Filter by star rating (1-5) |

**Return**: `PageResult<CommentVO>`

| Field | Type | Description |
|------|------|------|
| id | Long | Comment ID |
| productId | Long | Product ID |
| userId | Long | User ID |
| username | String | Username |
| nickname | String | Nickname |
| avatar | String | Avatar |
| orderItemId | Long | Order item ID |
| content | String | Comment content |
| images | List&lt;String&gt; | Comment image URL list (up to 4) |
| star | Integer | Star rating (1-5) |
| createdAt | LocalDateTime | Comment time |
| append | CommentVO | Follow-up review (returned only for top-level comments; same structure as this table) |
| replies | List&lt;CommentVO&gt; | Merchant reply list (same structure as this table) |

---

### 9.2 Post Comment

> Permission: Login required; only users who have received the order can comment

```
POST /api/comment
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| productId | Long | Yes | Product ID |
| orderItemId | Long | Yes | Order item ID |
| content | String | Yes | Comment content |
| images | List&lt;String&gt; | No | Comment image URL list, up to 4 (upload via 13.1 first) |
| star | Integer | Yes | Star rating (1-5) |

---

### 9.3 Add Follow-up Review

```
POST /api/comment/append
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| commentId | Long | Yes | Original comment ID |
| content | String | Yes | Follow-up review content |

---

### 9.4 Merchant Reply

> Permission: Merchant (can only reply to comments on their own products)

```
POST /api/comment/reply
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| parentId | Long | Yes | ID of the comment being replied to |
| content | String | Yes | Reply content |

---

## 10. Refund Management `/api/refund`

**Controller**: `RefundController`  
**Swagger Group**: Refund Management  
**Permission**: Login required

> Note: This module is implemented. Rules: a refund can be requested when the order status is Paid (2)/Shipped (3)/Received (4); after the request, the order enters Refunding (6); each order item allows only one refund request; the refund amount must be greater than 0 and must not exceed the item subtotal; approval → order Refunded (7), rejection → order reverts to Received (4). Error codes: 404 refund record/order item does not exist; 403 no permission/only merchants or administrators can review; 400 order status not refundable/invalid amount; 409 refund request already exists/already reviewed.

### 10.1 Request Refund/Return

```
POST /api/refund
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| orderItemId | Long | Yes | Order item ID |
| type | Integer | Yes | 1=Refund only, 2=Refund + Return |
| reason | String | Yes | Refund reason |
| amount | Integer | Yes | Refund amount (cents) |

**Return**: `Result<Long>`, the refund record ID.

---

### 10.2 Refund List (Mine)

```
GET /api/refund/my
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number |
| size | Integer | No | Page size |

**Return**: `PageResult<RefundVO>`.

---

### 10.3 Refund Details

```
GET /api/refund/{id}
```

**Return**: `RefundVO`

| Field | Type | Description |
|------|------|------|
| id | Long | Refund record ID |
| orderId | Long | Order ID |
| orderItemId | Long | Order item ID |
| userId | Long | Applicant ID |
| merchantId | Long | Merchant ID |
| productId | Long | Product ID |
| productName | String | Product name |
| type | Integer | 1=Refund only, 2=Return and refund |
| reason | String | Refund reason |
| amount | Integer | Refund amount (cents) |
| status | Integer | 0=Pending Review, 1=Approved, 2=Rejected, 3=Awaiting Return, 4=Awaiting Merchant Receipt, 5=Return Completed |
| reviewerId | Long | Reviewer ID |
| reviewReason | String | Review comment |
| reviewedAt | LocalDateTime | Review time |
| returnCompany | String | Return courier company |
| returnLogisticsNo | String | Return tracking number |
| returnedAt | LocalDateTime | Time the buyer shipped the return |
| returnReceivedAt | LocalDateTime | Time the merchant confirmed receipt |
| createdAt | LocalDateTime | Request time |

---

### 10.4 Review Refund/Return

> Permission: Merchant or Administrator

```
PUT /api/refund/{id}/review
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Refund record ID (path parameter) |
| approved | Integer | Yes | 1=Approve, 0=Reject |
| reviewReason | String | No | Review comment |

> For refund-only (type=1), approval sets status to 1 and marks the order refunded. For return and refund (type=2), approval sets status to 3 (awaiting return) and the order remains refunding until the buyer submits tracking.

---

### 10.5 Refund List (Merchant)

> Permission: approved merchant (status=1). Returns only refunds for this merchant's products; supports status filtering.

```
GET /api/merchant/refunds
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| status | Integer | No | 0=pending, 1=approved, 2=rejected, 3=awaiting return, 4=awaiting merchant receipt, 5=return completed |

**Return**: `PageResult<RefundVO>`, fields as in 10.3, scoped to the current merchant.

---

### 10.6 Refund List (Admin)

> Permission: administrator. Returns refunds from all merchants; supports status filtering.

```
GET /api/admin/refunds
```

Parameters and response are the same as 10.5, covering refunds from all merchants.

---

### 10.7 Submit Return Tracking (Buyer)

> Permission: refund applicant. Only for return-and-refund (type=2) in status 3 (awaiting return).

```
PUT /api/refund/{id}/return
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Refund record ID (path parameter) |
| returnCompany | String | Yes | Return courier company, max 50 chars |
| returnLogisticsNo | String | Yes | Return tracking number, max 50 chars |

> After submission the status becomes 4 (awaiting merchant receipt) and `returnedAt` is recorded.

---

### 10.8 Confirm Return Receipt (Merchant/Admin)

> Permission: merchant or administrator. Only for return-and-refund in status 4.

```
PUT /api/refund/{id}/return-received
```

> The status becomes 5 (return completed), the order is marked refunded, and `returnReceivedAt` is recorded.

---

## 11. Merchant Management `/api/merchant`

**Controller**: `MerchantController`  
**Swagger Group**: Merchant Management  
**Permission**: Login required (some APIs require the merchant role)

> Note: This module is implemented. All 6 APIs are available. The shipping API (§11.5) completes the real order loop: it validates that all products in the order belong to the merchant → the order status transitions from Paid (2) to Shipped (3).

### 11.1 Merchant Onboarding Application

```
POST /api/merchant/apply
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| shopName | String | Yes | Shop name |
| businessLicense | String | Yes | Business license image URL |
| contactPhone | String | Yes | Contact phone |

---

### 11.2 Get Merchant Info

```
GET /api/merchant/info
```

---

### 11.3 Merchant Product List

```
GET /api/merchant/products
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number |
| size | Integer | No | Page size |
| status | Integer | No | Status filter |

**Return**: `PageResult`; list item fields: `id, name, description, images (image JSON array string), brand, categoryId, merchantId, status, sales, createdAt, updatedAt`; `minPrice` and `merchantName` are not returned.

---

### 11.4 Merchant Order List

```
GET /api/merchant/orders
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| status | Integer | No | Order status filter |
| page | Integer | No | Page number |
| size | Integer | No | Page size |

**Return**: `PageResult<MerchantOrderVO>`; list items contain `id, orderNo, userId, receiverName, receiverPhone, receiverAddress, totalAmount, status, paidAt, shippedAt, createdAt, itemCount, items[]`; `items` contains `productId, productName, productImage, quantity, unitPrice, subtotal`.

---

### 11.5 Ship

```
PUT /api/merchant/ship/{orderId}
```

---

### 11.6 Merchant Sales Statistics

```
GET /api/merchant/stats
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| startDate | String | No | Start date `yyyy-MM-dd` |
| endDate | String | No | End date `yyyy-MM-dd` |

**Return**:

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

## 12. Admin Backend `/api/admin`

**Controller**: `AdminController`  
**Swagger Group**: Admin Backend  
**Permission**: Administrator only

> Note: This module is implemented. All 10 APIs are available, and the administrator role (role=3) is validated uniformly at the Service layer. When a merchant review is approved, the applicant's role is automatically upgraded to merchant; category deletion has protection rules (see §12.3).

### 12.1 User Management

#### User List

```
GET /api/admin/users
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| keyword | String | No | Search by username/phone number |
| role | Integer | No | Filter by role |
| status | Integer | No | Filter by status |
| page | Integer | No | Page number |
| size | Integer | No | Page size |

#### Enable/Disable User

```
PUT /api/admin/users/{id}/status
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| status | Integer | Yes | 1=Enable, 0=Disable |

---

### 12.2 Merchant Review

#### Merchant Application List

```
GET /api/admin/merchants
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| status | Integer | No | 0=Pending Review, 1=Approved, 2=Rejected |

**Return**: `PageResult<AdminMerchantVO>`; list items contain `id, userId, applicantUsername, shopName, contactPhone, status, reviewReason, reviewedAt, createdAt`.

#### Review Merchant

```
PUT /api/admin/merchants/{id}/review
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| approved | Integer | Yes | 1=Approve, 0=Reject |
| reviewReason | String | No | Review comment |

---

### 12.3 Category Management

#### Add Category

```
POST /api/admin/categories
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| name | String | Yes | Category name |
| parentId | Long | No | Parent category ID (omit it or pass 0 for first-level categories) |
| level | Integer | Yes | Level: 1/2/3 |
| sortOrder | Integer | No | Sort value |
| icon | String | No | Icon |

#### Update Category

```
PUT /api/admin/categories/{id}
```

> Only the name/sort/icon can be modified; parentId and level cannot be changed (to prevent breaking the tree structure).

#### Delete Category

```
DELETE /api/admin/categories/{id}
```

> Deletion protection rules: deletion is rejected if the category has subcategories (4053); deletion is rejected if products reference the category (4054). Subcategories and product attachments must be cleared before deletion.

---

### 12.4 Platform-wide Product Management

```
GET /api/admin/products
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| keyword | String | No | Search |
| categoryId | Long | No | Filter by category |
| merchantId | Long | No | Filter by merchant |
| status | Integer | No | 0=Unlisted, 1=Listed |
| page | Integer | No | Page number |
| size | Integer | No | Page size |

**Return**: `PageResult`; list item fields: `id, name, description, images (image JSON array string), brand, categoryId, merchantId, status, sales, createdAt, updatedAt`; `minPrice` and `merchantName` are not returned.

---

### 12.5 Platform-wide Order Management

```
GET /api/admin/orders
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| orderNo | String | No | Order number search |
| status | Integer | No | Status filter |
| page | Integer | No | Page number |
| size | Integer | No | Page size |

---

### 12.6 Global Sales Report

```
GET /api/admin/report
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| startDate | String | No | Start date |
| endDate | String | No | End date |
| groupBy | String | No | `day` / `week` / `month` |

**Return**:

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
    "orderTrend": [
      { "date": "2024-10-01", "createdCount": 120, "paidCount": 100, "completedCount": 80 }
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

### 12.7 Admin Product Status

> Permission: administrator. Merchant ownership is not checked; the Elasticsearch index is synchronized after the change.

```
PUT /api/admin/products/{id}/status
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Product ID (path parameter) |
| status | Integer | Yes | 0=off shelf, 1=on shelf |

---

### 12.8 Admin Delete Product

> Permission: administrator. Deletion is rejected when the product is referenced by carts or orders; the search index is removed on success.

```
DELETE /api/admin/products/{id}
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| id | Long | Yes | Product ID (path parameter) |

---

### 12.9 Admin Update Product

> Permission: administrator. Any merchant's product can be updated; the search index is synchronized after saving.

```
PUT /api/admin/products/{id}
```

Parameters are the same as 4.3 (`name`, `description`, `images`, `brand`, `categoryId`, `skuList`); a non-empty SKU list replaces all SKUs.

---

## 13. File Upload `/api/upload`

**Controller**: `UploadController`  
**Swagger Group**: File Upload  
**Permission**: Login required

> Note: This module is implemented. Validation order: non-empty → size (≤2MB) → extension → real image content validation via ImageIO; if width > 800px, scale proportionally; if ≤800px, upload as-is. Development defaults to local storage under `uploads/` (`upload.mode=local`, served via `/uploads/**`); set `UPLOAD_MODE=oss` with OSS keys to switch to object storage.
> Special note: the JDK natively does not support decoding the webp format, so the RIFF/WEBP magic header is used to validate file authenticity; after passing, the file is uploaded as-is (no scaling). Files exceeding the multipart limit are returned as 4062 by the global exception handler.

### 13.1 Upload Image

```
POST /api/upload/image
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| file | MultipartFile | Yes | Image file (form-data) |

**Validation Rules**:

| Rule | Value |
|------|-----|
| Allowed formats | `jpg`, `jpeg`, `png`, `webp` |
| Max per image | 2MB |
| Max per request | 4 images |
| Compression width | 800px (proportional scaling) |

**Return**:

```json
{
  "code": 200,
  "data": { "url": "https://oss.example.com/product/20241022/uuid.jpg" }
}
```

---

## 14. Instant Messaging `/api/chat`

**Controller**: `ChatController`  
**Swagger Group**: Instant Messaging  
**Permission**: Login required

> Note: This module is implemented. Conversation participants are the buyer user ID (`userId`) and the merchant user ID (`merchantId`), with one conversation per user-merchant pair (unique index prevents duplicates). Fetching history marks messages from the peer as read and clears the current user's unread count; every endpoint verifies that the current user belongs to the conversation.

### 14.1 Conversation List

```
GET /api/chat/conversations
```

**Return**:

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

### 14.2 Message History

```
GET /api/chat/history/{conversationId}
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| conversationId | Long | Yes | Conversation ID (path parameter) |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 20 |

**Return**: Message list (newest to oldest); each message contains sender information, content, type, time, and read status

---

### 14.3 Create/Get Conversation

```
POST /api/chat/conversation
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| merchantId | Long | Yes | Merchant user ID (the `merchantUserId` returned by product detail) |
| productId | Long | No | Product ID (associated product) |

> If a conversation between the same user and merchant already exists, return it directly; otherwise create a new one

---

### 14.4 Mark as Read

```
PUT /api/chat/read/{conversationId}
```

---

### 14.5 Total Unread Messages

```
GET /api/chat/unread-count
```

**Return**:

```json
{
  "code": 200,
  "data": { "count": 5 }
}
```

---

### 14.6 Send Message (dev:mock only)

> The backend sends messages through WebSocket (see section 17). The chat page connects directly to WebSocket in `npm run dev` and production; the HTTP endpoint is used only in `npm run dev:mock` demo mode.

```
POST /api/chat/message
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| conversationId | Long | Yes | Conversation ID |
| content | String | Yes | Message content |
| type | String | No | text/image, default text |

**Return**: `Result<ChatMessage>`, fields as in 14.2.

---

## 15. Product Search `/api/search`

**Controller**: `SearchController`  
**Swagger Group**: Product Search  
**Permission**: Public

> Implementation status: The backend search module is not yet implemented (`SearchController` is empty). During development Mock provides the full search APIs; the frontend search page calls these APIs and automatically falls back to local filtering based on `/api/product/list` when they are unavailable, so guests can use it normally.

### 15.1 Full-text Search

```
GET /api/search
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| keyword | String | Yes | Search keyword |
| categoryId | Long | No | Category filter |
| brand | String | No | Brand filter |
| minPrice | Integer | No | Minimum price (cents) |
| maxPrice | Integer | No | Maximum price (cents) |
| sort | String | No | `sales_desc` / `price_asc` / `price_desc` / `time_desc` |
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 20 |

> The frontend always goes through the backend proxy path `/api/search`, and the backend `SearchController` internally uses Elasticsearch for retrieval.

**Return**:

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

### 15.2 Search Suggestions (Autocomplete)

```
GET /api/search/suggest?keyword=iph
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| keyword | String | Yes | Prefix keyword |

**Return**:

```json
{
  "code": 200,
  "data": ["iPhone 15", "iPhone 15 Pro", "iPhone 14", "iPhone充电器"]
}
```

---

### 15.3 Search History

```
GET /api/search/history
```

> Logged-in users only; returns the most recent 20 entries

---

### 15.4 Clear Search History

```
DELETE /api/search/history
```

---

### 15.5 Trending Searches

```
GET /api/search/hot
```

> Public endpoint, based on search frequency over the past 7 days

---

## 16. Flash-sale Coupons `/api/seckill`

**Controller**: `SeckillController`  
**Swagger Group**: Flash-sale Coupons  
**Permission**: List/details public (`@Public` annotation); claiming/my coupons/calculation require login; create/edit/delete only for merchants or administrators

> Note: This module is implemented; the API paths and fields are fully consistent with the frontend Mock.

### 16.1 Flash-sale Coupon List

```
GET /api/seckill/list
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| status | Integer | No | Coupon status: 0=Not Started, 1=Ongoing, 2=Ended |

**Return**:

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "list": [
      {
        "id": 1,
        "name": "新人满减券",
        "description": "新用户专享，满50减10元",
        "discountType": 1,
        "discountValue": 1000,
        "minAmount": 5000,
        "totalCount": 1000,
        "remainCount": 987,
        "startTime": "2024-01-01 00:00:00",
        "endTime": "2026-12-31 23:59:59",
        "perUserLimit": 1,
        "status": 1
      }
    ]
  }
}
```

---

### 16.2 Flash-sale Coupon Details

```
GET /api/seckill/detail/{id}
```

**Return**: A single `SeckillCoupon` object (same structure as the list item in 16.1).

---

### 16.3 Claim Flash-sale Coupon

```
POST /api/seckill/claim/{couponId}
```

> Logged-in user. Validation: the coupon is ongoing, not past its validity period, remaining quantity > 0, and the per-user claim limit has not been exceeded.

**Return**:

```json
{
  "code": 200,
  "data": {
    "userCouponId": 6,
    "couponName": "新人满减券",
    "discountType": 1,
    "discountValue": 1000,
    "minAmount": 5000,
    "endTime": "2026-12-31 23:59:59"
  }
}
```

---

### 16.4 My Coupons

```
GET /api/seckill/my-coupons
```

| Parameter | Type | Required | Description |
|------|------|------|------|
| page | Integer | No | Page number, default 1 |
| size | Integer | No | Page size, default 10 |
| status | Integer | No | User coupon status: 0=Unused, 1=Used, 2=Expired |

**Return**: `PageResult<UserCoupon>`; see 16.9 for list item fields.

---

### 16.5 Create Flash-sale Coupon (Merchant/Administrator)

```
POST /api/seckill/create
```

Request body:

```json
{
  "name": "周末满减券",
  "description": "周末特惠，满100减20",
  "discountType": 1,
  "discountValue": 2000,
  "minAmount": 10000,
  "totalCount": 300,
  "startTime": "2026-11-01 00:00:00",
  "endTime": "2026-11-30 23:59:59",
  "perUserLimit": 2
}
```

**Return**: `{ "code": 200, "data": { "id": 6 } }`

---

### 16.6 Update Flash-sale Coupon (Merchant/Administrator)

```
PUT /api/seckill/update/{id}
```

Request body is the same as 16.5. If `totalCount` increases, the remaining quantity increases accordingly.

---

### 16.7 Delete Flash-sale Coupon (Merchant/Administrator)

```
DELETE /api/seckill/{id}
```

> Only coupons in the Not Started (status=0) state can be deleted.

---

### 16.8 Order Discount Calculation

```
POST /api/seckill/calculate
```

Request body:

```json
{
  "userCouponId": 6,
  "totalAmount": 15000
}
```

**Return**:

```json
{
  "code": 200,
  "data": {
    "originalAmount": 15000,
    "discountAmount": 1000,
    "finalAmount": 14000,
    "couponName": "新人满减券"
  }
}
```

---

### 16.9 Data Structures

#### SeckillCoupon

| Field | Type | Description |
|------|------|------|
| id | Long | Coupon ID |
| name | String | Coupon name |
| description | String | Description |
| discountType | Integer | Coupon type: 1=Amount off, 2=Discount |
| discountValue | Integer | For amount off, the discount amount (cents); for discount, the percentage (85 = 15% off) |
| minAmount | Integer | Minimum spending threshold (cents) |
| totalCount | Integer | Total issuance |
| remainCount | Integer | Remaining quantity |
| startTime | String | Start time |
| endTime | String | End time |
| perUserLimit | Integer | Per-user claim limit |
| status | Integer | 0=Not Started, 1=Ongoing, 2=Ended |

#### UserCoupon

| Field | Type | Description |
|------|------|------|
| id | Long | User coupon ID |
| couponId | Long | Coupon ID |
| couponName | String | Coupon name |
| discountType | Integer | Coupon type |
| discountValue | Integer | Discount value |
| minAmount | Integer | Minimum threshold (cents) |
| status | Integer | 0=Unused, 1=Used, 2=Expired |
| endTime | String | Validity deadline |
| claimTime | String | Claim time |

---

### 16.10 Error Codes

The flash-sale coupon module reuses HTTP status codes as `code` and distinguishes specific errors via `message`:

| Error Code | Meaning |
|--------|------|
| 404 | Flash-sale coupon does not exist / Coupon does not exist |
| 409 | Flash-sale coupon is sold out / Already claimed this flash-sale coupon / Coupon has expired or been used |
| 400 | Activity has not started or has ended / Minimum spending threshold not met / Only Not Started coupons can be deleted / Invalid time format / End time must be later than start time / Invalid discount type / Invalid discount value / Invalid status parameter |
| 403 | Only merchants or administrators can manage flash-sale coupons |

---

## 17. WebSocket Communication

**Endpoint**: `ws://localhost:8080/chat/{userId}?token=<jwt_token>`  
**Protocol**: JSR 356 native WebSocket (backend `@OnOpen` / `@OnMessage` / `@OnClose`)

### 17.1 Connection

```typescript
const ws = new WebSocket(`ws://localhost:8080/chat/${userId}?token=${jwtToken}`)
```

### 17.2 Send Message (JSON Format)

```json
{
  "type": "TEXT",
  "conversationId": 123,
  "content": "你好，这个商品有货吗？",
  "productId": 456,
  "timestamp": 1700000000000
}
```

| Field | Type | Description |
|------|------|------|
| type | String | `TEXT` (text) |
| conversationId | Long | Conversation ID |
| content | String | Message content |
| productId | Long | Associated product ID (optional) |
| timestamp | Long | Client timestamp (milliseconds) |

### 17.3 Receive Message

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

### 17.4 Connection Lifecycle

| Event | `@OnOpen` | Validate JWT Token; record `online:{userId}` in Redis |
|------|-----------|------|
| | | Push unread messages accumulated while offline |
| Event | `@OnClose` | Delete `online:{userId}` |
| Event | `@OnMessage` | Validate permission → store in MySQL → push to the receiver |
| Event | `@OnError` | Log the error → close the connection |

---

## 18. Error Code Reference

| Error Code | Meaning |
|--------|------|
| 200 | Operation successful |
| 400 | Parameter error / business validation failed (see the module messages below) |
| 401 | Not logged in or Token expired |
| 403 | No permission to access |
| 404 | Resource does not exist |
| 409 | Resource state conflict |
| 500 | Internal system error |

**Business error codes by module** (defined in the corresponding ErrorCode enums in the common module; all are HTTP semantic codes):

| Module | Enum | Code and Message |
|------|------|-----------|
| User Authentication | AuthErrorCode | 401 Not logged in or login expired / token expired / invalid token; 400 Invalid captcha; 409 Username already exists / Phone number already registered / Username does not exist / Incorrect password; 403 No permission / Account has been disabled; 404 Account does not exist |
| Category Management | CategoryErrorCode | 404 Category does not exist |
| Product Management | ProductErrorCode | 404 Product does not exist; 403 Only merchants can perform this operation / Merchant onboarding review not approved / No permission to operate on another merchant's products; 400 SKU list cannot be empty / SKU price must be greater than 0 / SKU stock cannot be negative / Invalid product status value; 409 Product is referenced by transactions and cannot be deleted |
| Flash-sale Coupons | SeckillErrorCode | 404 Flash-sale coupon does not exist / Coupon does not exist; 409 Flash-sale coupon is sold out / Already claimed this flash-sale coupon / Coupon has expired or been used; 400 Activity has not started or has ended / Minimum spending threshold not met / Only Not Started flash-sale coupons can be deleted / Invalid time or discount parameter; 403 Only merchants or administrators can manage flash-sale coupons |
| Cart | CartErrorCode | 404 SKU does not exist / Cart item does not exist; 400 Product is unlisted and cannot be added to the cart / Insufficient stock / Invalid selected value; 403 No permission to operate on another user's cart |
| Shipping Address | AddressErrorCode | 404 Shipping Address does not exist; 403 No permission to operate on another user's address; 400 Address limit reached (maximum 20) |
| Merchant Management | MerchantErrorCode | 4041 Merchant does not exist; 4042 Merchant status does not allow this operation; 4043 You have already submitted an onboarding application; 4044 You are not a merchant and have no permission; 4045 This order does not belong to your shop; 4046 Order status does not allow shipping; 4047 Merchant has no products yet |
| Admin Backend | AdminErrorCode | 4051 Only administrators can perform this operation; 4052 Cannot disable an administrator account; 4053 This category has subcategories and cannot be deleted; 4054 Products exist under this category and it cannot be deleted; 4055 Parent category does not exist or level is invalid; 4056 This merchant has already been reviewed; do not repeat the operation |
| File Upload | UploadErrorCode | 4061 Uploaded file cannot be empty; 4062 File size cannot exceed 2MB; 4063 Only jpg/jpeg/png/webp formats are supported; 4064 File upload failed |

---

## 19. API Permission Overview

| Module | Path Prefix | Guest | Registered User | Merchant | Administrator |
|------|----------|------|----------|------|--------|
| User Authentication | `/api/user` | Register/Login/Captcha | Info/Change Password/Logout | ← | ← |
| Category Management | `/api/category` | ✅ View | ✅ | ✅ | ✅ |
| Product Management | `/api/product` | ✅ View | ✅ View | ✅ Manage own | ✅ Manage all |
| Cart | `/api/cart` | ❌ | ✅ | ✅ | — |
| Order Management | `/api/order` | ❌ | ✅ Own | ✅ Own products' | ✅ All |
| Payment Management | `/api/payment` | ❌ | ✅ | ✅ | — |
| Shipping Address | `/api/address` | ❌ | ✅ | ✅ | — |
| Comment Management | `/api/comment` | ✅ View | ✅ Post/Follow-up | ✅ Reply | ✅ Manage |
| Refund Management | `/api/refund` | ❌ | ✅ Request | ✅ Review | ✅ Review |
| Merchant Management | `/api/merchant` | ❌ | ✅ Onboarding | ✅ Products/Orders/Statistics | — |
| Admin Backend | `/api/admin` | ❌ | ❌ | ❌ | ✅ All |
| File Upload | `/api/upload` | ❌ | ✅ | ✅ | ✅ |
| Instant Messaging | `/api/chat` | ❌ | ✅ | ✅ | — |
| Product Search | `/api/search` | ✅ | ✅ | ✅ | ✅ |
| Flash-sale Coupons | `/api/seckill` | ✅ View | ✅ Claim/My Coupons/Calculate | ✅ Create/Edit/Delete | ✅ Create/Edit/Delete |
| WebSocket | `ws://.../chat/` | ❌ | ✅ | ✅ | — |

> ✅ = accessible, ❌ = inaccessible, ← = same as left

---

> **Document update date**: 2026-11-10
>
> **Corresponding version**: v1.0.0

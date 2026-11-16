# Detailed Design

> [简体中文](../zh/detailed-design.md) | English

> This system uses a **monolithic service architecture**. The Maven multi-module setup is only used for code layering and organization; at runtime it is a single Spring Boot process. User information is passed through the request chain via `UserContext` (`ThreadLocal`).

---

## I. User Authentication Module

### 1.1 Get Graphic Captcha — `GET /api/user/captcha`

```mermaid
flowchart TD
    A["Frontend requests GET /api/user/captcha"] --> B["Generate unique identifier UUID"]
    B --> C["Generate line-interference captcha<br/>130x40 pixels, 4 characters"]
    C --> D["Extract captcha text + convert to Base64 image"]
    D --> E["Redis storage: captcha:uuid = captcha<br/>Expiration 2 minutes"]
    E --> F["Return { uuid, image(base64) }"]
    F --> G["Frontend displays captcha image<br/>Temporarily stores uuid in form"]
```

### 1.2 User Registration — `POST /api/user/register`

```mermaid
flowchart TD
    A["User submits registration information<br/>{ username, password, phone, email, role, captcha UUID, captcha input }"] --> B["@Valid parameter validation"]
    B -->|Failed| C["Throw MethodArgumentNotValidException<br/>Global exception handler returns 400"]
    B -->|Passed| D["Redis query: captcha:{uuid}"]
    D --> E{"Captcha exists and matches ignoring case?"}
    E -->|Expired/mismatch| F["Return error: Invalid captcha"]
    E -->|Correct| G["Redis delete: captcha:{uuid}"]
    G --> H["Query database: whether username already exists"]
    H --> I{"Username already exists?"}
    I -->|Yes| J["Return error: Username already exists"]
    I -->|No| K["Query database: whether phone is registered"]
    K --> L{"Phone already exists?"}
    L -->|Yes| M["Return error: Phone already registered"]
    L -->|No| N["BCrypt encrypt password"]
    N --> O["Build User entity<br/>username, password(encrypted), phone, email, role<br/>nickname=username, status=1 (normal)"]
    O --> P["userMapper.insert<br/>@Options useGeneratedKeys backfills id"]
    P --> Q["JWT generate Token<br/>payload: {userId, username, role}<br/>valid for 2 hours"]
    Q --> R["Return LoginVO { token, userInfo }"]
```

### 1.3 User Login — `POST /api/user/login`

```mermaid
flowchart TD
    A["User submits login information<br/>{ username, password, captcha UUID, captcha input }"] --> B["@Valid parameter validation"]
    B -->|Failed| C["Throw MethodArgumentNotValidException<br/>Global exception handler returns 400"]
    B -->|Passed| D["Redis query: captcha:{uuid}"]
    D --> E{"Captcha exists and matches ignoring case?"}
    E -->|Expired/mismatch| F["Return error: Invalid captcha"]
    E -->|Correct| G["Redis delete: captcha:{uuid}"]
    G --> H["Query user table: look up by username"]
    H --> I{"User exists?"}
    I -->|No| J["Return error: User does not exist"]
    I -->|Yes| K{"Account status normal status == 1"}
    K -->|No| L["Return error: Account has been disabled"]
    K -->|Yes| M["BCrypt verify password"]
    M --> N{"Password matches?"}
    N -->|No| O["Return error: Incorrect password"]
    N -->|Yes| P["JWT generate Token<br/>payload: {userId, username, role}<br/>valid for 2 hours"]
    P --> Q["Return LoginVO { token, userInfo }"]
```

### 1.4 Token Refresh and Expiration Handling (No Separate Refresh API)

> The backend does not provide a separate token refresh endpoint. Login state is validated by `JwtInterceptor`: when the JWT has expired or does not match the server-side `token:{userId}`, it directly returns `TOKEN_EXPIRED` / `TOKEN_INVALID`, and the frontend clears the local token and redirects to the login page. The token is valid for 2 hours, with the same lifetime as the Redis cache.

### 1.5 User Logout — `POST /api/user/logout`

```mermaid
flowchart TD
    A["User clicks log out"] --> B["POST /api/user/logout<br/>Header: Authorization Bearer token"]
    B --> C["Parse JWT and obtain userId"]
    C --> D["Redis delete: token:{userId}"]
    D --> E["Return success"]
    E --> F["Frontend clears localStorage token"]
    F --> G["Frontend clears global state store"]
    G --> H["Route redirects to login page"]
```

### 1.6 Forgot Password — Send Verification Code `POST /api/user/forgot-code`

```mermaid
flowchart TD
    A["User submits<br/>{ account (phone/email), graphic captcha UUID, graphic captcha }"] --> B["Redis query: captcha:{uuid}"]
    B --> C{"Graphic captcha correct?"}
    C -->|No| D["Return error: Invalid captcha"]
    C -->|Yes| E["Redis delete: captcha:{uuid}"]
    E --> F["Query user table: look up by phone or email"]
    F --> G{"Account exists?"}
    G -->|No| H["Return error: Account does not exist"]
    G -->|Yes| I["Generate 6-digit numeric verification code + UUID"]
    I --> J["Redis store: reset:code:{uuid} = verification code, 5 minutes"]
    J --> K["Simulate sending SMS/email (log it)"]
    K --> L["Return { uuid }"]
    L --> M["Frontend redirects to reset password page with uuid"]
```

### 1.7 Reset Password — `POST /api/user/reset-password`

```mermaid
flowchart TD
    A["User submits<br/>{ uuid, SMS verification code, new password }"] --> B["Redis query: reset:code:{uuid}"]
    B --> C{"Verification code correct?"}
    C -->|No/expired| D["Return error: Invalid verification code"]
    C -->|Yes| E["Redis delete: reset:code:{uuid}"]
    E --> F["Query user table to locate the user"]
    F --> G["BCrypt encrypt new password"]
    G --> H["Update user password"]
    H --> I["Redis delete: token:{userId}<br/>Force re-login"]
    I --> J["Return success"]
```

### 1.8 Change Password — `PUT /api/user/password`

```mermaid
flowchart TD
    A["User submits<br/>{ old password, new password }<br/>Header: Authorization Bearer token"] --> B["Parse JWT and obtain userId"]
    B --> C["Query user table"]
    C --> D["BCrypt verify old password"]
    D --> E{"Old password matches?"}
    E -->|No| F["Return error: Incorrect original password"]
    E -->|Yes| G["BCrypt encrypt new password"]
    G --> H["Update password"]
    H --> I["Redis delete: token:{userId}"]
    I --> J["Return success → frontend redirects to login page"]
```

### 1.9 Get Current User Information — `GET /api/user/info`

```mermaid
flowchart TD
    A["GET /api/user/info<br/>Header: Authorization Bearer token<br/>No request parameters required"] --> B["JwtInterceptor intercepts the request"]
    B --> C["Extract Header → parse JWT<br/>Obtain userId, username, role"]
    C --> D["Write to UserContext (ThreadLocal)"]
    D --> E["Controller calls UserContext.getUserId()"]
    E --> F["Query user table"]
    F --> G{"User exists?"}
    G -->|No| H["Return error: User does not exist"]
    G -->|Yes| I["Assemble user information (excluding password)"]
    I --> J["Return user information"]
    J --> K["JwtInterceptor.afterCompletion<br/>UserContext.clear() cleans up ThreadLocal"]
```

### 1.10 Update Personal Information — `PUT /api/user/profile`

```mermaid
flowchart TD
    A["User submits<br/>{ nickname, phone, email, avatar }<br/>Header: Authorization Bearer token"] --> B["JwtInterceptor intercepts → parses JWT<br/>Writes to UserContext (ThreadLocal)"]
    B --> C["Controller calls UserContext.getUserId()"]
    C --> D["Validate phone/email format"]
    D -->|Failed| E["Return 400"]
    D -->|Passed| F["Update user table"]
    F --> G["Return updated user information"]
```

---

## II. Product Module

### 2.1 Product List Query — `GET /api/product/list`

```mermaid
flowchart TD
    A["GET /api/product/list<br/>?page=&size=&categoryId=&sort=&keyword="] --> B{"Is keyword empty?"}
    B -->|Has keyword| C["Use ES search → return product ID list"]
    C --> D["Query MySQL by ID list to complete data"]
    B -->|No keyword| E["MySQL paginated query<br/>by category + sort conditions"]
    D --> F["Assemble paginated result"]
    E --> F
    F --> G["Return paginated result"]
```

### 2.2 Product Details — `GET /api/product/{id}`

```mermaid
flowchart TD
    A["GET /api/product/{productId}"] --> B["Query product table"]
    B --> C{"Product exists and status == Listed?"}
    C -->|No| D["Return error: Product does not exist or is unlisted"]
    C -->|Yes| E["Query SKU table: all SKUs of the product"]
    E --> F["Query merchant info (shop name / merchant user ID)"]
    F --> G["Assemble product details<br/>{ basic info, SKU list, merchant info }"]
    G --> H["Return product details (comments are fetched via the comment API)"]
```

### 2.3 Create Product — `POST /api/product`

```mermaid
flowchart TD
    A["Merchant submits<br/>{ product name, description, images[], category ID, brand, SKU list[] }"] --> B["Parse JWT → obtain userId → verify merchant identity"]
    B --> C["Query merchant table: verify the merchant has been approved"]
    C --> D{"Merchant approved?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F["Insert into product table"]
    F --> G["Iterate SKU list → batch insert into SKU table"]
    G --> H["Sync product to ES index"]
    H --> I["Return success"]
```

### 2.4 Update Product — `PUT /api/product/{id}`

```mermaid
flowchart TD
    A["Merchant submits update data"] --> B["Parse JWT → obtain userId → verify merchant identity"]
    B --> C["Query product table"]
    C --> D{"Does the product belong to the current merchant?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F["Update product table"]
    F --> G["Delete old SKUs → batch insert new SKUs"]
    G --> H["Update ES index"]
    H --> I["Return success"]
```

### 2.5 List/Unlist Product — `PUT /api/product/{id}/status`

```mermaid
flowchart TD
    A["PUT /api/product/{id}/status<br/>{ status: 0-unlisted, 1-listed }"] --> B["Parse JWT → verify the product's owning merchant"]
    B --> C["Query product table"]
    C --> D{"Permission to operate?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F{"status == 1 (listed)?"}
    F -->|Yes| G["Update product status to: Listed"]
    G --> H["Sync product to ES"]
    F -->|No| I["Update product status to: Unlisted"]
    I --> J["Delete the product index from ES"]
    H --> K["Return success"]
    J --> K
```

### 2.6 Delete Product — `DELETE /api/product/{id}`

```mermaid
flowchart TD
    A["DELETE /api/product/{id}"] --> B["Parse JWT → verify the product's owning merchant"]
    B --> C["Query product table"]
    C --> D{"Permission to operate?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F{"Product referenced by a cart/order?"}
    F -->|Yes| G["Return error: Product is referenced and cannot be deleted"]
    F -->|No| H["Delete product"]
    H --> I["Delete the product index from ES"]
    I --> J["Return success"]
```

---

## III. Category Module

### 3.1 Get Category Tree — `GET /api/category/tree`

```mermaid
flowchart TD
    A["GET /api/category/tree"] --> B["Redis query: category:tree"]
    B --> C{"Redis hit?"}
    C -->|Hit| D["Return cached JSON tree directly"]
    C -->|Miss| E["Query all categories"]
    E --> F["Recursively build the tree structure in memory<br/>group by parentId"]
    F --> G["Redis store: category:tree, 30 minutes"]
    G --> D
```

### 3.2 Add Category — `POST /api/admin/categories`

```mermaid
flowchart TD
    A["Administrator submits<br/>{ category name, parent ID, sort order, icon }"] --> B["Parse JWT → verify administrator identity"]
    B --> C{"Parent ID == 0?"}
    C -->|Yes| D["Level = 1 (first-level category)"]
    C -->|No| E["Query parent category"]
    E --> F{"Parent category exists and level <= 2?"}
    F -->|No| G["Return error: Invalid parent category"]
    F -->|Yes| H["Level = parent category level + 1"]
    D --> I["Insert into category table"]
    H --> I
    I --> J["Redis delete: category:tree<br/>Clear category tree cache"]
    J --> K["Return success"]
```

### 3.3 Edit Category — `PUT /api/admin/categories/{id}`

```mermaid
flowchart TD
    A["Administrator submits<br/>{ category name, sort order, icon }"] --> B["Parse JWT → verify administrator identity"]
    B --> C["Query category"]
    C --> D{"Category exists?"}
    D -->|No| E["Return error: Category does not exist"]
    D -->|Yes| F["Update category table"]
    F --> G["Redis delete: category:tree"]
    G --> H["Return success"]
```

### 3.4 Delete Category — `DELETE /api/admin/categories/{id}`

```mermaid
flowchart TD
    A["DELETE /api/admin/categories/{id}"] --> B["Parse JWT → verify administrator identity"]
    B --> C["Count the number of products under the category"]
    C --> D{"Are there products under the category?"}
    D -->|Yes| E["Return error: Category is not empty and cannot be deleted"]
    D -->|No| F["Query subcategory list"]
    F --> G["Delete the category"]
    G --> H["Recursively delete all subcategories"]
    H --> I["Redis delete: category:tree"]
    I --> J["Return success"]
```

---

## IV. Cart Module

### 4.1 Cart List — `GET /api/cart/list`

```mermaid
flowchart TD
    A["GET /api/cart/list<br/>Header: Authorization Bearer token"] --> B["Parse JWT and obtain userId"]
    B --> C["Query cart table: all items of the current user"]
    C --> D["Iterate: join query product table + SKU table<br/>complete name/image/specification/unit price"]
    D --> E["Assemble cart list<br/>{ product name, image, SKU specification, unit price, quantity, subtotal, selected status }"]
    E --> F["Return cart list + total amount"]
```

### 4.2 Add to Cart — `POST /api/cart/add`

```mermaid
flowchart TD
    A["User submits<br/>{ SKU_ID, quantity }<br/>Header: Authorization Bearer token"] --> B["Parse JWT and obtain userId"]
    B --> C["Query SKU table"]
    C --> D{"SKU exists and stock >= quantity?"}
    D -->|No| E["Return error: Insufficient stock"]
    D -->|Yes| F["Query product table"]
    F --> G{"Product status == Listed?"}
    G -->|No| H["Return error: Product has been unlisted"]
    G -->|Yes| I["Query cart table: whether the SKU is already present"]
    I --> J{"Product already in cart?"}
    J -->|Yes| K["Update: quantity = original quantity + new quantity"]
    J -->|No| L["Insert cart item<br/>(userId, productId, skuId, quantity, selected=1)"]
    K --> M["Return success"]
    L --> M
```

### 4.3 Update Quantity — `PUT /api/cart/update`

```mermaid
flowchart TD
    A["User submits<br/>{ cart item ID, quantity }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query cart item"]
    C --> D{"Belongs to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Quantity > 0?"}
    F -->|Yes| G["Query SKU stock"]
    G --> H{"Stock >= quantity?"}
    H -->|No| I["Return error: Insufficient stock"]
    H -->|Yes| J["Update cart quantity"]
    F -->|Quantity == 0| K["Delete the cart item"]
    J --> L["Return success"]
    K --> L
```

### 4.4 Select/Deselect — `PUT /api/cart/select`

```mermaid
flowchart TD
    A["User submits<br/>{ selected cart item IDs[] }"] --> B["Parse JWT and obtain userId"]
    B --> C["First set all items of the user to unselected"]
    C --> D["Then batch set the specified items to selected"]
    D --> E["Return success"]
```

### 4.5 Delete Cart Item — `DELETE /api/cart/{id}`

```mermaid
flowchart TD
    A["DELETE /api/cart/{cartItemId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query cart item"]
    C --> D{"Belongs to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F["Delete the cart item"]
    F --> G["Return success"]
```

### 4.6 Select All/Deselect All — `PUT /api/cart/select-all`

```mermaid
flowchart TD
    A["User submits<br/>{ selected: 1-select all, 0-deselect all }"] --> B["Parse JWT and obtain userId"]
    B --> C["Set all cart items of the current user to the specified selected state"]
    C --> D["Return success"]
```

### 4.7 Clear Cart — `DELETE /api/cart/clear`

```mermaid
flowchart TD
    A["DELETE /api/cart/clear"] --> B["Parse JWT and obtain userId"]
    B --> C["Delete all cart items of the current user"]
    C --> D["Return success"]
```

---

## V. Order Module

### 5.1 Get Anti-duplication Token — `GET /api/order/submit-token`

```mermaid
flowchart TD
    A["Frontend checkout page loads<br/>GET /api/order/submit-token"] --> B["Parse JWT and obtain userId"]
    B --> C["Generate unique identifier UUID"]
    C --> D["Redis store: order:submit-token:{token} = userId, 5 minutes"]
    D --> E["Return { token: uuid }"]
```

### 5.2 Create Order — `POST /api/order/create`

```mermaid
flowchart TD
    A["User submits order data<br/>{ anti-duplication Token, address ID, cart item IDs[], coupon ID? }"] --> B["Parse JWT and obtain userId"]
    B --> C["Redis validate: order:submit-token:{token} == userId"]
    C --> D{"Exists and bound to the current user?"}
    D -->|No| E["Return error: Do not submit repeatedly (token used/expired)"]
    D -->|Yes| F["Redis delete: order:submit-token:{token}<br/>(continue only if delete returns true, guaranteeing single use)"]
    F --> G["Begin database transaction"]
    G --> H["Validate that the shipping address belongs to the user"]
    H --> I["Query selected cart items (SQL limited to the owner)"]
    I --> J{"Product listed and stock sufficient?"}
    J -->|No| K["Roll back transaction → return error: Insufficient stock/unlisted"]
    J -->|Yes| L["Atomically deduct stock<br/>UPDATE stock = stock - quantity WHERE stock >= quantity"]
    L --> M["Calculate amount + coupon discount (reuse calculate logic)"]
    M --> N["Query shipping address → save JSON snapshot"]
    N --> O["Generate order number (timestamp + random number)"]
    O --> P["Insert order + batch insert order items<br/>snapshot unit price/quantity/subtotal"]
    P --> Q["Delete the ordered cart items"]
    Q --> R["Redeem coupon (conditional update prevents double redemption)"]
    R --> S["Commit transaction"]
    S --> T["Return { order ID, order number }"]
```

### 5.3 Order List — `GET /api/order/list`

```mermaid
flowchart TD
    A["GET /api/order/list<br/>?page=&size=&status="] --> B["Parse JWT and obtain userId"]
    B --> C["Paginated query on order table<br/>filter by userId + status"]
    C --> D["Iterate: query order items"]
    D --> E["Assemble paginated result"]
    E --> F["Return order list"]
```

### 5.4 Order Details — `GET /api/order/{id}`

```mermaid
flowchart TD
    A["GET /api/order/{orderId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Does the order belong to the current user?"}
    D -->|No| E["Return error: No permission to view"]
    D -->|Yes| F["Query order items"]
    F --> G["Query payment records"]
    G --> H["Query refund records"]
    H --> I["Assemble order details"]
    I --> J["Return order details"]
```

### 5.5 Cancel Order — `PUT /api/order/{id}/cancel`

```mermaid
flowchart TD
    A["PUT /api/order/{orderId}/cancel<br/>?reason=cancellation reason"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Does the order belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Order status == Pending Payment?"}
    F -->|No| G["Return error: Cannot cancel in the current status"]
    F -->|Yes| H["Update order status to: Cancelled<br/>Record cancellation reason"]
    H --> I["Restore stock<br/>Query order items → add each SKU's stock back"]
    I --> J["Return success"]
```

### 5.6 Confirm Receipt — `PUT /api/order/{id}/receive`

```mermaid
flowchart TD
    A["PUT /api/order/{orderId}/receive"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Does the order belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Order status == Shipped?"}
    F -->|No| G["Return error: Cannot confirm receipt in the current status"]
    F -->|Yes| H["Update order status to: Completed (4)<br/>Record receipt time"]
    H --> I["Return success"]
```

### 5.7 Delete Order — `DELETE /api/order/{id}`

```mermaid
flowchart TD
    A["DELETE /api/order/{orderId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Does the order belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Is the order in a deletable state (completed/cancelled)?"}
    F -->|No| G["Return error: Cannot delete in the current status"]
    F -->|Yes| H["Delete the order (and associated items)"]
    H --> I["Return success"]
```

---

## VI. Payment Module

### 6.1 Card Payment — `POST /api/payment/card`

```mermaid
flowchart TD
    A["User submits bank card information<br/>{ order ID, card number, expiry date, CVV }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Order belongs to current user && status == Pending Payment?"}
    D -->|No| E["Return error: Abnormal order status"]
    D -->|Yes| F["Validate card number with Luhn algorithm"]
    F --> G{"Card number valid?"}
    G -->|No| K["Return error: Invalid card number"]
    G -->|Yes| L["Validate expiry date"]
    L --> M{"Not expired?"}
    M -->|Expired| N["Return error: Card has expired"]
    M -->|Not expired| O["Simulate deduction → insert payment record (status=success)"]
    O --> P["Conditional update order: status=Paid<br/>WHERE id=? AND user_id=? AND status=Pending Payment"]
    P --> Q{"Rows updated?"}
    Q -->|0| R["Throw exception → transaction rollback (already paid/status changed)"]
    Q -->|1| S["Update product sales +1"]
    S --> T["Return payment success"]
```

### 6.2 Generate Payment QR Code — `POST /api/payment/qrcode`

```mermaid
flowchart TD
    A["POST /api/payment/qrcode<br/>{ order ID }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Order valid?"}
    D -->|No| E["Return error: Order does not exist"]
    D -->|Yes| F["Generate QR code content<br/>(order ID + amount + timestamp)"]
    F --> G["Redis store: qrcode:{orderId} = pending payment, 5 minutes"]
    G --> H["Return { QR code content }"]
```

### 6.3 Confirm QR Code Payment — `POST /api/payment/confirm-qrcode`

```mermaid
flowchart TD
    A["POST /api/payment/confirm-qrcode<br/>{ order ID }"] --> B["Parse JWT and obtain userId"]
    B --> C["Redis query: qrcode:{orderId}"]
    C --> D{"QR code not expired?"}
    D -->|Expired/nonexistent| E["Return error: QR code has expired"]
    D -->|Exists| F["Insert payment record (method=QR code, status=success)"]
    F --> G["Conditional update order: status=Paid<br/>WHERE status=Pending Payment"]
    G --> H{"Rows updated?"}
    H -->|0| I["Throw exception → transaction rollback (already paid/status changed)"]
    H -->|1| J["Redis delete: qrcode:{orderId}"]
    J --> K["Update product sales"]
    K --> L["Return payment success"]
```

---

## VII. Shipping Address Module

### 7.1 Address List — `GET /api/address/list`

```mermaid
flowchart TD
    A["GET /api/address/list"] --> B["Parse JWT and obtain userId"]
    B --> C["Query address table: all addresses of the current user<br/>sort by default first + creation time descending"]
    C --> D["Return address list"]
```

### 7.2 Add Address — `POST /api/address`

```mermaid
flowchart TD
    A["User submits<br/>{ recipient, phone, province, city, district, detailed address, whether default }"] --> B["Parse JWT and obtain userId"]
    B --> C{"Set as default address?"}
    C -->|Yes| D["First unset the default flag on the user's other addresses"]
    C -->|No| E["Insert into address table"]
    D --> E
    E --> F["Return success"]
```

### 7.3 Edit Address — `PUT /api/address/{id}`

```mermaid
flowchart TD
    A["PUT /api/address/{id}<br/>{ recipient, phone, ... , whether default }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query address"]
    C --> D{"Does the address belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Set as default?"}
    F -->|Yes| G["First unset the default flag on other addresses"]
    F -->|No| H["Update address table"]
    G --> H
    H --> I["Return success"]
```

### 7.4 Delete Address — `DELETE /api/address/{id}`

```mermaid
flowchart TD
    A["DELETE /api/address/{addressId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query address"]
    C --> D{"Does the address belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F["Check whether there are associated incomplete orders"]
    F --> G{"Associated incomplete orders?"}
    G -->|Yes| H["Return error: Cannot delete; associated orders exist"]
    G -->|No| I["Delete address"]
    I --> J["Return success"]
```

### 7.5 Set as Default Address — `PUT /api/address/default/{id}`

```mermaid
flowchart TD
    A["PUT /api/address/default/{addressId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query address"]
    C --> D{"Does the address belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F["First unset the default flag on all of the user's addresses"]
    F --> G["Set the specified address as default"]
    G --> H["Return success"]
```

---

## VIII. Comment Module

### 8.1 Comment List — `GET /api/comment/list/{productId}`

```mermaid
flowchart TD
    A["GET /api/comment/list/{productId}<br/>?page=&size=&star="] --> B["Paginated query on comment table<br/>filter by product ID + rating"]
    B --> C["Iterate: query user table for avatar/nickname"]
    C --> D["Iterate: query comment table for follow-up reviews/replies"]
    D --> E["Assemble paginated result"]
    E --> F["Return comment list"]
```

### 8.2 Post Initial Review — `POST /api/comment`

```mermaid
flowchart TD
    A["User submits<br/>{ product ID, order item ID, content, star, images[] }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order item"]
    C --> D{"Does the order item belong to the current user?"}
    D -->|No| E["Return error: No permission to review"]
    D -->|Yes| F["Query order table"]
    F --> G{"Order status == Completed (4)?"}
    G -->|No| H["Return error: Can only review after receipt"]
    G -->|Yes| I["Query comment table: whether the order item already has an initial review"]
    I --> J{"Initial review already exists?"}
    J -->|Yes| K["Return error: Already reviewed"]
    J -->|No| L["Insert comment (is_append=0, star=score)"]
    L --> M["Use image URLs returned by the upload API"]
    M --> N["Return comment ID"]
```

### 8.3 Post Follow-up Review — `POST /api/comment/append`

```mermaid
flowchart TD
    A["User submits<br/>{ initial review ID, follow-up content }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query initial review record"]
    C --> D{"Initial review exists && belongs to current user && no follow-up yet?"}
    D -->|No| E["Return error: Cannot post follow-up review"]
    D -->|Yes| F["Insert comment (linked to initial review ID, is_append=1)"]
    F --> G["Return success"]
```

### 8.4 Merchant Reply — `POST /api/comment/reply`

```mermaid
flowchart TD
    A["Merchant submits<br/>{ comment ID, reply content }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query merchant table: verify merchant identity and approval status"]
    C --> D{"Is a merchant and approved?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F["Query the comment"]
    F --> G["Query the product corresponding to the comment"]
    G --> H{"Does the product belong to the current merchant?"}
    H -->|No| I["Return error: No permission to reply"]
    H -->|Yes| J["Insert comment (linked to comment ID, parent_id=comment ID)"]
    J --> K["Return success"]
```

---

## IX. Refund/Return Module

### 9.1 Apply for Refund/Return — `POST /api/refund`

```mermaid
flowchart TD
    A["User submits<br/>{ order item ID, type (refund/return), reason, amount }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order item"]
    C --> D{"Does the order item belong to the current user?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F["Query order table"]
    F --> G{"Order status IN [Paid(2), Shipped(3), Completed(4)]?"}
    G -->|No| H["Return error: Cannot refund in the current status"]
    G -->|Yes| I["Query refund table: whether the item already has a refund record"]
    I --> J{"Refund record already exists?"}
    J -->|Yes| K["Return error: Refund already requested"]
    J -->|No| L["Insert refund record (status=Pending Review)"]
    L --> M["Return success"]
```

### 9.2 Refund List — `GET /api/refund/my`, `GET /api/merchant/refunds`, `GET /api/admin/refunds`

```mermaid
flowchart TD
    A["Refund list request (path selected by role)"] --> B["Parse JWT → obtain userId → obtain role"]
    B --> C{"Role?"}
    C -->|Regular User| D["GET /api/refund/my: filter by userId"]
    C -->|Merchant| E["GET /api/merchant/refunds: filter by merchant ID"]
    C -->|Administrator| F["GET /api/admin/refunds: all refunds"]
    D --> G["Assemble paginated result"]
    E --> G
    F --> G
    G --> H["Return refund list"]
```

### 9.3 Refund Details — `GET /api/refund/{id}`

```mermaid
flowchart TD
    A["GET /api/refund/{refundId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Query the refund record"]
    C --> D{"Does the refund record belong to the current user?"}
    D -->|No| E["Return error: No permission to view"]
    D -->|Yes| F["Assemble refund details<br/>{ refund amount, status, review comment, logistics information }"]
    F --> G["Return refund details"]
```

### 9.4 Review Refund/Return — `PUT /api/refund/{id}/review`

```mermaid
flowchart TD
    A["Merchant/Administrator submits<br/>{ approved: 1-approve, 0-reject, reason }"] --> B["Parse JWT → obtain userId → role"]
    B --> C["Query the refund record"]
    C --> D{"Has review permission?<br/>Merchants need valid status and product ownership"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F{"Refund status == Pending Review?"}
    F -->|No| G["Return error: Already reviewed"]
    F -->|Yes| H{"Approve?"}
    H -->|Reject| I["Update refund status: Rejected<br/>Record rejection reason"]
    I --> J["Restore order to Completed (4)"]
    H -->|Approve| K{"Refund type?"}
    K -->|Refund Only| L["Update refund status: Approved<br/>Set order to: Refunded"]
    K -->|Return| M["Update refund status: Awaiting Return<br/>Waiting for the buyer to submit return logistics"]
    L --> N["Return success"]
    M --> N
    J --> N
```

### 9.5 Submit Return Logistics — `PUT /api/refund/{id}/return`

```mermaid
flowchart TD
    A["Buyer submits<br/>{ courier company, tracking number }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query the refund record"]
    C --> D{"Belongs to current user && status == Awaiting Return?"}
    D -->|No| E["Return error: Cannot submit logistics in the current status"]
    D -->|Yes| F["Save return logistics information<br/>Update refund status to: Awaiting Merchant Receipt"]
    F --> G["Return success"]
```

### 9.6 Confirm Return Received — `PUT /api/refund/{id}/return-received`

```mermaid
flowchart TD
    A["PUT /api/refund/{id}/return-received"] --> B["Parse JWT → obtain userId → role"]
    B --> C["Query the refund record"]
    C --> D{"Has permission (merchant/administrator)?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F{"Refund status == Awaiting Merchant Receipt?"}
    F -->|No| G["Return error: Cannot confirm receipt in the current status"]
    F -->|Yes| H["Update refund status to: Return Completed<br/>Mark order as: Refunded"]
    H --> I["Return success"]
```

---

## X. Instant Messaging Module

### 10.1 Get Conversation List — `GET /api/chat/conversations`

```mermaid
flowchart TD
    A["GET /api/chat/conversations"] --> B["Parse JWT and obtain userId"]
    B --> C["Query conversation table: all conversations of the current user"]
    C --> D["Iterate: calculate unread counts → assemble conversation list"]
    D --> E["Return conversation list"]
```

### 10.2 Get Message History — `GET /api/chat/history/{conversationId}`

```mermaid
flowchart TD
    A["GET /api/chat/history/{conversationId}<br/>?page=&size="] --> B["Parse JWT and obtain userId"]
    B --> C["Query conversation table"]
    C --> D{"Does the conversation include the current user?"}
    D -->|No| E["Return error: No permission to view"]
    D -->|Yes| F["Paginated query on message table (newest to oldest)"]
    F --> G["Return paginated message list"]
```

### 10.3 Create/Get Conversation — `POST /api/chat/conversation`

```mermaid
flowchart TD
    A["User submits<br/>{ merchantId, productId }"] --> B["Parse JWT and obtain userId"]
    B --> C{"Conversation already exists?"}
    C -->|Yes| D["Return the existing conversation"]
    C -->|No| E["Create a new conversation"]
    E --> F["Return conversation information"]
    D --> F
```

### 10.4 Mark Conversation as Read — `PUT /api/chat/read/{conversationId}`

```mermaid
flowchart TD
    A["PUT /api/chat/read/{conversationId}"] --> B["Parse JWT and obtain userId"]
    B --> C["Mark messages sent by the other party in the conversation as read"]
    C --> D["Reset the conversation's unread count to zero"]
    D --> E["Return success"]
```

### 10.5 Total Unread Messages — `GET /api/chat/unread-count`

```mermaid
flowchart TD
    A["GET /api/chat/unread-count"] --> B["Parse JWT and obtain userId"]
    B --> C["Count the total unread messages across all conversations of the current user"]
    C --> D["Return { count }"]
```

### 10.6 WebSocket Connection Establishment — `ws://host/chat/{userId}?token=jwt`

```mermaid
sequenceDiagram
    actor Frontend as Frontend WebSocket
    participant Backend as ChatWebSocketEndpoint
    participant Redis as Redis
    participant DB as Database

    Frontend ->> Backend: ws://host/chat/{userId}?token=xxx
    Backend ->> Backend: Parse token from queryString → JWT parse userId
    Backend ->> Redis: Validate token:{userId} == token (prevent impersonation)
    alt Invalid token / userId does not match the path
        Backend -->> Frontend: Close connection (VIOLATED_POLICY)
    else Valid token
        Backend ->> Backend: Cache userId -> Session in local SESSIONS<br/>(kick out an existing old connection)
        Backend ->> Redis: set online:{userId} = "1"
        Backend ->> DB: Query unread messages
        Backend -->> Frontend: Push messages received while offline
    end
```

### 10.7 WebSocket Send Message

```mermaid
sequenceDiagram
    actor UserA as User A
    participant WS as ChatWebSocketEndpoint
    participant DB as Database
    actor UserB as User B (Merchant)

    UserA ->> WS: Send message { conversationId, content, type }
    WS ->> WS: Parse JSON → ChatService.sendMessage
    WS ->> DB: Save message + update conversation (last message/time) + increment the other party's unread count
    WS ->> DB: Query the peer userId of the conversation
    WS ->> WS: Get the receiver's Session from local SESSIONS
    alt Receiver online (local Session exists)
        WS -->> UserB: Push message in real time
    else Receiver offline
        WS ->> WS: Message persisted; wait for the other party to come online and push
    end
    WS -->> UserA: Echo message (sent status)
```

### 10.8 WebSocket Close Connection

```mermaid
sequenceDiagram
    actor Frontend as Frontend
    participant Backend as ChatWebSocketEndpoint
    participant Redis as Redis

    Frontend ->> Backend: WebSocket close / page closed
    Backend ->> Backend: @OnClose triggered
    Backend ->> Backend: Remove userId -> Session from local SESSIONS
    Backend ->> Redis: delete online:{userId}
```

---

## XI. Merchant Module

### 11.1 Merchant Onboarding Application — `POST /api/merchant/apply`

```mermaid
flowchart TD
    A["User submits<br/>{ store name, business license, contact phone }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query merchant table: whether the current user already has a record"]
    C --> D{"Application record already exists?"}
    D -->|Yes| E["Return error: You have already submitted an onboarding application"]
    D -->|No| F["Insert merchant record (status=0 Pending Review)"]
    F --> G["Return success"]
```

> Merchant self-registration: the registration API allows `role=2` and requires `shopName`; within the registration transaction it directly creates a `status=0` pending merchant record. `POST /api/merchant/apply` lets an already-registered regular user submit an onboarding application. Both paths allow only one submission; after approval, the administrator updates the user role to Merchant.

### 11.2 Merchant Order Management — `GET /api/merchant/orders`

```mermaid
flowchart TD
    A["GET /api/merchant/orders<br/>?page=&size=&status="] --> B["Parse JWT and obtain userId"]
    B --> C["Query merchant table"]
    C --> D{"Merchant approved?"}
    D -->|No| E["Return error: No permission"]
    D -->|Yes| F["Paginated query on order table: filter by merchant ID + status"]
    F --> G["Assemble paginated result"]
    G --> H["Return order list"]
```

### 11.3 Merchant Shipment — `PUT /api/merchant/ship/{orderId}`

```mermaid
flowchart TD
    A["Merchant submits<br/>{ tracking number, logistics company }"] --> B["Parse JWT and obtain userId"]
    B --> C["Query order table"]
    C --> D{"Do the products in the order belong to the current merchant?"}
    D -->|No| E["Return error: No permission to operate"]
    D -->|Yes| F{"Order status == Paid?"}
    F -->|No| G["Return error: Cannot ship in the current status"]
    F -->|Yes| H["Update order status: Shipped<br/>Record shipping time + logistics information"]
    H --> I["Return success"]
```

### 11.4 Sales Statistics — `GET /api/merchant/stats`

```mermaid
flowchart TD
    A["GET /api/merchant/stats?startDate=&endDate="] --> B["Parse JWT and obtain userId"]
    B --> C["Aggregate query on order table<br/>by merchant ID + time range"]
    C --> D["Statistics dimensions<br/>Revenue / Order Volume / Sales Ranking / Refund Rate / Time-period Trend"]
    D --> E["Assemble statistics result and return"]
```

### 11.5 Merchant Information — `GET /api/merchant/info`

```mermaid
flowchart TD
    A["GET /api/merchant/info"] --> B["Parse JWT and obtain userId"]
    B --> C["Query merchant table"]
    C --> D{"Merchant record exists?"}
    D -->|No| E["Return error: Not an onboarded merchant"]
    D -->|Yes| F["Return store information + review status"]
```

### 11.6 Merchant Product List — `GET /api/merchant/products`

```mermaid
flowchart TD
    A["GET /api/merchant/products<br/>?page=&size=&status="] --> B["Parse JWT and obtain userId"]
    B --> C["Verify merchant identity"]
    C --> D["Paginated query on product table: filter by merchant ID + status"]
    D --> E["Return product list"]
```

---

## XII. Administrator Module

### 12.1 User List — `GET /api/admin/users`

```mermaid
flowchart TD
    A["GET /api/admin/users<br/>?page=&size=&keyword=&status="] --> B["Parse JWT → verify administrator role"]
    B --> C{"Role == Administrator?"}
    C -->|No| D["Return error: No permission"]
    C -->|Yes| E["Paginated query on user table<br/>filter by keyword + status"]
    E --> F["Return paginated user list"]
```

### 12.2 Disable/Enable User — `PUT /api/admin/users/{id}/status`

```mermaid
flowchart TD
    A["PUT /api/admin/users/{id}/status<br/>{ status: 0-disabled, 1-normal }"] --> B["Parse JWT → verify administrator"]
    B --> C{"Administrator?"}
    C -->|No| D["Return error: No permission"]
    C -->|Yes| E["Query user table"]
    E --> F["Update user status (disabled/enabled)"]
    F --> G{"Is this a disable operation?"}
    G -->|Yes| H["Redis delete: token:{userId}<br/>Force logout"]
    G -->|No| I["Return success"]
    H --> I
```

### 12.3 Merchant Review List — `GET /api/admin/merchants`

```mermaid
flowchart TD
    A["GET /api/admin/merchants<br/>?page=&size=&status="] --> B["Parse JWT → verify administrator"]
    B --> C["Paginated query on merchant table<br/>filter by review status"]
    C --> D["Assemble paginated result"]
    D --> E["Return merchant application list"]
```

### 12.4 Review Merchant — `PUT /api/admin/merchants/{id}/review`

```mermaid
flowchart TD
    A["Administrator submits<br/>{ review result, reason }"] --> B["Parse JWT → verify administrator"]
    B --> C["Query merchant record"]
    C --> D{"Status == Pending Review?"}
    D -->|No| E["Return error: Already reviewed"]
    D -->|Yes| F{"Approve?"}
    F -->|Approve| G["Update merchant status: Approved<br/>Record review time"]
    F -->|Reject| H["Update merchant status: Rejected<br/>Record rejection reason + review time"]
    G --> I["Return success"]
    H --> I
```

### 12.5 Global Orders — `GET /api/admin/orders`

```mermaid
flowchart TD
    A["GET /api/admin/orders<br/>?page=&size=&status=&startDate=&endDate="] --> B["Parse JWT → verify administrator"]
    B --> C["Paginated query on order table<br/>filter by status + time range (all merchants)"]
    C --> D["Assemble paginated result"]
    D --> E["Return platform-wide order list"]
```

### 12.6 Data Report — `GET /api/admin/report`

```mermaid
flowchart TD
    A["GET /api/admin/report?startDate=&endDate="] --> B["Parse JWT → verify administrator"]
    B --> C["Aggregate statistics"]
    C --> D["Platform GMV / New users / Total orders"]
    C --> E["Refund rate / Top 10 best-selling categories / Merchant ranking"]
    C --> F["Sales trend by time period"]
    D --> G["Assemble report and return"]
    E --> G
    F --> G
```

### 12.7 Product Management — `GET/PUT/DELETE /api/admin/products`

```mermaid
flowchart TD
    A["GET /api/admin/products<br/>?page=&size=&keyword=&status="] --> B["Parse JWT → verify administrator"]
    B --> C["Paginated query on platform-wide products"]
    C --> D["Return product list"]

    A2["PUT /api/admin/products/{id} / {id}/status"] --> B2["Verify administrator"]
    B2 --> C2["Modify product information / list or unlist (platform-wide enforcement)"]
    C2 --> D2["Synchronously update the ES index"]

    A3["DELETE /api/admin/products/{id}"] --> B3["Verify administrator"]
    B3 --> C3["Delete product"]
    C3 --> D3["Delete the index from ES"]
```

### 12.8 Rebuild Search Index — `POST /api/admin/search-index/rebuild`

```mermaid
flowchart TD
    A["POST /api/admin/search-index/rebuild"] --> B["Parse JWT → verify administrator"]
    B --> C["Read all product data"]
    C --> D["Batch rebuild the ES index chrono_product"]
    D --> E["Return rebuild result"]
```

---

## XIII. Search Module

> Implementation note: all search-related APIs use `GET` (not POST). The ES document already contains every field needed by the response, so results are assembled and returned directly, **without querying MySQL again**; brand/price aggregation filters are not implemented.

### 13.1 Search Suggestions — `GET /api/search/suggest`

```mermaid
flowchart TD
    A["User enters a keyword (triggered in real time)"] --> B["GET /api/search/suggest?keyword="]
    B --> C["Build ES query<br/>match_bool_prefix(name) + term(status=1)"]
    C --> D["Query ES: chrono_product (size=10, only name)"]
    D --> E["Deduplicate product names in hit order"]
    E --> F["Return a string array"]
```

### 13.2 Product Search — `GET /api/search`

```mermaid
flowchart TD
    A["GET /api/search<br/>keyword / categoryId / brand / minPrice / maxPrice / sort / page / size"] --> B{"Are keyword and categoryId both empty?"}
    B -- Yes --> C["Return an empty page"]
    B -- No --> D["Logged-in users write search history (delete then insert for duplicate terms)"]
    D --> E["Build ES query<br/>must: multi_match(name^3, description, brand, operator=AND)<br/>filter: term(status=1) + term(categoryId) + term(brand) + range(minPrice)"]
    E --> F["Sort: relevance (optional _score, sales) / sales_desc / price_asc / price_desc / time_desc"]
    F --> G["Query ES: chrono_product (from/size)"]
    G --> H["Parse hits → SearchItemVO (id/name/description/brand/categoryId/categoryName/minPrice/sales/image)"]
    H --> I["Return paginated result (total/page/size/list)"]
```

### 13.3 Search History — `GET /api/search/history`

```mermaid
flowchart TD
    A["GET /api/search/history"] --> B["Parse JWT and obtain userId"]
    B --> C["Query search_history table<br/>descending by search time, latest 20 records (duplicate terms already removed)"]
    C --> D["Return search history list"]
    E["DELETE /api/search/history"] --> F["Delete all records by userId"]
```

### 13.4 Hot Searches — `GET /api/search/hot`

```mermaid
flowchart TD
    A["GET /api/search/hot"] --> B["Aggregate the search_history table<br/>descending by keyword occurrence count"]
    B --> C["Take the top N hot keywords"]
    C --> D["Return hot search list"]
```

### 13.5 Sync Products to ES

```mermaid
flowchart TD
    A["Triggered by product listing/update"] --> B["Assemble ES document<br/>{ id, name, description, brand, categoryId, categoryName, minPrice, sales, image, status, createdAt }"]
    B --> C["ES Index request: chrono_product / _doc / {productId} (overwrite by id)"]
    C --> D["ES automatically creates or updates the document"]
    D --> E["Sync complete (exceptions are only logged and do not roll back the main business)"]
```

### 13.6 Delete Product from ES

```mermaid
flowchart TD
    A["Triggered by product unlisting/deletion"] --> B["ES Delete request: chrono_product / _doc / {productId}"]
    B --> C{"Document does not exist (404)?"}
    C -- Yes --> D["Silently ignore"]
    C -- No --> E["Deletion complete"]
```

---

## XIV. Flash-sale Coupon Module

### 14.1 Coupon List and Details — `GET /api/seckill/list`, `GET /api/seckill/detail/{id}`

```mermaid
flowchart TD
    A["Coupon Center / Homepage coupon entry"] --> B["GET /api/seckill/list<br/>{ status?, page, size }"]
    B --> C["Validate status ∈ {0,1}, with pagination parameter fallbacks"]
    C --> D["Query seckill_coupon pagination + total count"]
    D --> E["Assemble SeckillCouponVO<br/>{ name, discount type/value, threshold, remaining quantity, per-user limit, time, status }"]
    E --> F["Return PageResult"]

    A --> G["Click coupon card"]
    G --> H["GET /api/seckill/detail/{id}"]
    H --> I{"Does the coupon exist"}
    I -->|No| J["404 Coupon not found"]
    I -->|Yes| K["Return coupon details"]
```

### 14.2 Claim Coupon — `POST /api/seckill/claim/{couponId}`

```mermaid
flowchart TD
    A["Logged-in user clicks to claim"] --> B["Validate coupon exists + activity is in progress"]
    B --> C["Redis lazy-load seckill:remain:{couponId}"]
    C --> D["DECR remaining stock"]
    D --> E{"Result < 0 ?"}
    E -->|Yes| F["Restore stock → Sold out"]
    E -->|No| G["INCR seckill:claimed:{couponId}:{userId}"]
    G --> H{"Exceeds per-user limit?"}
    H -->|Yes| I["Restore stock/count → Already claimed"]
    H -->|No| J["Insert user_coupon (status=0)"]
    J --> K["MySQL remain_count - 1"]
    K --> L["Return SeckillClaimVO"]
    J -->|Exception| M["Transaction rollback + Redis compensation"]
    C -->|Redis unavailable| N["Degrade: atomic database deduction<br/>UPDATE ... WHERE remain_count > 0"]
    N --> O["Validate per-user limit in database"]
```

**Key design:**

- Redis deducts stock first, then records the claim count, avoiding overselling and over-claiming under high concurrency.
- When Redis fails, degrade to atomic database deduction (`remain_count > 0` conditional update) to ensure availability.
- When database writes fail, compensate Redis (restore stock, roll back count) to ensure eventual consistency.

### 14.3 Order Deduction — `POST /api/seckill/calculate`

```mermaid
flowchart TD
    A["Select coupon on checkout page"] --> B["POST /api/seckill/calculate<br/>{ totalAmount, userCouponId }"]
    B --> C{"userCouponId is empty"}
    C -->|Yes| D["Original price is the amount payable"]
    C -->|No| E["Validate user coupon exists/belongs to current user/unused"]
    E --> F["Validate coupon not expired"]
    F --> G["Validate minimum spend threshold is met"]
    G --> H["Calculate discount: fixed-amount coupons use face value; percentage coupons calculated by discount rate"]
    H --> I["Return { originalAmount, discountAmount, finalAmount, couponName }"]
```

### 14.4 Admin Management and Status Refresh

| API | Permission | Description |
|------|------|------|
| `POST /api/seckill/create` | Administrator | Create coupon; initialize `remain_count` to `total_count` |
| `PUT /api/seckill/update/{id}` | Administrator | Modify coupon information; synchronize remaining stock and Redis keys when the total changes |
| `DELETE /api/seckill/{id}` | Administrator | Delete coupon |
| `GET /api/seckill/my-coupons` | Logged-in user | My coupons, filtered by unused/used/expired |

`SeckillStatusScheduler` refreshes once every 30 seconds:

- Not Started → In Progress: `start_time <= now and status = 0`
- In Progress → Ended: `end_time < now and status = 1`
- Unused user coupons are batch-marked as expired when the activity ends.

---

## XV. API List

| Module | Path Prefix | Number of APIs | Permission |
|------|----------|--------|------|
| User Authentication | `/api/user` | 9 | Public/Login |
| Product | `/api/product` | 6 | Public/Merchant |
| Category | `/api/category` | 2 | Public |
| Cart | `/api/cart` | 7 | Logged-in User |
| Order | `/api/order` | 7 | Logged-in User |
| Payment | `/api/payment` | 4 | Logged-in User |
| Shipping Address | `/api/address` | 6 | Logged-in User |
| Comment | `/api/comment` | 4 | Public view/Logged-in User/Merchant |
| Refund | `/api/refund` | 6 | Logged-in User/Merchant/Administrator |
| Merchant | `/api/merchant` | 7 | Registered user onboarding/Merchant |
| Administrator | `/api/admin` | 15 | Administrator |
| File Upload | `/api/upload` | 1 | Logged-in User/Merchant/Administrator |
| Instant Messaging | `/api/chat` | 5 | Logged-in User/Merchant |
| Search | `/api/search` | 5 | Public/Logged-in User |
| Flash-sale Coupon | `/api/seckill` | 8 | Public view/Logged-in claim/Administrator |
| **Total** | | **92** | |

---

## XVI. Caching Strategy

| Caching Key | Type | Expiration | Purpose |
|----------|------|------|------|
| `captcha:{uuid}` | String | 2 minutes | Graphic captcha |
| `reset:code:{uuid}` | String | 5 minutes | Password reset verification code |
| `reset:account:{uuid}` | String | 5 minutes | User ID bound to the password reset verification code |
| `token:{userId}` | String(JWT) | 2 hours | User login state |
| `order:submit-token:{token}` | String | 5 minutes | Order submission anti-duplication (value is userId) |
| `qrcode:{orderId}` | String | 5 minutes | Payment QR code |
| `category:tree` | JSON | 30 minutes | Category tree |
| `online:{userId}` | String | During connection | WebSocket online status (value "1") |
| `seckill:remain:{couponId}` | String | During the activity | Flash-sale coupon remaining stock |
| `seckill:claimed:{couponId}:{userId}` | String | During the activity | Flash-sale coupon per-user claim count |

---

## XVII. Database Tables and Responsibilities

| Table Name | Corresponding Module | Description |
|------|----------|------|
| `user` | Authentication | Basic user information, password hash, role, status |
| `merchant` | Merchant | Store information, business license, review status |
| `category` | Category | Category tree, level, sort order |
| `spec_template` | Product | Specification templates (color/size, etc.) |
| `spec_value` | Product | Specification values (red/XL, etc.) |
| `product` | Product | Basic product information, listed/unlisted status |
| `product_sku` | Product | Specification, price, stock |
| `cart_item` | Cart | User cart items, selected status |
| `order` | Order | Order master table, state transitions, address snapshot |
| `order_item` | Order | Order items, snapshot price and quantity |
| `payment` | Payment | Payment records, payment method |
| `refund` | Refund | Refund applications, review |
| `address` | Shipping Address | Shipping address |
| `comment` | Comment | Initial review/follow-up review/reply |
| `search_history` | Search | User search history |
| `conversation` | Instant Messaging | Conversation list |
| `message` | Instant Messaging | Message records |
| `seckill_coupon` | Flash-sale Coupon | Coupon definition, total/remaining quantity, activity time window |
| `user_coupon` | Flash-sale Coupon | User coupon claim records, usage status |
| `operation_log` | Admin Backend | AOP operation log (table structure reserved, not enabled) |

---

## XVIII. Transaction and Lock Design

| Scenario | Strategy | Description |
|------|------|------|
| Order placement | Anti-duplication token + database transaction | Redis anti-duplication token prevents duplicate submissions; the transaction guarantees atomicity of order + stock deduction |
| Stock deduction | Optimistic locking (conditional update) `WHERE stock >= ?` | Atomic deduction prevents overselling |
| Claiming flash-sale coupons | Redis atomic deduction + database transaction | Redis `DECR`/`INCR` prevents overselling and over-claiming; compensate Redis if MySQL writes fail |
| Payment | Order status conditional update | `UPDATE order SET status=Paid WHERE status=Pending Payment`; 0 rows updated rejects duplicate payment |
| Refund review | Optimistic locking (state machine) | Only the "Pending Review" status can be reviewed |

---

## XIX. Deployment Architecture

```mermaid
graph TB
    subgraph UserBrowser["User Browser"]
        BR["Browser"]
    end

    subgraph Nginx["Nginx (Port 80)"]
        NG["Static files + reverse proxy"]
    end

    subgraph App["Spring Boot Application (Port 8080)"]
        BE["chrono-bbs-server<br/>Runs as a single JAR"]
    end

    subgraph Infrastructure["Docker Compose Infrastructure"]
        DB[("MySQL 8.0<br/>Port 3306")]
        RD[("Redis 7<br/>Port 6379")]
        ES[("Elasticsearch 7.17<br/>Port 9200")]
    end

    BR -->|"/" static resources| NG
    BR -->|"/api/*"| NG
    BR -->|"/es/*"| NG
    BR -->|"ws:// /chat/"| NG

    NG -->|proxy_pass| BE
    NG -->|proxy_pass| ES
    NG -->|proxy_pass upgrade| BE

    BE --> DB
    BE --> RD
    BE --> ES
```

> The Maven multi-module setup (common / module-mapper / module-service / module-api / module-start) is only used for code layering and organization; at runtime it is merged into a single Spring Boot JAR.

---

## XX. Frontend Design Reference

### Page Route Structure

| Route | Page | Permission |
|------|------|------|
| `/home` | Home product list (`/` redirects here) | Public |
| `/product/:id` | Product details | Public |
| `/search` | Search results | Public |
| `/coupons` | Coupon Center | Public |
| `/help/:type` | Help Center | Public |
| `/login` | Login | Public |
| `/register` | Register (buyer/merchant) | Public |
| `/forgot-password` | Forgot Password | Public |
| `/cart` | Cart | Logged-in User |
| `/checkout` | Checkout | Logged-in User |
| `/payment/:orderId` | Payment | Logged-in User |
| `/orders` | Order list | Logged-in User |
| `/order/:id` | Order details | Logged-in User |
| `/payments` | Payment records | Logged-in User |
| `/refunds` | Refunds/after-sales | Logged-in User |
| `/profile` | Redirects to `/settings` | Logged-in User |
| `/settings` | Account settings | Logged-in User |
| `/address` | Address management | Logged-in User |
| `/chat/:conversationId?` | Instant Messaging | Logged-in User |
| `/merchant/apply` | Merchant onboarding | Regular User/Merchant |
| `/merchant/*` | Merchant Backend | Merchant |
| `/admin/*` | Administrator Backend | Administrator |

### Global State

| Store | Data | Purpose |
|-------|------|------|
| `userStore` | token / userInfo / role | Login state, permission checks (persisted to localStorage) |

> Currently only `userStore` is global Pinia state; cart, order, chat, and other data are fetched on demand by page components and maintained locally. No separate stores have been created.

### Data Flow

```mermaid
flowchart LR
    subgraph Vue["Vue Components"]
        V["Page Views"]
        C["Common Components"]
    end

    subgraph Store["Global State Pinia"]
        US["userStore<br/>token / userInfo / role"]
    end

    subgraph API["API Layer"]
        AX["Axios<br/>Request/Response interceptors"]
    end

    subgraph Backend["Backend"]
        BE["Spring Boot<br/>REST API"]
        ES["Elasticsearch<br/>REST API"]
        WS["WebSocket<br/>ws://"]
    end

    V <--> US
    C <--> US

    V --> AX
    AX --> BE
    BE --> ES
    V --> WS

    AX -- "Request interception: inject Token" --> AX
    AX -- "Response interception: 401 → clear token → redirect to login" --> AX
```

---

## Summary

| Module | Number of Functions | Description |
|------|--------|------|
| User Authentication | 9 | Captcha/register/login/logout/forgot password/reset password/change password/info/update profile |
| Product | 6 | List/details/create/update/list or unlist/delete |
| Category | 2 | Category tree/child categories |
| Cart | 7 | List/add/update quantity/select/select-all/delete/clear |
| Order | 7 | Anti-duplication token/create/list/details/cancel/confirm receipt/delete |
| Payment | 4 | Card payment/QR code/confirm payment/payment records |
| Shipping Address | 6 | List/details/add/edit/delete/set default |
| Comment | 4 | List/initial review/follow-up review/merchant reply |
| Refund/Return | 6 | Apply/list/details/review/submit return/confirm receipt |
| Instant Messaging | 5 | Conversation list/message history/create conversation/mark read/unread count (WebSocket endpoint counted separately) |
| Merchant | 7 | Onboarding application/information/products/orders/shipment/refunds/statistics |
| Administrator | 15 | Users/disable-enable/merchant review/categories/products/orders/refunds/report/rebuild index |
| File Upload | 1 | Image upload |
| Search | 5 | Suggestions/search/history/clear history/hot searches |
| Flash-sale Coupon | 8 | List/details/claim/my coupons/create/update/delete/calculate |
| **Total** | **92** | |

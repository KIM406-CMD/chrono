# Online Supermarket System — Requirements Analysis

> [简体中文](../zh/requirements-analysis.md) | English

> This document covers the business background, user role permissions, core business rules, functional requirements list (numbered + priority), business processes, data requirements, and runtime environment. It serves as the basis for the High-Level Design and Detailed Design.

---

## 1. Introduction

### 1.1 Project Origin

This system is a course design project. The original problem description is as follows:

> Users access the Online Supermarket System through a browser. The system displays all products in categories to help users progressively find the products they want; the system also provides keyword search; while browsing the product catalog, users can click to view detailed product information and prices; if satisfied, users can temporarily place products in the "Cart"; they can also remove products from the "Cart" at any time. When users have finished selecting, they can proceed to payment: the user enters a credit card number, the system contacts the corresponding bank payment system, and payment begins. Finally, the system should be able to provide administrators with query interfaces and various reports that summarize product sales.

| Item | Description |
|------|-------------|
| **Project Name** | Online Supermarket System |
| **Project Type** | Course Design |
| **Project Description** | Users access the Online Supermarket System through a browser, browse products by category, search by keyword, add products to the cart, and complete orders through simulated payment. The backend provides merchant onboarding, product management, order management, and sales report features. |

This document extends and refines the requirements based on the problem description.

### 1.2 Scope

This document provides a comprehensive requirements analysis for the Online Supermarket System, covering functional requirements, non-functional requirements, role analysis, business processes, and data requirements. It serves as the basis for the subsequent High-Level Design and Detailed Design.

### 1.3 Core Business Rules

#### Category System

- Supports three-level categories; example: `Electronics (Level 1) → Mobile Communications (Level 2) → Smartphones (Level 3)`
- The admin backend can dynamically create, read, update, and delete categories. The database implements the tree structure through a self-association on `parent_id`.

#### Product Attributes

| Field | Description |
|-------|-------------|
| Product name | Required |
| Product description | Rich text |
| Product images | Multiple images supported |
| Price | Stored in cents to avoid floating-point precision issues |
| Stock | Integer, pushed down to the SKU level |
| Brand | Optional |
| Category | Associated with a three-level category |
| Merchant | Associated with a merchant user |
| Status | Listed/Unlisted |
| Sales | Cumulative sales |

#### Cart Rules

- Stored server-side in the database; available only to logged-in users; guests cannot use it.
- Stock is validated when adding to the cart and validated again at checkout.

#### Payment Rules (Simulated Payment)

- **Card payment**: Validate the card number (Luhn algorithm), expiry date (MM/YY and not expired), and CVV (3 or 4 digits); if validation passes, simulate a successful payment.
- **QR code payment**: Generate a simulated payment QR code; after the user confirms, simulate a successful payment.
- No real third-party payment interface is integrated; only frontend simulation and format validation are performed.

#### Order and Merchant Rules

- For order state transitions, see Section 3.5; for the merchant onboarding process, see Section 5.3.

---

## 2. User Role Analysis

| Role | Description | Core Permissions |
|------|-------------|------------------|
| **Guest** | A visitor who has not logged in | Browse product lists, filter by category, view product details, keyword search |
| **Registered User** | A user who has registered and logged in | Guest permissions + add to cart, manage cart, place orders, pay, confirm receipt, request refund/return, post comments |
| **Merchant** | An approved merchant | User permissions + submit onboarding application, manage own products (CRUD/list/unlist/stock), manage orders (ship), review refunds/returns, view sales statistics |
| **Administrator** | Highest system privileges | User management, merchant onboarding review, three-level category management, global product management, global order management, refund/return review, global sales reports |

### 2.1 Role Inheritance

```
Guest ──(Login)──→ Registered User ──(Onboarding Review)──→ Merchant
                       │
                       └──(System-assigned)──→ Administrator
```

- Guest is the most basic anonymous role.
- After logging in, a registered user inherits all guest permissions and gains additional features such as the cart and order placement.
- A merchant inherits all registered user permissions and gains additional features such as product management and order management.
- The administrator is an independent backend role and does not inherit shopping-related features.

---

## 3. Functional Requirements

### 3.1 User Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-01 | User Registration | Register an account by entering a username, password, phone number, email, and graphical captcha | High |
| F-02 | User Login | Log in with username + password + graphical captcha and generate an authentication token | High |
| F-03 | User Logout | Clear the login state and redirect to the home page | High |
| F-04 | Personal Information Management | View and modify personal information (nickname, phone number, email) | Medium |
| F-05 | Change Password | Enter the old password and then change it to a new password | Medium |
| F-06 | Forgot Password | Obtain a verification code via phone number/email and reset the password after verification | Medium |
| F-07 | Shipping Address Management | Create, read, update, and delete multiple shipping addresses; set a default address | High |

### 3.2 Product Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-08 | Product List Browsing | The home page displays a product list with pagination support | High |
| F-09 | Browse by Category | Filter products progressively through three-level categories | High |
| F-10 | Product Detail View | Display product name, description (rich text), multiple images, brand, SKU specifications, price, stock, sales, and comments | High |
| F-11 | Keyword Search | Full-text search for products by keyword (name, description, brand) with search suggestions/autocomplete | High |
| F-12 | Multi-condition Filtering | Search results support filtering by price range, brand, and category, and sorting by sales/price/listing time | Medium |
| F-13 | Search History | Record the search history of logged-in users and support clearing it | Low |

### 3.3 Category Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-14 | Three-level Category Display | The frontend displays the category tree/navigation hierarchically | High |
| F-15 | Category Management (Administrator) | Create, read, update, and delete three-level categories; sorting | High |

**Category example**: `Electronics (Level 1) → Mobile Communications (Level 2) → Smartphones (Level 3)`

### 3.4 Cart Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-16 | Add to Cart | Select a product SKU and quantity, add it to the cart, and validate stock | High |
| F-17 | Modify Quantity | Modify the purchase quantity of a product in the cart | High |
| F-18 | Delete Product | Remove a product from the cart | High |
| F-19 | Select/Deselect | Select or deselect the products to be checked out | High |
| F-20 | Stock Validation | Validate stock both when adding to the cart and at checkout, and notify the user if stock is insufficient | High |

**Constraint**: Only logged-in users can use the cart; guests cannot.

### 3.5 Order Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-21 | Submit Order | Select products from the cart → confirm address → confirm amount → submit; deduct stock | High |
| F-22 | Order List | View own orders with status filtering and pagination | High |
| F-23 | Order Details | View complete order information (products, address, amount, status, timeline) | High |
| F-24 | Cancel Order | Cancel an order in Pending Payment status and restore stock | High |
| F-25 | Confirm Receipt | Manually confirm receipt when the order is in Shipped status | High |
| F-26 | Duplicate Submission Prevention | Obtain an anti-duplicate token before placing an order to prevent multiple orders caused by repeated clicks | Medium |

**Order state transitions**:

```
Pending Payment ──→ Cancelled (user cancellation / timeout cancellation)
  │
  └──→ Paid ──→ Shipped ──→ Received ──→ Completed
                   │            │
                   │            └──→ Refund/Return Request ──→ Under Review ──→ Approved (refund completed)
                   │
                   └──→ Refund/Return Request ──→ Under Review ──→ Approved (refund completed)
```

### 3.6 Payment Module (Simulated Payment)

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-27 | Card Payment | Enter card number, expiry date, and CVV → frontend Luhn algorithm + format pre-validation → backend validation → simulated payment success | High |
| F-28 | QR Code Payment | Generate a simulated payment QR code → user confirmation → simulated payment success | Medium |
| F-29 | Payment Record | Record the payment method, amount, and time of each payment | Medium |

### 3.7 Refund/Return Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-30 | Request Refund/Return | Users initiate a refund or return request for orders in Shipped/Received status and fill in the reason | Medium |
| F-31 | Refund/Return Review | The merchant or administrator reviews the request, approves or rejects it, and fills in the reason | Medium |
| F-32 | Return Shipment Confirmation | After the return request is approved, the merchant confirms receipt of the returned product → executes the refund | Medium |

### 3.8 Comment Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-33 | Post Comment | Users post comments on purchased products (text + images + 1-5 star rating) | Low |
| F-34 | Reply to Comment | Merchants reply to user comments | Low |

### 3.9 Merchant Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-35 | Onboarding Application | After registering, a user submits a merchant onboarding application (shop name, business license, contact phone) | High |
| F-36 | Product Management | Merchants create, read, update, and delete their own products and set them as Listed/Unlisted | High |
| F-37 | SKU Management | Set specification templates and Specification Values for products, generate SKUs by combination, and set prices and stock for different SKUs | High |
| F-38 | Inventory Management | Modify the SKU stock quantities of products | Medium |
| F-39 | Order Management | View order lists for their own products and perform shipping operations | High |
| F-40 | Sales Statistics | View sales revenue, sales trends, and best-selling rankings for their own products | Medium |

### 3.10 Administrator Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-41 | User Management | View all users; enable/disable users | Medium |
| F-42 | Merchant Review | Review merchant onboarding applications (Approve/Reject + review comments) | High |
| F-43 | Category Management | Create, read, update, and delete three-level categories | High |
| F-44 | Product Management | Global product management; can operate on all merchants' products | Medium |
| F-45 | Order Management | View all orders and process refund/return reviews | Medium |
| F-46 | Sales Reports | Global sales data statistics (see Section 3.11 for details) | High |

### 3.11 Sales Report Module (Administrator/Merchant)

| ID | Function | Description | Priority |
|------|------|------|--------|
| R-01 | Sales Revenue Statistics | Calculate total sales by day/week/month; support line chart/bar chart display | High |
| R-02 | Best-selling Products Ranking | Rank the top N products in descending order of sales | Medium |
| R-03 | Category Sales Statistics | Calculate the sales revenue proportion of each category by three-level category (pie chart) | Medium |
| R-04 | Merchant Sales Statistics | Rank merchants by sales revenue (administrator view) | Medium |
| R-05 | Order Volume Trends | Display the trends of orders placed, paid, and completed by day in a line chart | Medium |
| R-06 | Average Order Value Analysis | Calculate the average spending per order | Low |

### 3.12 Instant Messaging Module

| ID | Function | Description | Priority |
|------|------|------|--------|
| F-47 | User-Merchant Chat | WebSocket real-time text messages; support initiating a conversation from the product detail page | Low |
| F-48 | Conversation List | Display all historical conversations and support unread message reminders | Low |

---

## 4. Non-Functional Requirements

| Category | Requirement Description |
|------|----------|
| **Performance** | Product list query response time < 1s; search response time < 2s; support 100 concurrent users |
| **Security** | Passwords stored with BCrypt encryption; JWT + Redis dual-validation authentication; SQL injection prevention (MyBatis parameterization); API permission validation |
| **Availability** | Frontend adapted to PC browsers (Chrome / Edge) with a responsive layout |
| **Maintainability** | Backend: Controller → Service → Mapper layering; frontend: component-based development; unified exception handling |
| **Reliability** | Order placement uses distributed locks to prevent concurrency; payment is idempotent; stock deduction uses Optimistic Locking (`WHERE stock >= ?`) |

---

## 5. Business Processes

### 5.1 Core Shopping Process

```
Browse products (category/search)
    → Click to view details
    → Select specifications → Add to cart (login required)
    → Go to cart → Select products → Checkout
    → Confirm shipping address → Confirm order amount
    → Submit order (anti-duplicate token + stock deduction)
    → Select payment method (card/QR code)
    → Payment successful
    → Merchant ships
    → Confirm receipt
    → Complete
```

### 5.2 Refund/Return Process

```
Customer side: Confirm receipt → Order details → Request refund/return (select type + fill in reason)
Merchant/administrator side: Review request → Approve/Reject
  ├── Approve: User ships the product back → Merchant confirms receipt → Refund → Restore stock
  └── Reject: Fill in rejection reason → Process terminates
```

### 5.3 Merchant Onboarding Process

```
User registers → Selects the "Merchant" role → Fills in onboarding information (shop name/business license/phone)
    → Submits application → Administrator reviews → Approve/Reject
    → After approval, the merchant can log in to the backend to manage products
```

### 5.4 Forgot Password Process

```
Customer side: Click "Forgot Password" → Enter graphical captcha → After verification, enter phone number/email
    → Obtain SMS/email verification code → Enter verification code → Verification passes → Enter new password
    → Store with BCrypt encryption → Redirect to login page
```

### 5.5 User Registration/Login Process (Captcha Version)

```
Customer side: Enter registration/login page → Request graphical captcha on page load (UUID + Base64 image)
    → Fill in the form (including graphical captcha) → Validate graphical captcha on submit (one-time, deleted after validation)
    → After verification, continue with the registration/login logic
```

---

## 6. Data Requirements Summary

| Data Entity | Core Fields | Description |
|----------|----------|------|
| User (user) | Username, password (BCrypt), phone number, email, role, status | Supports Guest/User/Merchant/Administrator |
| Merchant (merchant) | Associated user ID, shop name, business license URL, review status | One-to-one between user and merchant |
| Category (category) | Name, parent ID, level (1/2/3), sorting | `parent_id` self-association |
| Product (product) | Name, description, images, brand, category, merchant, listing status | Price and stock pushed down to SKU |
| Product SKU (product_sku) | Associated product, specification value combination, price (cents), stock | Supports multiple specifications and prices |
| Cart (cart_item) | User, SKU, quantity, selected status | Merge when the same SKU is added repeatedly |
| Order (order) | Order number, user, address snapshot, amount, status, timestamps | Address is snapshotted when the order is placed |
| Order Item (order_item) | Order, product, SKU, quantity, unit price, subtotal | Unit price stores the value at order time |
| Payment Record (payment) | Order, payment method, amount, status | Simulated payment |
| Refund/Return (refund) | Order item, applicant, type, amount, review status | Supports partial refunds |
| Comment (comment) | Product, user, content, rating, images | Supports follow-up comments |
| Conversation/Message (conversation/message) | User, merchant, message content | WebSocket communication |

---

## 7. Functional Priority Overview

### 7.1 High Priority (Must Implement)

| ID | Function |
|------|------|
| F-01 ~ F-03 | Registration, login, logout |
| F-07 | Shipping Address management |
| F-08 ~ F-11 | Product list, category browsing, details, search |
| F-14 ~ F-15 | Category display and management |
| F-16 ~ F-21 | Cart CRUD + submit order |
| F-22 ~ F-25 | Order list, details, cancellation, confirm receipt |
| F-27 | Card payment (simulated) |
| F-35 ~ F-37 | Merchant onboarding, product management, SKU management |
| F-39 | Merchant order management (shipping) |
| F-42 ~ F-43 | Administrator merchant review, category management |
| R-01 | Sales revenue statistics |
| F-46 | Sales reports (administrator view) |

### 7.2 Medium Priority (Implement If Possible)

| ID | Function |
|------|------|
| F-04 ~ F-06 | Personal information, change password, forgot password |
| F-12 | Multi-condition search filtering |
| F-26 | Anti-duplicate submission token |
| F-28 ~ F-29 | QR code payment, payment records |
| F-30 ~ F-32 | Complete refund/return process |
| F-38 | Merchant inventory management |
| F-40 | Merchant sales statistics |
| F-41 | Administrator user management |
| F-44 ~ F-45 | Administrator product/order management |
| R-02 ~ R-06 | Best-selling ranking, category statistics, merchant statistics, order trends, average order value |

### 7.3 Low Priority (Do If Time Permits)

| ID | Function |
|------|------|
| F-13 | Search history |
| F-33 ~ F-34 | Comment features |
| F-47 ~ F-48 | Instant messaging |

---

## 8. Runtime Environment Requirements

| Component | Description |
|------|------|
| Server | Java 21 + Spring Boot 3.x |
| Frontend | Vue 3 + Vite (deployed on Nginx) |
| Database | MySQL 8.x |
| Cache | Redis 7.x |
| Search engine | Elasticsearch 7.x / 8.x |
| Containerization | Docker + Docker Compose orchestration |
| Demo environment | Personal laptop; all services run locally |

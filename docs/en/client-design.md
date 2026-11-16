# Online Supermarket System - Client High-Level Design

> [简体中文](../zh/client-design.md) | English

---

## 1. Technology Selection

| Category | Technology | Version/Notes |
|------|------|-----------|
| Core framework | Vue 3 | Composition API + `<script setup>` |
| Development language | TypeScript | Strong type constraints |
| Build tool | Vite | Development hot reload + production Rollup bundling |
| UI component library | Element Plus | Officially recommended for Vue 3 |
| State management | Pinia | Officially recommended for Vue 3, TypeScript-friendly |
| HTTP client | Axios | Interceptors for unified token/error handling |
| CSS approach | Scoped CSS + CSS variables | Vue SFC style isolation + global design tokens |
| Routing | Vue Router 4 | History mode |
| Mock data | vite-plugin-mock | Used for joint debugging with Mock APIs before the backend is complete |
| Icons | @element-plus/icons-vue | Element Plus icon library |

---

## 2. Project Directory Structure (Hybrid Approach)

```
chrono-bbs-client/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── mock/                             # ===== Mock data layer (vite-plugin-mock) =====
│   ├── index.ts                      # Mock entry
│   ├── user.ts                       # User authentication Mock
│   ├── product.ts                    # Product Mock (including category tree)
│   ├── category.ts                   # Category Mock
│   ├── cart.ts                       # Cart Mock
│   ├── order.ts                      # Order Mock
│   ├── payment.ts                    # Payment Mock
│   ├── address.ts                    # Address Mock
│   ├── merchant.ts                   # Merchant Mock
│   ├── admin.ts                      # Administrator Mock
│   ├── refund.ts                     # Refund Mock
│   ├── chat.ts                       # Chat Mock
│   ├── upload.ts                     # Upload Mock
│   ├── search.ts                     # Search Mock
│   ├── comment.ts                    # Comment Mock
│   └── seckill.ts                    # Flash-sale Coupon Mock
├── public/
│   ├── favicon.svg
│   ├── icons.svg
│   └── img/products/                # Product images
└── src/
    ├── main.ts                          # Entry file
    ├── App.vue                          # Root component
    ├── api/                             # ===== API layer =====
    │   ├── index.ts                     # Axios instance + interceptor wrappers
    │   ├── user.ts                      # User APIs
    │   ├── product.ts                   # Product APIs
    │   ├── cart.ts                      # Cart APIs
    │   ├── order.ts                     # Order APIs
    │   ├── payment.ts                   # Payment APIs
    │   ├── address.ts                   # Address APIs
    │   ├── merchant.ts                  # Merchant APIs
    │   ├── admin.ts                     # Administrator APIs
    │   ├── refund.ts                    # Refund APIs
    │   ├── chat.ts                      # Chat APIs
    │   ├── search.ts                    # Search APIs
    │   ├── comment.ts                   # Comment APIs
    │   ├── seckill.ts                   # Flash-sale Coupon APIs
    │   └── upload.ts                    # Upload APIs
    ├── router/                          # ===== Route layer =====
    │   └── index.ts                     # Route configuration + guards + role metadata
    ├── config/                          # ===== Feature flags =====
    │   └── features.ts                  # CHAT_ENABLED / REFUND_AUDIT_ENABLED
    ├── stores/                          # ===== State management layer =====
    │   └── user.ts                      # User login state (token + userInfo)
    ├── views/                           # ===== Page view layer =====
    │   ├── home/                        # Home page (product list)
    │   │   └── HomeView.vue
    │   ├── product/                     # Product module
    │   │   ├── ProductDetailView.vue    # Product details
    │   │   └── SearchResultView.vue     # Search results
    │   ├── cart/                        # Cart
    │   │   └── CartView.vue
    │   ├── coupon/                      # Coupons
    │   │   └── CouponCenterView.vue     # Coupon Center
    │   ├── order/                       # Order module
    │   │   ├── CheckoutView.vue         # Checkout/Submit order
    │   │   ├── OrderListView.vue        # Order list
    │   │   ├── OrderDetailView.vue      # Order details
    │   │   ├── PaymentView.vue          # Payment page
    │   │   └── PaymentRecordView.vue    # Payment records
    │   ├── refund/                      # Refunds / after-sales
    │   │   └── RefundListView.vue       # My refunds/after-sales
    │   ├── user/                        # User module
    │   │   ├── LoginView.vue            # Login
    │   │   ├── RegisterView.vue         # Register
    │   │   ├── ForgotPasswordView.vue   # Forgot password
    │   │   ├── SettingsView.vue         # Account settings (/profile redirects here)
    │   │   └── AddressView.vue          # Shipping address management
    │   ├── merchant/                    # Merchant backend
    │   │   ├── MerchantApplyView.vue    # Onboarding application
    │   │   ├── ProductManageView.vue    # Product management
    │   │   ├── CommentManageView.vue    # Comment management
    │   │   ├── OrderManageView.vue      # Order management
    │   │   ├── RefundManageView.vue     # Refund/Return review
    │   │   └── SalesStatsView.vue       # Sales statistics
    │   ├── admin/                       # Administrator backend
    │   │   ├── UserManageView.vue       # User management
    │   │   ├── MerchantAuditView.vue    # Merchant review
    │   │   ├── CategoryManageView.vue   # Category management
    │   │   ├── ProductAllView.vue       # All-platform product management
    │   │   ├── OrderAllView.vue         # All-platform order management
    │   │   ├── SalesReportView.vue      # Global sales reports
    │   │   └── CouponManageView.vue     # Flash-sale Coupon management
    │   ├── chat/                        # Instant messaging
    │   │   └── ChatView.vue
    │   ├── common/                      # Common pages
    │   │   └── HelpView.vue             # Help center
    │   └── NotFound.vue                 # 404 page
    ├── components/                      # ===== Common component layer =====
    │   ├── layout/                      # Layout components
    │   │   ├── MallLayout.vue           # Mall layout (top navigation + category bar + content area + footer)
    │   │   ├── MerchantLayout.vue       # Merchant backend layout (sidebar + content area)
    │   │   └── AdminLayout.vue          # Administrator backend layout (sidebar + content area)
    │   ├── category/
    │   │   └── CategoryFlyout.vue       # Three-level category flyout panel
    │   └── chart/
    │       └── BaseChart.vue            # ECharts wrapper (on-demand imports)
    ├── utils/                           # ===== Utility functions =====
    │   ├── token.ts                     # Token storage (localStorage)
    │   ├── settings.ts                  # User preference settings (localStorage)
    │   ├── chatSocket.ts                # WebSocket client (singleton, auto-reconnect)
    │   ├── luhn.ts                      # Card formatting and Luhn/expiry/CVV validation
    │   ├── region.ts                    # Province/city/district data
    │   ├── sanitizeHtml.ts              # Product description HTML sanitizer
    │   ├── luhn.spec.ts                 # Luhn unit tests
    │   └── sanitizeHtml.spec.ts         # HTML sanitizer unit tests
    ├── styles/                          # ===== Global styles =====
    │   ├── variables.css                # CSS variable system (design tokens)
    │   └── global.css                   # Global base styles
    └── types/                           # ===== TypeScript type definitions =====
        ├── user.ts                      # UserInfo, LoginRequest, RegisterRequest
        ├── product.ts                   # Product, ProductSKU, Category
        ├── order.ts                     # Order, OrderItem, OrderStatus
        ├── cart.ts                      # CartItem, AddCartRequest
        ├── address.ts                   # Address
        ├── comment.ts                   # CommentItem, AddCommentRequest, etc.
        ├── coupon.ts                    # Coupon types
        └── common.ts                    # Result<T>, PageResult<T>, Role, OrderStatus
```

---

## 3. Multiple Independent Layout Designs

The system provides **three independent layouts** according to role, and automatically switches among them in the route guard based on the user role.

### 3.1 Mall Layout `MallLayout.vue`

Used for the **Guest** and **Registered User** perspective, with an e-commerce style:

```
┌────────────────────────────────────────────────────────────────────────┐
│  Logo    Search bar [________________] 🔍   Cart                        │  ← Top navigation
├────────────────────────────────────────────────────────────────────────┤
│  All Categories  │  Level-1 Category  │  Level-2 Category  │ ...       │  ← Category navigation bar
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│                       <router-view />                                  │  ← Product list/details/search results
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│        © 2025 Online Supermarket. All rights reserved.                 │  ← Footer
└────────────────────────────────────────────────────────────────────────┘
```

- Not logged in: display the "Login/Register" button.
- Logged in: display the user avatar and nickname; the dropdown menu contains "My Orders", "Shipping Address", and "Logout".

### 3.2 Merchant Backend Layout `MerchantLayout.vue`

Used for the **Merchant** role, with an admin panel style:

```
┌────────────────────────┬────────────────────────────────────────────┐
│  Shop Name             │                                            │
│                        │                                            │
│  Product Management    │              <router-view />               │
│  Comment Management    │                                            │
│  Order Management      │                                            │
│  Refund Review         │                                            │
│  Sales Statistics      │                                            │
└────────────────────────┴────────────────────────────────────────────┘
```

- Fixed sidebar on the left.
- The content area displays the corresponding management page.

### 3.3 Administrator Backend Layout `AdminLayout.vue`

Used for the **Administrator** role; the structure is similar to the merchant backend:

```
┌────────────────────────┬────────────────────────────────────────────┐
│  Administrator Panel   │                                            │
│                        │                                            │
│  User Management       │              <router-view />               │
│  Merchant Review       │                                            │
│  Category Management   │                                            │
│  Product Management    │                                            │
│  Order Management      │                                            │
│  Sales Reports         │                                            │
└────────────────────────┴────────────────────────────────────────────┘
```

### 3.4 Layout Switching Logic

In `App.vue`, the layout component is dynamically selected based on the `meta.layout` field of the current route:

```typescript
// Route configuration example in router/index.ts
{
  path: '/',
  component: MallLayout,
  meta: { layout: 'mall' },
  children: [
    { path: '', component: HomeView },
    { path: 'product/:id', component: ProductDetailView },
    // ...
  ]
},
{
  path: '/merchant',
  component: MerchantLayout,
  meta: { layout: 'merchant', roles: ['MERCHANT'] },
  children: [
    { path: 'products', component: ProductManageView },
    // ...
  ]
},
{
  path: '/admin',
  component: AdminLayout,
  meta: { layout: 'admin', roles: ['ADMIN'] },
  children: [
    { path: 'users', component: UserManageView },
    // ...
  ]
}
```

---

## 4. Axios Wrapper and Interceptors

### 4.1 Axios Instance `api/index.ts`

```typescript
import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/token'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Request interceptor: attach the token automatically
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// Response interceptor: handle business codes and 401 uniformly
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || 'Request failed')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || 'Network error'
    if (status === 401) {
      removeToken()
      router.push('/login')
      ElMessage.error('Session expired, please sign in again')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export default service
```

### 4.2 API Module Example `api/cart.ts`

```typescript
import service from '@/api'
import type { CartItem, AddCartRequest, UpdateCartRequest, SelectCartRequest } from '@/types/cart'

// Get the cart list
export const getCartList = () => {
  return service.get<any, { code: number; message: string; data: CartItem[]; timestamp: number }>('/cart/list')
}

// Add an item to the cart
export const addToCart = (data: AddCartRequest) => {
  return service.post('/cart/add', data)
}

// Update the cart item quantity
export const updateCartQuantity = (data: UpdateCartRequest) => {
  return service.put('/cart/update', data)
}

// Remove a cart item
export const removeFromCart = (cartItemId: number) => {
  return service.delete(`/cart/${cartItemId}`)
}

// Select/deselect cart items
export const selectCartItems = (data: SelectCartRequest) => {
  return service.put('/cart/select', data)
}

// Select all/deselect all
export const selectAllCart = (selected: number) => {
  return service.put('/cart/select-all', { selected })
}
```

---

## 5. Route Guards and Permission Control

### 5.1 Static Routes + Role Metadata `router/index.ts`

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// Route meta type extension
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    roles?: string[]        // List of roles allowed to access: 'USER' | 'MERCHANT' | 'ADMIN'
    layout?: 'mall' | 'merchant' | 'admin'
    public?: boolean
  }
}

// Public routes (no login required): mall browsing + login/registration
const publicRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/user/LoginView.vue'), meta: { title: 'Login' } },
  { path: '/register', name: 'Register', component: () => import('@/views/user/RegisterView.vue'), meta: { title: 'Register' } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/user/ForgotPasswordView.vue'), meta: { title: 'Forgot Password' } },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/product/ProductDetailView.vue'), meta: { title: 'Product Details' } },
]

// Mall layout routes (public children such as home/search/coupons + children requiring login)
const userRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/MallLayout.vue'),
    meta: { layout: 'mall' },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/home/HomeView.vue'), meta: { title: 'Home' } },
      { path: 'coupons', name: 'CouponCenter', component: () => import('@/views/coupon/CouponCenterView.vue'), meta: { title: 'Coupon Center' } },
      { path: 'search', name: 'SearchResult', component: () => import('@/views/product/SearchResultView.vue'), meta: { title: 'Search Results' } },
      { path: 'cart', name: 'Cart', meta: { title: 'Cart', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/cart/CartView.vue') },
      { path: 'checkout', name: 'Checkout', meta: { title: 'Checkout', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/CheckoutView.vue') },
      { path: 'orders', name: 'OrderList', meta: { title: 'My Orders', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/OrderListView.vue') },
      { path: 'payments', name: 'PaymentRecords', meta: { title: 'Payment Records', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/PaymentRecordView.vue') },
      { path: 'order/:id', name: 'OrderDetail', meta: { title: 'Order Details', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/OrderDetailView.vue') },
      { path: 'payment/:orderId', name: 'Payment', meta: { title: 'Payment', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/PaymentView.vue') },
      { path: 'merchant/apply', name: 'MerchantApply', meta: { title: 'Merchant Onboarding', roles: ['USER', 'MERCHANT'] }, component: () => import('@/views/merchant/MerchantApplyView.vue') },
      { path: 'refunds', name: 'RefundList', meta: { title: 'Refunds / After-sales', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/refund/RefundListView.vue') },
      { path: 'profile', name: 'Profile', redirect: '/settings' },
      { path: 'settings', name: 'Settings', meta: { title: 'Account Settings', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/user/SettingsView.vue') },
      { path: 'address', name: 'Address', meta: { title: 'Shipping Address', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/user/AddressView.vue') },
      { path: 'chat/:conversationId?', name: 'Chat', meta: { title: 'Chat', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/chat/ChatView.vue') },
      { path: 'help/:type', name: 'Help', meta: { title: 'Help Center' }, component: () => import('@/views/common/HelpView.vue') },
    ]
  },
]

// Merchant backend routes
const merchantRoutes: RouteRecordRaw[] = [
  {
    path: '/merchant',
    component: () => import('@/components/layout/MerchantLayout.vue'),
    meta: { layout: 'merchant', roles: ['MERCHANT'] },
    children: [
      { path: 'products', name: 'ProductManage', meta: { title: 'Product Management' }, component: () => import('@/views/merchant/ProductManageView.vue') },
      { path: 'comments', name: 'MerchantComments', meta: { title: 'Comment Management' }, component: () => import('@/views/merchant/CommentManageView.vue') },
      { path: 'orders', name: 'MerchantOrders', meta: { title: 'Order Management' }, component: () => import('@/views/merchant/OrderManageView.vue') },
      { path: 'refunds', name: 'RefundManage', meta: { title: 'Refund Review' }, component: () => import('@/views/merchant/RefundManageView.vue') },
      { path: 'stats', name: 'SalesStats', meta: { title: 'Sales Statistics' }, component: () => import('@/views/merchant/SalesStatsView.vue') },
    ]
  },
]

// Administrator backend routes
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { layout: 'admin', roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'dashboard', redirect: '/admin/users' },
      { path: 'users', name: 'UserManage', meta: { title: 'User Management' }, component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'merchants', name: 'MerchantAudit', meta: { title: 'Merchant Review' }, component: () => import('@/views/admin/MerchantAuditView.vue') },
      { path: 'categories', name: 'CategoryManage', meta: { title: 'Category Management' }, component: () => import('@/views/admin/CategoryManageView.vue') },
      { path: 'products', name: 'ProductAll', meta: { title: 'Product Management' }, component: () => import('@/views/admin/ProductAllView.vue') },
      { path: 'orders', name: 'OrderAll', meta: { title: 'Order Management' }, component: () => import('@/views/admin/OrderAllView.vue') },
      { path: 'refunds', name: 'AdminRefundAudit', meta: { title: 'Refund Review' }, component: () => import('@/views/merchant/RefundManageView.vue') },
      { path: 'report', name: 'SalesReport', meta: { title: 'Sales Reports' }, component: () => import('@/views/admin/SalesReportView.vue') },
      { path: 'coupons', name: 'CouponManage', meta: { title: 'Flash-sale Coupon Management' }, component: () => import('@/views/admin/CouponManageView.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...publicRoutes,
    ...userRoutes,
    ...merchantRoutes,
    ...adminRoutes,
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue'), meta: { title: 'Page Not Found' } },
  ],
})
```

### 5.2 Global Before Guard `beforeEach`

```typescript
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'Online Supermarket'} - Chrono BBS`
  const userStore = useUserStore()

  // Logged-in users cannot access the login/registration pages
  if (to.path === '/login' || to.path === '/register') {
    if (userStore.token) return next('/home')
    return next()
  }

  const roles = to.meta.roles
  if (!roles || roles.length === 0) {
    return next()
  }

  if (!userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (!roles.includes(userStore.roleName)) {
    ElMessage.warning('Access denied')
    return next('/home')
  }

  next()
})
```

---

## 6. Pinia State Management

### 6.1 User State `stores/user.ts`

Currently only the user state store is implemented, managing the persistence of the token and user information:

```typescript
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { Role } from '@/types/common'
import { getToken, removeToken, setToken as persistToken } from '@/utils/token'

const USER_INFO_KEY = 'user_info'

const loadUserInfo = (): UserInfo | null => {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    return raw ? (JSON.parse(raw) as UserInfo) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(loadUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)
  const roleName = computed(() => {
    switch (role.value) {
      case Role.USER:
        return 'USER'
      case Role.MERCHANT:
        return 'MERCHANT'
      case Role.ADMIN:
        return 'ADMIN'
      default:
        return ''
    }
  })

  function setToken(t: string) {
    token.value = t
    persistToken(t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    localStorage.removeItem(USER_INFO_KEY)
  }

  return { token, userInfo, isLoggedIn, role, roleName, setToken, setUserInfo, logout }
})
```

> **Note**: State such as the cart and chat is currently obtained and managed directly in each page component by calling APIs, and has not been extracted into separate Pinia stores. Role checks uniformly use the `Role` enum (USER=1, MERCHANT=2, ADMIN=3), and role name strings are uppercase `'USER'` / `'MERCHANT'` / `'ADMIN'`.

---

## 7. Instant Messaging

> **Implementation status**: The chat entry is enabled (`CHAT_ENABLED = true`). `npm run dev` and production connect directly to the backend WebSocket through `src/utils/chatSocket.ts`, supporting real-time send/receive, live unread badges and automatic reconnection. Only `npm run dev:mock` uses the Mock HTTP APIs.

### 7.1 Backend WebSocket Contract

The backend uses JSR 356 annotations (`@OnOpen` / `@OnClose` / `@OnMessage`). The connection URL format is (same origin as the current page; the protocol automatically switches between `ws`/`wss`, and the token is passed as a query parameter):

```
/chat/{userId}?token={jwt}
```

Message format (JSON):

```json
{
  "type": "TEXT",
  "conversationId": 123,
  "messageId": 1001,
  "senderId": 10,
  "senderRole": 0,
  "content": "Hello, is this product in stock?",
  "productId": 456,
  "isRead": 0,
  "timestamp": 1700000000000
}
```

### 7.2 Frontend Wrapper `utils/chatSocket.ts`

A global `chatSocket` singleton is held by the mall layout and shared with the chat page:

- `connect(userId, token)` opens `/chat/{userId}?token=...` against the current origin and reuses an existing connection;
- `send({ conversationId, content })` sends a `TEXT` frame when the connection is ready and returns `false` otherwise;
- `onFrame(listener)` / `onStatus(listener)` subscribe to messages and connection state, returning an unsubscribe function;
- `disconnect()` closes the connection on logout or when leaving the mall layout;
- Reconnection uses exponential backoff (1s, 2s, 4s, ... capped at 30s); a 1008 (authentication failure) close stops reconnection until the user logs in again;
- Vite and Nginx both proxy `/chat/` WebSocket traffic, so the frontend never hardcodes the backend port.

---

## 8. Product Search (Backend API)

> **Implementation status**: The search page integrates the APIs in this section (Elasticsearch is used internally by the backend; the frontend does not connect to ES directly); see `api/search.ts`. When the backend APIs are unavailable, the page falls back to local filtering over `/api/product/list` so guests can still search.

### 8.1 API List

| API | Method | Path | Description |
|------|------|------|------|
| Full-text search | GET | `/api/search` | Keyword + category/brand/price range filtering + sorting + pagination |
| Search suggestions | GET | `/api/search/suggest` | Keyword autocomplete |
| Search history | GET | `/api/search/history` | Last 20 records of the logged-in user |
| Clear search history | DELETE | `/api/search/history` | Clear search history |
| Hot search ranking | GET | `/api/search/hot` | Search frequency over the last 7 days |

### 8.2 Search Parameters `api/search.ts`

```typescript
import service from '@/api'

interface SearchResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface SearchParams {
  keyword: string
  categoryId?: number
  brand?: string
  minPrice?: number
  maxPrice?: number
  sort?: 'sales_desc' | 'price_asc' | 'price_desc' | 'time_desc'
  page?: number
  size?: number
}

// Search result item
export interface SearchItem {
  id: number
  name: string
  description?: string
  brand?: string
  categoryId?: number
  categoryName?: string
  minPrice: number
  sales: number
  image?: string
}

// Paginated search result structure
export interface SearchResult {
  total: number
  page: number
  size: number
  list: SearchItem[]
}

export const searchProducts = (params: SearchParams) =>
  service.get<any, SearchResponse<SearchResult>>('/search', { params })

export const getSearchSuggest = (keyword: string) =>
  service.get<any, SearchResponse<string[]>>('/search/suggest', { params: { keyword } })

export const getSearchHistory = () =>
  service.get<any, SearchResponse<string[]>>('/search/history')

export const clearSearchHistory = () =>
  service.delete<any, SearchResponse<null>>('/search/history')

export const getHotSearch = () =>
  service.get<any, SearchResponse<string[]>>('/search/hot')
```

> Note: The old "frontend directly connects to ES (`/es` proxy)" approach has been deprecated; search logic is now uniformly encapsulated by the backend `SearchController`.

---

## 9. Image Upload (Frontend → Backend → Local Directory / OSS)

### 9.1 Upload API `api/upload.ts` and Inline Page Usage

> The frontend does not extract a standalone `ImageUpload` component. Pages (product management, account settings, category management, order details, etc.) use `el-upload` inline and upload manually through `api/upload.ts`. The authorization header is injected by the Axios interceptor and does not need to be set manually.

Upload API wrapper:

```typescript
// api/upload.ts
import service from '@/api'

export interface UploadImageResponse {
  url: string
}

export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return service.post<any, { code: number; message: string; data: UploadImageResponse; timestamp: number }>(
    '/upload/image',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
}
```

Inline usage in a page (product management `ProductManageView.vue` as an example):

```vue
<el-upload
  v-model:file-list="imageFileList"
  list-type="picture-card"
  :auto-upload="false"
  :limit="4"
  :on-change="handleImageChange"
  :on-remove="handleImageRemove"
>
  <el-icon><Plus /></el-icon>
</el-upload>
```

```typescript
import { uploadImage } from '@/api/upload'
import type { UploadUserFile } from 'element-plus'

const handleImageChange = async (file: UploadUserFile) => {
  if (!file.raw) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)) {
    ElMessage.warning('Only jpg/jpeg/png/webp formats are supported')
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('Image size cannot exceed 2MB')
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  try {
    const res = await uploadImage(file.raw)
    file.url = res.data.url
    syncImageUrls()
  } catch {
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
  }
}
```

---

## 10. Payment Module (Frontend Simulation)

### 10.1 Card Payment

- The user selects "Card Payment" on the payment page.
- Enter the card number, expiry date (MM/YY), and CVV.
- **Frontend pre-validation**:
  - Card number format (16-19 digits) and Luhn algorithm validation
  - Expiry date is not in the past
  - CVV is 3 or 4 digits
- After validation passes, call `POST /api/payment/card`.

### 10.2 Luhn Algorithm `utils/luhn.ts`

```typescript
export function luhnCheck(cardNo: string): boolean {
  const digits = cardNo.replace(/\D/g, '')
  if (digits.length < 13 || digits.length > 19) return false

  let sum = 0
  let alternate = false
  for (let i = digits.length - 1; i >= 0; i--) {
    let n = parseInt(digits[i])
    if (alternate) {
      n *= 2
      if (n > 9) n -= 9
    }
    sum += n
    alternate = !alternate
  }
  return sum % 10 === 0
}
```

### 10.3 QR Code Payment

- The user selects "QR Code Payment".
- The frontend calls `POST /api/payment/qrcode` to obtain the QR code URL.
- Display the QR code image (actually shows an order information summary + amount).
- The user clicks "Confirm Payment" → `POST /api/payment/confirm-qrcode`.
- After successful payment, redirect to the order details page.

---

## 11. Duplicate Submission Prevention Token

### 11.1 Order Placement Flow

```
User clicks "Submit Order"
  → GET /api/order/submit-token   (obtain the anti-duplicate token, valid for 5 minutes)
  → POST /api/order/create        (carry the token + address + selected cart item IDs)
  → On success, redirect to the payment page
```

### 11.2 Frontend Implementation

```typescript
// Checkout page (CheckoutView.vue)
const loadData = async () => {
  // Fetch the cart, shipping addresses and anti-duplicate token in parallel
  const [cartRes, addrRes, tokenRes] = await Promise.all([
    getCartList(),
    getAddressList(),
    getSubmitToken(),
  ])
  cartItems.value = (cartRes.data || []).filter(i => i.selected === 1)
  addresses.value = addrRes.data || []
  submitToken.value = tokenRes.data.token
  // ...
}

const submitOrder = async () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('No products selected')
    return
  }
  submitting.value = true
  try {
    // Submit with the anti-duplicate token + address + selected cart item IDs
    const res = await createOrder({
      token: submitToken.value,
      addressId: selectedAddress.value.id,
      cartItemIds: cartItems.value.map(i => i.id),
      remark: remark.value || undefined,
      userCouponId: selectedCouponId.value ?? undefined,
    })
    ElMessage.success('Order created successfully')
    router.replace(`/payment/${res.data.orderId}`)
  } catch {
    ElMessage.error('Failed to create the order')
  } finally {
    submitting.value = false
  }
}
```

---

## 12. Nginx Deployment Configuration

### 12.1 Nginx Configuration Example

```nginx
server {
    listen 80;
    server_name localhost;

    # Frontend static resources
    root /usr/share/nginx/html;
    index index.html;

    # Product images up to 2MB each; total request size 10MB
    client_max_body_size 10m;

    # History mode fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API to the backend Spring Boot (including search /api/search)
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 60s;
    }

    # Long-term cache for build artifacts
    location /assets/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800, immutable";
    }

    # Chat WebSocket proxy
    location /chat/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }
}
```

> For the complete deployable configuration, see [deploy/nginx.conf](../../deploy/nginx.conf) in the repository root.

---

## 13. Page Route Overview

| Path | Page | Role | Description |
|------|------|------|------|
| `/` | Home/Product List | Guest/User/Merchant/Administrator | Category browsing, search entry |
| `/product/:id` | Product Details | Guest/User/Merchant/Administrator | Includes SKU selection and comment list |
| `/search` | Search Results | Guest/User/Merchant/Administrator | Keyword full-text search results |
| `/coupons` | Coupon Center | Guest/User/Merchant/Administrator | Guests can browse; claiming requires login |
| `/login` | Login | Guest | Username, password + captcha |
| `/register` | Register | Guest | Select role |
| `/forgot-password` | Forgot Password | Guest | Reset password |
| `/cart` | Cart | User/Merchant | Login required |
| `/checkout` | Checkout | User/Merchant | Confirm address, products, amount |
| `/payment/:orderId` | Payment | User/Merchant | Card/QR code payment |
| `/orders` | My Orders | User/Merchant | Order list |
| `/payments` | Payment Records | User/Merchant/Administrator | Payment method, amount and time |
| `/order/:id` | Order Details | User/Merchant | Includes refund/return entry |
| `/refunds` | My Refunds/After-sales | User/Merchant/Administrator | Refund/return request list |
| `/profile` | Personal Information | User/Merchant/Administrator | Redirects to `/settings` |
| `/settings` | Account Settings | User/Merchant/Administrator | Preferences + avatar upload |
| `/address` | Shipping Address | User/Merchant | Create, read, update, delete |
| `/chat/:conversationId?` | Chat | User/Merchant | WebSocket instant messaging |
| `/help/:type` | Help Center | Guest/User | Static help page |
| `/merchant/apply` | Merchant Onboarding | User | Submit onboarding application |
| `/merchant/products` | Product Management | Merchant | Create, read, update, delete; list/unlist |
| `/merchant/comments` | Comment Management | Merchant | View and reply to comments/follow-up comments by product |
| `/merchant/orders` | Order Management | Merchant | Shipping operations |
| `/merchant/refunds` | Refund Review | Merchant | Review refunds and returns |
| `/merchant/stats` | Sales Statistics | Merchant | Own sales data |
| `/admin/users` | User Management | Administrator | Enable/disable |
| `/admin/merchants` | Merchant Review | Administrator | Review onboarding applications |
| `/admin/categories` | Category Management | Administrator | Create, read, update, and delete three-level categories |
| `/admin/products` | Product Management | Administrator | Global product management |
| `/admin/orders` | Order Management | Administrator | Global order management |
| `/admin/refunds` | Refund Review | Administrator | Review refunds/returns across all merchants |
| `/admin/report` | Sales Reports | Administrator | Global sales statistics |
| `/admin/coupons` | Flash-sale Coupon Management | Administrator | Create, read, update, and delete coupons |

---

## 14. Summary of Backend Data Interactions

| Frontend Operation | HTTP Method | API Path | Request Body/Parameters |
|----------|-----------|----------|-------------|
| CAPTCHA | GET | `/api/user/captcha` | — |
| Register | POST | `/api/user/register` | `{ username, password, phone, email, role, shopName, captchaUuid, captchaCode }` (`role=2` and `shopName` required for merchant registration) |
| Login | POST | `/api/user/login` | `{ username, password, captchaUuid, captchaCode }` |
| Get user information | GET | `/api/user/info` | — |
| Update profile | PUT | `/api/user/profile` | `{ nickname, phone, email, avatar }` |
| Change password | PUT | `/api/user/password` | `{ oldPassword, newPassword }` |
| Log out | POST | `/api/user/logout` | — |
| Get password reset verification code | POST | `/api/user/forgot-code` | `{ account, captchaUuid, captchaCode }` |
| Reset password | POST | `/api/user/reset-password` | `{ uuid, code, newPassword }` |
| Product list | GET | `/api/product/list` | `?categoryId=&page=&size=` |
| Product details | GET | `/api/product/{id}` | — |
| Category tree | GET | `/api/category/tree` | — |
| Child category list | GET | `/api/category/children` | `?parentId=` |
| Search | GET | `/api/search` | `?keyword=&categoryId=&brand=&minPrice=&maxPrice=&sort=&page=&size=` |
| Search suggestions | GET | `/api/search/suggest` | `?keyword=` |
| Search history | GET | `/api/search/history` | — |
| Clear search history | DELETE | `/api/search/history` | — |
| Hot search ranking | GET | `/api/search/hot` | — |
| Get cart | GET | `/api/cart/list` | — |
| Add to cart | POST | `/api/cart/add` | `{ productId, skuId, quantity }` |
| Update cart quantity | PUT | `/api/cart/update` | `{ cartItemId, quantity }` |
| Delete cart item | DELETE | `/api/cart/{cartItemId}` | — |
| Select/deselect cart items | PUT | `/api/cart/select` | `{ cartItemIds, selected }` |
| Select all/deselect all | PUT | `/api/cart/select-all` | `{ selected }` |
| Get anti-duplicate token | GET | `/api/order/submit-token` | — |
| Create order | POST | `/api/order/create` | `{ token, addressId, cartItemIds, remark?, userCouponId? }` |
| Order list | GET | `/api/order/list` | `?status=&page=&size=` |
| Order details | GET | `/api/order/{id}` | — |
| Cancel order | PUT | `/api/order/{id}/cancel` | `{ reason }` |
| Confirm receipt | PUT | `/api/order/{id}/receive` | — |
| Delete order | DELETE | `/api/order/{id}` | — |
| Card payment | POST | `/api/payment/card` | `{ orderId, cardNo, expireDate, cvv }` |
| Get payment QR code | POST | `/api/payment/qrcode` | `{ orderId }` |
| Confirm QR code payment | POST | `/api/payment/confirm-qrcode` | `{ orderId }` |
| Payment records | GET | `/api/payment/records` | `?page=&size=` |
| Upload image | POST | `/api/upload/image` | FormData(file) |
| Address list | GET | `/api/address/list` | — |
| Address details | GET | `/api/address/{id}` | — |
| Create address | POST | `/api/address` | `{ receiverName, phone, province, city, district, detail, isDefault }` |
| Update address | PUT | `/api/address/{id}` | Same as above |
| Delete address | DELETE | `/api/address/{id}` | — |
| Set default address | PUT | `/api/address/{id}/default` | — |
| Comment list | GET | `/api/comment/list/{productId}` | `?page=&size=&star=` |
| Post comment | POST | `/api/comment` | `{ productId, orderItemId, content, star, images }` |
| Append comment | POST | `/api/comment/append` | `{ commentId, content }` |
| Merchant reply | POST | `/api/comment/reply` | `{ parentId, content }` |
| Request refund | POST | `/api/refund` | `{ orderItemId, type, reason, amount }` |
| My refund list | GET | `/api/refund/my` | `?page=&size=` |
| Refund details | GET | `/api/refund/{id}` | — |
| Review refund | PUT | `/api/refund/{id}/review` | `{ approved, reviewReason }` |
| Submit return tracking | PUT | `/api/refund/{id}/return` | `{ returnCompany, returnLogisticsNo }` |
| Confirm return receipt | PUT | `/api/refund/{id}/return-received` | — |
| Conversation list | GET | `/api/chat/conversations` | — |
| Message history | GET | `/api/chat/history/{conversationId}` | `?page=&size=` |
| Send message | POST | `/api/chat/message` | `{ conversationId, content, type }` (Mock debugging only) |
| Create conversation | POST | `/api/chat/conversation` | `{ merchantId, productId }` |
| Mark as read | PUT | `/api/chat/read/{conversationId}` | — |
| Unread count | GET | `/api/chat/unread-count` | — |
| Flash-sale coupon list | GET | `/api/seckill/list` | `?page=&size=` |
| Flash-sale coupon details | GET | `/api/seckill/detail/{id}` | — |
| Claim flash-sale coupon | POST | `/api/seckill/claim/{couponId}` | — |
| My coupons | GET | `/api/seckill/my-coupons` | `?page=&size=` |
| Create flash-sale coupon | POST | `/api/seckill/create` | `{ name, ... }` |
| Update flash-sale coupon | PUT | `/api/seckill/update/{id}` | Same as above |
| Delete flash-sale coupon | DELETE | `/api/seckill/{id}` | — |
| Calculate order discount | POST | `/api/seckill/calculate` | `{ ... }` |
| Merchant onboarding application | POST | `/api/merchant/apply` | `{ shopName, contactPhone, businessLicense }` |
| Merchant information | GET | `/api/merchant/info` | — |
| Merchant refund list | GET | `/api/merchant/refunds` | `?page=&size=&status=` |
| Merchant product list | GET | `/api/merchant/products` | `?page=&size=&status=` |
| Merchant order list | GET | `/api/merchant/orders` | `?page=&size=&status=` |
| Ship order | PUT | `/api/merchant/ship/{orderId}` | — |
| Merchant sales statistics | GET | `/api/merchant/stats` | `?startDate=&endDate=` |
| Create product | POST | `/api/product` | `{ ...ProductFormData }` |
| Update product | PUT | `/api/product/{id}` | Same as above |
| List/unlist product | PUT | `/api/product/{id}/status` | `{ status }` |
| Delete product | DELETE | `/api/product/{id}` | — |
| User list | GET | `/api/admin/users` | `?page=&size=&keyword=&role=&status=` |
| Set user status | PUT | `/api/admin/users/{id}/status` | `{ status }` |
| Merchant list | GET | `/api/admin/merchants` | `?status=&page=&size=` |
| Review merchant | PUT | `/api/admin/merchants/{id}/review` | `{ approved, reviewReason }` |
| Create category | POST | `/api/admin/categories` | `{ name, parentId, level, sortOrder, icon }` |
| Update category | PUT | `/api/admin/categories/{id}` | Same as above |
| Delete category | DELETE | `/api/admin/categories/{id}` | — |
| Admin product list | GET | `/api/admin/products` | `?page=&size=&keyword=&categoryId=&merchantId=&status=` |
| Admin order list | GET | `/api/admin/orders` | `?page=&size=&orderNo=&status=` |
| Admin sales report | GET | `/api/admin/report` | `?startDate=&endDate=&groupBy=` |
| Admin refund list | GET | `/api/admin/refunds` | `?page=&size=&status=` |
| Admin update product | PUT | `/api/admin/products/{id}` | `{ ...ProductFormData }` |
| Admin list/unlist product | PUT | `/api/admin/products/{id}/status` | `{ status }` |
| Admin delete product | DELETE | `/api/admin/products/{id}` | — |
| WebSocket connection | WS | `/chat/{userId}?token={jwt}` | — |


---

## 15. Frontend Enhancements (Current Implementation)

All items below are connected to the real backend; `npm run dev:mock` can still demo them without starting the backend.

### 15.1 Multi-SKU Management for Merchants

- The product create/edit dialog supports multiple specification groups (name + comma-separated values) and generates the cartesian product of SKUs.
- Each SKU has its own price and stock; the payload maps to `ProductCreateDTO.skuList`.
- When editing, specification groups are reconstructed from the existing `specText`, preserving SKU prices and stock.
- Files: `src/views/merchant/ProductManageView.vue`, `mock/product.ts`.

### 15.2 Report Charts

- ECharts 6 is imported on demand and wrapped by `src/components/chart/BaseChart.vue`.
- Admin sales report: line chart for trend, bar charts for top products and merchants, pie chart for categories.
- Merchant sales stats: line chart for trend, bar chart for top products, pie chart for categories.
- Charts and detail tables are shown together; amounts are displayed in yuan.

### 15.3 Card Payment Pre-validation

- `src/utils/luhn.ts` provides card formatting, Luhn validation, MM/YY expiry validation and CVV validation.
- The cashier page formats input automatically and validates before submission; the backend still performs the final check.

### 15.4 Search Filters and Three-level Categories

- Search results support cascading category, brand and price-range filters, with reset and category path display.
- Search and filtering call `/api/search` (Elasticsearch) first; when the API is unavailable, they fall back to local filtering over products loaded from `/api/product/list`.
- The top navigation and home sidebar show hover flyouts for selecting level-2/level-3 categories.
- Files: `src/views/product/SearchResultView.vue`, `src/components/category/CategoryFlyout.vue`.

### 15.5 Rich Text Description and Specifications

- The product detail tab renders sanitized rich text (`src/utils/sanitizeHtml.ts`), removing scripts and event attributes.
- The specifications tab shows brand, category, shop, sales and all SKU prices/stocks.

### 15.6 Home Product Pagination

- "Guess You Like" loads 10 items per page with pagination; "Hot Recommendations" shows the top 10 by sales.

### 15.7 Mock Debug Mode

- `npm run dev` uses the real backend; `npm run dev:mock` uses the `mock/` directory. They are independent.
- Mock product create/edit persists multiple SKUs and recalculates min/max prices; the admin report fields match `AdminReportVO`.

### 15.8 Chat Integration

- The mall header message entry and "My Messages" menu item are enabled by default; the unread badge calls `GET /api/chat/unread-count`.
- The chat page supports conversation list, history, real-time send/receive and marking as read; `npm run dev` and production connect through WebSocket, while `dev:mock` uses `POST /api/chat/message`.
- The product detail shop card adds "Contact Support": after login it calls `POST /api/chat/conversation` to create or reuse a conversation and opens the chat page.
- The unread badge refreshes on real-time WebSocket messages and clears automatically when a conversation is opened.

### 15.9 Refund Review (Merchant/Admin)

- The merchant "Refund Review" and admin "Refund Review" pages reuse `RefundManageView.vue` and select the data source by route prefix.
- The list supports status filtering, approve/reject actions and a rejection reason; rows update immediately after review.
- List APIs: `GET /api/merchant/refunds`, `GET /api/admin/refunds`; review reuses `PUT /api/refund/{id}/review`. Mock provides the same paths in `dev:mock`.

### 15.10 Admin Product Operations

- The global product list supports on/off-shelf toggling and deletion with confirmation prompts.
- APIs: `PUT /api/admin/products/{id}/status`, `DELETE /api/admin/products/{id}`; Mock provides the same paths in `dev:mock`.

### 15.11 Search API Integration and Fallback

- When a keyword is present, the page calls `/api/search` (category/brand/price/sort/pagination); suggestions, history and hot keywords use APIs 15.2-15.5.
- Category-only browsing or filters without a keyword use local filtering over `/api/product/list`; the same fallback applies when search APIs are unavailable.
- Mock expands category filters to all descendants, matching the three-level navigation behavior.

### 15.12 Payment Records and Order Trend

- Added the `/payments` page showing payment method, masked card number, amount, status and time; the backend provides `GET /api/payment/records`.
- The admin sales report adds an order-count trend chart (created/paid/completed); `AdminReportVO.orderTrend` and Mock data are synchronized.
- Search sorting adds "listing time"; category navigation renders `category.icon` when available.

### 15.13 Return Shipping, Admin Editing and Comment Images

- After a return refund is approved it enters "Awaiting Return"; the buyer submits the courier company and tracking number on the refunds/after-sales page, and the merchant/admin confirms receipt on the refund review page to complete the refund.
- The admin global product list supports editing products and SKU price/stock; the backend provides `PUT /api/admin/products/{id}`.
- Posting a review supports up to 4 images, which are displayed in the product detail comment list.

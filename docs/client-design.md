# 网上超市系统 - 客户端概要设计

---

## 一、技术选型

| 类别 | 技术 | 版本/说明 |
|------|------|-----------|
| 核心框架 | Vue 3 | Composition API |
| 开发语言 | TypeScript | 强类型约束 |
| 构建工具 | Vite | 开发热更新 + 生产 Rollup 打包 |
| UI 组件库 | Element Plus | Vue 3 官方推荐 |
| 状态管理 | Pinia | Vue 3 官方推荐，TS 友好 |
| HTTP 客户端 | Axios | 拦截器统一处理 Token / 错误 |
| CSS 方案 | Scoped CSS | Vue SFC 原生样式隔离 |
| 路由 | Vue Router 4 | History 模式 |
| WebSocket | 原生 WebSocket API | 对接后端 JSR 356 `@OnOpen` |
| 搜索 | 前端直连 Elasticsearch REST API | 减轻后端压力 |

---

## 二、项目目录结构（混合方式）

```
chrono-bbs-client/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts                          # 入口文件
    ├── App.vue                          # 根组件
    ├── api/                             # ===== API 接口层 =====
    │   ├── index.ts                     # Axios 实例 + 拦截器封装
    │   ├── user.ts                      # 用户相关接口
    │   ├── product.ts                   # 商品相关接口
    │   ├── category.ts                  # 分类相关接口
    │   ├── cart.ts                      # 购物车接口
    │   ├── order.ts                     # 订单接口
    │   ├── payment.ts                   # 支付接口
    │   ├── address.ts                   # 地址接口
    │   ├── comment.ts                   # 评论接口
    │   ├── merchant.ts                  # 商家接口
    │   ├── admin.ts                     # 管理员接口
    │   ├── chat.ts                      # 聊天接口
    │   └── search.ts                    # ES 搜索接口（直连）
    ├── router/                          # ===== 路由层 =====
    │   └── index.ts                     # 路由配置 + 守卫 + 角色元信息
    ├── stores/                          # ===== 状态管理层 =====
    │   ├── user.ts                      # 用户登录态
    │   ├── cart.ts                      # 购物车状态
    │   └── chat.ts                      # 未读消息
    ├── views/                           # ===== 页面视图层 =====
    │   ├── home/                        # 首页（商品列表）
    │   │   └── HomeView.vue
    │   ├── product/                     # 商品模块
    │   │   ├── ProductDetailView.vue    # 商品详情
    │   │   └── SearchResultView.vue     # 搜索结果
    │   ├── cart/                        # 购物车
    │   │   └── CartView.vue
    │   ├── order/                       # 订单模块
    │   │   ├── CheckoutView.vue         # 结算/提交订单
    │   │   ├── OrderListView.vue        # 订单列表
    │   │   ├── OrderDetailView.vue      # 订单详情
    │   │   └── PaymentView.vue          # 支付页面
    │   ├── user/                        # 用户模块
    │   │   ├── LoginView.vue            # 登录
    │   │   ├── RegisterView.vue         # 注册
    │   │   ├── ForgotPasswordView.vue   # 忘记密码
    │   │   ├── ProfileView.vue          # 个人信息
    │   │   └── AddressView.vue          # 收货地址管理
    │   ├── merchant/                    # 商家后台
    │   │   ├── MerchantApplyView.vue    # 入驻申请
    │   │   ├── ProductManageView.vue    # 商品管理
    │   │   ├── OrderManageView.vue      # 订单管理
    │   │   ├── RefundManageView.vue     # 退款/退货审核
    │   │   └── SalesStatsView.vue       # 销售统计
    │   ├── admin/                       # 管理员后台
    │   │   ├── UserManageView.vue       # 用户管理
    │   │   ├── MerchantAuditView.vue    # 商家审核
    │   │   ├── CategoryManageView.vue   # 分类管理
    │   │   ├── ProductAllView.vue       # 全平台商品管理
    │   │   ├── OrderAllView.vue         # 全平台订单管理
    │   │   └── SalesReportView.vue      # 全局销售报表
    │   └── chat/                        # 即时通讯
    │       └── ChatView.vue
    ├── components/                      # ===== 通用组件层 =====
    │   ├── layout/                      # 布局组件
    │   │   ├── MallLayout.vue           # 商城布局（顶部搜索栏 + 内容区）
    │   │   ├── MerchantLayout.vue       # 商家后台布局（侧边栏 + 内容区）
    │   │   └── AdminLayout.vue          # 管理员后台布局（侧边栏 + 内容区）
    │   ├── ProductCard.vue              # 商品卡片
    │   ├── PaymentCard.vue              # 卡号支付表单
    │   ├── PaymentQRCode.vue            # 二维码支付
    │   ├── SearchBar.vue                # 搜索栏（含自动补全）
    │   └── ImageUpload.vue              # 图片上传组件
    ├── composables/                     # ===== 组合式函数 =====
    │   ├── useWebSocket.ts              # WebSocket 封装
    │   ├── usePagination.ts             # 分页逻辑
    │   └── useDebounce.ts               # 防抖
    ├── utils/                           # ===== 工具函数 =====
    │   ├── token.ts                     # Token 存取（localStorage）
    │   ├── luhn.ts                      # Luhn 算法（前端卡号预校验）
    │   └── validators.ts               # 自定义校验规则
    └── types/                           # ===== TypeScript 类型定义 =====
        ├── user.ts                      # User、LoginDTO、RegisterDTO
        ├── product.ts                   # Product、ProductSKU、Category
        ├── order.ts                     # Order、OrderItem、OrderStatusEnum
        ├── cart.ts                      # CartItem
        ├── payment.ts                   # Payment、PayMethodEnum
        ├── comment.ts                   # Comment
        ├── chat.ts                      # Conversation、Message
        └── common.ts                    # Result<T>、PageResult<T>、ErrorCode
```

---

## 三、多套独立布局设计

系统根据角色提供 **三套独立布局**，在路由守卫中根据用户角色自动切换。

### 3.1 商城布局 `MallLayout.vue`

用于**游客**和**注册用户**视角，偏电商风格：

```
┌─────────────────────────────────────────────┐
│  Logo   搜索栏 [________________] 🔍  购物车 │  ← 顶部导航
├─────────────────────────────────────────────┤
│  全部分类  │  一级分类 │ 二级分类 │ ...      │  ← 分类导航条
├─────────────────────────────────────────────┤
│                                             │
│              <router-view />                │  ← 商品列表/详情/搜索结果
│                                             │
├─────────────────────────────────────────────┤
│         © 2025 网上超市 版权所有             │  ← 底部栏
└─────────────────────────────────────────────┘
```

- 未登录：显示"登录/注册"按钮
- 已登录：显示用户头像、昵称，下拉菜单含"我的订单""收货地址""退出登录"

### 3.2 商家后台布局 `MerchantLayout.vue`

用于**商家**角色，偏管理面板风格：

```
┌────────────┬────────────────────────────────┐
│  店铺名称   │                                │
│            │                                │
│  商品管理   │                                │
│  订单管理   │        <router-view />         │
│  退款审核   │                                │
│  销售统计   │                                │
│            │                                │
└────────────┴────────────────────────────────┘
```

- 左侧固定侧边栏
- 内容区显示对应管理页面

### 3.3 管理员后台布局 `AdminLayout.vue`

用于**管理员**角色，结构类似商家后台：

```
┌────────────┬────────────────────────────────┐
│  管理员面板 │                                │
│            │                                │
│  用户管理   │                                │
│  商家审核   │        <router-view />         │
│  分类管理   │                                │
│  商品管理   │                                │
│  订单管理   │                                │
│  评论管理   │                                │
│  销售报表   │                                │
└────────────┴────────────────────────────────┘
```

### 3.4 布局切换逻辑

`App.vue` 中根据当前路由的 `meta.layout` 字段动态选择布局组件：

```typescript
// router/index.ts 路由配置示例
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
  meta: { layout: 'merchant', roles: ['merchant'] },
  children: [
    { path: 'products', component: ProductManageView },
    // ...
  ]
},
{
  path: '/admin',
  component: AdminLayout,
  meta: { layout: 'admin', roles: ['admin'] },
  children: [
    { path: 'users', component: UserManageView },
    // ...
  ]
}
```

---

## 四、Axios 封装与拦截器

### 4.1 Axios 实例 `api/index.ts`

```typescript
import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeToken } from '@/utils/token'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401 未登录 → 跳转登录页
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
```

### 4.2 API 模块示例 `api/cart.ts`

```typescript
import request from './index'
import type { CartItem } from '@/types/cart'

export function getCartList(): Promise<CartItem[]> {
  return request.get('/cart/list')
}

export function addToCart(skuId: number, quantity: number) {
  return request.post('/cart/add', { skuId, quantity })
}

export function updateQuantity(cartItemId: number, quantity: number) {
  return request.put('/cart/update', { cartItemId, quantity })
}

export function removeFromCart(cartItemId: number) {
  return request.delete(`/cart/${cartItemId}`)
}

export function selectCartItems(cartItemIds: number[]) {
  return request.put('/cart/select', { cartItemIds })
}
```

---

## 五、路由守卫与权限控制

### 5.1 静态路由 + 角色元信息 `router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/token'
import { useUserStore } from '@/stores/user'

// 路由 meta 类型扩展
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    roles?: string[]        // 允许访问的角色列表
    layout?: 'mall' | 'merchant' | 'admin'
  }
}

// 公开路由（无需登录）
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/MallLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/HomeView.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/product/ProductDetailView.vue') },
      { path: 'search', name: 'SearchResult', component: () => import('@/views/product/SearchResultView.vue') },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/user/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/user/RegisterView.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/user/ForgotPasswordView.vue') },
]

// 需登录路由（普通用户）
const userRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/MallLayout.vue'),
    meta: { roles: ['user', 'merchant', 'admin'] },
    children: [
      { path: 'cart', name: 'Cart', meta: { title: '购物车' }, component: () => import('@/views/cart/CartView.vue') },
      { path: 'checkout', name: 'Checkout', meta: { title: '结算' }, component: () => import('@/views/order/CheckoutView.vue') },
      { path: 'orders', name: 'OrderList', meta: { title: '我的订单' }, component: () => import('@/views/order/OrderListView.vue') },
      { path: 'order/:id', name: 'OrderDetail', meta: { title: '订单详情' }, component: () => import('@/views/order/OrderDetailView.vue') },
      { path: 'payment/:orderId', name: 'Payment', meta: { title: '支付' }, component: () => import('@/views/order/PaymentView.vue') },
      { path: 'profile', name: 'Profile', meta: { title: '个人信息' }, component: () => import('@/views/user/ProfileView.vue') },
      { path: 'address', name: 'Address', meta: { title: '收货地址' }, component: () => import('@/views/user/AddressView.vue') },
      { path: 'chat/:conversationId?', name: 'Chat', meta: { title: '聊天' }, component: () => import('@/views/chat/ChatView.vue') },
    ]
  },
]

// 商家路由
const merchantRoutes: RouteRecordRaw[] = [
  {
    path: '/merchant',
    component: () => import('@/components/layout/MerchantLayout.vue'),
    meta: { roles: ['merchant'] },
    children: [
      { path: 'apply', name: 'MerchantApply', meta: { title: '商家入驻' }, component: () => import('@/views/merchant/MerchantApplyView.vue') },
      { path: 'products', name: 'ProductManage', meta: { title: '商品管理' }, component: () => import('@/views/merchant/ProductManageView.vue') },
      { path: 'orders', name: 'MerchantOrders', meta: { title: '订单管理' }, component: () => import('@/views/merchant/OrderManageView.vue') },
      { path: 'refunds', name: 'RefundManage', meta: { title: '退款审核' }, component: () => import('@/views/merchant/RefundManageView.vue') },
      { path: 'stats', name: 'SalesStats', meta: { title: '销售统计' }, component: () => import('@/views/merchant/SalesStatsView.vue') },
    ]
  },
]

// 管理员路由
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { roles: ['admin'] },
    children: [
      { path: 'users', name: 'UserManage', meta: { title: '用户管理' }, component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'merchants', name: 'MerchantAudit', meta: { title: '商家审核' }, component: () => import('@/views/admin/MerchantAuditView.vue') },
      { path: 'categories', name: 'CategoryManage', meta: { title: '分类管理' }, component: () => import('@/views/admin/CategoryManageView.vue') },
      { path: 'products', name: 'ProductAll', meta: { title: '商品管理' }, component: () => import('@/views/admin/ProductAllView.vue') },
      { path: 'orders', name: 'OrderAll', meta: { title: '订单管理' }, component: () => import('@/views/admin/OrderAllView.vue') },
      { path: 'report', name: 'SalesReport', meta: { title: '销售报表' }, component: () => import('@/views/admin/SalesReportView.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...userRoutes, ...merchantRoutes, ...adminRoutes],
})
```

### 5.2 全局前置守卫 `beforeEach`

```typescript
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '网上超市'

  const token = getToken()

  if (to.path === '/login' || to.path === '/register') {
    if (token) return next('/')       // 已登录不能访问登录页
    return next()
  }

  // 目标路由无需角色 → 放行（公开页面）
  if (!to.meta.roles || to.meta.roles.length === 0) {
    return next()
  }

  // 目标路由需要角色 → 检查登录
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 检查角色是否匹配
  const userStore = useUserStore()
  const userRole = userStore.userInfo?.role
  if (to.meta.roles && !to.meta.roles.includes(userRole || '')) {
    ElMessage.warning('无权限访问')
    return next('/')
  }

  next()
})
```

---

## 六、Pinia 状态管理

### 6.1 用户状态 `stores/user.ts`

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getUserInfo } from '@/api/user'
import { setToken, removeToken, getToken } from '@/utils/token'
import type { UserInfo, LoginDTO, RegisterDTO } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)

  async function loginAction(dto: LoginDTO) {
    const res = await login(dto)
    token.value = res.token
    userInfo.value = res.userInfo
    setToken(res.token)
  }

  async function registerAction(dto: RegisterDTO) {
    const res = await register(dto)
    token.value = res.token
    userInfo.value = res.userInfo
    setToken(res.token)
  }

  function logoutAction() {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  async function fetchUserInfo() {
    userInfo.value = await getUserInfo()
  }

  return { token, userInfo, isLoggedIn, role, loginAction, registerAction, logoutAction, fetchUserInfo }
})
```

### 6.2 购物车状态 `stores/cart.ts`

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addToCart, updateQuantity, removeFromCart, selectCartItems } from '@/api/cart'
import type { CartItem } from '@/types/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedItems = computed(() => items.value.filter(item => item.selected))
  const totalAmount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

  async function fetchCart() {
    items.value = await getCartList()
  }

  async function add(skuId: number, quantity: number) {
    await addToCart(skuId, quantity)
    await fetchCart()
  }

  async function update(id: number, quantity: number) {
    await updateQuantity(id, quantity)
    await fetchCart()
  }

  async function remove(id: number) {
    await removeFromCart(id)
    await fetchCart()
  }

  function clear() {
    items.value = []
  }

  return { items, totalCount, selectedItems, totalAmount, fetchCart, add, update, remove, clear }
})
```

---

## 七、WebSocket 即时通讯

### 7.1 后端约定

后端使用 JSR 356 注解（`@OnOpen` / `@OnClose` / `@OnMessage`），连接地址格式：

```
ws://localhost:8080/chat/{userId}
```

消息格式（JSON）：

```json
{
  "type": "TEXT",
  "conversationId": 123,
  "content": "你好，这个商品有货吗？",
  "productId": 456,
  "timestamp": 1700000000000
}
```

### 7.2 前端封装 `composables/useWebSocket.ts`

```typescript
import { ref, onUnmounted } from 'vue'
import { getToken } from '@/utils/token'

interface WSMessage {
  type: 'TEXT' | 'IMAGE'
  conversationId: number
  content: string
  productId?: number
  timestamp: number
}

export function useWebSocket(userId: number) {
  const messages = ref<WSMessage[]>([])
  const isConnected = ref(false)
  let ws: WebSocket | null = null
  let reconnectTimer: number | null = null

  function connect() {
    const token = getToken()
    ws = new WebSocket(`ws://localhost:8080/chat/${userId}?token=${token}`)

    ws.onopen = () => {
      isConnected.value = true
      console.log('WebSocket 已连接')
    }

    ws.onmessage = (event) => {
      const msg: WSMessage = JSON.parse(event.data)
      messages.value.push(msg)
    }

    ws.onclose = () => {
      isConnected.value = false
      // 自动重连（3 秒后重试）
      reconnectTimer = window.setTimeout(connect, 3000)
    }

    ws.onerror = (err) => {
      console.error('WebSocket 错误', err)
    }
  }

  function send(msg: { type: string; conversationId: number; content: string; productId?: number }) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ ...msg, timestamp: Date.now() }))
    }
  }

  function disconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    ws?.close()
  }

  onUnmounted(disconnect)

  return { messages, isConnected, connect, send, disconnect }
}
```

---

## 八、Elasticsearch 直连搜索

### 8.1 搜索流程

前端直接调用 ES REST API，需要在 Vite 开发环境配置代理，生产环境由 Nginx 转发。

### 8.2 Vite 代理配置 `vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {                         // 后端 API
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/es': {                          // Elasticsearch
        target: 'http://localhost:9200',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/es/, ''),
      },
    },
  },
})
```

### 8.3 搜索 API 封装 `api/search.ts`

```typescript
import axios from 'axios'

const esClient = axios.create({
  baseURL: '/es',                       // Vite proxy → ES
  timeout: 10000,
})

interface SearchParams {
  keyword?: string
  categoryId?: number
  brand?: string
  minPrice?: number
  maxPrice?: number
  sort?: 'sales_desc' | 'price_asc' | 'price_desc' | 'time_desc'
  page?: number
  size?: number
}

// 商品全文搜索
export function searchProducts(params: SearchParams) {
  const must: any[] = []

  if (params.keyword) {
    must.push({
      multi_match: {
        query: params.keyword,
        fields: ['name^3', 'description', 'brand'],
      },
    })
  }

  if (params.categoryId) {
    must.push({ term: { category_id: params.categoryId } })
  }

  if (params.brand) {
    must.push({ term: { brand: params.brand } })
  }

  const filter: any[] = []
  if (params.minPrice || params.maxPrice) {
    filter.push({
      range: { price: { gte: params.minPrice || 0, lte: params.maxPrice || 99999999 } },
    })
  }

  let sortArr: any[] = []
  switch (params.sort) {
    case 'sales_desc':   sortArr = [{ sales: 'desc' }]; break
    case 'price_asc':    sortArr = [{ price: 'asc' }]; break
    case 'price_desc':   sortArr = [{ price: 'desc' }]; break
    case 'time_desc':    sortArr = [{ created_at: 'desc' }]; break
  }

  const from = ((params.page || 1) - 1) * (params.size || 20)

  return esClient.post('/product_index/_search', {
    query: { bool: { must, filter } },
    sort: sortArr,
    from,
    size: params.size || 20,
  })
}

// 搜索建议/自动补全
export function searchSuggest(keyword: string) {
  return esClient.post('/product_index/_search', {
    suggest: {
      keyword_suggest: {
        prefix: keyword,
        completion: { field: 'suggest' },
      },
    },
  })
}
```

---

## 九、图片上传（前端 → 后端 → OSS）

### 9.1 上传组件 `components/ImageUpload.vue`

```vue
<template>
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      list-type="picture-card"
      :limit="limit"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :file-list="fileList"
      multiple
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getToken } from '@/utils/token'
import { ElMessage } from 'element-plus'
import type { UploadProps, UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string[]          // 已上传的 URL 列表
  limit?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', urls: string[]): void
}>()

const uploadUrl = '/api/upload/image'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`,
}))

const fileList = ref<UploadFile[]>(
  props.modelValue.map((url, idx) => ({ uid: idx, name: `image-${idx}`, url, status: 'success' }))
)

const handleSuccess: UploadProps['onSuccess'] = (response: any, _file, _fileList) => {
  const urls = _fileList.filter(f => f.url || f.response?.url).map(f => f.url || f.response?.url)
  emit('update:modelValue', urls)
}

const handleError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败，请重试')
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage)  { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M)   { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}
</script>
```

---

## 十、支付模块（前端模拟）

### 10.1 卡号支付

- 用户在支付页面选择"卡号支付"
- 输入卡号、有效期（MM/YY）、CVV
- **前端预校验**：
  - 卡号格式（16-19 位数字）、Luhn 算法校验
  - 有效期不为过期日期
  - CVV 为 3 或 4 位数字
- 校验通过后调用 `POST /api/payment/card`

### 10.2 Luhn 算法 `utils/luhn.ts`

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

### 10.3 二维码支付

- 用户选择"二维码支付"
- 前端调用 `POST /api/payment/qrcode` 获取二维码 URL
- 展示二维码图片（实际展示订单信息摘要 + 金额）
- 用户点击"确认支付" → `POST /api/payment/confirm-qrcode`
- 支付成功后跳转订单详情页

---

## 十一、防重复提交 Token

### 11.1 下单流程

```
用户点击"提交订单"
  → GET /api/order/submit-token   （获取防重 Token，5分钟有效）
  → POST /api/order/create        （携带 Token + 地址 + 购物车选中项 ID）
  → 成功后跳转支付页面
```

### 11.2 前端实现

```typescript
// 下单页面
const submitOrder = async () => {
  // 1. 获取防重复提交 Token
  const { data: tokenData } = await getSubmitToken()
  const submitToken = tokenData.token

  // 2. 提交订单
  const { data: orderData } = await createOrder({
    token: submitToken,
    addressId: selectedAddressId.value,
    cartItemIds: cartStore.selectedItems.map(i => i.id),
  })

  // 3. 跳转支付页面
  router.push(`/payment/${orderData.orderId}`)
}
```

---

## 十二、Nginx 部署配置

### 12.1 Nginx 配置示例

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    root /usr/share/nginx/html;
    index index.html;

    # History 模式 fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端 Spring Boot
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Elasticsearch 代理
    location /es/ {
        proxy_pass http://127.0.0.1:9200/;
        proxy_set_header Host $host;
    }

    # WebSocket 代理
    location /chat/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

---

## 十三、页面路由总览

| 路径 | 页面 | 角色 | 说明 |
|------|------|------|------|
| `/` | 首页/商品列表 | 游客/用户/商家/管理员 | 分类浏览、搜索入口 |
| `/product/:id` | 商品详情 | 游客/用户/商家/管理员 | 含 SKU 选择、评论列表 |
| `/search` | 搜索结果 | 游客/用户/商家/管理员 | ES 全文检索结果 |
| `/login` | 登录 | 游客 | 用户名密码 + 验证码 |
| `/register` | 注册 | 游客 | 选择角色 |
| `/cart` | 购物车 | 用户/商家 | 需登录 |
| `/checkout` | 结算 | 用户/商家 | 确认地址、商品、金额 |
| `/payment/:orderId` | 支付 | 用户/商家 | 卡号/二维码支付 |
| `/orders` | 我的订单 | 用户/商家 | 订单列表 |
| `/order/:id` | 订单详情 | 用户/商家 | 含退款/退货入口 |
| `/profile` | 个人信息 | 用户/商家/管理员 | 修改密码等 |
| `/address` | 收货地址 | 用户/商家 | 增删改查 |
| `/chat/:id?` | 聊天 | 用户/商家 | WebSocket 即时通讯 |
| `/merchant/apply` | 商家入驻 | 用户 | 提交入驻申请 |
| `/merchant/products` | 商品管理 | 商家 | 增删改查、上下架 |
| `/merchant/orders` | 订单管理 | 商家 | 发货操作 |
| `/merchant/refunds` | 退款审核 | 商家 | 审核退款退货 |
| `/merchant/stats` | 销售统计 | 商家 | 自己的销售数据 |
| `/admin/users` | 用户管理 | 管理员 | 启用/禁用 |
| `/admin/merchants` | 商家审核 | 管理员 | 审核入驻申请 |
| `/admin/categories` | 分类管理 | 管理员 | 三级分类增删改 |
| `/admin/products` | 商品管理 | 管理员 | 全局商品管理 |
| `/admin/orders` | 订单管理 | 管理员 | 全局订单管理 |
| `/admin/report` | 销售报表 | 管理员 | 全局销售统计 |

---

## 十四、与后端数据交互汇总

| 前端操作 | HTTP 方法 | API 路径 | 请求体/参数 |
|----------|-----------|----------|-------------|
| 注册 | POST | `/api/user/register` | `{ username, password, phone, email, role, captchaUuid, captchaCode }` |
| 登录 | POST | `/api/user/login` | `{ username, password, captchaUuid, captchaCode }` |
| 获取重置密码验证码 | POST | `/api/user/forgot-code` | `{ account, captchaUuid, captchaCode }` |
| 重置密码 | POST | `/api/user/reset-password` | `{ uuid, code, newPassword }` |
| 获取用户信息 | GET | `/api/user/info` | — |
| 商品列表 | GET | `/api/product/list` | `?categoryId=&page=&size=` |
| 商品详情 | GET | `/api/product/{id}` | — |
| 搜索 | POST | `/es/product_index/_search` | ES DSL JSON |
| 搜索建议 | POST | `/es/product_index/_search` | suggest DSL |
| 获取购物车 | GET | `/api/cart/list` | — |
| 加入购物车 | POST | `/api/cart/add` | `{ skuId, quantity }` |
| 更新购物车数量 | PUT | `/api/cart/update` | `{ cartItemId, quantity }` |
| 删除购物车 | DELETE | `/api/cart/{id}` | — |
| 获取防重 Token | GET | `/api/order/submit-token` | — |
| 创建订单 | POST | `/api/order/create` | `{ token, addressId, cartItemIds }` |
| 订单列表 | GET | `/api/order/list` | `?status=&page=&size=` |
| 订单详情 | GET | `/api/order/{id}` | — |
| 取消订单 | PUT | `/api/order/{id}/cancel` | — |
| 确认收货 | PUT | `/api/order/{id}/receive` | — |
| 卡号支付 | POST | `/api/payment/card` | `{ orderId, cardNo, expireDate, cvv }` |
| 获取支付二维码 | POST | `/api/payment/qrcode` | `{ orderId }` |
| 确认二维码支付 | POST | `/api/payment/confirm-qrcode` | `{ orderId }` |
| 上传图片 | POST | `/api/upload/image` | FormData(file) |
| 收货地址 CRUD | GET/POST/PUT/DELETE | `/api/address` | — |
| 发表评论 | POST | `/api/comment` | `{ productId, content, rating, images }` |
| 申请退款 | POST | `/api/refund` | `{ orderItemId, type, reason, amount }` |
| WebSocket 连接 | WS | `ws://host/chat/{userId}` | — |
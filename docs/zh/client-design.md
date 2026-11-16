# 网上超市系统 - 客户端概要设计

> [English](../en/client-design.md) | 简体中文


---

## 一、技术选型

| 类别 | 技术 | 版本/说明 |
|------|------|-----------|
| 核心框架 | Vue 3 | Composition API + `<script setup>` |
| 开发语言 | TypeScript | 强类型约束 |
| 构建工具 | Vite | 开发热更新 + 生产 Rollup 打包 |
| UI 组件库 | Element Plus | Vue 3 官方推荐 |
| 状态管理 | Pinia | Vue 3 官方推荐，TS 友好 |
| HTTP 客户端 | Axios | 拦截器统一处理 Token / 错误 |
| CSS 方案 | Scoped CSS + CSS 变量 | Vue SFC 样式隔离 + 全局设计 Token |
| 路由 | Vue Router 4 | History 模式 |
| Mock 数据 | vite-plugin-mock | 后端未完成时使用 Mock 接口联调 |
| 图标 | @element-plus/icons-vue | Element Plus 图标库 |

---

## 二、项目目录结构（混合方式）

```
chrono-bbs-client/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── mock/                             # ===== Mock 数据层（vite-plugin-mock）=====
│   ├── index.ts                     # Mock 入口
│   ├── user.ts                      # 用户认证 Mock
│   ├── product.ts                   # 商品 Mock（含分类树）
│   ├── category.ts                  # 分类 Mock
│   ├── cart.ts                      # 购物车 Mock
│   ├── order.ts                     # 订单 Mock
│   ├── payment.ts                   # 支付 Mock
│   ├── address.ts                   # 地址 Mock
│   ├── merchant.ts                  # 商家 Mock
│   ├── admin.ts                     # 管理员 Mock
│   ├── refund.ts                    # 退款 Mock
│   ├── chat.ts                      # 聊天 Mock
│   ├── upload.ts                    # 上传 Mock
│   ├── search.ts                    # 搜索 Mock
│   ├── comment.ts                   # 评论 Mock
│   └── seckill.ts                   # 秒杀券 Mock
├── public/
│   ├── favicon.svg
│   ├── icons.svg
│   └── img/products/                # 商品图片
└── src/
    ├── main.ts                          # 入口文件
    ├── App.vue                          # 根组件
    ├── api/                             # ===== API 接口层 =====
    │   ├── index.ts                     # Axios 实例 + 拦截器封装
    │   ├── user.ts                      # 用户相关接口
    │   ├── product.ts                   # 商品相关接口
    │   ├── cart.ts                      # 购物车接口
    │   ├── order.ts                     # 订单接口
    │   ├── payment.ts                   # 支付接口
    │   ├── address.ts                   # 地址接口
    │   ├── merchant.ts                  # 商家接口
    │   ├── admin.ts                     # 管理员接口
    │   ├── refund.ts                    # 退款接口
    │   ├── chat.ts                      # 聊天接口
    │   ├── search.ts                    # 搜索接口
    │   ├── comment.ts                   # 评论接口
    │   ├── seckill.ts                   # 秒杀券接口
    │   └── upload.ts                    # 上传接口
    ├── router/                          # ===== 路由层 =====
    │   └── index.ts                     # 路由配置 + 守卫 + 角色元信息
    ├── config/                          # ===== 功能开关 =====
    │   └── features.ts                  # CHAT_ENABLED / REFUND_AUDIT_ENABLED
    ├── stores/                          # ===== 状态管理层 =====
    │   └── user.ts                      # 用户登录态（token + userInfo）
    ├── views/                           # ===== 页面视图层 =====
    │   ├── home/                        # 首页（商品列表）
    │   │   └── HomeView.vue
    │   ├── product/                     # 商品模块
    │   │   ├── ProductDetailView.vue    # 商品详情
    │   │   └── SearchResultView.vue     # 搜索结果
    │   ├── cart/                        # 购物车
    │   │   └── CartView.vue
    │   ├── coupon/                      # 优惠券
    │   │   └── CouponCenterView.vue     # 领券中心
    │   ├── order/                       # 订单模块
    │   │   ├── CheckoutView.vue         # 结算/提交订单
    │   │   ├── OrderListView.vue        # 订单列表
    │   │   ├── OrderDetailView.vue      # 订单详情
    │   │   ├── PaymentView.vue          # 支付页面
    │   │   └── PaymentRecordView.vue    # 支付记录
    │   ├── refund/                      # 退款/售后
    │   │   └── RefundListView.vue       # 我的退款/售后
    │   ├── user/                        # 用户模块
    │   │   ├── LoginView.vue            # 登录
    │   │   ├── RegisterView.vue         # 注册
    │   │   ├── ForgotPasswordView.vue   # 忘记密码
    │   │   ├── SettingsView.vue         # 账户设置（/profile 重定向至此）
    │   │   └── AddressView.vue          # 收货地址管理
    │   ├── merchant/                    # 商家后台
    │   │   ├── MerchantApplyView.vue    # 入驻申请
    │   │   ├── ProductManageView.vue    # 商品管理
    │   │   ├── CommentManageView.vue    # 评论管理
    │   │   ├── OrderManageView.vue      # 订单管理
    │   │   ├── RefundManageView.vue     # 退款/退货审核
    │   │   └── SalesStatsView.vue       # 销售统计
    │   ├── admin/                       # 管理员后台
    │   │   ├── UserManageView.vue       # 用户管理
    │   │   ├── MerchantAuditView.vue    # 商家审核
    │   │   ├── CategoryManageView.vue   # 分类管理
    │   │   ├── ProductAllView.vue       # 全平台商品管理
    │   │   ├── OrderAllView.vue         # 全平台订单管理
    │   │   ├── SalesReportView.vue      # 全局销售报表
    │   │   └── CouponManageView.vue     # 秒杀券管理
    │   ├── chat/                        # 即时通讯
    │   │   └── ChatView.vue
    │   ├── common/                      # 通用页面
    │   │   └── HelpView.vue             # 帮助中心
    │   └── NotFound.vue                 # 404 页面
    ├── components/                      # ===== 通用组件层 =====
    │   ├── layout/                      # 布局组件
    │   │   ├── MallLayout.vue           # 商城布局（顶部导航 + 分类栏 + 内容区 + 页脚）
    │   │   ├── MerchantLayout.vue       # 商家后台布局（侧边栏 + 内容区）
    │   │   └── AdminLayout.vue          # 管理员后台布局（侧边栏 + 内容区）
    │   ├── category/
    │   │   └── CategoryFlyout.vue       # 三级分类悬浮面板
    │   └── chart/
    │       └── BaseChart.vue            # ECharts 图表封装（按需引入）
    ├── utils/                           # ===== 工具函数 =====
    │   ├── token.ts                     # Token 存取（localStorage）
    │   ├── settings.ts                  # 用户偏好设置（localStorage）
    │   ├── chatSocket.ts                # WebSocket 客户端（单例、自动重连）
    │   ├── luhn.ts                      # 银行卡号格式化与 Luhn/有效期/CVV 校验
    │   ├── region.ts                    # 省市区数据
    │   ├── sanitizeHtml.ts              # 商品详情富文本清洗
    │   ├── luhn.spec.ts                 # Luhn 单元测试
    │   └── sanitizeHtml.spec.ts         # 富文本清洗单元测试
    ├── styles/                          # ===== 全局样式 =====
    │   ├── variables.css                # CSS 变量系统（设计 Token）
    │   └── global.css                   # 全局基础样式
    └── types/                           # ===== TypeScript 类型定义 =====
        ├── user.ts                      # UserInfo、LoginRequest、RegisterRequest
        ├── product.ts                   # Product、ProductSKU、Category
        ├── order.ts                     # Order、OrderItem、OrderStatus
        ├── cart.ts                      # CartItem、AddCartRequest
        ├── address.ts                   # Address
        ├── comment.ts                   # CommentItem、AddCommentRequest 等
        ├── coupon.ts                    # 优惠券类型
        └── common.ts                    # Result<T>、PageResult<T>、Role、OrderStatus
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
│  评论管理   │        <router-view />         │
│  订单管理   │                                │
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

## 四、Axios 封装与拦截器

### 4.1 Axios 实例 `api/index.ts`

```typescript
import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/token'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动附加 Token
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

// 响应拦截器：统一处理业务码与 401
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      removeToken()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export default service
```

### 4.2 API 模块示例 `api/cart.ts`

```typescript
import service from '@/api'
import type { CartItem, AddCartRequest, UpdateCartRequest, SelectCartRequest } from '@/types/cart'

// 获取购物车列表
export const getCartList = () => {
  return service.get<any, { code: number; message: string; data: CartItem[]; timestamp: number }>('/cart/list')
}

// 加入购物车
export const addToCart = (data: AddCartRequest) => {
  return service.post('/cart/add', data)
}

// 更新购物车数量
export const updateCartQuantity = (data: UpdateCartRequest) => {
  return service.put('/cart/update', data)
}

// 删除购物车商品
export const removeFromCart = (cartItemId: number) => {
  return service.delete(`/cart/${cartItemId}`)
}

// 选中/取消选中
export const selectCartItems = (data: SelectCartRequest) => {
  return service.put('/cart/select', data)
}

// 全选/取消全选
export const selectAllCart = (selected: number) => {
  return service.put('/cart/select-all', { selected })
}
```

---

## 五、路由守卫与权限控制

### 5.1 静态路由 + 角色元信息 `router/index.ts`

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 路由 meta 类型扩展
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    roles?: string[]        // 允许访问的角色列表：'USER' | 'MERCHANT' | 'ADMIN'
    layout?: 'mall' | 'merchant' | 'admin'
    public?: boolean
  }
}

// 公开路由（无需登录）：登录/注册 + 商品详情
const publicRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/user/LoginView.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/user/RegisterView.vue'), meta: { title: '注册' } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/user/ForgotPasswordView.vue'), meta: { title: '忘记密码' } },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/product/ProductDetailView.vue'), meta: { title: '商品详情' } },
]

// 商城布局路由（首页/搜索/领券等公开子路由 + 需登录子路由）
const userRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/MallLayout.vue'),
    meta: { layout: 'mall' },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/home/HomeView.vue'), meta: { title: '首页' } },
      { path: 'coupons', name: 'CouponCenter', component: () => import('@/views/coupon/CouponCenterView.vue'), meta: { title: '领券中心' } },
      { path: 'search', name: 'SearchResult', component: () => import('@/views/product/SearchResultView.vue'), meta: { title: '搜索结果' } },
      { path: 'cart', name: 'Cart', meta: { title: '购物车', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/cart/CartView.vue') },
      { path: 'checkout', name: 'Checkout', meta: { title: '结算', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/CheckoutView.vue') },
      { path: 'orders', name: 'OrderList', meta: { title: '我的订单', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/OrderListView.vue') },
      { path: 'payments', name: 'PaymentRecords', meta: { title: '支付记录', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/PaymentRecordView.vue') },
      { path: 'order/:id', name: 'OrderDetail', meta: { title: '订单详情', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/OrderDetailView.vue') },
      { path: 'payment/:orderId', name: 'Payment', meta: { title: '支付', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/order/PaymentView.vue') },
      { path: 'merchant/apply', name: 'MerchantApply', meta: { title: '商家入驻', roles: ['USER', 'MERCHANT'] }, component: () => import('@/views/merchant/MerchantApplyView.vue') },
      { path: 'refunds', name: 'RefundList', meta: { title: '退款/售后', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/refund/RefundListView.vue') },
      { path: 'profile', name: 'Profile', redirect: '/settings' },
      { path: 'settings', name: 'Settings', meta: { title: '账户设置', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/user/SettingsView.vue') },
      { path: 'address', name: 'Address', meta: { title: '收货地址', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/user/AddressView.vue') },
      { path: 'chat/:conversationId?', name: 'Chat', meta: { title: '聊天', roles: ['USER', 'MERCHANT', 'ADMIN'] }, component: () => import('@/views/chat/ChatView.vue') },
      { path: 'help/:type', name: 'Help', meta: { title: '帮助中心' }, component: () => import('@/views/common/HelpView.vue') },
    ]
  },
]

// 商家后台路由
const merchantRoutes: RouteRecordRaw[] = [
  {
    path: '/merchant',
    component: () => import('@/components/layout/MerchantLayout.vue'),
    meta: { layout: 'merchant', roles: ['MERCHANT'] },
    children: [
      { path: 'products', name: 'ProductManage', meta: { title: '商品管理' }, component: () => import('@/views/merchant/ProductManageView.vue') },
      { path: 'comments', name: 'MerchantComments', meta: { title: '评论管理' }, component: () => import('@/views/merchant/CommentManageView.vue') },
      { path: 'orders', name: 'MerchantOrders', meta: { title: '订单管理' }, component: () => import('@/views/merchant/OrderManageView.vue') },
      { path: 'refunds', name: 'RefundManage', meta: { title: '退款审核' }, component: () => import('@/views/merchant/RefundManageView.vue') },
      { path: 'stats', name: 'SalesStats', meta: { title: '销售统计' }, component: () => import('@/views/merchant/SalesStatsView.vue') },
    ]
  },
]

// 管理员后台路由
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { layout: 'admin', roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'dashboard', redirect: '/admin/users' },
      { path: 'users', name: 'UserManage', meta: { title: '用户管理' }, component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'merchants', name: 'MerchantAudit', meta: { title: '商家审核' }, component: () => import('@/views/admin/MerchantAuditView.vue') },
      { path: 'categories', name: 'CategoryManage', meta: { title: '分类管理' }, component: () => import('@/views/admin/CategoryManageView.vue') },
      { path: 'products', name: 'ProductAll', meta: { title: '商品管理' }, component: () => import('@/views/admin/ProductAllView.vue') },
      { path: 'orders', name: 'OrderAll', meta: { title: '订单管理' }, component: () => import('@/views/admin/OrderAllView.vue') },
      { path: 'refunds', name: 'AdminRefundAudit', meta: { title: '退款审核' }, component: () => import('@/views/merchant/RefundManageView.vue') },
      { path: 'report', name: 'SalesReport', meta: { title: '销售报表' }, component: () => import('@/views/admin/SalesReportView.vue') },
      { path: 'coupons', name: 'CouponManage', meta: { title: '秒杀券管理' }, component: () => import('@/views/admin/CouponManageView.vue') },
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
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue'), meta: { title: '页面不存在' } },
  ],
})
```

### 5.2 全局前置守卫 `beforeEach`

```typescript
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '网上超市'} - Chrono BBS`
  const userStore = useUserStore()

  // 已登录不能访问登录/注册页
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
    ElMessage.warning('无权限访问')
    return next('/home')
  }

  next()
})
```

---

## 六、Pinia 状态管理

### 6.1 用户状态 `stores/user.ts`

当前仅实现了用户状态 store，管理 token 和用户信息的持久化：

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

> **说明**：购物车、聊天等状态目前直接在各页面组件中通过调用 API 获取和管理，未单独抽离为 Pinia store。角色判断统一使用 `Role` 枚举（USER=1, MERCHANT=2, ADMIN=3），角色名字符串为大写 `'USER'` / `'MERCHANT'` / `'ADMIN'`。

---

## 七、即时通讯

> **实现状态**：聊天入口已启用（`CHAT_ENABLED = true`）。`npm run dev` 与生产环境通过 `src/utils/chatSocket.ts` 直连后端 WebSocket，聊天页实时收发、未读角标实时刷新、断线自动重连；仅 `npm run dev:mock` 演示模式使用 Mock HTTP 接口。

### 7.1 后端 WebSocket 约定

后端使用 JSR 356 注解（`@OnOpen` / `@OnClose` / `@OnMessage`），连接地址格式（与当前页面同源，协议根据页面自动选择 `ws`/`wss`，Token 经查询参数传递）：

```
/chat/{userId}?token={jwt}
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

### 7.2 前端封装 `utils/chatSocket.ts`

采用全局单例 `chatSocket`，由商城布局持有连接、聊天页共享同一实例：

- `connect(userId, token)`：以当前页面同源地址建立 `/chat/{userId}?token=...` 连接，重复调用复用现有连接；
- `send({ conversationId, content })`：连接就绪时发送 `TEXT` 帧，未就绪返回 `false`；
- `onFrame(listener)` / `onStatus(listener)`：订阅实时消息与连接状态，返回取消订阅函数；
- `disconnect()`：登出或离开商城布局时主动关闭；
- 断线后按 1s、2s、4s……最长 30s 指数退避重连；收到 1008（鉴权失败）时停止重连，等待重新登录；
- Vite 开发服务器与 Nginx 均配置 `/chat/` 的 WebSocket 转发，前端无需硬编码后端端口。

---

## 八、商品搜索（后端接口）

> **实现状态**：搜索页已按本节接口接入（后端内部使用 Elasticsearch，前端不直连 ES），封装见 `api/search.ts`。接口异常时自动降级为 `/api/product/list` 本地过滤，保证游客可用。

### 8.1 接口清单

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 全文检索 | GET | `/api/search` | 关键词 + 分类/品牌/价格区间筛选 + 排序 + 分页 |
| 搜索建议 | GET | `/api/search/suggest` | 关键词自动补全 |
| 搜索历史 | GET | `/api/search/history` | 登录用户最近 20 条 |
| 清除搜索历史 | DELETE | `/api/search/history` | 清空搜索历史 |
| 热搜榜 | GET | `/api/search/hot` | 近 7 天搜索频率 |

### 8.2 搜索参数 `api/search.ts`

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

// 搜索结果项
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

// 搜索结果分页结构
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

> 注：旧版「前端直连 ES（`/es` proxy）」方案已废弃，改由后端 `SearchController` 统一封装搜索逻辑。

---

## 九、图片上传（前端 → 后端 → 本地目录 / OSS）

### 9.1 上传接口 `api/upload.ts` 与页面内联用法

> 前端未抽离独立的 `ImageUpload` 组件，各页面（商品管理、账户设置、分类管理、订单详情等）直接内联 `el-upload`，通过 `api/upload.ts` 手动上传。认证头由 Axios 拦截器统一注入，无需手动设置。

上传接口封装：

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

页面内联用法（以商品管理 `ProductManageView.vue` 为例）：

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
    ElMessage.warning('仅支持 jpg/jpeg/png/webp 格式')
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
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
// 结算页（CheckoutView.vue）
const loadData = async () => {
  // 并行获取购物车、收货地址与防重复提交 Token
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
    ElMessage.warning('没有选中商品')
    return
  }
  submitting.value = true
  try {
    // 携带防重 Token + 地址 + 选中购物车项 ID 提交订单
    const res = await createOrder({
      token: submitToken.value,
      addressId: selectedAddress.value.id,
      cartItemIds: cartItems.value.map(i => i.id),
      remark: remark.value || undefined,
      userCouponId: selectedCouponId.value ?? undefined,
    })
    ElMessage.success('订单创建成功')
    router.replace(`/payment/${res.data.orderId}`)
  } catch {
    ElMessage.error('订单创建失败')
  } finally {
    submitting.value = false
  }
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

    # 商品图片单张 2MB、请求总大小 10MB
    client_max_body_size 10m;

    # History 模式 fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端 Spring Boot（含搜索 /api/search）
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 60s;
    }

    # 构建产物长缓存
    location /assets/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800, immutable";
    }

    # 聊天 WebSocket 代理
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

> 完整可部署配置见仓库根目录 [deploy/nginx.conf](../../deploy/nginx.conf)。

---

## 十三、页面路由总览

| 路径 | 页面 | 角色 | 说明 |
|------|------|------|------|
| `/` | 首页/商品列表 | 游客/用户/商家/管理员 | 分类浏览、搜索入口 |
| `/product/:id` | 商品详情 | 游客/用户/商家/管理员 | 含 SKU 选择、评论列表 |
| `/search` | 搜索结果 | 游客/用户/商家/管理员 | 关键词全文检索结果 |
| `/coupons` | 领券中心 | 游客/用户/商家/管理员 | 游客可浏览，领取需登录 |
| `/login` | 登录 | 游客 | 用户名密码 + 验证码 |
| `/register` | 注册 | 游客 | 选择角色 |
| `/forgot-password` | 忘记密码 | 游客 | 重置密码 |
| `/cart` | 购物车 | 用户/商家 | 需登录 |
| `/checkout` | 结算 | 用户/商家 | 确认地址、商品、金额 |
| `/payment/:orderId` | 支付 | 用户/商家 | 卡号/二维码支付 |
| `/orders` | 我的订单 | 用户/商家 | 订单列表 |
| `/payments` | 支付记录 | 用户/商家/管理员 | 支付方式、金额与时间 |
| `/order/:id` | 订单详情 | 用户/商家 | 含退款/退货入口 |
| `/refunds` | 我的退款/售后 | 用户/商家/管理员 | 退款/退货申请列表 |
| `/profile` | 个人信息 | 用户/商家/管理员 | 重定向至 `/settings` |
| `/settings` | 账户设置 | 用户/商家/管理员 | 偏好设置 + 头像上传 |
| `/address` | 收货地址 | 用户/商家 | 增删改查 |
| `/chat/:conversationId?` | 聊天 | 用户/商家 | WebSocket 即时通讯 |
| `/help/:type` | 帮助中心 | 游客/用户 | 静态帮助页 |
| `/merchant/apply` | 商家入驻 | 用户 | 提交入驻申请 |
| `/merchant/products` | 商品管理 | 商家 | 增删改查、上下架 |
| `/merchant/comments` | 评论管理 | 商家 | 按商品查看评论/追评并回复 |
| `/merchant/orders` | 订单管理 | 商家 | 发货操作 |
| `/merchant/refunds` | 退款审核 | 商家 | 审核退款退货 |
| `/merchant/stats` | 销售统计 | 商家 | 自己的销售数据 |
| `/admin/users` | 用户管理 | 管理员 | 启用/禁用 |
| `/admin/merchants` | 商家审核 | 管理员 | 审核入驻申请 |
| `/admin/categories` | 分类管理 | 管理员 | 三级分类增删改 |
| `/admin/products` | 商品管理 | 管理员 | 全局商品管理 |
| `/admin/orders` | 订单管理 | 管理员 | 全局订单管理 |
| `/admin/refunds` | 退款审核 | 管理员 | 审核全平台退款/退货 |
| `/admin/report` | 销售报表 | 管理员 | 全局销售统计 |
| `/admin/coupons` | 秒杀券管理 | 管理员 | 券的增删改查 |

---

## 十四、与后端数据交互汇总

| 前端操作 | HTTP 方法 | API 路径 | 请求体/参数 |
|----------|-----------|----------|-------------|
| 图形验证码 | GET | `/api/user/captcha` | — |
| 注册 | POST | `/api/user/register` | `{ username, password, phone, email, role, shopName, captchaUuid, captchaCode }`（商家注册需传 `role=2` 与 `shopName`） |
| 登录 | POST | `/api/user/login` | `{ username, password, captchaUuid, captchaCode }` |
| 获取用户信息 | GET | `/api/user/info` | — |
| 修改个人信息 | PUT | `/api/user/profile` | `{ nickname, phone, email, avatar }` |
| 修改密码 | PUT | `/api/user/password` | `{ oldPassword, newPassword }` |
| 退出登录 | POST | `/api/user/logout` | — |
| 获取重置密码验证码 | POST | `/api/user/forgot-code` | `{ account, captchaUuid, captchaCode }` |
| 重置密码 | POST | `/api/user/reset-password` | `{ uuid, code, newPassword }` |
| 商品列表 | GET | `/api/product/list` | `?categoryId=&page=&size=` |
| 商品详情 | GET | `/api/product/{id}` | — |
| 分类树 | GET | `/api/category/tree` | — |
| 子分类列表 | GET | `/api/category/children` | `?parentId=` |
| 搜索 | GET | `/api/search` | `?keyword=&categoryId=&brand=&minPrice=&maxPrice=&sort=&page=&size=` |
| 搜索建议 | GET | `/api/search/suggest` | `?keyword=` |
| 搜索历史 | GET | `/api/search/history` | — |
| 清除搜索历史 | DELETE | `/api/search/history` | — |
| 热搜榜 | GET | `/api/search/hot` | — |
| 获取购物车 | GET | `/api/cart/list` | — |
| 加入购物车 | POST | `/api/cart/add` | `{ productId, skuId, quantity }` |
| 更新购物车数量 | PUT | `/api/cart/update` | `{ cartItemId, quantity }` |
| 删除购物车 | DELETE | `/api/cart/{cartItemId}` | — |
| 选中/取消选中 | PUT | `/api/cart/select` | `{ cartItemIds, selected }` |
| 全选/取消全选 | PUT | `/api/cart/select-all` | `{ selected }` |
| 获取防重 Token | GET | `/api/order/submit-token` | — |
| 创建订单 | POST | `/api/order/create` | `{ token, addressId, cartItemIds, remark?, userCouponId? }` |
| 订单列表 | GET | `/api/order/list` | `?status=&page=&size=` |
| 订单详情 | GET | `/api/order/{id}` | — |
| 取消订单 | PUT | `/api/order/{id}/cancel` | `{ reason }` |
| 确认收货 | PUT | `/api/order/{id}/receive` | — |
| 删除订单 | DELETE | `/api/order/{id}` | — |
| 卡号支付 | POST | `/api/payment/card` | `{ orderId, cardNo, expireDate, cvv }` |
| 获取支付二维码 | POST | `/api/payment/qrcode` | `{ orderId }` |
| 确认二维码支付 | POST | `/api/payment/confirm-qrcode` | `{ orderId }` |
| 支付记录 | GET | `/api/payment/records` | `?page=&size=` |
| 上传图片 | POST | `/api/upload/image` | FormData(file) |
| 地址列表 | GET | `/api/address/list` | — |
| 地址详情 | GET | `/api/address/{id}` | — |
| 新增地址 | POST | `/api/address` | `{ receiverName, phone, province, city, district, detail, isDefault }` |
| 修改地址 | PUT | `/api/address/{id}` | 同上 |
| 删除地址 | DELETE | `/api/address/{id}` | — |
| 设为默认地址 | PUT | `/api/address/{id}/default` | — |
| 评论列表 | GET | `/api/comment/list/{productId}` | `?page=&size=&star=` |
| 发表评论 | POST | `/api/comment` | `{ productId, orderItemId, content, star, images }` |
| 追加评论 | POST | `/api/comment/append` | `{ commentId, content }` |
| 商家回复 | POST | `/api/comment/reply` | `{ parentId, content }` |
| 申请退款 | POST | `/api/refund` | `{ orderItemId, type, reason, amount }` |
| 我的退款列表 | GET | `/api/refund/my` | `?page=&size=` |
| 退款详情 | GET | `/api/refund/{id}` | — |
| 审核退款 | PUT | `/api/refund/{id}/review` | `{ approved, reviewReason }` |
| 填写退货物流 | PUT | `/api/refund/{id}/return` | `{ returnCompany, returnLogisticsNo }` |
| 确认收到退货 | PUT | `/api/refund/{id}/return-received` | — |
| 会话列表 | GET | `/api/chat/conversations` | — |
| 历史消息 | GET | `/api/chat/history/{conversationId}` | `?page=&size=` |
| 发送消息 | POST | `/api/chat/message` | `{ conversationId, content, type }`（仅 Mock 联调） |
| 创建会话 | POST | `/api/chat/conversation` | `{ merchantId, productId }` |
| 标记已读 | PUT | `/api/chat/read/{conversationId}` | — |
| 未读计数 | GET | `/api/chat/unread-count` | — |
| 秒杀券列表 | GET | `/api/seckill/list` | `?page=&size=` |
| 秒杀券详情 | GET | `/api/seckill/detail/{id}` | — |
| 抢领秒杀券 | POST | `/api/seckill/claim/{couponId}` | — |
| 我的优惠券 | GET | `/api/seckill/my-coupons` | `?page=&size=` |
| 创建秒杀券 | POST | `/api/seckill/create` | `{ name, ... }` |
| 编辑秒杀券 | PUT | `/api/seckill/update/{id}` | 同上 |
| 删除秒杀券 | DELETE | `/api/seckill/{id}` | — |
| 下单优惠计算 | POST | `/api/seckill/calculate` | `{ ... }` |
| 商家入驻申请 | POST | `/api/merchant/apply` | `{ shopName, contactPhone, businessLicense }` |
| 商家信息 | GET | `/api/merchant/info` | — |
| 更新商家信息 | PUT | `/api/merchant/info` | `{ shopName, contactPhone }` |
| 商家退款列表 | GET | `/api/merchant/refunds` | `?page=&size=&status=` |
| 商家商品列表 | GET | `/api/merchant/products` | `?page=&size=&status=` |
| 商家订单列表 | GET | `/api/merchant/orders` | `?page=&size=&status=` |
| 发货 | PUT | `/api/merchant/ship/{orderId}` | — |
| 商家销售统计 | GET | `/api/merchant/stats` | `?startDate=&endDate=` |
| 添加商品 | POST | `/api/product` | `{ ...ProductFormData }` |
| 修改商品 | PUT | `/api/product/{id}` | 同上 |
| 商品上下架 | PUT | `/api/product/{id}/status` | `{ status }` |
| 删除商品 | DELETE | `/api/product/{id}` | — |
| 用户列表 | GET | `/api/admin/users` | `?page=&size=&keyword=&role=&status=` |
| 设置用户状态 | PUT | `/api/admin/users/{id}/status` | `{ status }` |
| 商家列表 | GET | `/api/admin/merchants` | `?status=&page=&size=` |
| 审核商家 | PUT | `/api/admin/merchants/{id}/review` | `{ approved, reviewReason }` |
| 创建分类 | POST | `/api/admin/categories` | `{ name, parentId, level, sortOrder, icon }` |
| 更新分类 | PUT | `/api/admin/categories/{id}` | 同上 |
| 删除分类 | DELETE | `/api/admin/categories/{id}` | — |
| 管理员商品列表 | GET | `/api/admin/products` | `?page=&size=&keyword=&categoryId=&merchantId=&status=` |
| 管理员订单列表 | GET | `/api/admin/orders` | `?page=&size=&orderNo=&status=` |
| 管理员销售报表 | GET | `/api/admin/report` | `?startDate=&endDate=&groupBy=` |
| 管理员退款列表 | GET | `/api/admin/refunds` | `?page=&size=&status=` |
| 管理员修改商品 | PUT | `/api/admin/products/{id}` | `{ ...ProductFormData }` |
| 管理员商品上下架 | PUT | `/api/admin/products/{id}/status` | `{ status }` |
| 管理员删除商品 | DELETE | `/api/admin/products/{id}` | — |
| WebSocket 连接 | WS | `/chat/{userId}?token={jwt}` | — |

---

## 十五、前端增强能力（当前实现）

以下能力均已接入正式后端；`npm run dev:mock` 仍可在不启动后端时独立演示。

### 15.1 商家多规格 SKU

- 商品新增/编辑弹窗支持维护多个规格组（规格名 + 逗号分隔的规格值），自动生成笛卡尔积 SKU 组合。
- 每个 SKU 可单独设置价格与库存；提交时转换为后端 `ProductCreateDTO.skuList`。
- 编辑商品时根据已有 `specText` 反推规格组，保留原 SKU 价格/库存。
- 对应文件：`src/views/merchant/ProductManageView.vue`、`mock/product.ts`。

### 15.2 报表图表

- 引入 ECharts 6（按需引入），封装 `src/components/chart/BaseChart.vue`。
- 管理员销售报表：销售趋势折线图、热销商品柱状图、分类销售饼图、商家销售柱状图。
- 商家销售统计：销售趋势折线图、热销商品柱状图、分类销售饼图。
- 图表与明细表格并存；金额统一按元展示。

### 15.3 卡号支付前端校验

- `src/utils/luhn.ts` 提供卡号格式化、Luhn 校验、有效期 MM/YY 校验与 CVV 校验。
- 收银台输入自动格式化，提交前完成前端预校验并给出具体提示；后端仍做最终校验。

### 15.4 搜索筛选与三级分类

- 搜索结果页新增级联分类、品牌、价格区间筛选，支持重置与分类路径展示。
- 搜索与筛选优先调用 `/api/search`（Elasticsearch）；接口异常时降级为 `/api/product/list` 已加载商品的前端过滤。
- 顶部导航与首页分类侧边栏支持 hover 浮层，可直接选择二级/三级分类。
- 对应文件：`src/views/product/SearchResultView.vue`、`src/components/category/CategoryFlyout.vue`。

### 15.5 商品详情富文本与规格参数

- 商品详情标签页渲染清洗后的富文本描述（`src/utils/sanitizeHtml.ts`），移除脚本与事件属性。
- 规格参数标签页展示品牌、分类、店铺、销量及全部 SKU 价格/库存。

### 15.6 首页商品分页

- “猜你喜欢”区域按页加载（每页 10 条）并支持分页；“热销推荐”按销量排序展示前 10 条。

### 15.7 Mock 调试模式

- `npm run dev` 连接真实后端；`npm run dev:mock` 使用 `mock/` 目录，互不影响。
- Mock 商品创建/编辑已持久化多 SKU，并自动计算最低/最高价；管理员报表字段与后端 `AdminReportVO` 对齐。

> 说明：商家退款审核列表、管理员商品上下架/删除等能力仍受后端扩展接口限制，相关入口在 `dev:mock` 下通过 Mock 演示。


### 15.8 聊天联调

- 商城顶部消息入口、用户菜单“我的消息”默认开启；未读角标调用 `GET /api/chat/unread-count`。
- 聊天页支持会话列表、历史消息、实时收发、标记已读；`npm run dev` / 生产环境通过 WebSocket 直连后端，`dev:mock` 下才使用 `POST /api/chat/message`。
- 商品详情页店铺卡片新增“联系客服”：登录后调用 `POST /api/chat/conversation` 创建/复用会话并跳转聊天页。
- 未读角标随 WebSocket 实时消息刷新，打开会话后自动清零。

### 15.9 退款审核（商家/管理员）

- 商家后台“退款审核”与管理员后台“退款审核”复用同一页面 `RefundManageView.vue`，根据路由前缀选择数据源。
- 列表支持按状态筛选，同意/拒绝退款并填写拒绝原因；审核后列表状态立即更新。
- 列表接口：`GET /api/merchant/refunds`、`GET /api/admin/refunds`；审核复用 `PUT /api/refund/{id}/review`，`dev:mock` 下由 Mock 提供同名接口。

### 15.10 管理员商品操作

- 全平台商品列表支持上架/下架与删除，操作前二次确认。
- 接口：`PUT /api/admin/products/{id}/status`、`DELETE /api/admin/products/{id}`，`dev:mock` 下由 Mock 提供同名接口。

### 15.11 搜索接口接入与降级

- 有关键词时调用 `/api/search`（分类/品牌/价格/排序/分页），建议、历史、热搜分别调用 15.2-15.5 接口。
- 仅按分类浏览或无关键词筛选时使用 `/api/product/list` 本地过滤；搜索接口不可用时同样自动降级。
- 分类筛选在 Mock 中按分类子树展开，与首页/顶部分类导航的三级分类行为一致。

### 15.12 支付记录与订单量趋势

- 新增 `/payments` 支付记录页，展示支付方式、脱敏卡号、金额、状态与时间，后端提供 `GET /api/payment/records`。
- 管理员销售报表新增“订单量趋势”折线图（下单量/支付量/完成量），后端 `AdminReportVO.orderTrend` 与 Mock 数据同步。
- 搜索排序新增“上架时间”，分类导航优先渲染 `category.icon`。

### 15.13 退货寄回、管理员编辑与评论图片

- 退货退款审核通过后进入“待寄回”，买家在退款/售后页填写快递公司与物流单号；商家/管理员在退款审核页确认收货后完成退款。
- 管理员全平台商品列表支持编辑商品与 SKU 价格/库存，后端提供 `PUT /api/admin/products/{id}`。
- 发表评价支持上传最多 4 张图片，商品详情评论列表展示图片。
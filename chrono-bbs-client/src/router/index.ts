import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    roles?: string[]
    layout?: 'mall' | 'merchant' | 'admin'
    public?: boolean
  }
}

// 公开路由（无需登录）：商城浏览 + 登录注册
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/RegisterView.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/user/ForgotPasswordView.vue'),
    meta: { title: '忘记密码' },
  },
]

// 需登录路由（商城布局）
const userRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/MallLayout.vue'),
    meta: { layout: 'mall' },
    children: [
      {
        path: '',
        redirect: '/home',
      },
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/ProductDetailView.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'search',
        name: 'SearchResult',
        component: () => import('@/views/product/SearchResultView.vue'),
        meta: { title: '搜索结果' },
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/cart/CartView.vue'),
        meta: { title: '购物车', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/order/CheckoutView.vue'),
        meta: { title: '结算', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderListView.vue'),
        meta: { title: '我的订单', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetailView.vue'),
        meta: { title: '订单详情', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'payment/:orderId',
        name: 'Payment',
        component: () => import('@/views/order/PaymentView.vue'),
        meta: { title: '支付', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人信息', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/user/AddressView.vue'),
        meta: { title: '收货地址', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
      {
        path: 'chat/:conversationId?',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: '聊天', roles: ['USER', 'MERCHANT', 'ADMIN'] },
      },
    ],
  },
]

// 商家后台路由
const merchantRoutes: RouteRecordRaw[] = [
  {
    path: '/merchant',
    component: () => import('@/components/layout/MerchantLayout.vue'),
    meta: { layout: 'merchant', roles: ['MERCHANT'] },
    children: [
      {
        path: 'apply',
        name: 'MerchantApply',
        component: () => import('@/views/merchant/MerchantApplyView.vue'),
        meta: { title: '商家入驻', roles: ['USER', 'MERCHANT'] },
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/merchant/ProductManageView.vue'),
        meta: { title: '商品管理' },
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderManageView.vue'),
        meta: { title: '订单管理' },
      },
      {
        path: 'refunds',
        name: 'RefundManage',
        component: () => import('@/views/merchant/RefundManageView.vue'),
        meta: { title: '退款审核' },
      },
      {
        path: 'stats',
        name: 'SalesStats',
        component: () => import('@/views/merchant/SalesStatsView.vue'),
        meta: { title: '销售统计' },
      },
    ],
  },
]

// 管理员后台路由
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { layout: 'admin', roles: ['ADMIN'] },
    children: [
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'merchants',
        name: 'MerchantAudit',
        component: () => import('@/views/admin/MerchantAuditView.vue'),
        meta: { title: '商家审核' },
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManageView.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'products',
        name: 'ProductAll',
        component: () => import('@/views/admin/ProductAllView.vue'),
        meta: { title: '商品管理' },
      },
      {
        path: 'orders',
        name: 'OrderAll',
        component: () => import('@/views/admin/OrderAllView.vue'),
        meta: { title: '订单管理' },
      },
      {
        path: 'report',
        name: 'SalesReport',
        component: () => import('@/views/admin/SalesReportView.vue'),
        meta: { title: '销售报表' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...publicRoutes,
    ...userRoutes,
    ...merchantRoutes,
    ...adminRoutes,
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

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

  if (!roles.includes(userStore.role ?? '')) {
    ElMessage.warning('无权限访问')
    return next('/home')
  }

  next()
})

export default router

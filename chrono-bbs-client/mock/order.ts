import { MockMethod } from 'vite-plugin-mock'
import Mock from 'mockjs'
import { allProducts } from './product'

function getProductInfo(productId: number) {
  const p = allProducts.find(item => item.id === productId)
  if (!p) {
    return {
      name: `商品${productId}`,
      image: '',
      price: productId * 1000,
    }
  }
  return {
    name: p.name,
    image: '',
    price: p.minPrice,
  }
}

const op1 = getProductInfo(1)
const op2 = getProductInfo(2)
const op3 = getProductInfo(3)
const op4 = getProductInfo(4)
const op5 = getProductInfo(5)
const op6 = getProductInfo(6)

// 订单数据
let orders = [
  {
    id: 1001,
    orderNo: '202501011000001001',
    userId: 100,
    addressSnapshot: JSON.stringify({
      receiverName: '张三',
      phone: '13800138000',
      province: '北京市',
      city: '北京市',
      district: '朝阳区',
      detail: 'XXX街道XXX号',
    }),
    totalAmount: op1.price * 2 + op2.price, // 分
    status: 1, // 1=待支付
    cancelReason: null,
    paidAt: null,
    shippedAt: null,
    receivedAt: null,
    completedAt: null,
    createdAt: '2025-01-01 10:00:00',
    updatedAt: '2025-01-01 10:00:00',
    // 订单项
    items: [
      {
        id: 10001,
        orderId: 1001,
        productId: 1,
        skuId: 11,
        quantity: 2,
        unitPrice: op1.price,
        subtotal: op1.price * 2,
        productName: op1.name,
        productImage: op1.image, // 图片为空
        skuSpecText: '默认规格',
      },
      {
        id: 10002,
        orderId: 1001,
        productId: 2,
        skuId: 21,
        quantity: 1,
        unitPrice: op2.price,
        subtotal: op2.price,
        productName: op2.name,
        productImage: op2.image,
        skuSpecText: '规格2',
      },
    ],
  },
  {
    id: 1002,
    orderNo: '202501011100001002',
    userId: 100,
    addressSnapshot: JSON.stringify({
      receiverName: '李四',
      phone: '13900139000',
      province: '上海市',
      city: '上海市',
      district: '浦东新区',
      detail: 'YYY路YYY号',
    }),
    totalAmount: op3.price,
    status: 2, // 2=已支付（待发货）
    cancelReason: null,
    paidAt: '2025-01-01 11:05:00',
    shippedAt: null,
    receivedAt: null,
    completedAt: null,
    createdAt: '2025-01-01 11:00:00',
    updatedAt: '2025-01-01 11:05:00',
    items: [
      {
        id: 10003,
        orderId: 1002,
        productId: 3,
        skuId: 31,
        quantity: 1,
        unitPrice: op3.price,
        subtotal: op3.price,
        productName: op3.name,
        productImage: op3.image,
        skuSpecText: '默认规格',
      },
    ],
  },
  {
    id: 1003,
    orderNo: '202501011200001003',
    userId: 100,
    addressSnapshot: JSON.stringify({
      receiverName: '王五',
      phone: '13700137000',
      province: '广州市',
      city: '广州市',
      district: '天河区',
      detail: 'ZZZ路ZZZ号',
    }),
    totalAmount: op4.price,
    status: 3, // 3=已发货（待收货）
    cancelReason: null,
    paidAt: '2025-01-01 12:05:00',
    shippedAt: '2025-01-01 14:00:00',
    receivedAt: null,
    completedAt: null,
    createdAt: '2025-01-01 12:00:00',
    updatedAt: '2025-01-01 14:00:00',
    items: [
      {
        id: 10004,
        orderId: 1003,
        productId: 4,
        skuId: 41,
        quantity: 1,
        unitPrice: op4.price,
        subtotal: op4.price,
        productName: op4.name,
        productImage: op4.image,
        skuSpecText: '默认规格',
      },
    ],
  },
  {
    id: 1004,
    orderNo: '202501011300001004',
    userId: 100,
    addressSnapshot: JSON.stringify({
      receiverName: '赵六',
      phone: '13600136000',
      province: '深圳市',
      city: '深圳市',
      district: '南山区',
      detail: 'AAA路AAA号',
    }),
    totalAmount: op5.price * 2,
    status: 4, // 4=已完成
    cancelReason: null,
    paidAt: '2025-01-01 13:05:00',
    shippedAt: '2025-01-01 15:00:00',
    receivedAt: '2025-01-02 10:00:00',
    completedAt: '2025-01-02 10:00:00',
    createdAt: '2025-01-01 13:00:00',
    updatedAt: '2025-01-02 10:00:00',
    items: [
      {
        id: 10005,
        orderId: 1004,
        productId: 5,
        skuId: 51,
        quantity: 2,
        unitPrice: op5.price,
        subtotal: op5.price * 2,
        productName: op5.name,
        productImage: op5.image,
        skuSpecText: '默认规格',
      },
    ],
  },
  {
    id: 1005,
    orderNo: '202501011400001005',
    userId: 100,
    addressSnapshot: JSON.stringify({
      receiverName: '孙七',
      phone: '13500135000',
      province: '杭州市',
      city: '杭州市',
      district: '西湖区',
      detail: 'BBB路BBB号',
    }),
    totalAmount: op6.price,
    status: 5, // 5=已取消
    cancelReason: '不想要了',
    paidAt: null,
    shippedAt: null,
    receivedAt: null,
    completedAt: null,
    createdAt: '2025-01-01 14:00:00',
    updatedAt: '2025-01-01 14:30:00',
    items: [
      {
        id: 10006,
        orderId: 1005,
        productId: 6,
        skuId: 61,
        quantity: 1,
        unitPrice: op6.price,
        subtotal: op6.price,
        productName: op6.name,
        productImage: op6.image,
        skuSpecText: '默认规格',
      },
    ],
  },
]

export default [
  // 6.1 获取防重复提交 Token
  {
    url: '/api/order/submit-token',
    method: 'get',
    timeout: 200,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          token: Mock.Random.guid(),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 6.2 创建订单
  {
    url: '/api/order/create',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      const { addressId, cartItemIds, remark } = body
      const orderId = 1000 + orders.length + 1
      const orderNo = `20250101${String(Date.now()).slice(-6)}${orderId}`
      
      const newOrder = {
        id: orderId,
        orderNo,
        userId: 100,
        addressSnapshot: JSON.stringify({
          receiverName: '测试用户',
          phone: '13800138000',
          province: '北京市',
          city: '北京市',
          district: '朝阳区',
          detail: '测试地址',
        }),
        items: cartItemIds.map((id: number, index: number) => {
          const info = getProductInfo(id)
          return {
            id: orderId * 10 + index + 1,
            orderId,
            productId: id,
            skuId: id * 10 + 1,
            quantity: 1,
            unitPrice: info.price,
            subtotal: info.price,
            productName: info.name,
            productImage: info.image,
            skuSpecText: '默认规格',
          }
        }),
        totalAmount: cartItemIds.reduce((sum: number, id: number) => sum + getProductInfo(id).price, 0),
        status: 1, // 待支付
        cancelReason: null,
        paidAt: null,
        shippedAt: null,
        receivedAt: null,
        completedAt: null,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      }
      
      orders.unshift(newOrder)
      
      return {
        code: 200,
        message: '订单创建成功',
        data: {
          orderId: newOrder.id,
          orderNo: newOrder.orderNo,
          totalAmount: newOrder.totalAmount,
          status: newOrder.status,
          createdAt: newOrder.createdAt,
        },
        timestamp: Date.now(),
      }
    },
  },
  // 6.3 订单列表
  {
    url: '/api/order/list',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const status = query.status ? parseInt(query.status) : null
      
      let filtered = [...orders]
      
      if (status) {
        filtered = filtered.filter(o => o.status === status)
      }
      
      const total = filtered.length
      const start = (page - 1) * size
      const end = start + size
      const list = filtered.slice(start, end)
      
      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page,
          size,
          list: list.map(o => ({
            id: o.id,
            orderNo: o.orderNo,
            totalAmount: o.totalAmount,
            status: o.status,
            createdAt: o.createdAt,
            itemCount: o.items.length,
            firstItemName: o.items[0]?.productName,
            firstItemImage: o.items[0]?.productImage || '',
          })),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 6.4 订单详情
  {
    url: '/api/order/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const orderId = parseInt(query.id)
      const order = orders.find(o => o.id === orderId)
      
      if (order) {
        return {
          code: 200,
          message: 'success',
          data: order,
          timestamp: Date.now(),
        }
      }
      
      return {
        code: 404,
        message: '订单不存在',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 6.5 取消订单
  {
    url: '/api/order/:id/cancel',
    method: 'put',
    timeout: 300,
    response: ({ body }) => {
      return {
        code: 200,
        message: '订单已取消',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 6.6 确认收货
  {
    url: '/api/order/:id/receive',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '确认收货成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 6.7 删除订单
  {
    url: '/api/order/:id',
    method: 'delete',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '订单已删除',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
] as MockMethod[]
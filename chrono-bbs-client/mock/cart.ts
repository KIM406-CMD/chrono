import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

function getProductInfo(productId: number) {
  const p = allProducts.find(item => item.id === productId)
  if (!p) {
    return {
      name: `商品${productId}`,
      image: '',
      price: productId * 1000,
      merchantId: (productId % 5) + 1,
      merchantName: `店铺${(productId % 5) + 1}`,
    }
  }
  return {
    name: p.name,
    image: '',
    price: p.minPrice,
    merchantId: p.merchantId,
    merchantName: p.merchantName,
  }
}

const p1 = getProductInfo(1)
const p2 = getProductInfo(2)
const p3 = getProductInfo(3)

// 购物车数据（模拟数据库存储）
let cartItems = [
  {
    id: 1,
    userId: 100,
    productId: 1,
    skuId: 11,
    quantity: 2,
    selected: 1, // 1=已选中, 0=未选中
    createdAt: '2025-01-01 10:00:00',
    // 关联商品信息（后端 VO 返回）
    productName: p1.name,
    productImage: p1.image, // 图片为空
    skuSpecText: '默认规格',
    skuPrice: p1.price, // 分
    skuStock: 100,
    merchantId: p1.merchantId,
    merchantName: p1.merchantName,
  },
  {
    id: 2,
    userId: 100,
    productId: 2,
    skuId: 21,
    quantity: 1,
    selected: 1,
    createdAt: '2025-01-01 11:00:00',
    productName: p2.name,
    productImage: p2.image,
    skuSpecText: '规格2',
    skuPrice: p2.price,
    skuStock: 50,
    merchantId: p2.merchantId,
    merchantName: p2.merchantName,
  },
  {
    id: 3,
    userId: 100,
    productId: 3,
    skuId: 31,
    quantity: 1,
    selected: 0,
    createdAt: '2025-01-01 12:00:00',
    productName: p3.name,
    productImage: p3.image,
    skuSpecText: '默认规格',
    skuPrice: p3.price,
    skuStock: 0, // 库存不足
    merchantId: p3.merchantId,
    merchantName: p3.merchantName,
  },
]

export default [
  // 5.1 获取购物车列表
  {
    url: '/api/cart/list',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: cartItems,
        timestamp: Date.now(),
      }
    },
  },
  // 5.2 加入购物车
  {
    url: '/api/cart/add',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const { productId, skuId, quantity } = body
      
      // 检查是否已存在相同 SKU
      const existingItem = cartItems.find(item => item.skuId === skuId)
      
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        const info = getProductInfo(productId)
        cartItems.push({
          id: cartItems.length + 1,
          userId: 100,
          productId,
          skuId,
          quantity,
          selected: 1,
          createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
          productName: info.name,
          productImage: info.image,
          skuSpecText: '默认规格',
          skuPrice: info.price,
          skuStock: 100,
          merchantId: info.merchantId,
          merchantName: info.merchantName,
        })
      }
      
      return {
        code: 200,
        message: '加入购物车成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 5.3 更新购物车数量
  {
    url: '/api/cart/update',
    method: 'put',
    timeout: 300,
    response: ({ body }) => {
      const { cartItemId, quantity } = body
      const item = cartItems.find(i => i.id === cartItemId)
      
      if (item) {
        item.quantity = quantity
      }
      
      return {
        code: 200,
        message: '更新成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 5.4 删除购物车商品
  {
    url: '/api/cart/:id',
    method: 'delete',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '删除成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 5.5 选中/取消选中
  {
    url: '/api/cart/select',
    method: 'put',
    timeout: 300,
    response: ({ body }) => {
      const { cartItemIds, selected } = body
      
      if (cartItemIds) {
        // 选中指定商品
        cartItems.forEach(item => {
          if (cartItemIds.includes(item.id)) {
            item.selected = selected ? 1 : 0
          }
        })
      } else {
        // 全选/取消全选
        cartItems.forEach(item => {
          item.selected = selected ? 1 : 0
        })
      }
      
      return {
        code: 200,
        message: '操作成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 5.6 清空购物车
  {
    url: '/api/cart/clear',
    method: 'delete',
    timeout: 300,
    response: () => {
      cartItems = []
      return {
        code: 200,
        message: '清空成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
] as MockMethod[]
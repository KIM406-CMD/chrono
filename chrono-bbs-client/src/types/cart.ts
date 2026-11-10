// 购物车项
export interface CartItem {
  id: number
  userId: number
  productId: number
  skuId: number
  quantity: number
  selected: number  // 1=已选中, 0=未选中
  createdAt: string
  // 关联商品信息
  productName: string
  productImage: string
  skuSpecText: string
  skuPrice: number
  skuStock: number
  merchantId: number
  merchantName: string
}

// 加入购物车请求
export interface AddCartRequest {
  productId: number
  skuId: number
  quantity: number
}

// 更新购物车请求
export interface UpdateCartRequest {
  cartItemId: number
  quantity: number
}

// 选中购物车请求
export interface SelectCartRequest {
  cartItemIds?: number[]
  selected: boolean
}
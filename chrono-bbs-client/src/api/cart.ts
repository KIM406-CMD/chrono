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

// 清空购物车
export const clearCart = () => {
  return service.delete('/cart/clear')
}
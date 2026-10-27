import service from '@/api'
import type { Order, OrderListItem, CreateOrderRequest, OrderQueryParams, CreateOrderResponse } from '@/types/order'
import type { PageResult } from '@/types/common'

// 获取防重复提交 Token
export const getSubmitToken = () => {
  return service.get<any, { code: number; message: string; data: { token: string }; timestamp: number }>('/order/submit-token')
}

// 创建订单
export const createOrder = (data: CreateOrderRequest) => {
  return service.post<any, { code: number; message: string; data: CreateOrderResponse; timestamp: number }>('/order/create', data)
}

// 订单列表
export const getOrderList = (params: OrderQueryParams) => {
  return service.get<any, { code: number; message: string; data: PageResult<OrderListItem>; timestamp: number }>('/order/list', { params })
}

// 订单详情
export const getOrderDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: Order; timestamp: number }>(`/order/${id}`)
}

// 取消订单
export const cancelOrder = (id: number, reason?: string) => {
  return service.put(`/order/${id}/cancel`, { reason })
}

// 确认收货
export const receiveOrder = (id: number) => {
  return service.put(`/order/${id}/receive`)
}

// 删除订单
export const deleteOrder = (id: number) => {
  return service.delete(`/order/${id}`)
}
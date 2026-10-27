import service from '@/api'
import type { Address, CreateAddressRequest, UpdateAddressRequest } from '@/types/address'

// 地址列表
export const getAddressList = () => {
  return service.get<any, { code: number; message: string; data: Address[]; timestamp: number }>('/address/list')
}

// 地址详情
export const getAddressDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: Address; timestamp: number }>(`/address/${id}`)
}

// 新增地址
export const createAddress = (data: CreateAddressRequest) => {
  return service.post<any, { code: number; message: string; data: Address; timestamp: number }>('/address', data)
}

// 修改地址
export const updateAddress = (data: UpdateAddressRequest) => {
  return service.put<any, { code: number; message: string; data: Address; timestamp: number }>(`/address/${data.id}`, data)
}

// 删除地址
export const deleteAddress = (id: number) => {
  return service.delete(`/address/${id}`)
}

// 设为默认地址
export const setDefaultAddress = (id: number) => {
  return service.put(`/address/${id}/default`)
}
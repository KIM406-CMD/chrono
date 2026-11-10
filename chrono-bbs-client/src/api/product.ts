import service from '@/api'
import type { Product, ProductQueryParams, Category, ProductFormData } from '@/types/product'
import type { PageResult } from '@/types/common'

// 分页查询商品列表
export const getProductList = (params: ProductQueryParams) => {
  return service.get<any, { code: number; message: string; data: PageResult<Product>; timestamp: number }>('/product/list', { params })
}

// 商品详情
export const getProductDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: Product; timestamp: number }>(`/product/${id}`)
}

// 获取分类树
export const getCategoryTree = () => {
  return service.get<any, { code: number; message: string; data: Category[]; timestamp: number }>('/category/tree')
}

// 获取子分类列表
export const getCategoryChildren = (parentId: number) => {
  return service.get<any, { code: number; message: string; data: Category[]; timestamp: number }>('/category/children', {
    params: { parentId },
  })
}

// 4.3 添加商品
export const createProduct = (data: ProductFormData) => {
  return service.post('/product', data)
}

// 4.4 修改商品
export const updateProduct = (id: number, data: ProductFormData) => {
  return service.put(`/product/${id}`, data)
}

// 4.5 商品上下架
export const updateProductStatus = (id: number, status: number) => {
  return service.put(`/product/${id}/status`, { status })
}

// 4.6 删除商品
export const deleteProduct = (id: number) => {
  return service.delete(`/product/${id}`)
}
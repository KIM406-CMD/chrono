import service from '@/api'
import type { Product, ProductQueryParams, Category } from '@/types/product'
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
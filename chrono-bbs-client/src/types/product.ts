import type { ProductStatus } from './common'

// 分类
export interface Category {
  id: number
  name: string
  level: number
  parentId?: number
  children?: Category[]
}

// SKU
export interface Sku {
  id: number
  productId: number
  specIds: string
  specText: string
  price: number
  stock: number
  status: number
}

// 规格
export interface Spec {
  id: number
  name: string
  values: SpecValue[]
}

// 规格值
export interface SpecValue {
  id: number
  value: string
}

// 商品
export interface Product {
  id: number
  name: string
  description: string
  images: string[]
  brand: string
  categoryId: number
  categoryName: string
  merchantId: number
  merchantName: string
  status: ProductStatus
  sales: number
  minPrice: number
  maxPrice: number
  createdAt: string
  updatedAt: string
  skus?: Sku[]
  specs?: Spec[]
}

// 商品列表查询参数
export interface ProductQueryParams {
  page?: number
  size?: number
  categoryId?: number
  keyword?: string
  brand?: string
  minPrice?: number
  maxPrice?: number
  sort?: string
}

// 创建/修改商品 SKU 项
export interface ProductSkuItem {
  specIds: string
  specText: string
  price: number
  stock: number
}

// 创建/修改商品请求
export interface ProductFormData {
  name: string
  description?: string
  images?: string
  brand?: string
  categoryId: number
  skuList: ProductSkuItem[]
}
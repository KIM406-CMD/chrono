import service from '@/api'

// 搜索接口统一返回结构
interface SearchResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 搜索查询参数
export interface SearchParams {
  keyword: string
  categoryId?: number
  brand?: string
  minPrice?: number
  maxPrice?: number
  sort?: 'sales_desc' | 'price_asc' | 'price_desc' | 'time_desc'
  page?: number
  size?: number
}

// 搜索结果项（文档 15.1 返回结构）
export interface SearchItem {
  id: number
  name: string
  description?: string
  brand?: string
  categoryId?: number
  categoryName?: string
  minPrice: number
  sales: number
  image?: string
}

// 搜索结果分页结构
export interface SearchResult {
  total: number
  page: number
  size: number
  list: SearchItem[]
}

// 15.1 全文检索
export const searchProducts = (params: SearchParams) => {
  return service.get<any, SearchResponse<SearchResult>>('/search', { params })
}

// 15.2 搜索建议（自动补全）
export const getSearchSuggest = (keyword: string) => {
  return service.get<any, SearchResponse<string[]>>('/search/suggest', {
    params: { keyword },
  })
}

// 15.3 搜索历史（仅登录用户，返回最近 20 条）
export const getSearchHistory = () => {
  return service.get<any, SearchResponse<string[]>>('/search/history')
}

// 15.4 清除搜索历史
export const clearSearchHistory = () => {
  return service.delete<any, SearchResponse<null>>('/search/history')
}

// 15.5 热搜榜（公开接口，基于近 7 天搜索频率）
export const getHotSearch = () => {
  return service.get<any, SearchResponse<string[]>>('/search/hot')
}
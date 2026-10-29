import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

const HOT_KEYWORDS = ['智能手机', '笔记本电脑', 'T恤', '坚果炒货', '平板电脑', '华为', '小米', '苹果']

let searchHistory: string[] = ['智能手机', '华为', 'T恤']

const SUGGEST_POOL = allProducts.map(p => p.name)

const mockMethods: MockMethod[] = [
  // 15.2 搜索建议（自动补全）
  {
    url: '/api/search/suggest',
    method: 'get',
    timeout: 200,
    response: ({ query }) => {
      const keyword = String(query.keyword || '').trim().toLowerCase()
      if (!keyword) {
        return { code: 200, message: 'success', data: [], timestamp: Date.now() }
      }
      const matches = SUGGEST_POOL.filter(name => name.toLowerCase().includes(keyword)).slice(0, 8)
      const data = matches.length ? matches : HOT_KEYWORDS.filter(k => k.toLowerCase().includes(keyword)).slice(0, 8)
      return { code: 200, message: 'success', data, timestamp: Date.now() }
    },
  },
  // 15.3 搜索历史
  {
    url: '/api/search/history',
    method: 'get',
    timeout: 200,
    response: () => {
      return { code: 200, message: 'success', data: searchHistory.slice(0, 20), timestamp: Date.now() }
    },
  },
  // 15.4 清除搜索历史
  {
    url: '/api/search/history',
    method: 'delete',
    timeout: 200,
    response: () => {
      searchHistory = []
      return { code: 200, message: '已清除', data: null, timestamp: Date.now() }
    },
  },
  // 15.5 热搜榜
  {
    url: '/api/search/hot',
    method: 'get',
    timeout: 200,
    response: () => {
      return { code: 200, message: 'success', data: HOT_KEYWORDS, timestamp: Date.now() }
    },
  },
  // 15.1 全文检索（需放在最后，避免前缀匹配影响上面的路由）
  {
    url: '/api/search',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const keyword = String(query.keyword || '').trim()
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 20
      const categoryId = query.categoryId ? parseInt(query.categoryId) : null
      const brand = query.brand ? String(query.brand) : null
      const minPrice = query.minPrice ? parseInt(query.minPrice) : null
      const maxPrice = query.maxPrice ? parseInt(query.maxPrice) : null
      const sort = query.sort ? String(query.sort) : null

      if (!keyword) {
        return { code: 400, message: '搜索关键词不能为空', data: null, timestamp: Date.now() }
      }

      if (!searchHistory.includes(keyword)) {
        searchHistory = [keyword, ...searchHistory].slice(0, 20)
      }

      const kw = keyword.toLowerCase()
      let filtered = allProducts.filter(
        p =>
          p.status === 1 &&
          (p.name.toLowerCase().includes(kw) ||
            (p.description && p.description.toLowerCase().includes(kw)) ||
            (p.brand && p.brand.toLowerCase().includes(kw))),
      )

      if (categoryId) filtered = filtered.filter(p => p.categoryId === categoryId)
      if (brand) filtered = filtered.filter(p => p.brand === brand)
      if (minPrice !== null) filtered = filtered.filter(p => p.minPrice >= minPrice)
      if (maxPrice !== null) filtered = filtered.filter(p => p.minPrice <= maxPrice)

      if (sort === 'price_asc') filtered.sort((a, b) => a.minPrice - b.minPrice)
      else if (sort === 'price_desc') filtered.sort((a, b) => b.minPrice - a.minPrice)
      else if (sort === 'sales_desc') filtered.sort((a, b) => b.sales - a.sales)
      else if (sort === 'time_desc') filtered.sort((a, b) => b.id - a.id)

      const total = filtered.length
      const list = filtered.slice((page - 1) * size, page * size).map(p => ({
        id: p.id,
        name: p.name,
        description: p.description,
        brand: p.brand,
        categoryId: p.categoryId,
        minPrice: p.minPrice,
        sales: p.sales,
        image: '',
      }))

      return { code: 200, message: 'success', data: { total, page, size, list }, timestamp: Date.now() }
    },
  },
]
export default mockMethods
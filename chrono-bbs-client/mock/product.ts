import { MockMethod } from 'vite-plugin-mock'

// 分类层级关系（用于父分类过滤时包含所有子分类）
const categoryTree = [
  { id: 1, name: '电子产品', children: [
    { id: 2, name: '手机通讯', children: [
      { id: 3, name: '智能手机' },
      { id: 4, name: '功能手机' },
    ]},
    { id: 5, name: '电脑办公', children: [
      { id: 6, name: '笔记本电脑' },
      { id: 7, name: '平板电脑' },
    ]},
  ]},
  { id: 8, name: '服装鞋帽', children: [
    { id: 9, name: '男装', children: [
      { id: 10, name: 'T恤' },
      { id: 11, name: '衬衫' },
    ]},
  ]},
  { id: 12, name: '食品饮料', children: [
    { id: 13, name: '休闲零食', children: [
      { id: 14, name: '坚果炒货' },
      { id: 15, name: '糖果巧克力' },
    ]},
  ]},
  { id: 16, name: '家居用品', children: [] },
]

// 收集分类及其所有子孙分类的 ID
function collectCategoryIds(rootId: number): number[] {
  const ids: number[] = []
  const findAndCollect = (nodes: any[]): boolean => {
    for (const n of nodes) {
      if (n.id === rootId) {
        ids.push(n.id)
        const collectDescendants = (desc: any[]) => {
          for (const d of desc) {
            ids.push(d.id)
            if (d.children) collectDescendants(d.children)
          }
        }
        if (n.children) collectDescendants(n.children)
        return true
      }
      if (n.children && findAndCollect(n.children)) return true
    }
    return false
  }
  findAndCollect(categoryTree)
  return ids
}

// 生成商品数据
function generateProducts(count: number) {
  const products = []
  const categoryIds = [3, 4, 6, 7, 10, 11, 14, 15] // 三级分类ID
  const categoryNames: Record<number, string> = {
    3: '智能手机', 4: '功能手机', 6: '笔记本电脑', 7: '平板电脑',
    10: 'T恤', 11: '衬衫', 14: '坚果炒货', 15: '糖果巧克力',
  }
  const brandPool = ['华为', '小米', '苹果', '三星', 'OPPO', '耐克', '阿迪达斯', '优衣库', '三只松鼠', '良品铺子']
  
  for (let i = 0; i < count; i++) {
    const catId = categoryIds[i % categoryIds.length]
    const basePrice = (i + 1) * 1000 // 价格以分为单位
    const brand = brandPool[i % brandPool.length]
    products.push({
      id: i + 1,
      name: `${brand} ${categoryNames[catId]} 型号${i + 1}`,
      description: `这是${brand}品牌的${categoryNames[catId]}商品，品质保证，值得信赖`,
      images: [], // 图片为空
      brand,
      categoryId: catId,
      categoryName: categoryNames[catId],
      merchantId: (i % 5) + 1,
      merchantName: `官方旗舰店${(i % 5) + 1}`,
      status: i % 10 === 0 ? 0 : 1, // 0=下架, 1=上架
      sales: Math.floor(Math.random() * 1000),
      minPrice: basePrice, // 最低价（分）
      maxPrice: basePrice + 5000, // 最高价（分）
      createdAt: '2025-01-01 10:00:00',
      updatedAt: '2025-01-01 10:00:00',
    })
  }
  return products
}

export const allProducts = generateProducts(100)

const mockMethods: MockMethod[] = [
  // 4.1 分页查询商品列表
  {
    url: '/api/product/list',
    method: 'get',
    timeout: 500,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 20
      const categoryId = query.categoryId ? parseInt(query.categoryId) : null
      const keyword = query.keyword || query.q || null
      const sortField = query.sortField || null
      const sortOrder = query.sortOrder || null
      
      let filtered = [...allProducts]
      
      // 按分类筛选（包含所有子分类）
      if (categoryId) {
        const ids = collectCategoryIds(categoryId)
        filtered = filtered.filter(p => ids.includes(p.categoryId))
      }
      
      // 按关键词搜索
      if (keyword) {
        const kw = String(keyword).toLowerCase()
        filtered = filtered.filter(p =>
          p.name.toLowerCase().includes(kw) ||
          (p.description && p.description.toLowerCase().includes(kw)) ||
          (p.brand && p.brand.toLowerCase().includes(kw))
        )
      }
      
      // 排序
      if (sortField) {
        const field = String(sortField)
        const order = String(sortOrder || 'asc')
        filtered.sort((a, b) => {
          let va = a[field as keyof typeof a] ?? 0
          let vb = b[field as keyof typeof b] ?? 0
          if (typeof va === 'string') {
            return order === 'asc' ? va.localeCompare(vb as string) : (vb as string).localeCompare(va as string)
          }
          return order === 'asc' ? (va as number) - (vb as number) : (vb as number) - (va as number)
        })
      }
      
      const total = filtered.length
      const start = (page - 1) * size
      const end = start + size
      const list = filtered.slice(start, end)
      
      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page,
          size,
          list,
        },
        timestamp: Date.now(),
      }
    },
  },
  // 4.2 商品详情
  {
    url: '/api/product/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const productId = parseInt(query.id)
      const product = allProducts.find(p => p.id === productId)
      
      if (product) {
        return {
          code: 200,
          message: 'success',
          data: {
            id: product.id,
            name: product.name,
            description: product.description,
            images: [], // 图片为空
            brand: product.brand,
            categoryId: product.categoryId,
            merchantId: product.merchantId,
            merchantName: product.merchantName,
            status: product.status,
            sales: product.sales,
            createdAt: product.createdAt,
            updatedAt: product.updatedAt,
            // SKU 列表
            skus: [
              {
                id: productId * 10 + 1,
                productId: product.id,
                specIds: '[1]',
                specText: '默认规格',
                price: product.minPrice,
                stock: 100,
                status: 1,
              },
              {
                id: productId * 10 + 2,
                productId: product.id,
                specIds: '[2]',
                specText: '规格2',
                price: product.minPrice + 1000,
                stock: 50,
                status: 1,
              },
            ],
            // 规格列表
            specs: [
              {
                id: 1,
                name: '颜色',
                values: [
                  { id: 1, value: '红色' },
                  { id: 2, value: '蓝色' },
                ],
              },
            ],
          },
          timestamp: Date.now(),
        }
      }
      
      return {
        code: 404,
        message: '商品不存在',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
// 4.3 创建商品
  {
    url: '/api/product',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      const newProduct = {
        id: allProducts.length + 1,
        name: body.name,
        description: body.description || '',
        images: [],
        brand: body.brand || '',
        categoryId: body.categoryId,
        categoryName: '',
        merchantId: 1,
        merchantName: '测试店铺',
        status: 0,
        sales: 0,
        minPrice: 0,
        maxPrice: 0,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      }
      allProducts.push(newProduct)
      return {
        code: 200,
        message: '商品创建成功',
        data: { id: newProduct.id },
        timestamp: Date.now(),
      }
    },
  },
  // 4.4 编辑商品
  {
    url: '/api/product/:id',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '商品更新成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 4.5 上架/下架商品
  {
    url: '/api/product/:id/status',
    method: 'put',
    timeout: 300,
    response: ({ body }) => {
      return {
        code: 200,
        message: body.status === 1 ? '上架成功' : '下架成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 4.6 删除商品
  {
    url: '/api/product/:id',
    method: 'delete',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '商品删除成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods
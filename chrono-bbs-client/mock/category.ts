import { MockMethod } from 'vite-plugin-mock'

export default [
  // 3.1 获取分类树（三级）
  {
    url: '/api/category/tree',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: [
          {
            id: 1,
            name: '电子产品',
            level: 1,
            children: [
              {
                id: 2,
                name: '手机通讯',
                level: 2,
                children: [
                  { id: 3, name: '智能手机', level: 3 },
                  { id: 4, name: '功能手机', level: 3 },
                ],
              },
              {
                id: 5,
                name: '电脑办公',
                level: 2,
                children: [
                  { id: 6, name: '笔记本电脑', level: 3 },
                  { id: 7, name: '平板电脑', level: 3 },
                ],
              },
            ],
          },
          {
            id: 8,
            name: '服装鞋帽',
            level: 1,
            children: [
              {
                id: 9,
                name: '男装',
                level: 2,
                children: [
                  { id: 10, name: 'T恤', level: 3 },
                  { id: 11, name: '衬衫', level: 3 },
                ],
              },
            ],
          },
          {
            id: 12,
            name: '食品饮料',
            level: 1,
            children: [
              {
                id: 13,
                name: '休闲零食',
                level: 2,
                children: [
                  { id: 14, name: '坚果炒货', level: 3 },
                  { id: 15, name: '糖果巧克力', level: 3 },
                ],
              },
            ],
          },
          {
            id: 16,
            name: '家居用品',
            level: 1,
            children: [],
          },
        ],
        timestamp: Date.now(),
      }
    },
  },
  // 3.2 获取子分类列表
  {
    url: '/api/category/children',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const parentId = parseInt(query.parentId) || 0
      const allCategories = [
        { id: 1, name: '电子产品', parentId: 0, level: 1 },
        { id: 2, name: '手机通讯', parentId: 1, level: 2 },
        { id: 3, name: '智能手机', parentId: 2, level: 3 },
        { id: 4, name: '功能手机', parentId: 2, level: 3 },
        { id: 5, name: '电脑办公', parentId: 1, level: 2 },
        { id: 6, name: '笔记本电脑', parentId: 5, level: 3 },
        { id: 7, name: '平板电脑', parentId: 5, level: 3 },
        { id: 8, name: '服装鞋帽', parentId: 0, level: 1 },
        { id: 9, name: '男装', parentId: 8, level: 2 },
        { id: 10, name: 'T恤', parentId: 9, level: 3 },
        { id: 11, name: '衬衫', parentId: 9, level: 3 },
        { id: 12, name: '食品饮料', parentId: 0, level: 1 },
        { id: 13, name: '休闲零食', parentId: 12, level: 2 },
        { id: 14, name: '坚果炒货', parentId: 13, level: 3 },
        { id: 15, name: '糖果巧克力', parentId: 13, level: 3 },
        { id: 16, name: '家居用品', parentId: 0, level: 1 },
      ]
      
      const children = allCategories.filter(c => c.parentId === parentId)
      
      return {
        code: 200,
        message: 'success',
        data: children,
        timestamp: Date.now(),
      }
    },
  },
] as MockMethod[]
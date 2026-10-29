import { MockMethod } from 'vite-plugin-mock'

// 地址数据
let addresses = [
  {
    id: 1,
    userId: 100,
    receiverName: '张三',
    phone: '13800138000',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detail: 'XXX街道XXX号',
    isDefault: 1,
    createdAt: '2025-01-01 10:00:00',
    updatedAt: '2025-01-01 10:00:00',
  },
  {
    id: 2,
    userId: 100,
    receiverName: '李四',
    phone: '13900139000',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detail: 'YYY路YYY号',
    isDefault: 0,
    createdAt: '2025-01-01 11:00:00',
    updatedAt: '2025-01-01 11:00:00',
  },
]

const mockMethods: MockMethod[] = [
  // 8.1 地址列表
  {
    url: '/api/address/list',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: addresses,
        timestamp: Date.now(),
      }
    },
  },
  // 8.2 地址详情
  {
    url: '/api/address/:id',
    method: 'get',
    timeout: 200,
    response: ({ query }) => {
      const addressId = parseInt(query.id)
      const address = addresses.find(a => a.id === addressId)
      
      if (address) {
        return {
          code: 200,
          message: 'success',
          data: address,
          timestamp: Date.now(),
        }
      }
      
      return {
        code: 404,
        message: '地址不存在',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 8.3 新增地址
  {
    url: '/api/address',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const { receiverName, phone, province, city, district, detail, isDefault } = body
      
      // 如果设为默认，取消其他默认
      if (isDefault === 1) {
        addresses.forEach(a => {
          a.isDefault = 0
        })
      }
      
      const newAddress = {
        id: addresses.length + 1,
        userId: 100,
        receiverName,
        phone,
        province,
        city,
        district,
        detail,
        isDefault: isDefault || 0,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      }
      
      addresses.push(newAddress)
      
      return {
        code: 200,
        message: '地址添加成功',
        data: newAddress,
        timestamp: Date.now(),
      }
    },
  },
  // 8.4 修改地址
  {
    url: '/api/address/:id',
    method: 'put',
    timeout: 300,
    response: ({ body }) => {
      const addressId = parseInt(body.id)
      const address = addresses.find(a => a.id === addressId)
      
      if (address) {
        // 如果设为默认，取消其他默认
        if (body.isDefault === 1) {
          addresses.forEach(a => {
            a.isDefault = 0
          })
        }
        
        Object.assign(address, {
          receiverName: body.receiverName || address.receiverName,
          phone: body.phone || address.phone,
          province: body.province || address.province,
          city: body.city || address.city,
          district: body.district || address.district,
          detail: body.detail || address.detail,
          isDefault: body.isDefault !== undefined ? body.isDefault : address.isDefault,
          updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        })
        
        return {
          code: 200,
          message: '地址修改成功',
          data: address,
          timestamp: Date.now(),
        }
      }
      
      return {
        code: 404,
        message: '地址不存在',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 8.5 删除地址
  {
    url: '/api/address/:id',
    method: 'delete',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '地址删除成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 8.6 设为默认地址
  {
    url: '/api/address/:id/default',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '设置成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods
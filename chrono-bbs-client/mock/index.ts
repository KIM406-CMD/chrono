/**
 * Mock 数据模块说明
 * 
 * 本目录包含所有前端开发用的 Mock 数据，与后端 API 文档完全一致。
 * 
 * 模块列表：
 * - user.ts       : 用户认证（登录、注册、忘记密码等）
 * - category.ts   : 分类管理（分类树、子分类）
 * - product.ts    : 商品管理（商品列表、商品详情）
 * - cart.ts       : 购物车（增删改查、选中）
 * - order.ts      : 订单管理（创建订单、订单列表、订单详情等）
 * - payment.ts    : 支付管理（卡号支付、二维码支付）
 * - address.ts    : 收货地址（增删改查、设为默认）
 * - comment.ts    : 评论管理（评论列表、发表评论）
 * - refund.ts     : 退款管理（申请退款、退款列表、审核）
 * - merchant.ts   : 商家管理（入驻申请、商家信息）
 * - admin.ts      : 管理后台（用户管理、商家审核、销售报表等）
 * - upload.ts     : 文件上传
 * 
 * 数据格式说明：
 * - 所有接口返回格式与后端 API 文档一致
 * - 统一返回结构：{ code, message, data, timestamp }
 * - 分页格式：{ total, page, size, list }
 * - 图片字段均为空字符串（""），后续接入真实后端时替换
 * - 价格均以"分"为单位
 * 
 * 角色说明：
 * - 普通用户 role = 1
 * - 商家 role = 2
 * - 管理员 role = 3
 * 
 * 订单状态：
 * - 1 = 待支付
 * - 2 = 已支付（待发货）
 * - 3 = 已发货（待收货）
 * - 4 = 已完成
 * - 5 = 已取消
 * - 6 = 退款中
 * - 7 = 已退款
 * 
 * 如何关闭 Mock：
 * 在 vite.config.ts 中将 viteMockServe 的 enable 改为 false 即可
 */

export {}
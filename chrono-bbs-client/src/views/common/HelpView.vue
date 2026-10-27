<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

interface HelpItem {
  title: string
  content: string
}

interface HelpGroup {
  type: string
  label: string
  items: HelpItem[]
}

const helpGroups: HelpGroup[] = [
  {
    type: 'shopping',
    label: '购物指南',
    items: [
      {
        title: '购物流程',
        content:
          '1. 浏览商品：在首页或分类页浏览您感兴趣的商品。\n' +
          '2. 查看详情：点击商品图片或名称进入详情页，了解商品参数、评价等信息。\n' +
          '3. 加入购物车：选择规格后点击"加入购物车"，可继续浏览或去结算。\n' +
          '4. 提交订单：在购物车确认商品与数量后点击"去结算"，选择收货地址并提交。\n' +
          '5. 完成支付：选择支付方式完成支付，等待商家发货。\n' +
          '6. 确认收货：收到商品后检查无误，在"我的订单"中点击"确认收货"，交易完成。',
      },
      {
        title: '会员介绍',
        content:
          '本商城会员分为普通会员与商家会员：\n' +
          '- 普通会员：注册即可享受购物、评价、收藏等基础功能。\n' +
          '- 商家会员：需提交入驻申请，审核通过后可发布商品、管理订单。\n' +
          '会员等级将根据消费金额累计提升，享受更多专属优惠与服务。',
      },
      {
        title: '常见问题',
        content:
          'Q：如何修改收货地址？\n' +
          'A：进入"我的-收货地址"，可新增、编辑、删除地址，并设置默认地址。\n\n' +
          'Q：忘记密码怎么办？\n' +
          'A：在登录页点击"忘记密码"，通过注册手机号验证后重置密码。\n\n' +
          'Q：如何申请退款？\n' +
          'A：在"我的订单"中找到对应订单，点击"申请退款/退货"，填写原因后等待商家审核。',
      },
    ],
  },
  {
    type: 'delivery',
    label: '配送方式',
    items: [
      {
        title: '配送范围',
        content:
          '本商城支持全国大部分地区配送，包括中国大陆地区所有省市。\n' +
          '偏远地区（如新疆、西藏、青海等）配送时间可能延长，部分商品可能不支持配送，具体以提交订单时显示为准。',
      },
      {
        title: '配送时间',
        content:
          '- 现货商品：付款后 24 小时内发货，3-5 个工作日送达。\n' +
          '- 预售商品：发货时间以商品详情页标注为准。\n' +
          '- 节假日或大促期间，配送时间可能略有延迟，请您谅解。\n' +
          '您可在"我的订单"中实时查看物流状态。',
      },
      {
        title: '验货与签收',
        content:
          '1. 签收前请务必检查外包装是否完好。\n' +
          '2. 如有破损、变形等异常，请当面拒收并联系客服。\n' +
          '3. 签收后请尽快开箱验货，如发现商品损坏、缺少配件或与订单不符，请在 7 天内联系客服处理。\n' +
          '4. 建议保留商品包装及配件至少 7 天，以便退换货使用。',
      },
    ],
  },
  {
    type: 'payment',
    label: '支付方式',
    items: [
      {
        title: '在线支付',
        content:
          '本商城支持多种在线支付方式：\n' +
          '- 银行卡支付：支持各大银行储蓄卡与信用卡。\n' +
          '- 第三方支付：支持微信支付、支付宝等。\n' +
          '支付成功后订单状态将自动更新为"待发货"，如遇支付异常请联系客服。',
      },
      {
        title: '分期付款',
        content:
          '部分高价值商品支持分期付款：\n' +
          '- 支持 3 期、6 期、12 期分期。\n' +
          '- 分期手续费以支付页面展示为准。\n' +
          '- 分期后将从绑定银行卡按月自动扣款，请确保余额充足。\n' +
          '具体可分期商品请以商品详情页"分期"标识为准。',
      },
      {
        title: '发票制度',
        content:
          '- 本商城所有商品均支持开具电子发票。\n' +
          '- 下单时在"发票信息"中填写抬头与税号。\n' +
          '- 电子发票将在订单完成后发送至您的邮箱。\n' +
          '- 如需增值税专用发票，请联系客服另行办理。',
      },
    ],
  },
  {
    type: 'after-sale',
    label: '售后服务',
    items: [
      {
        title: '退换货政策',
        content:
          '- 7 天无理由退换：商品签收后 7 天内，保持商品完好、不影响二次销售的前提下，可申请无理由退换。\n' +
          '- 质量问题：商品存在质量问题的，15 天内可申请退换，运费由商家承担。\n' +
          '- 以下情况不支持退换：已拆封的个人卫生用品、定制商品、虚拟商品等。\n' +
          '具体规则以《消费者权益保护法》及平台相关规定为准。',
      },
      {
        title: '退换货流程',
        content:
          '1. 申请：在"我的订单"中点击"申请退款/退货"，选择原因并提交。\n' +
          '2. 审核：商家将在 48 小时内审核您的申请。\n' +
          '3. 寄回：审核通过后，请按系统提供的地址将商品寄回（保留快递单号）。\n' +
          '4. 确认：商家收到商品并确认无误后，将在 3-5 个工作日内完成退款。\n' +
          '5. 退款：退款将原路返回至您的支付账户。',
      },
      {
        title: '价格保护',
        content:
          '为保障消费者权益，本商城提供价格保护服务：\n' +
          '- 价保期限：商品签收后 7 天内。\n' +
          '- 适用范围：商品降价且仍在售的情况下。\n' +
          '- 申请方式：联系在线客服提供订单号，客服核实后将退还差价。\n' +
          '- 以下情况不支持价保：秒杀、预售、套餐、赠品等特殊活动商品。',
      },
    ],
  },
]

const currentGroup = computed(() => {
  const type = route.params.type as string
  return helpGroups.find((g) => g.type === type) || helpGroups[0]
})
</script>

<template>
  <div class="help-page">
    <div class="help-container">
      <div class="help-sidebar">
        <h3 class="sidebar-title">帮助中心</h3>
        <el-menu
          :default-active="currentGroup.type"
          router
          class="help-menu"
        >
          <el-menu-item
            v-for="group in helpGroups"
            :key="group.type"
            :index="`/help/${group.type}`"
          >
            {{ group.label }}
          </el-menu-item>
        </el-menu>
      </div>

      <div class="help-content">
        <h1 class="help-main-title">{{ currentGroup.label }}</h1>
        <div v-for="item in currentGroup.items" :key="item.title" class="help-item">
          <h2 class="help-item-title">{{ item.title }}</h2>
          <p class="help-item-content">{{ item.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.help-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.help-container {
  display: flex;
  gap: 24px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  min-height: 600px;
}

.help-sidebar {
  width: 220px;
  background: #fafafa;
  border-right: 1px solid #eee;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  padding: 20px;
  margin: 0;
  color: #333;
  border-bottom: 1px solid #eee;
}

.help-menu {
  border-right: none;
}

.help-content {
  flex: 1;
  padding: 28px 36px;
}

.help-main-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2231a;
  color: #333;
}

.help-item {
  margin-bottom: 28px;
}

.help-item-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 10px;
  color: #333;
}

.help-item-content {
  font-size: 14px;
  line-height: 2;
  color: #666;
  white-space: pre-line;
  margin: 0;
}
</style>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, CircleCheckFilled } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { payByCard } from '@/api/payment'
import type { Order } from '@/types/order'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => Number(route.params.orderId))
const order = ref<Order | null>(null)
const loading = ref(false)
const paying = ref(false)
const paid = ref(false)
const activeMethod = ref<'card' | 'qrcode'>('card')

const cardForm = ref({
  cardNo: '',
  expireDate: '',
  cvv: '',
})

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handlePayCard = async () => {
  if (!cardForm.value.cardNo || !cardForm.value.expireDate || !cardForm.value.cvv) {
    ElMessage.warning('请填写完整的卡号信息')
    return
  }
  paying.value = true
  try {
    await payByCard({
      orderId: orderId.value,
      cardNo: cardForm.value.cardNo,
      expireDate: cardForm.value.expireDate,
      cvv: cardForm.value.cvv,
    })
    paid.value = true
    ElMessage.success('支付成功！')
  } catch {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

const handleViewOrder = () => {
  router.replace(`/order/${orderId.value}`)
}

const handleBackHome = () => {
  router.replace('/home')
}

onMounted(() => {
  if (!orderId.value) {
    router.replace('/cart')
    return
  }
  loadOrder()
})
</script>

<template>
  <div class="payment-page" v-loading="loading">
    <div class="payment-header">
      <el-button text class="back-btn" @click="router.push('/cart')">
        <ArrowLeft />
        <span>返回</span>
      </el-button>
      <h2 class="payment-title">收银台</h2>
    </div>

    <!-- 支付成功 -->
    <div v-if="paid" class="pay-success">
      <div class="success-icon">
        <CircleCheckFilled />
      </div>
      <h2>支付成功！</h2>
      <p class="success-amount">
        已支付 ¥{{ order ? priceYuan(order.totalAmount) : '0.00' }}
      </p>
      <div class="success-actions">
        <el-button type="primary" size="large" @click="handleViewOrder">
          查看订单
        </el-button>
        <el-button size="large" @click="handleBackHome">返回首页</el-button>
      </div>
    </div>

    <!-- 支付中 -->
    <div v-else class="payment-container">
      <div class="order-summary">
        <div class="summary-head">
          订单号：{{ order?.orderNo || '---' }}
        </div>
        <div class="summary-body">
          <span>应付金额</span>
          <span class="pay-amount">
            ¥{{ order ? priceYuan(order.totalAmount) : '0.00' }}
          </span>
        </div>
        <div class="summary-footer">
          <Clock />
          <span>请在 30 分钟内完成支付，超时订单将自动取消</span>
        </div>
      </div>

      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <div class="method-tabs">
          <div
            class="method-tab"
            :class="{ active: activeMethod === 'card' }"
            @click="activeMethod = 'card'"
          >
            <span class="method-icon">💳</span>
            <span>银行卡支付</span>
          </div>
          <div
            class="method-tab"
            :class="{ active: activeMethod === 'qrcode' }"
            @click="activeMethod = 'qrcode'"
          >
            <span class="method-icon">📱</span>
            <span>扫码支付</span>
          </div>
        </div>

        <!-- 银行卡支付 -->
        <div v-show="activeMethod === 'card'" class="card-form">
          <div class="form-item">
            <label>卡号</label>
            <el-input
              v-model="cardForm.cardNo"
              placeholder="请输入银行卡号"
              maxlength="19"
            />
          </div>
          <div class="form-row">
            <div class="form-item">
              <label>有效期 (MM/YY)</label>
              <el-input
                v-model="cardForm.expireDate"
                placeholder="MM/YY"
                maxlength="5"
              />
            </div>
            <div class="form-item">
              <label>CVV 安全码</label>
              <el-input
                v-model="cardForm.cvv"
                placeholder="CVV"
                maxlength="3"
                type="password"
              />
            </div>
          </div>
          <el-button
            type="danger"
            size="large"
            class="pay-btn"
            :loading="paying"
            @click="handlePayCard"
          >
            ¥{{ order ? priceYuan(order.totalAmount) : '0.00' }} 立即支付
          </el-button>
        </div>

        <!-- 扫码支付 -->
        <div v-show="activeMethod === 'qrcode'" class="qrcode-section">
          <div class="qrcode-placeholder">
            <span class="qr-icon">📱</span>
            <p>请使用手机扫码支付</p>
            <p class="qr-hint">（Mock 模式，扫码即支付成功）</p>
          </div>
          <el-button
            type="danger"
            size="large"
            class="pay-btn"
            :loading="paying"
            @click="handlePayCard"
          >
            模拟扫码支付
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.payment-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  font-size: 14px;
  color: #666;
}

.back-btn:hover {
  color: #e2231a;
}

.payment-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

/* ========== 支付成功 ========== */
.pay-success {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.success-icon {
  font-size: 72px;
  color: #67c23a;
  margin-bottom: 16px;
}

.pay-success h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px;
}

.success-amount {
  font-size: 18px;
  color: #e2231a;
  font-weight: 600;
  margin: 0 0 24px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* ========== 订单摘要 ========== */
.order-summary {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.summary-head {
  background: #f5f5f5;
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
}

.summary-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 20px;
  font-size: 16px;
  color: #333;
}

.pay-amount {
  font-size: 28px;
  font-weight: 700;
  color: #e2231a;
}

.summary-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #fef8e7;
  font-size: 13px;
  color: #e6a23c;
}

/* ========== 支付方式 ========== */
.payment-methods {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.payment-methods h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #333;
}

.method-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.method-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  transition: all 0.2s;
}

.method-tab:hover {
  border-color: #f5a3a3;
}

.method-tab.active {
  border-color: #e2231a;
  color: #e2231a;
  background: #fef5f5;
}

.method-icon {
  font-size: 20px;
}

/* ========== 卡号表单 ========== */
.card-form {
  padding-top: 8px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-item {
  flex: 1;
}

.pay-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  margin-top: 8px;
}

/* ========== 扫码支付 ========== */
.qrcode-section {
  text-align: center;
  padding-top: 8px;
}

.qrcode-placeholder {
  background: #fafafa;
  border: 1px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  margin-bottom: 20px;
}

.qr-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.qrcode-placeholder p {
  margin: 4px 0;
  color: #666;
}

.qr-hint {
  font-size: 12px;
  color: #999;
}
</style>
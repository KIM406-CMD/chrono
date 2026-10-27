<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart, ArrowLeft } from '@element-plus/icons-vue'
import { getCartList, updateCartQuantity, removeFromCart, selectCartItems, clearCart } from '@/api/cart'
import type { CartItem } from '@/types/cart'

const router = useRouter()

const cartList = ref<CartItem[]>([])
const loading = ref(false)
const editingAll = ref(false)

const allSelected = computed({
  get: () => cartList.value.length > 0 && cartList.value.every(item => item.selected === 1),
  set: (val: boolean) => handleSelectAll(val),
})

const selectedItems = computed(() => cartList.value.filter(item => item.selected === 1))

const selectedCount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
)

const totalPriceYuan = computed(() => {
  const totalFen = selectedItems.value.reduce((sum, item) => sum + item.skuPrice * item.quantity, 0)
  return (totalFen / 100).toFixed(2)
})

const hasStock = (item: CartItem) => item.skuStock > 0

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const subtotalYuan = (item: CartItem) => ((item.skuPrice * item.quantity) / 100).toFixed(2)

const fetchCartList = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data || []
  } catch {
    cartList.value = []
  } finally {
    loading.value = false
  }
}

const handleQuantityChange = async (item: CartItem, delta: number) => {
  const newQty = item.quantity + delta
  if (newQty < 1) {
    handleRemove(item)
    return
  }
  if (newQty > item.skuStock) {
    ElMessage.warning(`库存不足，最多可购买 ${item.skuStock} 件`)
    return
  }
  item.quantity = newQty
  try {
    await updateCartQuantity({ cartItemId: item.id, quantity: newQty })
  } catch {
    item.quantity -= delta
  }
}

const handleRemove = async (item: CartItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.productName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await removeFromCart(item.id)
    cartList.value = cartList.value.filter(i => i.id !== item.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleRemoveSelected = async () => {
  if (selectedItems.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  const ids = selectedItems.value.map(i => i.id)
  for (const id of ids) {
    try {
      await removeFromCart(id)
    } catch { /* ignore */ }
  }
  cartList.value = cartList.value.filter(i => !ids.includes(i.id))
  ElMessage.success('已删除选中商品')
}

const handleSelectAll = async (selected: boolean) => {
  editingAll.value = true
  try {
    await selectCartItems({ selected })
    cartList.value.forEach(item => {
      item.selected = selected ? 1 : 0
    })
  } catch {
    /* ignore */
  } finally {
    editingAll.value = false
  }
}

const handleSelectItem = async (item: CartItem) => {
  const newSelected = item.selected === 1 ? 0 : 1
  item.selected = newSelected
  try {
    await selectCartItems({ cartItemIds: [item.id], selected: newSelected === 1 })
  } catch {
    item.selected = item.selected === 1 ? 0 : 1
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？所有商品将被清除。', '清空购物车', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await clearCart()
    cartList.value = []
    ElMessage.success('购物车已清空')
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要购买的商品')
    return
  }
  router.push('/checkout')
}

const goProduct = (item: CartItem) => {
  router.push(`/product/${item.productId}`)
}

onMounted(() => {
  fetchCartList()
})
</script>

<template>
  <div class="cart-page">
    <div class="cart-header">
      <el-button text class="back-btn" @click="router.push('/home')">
        <ArrowLeft />
        <span>返回首页</span>
      </el-button>
      <h2 class="cart-title">我的购物车</h2>
    </div>

    <!-- 空购物车 -->
    <div v-if="!loading && cartList.length === 0" class="cart-empty">
      <div class="empty-icon">
        <ShoppingCart />
      </div>
      <p class="empty-text">购物车还是空的</p>
      <p class="empty-hint">快去逛逛吧，把喜欢的好物带回家</p>
      <el-button type="primary" size="large" @click="router.push('/home')">去逛逛</el-button>
    </div>

    <!-- 购物车列表 -->
    <div v-else class="cart-content">
      <!-- 表头 -->
      <div class="cart-table-header">
        <div class="col-check">
          <el-checkbox
            v-model="allSelected"
            :disabled="cartList.length === 0 || editingAll"
          />
          <span class="check-label">全选</span>
        </div>
        <div class="col-product">商品信息</div>
        <div class="col-price">单价</div>
        <div class="col-quantity">数量</div>
        <div class="col-subtotal">小计</div>
        <div class="col-action">操作</div>
      </div>

      <!-- 商品列表 -->
      <div v-loading="loading" class="cart-list">
        <div
          v-for="item in cartList"
          :key="item.id"
          class="cart-item"
          :class="{ 'stock-out': !hasStock(item) }"
        >
          <div class="col-check">
            <el-checkbox
              :model-value="item.selected === 1"
              :disabled="!hasStock(item)"
              @change="handleSelectItem(item)"
            />
          </div>

          <div class="col-product">
            <div class="product-info">
              <div class="product-img" @click="goProduct(item)">
                <el-image v-if="item.productImage" :src="item.productImage" fit="cover" />
                <span v-else class="img-placeholder">暂无图片</span>
              </div>
              <div class="product-detail">
                <p class="product-name" @click="goProduct(item)">{{ item.productName }}</p>
                <p class="product-sku">{{ item.skuSpecText }}</p>
                <div class="product-merchant">
                  <span class="merchant-tag">店铺：{{ item.merchantName }}</span>
                </div>
                <el-tag v-if="!hasStock(item)" type="danger" size="small">缺货</el-tag>
              </div>
            </div>
          </div>

          <div class="col-price">
            <span class="price-current">¥{{ priceYuan(item.skuPrice) }}</span>
          </div>

          <div class="col-quantity">
            <div class="qty-control">
              <button
                class="qty-btn"
                :disabled="item.quantity <= 1"
                @click="handleQuantityChange(item, -1)"
              >
                -
              </button>
              <input
                class="qty-input"
                type="text"
                :value="item.quantity"
                readonly
              />
              <button
                class="qty-btn"
                :disabled="item.quantity >= item.skuStock"
                @click="handleQuantityChange(item, 1)"
              >
                +
              </button>
            </div>
          </div>

          <div class="col-subtotal">
            <span class="subtotal-value">¥{{ subtotalYuan(item) }}</span>
          </div>

          <div class="col-action">
            <el-button text type="danger" :icon="Delete" @click="handleRemove(item)">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer" v-if="cartList.length > 0">
        <div class="footer-left">
          <el-checkbox
            v-model="allSelected"
            :disabled="cartList.length === 0 || editingAll"
          />
          <span class="select-all-text">全选</span>
          <el-button text type="danger" class="btn-remove-selected" @click="handleRemoveSelected">
            删除选中
          </el-button>
          <el-button text class="btn-clear" @click="handleClear">
            清空购物车
          </el-button>
        </div>
        <div class="footer-right">
          <div class="summary-info">
            <span class="summary-text">
              已选 <em class="count">{{ selectedCount }}</em> 件
            </span>
            <span class="summary-total">
              合计：<em class="total-price">¥{{ totalPriceYuan }}</em>
            </span>
          </div>
          <el-button
            type="danger"
            size="large"
            class="checkout-btn"
            :disabled="selectedItems.length === 0"
            @click="handleCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cart-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 14px;
  color: #666;
  padding: 4px 8px;
}

.back-btn:hover {
  color: #e2231a;
}

.cart-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

/* 空购物车 */
.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 72px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin: 0 0 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

/* 表格头 */
.cart-table-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 4px 4px 0 0;
  font-size: 14px;
  color: #666;
  font-weight: 600;
}

/* 购物车列表 */
.cart-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.cart-list {
  min-height: 200px;
}

/* 单条商品 */
.cart-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.cart-item:hover {
  background: #fafafa;
}

.cart-item.stock-out {
  background: #fef0f0;
}

/* 列宽 */
.col-check {
  width: 80px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.check-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.col-product {
  flex: 1;
  min-width: 0;
}

.col-price {
  width: 120px;
  text-align: center;
  flex-shrink: 0;
  font-size: 15px;
  color: #333;
}

.col-quantity {
  width: 150px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.col-subtotal {
  width: 130px;
  text-align: center;
  flex-shrink: 0;
}

.col-action {
  width: 100px;
  text-align: center;
  flex-shrink: 0;
}

/* 商品信息 */
.product-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  flex-shrink: 0;
}

.product-img :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.img-placeholder {
  font-size: 12px;
  color: #bbb;
}

.product-detail {
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 320px;
}

.product-name:hover {
  color: #e2231a;
}

.product-sku {
  font-size: 12px;
  color: #999;
  margin: 0 0 4px;
}

.product-merchant {
  margin-bottom: 4px;
}

.merchant-tag {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 1px 6px;
  border-radius: 2px;
}

.price-current {
  font-weight: 600;
}

/* 数量控制器 */
.qty-control {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  color: #666;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.qty-btn:hover:not(:disabled) {
  background: #e8e8e8;
}

.qty-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.qty-input {
  width: 48px;
  height: 32px;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
  text-align: center;
  font-size: 14px;
  color: #333;
  outline: none;
  background: #fff;
}

.subtotal-value {
  font-size: 15px;
  font-weight: 700;
  color: #e2231a;
}

/* 底部结算栏 */
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
  position: sticky;
  bottom: 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.select-all-text {
  font-size: 14px;
  color: #666;
}

.btn-remove-selected {
  font-size: 13px;
}

.btn-clear {
  font-size: 13px;
  color: #999;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary-info {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.summary-text {
  font-size: 14px;
  color: #666;
}

.count {
  font-style: normal;
  color: #e2231a;
  font-weight: 600;
}

.summary-total {
  font-size: 14px;
  color: #333;
}

.total-price {
  font-style: normal;
  font-size: 22px;
  font-weight: 700;
  color: #e2231a;
}

.checkout-btn {
  min-width: 140px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 20px;
}
</style>
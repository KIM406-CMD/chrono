<script setup lang="ts">
import type { Category } from '@/types/product'

withDefaults(
  defineProps<{
    /** 一级分类，展示其二级分组与三级分类 */
    category: Category
    /** 弹出方向：bottom 用于顶部导航，right 用于首页侧边栏 */
    placement?: 'bottom' | 'right'
  }>(),
  { placement: 'bottom' },
)

const emit = defineEmits<{
  /** 点击任意二级/三级分类 */
  select: [category: Category]
}>()
</script>

<template>
  <div
    v-if="category.children?.length"
    class="category-flyout"
    :class="placement"
  >
    <div v-for="sub in category.children" :key="sub.id" class="flyout-group">
      <p class="flyout-title" @click="emit('select', sub)">{{ sub.name }}</p>
      <div v-if="sub.children?.length" class="flyout-values">
        <span
          v-for="leaf in sub.children"
          :key="leaf.id"
          class="flyout-leaf"
          @click="emit('select', leaf)"
        >
          {{ leaf.name }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-flyout {
  position: absolute;
  z-index: 300;
  min-width: 420px;
  max-width: min(720px, 70vw);
  padding: 14px 18px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: left;
}

.category-flyout.bottom {
  top: 100%;
  left: 0;
}

.category-flyout.right {
  top: 0;
  left: 100%;
}

.flyout-group + .flyout-group {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #f0f0f0;
}

.flyout-title {
  margin: 0 0 6px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}

.flyout-title:hover {
  color: #e2231a;
}

.flyout-values {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 16px;
}

.flyout-leaf {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
}

.flyout-leaf:hover {
  color: #e2231a;
}
</style>

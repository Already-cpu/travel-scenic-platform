<template>
  <article class="scenic-card" @click="$router.push(`/scenic/${scenic.id}`)">
    <img class="scenic-card-image" :src="scenic.image || fallback" :alt="scenic.name" @error="onImageError" />
    <div class="scenic-card-body">
      <div class="scenic-card-meta">
        <span class="type">{{ scenic.type }}</span>
        <span>{{ scenic.city }}</span>
      </div>
      <h3>{{ scenic.name }}</h3>
      <p class="scenic-card-address">{{ scenic.address }}</p>
      <div class="scenic-card-bottom">
        <span class="price">{{ scenic.price > 0 ? `¥${scenic.price}` : '免费' }} <small v-if="scenic.price > 0">起</small></span>
        <el-button link type="primary">查看详情 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({ scenic: { type: Object, required: true } })
const fallback = '/image-unavailable.svg'
const onImageError = (event) => { event.target.onerror = null; event.target.src = fallback }
</script>

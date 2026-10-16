<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <router-link to="/admin/dashboard" class="admin-brand"><span class="brand-mark"><el-icon><Promotion /></el-icon></span><span>行旅志后台</span></router-link>
      <nav class="admin-nav"><router-link v-for="item in menus" :key="item.path" :to="item.path"><el-icon :size="18"><component :is="item.icon" /></el-icon><span>{{ item.label }}</span></router-link></nav>
    </aside>
    <div class="admin-main">
      <header class="admin-topbar"><h1>{{ pageTitle }}</h1><div class="admin-user"><el-avatar :size="32">管</el-avatar><span>{{ admin?.nickname || '平台管理员' }}</span><el-button text @click="logout">退出</el-button></div></header>
      <main class="admin-content"><router-view /></main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, DataAnalysis, Document, Location, Promotion, User } from '@element-plus/icons-vue'
import { adminApi } from '../api/http'
import '../styles/admin.css'

const route = useRoute(); const router = useRouter(); const admin = ref(null)
const menus = [{ path: '/admin/dashboard', label: '数据仪表盘', icon: DataAnalysis }, { path: '/admin/scenic', label: '景点管理', icon: Location }, { path: '/admin/order', label: '订单管理', icon: Document }, { path: '/admin/user', label: '用户管理', icon: User }, { path: '/admin/comment', label: '评价管理', icon: ChatDotRound }]
const pageTitle = computed(() => menus.find(item => route.path.startsWith(item.path))?.label || '数据仪表盘')
onMounted(async () => { try { admin.value = await adminApi.info() } catch {} })
const logout = () => { sessionStorage.removeItem('admin_token'); router.push('/admin/login') }
</script>

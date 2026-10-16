<template>
  <div class="page-shell">
    <header class="site-header">
      <div class="container">
        <router-link to="/" class="brand">
          <span class="brand-mark"><el-icon><Promotion /></el-icon></span>
          行旅志
        </router-link>
        <nav class="main-nav">
          <router-link to="/">发现目的地</router-link>
          <router-link to="/scenic">景点列表</router-link>
          <router-link v-if="userToken" to="/account">我的行程</router-link>
        </nav>
        <div class="header-actions">
          <template v-if="userToken">
            <router-link to="/account" class="header-user">
              <el-avatar :size="30" :src="user?.avatar">{{ user?.nickname?.slice(0, 1) }}</el-avatar>
              <span>{{ user?.nickname || '旅行者' }}</span>
            </router-link>
            <el-button text @click="logout">退出</el-button>
          </template>
          <template v-else>
            <router-link to="/login"><el-button text>登录</el-button></router-link>
            <router-link to="/register"><el-button type="primary" round>开始旅行</el-button></router-link>
          </template>
        </div>
      </div>
    </header>
    <main><router-view /></main>
    <footer class="site-footer">
      <div class="container">
        <div class="footer-grid">
          <div>
            <div class="brand" style="color: white"><span class="brand-mark"><el-icon><Promotion /></el-icon></span>行旅志</div>
            <p class="footer-copy">把目的地、行程和此刻的好心情，放进一次从容的出发里。</p>
          </div>
          <div><div class="footer-title">探索</div><div class="footer-links"><router-link to="/">首页推荐</router-link><router-link to="/scenic">全部景点</router-link></div></div>
          <div><div class="footer-title">服务</div><div class="footer-links"><router-link to="/account">我的订单</router-link><router-link to="/login">用户登录</router-link><a href="/photo-credits.html" target="_blank" rel="noopener">实景照片来源</a></div></div>
        </div>
        <div class="footer-bottom">© 2026 行旅志旅游景点网站 · 课程演示项目，订单与标记的评价为示例数据，不提供真实售票服务</div>
      </div>
    </footer>
    <TravelAssistant />
    <LoginPromptDialog v-model="loginPromptVisible" :redirect="loginRedirect" message="登录后才能查看我的行程、收藏景点和管理订单。" @closed="clearLoginPrompt" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Promotion } from '@element-plus/icons-vue'
import { userApi } from '../api/http'
import TravelAssistant from '../components/TravelAssistant.vue'
import LoginPromptDialog from '../components/LoginPromptDialog.vue'

const router = useRouter(); const route = useRoute()
const userToken = ref(sessionStorage.getItem('user_token'))
const user = ref(null)
const loginPromptVisible = ref(false)
const loginRedirect = ref('/')
const loadUser = async () => {
  userToken.value = sessionStorage.getItem('user_token')
  if (!userToken.value) { user.value = null; return }
  try { user.value = await userApi.info() } catch { sessionStorage.removeItem('user_token'); userToken.value = null }
}
onMounted(() => { loadUser(); window.addEventListener('user-profile-updated', loadUser) })
onBeforeUnmount(() => window.removeEventListener('user-profile-updated', loadUser))
watch(() => [route.query.loginPrompt, route.query.redirect], ([prompt, redirect]) => {
  if (prompt === '1' && !sessionStorage.getItem('user_token')) {
    loginRedirect.value = redirect || '/account'
    loginPromptVisible.value = true
  }
}, { immediate: true })
const logout = () => {
  sessionStorage.removeItem('user_token')
  userToken.value = null
  user.value = null
  router.push('/')
}
const clearLoginPrompt = () => { if (route.query.loginPrompt === '1') router.replace('/') }
</script>

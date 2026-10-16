<template>
  <el-dialog v-model="visible" title="登录后继续" width="min(430px, calc(100% - 28px))" align-center @closed="emit('closed')">
    <div class="login-prompt-body">
      <div class="login-prompt-icon"><el-icon :size="28"><Lock /></el-icon></div>
      <h3>先登录，再继续这一步</h3>
      <p>{{ message }}</p>
      <small>支持密码、短信验证码和扫码登录，登录后会回到当前页面。</small>
    </div>
    <template #footer>
      <el-button @click="visible = false">先看看</el-button>
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'

const props = defineProps({ modelValue: Boolean, redirect: { type: String, default: '/' }, message: { type: String, default: '登录后才能收藏景点、预订门票或发表评价。' } })
const emit = defineEmits(['update:modelValue', 'closed'])
const router = useRouter()
const visible = computed({ get: () => props.modelValue, set: value => emit('update:modelValue', value) })
const goLogin = () => { visible.value = false; router.push({ path: '/login', query: { redirect: props.redirect } }) }
</script>

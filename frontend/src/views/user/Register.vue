<template>
  <div class="auth-page">
    <div class="auth-card">
      <router-link to="/" class="brand"><span class="brand-mark"><el-icon><Promotion /></el-icon></span>行旅志</router-link>
      <h1>创建旅行账号</h1><p>收藏喜欢的风景，把下一次出发安排得刚刚好。</p>
      <el-form class="auth-form" :model="form" :rules="rules" ref="formRef" @submit.prevent="submit">
        <el-form-item prop="nickname"><el-input v-model="form.nickname" size="large" placeholder="你的昵称"><template #prefix><el-icon><User /></el-icon></template></el-input></el-form-item>
        <el-form-item prop="phone"><el-input v-model="form.phone" size="large" placeholder="手机号" maxlength="11"><template #prefix><el-icon><Iphone /></el-icon></template></el-input></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" size="large" placeholder="设置密码（至少 6 位）" type="password" show-password><template #prefix><el-icon><Lock /></el-icon></template></el-input></el-form-item>
        <el-button type="primary" size="large" round native-type="submit" :loading="loading">注册并开始</el-button>
      </el-form>
      <div class="auth-footer">已经有账号？<router-link to="/login">直接登录</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Promotion, User } from '@element-plus/icons-vue'
import { userApi } from '../../api/http'

const router = useRouter(); const formRef = ref(); const loading = ref(false)
const form = reactive({ nickname: '', phone: '', password: '' })
const rules = { nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }], phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }], password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }] }
const submit = async () => { if (!await formRef.value.validate().catch(() => false)) return; loading.value = true; try { await userApi.register(form); ElMessage.success('注册成功，请登录'); router.push('/login') } finally { loading.value = false } }
</script>

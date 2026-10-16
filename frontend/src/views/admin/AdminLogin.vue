<template>
  <div class="admin-login">
    <div class="admin-login-card">
      <div class="admin-brand"><span class="brand-mark"><el-icon><Promotion /></el-icon></span>行旅志后台</div>
      <h1>管理员登录</h1><p>登录管理景点、订单、用户和评价数据。</p>
      <el-form :model="form" style="display:grid;gap:15px"><el-form-item :error="errors.username"><el-input v-model="form.username" :validate-event="false" size="large" placeholder="管理员账号" @input="errors.username = ''"><template #prefix><el-icon><User /></el-icon></template></el-input></el-form-item><el-form-item :error="errors.password"><el-input v-model="form.password" :validate-event="false" size="large" type="password" show-password placeholder="登录密码" @input="errors.password = ''"><template #prefix><el-icon><Lock /></el-icon></template></el-input></el-form-item><el-button type="primary" size="large" :loading="loading" @click="submit">进入管理后台</el-button></el-form>
      <div class="admin-quick-login"><el-button plain size="large" @click="quickLogin" :loading="loading">一键登录演示管理员</el-button><small>管理员入口独立，不能使用用户账号登录</small></div>
      <div style="text-align:center;color:#a0acb8;font-size:12px;margin-top:22px">管理员账号由超级管理员统一维护</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Promotion, User } from '@element-plus/icons-vue'
import { adminApi } from '../../api/http'

const router = useRouter(); const loading = ref(false); const form = reactive({ username: 'admin', password: 'admin123' }); const errors = reactive({ username: '', password: '' })
const submit = async () => { errors.username = form.username ? '' : '请输入账号'; errors.password = form.password ? '' : '请输入密码'; if (errors.username || errors.password) return; loading.value = true; try { const data = await adminApi.login(form); sessionStorage.setItem('admin_token', data.token); ElMessage.success('登录成功'); router.push('/admin/dashboard') } finally { loading.value = false } }
const quickLogin = () => { form.username = 'admin'; form.password = 'admin123'; submit() }
</script>

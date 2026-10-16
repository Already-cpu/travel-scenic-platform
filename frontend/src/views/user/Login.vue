<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-topline"><router-link to="/" class="brand"><span class="brand-mark"><el-icon><Promotion /></el-icon></span>行旅志</router-link><el-button text circle class="auth-close" aria-label="关闭登录页" @click="close"><el-icon><Close /></el-icon></el-button></div>
      <h1>欢迎回来</h1><p>登录后管理收藏、订单与旅行计划，登录方式由你选择。</p>
      <el-tabs v-model="mode" stretch class="login-tabs">
        <el-tab-pane label="密码登录" name="password"><el-form class="auth-form" :model="form"><el-form-item :error="passwordErrors.phone"><el-input v-model="form.phone" :validate-event="false" size="large" placeholder="手机号" maxlength="11" @input="passwordErrors.phone = ''"><template #prefix><el-icon><Iphone /></el-icon></template></el-input></el-form-item><el-form-item :error="passwordErrors.password"><el-input v-model="form.password" :validate-event="false" size="large" placeholder="密码" type="password" show-password @input="passwordErrors.password = ''"><template #prefix><el-icon><Lock /></el-icon></template></el-input></el-form-item><el-button type="primary" size="large" round :loading="loading" @click="submitPassword">登录</el-button></el-form></el-tab-pane>
        <el-tab-pane label="短信验证码" name="sms"><el-form class="auth-form" :model="smsForm"><el-form-item :error="smsErrors.phone"><el-input v-model="smsForm.phone" :validate-event="false" size="large" placeholder="手机号" maxlength="11" @input="smsErrors.phone = ''"><template #prefix><el-icon><Iphone /></el-icon></template></el-input></el-form-item><el-form-item :error="smsErrors.code"><div class="code-line"><el-input v-model="smsForm.code" :validate-event="false" size="large" placeholder="6 位验证码" maxlength="6" @input="smsErrors.code = ''"><template #prefix><el-icon><Message /></el-icon></template></el-input><el-button size="large" :disabled="countdown > 0" @click="sendCode">{{ countdown ? `${countdown}s 后重发` : '获取验证码' }}</el-button></div></el-form-item><el-button type="primary" size="large" round :loading="loading" @click="submitSms">验证码登录</el-button></el-form><p class="demo-hint">课程演示验证码：<strong>123456</strong>，无需真实短信服务。</p></el-tab-pane>
        <el-tab-pane label="扫码登录" name="qr"><div class="qr-login-panel"><div class="qr-status"><span class="qr-status-dot"></span>安全扫码登录</div><div class="fake-qr" aria-label="行旅志扫码登录二维码"><i v-for="n in qrSize * qrSize" :key="n" :class="{dark: qrPattern[n - 1]}" /><span class="qr-scan-line"></span><span class="qr-logo">行旅志</span></div><strong>打开手机扫码登录</strong><p>使用行旅志 App 扫描二维码<br />课程演示可直接点击下方模拟扫码</p><el-button type="primary" plain round :loading="loading" @click="scanLogin">模拟扫码成功</el-button><small>二维码每 2 分钟刷新 · 演示二维码仅用于课程答辩</small></div></el-tab-pane>
      </el-tabs>
      <div class="quick-login"><span>快速体验</span><el-button plain size="large" @click="quickLogin" :loading="loading">一键登录演示用户</el-button><small>密码、短信、扫码均只进入用户端，不与管理员账号互通</small></div>
      <div class="auth-footer">还没有账号？<router-link to="/register">注册一个</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, Iphone, Lock, Message, Promotion } from '@element-plus/icons-vue'
import { userApi } from '../../api/http'

const router = useRouter(); const route = useRoute(); const loading = ref(false); const mode = ref('password'); const countdown = ref(0); let timer
const form = reactive({ phone: '', password: '' }); const smsForm = reactive({ phone: '13800138000', code: '' })
const passwordErrors = reactive({ phone: '', password: '' }); const smsErrors = reactive({ phone: '', code: '' })
const qrSize = 29
const qrPattern = computed(() => {
  const values = Array(qrSize * qrSize).fill(false)
  const reserved = Array(qrSize * qrSize).fill(false)
  const index = (x, y) => y * qrSize + x
  const markFinder = (originX, originY) => {
    for (let y = -1; y <= 7; y++) for (let x = -1; x <= 7; x++) {
      const px = originX + x; const py = originY + y
      if (px < 0 || py < 0 || px >= qrSize || py >= qrSize) continue
      reserved[index(px, py)] = true
      values[index(px, py)] = x >= 0 && x <= 6 && y >= 0 && y <= 6 && (x === 0 || x === 6 || y === 0 || y === 6 || (x >= 2 && x <= 4 && y >= 2 && y <= 4))
    }
  }
  const markAlignment = (originX, originY) => {
    for (let y = -2; y <= 2; y++) for (let x = -2; x <= 2; x++) {
      const px = originX + x; const py = originY + y
      reserved[index(px, py)] = true
      values[index(px, py)] = Math.max(Math.abs(x), Math.abs(y)) !== 1
    }
  }
  markFinder(0, 0); markFinder(qrSize - 7, 0); markFinder(0, qrSize - 7)
  for (let i = 8; i < qrSize - 8; i++) { reserved[index(i, 6)] = true; values[index(i, 6)] = i % 2 === 0; reserved[index(6, i)] = true; values[index(6, i)] = i % 2 === 0 }
  markAlignment(qrSize - 7, qrSize - 7)
  let seed = 20261008
  for (let y = 0; y < qrSize; y++) for (let x = 0; x < qrSize; x++) if (!reserved[index(x, y)]) { seed = (seed * 9301 + 49297) % 233280; values[index(x, y)] = seed / 233280 > .53 }
  return values
})
const finishLogin = data => { sessionStorage.setItem('user_token', data.token); ElMessage.success('登录成功'); router.push(route.query.redirect || '/') }
const validatePassword = () => { passwordErrors.phone = !form.phone ? '请输入手机号' : !/^1[3-9]\d{9}$/.test(form.phone) ? '手机号格式不正确' : ''; passwordErrors.password = form.password ? '' : '请输入密码'; return !passwordErrors.phone && !passwordErrors.password }
const validateSms = () => { smsErrors.phone = !smsForm.phone ? '请输入手机号' : !/^1[3-9]\d{9}$/.test(smsForm.phone) ? '手机号格式不正确' : ''; smsErrors.code = !smsForm.code ? '请输入验证码' : smsForm.code.length !== 6 ? '验证码为 6 位' : ''; return !smsErrors.phone && !smsErrors.code }
const submitPassword = async () => { if (!validatePassword()) return; loading.value = true; try { finishLogin(await userApi.login(form)) } finally { loading.value = false } }
const submitSms = async () => { if (!validateSms()) return; loading.value = true; try { finishLogin(await userApi.smsLogin(smsForm)) } finally { loading.value = false } }
const sendCode = () => { countdown.value = 60; clearInterval(timer); timer = setInterval(() => { countdown.value -= 1; if (countdown.value <= 0) clearInterval(timer) }, 1000); ElMessage.success('演示验证码已发送，请输入 123456') }
const scanLogin = async () => { loading.value = true; try { finishLogin(await userApi.qrLogin({ qrCode: 'TRAVEL-DEMO-QR-2026' })) } finally { loading.value = false } }
const quickLogin = () => { mode.value = 'password'; form.phone = '13800138000'; form.password = '123456'; submitPassword() }
const close = () => { router.push(route.query.redirect ? String(route.query.redirect) : '/') }
onBeforeUnmount(() => clearInterval(timer))
</script>

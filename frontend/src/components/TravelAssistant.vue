<template>
  <div class="assistant-shell">
    <transition name="assistant-pop">
      <section v-if="open" class="assistant-panel">
        <header><div><strong>行旅 AI 助手</strong><small>景点推荐与功能指引</small></div><el-button circle text @click="open=false"><el-icon><Close /></el-icon></el-button></header>
        <div ref="messageBox" class="assistant-messages">
          <div class="assistant-message bot">你好，我可以按预算、地区和旅行偏好推荐景点，也能告诉你如何购票和评价。</div>
          <template v-for="(item,index) in messages" :key="index">
            <div class="assistant-message user">{{ item.question }}</div>
            <div class="assistant-message bot">{{ item.answer }}</div>
            <div v-if="item.recommendations?.length" class="assistant-recommendations">
              <button v-for="scenic in item.recommendations" :key="scenic.id" @click="go(scenic.id)"><img :src="scenic.image || '/image-unavailable.svg'" /><span><strong>{{ scenic.name }}</strong><small>{{ scenic.city }} · ¥{{ scenic.price }} 起</small></span></button>
            </div>
          </template>
          <div v-if="loading" class="assistant-message bot">正在整理适合你的目的地…</div>
        </div>
        <div class="assistant-suggestions"><button v-for="text in suggestions" :key="text" @click="ask(text)">{{ text }}</button></div>
        <form class="assistant-input" @submit.prevent="ask(input)"><el-input v-model="input" maxlength="300" placeholder="例如：预算 100 元以内的亲子游" /><el-button type="primary" native-type="submit" :loading="loading"><el-icon><Promotion /></el-icon></el-button></form>
      </section>
    </transition>
    <button class="assistant-fab" type="button" @click="open=!open"><el-icon :size="24"><ChatDotRound /></el-icon><span>AI 助手</span></button>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Promotion } from '@element-plus/icons-vue'
import { assistantApi } from '../api/http'

const router = useRouter(); const open = ref(false); const input = ref(''); const loading = ref(false); const messages = ref([]); const messageBox = ref()
const suggestions = ref(['适合周末的景点', '预算 100 元以内', '亲子游推荐', '如何发表评价'])
const ask = async text => { const question = String(text || '').trim(); if (!question || loading.value) return; input.value=''; loading.value=true; try { const data=await assistantApi.chat(question); messages.value.push({ question, ...data }); suggestions.value=data.suggestions || suggestions.value; await nextTick(); messageBox.value?.scrollTo({ top: messageBox.value.scrollHeight, behavior:'smooth' }) } finally { loading.value=false } }
const go = id => { open.value=false; router.push(`/scenic/${id}`) }
</script>

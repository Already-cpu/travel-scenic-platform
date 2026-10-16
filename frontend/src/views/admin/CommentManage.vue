<template>
  <div class="admin-panel">
    <div class="admin-toolbar"><el-input v-model="keyword" placeholder="搜索评价内容" clearable @keyup.enter="load"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-select v-model="status" placeholder="审核状态" clearable @change="load"><el-option label="显示中" :value="1" /><el-option label="已隐藏" :value="0" /></el-select><el-button type="primary" @click="load">查询</el-button></div>
    <el-table v-loading="loading" :data="records" class="admin-table" stripe>
      <el-table-column label="评价" min-width="390"><template #default="{row}"><div class="table-title"><el-rate v-model="row.score" disabled /></div><div class="comment-admin-content">{{row.content}}</div><div v-if="row.images?.length" class="comment-admin-images"><el-image v-for="image in row.images" :key="image" :src="image" fit="cover" :preview-src-list="row.images" preview-teleported /></div><div v-if="row.replies?.length" class="table-sub reply-count">已回复 {{row.replies.length}} 次 · 最近：{{row.replies[row.replies.length-1].content}}</div></template></el-table-column>
      <el-table-column label="景点" prop="scenicId" width="100"/><el-table-column label="用户" prop="userId" width="100"/><el-table-column label="时间" width="165"><template #default="{row}">{{formatDate(row.createTime)}}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status?'success':'info'">{{row.status?'显示':'隐藏'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="220" fixed="right"><template #default="{row}"><el-button link type="primary" @click="openReply(row)">继续回复</el-button><el-button link @click="toggleStatus(row)">{{row.status?'隐藏':'显示'}}</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="评价回复记录" width="min(600px, calc(100% - 28px))">
      <div v-if="current" class="reply-dialog"><div class="reply-original"><el-rate v-model="current.score" disabled/><p>{{current.content}}</p></div><div v-if="current.replies?.length" class="reply-history"><div v-for="item in current.replies" :key="item.id || item.content"><strong>{{item.adminName || '景区客服'}}</strong><small>{{formatDate(item.createTime)}}</small><p>{{item.content}}</p></div></div><el-input v-model="reply" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="输入新的回复，本次回复会追加到历史记录中"/></div>
      <template #footer><el-button @click="dialogVisible=false">关闭</el-button><el-button type="primary" :loading="saving" @click="saveReply">追加回复</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted,ref} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {Search} from '@element-plus/icons-vue'
import {adminApi} from '../../api/http'
const records=ref([]);const keyword=ref('');const status=ref();const loading=ref(false);const dialogVisible=ref(false);const reply=ref('');const current=ref(null);const saving=ref(false)
const load=async()=>{loading.value=true;try{records.value=await adminApi.comments({keyword:keyword.value,status:status.value})}finally{loading.value=false}};onMounted(load)
const openReply=row=>{current.value=row;reply.value='';dialogVisible.value=true}
const saveReply=async()=>{if(!reply.value.trim())return ElMessage.warning('请输入回复内容');saving.value=true;try{const result=await adminApi.replyComment({id:current.value.id,reply:reply.value.trim()});current.value.replies=current.value.replies||[];current.value.replies.push(result);current.value.status=1;reply.value='';ElMessage.success('回复已追加，可继续回复')}finally{saving.value=false}}
const toggleStatus=async row=>{await adminApi.commentStatus({id:row.id,status:row.status?0:1});row.status=row.status?0:1;ElMessage.success(row.status?'评价已显示':'评价已隐藏')}
const remove=async row=>{await ElMessageBox.confirm('确定删除这条评价及全部图片、回复吗？','删除评价',{type:'warning'});await adminApi.deleteComment(row.id);ElMessage.success('评价已删除');await load()}
const formatDate=value=>value?String(value).replace('T',' ').slice(0,16):''
</script>

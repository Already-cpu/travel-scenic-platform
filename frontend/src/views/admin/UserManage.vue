<template>
  <div class="admin-panel"><div class="admin-toolbar"><el-input v-model="keyword" placeholder="手机号 / 昵称" clearable @keyup.enter="search"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-button type="primary" @click="search">查询</el-button></div><el-table v-loading="loading" :data="records" class="admin-table" stripe><el-table-column label="用户" min-width="210"><template #default="{ row }"><div style="display:flex;align-items:center;gap:10px"><el-avatar :size="34" :src="row.avatar">{{ row.nickname?.slice(0,1) }}</el-avatar><div><div class="table-title">{{ row.nickname }}</div><div class="table-sub">{{ row.phone }}</div></div></div></template></el-table-column><el-table-column label="注册时间" width="180"><template #default="{ row }">{{ formatDate(row.createTime) }}</template></el-table-column><el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="row.status ? 'success' : 'danger'">{{ row.status ? '正常' : '已禁用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="180"><template #default="{ row }"><el-button link type="primary" @click="toggle(row)">{{ row.status ? '禁用' : '启用' }}</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><div style="display:flex;justify-content:flex-end;margin-top:20px"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" /></div></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '../../api/http'

const records = ref([]); const total = ref(0); const page = ref(1); const size = ref(10); const keyword = ref(''); const loading = ref(false)
const load = async () => { loading.value = true; try { const data = await adminApi.users({ current: page.value, size: size.value, keyword: keyword.value }); records.value = data.records; total.value = data.total } finally { loading.value = false } }; onMounted(load)
const search = () => { page.value = 1; load() }
const toggle = async row => { await adminApi.userStatus({ id: row.id, status: row.status ? 0 : 1 }); row.status = row.status ? 0 : 1; ElMessage.success('用户状态已更新') }; const remove = async row => { await ElMessageBox.confirm(`确定删除用户“${row.nickname}”吗？`, '删除用户', { type: 'warning' }); await adminApi.deleteUser(row.id); ElMessage.success('用户已删除'); await load() }; const formatDate = value => value ? String(value).replace('T', ' ').slice(0, 16) : ''
</script>

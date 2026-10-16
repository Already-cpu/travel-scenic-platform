<template>
  <div class="admin-panel">
    <div class="admin-toolbar">
      <el-input v-model="keyword" placeholder="订单号 / 景点 / 用户 / 联系人" clearable @keyup.enter="search"><template #prefix><el-icon><Search /></el-icon></template></el-input>
      <el-select v-model="status" placeholder="订单状态" clearable @change="search"><el-option label="待支付" value="PENDING" /><el-option label="已支付" value="PAID" /><el-option label="已取消" value="CANCELLED" /><el-option label="已完成" value="COMPLETED" /></el-select>
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" @change="search" /><el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="records" class="admin-table" stripe>
      <el-table-column label="订单信息" min-width="220"><template #default="{ row }"><div class="table-title">{{ row.scenicName }}</div><div class="table-sub">{{ row.orderNo }} · {{ row.visitDate }}</div></template></el-table-column>
      <el-table-column label="联系人" min-width="150"><template #default="{ row }">{{ row.contactName }}<div class="table-sub">{{ row.contactPhone }}</div></template></el-table-column>
      <el-table-column label="数量" prop="quantity" width="80" /><el-table-column label="金额" width="110"><template #default="{ row }"><strong style="color:#c65d3b">¥{{ row.totalPrice }}</strong></template></el-table-column>
      <el-table-column label="创建时间" width="170"><template #default="{ row }">{{ formatDate(row.createTime) }}</template></el-table-column>
      <el-table-column label="状态" width="150"><template #default="{ row }"><el-select :model-value="row.status" size="small" @change="value => changeStatus(row, value)"><el-option label="待支付" value="PENDING" /><el-option label="已支付" value="PAID" /><el-option label="已取消" value="CANCELLED" /><el-option label="已完成" value="COMPLETED" /></el-select></template></el-table-column>
      <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="showDetail(row)">详情</el-button></template></el-table-column>
    </el-table>
    <div style="display:flex;justify-content:flex-end;margin-top:20px"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" /></div>
    <el-dialog v-model="detailVisible" title="订单详情" width="min(520px, calc(100% - 30px))"><el-descriptions v-if="detail" :column="1" border><el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item><el-descriptions-item label="景点">{{ detail.scenicName }}</el-descriptions-item><el-descriptions-item label="游览日期">{{ detail.visitDate }}</el-descriptions-item><el-descriptions-item label="票种">{{ detail.ticketType || '成人票' }}</el-descriptions-item><el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item><el-descriptions-item label="联系人">{{ detail.contactName }} / {{ detail.contactPhone }}</el-descriptions-item><el-descriptions-item label="订单金额">¥{{ detail.totalPrice }}</el-descriptions-item><el-descriptions-item label="支付方式">{{ paymentMethodText(detail.paymentMethod) }}</el-descriptions-item><el-descriptions-item label="创建时间">{{ formatDate(detail.createTime) }}</el-descriptions-item><el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item></el-descriptions></el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '../../api/http'

const records = ref([]); const total = ref(0); const page = ref(1); const size = ref(10); const keyword = ref(''); const status = ref(''); const dateRange = ref([]); const loading = ref(false); const detailVisible = ref(false); const detail = ref(null)
const load = async () => { loading.value = true; try { const data = await adminApi.orders({ current: page.value, size: size.value, keyword: keyword.value, status: status.value, startDate: dateRange.value?.[0], endDate: dateRange.value?.[1] }); records.value = data.records; total.value = data.total } finally { loading.value = false } }; onMounted(load)
const search = () => { page.value = 1; load() }
const changeStatus = async (row, value) => { await adminApi.orderStatus({ id: row.id, status: value }); row.status = value; ElMessage.success('订单状态已更新') }
const showDetail = row => { detail.value = row; detailVisible.value = true }
const statusText = value => ({ PENDING: '待支付', PAID: '已支付', CANCELLED: '已取消', COMPLETED: '已完成' }[value] || value)
const paymentMethodText = value => ({ wallet: '钱包余额支付', scan: '扫码支付' }[value] || '尚未支付')
const formatDate = value => value ? String(value).replace('T', ' ').slice(0, 16) : ''
</script>

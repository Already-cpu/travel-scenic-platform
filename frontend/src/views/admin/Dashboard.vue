<template>
  <div ref="screen" class="dashboard" v-loading="loading">
    <div class="dashboard-heading">
      <div><div class="eyebrow">TRAVEL ANALYTICS</div><h2>运营数据总览</h2><p>从目的地热度到旅行反馈，让每一次运营有据可循。</p></div>
      <div class="heading-actions"><el-button @click="load" :disabled="loading">刷新数据</el-button><el-button type="primary" plain @click="toggleFullscreen">{{ fullscreen ? '退出大屏' : '全屏大屏' }}</el-button></div>
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="data-alert" />
    <template v-if="overview">
      <div class="stats-grid"><div v-for="item in stats" :key="item.label" class="stat-card"><div class="stat-label">{{ item.label }}</div><div class="stat-value">{{ number(item.value) }}</div></div></div>
      <div class="quick-grid"><button v-for="item in quickActions" :key="item.text" class="quick-action" @click="$router.push(item.to)"><el-icon :size="22"><component :is="item.icon" /></el-icon><span><strong>{{ item.text }}</strong>{{ item.note }}</span></button></div>
      <section class="ops-spotlight"><div class="ops-spotlight-head"><div><div class="eyebrow">TODAY'S WORKBENCH</div><h3>今日运营工作台</h3></div><span>把需要处理的事项集中在这里</span></div><div class="ops-spotlight-grid"><button v-for="item in opsHighlights" :key="item.title" class="ops-item" @click="$router.push(item.to)"><span class="ops-item-icon">{{ item.icon }}</span><span><strong>{{ item.title }}</strong><small>{{ item.text }}</small></span><b>{{ item.count }}</b><el-icon><ArrowRight /></el-icon></button></div></section>
      <div class="chart-grid core-grid">
        <section class="chart-panel"><header><div><h3>景点浏览量 TOP 10</h3><p>累计浏览次数 · 按浏览量降序</p></div></header><AnalyticsChart :option="viewsOption" label="景点浏览量前十名水平柱状图" :height="340" /></section>
        <section class="chart-panel"><header><div><h3>近 12 个月订单趋势</h3><p>按订单创建月份统计 · 无订单月份补零</p></div></header><AnalyticsChart :option="trendOption" label="近十二个月订单数量面积折线图" :height="340" /></section>
        <section class="chart-panel"><header><div><h3>景点地区分布</h3><p>全部景点按省级地区统计（含下架）</p></div></header><AnalyticsChart :option="regionOption" label="各省景点数量环形图" :height="340" /></section>
        <section class="recent-panel"><header><div><h3>最近 5 条订单</h3><p>按下单时间倒序排列</p></div><router-link to="/admin/order">查看全部 →</router-link></header>
          <div v-if="recentOrders.length"><div v-for="item in recentOrders" :key="item.id" class="recent-order"><div><strong>{{ item.scenicName }}</strong><small>{{ item.orderNo }} · {{ item.contactName }}</small><small>{{ formatTime(item.createTime) }} · ¥{{ money(item.totalPrice) }}</small></div><el-tag size="small" :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag></div></div>
          <el-empty v-else description="暂无订单" :image-size="80" />
        </section>
      </div>
      <div class="section-title"><div><div class="eyebrow">DEEPER INSIGHTS</div><h2>多维度运营分析</h2></div><span>数据来自 MySQL 实时汇总</span></div>
      <el-alert title="演示环境：包含标记为“演示”的订单、账号和评价，图表不代表真实经营业绩。" type="info" :closable="false" show-icon class="data-alert" />
      <div class="chart-grid">
        <section class="chart-panel">
          <header><div><h3>景点热度对比</h3><p>全部景点 · 指标可切换 · 滑动查看其余景点</p></div>
            <el-select v-model="heatMetric" aria-label="热度指标" class="compact-select"><el-option v-for="(label, key) in heatLabels" :key="key" :label="label" :value="key" /></el-select>
          </header>
          <AnalyticsChart :option="heatOption" label="各景点浏览量、有效预订量或收藏量排名" :height="385" />
          <p class="chart-note">有效预订量为已支付、已完成订单数；收藏量为当前收藏记录数。</p>
        </section>
        <section class="chart-panel">
          <header><div><h3>各景点评分分布</h3><p>仅统计公开评价 · 1—5 星完整分布</p></div>
            <el-select v-model="ratingId" filterable aria-label="评分景点" class="rating-select"><el-option v-for="item in overview.ratings" :key="item.scenicId" :label="item.name" :value="item.scenicId" /></el-select>
          </header>
          <div class="rating-summary"><strong>{{ selectedRating?.total ? Number(selectedRating.average).toFixed(2) : '—' }}<small> / 5 分</small></strong><span>{{ selectedRating?.total || 0 }} 条公开评价</span></div>
          <AnalyticsChart :option="ratingOption" label="所选景点一至五星评价数量柱状图" :height="322" />
          <p class="chart-note">隐藏评价不计入分布和均分；零评价景点显示空分布，不伪造评分。</p>
        </section>
        <section class="chart-panel order-analysis" v-loading="ordersLoading">
          <header><div><h3>订单数量与金额</h3><p>左轴：订单数 / 单；右轴：金额 / 元</p></div>
            <el-radio-group v-model="granularity" @change="loadOrders"><el-radio-button value="day" label="day">按日</el-radio-button><el-radio-button value="month" label="month">按月</el-radio-button><el-radio-button value="year" label="year">按年</el-radio-button></el-radio-group>
          </header>
          <div class="date-controls"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" :disabled-date="futureDate" @change="loadOrders" /><el-button text @click="resetRange">恢复默认范围</el-button><span v-if="orderData">{{ orderData.startDate }} — {{ orderData.endDate }}</span></div>
          <el-alert v-if="ordersError" :title="ordersError" type="error" :closable="false" />
          <div v-if="orderData" class="order-totals"><span>订单数 <strong>{{ number(orderTotals.count) }}</strong></span><span>下单金额 <strong>¥{{ money(orderTotals.amount) }}</strong></span><span>有效已付金额 <strong>¥{{ money(orderTotals.paidAmount) }}</strong></span></div>
          <AnalyticsChart :option="orderOption" label="日月年订单数量与金额双坐标轴图" :height="340" />
          <p class="chart-note">按订单创建时间归组。下单金额含全部状态；有效已付金额仅计当前已支付、已完成订单，并非支付日流水。统计时区为服务器时区。</p>
        </section>
        <section class="chart-panel user-analysis">
          <header><div><h3>用户增长与活跃</h3><p>近 12 个月注册趋势 · 活跃口径为最近登录时间</p></div></header>
          <div class="user-metrics"><div><span>注册用户</span><strong>{{ number(overview.users.registered) }}</strong></div><div><span>今日注册</span><strong>{{ number(overview.users.newToday) }}</strong></div><div><span>今日活跃</span><strong>{{ number(overview.users.activeToday) }}</strong></div><div><span>近 30 天活跃</span><strong>{{ number(overview.users.active30Days) }}</strong></div></div>
          <AnalyticsChart :option="userOption" label="近十二个月新增注册用户数量趋势图" :height="300" />
          <p class="chart-note">活跃人数按已启用用户去重；近 30 天活跃为滚动 30 天内登录的用户数，不是浏览次数。</p>
        </section>
      </div>
      <p class="dashboard-footnote">最后刷新 {{ refreshedAt }} · 统计卡片包含全部状态记录；数据变化后点击“刷新数据”。</p>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, ChatDotRound, DocumentAdd, Location, User } from '@element-plus/icons-vue'
import AnalyticsChart from '../../components/AnalyticsChart.vue'
import { adminApi } from '../../api/http'

const overview = ref(null), views = ref([]), trend = ref([]), regions = ref([]), recentOrders = ref([])
const loading = ref(false), error = ref(''), refreshedAt = ref(''), screen = ref(), fullscreen = ref(false)
const heatMetric = ref('views'), ratingId = ref(null), granularity = ref('month'), dateRange = ref(null)
const orderData = ref(null), ordersLoading = ref(false), ordersError = ref('')
let mounted = true, orderRequest = 0
const number = value => Number(value || 0).toLocaleString('zh-CN')
const money = value => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatTime = value => (value || '').replace('T', ' ').slice(0, 16)
const futureDate = date => date > new Date()
const statusText = s => ({ PENDING: '待支付', PAID: '已支付', CANCELLED: '已取消', COMPLETED: '已完成' }[s] || s)
const statusType = s => ({ PENDING: 'warning', PAID: 'success', CANCELLED: 'info', COMPLETED: 'primary' }[s] || 'info')
const heatLabels = { views: '浏览量', reservations: '有效预订量', favorites: '收藏量' }
const quickActions = [
  { text: '新增景点', note: '补充目的地', to: '/admin/scenic?create=1', icon: DocumentAdd },
  { text: '查看订单', note: '处理预约状态', to: '/admin/order', icon: Location },
  { text: '用户管理', note: '维护用户账号', to: '/admin/user', icon: User },
  { text: '评价管理', note: '回复游客反馈', to: '/admin/comment', icon: ChatDotRound }
]
const opsHighlights = computed(() => [
  { icon:'🧾', title:'待支付订单', text:'最近订单中需要关注的预约', count:recentOrders.value.filter(item => item.status === 'PENDING').length, to:'/admin/order' },
  { icon:'💬', title:'游客评价', text:'进入评价管理回复游客反馈', count:number(overview.value?.commentCount), to:'/admin/comment' },
  { icon:'🖼️', title:'景点内容', text:'检查图片、开放时间和票种库存', count:number(overview.value?.scenicCount), to:'/admin/scenic' }
])
const stats = computed(() => [
  { label: '总用户数', value: overview.value?.userCount }, { label: '总景点数', value: overview.value?.scenicCount },
  { label: '总订单数', value: overview.value?.orderCount }, { label: '总评价数', value: overview.value?.commentCount }
])
const colors = ['#c65d3b', '#28546a', '#69a49a', '#d1a565', '#7994b6', '#bf8f9d', '#8eaa70', '#a89278']
const axis = { type: 'value', min: 0, minInterval: 1, splitLine: { lineStyle: { color: '#eef1f4' } }, axisLabel: { color: '#728095' } }
const base = { color: colors, textStyle: { fontFamily: 'Microsoft YaHei, sans-serif' }, tooltip: { trigger: 'axis', confine: true }, animationDuration: 350, aria: { enabled: true } }
const horizontal = (rows, key, name, scrolling = false) => ({
  ...base, grid: { left: 8, right: scrolling ? 42 : 34, top: 12, bottom: 18, containLabel: true },
  xAxis: { ...axis, name: '', axisLabel: { color: '#728095', formatter: v => v >= 10000 ? (v / 10000).toFixed(1) + '万' : v } },
  yAxis: { type: 'category', inverse: true, data: rows.map(r => r.name), axisTick: { show: false }, axisLine: { show: false }, axisLabel: { color: '#526173', width: 120, overflow: 'truncate' } },
  dataZoom: scrolling ? [{ type: 'slider', yAxisIndex: 0, right: 0, width: 12, startValue: 0, endValue: Math.min(9, rows.length - 1), showDetail: false }] : [],
  series: [{ name, type: 'bar', data: rows.map(r => Number(r[key])), barMaxWidth: 17, itemStyle: { borderRadius: [0, 4, 4, 0] }, label: { show: true, position: 'right', fontSize: 10 } }]
})
const viewsOption = computed(() => horizontal(views.value, 'views', '浏览次数'))
const heatOption = computed(() => horizontal([...(overview.value?.hotness || [])].sort((a, b) => b[heatMetric.value] - a[heatMetric.value]), heatMetric.value, heatLabels[heatMetric.value], true))
const trendOption = computed(() => ({
  ...base, grid: { left: 12, right: 14, top: 25, bottom: 16, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: trend.value.map(r => r.month), axisLabel: { rotate: 35, color: '#728095' } }, yAxis: axis,
  series: [{ name: '订单数', type: 'line', smooth: true, data: trend.value.map(r => r.count), symbolSize: 6, lineStyle: { width: 3 }, areaStyle: { color: 'rgba(198,93,59,.15)' } }]
}))
const regionOption = computed(() => ({
  ...base, tooltip: { trigger: 'item', confine: true, formatter: '{b}: {c} 个 ({d}%)' },
  legend: { type: 'scroll', bottom: 0, textStyle: { color: '#728095' } },
  series: [{ type: 'pie', radius: ['40%', '64%'], center: ['50%', '44%'], minShowLabelAngle: 8, label: { formatter: '{b}', fontSize: 11 }, labelLayout: { hideOverlap: true }, data: regions.value, itemStyle: { borderColor: '#fff', borderWidth: 3 } }]
}))
const selectedRating = computed(() => overview.value?.ratings?.find(r => r.scenicId === ratingId.value))
const ratingOption = computed(() => ({
  ...base, grid: { left: 12, right: 15, top: 22, bottom: 20, containLabel: true },
  xAxis: { type: 'category', data: ['1 星', '2 星', '3 星', '4 星', '5 星'] }, yAxis: { ...axis, name: '评价数' },
  series: [{ name: '评价数', type: 'bar', barMaxWidth: 36, data: [1, 2, 3, 4, 5].map(n => Number(selectedRating.value?.['score' + n] || 0)), label: { show: true, position: 'top' }, itemStyle: { color: '#d1a565', borderRadius: [5, 5, 0, 0] } }]
}))
const orderTotals = computed(() => (orderData.value?.records || []).reduce((sum, row) => ({ count: sum.count + Number(row.count), amount: sum.amount + Number(row.amount), paidAmount: sum.paidAmount + Number(row.paidAmount) }), { count: 0, amount: 0, paidAmount: 0 }))
const orderOption = computed(() => {
  const records = orderData.value?.records || []
  return {
    ...base, legend: { top: 3, data: ['订单数', '下单金额', '有效已付金额'] },
    grid: { left: 15, right: 20, top: 65, bottom: 52, containLabel: true },
    xAxis: { type: 'category', data: records.map(r => r.period), axisLabel: { hideOverlap: true, color: '#728095' } },
    yAxis: [{ ...axis, name: '数量 / 单' }, { ...axis, name: '金额 / 元', minInterval: 0, splitLine: { show: false }, axisLabel: { formatter: v => v >= 10000 ? (v / 10000).toFixed(1) + '万' : v } }],
    dataZoom: records.length > 15 ? [{ type: 'slider', bottom: 5, height: 20, start: 0, end: 100 }] : [],
    series: [
      { name: '订单数', type: 'bar', data: records.map(r => r.count), barMaxWidth: 28 },
      { name: '下单金额', type: 'line', yAxisIndex: 1, data: records.map(r => r.amount), symbolSize: 5 },
      { name: '有效已付金额', type: 'line', yAxisIndex: 1, data: records.map(r => r.paidAmount), symbolSize: 5 }
    ]
  }
})
const userOption = computed(() => ({
  ...base, grid: { left: 12, right: 15, top: 30, bottom: 18, containLabel: true },
  xAxis: { type: 'category', data: (overview.value?.users?.trend || []).map(r => r.month), axisLabel: { rotate: 35, color: '#728095' } }, yAxis: { ...axis, name: '新增用户 / 人' },
  series: [{ name: '新增注册', type: 'bar', barMaxWidth: 24, data: (overview.value?.users?.trend || []).map(r => r.count), itemStyle: { color: '#69a49a', borderRadius: [4, 4, 0, 0] } }]
}))
async function load() {
  if (loading.value) return
  loading.value = true; error.value = ''
  try {
    // Exactly five initial parallel requests, matching requirement 4.3.
    const result = await Promise.all([adminApi.stats.overview(), adminApi.stats.scenicViews(), adminApi.stats.orderTrend(), adminApi.stats.region(), adminApi.recentOrders()])
    if (!mounted) return
    ;[overview.value, views.value, trend.value, regions.value, recentOrders.value] = result
    ratingId.value = overview.value.ratings?.some(r => r.scenicId === ratingId.value) ? ratingId.value : overview.value.ratings?.[0]?.scenicId
    orderRequest++; orderData.value = overview.value.orders; granularity.value = 'month'; dateRange.value = null; ordersError.value = ''; ordersLoading.value = false
    refreshedAt.value = new Date().toLocaleString('zh-CN')
  } catch { error.value = '数据加载失败，请确认后端已启动，然后点击刷新数据。' }
  finally { loading.value = false }
}
async function loadOrders() {
  const current = ++orderRequest
  ordersLoading.value = true; ordersError.value = ''; orderData.value = null
  try {
    const result = await adminApi.stats.orders({ granularity: granularity.value, startDate: dateRange.value?.[0], endDate: dateRange.value?.[1] })
    if (mounted && current === orderRequest) orderData.value = result
  } catch (e) { if (current === orderRequest) ordersError.value = e.message || '订单统计加载失败' }
  finally { if (current === orderRequest) ordersLoading.value = false }
}
function resetRange() { dateRange.value = null; loadOrders() }
const updateFullscreen = () => { fullscreen.value = document.fullscreenElement === screen.value }
async function toggleFullscreen() {
  try { if (document.fullscreenElement) await document.exitFullscreen(); else await screen.value.requestFullscreen() }
  catch { ElMessage.info('当前浏览器不支持全屏，可使用浏览器全屏模式。') }
}
onMounted(() => { document.addEventListener('fullscreenchange', updateFullscreen); load() })
onBeforeUnmount(() => { mounted = false; orderRequest++; document.removeEventListener('fullscreenchange', updateFullscreen) })
</script>

<style scoped>
.dashboard { min-width: 0; }
.dashboard:fullscreen { overflow: auto; padding: 28px; background: #f3f5f7; }
.dashboard-heading, .section-title { display: flex; justify-content: space-between; align-items: center; gap: 15px; margin-bottom: 22px; }
.dashboard-heading h2, .section-title h2 { margin: 6px 0; font-size: 24px; letter-spacing: -.03em; }
.dashboard-heading p, .section-title > span { color: #728095; font-size: 13px; margin: 8px 0 0; }
.heading-actions { display: flex; flex-shrink: 0; }
.section-title { margin-top: 36px; }
.data-alert { margin-bottom: 20px; }
.chart-panel, .recent-panel { min-width: 0; }
.chart-panel header, .recent-panel header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 10px; }
header h3 { margin: 0; font-size: 16px; }
header p { margin: 7px 0 0; font-size: 12px; color: #728095; line-height: 1.6; }
header a { color: #c65d3b; font-size: 12px; }
.compact-select { width: 135px; }
.rating-select { width: 210px; max-width: 100%; }
.chart-note, .dashboard-footnote { color: #728095; font-size: 12px; line-height: 1.8; margin: 8px 0 0; }
.dashboard-footnote { text-align: right; margin-top: 20px; }
.order-analysis, .user-analysis { grid-column: 1 / -1; }
.date-controls { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin: 16px 0; font-size: 12px; color: #728095; }
.date-controls :deep(.el-date-editor) { max-width: 360px; width: 100%; flex: initial; box-sizing: border-box; }
.order-totals { display: flex; flex-wrap: wrap; gap: 15px 28px; font-size: 12px; color: #728095; margin-bottom: 14px; }
.order-totals strong { color: #172b4d; font-size: 18px; margin-left: 8px; }
.user-metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin: 20px 0; }
.user-metrics div { background: #f6f9fa; padding: 14px; border-radius: 8px; }
.user-metrics span { font-size: 12px; color: #728095; }
.user-metrics strong { display: block; margin-top: 6px; font-size: 24px; }
.rating-summary { display: flex; align-items: baseline; gap: 18px; margin-top: 14px; color: #728095; font-size: 12px; }
.rating-summary strong { font-size: 26px; color: #a86c27; }
.rating-summary small { font-size: 13px; font-weight: 400; }
.recent-order > div { min-width: 0; }
.recent-order small { overflow-wrap: anywhere; }
.recent-order .el-tag { flex-shrink: 0; }
.ops-spotlight { margin:18px 0 22px; padding:18px 20px; border:1px solid #e3e8ed; border-radius:12px; background:linear-gradient(110deg,#f7fafb,#fff8f4); }
.ops-spotlight-head { display:flex; align-items:end; justify-content:space-between; gap:12px; margin-bottom:13px; }.ops-spotlight-head .eyebrow { margin:0 0 5px; }.ops-spotlight-head h3 { margin:0; color:#172b4d; font-size:18px; }.ops-spotlight-head > span { color:#728095; font-size:12px; }
.ops-spotlight-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; }.ops-item { display:grid; grid-template-columns:auto 1fr auto auto; align-items:center; gap:10px; min-width:0; padding:13px; border:1px solid #e4e9ec; border-radius:10px; background:#fff; text-align:left; color:#172b4d; cursor:pointer; transition:.2s; }.ops-item:hover { border-color:#e2ad99; transform:translateY(-2px); box-shadow:0 8px 18px rgba(16,42,67,.07); }.ops-item-icon { display:grid; place-items:center; width:35px; height:35px; border-radius:10px; background:#fff1eb; font-size:18px; }.ops-item > span:nth-child(2) { min-width:0; display:grid; gap:4px; }.ops-item strong,.ops-item small { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }.ops-item strong { font-size:13px; }.ops-item small { color:#728095; font-size:11px; }.ops-item b { color:#c65d3b; font-size:17px; }.ops-item > .el-icon { color:#aeb8c1; }
@media (max-width: 800px) { .dashboard-heading { align-items: flex-start; flex-direction: column; } .chart-grid { grid-template-columns: 1fr; } .section-title { align-items: flex-start; flex-direction: column; } }
@media (max-width: 800px) { .ops-spotlight-grid { grid-template-columns:1fr; } .ops-spotlight-head { align-items:flex-start; flex-direction:column; } }
@media (max-width: 500px) { .chart-panel, .recent-panel { padding: 12px; } .user-metrics { grid-template-columns: repeat(2, 1fr); } .dashboard-heading h2 { font-size: 21px; } .quick-action { font-size: 11px; padding: 10px; } .ops-spotlight { padding:14px; } }
</style>

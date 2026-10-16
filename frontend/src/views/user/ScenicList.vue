<template>
  <div class="container">
    <div class="page-heading"><div class="eyebrow">Find your next view</div><h1>景点列表</h1><p>按地区、类型和价格找到适合这一趟旅程的风景。</p></div>
    <div class="filter-panel">
      <el-input v-model="filters.keyword" placeholder="搜索景点名称或关键词" clearable @keyup.enter="search"><template #prefix><el-icon><Search /></el-icon></template></el-input>
      <el-select v-model="filters.province" placeholder="全部地区" clearable @change="search"><el-option v-for="item in provinces" :key="item" :label="item" :value="item" /></el-select>
      <el-select v-model="filters.type" placeholder="全部类型" clearable @change="search"><el-option v-for="item in types" :key="item" :label="item" :value="item" /></el-select>
      <el-select v-model="filters.sort" placeholder="推荐排序" clearable @change="search"><el-option label="最新推荐" value="newest" /><el-option label="价格从低到高" value="priceAsc" /><el-option label="价格从高到低" value="priceDesc" /><el-option label="人气优先" value="views" /></el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>
    <div class="list-discovery-bar"><div><strong>发现 {{ total }} 处目的地</strong><span>{{ resultHint }}</span></div><div class="list-view-actions"><el-button :type="viewMode === 'grid' ? 'primary' : 'info'" plain size="small" @click="viewMode='grid'">▦ 卡片</el-button><el-button :type="viewMode === 'list' ? 'primary' : 'info'" plain size="small" @click="viewMode='list'">☰ 列表</el-button></div></div>
    <div class="list-curated-strip"><button v-for="item in curatedTags" :key="item.title" @click="applyCurated(item)"><img :src="item.image" :alt="item.title"/><span><strong>{{ item.title }}</strong><small>{{ item.note }}</small></span><el-icon><ArrowRight /></el-icon></button></div>
    <div v-if="list.length" :class="['scenic-grid','scenic-results',{ 'scenic-list-view': viewMode === 'list' }]" style="margin-top: 20px"><ScenicCard v-for="item in list" :key="item.id" :scenic="item" /></div>
    <div v-else class="empty-state">没有找到匹配的景点，换个关键词试试。</div>
    <div v-if="total" style="display:flex; justify-content:center; margin: 34px 0 70px"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="prev, pager, next" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowRight, Search } from '@element-plus/icons-vue'
import { scenicApi } from '../../api/http'
import ScenicCard from '../../components/ScenicCard.vue'

const route = useRoute()
const list = ref([]); const total = ref(0); const page = ref(1); const size = ref(9)
const viewMode = ref('grid')
const filters = reactive({ keyword: route.query.keyword || '', province: route.query.province || '', type: route.query.type || '', sort: '' })
const provinces = ref([])
const types = ref([])
const curatedTags = [
  { title:'周末山水', note:'适合短途放空', image:'/scenic-images/11-li-river.jpg', query:{ type:'湖泊山水' } },
  { title:'人文古迹', note:'走进城市故事', image:'/scenic-images/2-forbidden-city.jpg', query:{ type:'历史古迹' } },
  { title:'亲子自然', note:'轻松认识世界', image:'/scenic-images/6-panda.jpg', query:{ type:'亲子研学' } },
  { title:'海边慢游', note:'把假期交给海风', image:'/scenic-images/9-yalong-bay.jpg', query:{ type:'海滨休闲' } }
]
const resultHint = computed(() => { const parts = [filters.province, filters.type, filters.keyword].filter(Boolean); return parts.length ? `当前筛选：${parts.join(' · ')}` : '实景图片、票种价格和开放信息已整理'; })
const load = async () => { const data = await scenicApi.list({ current: page.value, size: size.value, ...filters }); list.value = data.records; total.value = data.total }
const search = () => { page.value = 1; load() }
const applyCurated = item => { Object.assign(filters, { keyword:'', province:'', type:item.query.type || '', sort:'' }); search() }
watch(() => route.query, value => { filters.keyword = value.keyword || ''; filters.province = value.province || ''; filters.type = value.type || ''; search() }, { deep: true })
onMounted(async () => { const [meta] = await Promise.all([scenicApi.filters(), load()]); provinces.value = meta.provinces; types.value = meta.types })
</script>

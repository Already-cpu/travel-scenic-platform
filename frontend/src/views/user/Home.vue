<template>
  <div>
    <section class="hero">
      <div class="container">
        <div class="hero-layout">
          <div class="hero-copy">
            <div class="hero-kicker">A slower way to see China</div>
            <h1>下一站，<br />去看山河。</h1>
            <p>从一处风景开始，找到适合你的周末、假期，和那些值得被记住的路。</p>
            <div class="hero-search">
              <el-input v-model="keyword" size="large" placeholder="搜索景点、城市或旅行灵感" @keyup.enter="search">
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" size="large" @click="search">开始探索</el-button>
            </div>
            <div class="hero-notes"><span><strong>{{ total }}</strong>精选目的地</span><span><strong>24h</strong>随时规划</span><span><strong>实景</strong>山河影像</span></div>
          </div>
          <div v-if="hot.length" class="home-carousel">
            <el-carousel height="var(--home-carousel-height)" indicator-position="outside" motion-blur>
              <el-carousel-item v-for="item in hot" :key="item.id">
                <button class="carousel-slide" type="button" @click="router.push(`/scenic/${item.id}`)">
                  <img :src="item.image || fallback" :alt="item.name" @error="onImageError" />
                  <span class="carousel-overlay"></span>
                  <span class="carousel-caption"><small>{{ item.province }} · {{ item.type }}</small><strong>{{ item.name }}</strong><em>查看景点详情 →</em></span>
                </button>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>
      </div>
    </section>

    <section class="home-quick-nav"><div class="container"><button v-for="item in quickNav" :key="item.title" @click="goCategory(item.query)"><span class="quick-nav-icon"><el-icon><component :is="item.icon" /></el-icon></span><span><strong>{{ item.title }}</strong><small>{{ item.subtitle }}</small></span><el-icon><ArrowRight /></el-icon></button></div></section>

    <section class="section destination-strip-section"><div class="container"><div class="section-head"><div><div class="eyebrow">Trending destinations</div><h2>大家都在搜</h2><p>从热门城市开始，快速找到下一站的灵感。</p></div><router-link to="/scenic"><el-button text type="primary">全部目的地 <el-icon><ArrowRight /></el-icon></el-button></router-link></div><div class="destination-strip"><button v-for="item in destinationTags" :key="item.name" @click="router.push({path:'/scenic',query:{keyword:item.name}})"><img :src="item.image" :alt="item.name"/><span><strong>{{ item.name }}</strong><small>{{ item.note }}</small></span><el-icon><ArrowRight /></el-icon></button></div></div></section>

    <section class="section">
      <div class="container">
        <div class="section-head"><div><div class="eyebrow">Curated for you</div><h2>此刻值得出发</h2><p>从热门目的地开始，为下一次周末留一份期待。</p></div><router-link to="/scenic"><el-button text type="primary">查看全部 <el-icon><ArrowRight /></el-icon></el-button></router-link></div>
        <div v-if="hot.length" class="scenic-grid"><ScenicCard v-for="item in hot" :key="item.id" :scenic="item" /></div>
        <div v-else class="empty-state">正在加载精选景点…</div>
      </div>
    </section>

    <section class="section journey-section">
      <div class="container"><div class="section-head"><div><div class="eyebrow">Travel inspiration</div><h2>按旅行心情出发</h2><p>像选择一段生活方式一样，选择这次旅程。</p></div></div><div class="journey-grid"><button v-for="item in journeys" :key="item.title" @click="goCategory(item.query)" :style="{backgroundImage:`linear-gradient(180deg,transparent,rgba(8,28,45,.82)),url(${item.image})`}"><span>{{item.tag}}</span><strong>{{item.title}}</strong><small>{{item.text}}</small></button></div></div>
    </section>

    <section class="section theme-section"><div class="container"><div class="section-head"><div><div class="eyebrow">Trip themes</div><h2>按主题安排一场旅行</h2><p>不用先决定去哪座城市，从旅行目的出发，也能找到适合自己的目的地。</p></div><router-link to="/scenic"><el-button text type="primary">探索更多 <el-icon><ArrowRight /></el-icon></el-button></router-link></div><div class="theme-layout"><div class="theme-grid"><button v-for="item in themeCollections" :key="item.title" class="theme-card" @click="item.link ? router.push(item.link) : goCategory(item.query)"><img :src="item.image" :alt="item.title"/><span class="theme-shade"></span><span class="theme-copy"><small>{{ item.tag }}</small><strong>{{ item.title }}</strong><em>{{ item.text }}</em></span></button></div><aside class="trip-checklist"><div class="checklist-head"><span>出发前 10 分钟</span><strong>旅行准备清单</strong><small>把重要的小事提前做好，旅途会轻松很多。</small></div><div v-for="(item,index) in tripChecklist" :key="item.title" class="checklist-item"><b>0{{ index + 1 }}</b><span><strong>{{ item.title }}</strong><small>{{ item.text }}</small></span></div><router-link to="/scenic" class="checklist-link">先去挑一个目的地 <el-icon><ArrowRight /></el-icon></router-link></aside></div></div></section>

    <section class="section seasonal-section"><div class="container"><div class="section-head"><div><div class="eyebrow">Seasonal picks</div><h2>十月正当时</h2><p>结合季节景观与站内热度，挑选适合近期出发的目的地。</p></div></div><div class="seasonal-layout"><button class="seasonal-feature" @click="router.push('/scenic/12')"><img src="/scenic-images/12-jiuzhaigou.jpg" alt="九寨沟秋季景观"/><span class="seasonal-shade"></span><span class="seasonal-copy"><small>秋色目的地 · 四川阿坝</small><strong>九寨沟层林与彩池</strong><p>建议预留一整天，清晨入园可以更从容地安排长线游览。</p><em>查看景点与票价 →</em></span></button><div class="travel-ranking"><div class="ranking-head"><div><span>站内热度榜</span><strong>本周热门景点</strong></div><small>依据浏览量排序</small></div><button v-for="(item,index) in hot.slice(0,5)" :key="item.id" @click="router.push(`/scenic/${item.id}`)"><b>{{ String(index+1).padStart(2,'0') }}</b><img :src="item.image" :alt="item.name"/><span><strong>{{ item.name }}</strong><small>{{ item.city }} · {{ item.type }}</small></span><em>{{ item.price>0?`¥${item.price}起`:'免费' }}</em></button></div></div></div></section>

    <section class="section editorial-section"><div class="container"><div class="section-head"><div><div class="eyebrow">Editor's guide</div><h2>编辑精选旅行指南</h2><p>从路线安排、游览时长到拍照时间，提前了解目的地的游玩重点。</p></div></div><div class="editorial-grid"><article v-for="item in editorialGuides" :key="item.title" @click="router.push(item.link)"><img :src="item.image" :alt="item.title"/><div><span>{{ item.tag }}</span><h3>{{ item.title }}</h3><p>{{ item.text }}</p><small>{{ item.readTime }} · {{ item.city }}</small></div></article></div></div></section>

    <section class="section service-section"><div class="container"><div class="section-head"><div><div class="eyebrow">Travel service</div><h2>一站式旅行灵感</h2><p>从挑选景点到收藏计划，把出发前的准备做得更轻松。</p></div></div><div class="service-grid"><div v-for="item in services" :key="item.title" class="service-card"><span class="service-card-icon"><el-icon :size="22"><component :is="item.icon" /></el-icon></span><div><strong>{{ item.title }}</strong><p>{{ item.text }}</p></div><el-icon class="service-arrow"><ArrowRight /></el-icon></div></div></div></section>

    <section class="section" style="padding-top: 0">
      <div class="container">
        <div class="feature-band">
          <div class="feature-item"><div class="feature-icon"><el-icon :size="20"><Compass /></el-icon></div><h3>信息清楚再出发</h3><p>实景图片、开放时间、分票种价格、库存与图文评价一次看清。</p></div>
          <div class="feature-item"><div class="feature-icon"><el-icon :size="20"><Calendar /></el-icon></div><h3>先登录再预订</h3><p>避免填完信息才跳转登录，登录后直接选择票种、日期和数量。</p></div>
          <div class="feature-item"><div class="feature-icon"><el-icon :size="20"><ChatDotRound /></el-icon></div><h3>AI 帮你做选择</h3><p>按预算、地区和旅行偏好提问，快速得到站内景点推荐。</p></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Calendar, ChatDotRound, Compass, Food, Guide, Search, Ship, Star, Tickets, MapLocation, Camera } from '@element-plus/icons-vue'
import { scenicApi } from '../../api/http'
import ScenicCard from '../../components/ScenicCard.vue'

const router = useRouter()
const hot = ref([])
const keyword = ref('')
const total = ref(0)
const fallback = '/image-unavailable.svg'
const quickNav = [
  { title:'山河秘境', subtitle:'登高与自然', query:{ type:'山岳风光' }, icon:Guide },
  { title:'江南古镇', subtitle:'慢游与人文', query:{ type:'古镇人文' }, icon:Compass },
  { title:'海边度假', subtitle:'阳光与浪花', query:{ type:'海滨休闲' }, icon:Ship },
  { title:'亲子研学', subtitle:'陪孩子探索', query:{ type:'亲子研学' }, icon:Star },
  { title:'城市乐园', subtitle:'轻松玩一天', query:{ type:'主题乐园' }, icon:Food }
]
const journeys = [
  { tag:'山野', title:'去高处看云海', text:'把城市留在山脚', image:'/scenic-images/5-huangshan.jpg', query:{ type:'山岳风光' } },
  { tag:'水乡', title:'在古镇住一晚', text:'等灯火落进河里', image:'/scenic-images/8-wuzhen.jpg', query:{ type:'古镇人文' } },
  { tag:'海风', title:'把假期交给海岸', text:'慢一点，也很好', image:'/scenic-images/9-yalong-bay.jpg', query:{ type:'海滨休闲' } }
]
const destinationTags = [
  { name:'北京', note:'人文古迹', image:'/scenic-images/2-forbidden-city.jpg' },
  { name:'杭州', note:'湖光山色', image:'/scenic-images/1-west-lake.jpg' },
  { name:'成都', note:'亲子慢游', image:'/scenic-images/6-panda.jpg' },
  { name:'三亚', note:'海岛假期', image:'/scenic-images/9-yalong-bay.jpg' },
  { name:'桂林', note:'山水画卷', image:'/scenic-images/11-li-river.jpg' }
]
const services = [
  { title:'景点门票', text:'分票种展示价格与库存，先登录再预订。', icon:Tickets },
  { title:'目的地攻略', text:'开放时间、地址、类型和真实景观图一次看全。', icon:MapLocation },
  { title:'游客体验', text:'查看带图评价与景区多次回复，做出更安心的选择。', icon:Camera },
  { title:'AI 旅行助手', text:'告诉它预算和偏好，快速获得站内景点推荐。', icon:ChatDotRound }
]
const editorialGuides = [
  { tag:'路线规划', title:'第一次去北京，经典古迹怎么排更顺路', text:'从故宫、天坛到颐和园，按区域减少往返，把有限时间留给真正想看的风景。', readTime:'阅读 5 分钟', city:'北京', image:'/scenic-images/2-forbidden-city.jpg', link:'/scenic/2' },
  { tag:'亲子旅行', title:'成都熊猫基地半日游，带孩子这样逛更轻松', text:'早到、少走回头路，并为休息和用餐预留时间，亲子行程不必安排得太满。', readTime:'阅读 4 分钟', city:'成都', image:'/scenic-images/6-panda.jpg', link:'/scenic/6' },
  { tag:'摄影建议', title:'黄山看云海，季节、天气和机位都很重要', text:'山地天气变化快，提前准备备选路线，在安全范围内等待更合适的光线。', readTime:'阅读 6 分钟', city:'黄山', image:'/scenic-images/5-huangshan.jpg', link:'/scenic/5' }
]
const themeCollections = [
  { tag:'2 天 1 夜', title:'周末短途放空', text:'山水、古镇和一段不赶路的时间', image:'/scenic-images/11-li-river.jpg', query:{ type:'湖泊山水' } },
  { tag:'亲子友好', title:'带孩子认识自然', text:'轻松步道、动物朋友与探索时光', image:'/scenic-images/6-panda.jpg', query:{ type:'亲子研学' } },
  { tag:'人文漫游', title:'走进城市旧时光', text:'从一座建筑读懂一座城', image:'/scenic-images/2-forbidden-city.jpg', link:'/scenic/2' },
  { tag:'海边慢生活', title:'把假期交给海风', text:'看日落、听海浪，暂时放慢脚步', image:'/scenic-images/9-yalong-bay.jpg', query:{ type:'海滨休闲' } }
]
const tripChecklist = [
  { title:'先看开放时间', text:'避开临时闭馆和最后入园时段' },
  { title:'确认票种与库存', text:'成人、学生和优待票分别选择' },
  { title:'保存电子订单', text:'登录后在“我的行程”随时查看' },
  { title:'准备天气装备', text:'防晒、雨具和舒适鞋服别忘记' }
]
onMounted(async () => { try { const [items, meta] = await Promise.all([scenicApi.hot(), scenicApi.filters()]); hot.value = items; total.value = meta.total } catch { /* message handled by http */ } })
const search = () => router.push({ path: '/scenic', query: keyword.value ? { keyword: keyword.value } : {} })
const goCategory = query => router.push({ path:'/scenic', query })
const onImageError = (event) => { event.target.onerror = null; event.target.src = fallback }
</script>

<template>
  <div v-if="scenic" class="container">
    <nav class="detail-breadcrumb"><router-link to="/">首页</router-link><span>/</span><router-link to="/scenic">景点列表</router-link><span>/</span><router-link :to="{path:'/scenic',query:{province:scenic.province}}">{{ scenic.province }}</router-link><span>/</span><strong>{{ scenic.name }}</strong></nav>
    <section class="detail-hero"><div class="detail-layout">
      <div class="detail-visual"><img class="detail-image" :src="scenic.image" :alt="scenic.name" @error="imageError" /><span class="detail-badge">{{ scenic.type }}</span></div>
      <div class="detail-copy">
        <div class="eyebrow">{{ scenic.province }} · {{ scenic.city }}</div><h1>{{ scenic.name }}</h1><div class="detail-rating-line"><strong>{{ ratingAverage || '暂无' }}</strong><el-rate :model-value="ratingAverage" disabled/><span>{{ comments.length }} 条评价</span><i>·</i><span>{{ profile.duration }}</span></div><p class="detail-intro">{{ scenic.intro }}</p>
        <div class="detail-info">
          <div class="detail-info-item"><el-icon><Location /></el-icon><span>地址<br /><strong>{{ scenic.address }}</strong></span></div>
          <div class="detail-info-item"><el-icon><Clock /></el-icon><span>开放时间<br /><strong>{{ scenic.openTime || '以景区公告为准' }}</strong></span></div>
          <div class="detail-info-item"><el-icon><View /></el-icon><span>浏览量<br /><strong>{{ scenic.views }}</strong></span></div>
          <div class="detail-info-item"><el-icon><Ticket /></el-icon><span>门票起价<br /><strong class="price">{{ minPrice > 0 ? `¥${minPrice}` : '免费' }}</strong></span></div>
        </div>
        <div class="ticket-preview"><div v-for="ticket in activeTickets" :key="ticket.id" :class="['ticket-preview-item',{sold:!ticket.stock}]"><span>{{ ticket.name }}</span><strong>{{ ticket.price>0?`¥${ticket.price}`:'免费' }}</strong><small>{{ ticket.stock ? `余 ${ticket.stock} 张` : '已售罄' }}</small></div></div>
        <p class="demo-notice">课程演示：价格、库存、开放时间及模拟支付仅用于功能展示，出行请以景区官方公告为准。</p>
        <div class="detail-trust"><span>✓ 随时退改演示</span><span>✓ 库存实时显示</span><span>✓ 订单安全留存</span></div>
        <div class="detail-actions"><el-button type="primary" size="large" round @click="openBooking">预订门票</el-button><el-button size="large" round @click="toggleFavorite"><el-icon><StarFilled v-if="favorite" /><Star v-else /></el-icon>{{ favorite ? '已收藏' : '收藏景点' }}</el-button></div>
      </div>
    </div></section>

    <div class="detail-directory-layout">
      <nav class="detail-anchor-nav" aria-label="景点详情目录"><div class="directory-label">本页目录</div><a v-for="item in directoryItems" :key="item.id" :href="`#${item.id}`" :class="{active: activeDirectory === item.id}" @click="activeDirectory = item.id"><span>{{ item.number }}</span><strong>{{ item.label }}</strong><small>{{ item.note }}</small></a></nav>

    <section id="overview" class="detail-rich-section">
      <div class="detail-main-column">
        <article class="detail-content-card"><div class="eyebrow">Highlights</div><h2>为什么值得去</h2><p class="detail-story">{{ scenic.intro }} 结合当地景观特色，建议预留 {{ profile.duration }}，放慢节奏感受沿途空间和光线变化。</p><div class="highlight-list"><div v-for="(item,index) in profile.highlights" :key="item"><span>0{{ index + 1 }}</span><p>{{ item }}</p></div></div></article>
        <article id="route-guide" class="detail-content-card"><div class="detail-card-head"><div><div class="eyebrow">Suggested itinerary</div><h2>推荐游览路线</h2></div><el-tag effect="plain">{{ profile.duration }}</el-tag></div><div class="route-timeline"><div v-for="(step,index) in routeSteps" :key="step.title"><span>{{ index + 1 }}</span><div><strong>{{ step.title }}</strong><small>{{ step.time }}</small><p>{{ step.text }}</p></div></div></div><p class="route-note">路线为课程项目中的通用游览建议，请根据当日开放区域、天气和体力灵活调整。</p></article>
        <article id="tickets" class="detail-content-card"><div class="detail-card-head"><div><div class="eyebrow">Booking guide</div><h2>门票与入园须知</h2></div><el-tag type="warning" effect="light">出行前确认</el-tag></div><div class="notice-grid"><div><strong>预订说明</strong><p>请选择游览日期和对应票种，库存会在下单时实时扣减。课程项目为模拟支付，不产生真实交易。</p></div><div><strong>入园凭证</strong><p>订单支付后可在“我的行程”查看。答辩演示中以订单号作为电子凭证。</p></div><div><strong>退改规则</strong><p>待支付订单可以取消并自动返还库存；已支付或已完成订单请联系景区工作人员处理。</p></div><div><strong>优惠人群</strong><p>学生票、儿童票和优待票的适用条件以景区现场政策为准，建议随身携带有效证件。</p></div></div></article>
        <article id="facilities" class="detail-content-card"><div class="eyebrow">Visitor services</div><h2>服务设施</h2><div class="facility-grid"><div v-for="item in profile.facilities" :key="item.label"><span>{{ item.icon }}</span><strong>{{ item.label }}</strong><small>{{ item.text }}</small></div></div></article>
        <article id="travel-tips" class="detail-content-card"><div class="eyebrow">Practical tips</div><div class="detail-card-head"><div><h2>到达与游玩建议</h2><p class="card-subtitle">把交通、拍照、用餐和安全信息提前放进行程。</p></div><el-tag type="success" effect="light">出发前收藏</el-tag></div><div class="practical-grid"><div v-for="item in practicalTips" :key="item.title"><span class="practical-icon">{{ item.icon }}</span><strong>{{ item.title }}</strong><p>{{ item.text }}</p></div></div></article>
        <article id="faq" class="detail-content-card"><div class="eyebrow">FAQ</div><h2>常见问题</h2><el-collapse class="detail-faq"><el-collapse-item v-for="(item,index) in faqItems" :key="item.question" :name="index"><template #title><strong>{{ item.question }}</strong></template><p>{{ item.answer }}</p></el-collapse-item></el-collapse></article>
      </div>
      <aside class="detail-side-column">
        <div class="rating-summary"><div><strong>{{ ratingAverage || '—' }}</strong><span>综合评分</span></div><el-rate :model-value="ratingAverage" disabled/><p>{{ comments.length ? `来自 ${comments.length} 位游客的公开评价` : '暂时还没有公开评价' }}</p><div class="score-bars"><div v-for="item in scoreDistribution" :key="item.score"><span>{{ item.score }} 分</span><i><em :style="{width:`${item.percent}%`}"></em></i><small>{{ item.count }}</small></div></div></div>
        <div class="travel-facts"><h3>行前参考</h3><div><span>建议时长</span><strong>{{ profile.duration }}</strong></div><div><span>推荐季节</span><strong>{{ profile.season }}</strong></div><div><span>游玩强度</span><strong>{{ profile.level }}</strong></div><div><span>适合人群</span><strong>{{ profile.people }}</strong></div><p>开放时间和天气可能临时调整，出发前请再次核实景区公告。</p></div>
      </aside>
    </section>
    </div>

    <section v-if="similarScenics.length" class="section similar-section"><div class="section-head"><div><div class="eyebrow">You may also like</div><h2>同类型景点推荐</h2><p>继续看看 {{ scenic.type }} 目的地，为行程多准备一个选择。</p></div><router-link :to="{path:'/scenic',query:{type:scenic.type}}"><el-button text type="primary">查看更多</el-button></router-link></div><div class="scenic-grid"><ScenicCard v-for="item in similarScenics" :key="item.id" :scenic="item" /></div></section>

    <section id="reviews" class="section review-section"><div class="section-head"><div><div class="eyebrow">Traveler stories</div><h2>游客图文评价</h2><p>{{ comments.length ? `已有 ${comments.length} 条真实体验记录，其中 ${photoReviewCount} 条包含实景照片` : '成为第一个分享体验的人' }}</p></div><el-button type="primary" plain @click="openReview"><el-icon><EditPen /></el-icon>发表评价</el-button></div>
      <div class="review-list" v-if="comments.length"><article v-for="comment in comments" :key="comment.id" class="review-card"><div class="review-author"><el-avatar :size="42">旅</el-avatar><div><strong>旅行者 #{{ comment.userId }}</strong><small>{{ formatDate(comment.createTime) }}</small></div><el-rate v-model="comment.score" disabled /></div><p>{{ comment.content }}</p><div v-if="comment.images?.length" class="review-gallery"><el-image v-for="image in comment.images" :key="image" :src="image" fit="cover" :preview-src-list="comment.images" preview-teleported /></div><div v-if="comment.replies?.length" class="reply-thread"><div v-for="reply in comment.replies" :key="reply.id || reply.content" class="reply-item"><span>景区回复</span><div><strong>{{ reply.adminName || '景区客服' }}</strong><p>{{ reply.content }}</p><small>{{ formatDate(reply.createTime) }}</small></div></div></div></article></div>
      <div v-else class="empty-state review-empty">还没有评价，去亲自看看这处风景吧。</div>
    </section>

    <el-dialog v-model="reviewVisible" title="发表图文评价" width="min(600px, calc(100% - 24px))" destroy-on-close>
      <p class="dialog-intro">登录后即可分享你对 {{ scenic.name }} 的真实体验，每位用户对同一景点可评价一次。</p>
      <el-form ref="reviewRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="评分" prop="score"><el-rate v-model="reviewForm.score" show-score score-template="{value} 分" /></el-form-item>
        <el-form-item label="评价内容" prop="content"><el-input v-model="reviewForm.content" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="说说景色、服务或游览体验吧" /></el-form-item>
        <el-form-item label="添加照片（最多 6 张）"><el-upload action="#" :show-file-list="false" :http-request="uploadReviewImage" accept="image/jpeg,image/png,image/gif,image/webp" :disabled="reviewForm.images.length>=6"><el-button :loading="reviewUploading" plain><el-icon><Picture /></el-icon>上传实景照片</el-button></el-upload><div v-if="reviewForm.images.length" class="review-upload-grid"><div v-for="(image,index) in reviewForm.images" :key="image"><img :src="image" /><button type="button" @click="reviewForm.images.splice(index,1)"><el-icon><Close /></el-icon></button></div></div></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewVisible=false">取消</el-button><el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button></template>
    </el-dialog>

    <el-dialog v-model="bookingVisible" title="预订门票" width="min(580px, calc(100% - 24px))" destroy-on-close>
      <p class="dialog-intro">{{ scenic.name }} · 选择票种后将显示准确价格和剩余库存</p>
      <el-form :model="orderForm" :rules="orderRules" ref="orderRef" label-position="top">
        <el-form-item label="游览日期" prop="visitDate"><el-date-picker v-model="orderForm.visitDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disableDate" placeholder="选择游览日期" style="width:100%" /></el-form-item>
        <el-form-item label="选择票种" prop="ticketId"><div class="booking-ticket-list"><button v-for="ticket in activeTickets" :key="ticket.id" type="button" :disabled="!ticket.stock" :class="{active:orderForm.ticketId===ticket.id}" @click="selectTicket(ticket)"><span>{{ ticket.name }}</span><strong>{{ ticket.price>0?`¥${ticket.price}`:'免费' }}</strong><small>{{ ticket.stock?`剩余 ${ticket.stock} 张`:'已售罄' }}</small></button></div></el-form-item>
        <div class="booking-form-grid"><el-form-item label="数量" prop="quantity"><el-input-number v-model="orderForm.quantity" :min="1" :max="maxQuantity" style="width:100%" /></el-form-item><el-form-item label="联系人" prop="contactName"><el-input v-model="orderForm.contactName" placeholder="姓名" /></el-form-item></div>
        <el-form-item label="手机号" prop="contactPhone"><el-input v-model="orderForm.contactPhone" maxlength="11" placeholder="用于接收演示订单信息" /></el-form-item>
      </el-form>
      <template #footer><div class="booking-footer"><div><small>{{ selectedTicket?.name || '请选择票种' }}</small><span class="price">合计 ¥{{ totalPrice }}</span></div><span><el-button @click="bookingVisible=false">稍后再订</el-button><el-button type="primary" @click="submitOrder" :loading="submitting" :disabled="!selectedTicket?.stock">提交订单</el-button></span></div></template>
    </el-dialog>

    <el-dialog v-model="paymentVisible" title="订单已创建 · 请选择支付方式" width="min(460px, calc(100% - 28px))" destroy-on-close>
      <div class="payment-order-summary"><span>{{ paymentOrder?.scenicName }}</span><strong>¥{{ paymentOrder?.totalPrice }}</strong><small>订单号：{{ paymentOrder?.orderNo }} · {{ paymentOrder?.ticketType }} × {{ paymentOrder?.quantity }}</small></div>
      <el-radio-group v-model="paymentMethod" class="payment-methods"><el-radio-button value="wallet" label="wallet"><span class="payment-method"><b>💰</b><span><strong>钱包余额支付</strong><small>演示钱包余额 ¥{{ walletBalance.toFixed(2) }}</small></span></span></el-radio-button><el-radio-button value="scan" label="scan"><span class="payment-method"><b>▦</b><span><strong>扫码支付</strong><small>使用手机扫码完成演示支付</small></span></span></el-radio-button></el-radio-group>
      <div v-if="paymentMethod === 'scan'" class="payment-scan-box"><div class="payment-qr"><i v-for="n in 169" :key="n" :class="{dark: paymentQr[n - 1]}"/><span>行旅志</span></div><p>请使用手机扫描二维码<br/><small>课程演示二维码，不产生真实扣款</small></p></div>
      <div v-else class="wallet-balance-box"><span>当前演示钱包余额</span><strong>¥{{ walletBalance.toFixed(2) }}</strong><small>支付成功后订单状态将更新为“已支付”</small></div>
      <p class="dialog-intro" style="margin:14px 0 0">暂不支付也不会丢失订单，可稍后前往“我的行程”继续支付。</p>
      <template #footer><el-button @click="paymentVisible=false">暂不支付</el-button><el-button type="primary" :loading="paymentSubmitting" @click="confirmPay">确认支付</el-button></template>
    </el-dialog>
    <LoginPromptDialog v-model="loginPromptVisible" :redirect="route.fullPath" />
  </div>
  <div v-else class="empty-state">正在打开景点详情…</div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Close, EditPen, Location, Picture, Star, StarFilled, Ticket, View } from '@element-plus/icons-vue'
import { commentApi, favoriteApi, orderApi, scenicApi, uploadApi, userApi } from '../../api/http'
import LoginPromptDialog from '../../components/LoginPromptDialog.vue'
import ScenicCard from '../../components/ScenicCard.vue'

const route=useRoute();const scenic=ref(null);const comments=ref([]);const similarScenics=ref([]);const favorite=ref(false);const bookingVisible=ref(false);const submitting=ref(false);const orderRef=ref();const reviewVisible=ref(false);const reviewSubmitting=ref(false);const reviewUploading=ref(false);const reviewRef=ref()
const orderForm=reactive({visitDate:'',ticketId:null,ticketType:'',quantity:1,contactName:'',contactPhone:''});const reviewForm=reactive({score:5,content:'',images:[]});const loginPromptVisible=ref(false);const paymentVisible=ref(false);const paymentSubmitting=ref(false);const paymentOrder=ref(null);const paymentMethod=ref('wallet');const walletBalance=ref(0)
const directoryItems=[{id:'overview',number:'01',label:'景点概览',note:'亮点介绍'},{id:'route-guide',number:'02',label:'推荐路线',note:'游览安排'},{id:'tickets',number:'03',label:'门票须知',note:'入园规则'},{id:'facilities',number:'04',label:'服务设施',note:'现场服务'},{id:'travel-tips',number:'05',label:'实用攻略',note:'出行建议'},{id:'faq',number:'06',label:'常见问题',note:'出行答疑'},{id:'reviews',number:'07',label:'游客评价',note:'真实体验'}];const activeDirectory=ref('overview')
const activeTickets=computed(()=>(scenic.value?.tickets||[]).filter(ticket=>ticket.status)); const selectedTicket=computed(()=>activeTickets.value.find(ticket=>ticket.id===orderForm.ticketId)); const minPrice=computed(()=>activeTickets.value.length?Math.min(...activeTickets.value.map(ticket=>Number(ticket.price))):Number(scenic.value?.price||0));const maxQuantity=computed(()=>Math.max(1,Math.min(20,Number(selectedTicket.value?.stock||1))));const totalPrice=computed(()=>((Number(selectedTicket.value?.price||0))*orderForm.quantity).toFixed(2))
const paymentQr=computed(()=>{const values=[];for(let i=0;i<169;i++)values.push(((i*31+i%7*11+17)%13)<6);const mark=(x,y)=>{for(let dy=0;dy<5;dy++)for(let dx=0;dx<5;dx++)values[(y+dy)*13+x+dx]=dx===0||dx===4||dy===0||dy===4||(dx>1&&dx<4&&dy>1&&dy<4)};mark(0,0);mark(8,0);mark(0,8);return values})
const profiles={山岳风光:{duration:'4–6 小时',season:'春、秋季',level:'中等',people:'摄影、徒步游客',highlights:['登高途中可从不同角度观察山体、云雾与植被层次。','清晨和傍晚光线更柔和，适合拍摄风景与人物照片。','山地天气变化较快，合理安排索道、步行和休息时间。'],facilities:[{icon:'🚡',label:'观光交通',text:'部分路段可乘索道或接驳车'},{icon:'🥤',label:'补给点',text:'主要游览节点设有饮水和简餐'},{icon:'🩹',label:'医疗服务',text:'游客中心提供基础应急帮助'},{icon:'🧳',label:'行李寄存',text:'以游客中心现场服务为准'}]},湖泊山水:{duration:'3–5 小时',season:'春夏秋季',level:'轻松',people:'家庭、摄影游客',highlights:['沿湖或沿江步道视野开阔，适合慢行和停留拍照。','水面、远山与城市人文景观形成丰富的游览层次。','建议避开正午强光，清晨或傍晚的倒影和色彩更柔和。'],facilities:[{icon:'⛵',label:'游船服务',text:'部分区域可体验游船或摆渡'},{icon:'☕',label:'休息驿站',text:'沿线设有座椅和休息空间'},{icon:'📷',label:'观景平台',text:'重点景观附近设拍照点位'},{icon:'🚻',label:'公共卫生间',text:'主要入口及游览区均有设置'}]},历史古迹:{duration:'2–4 小时',season:'四季皆宜',level:'轻松',people:'人文、亲子游客',highlights:['建筑空间、历史陈列与传统文化共同构成游览重点。','建议提前了解历史背景，配合现场讲解获得更完整体验。','热门时段游客较多，早到可以更从容地安排参观路线。'],facilities:[{icon:'🎧',label:'讲解服务',text:'可按现场规则租用语音讲解'},{icon:'🗺️',label:'导览地图',text:'入口处提供路线与区域说明'},{icon:'🧑‍🦽',label:'无障碍服务',text:'部分区域提供无障碍通道'},{icon:'🎒',label:'寄存服务',text:'大件物品按景区要求寄存'}]},古镇人文:{duration:'半天–1 天',season:'春、秋季',level:'轻松',people:'情侣、家庭游客',highlights:['水巷、街道和传统建筑适合放慢节奏边走边看。','白天适合看建筑细节，傍晚与夜间灯光更有氛围。','可以结合地方饮食与手工体验，丰富古镇游览内容。'],facilities:[{icon:'🛶',label:'水上体验',text:'部分古镇提供摇橹船项目'},{icon:'🍜',label:'地方餐饮',text:'主街和支巷分布特色小吃'},{icon:'🏮',label:'夜游灯光',text:'亮灯时间以当日公告为准'},{icon:'🛍️',label:'文创商店',text:'可选购地方手作和纪念品'}]},海滨休闲:{duration:'半天–1 天',season:'秋冬春季',level:'轻松',people:'亲子、度假游客',highlights:['海岸线开阔，适合散步、看日落和轻松度假。','紫外线和海风较强，建议准备防晒、防风和饮水。','涉水活动应关注天气、潮汐与现场安全提示。'],facilities:[{icon:'🏖️',label:'沙滩服务',text:'开放区域设休息和冲洗设施'},{icon:'🛟',label:'安全救援',text:'请在指定水域和时段活动'},{icon:'🥥',label:'餐饮补给',text:'周边提供饮品和简餐'},{icon:'🚿',label:'更衣冲洗',text:'具体开放情况以现场为准'}]}}
const defaultProfile={duration:'3–5 小时',season:'四季皆宜',level:'轻松',people:'家庭、朋友出游',highlights:['景点拥有鲜明的地方特色，适合安排半日深度游览。','建议提前查看开放时间和票种库存，错峰出行体验更好。','穿着舒适鞋服，并根据天气准备防晒或雨具。'],facilities:[{icon:'ℹ️',label:'游客中心',text:'提供咨询和基础导览服务'},{icon:'🍱',label:'餐饮补给',text:'主要游览区周边可用餐'},{icon:'🚻',label:'公共卫生间',text:'入口和主要节点均有设置'},{icon:'🅿️',label:'停车服务',text:'车位情况以现场为准'}]}
const profile=computed(()=>profiles[scenic.value?.type]||defaultProfile);const ratingAverage=computed(()=>comments.value.length?Number((comments.value.reduce((sum,item)=>sum+Number(item.score||0),0)/comments.value.length).toFixed(1)):0);const photoReviewCount=computed(()=>comments.value.filter(item=>item.images?.length).length);const scoreDistribution=computed(()=>[5,4,3,2,1].map(score=>{const count=comments.value.filter(item=>Number(item.score)===score).length;return{score,count,percent:comments.value.length?Math.round(count/comments.value.length*100):0}}))
const routeSteps=computed(()=>{
  const type=scenic.value?.type||''
  if(type.includes('山岳'))return[{title:'游客中心与入园准备',time:'约 20 分钟',text:'核对票务、天气和线路开放情况，补充饮水并做好防晒。'},{title:'核心山景游览',time:'约 2–3 小时',text:'结合步行、索道或接驳交通游览主要观景区域，减少重复路线。'},{title:'高处观景与休息',time:'约 1 小时',text:'在安全观景区停留拍照，根据体力决定是否继续延伸路线。'},{title:'返程与文创休息区',time:'约 40 分钟',text:'预留返程排队时间，在游客中心整理行程并适当休息。'}]
  if(type.includes('古迹')||type.includes('博物馆'))return[{title:'入口导览与安检',time:'约 20 分钟',text:'提前准备身份证件，领取或查看电子导览地图。'},{title:'核心建筑与常设展',time:'约 1.5 小时',text:'优先参观代表性建筑和重点展区，了解历史背景。'},{title:'专题区域深度参观',time:'约 1 小时',text:'根据兴趣选择专题陈列、园林或文化体验区域。'},{title:'出口文创与周边漫步',time:'约 30 分钟',text:'避开闭馆前集中离场，为周边街区留出少量时间。'}]
  return[{title:'游客中心确认信息',time:'约 15 分钟',text:'查看开放区域、票务凭证和推荐路线，确认返程时间。'},{title:'核心景观慢游',time:'约 1.5–2 小时',text:'沿主要游线欣赏代表性景观，在重点位置适当停留。'},{title:'特色体验与休息',time:'约 1 小时',text:'根据兴趣选择游船、讲解、餐饮或亲子体验项目。'},{title:'返程与周边补充游览',time:'约 30–60 分钟',text:'在体力允许的情况下游览附近区域，并预留离场时间。'}]
})
const faqItems=computed(()=>[
  {question:'需要提前多久预订？',answer:`热门日期建议提前 1–3 天查看 ${scenic.value?.name||'景点'} 的票种库存。本项目库存会在下单时实时扣减。`},
  {question:'儿童、学生或老人如何购买优惠票？',answer:'请选择对应优惠票种，并按景区实际规定携带学生证、身份证等有效证件；课程演示价格不代表官方票价。'},
  {question:'天气不好还能正常游览吗？',answer:'开放情况由景区根据天气和安全条件决定。山地、水上及海滨项目更容易受到天气影响，建议出发前再次查看公告。'},
  {question:'订单在哪里查看和取消？',answer:'登录用户端后进入“我的行程”查看订单。待支付订单可以取消，系统会自动返还对应票种库存。'}
])
const practicalTips=computed(()=>{
  const type=scenic.value?.type||''
  if(type.includes('山岳'))return[{icon:'🚗',title:'如何到达',text:'建议从游客中心开始规划，山路和接驳车时间要留出余量。'},{icon:'📷',title:'拍照时段',text:'清晨云雾和傍晚侧光更适合拍山景，观景台请按秩序停留。'},{icon:'🍜',title:'补给建议',text:'入园前准备饮水和少量能量补给，沿线餐饮点以现场开放为准。'},{icon:'🧥',title:'安全提醒',text:'山顶温度和风力变化较快，穿防滑鞋，遇雨雾时优先选择安全路线。'}]
  if(type.includes('古迹')||type.includes('博物馆'))return[{icon:'🚇',title:'如何到达',text:'热门时段建议公共交通出行，安检和预约核验请预留时间。'},{icon:'📷',title:'拍照时段',text:'开馆后和闭馆前人流相对舒缓，室内展区请遵守拍摄规定。'},{icon:'🍵',title:'周边用餐',text:'可在景区外围安排地方餐饮，参观中合理规划休息和用餐时间。'},{icon:'🎫',title:'入馆提醒',text:'携带有效证件，部分展区可能需要单独预约，以现场公告为准。'}]
  return[{icon:'🚌',title:'如何到达',text:'优先确认景区入口和停车区域，周末及节假日建议提前出发。'},{icon:'📷',title:'拍照时段',text:'清晨和傍晚光线更柔和，热门机位请排队并注意不要影响他人。'},{icon:'🍜',title:'周边用餐',text:'景区周边通常有地方餐饮和简餐，长时间游览可提前安排休息点。'},{icon:'🧴',title:'安全提醒',text:'根据天气准备防晒或雨具，涉水、亲子和夜游项目请遵循现场提示。'}]
})
const orderRules={visitDate:[{required:true,message:'请选择游览日期',trigger:'change'}],ticketId:[{required:true,message:'请选择票种',trigger:'change'}],quantity:[{required:true,message:'请选择数量',trigger:'change'}],contactName:[{required:true,message:'请输入联系人',trigger:'blur'}],contactPhone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1[3-9]\d{9}$/,message:'手机号格式不正确',trigger:'blur'}]};const reviewRules={score:[{required:true,message:'请选择评分',trigger:'change'}],content:[{required:true,message:'请输入评价内容',trigger:'blur'},{min:5,message:'评价至少 5 个字',trigger:'blur'}]}
const load=async()=>{const id=route.params.id;const[detail,list]=await Promise.all([scenicApi.detail(id),commentApi.scenic(id)]);scenic.value=detail;comments.value=list;const related=await scenicApi.list({current:1,size:6,type:detail.type});similarScenics.value=(related.records||[]).filter(item=>String(item.id)!==String(id)).slice(0,3);const first=activeTickets.value.find(ticket=>ticket.stock)||activeTickets.value[0];if(first)selectTicket(first);favorite.value=false;if(sessionStorage.getItem('user_token')){try{favorite.value=await favoriteApi.check(id)}catch{}}};onMounted(load);watch(()=>route.params.id,()=>{window.scrollTo({top:0,behavior:'smooth'});load()})
const imageError=event=>{event.target.onerror=null;event.target.src='/image-unavailable.svg'};const disableDate=date=>date<new Date(new Date().setHours(0,0,0,0));const formatDate=value=>value?String(value).replace('T',' ').slice(0,16):''
const requireLogin=()=>{if(sessionStorage.getItem('user_token'))return true;loginPromptVisible.value=true;return false}
const openBooking=async()=>{if(!requireLogin())return;const traveler=await userApi.info();orderForm.contactName=traveler.realName||traveler.nickname||'';orderForm.contactPhone=traveler.phone||'';walletBalance.value=Number(traveler.walletBalance||0);bookingVisible.value=true};const selectTicket=ticket=>{orderForm.ticketId=ticket.id;orderForm.ticketType=ticket.name;if(orderForm.quantity>ticket.stock)orderForm.quantity=Math.max(1,Math.min(20,ticket.stock))}
const toggleFavorite=async()=>{if(!requireLogin())return;if(favorite.value)await favoriteApi.remove(scenic.value.id);else await favoriteApi.add(scenic.value.id);favorite.value=!favorite.value;ElMessage.success(favorite.value?'已收藏':'已取消收藏')}
const submitOrder=async()=>{if(!requireLogin())return;if(!await orderRef.value.validate().catch(()=>false))return;submitting.value=true;try{const order=await orderApi.create({scenicId:scenic.value.id,...orderForm});const ticket=activeTickets.value.find(item=>item.id===order.ticketId);if(ticket)ticket.stock=Math.max(0,Number(ticket.stock)-Number(order.quantity));paymentOrder.value=order;paymentMethod.value='wallet';bookingVisible.value=false;paymentVisible.value=true;ElMessage.success('订单已创建，请选择支付方式')}finally{submitting.value=false}}
const confirmPay=async()=>{if(!paymentOrder.value)return;if(paymentMethod.value==='wallet'&&Number(paymentOrder.value.totalPrice)>walletBalance.value)return ElMessage.warning('钱包余额不足，请选择扫码支付');paymentSubmitting.value=true;try{const balance=await orderApi.pay(paymentOrder.value.id,paymentMethod.value);walletBalance.value=Number(balance);paymentOrder.value.paymentMethod=paymentMethod.value;paymentOrder.value.status='PAID';paymentVisible.value=false;ElMessage.success(paymentMethod.value==='wallet'?'钱包余额支付成功，可在我的行程查看订单':'扫码支付成功，可在我的行程查看订单')}finally{paymentSubmitting.value=false}}
const openReview=()=>{if(!requireLogin())return;reviewForm.score=5;reviewForm.content='';reviewForm.images=[];reviewVisible.value=true}
const uploadReviewImage=async({file})=>{if(reviewForm.images.length>=6)return;reviewUploading.value=true;try{reviewForm.images.push(await uploadApi.image(file));ElMessage.success('照片已添加')}finally{reviewUploading.value=false}}
const submitReview=async()=>{if(!await reviewRef.value.validate().catch(()=>false))return;reviewSubmitting.value=true;try{const comment=await commentApi.create({scenicId:scenic.value.id,score:reviewForm.score,content:reviewForm.content,images:[...reviewForm.images]});comments.value.unshift(comment);reviewVisible.value=false;ElMessage.success('图文评价已提交')}finally{reviewSubmitting.value=false}}
</script>

<template>
  <div class="admin-panel">
    <div class="admin-toolbar">
      <el-input v-model="keyword" placeholder="按名称搜索" clearable @keyup.enter="search"><template #prefix><el-icon><Search /></el-icon></template></el-input>
      <el-select v-model="province" placeholder="地区筛选" clearable @change="search"><el-option v-for="item in filterProvinces" :key="item" :label="item" :value="item" /></el-select>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增景点</el-button>
    </div>
    <el-table v-loading="loading" :data="records" class="admin-table" stripe>
      <el-table-column prop="name" label="景点名称" min-width="190"><template #default="{ row }"><div class="table-title">{{ row.name }}</div><div class="table-sub">{{ row.province }} · {{ row.city }}</div></template></el-table-column>
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column label="票种" min-width="190"><template #default="{ row }"><div class="ticket-tags"><el-tag v-for="ticket in row.tickets" :key="ticket.id" size="small" :type="ticket.stock ? '' : 'danger'">{{ ticket.name }} ¥{{ ticket.price }} / {{ ticket.stock }}张</el-tag></div></template></el-table-column>
      <el-table-column prop="views" label="浏览量" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '已上架' : '已下架' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="210" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link @click="toggleStatus(row)">{{ row.status ? '下架' : '上架' }}</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" /></div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑景点' : '新增景点'" width="min(820px, calc(100% - 28px))" destroy-on-close>
      <el-form :model="form" label-position="top" :rules="rules" ref="formRef">
        <div class="scenic-form-grid">
          <el-form-item label="景点名称" prop="name"><el-input v-model="form.name" placeholder="请输入景点正式名称" /></el-form-item>
          <el-form-item label="景点类型" prop="type"><el-select v-model="form.type" filterable style="width:100%"><el-option v-for="item in scenicTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item>
          <el-form-item label="省份" prop="province"><el-select v-model="form.province" filterable style="width:100%" @change="provinceChanged"><el-option v-for="item in allProvinces" :key="item" :label="item" :value="item" /></el-select></el-form-item>
          <el-form-item label="城市" prop="city"><el-select v-model="form.city" filterable style="width:100%"><el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
          <el-form-item label="开放时间" prop="openTime"><div class="open-time-picker"><el-time-picker v-model="form.startTime" format="HH:mm" value-format="HH:mm" placeholder="开始时间" @change="syncOpenTime"/><span>至</span><el-time-picker v-model="form.endTime" format="HH:mm" value-format="HH:mm" placeholder="结束时间" @change="syncOpenTime"/></div><small class="form-tip">开始时间必须早于结束时间，当前值：{{ form.openTime || '未设置' }}</small></el-form-item>
          <el-form-item label="上下架状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" /></el-form-item>
        </div>
        <el-form-item label="详细地址" prop="address"><el-input v-model="form.address" placeholder="选择省市后补充街道和景区入口" /></el-form-item>

        <div class="ticket-editor-head"><div><strong>票种与库存</strong><p>不同票种分别设置价格和库存，下单时会实时扣减。</p></div><el-button plain type="primary" @click="addTicket"><el-icon><Plus /></el-icon>添加票种</el-button></div>
        <div class="ticket-editor">
          <div v-for="(ticket,index) in form.tickets" :key="index" class="ticket-editor-row">
            <el-select v-model="ticket.name" filterable allow-create default-first-option placeholder="票种名称"><el-option v-for="name in ticketNames" :key="name" :label="name" :value="name" /></el-select>
            <el-input-number v-model="ticket.price" :min="0" :precision="2" controls-position="right"><template #prefix>¥</template></el-input-number>
            <el-input-number v-model="ticket.stock" :min="0" :max="99999" controls-position="right" />
            <el-switch v-model="ticket.status" :active-value="1" :inactive-value="0" />
            <el-button circle text type="danger" :disabled="form.tickets.length===1" @click="form.tickets.splice(index,1)"><el-icon><Delete /></el-icon></el-button>
          </div>
          <div class="ticket-editor-labels"><span>票种名称</span><span>单价</span><span>剩余库存</span><span>在售</span><span></span></div>
        </div>

        <el-form-item label="景点图片"><div class="image-upload-line"><el-upload action="#" :show-file-list="false" :http-request="uploadImage" accept="image/jpeg,image/png,image/gif,image/webp"><el-button :loading="uploading"><el-icon><Upload /></el-icon>上传实景图</el-button></el-upload><el-input v-model="form.image" placeholder="也可以填写图片 URL" /></div><img v-if="form.image" :src="form.image" class="image-preview scenic-preview" /></el-form-item>
        <el-form-item label="景点介绍" prop="intro"><el-input v-model="form.intro" type="textarea" :rows="5" maxlength="2000" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存景点</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, Upload } from '@element-plus/icons-vue'
import { scenicApi, uploadApi } from '../../api/http'
import { citiesFor, provinces as allProvinces, scenicTypes } from '../../data/regions'

const records=ref([]); const total=ref(0); const page=ref(1); const size=ref(10); const keyword=ref(''); const province=ref(''); const loading=ref(false); const saving=ref(false); const uploading=ref(false); const dialogVisible=ref(false); const formRef=ref(); const route=useRoute(); const filterProvinces=ref([])
const ticketNames=['成人票','学生票','儿童票','老人票','优待票','亲子票','家庭票']
const defaultTickets = price => [{ name:'成人票', price:Number(price||0), stock:500, status:1 }, { name:'学生票', price:Number((Number(price||0)*0.65).toFixed(2)), stock:300, status:1 }, { name:'优待票', price:Number((Number(price||0)*0.5).toFixed(2)), stock:200, status:1 }]
const blank=()=>({ id:null,name:'',province:'',city:'',address:'',type:'自然风光',price:0,intro:'',image:'',openTime:'08:00-18:00',startTime:'08:00',endTime:'18:00',status:1,tickets:defaultTickets(0) }); const form=reactive(blank()); const cityOptions=computed(()=>citiesFor(form.province))
const rules={ name:[{required:true,message:'请输入景点名称',trigger:'blur'}],province:[{required:true,message:'请选择省份',trigger:'change'}],city:[{required:true,message:'请选择城市',trigger:'change'}],address:[{required:true,message:'请输入地址',trigger:'blur'}],type:[{required:true,message:'请选择类型',trigger:'change'}],openTime:[{required:true,message:'请选择开放时间',trigger:'change'}],intro:[{required:true,message:'请输入介绍',trigger:'blur'}] }
const load=async()=>{ loading.value=true; try{ const data=await scenicApi.adminList({current:page.value,size:size.value,keyword:keyword.value,province:province.value}); records.value=data.records; total.value=data.total } finally{loading.value=false} }
onMounted(async()=>{ const [meta]=await Promise.all([scenicApi.adminFilters(),load()]); filterProvinces.value=meta.provinces; if(route.query.create==='1') openCreate() })
const search=()=>{page.value=1;load()}; const provinceChanged=()=>{form.city=''}
const parseOpenTime=value=>{const match=String(value||'').match(/^(\d{2}:\d{2})-(\d{2}:\d{2})$/);return match?{startTime:match[1],endTime:match[2]}:{startTime:'08:00',endTime:'18:00'}}
const syncOpenTime=()=>{if(form.startTime&&form.endTime)form.openTime=`${form.startTime}-${form.endTime}`}
const openCreate=()=>{Object.assign(form,blank());dialogVisible.value=true}
const openEdit=row=>{Object.assign(form,{...row,...parseOpenTime(row.openTime),tickets:(row.tickets?.length?row.tickets:defaultTickets(row.price)).map(ticket=>({...ticket}))});dialogVisible.value=true}
const addTicket=()=>{ if(form.tickets.length>=8)return ElMessage.warning('最多配置 8 个票种'); form.tickets.push({name:'',price:0,stock:100,status:1}) }
const uploadImage=async({file})=>{uploading.value=true;try{form.image=await uploadApi.image(file);ElMessage.success('图片上传成功')}finally{uploading.value=false}}
const save=async()=>{ if(!await formRef.value.validate().catch(()=>false))return; if(!form.startTime||!form.endTime)return ElMessage.warning('请选择开放开始和结束时间'); if(form.startTime>=form.endTime)return ElMessage.warning('开放开始时间必须早于结束时间'); syncOpenTime(); if(!form.tickets.length||form.tickets.some(t=>!t.name||t.price==null||t.stock==null))return ElMessage.warning('请完整填写票种、价格和库存'); if(new Set(form.tickets.map(t=>t.name.trim())).size!==form.tickets.length)return ElMessage.warning('票种名称不能重复'); form.price=Math.min(...form.tickets.filter(t=>t.status).map(t=>Number(t.price)),...form.tickets.map(t=>Number(t.price))); saving.value=true;try{const payload=JSON.parse(JSON.stringify(form));if(form.id)await scenicApi.update(payload);else await scenicApi.create(payload);dialogVisible.value=false;ElMessage.success('保存成功');await load()}finally{saving.value=false}}
const toggleStatus=async row=>{await scenicApi.status({id:row.id,status:row.status?0:1});ElMessage.success('状态已更新');await load()}
const remove=async row=>{await ElMessageBox.confirm(`确定删除“${row.name}”吗？删除后不可恢复。`,'删除景点',{type:'warning'});await scenicApi.remove(row.id);ElMessage.success('已删除');await load()}
</script>

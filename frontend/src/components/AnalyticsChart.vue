<template><div ref="container" class="analytics-chart" :style="{ height: `${height}px` }" role="img" :aria-label="label"></div></template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ option: { type: Object, required: true }, label: String, height: { type: Number, default: 320 } })
const container = ref()
let chart, observer
onMounted(() => {
  chart = echarts.init(container.value)
  chart.setOption(props.option)
  observer = new ResizeObserver(() => chart?.resize())
  observer.observe(container.value)
})
watch(() => props.option, option => chart?.setOption(option, true), { deep: true })
onBeforeUnmount(() => { observer?.disconnect(); chart?.dispose(); chart = null })
</script>

<style scoped>.analytics-chart { width: 100%; min-width: 0; }</style>

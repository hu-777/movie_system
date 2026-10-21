<template>
  <div class="charts-grid">
    <section v-for="(title,i) in titles" :key="title" class="surface chart-card" v-loading="loading[i]">
      <header><h2>{{ title }}</h2><el-button v-if="errors[i]" text @click="loadChart(i)">重试</el-button></header>
      <div v-if="errors[i]" class="chart-message">{{ errors[i] }}</div>
      <div v-else-if="empty[i]" class="chart-message">暂无统计数据</div>
      <div :ref="el => elements[i] = el" class="chart" :class="{hidden:errors[i] || empty[i]}" />
    </section>
  </div>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { init, use } from 'echarts/core'
import { LineChart, BarChart, PieChart, HeatmapChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, VisualMapComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import request from '@/utils/request'
use([LineChart,BarChart,PieChart,HeatmapChart,GridComponent,TooltipComponent,LegendComponent,VisualMapComponent,CanvasRenderer])
const titles = ['订单趋势','电影类型','用户交易占比','下单时段']
const endpoints = ['/lineCharts','/barCharts','/pieCharts','/scatterCharts']
const elements = [], charts = [], loading = ref([true,true,true,true]), errors = ref(['','','','']), empty = ref([false,false,false,false])
let observer, disposed = false
const colors = ['#56738b','#93acbe','#b7c9d5','#728e97','#d1dce3','#7d879a']
function optionFor(i, raw) {
  const base = { color:colors, textStyle:{fontFamily:'sans-serif',color:'#77838e'}, tooltip:{trigger:'axis'}, grid:{left:16,right:20,top:42,bottom:60,containLabel:true}, legend:{bottom:0}, animationDuration:350 }
  if (i < 2) return {...base, xAxis:{type:'category',data:raw.map(v => i ? v.type : v.date),axisLine:{lineStyle:{color:'#e5e9ed'}},axisTick:{show:false}},yAxis:[{type:'value',name:'金额 / 元',splitLine:{lineStyle:{color:'#eef1f4'}}},{type:'value',name:'订单 / 笔',minInterval:1,splitLine:{show:false}}],series:[{name:'订单金额',type:i?'bar':'line',smooth:true,barMaxWidth:26,data:raw.map(v=>v.amount)},{name:'订单数量',type:'line',yAxisIndex:1,smooth:true,data:raw.map(v=>v.count)}]}
  if (i === 2) return {...base,tooltip:{trigger:'item',valueFormatter:v=>`${Number(v).toFixed(2)}%`},legend:{type:'scroll',bottom:0},series:[{name:'数量占比',type:'pie',radius:['25%','43%'],center:['50%','46%'],label:{show:false},data:raw.map(v=>({name:v.name,value:v.countRatio*100}))},{name:'金额占比',type:'pie',radius:['52%','70%'],center:['50%','46%'],label:{show:false},data:raw.map(v=>({name:v.name,value:v.amountRatio*100}))}]}
  const dates = [...new Set(raw.map(v=>v.y))].sort()
  return {...base,tooltip:{position:'top',formatter:p=>`${dates[p.value[1]]} ${p.value[0]}时：${p.value[2]} 笔`},grid:{...base.grid,bottom:80},xAxis:{type:'category',data:Array.from({length:24},(_,hour)=>hour+'时'),splitArea:{show:true}},yAxis:{type:'category',data:dates},visualMap:{min:0,max:Math.max(1,...raw.map(v=>Number(v.value)||0)),calculable:true,orient:'horizontal',left:'center',bottom:0,inRange:{color:['#f0f4f7','#a9bfce','#56738b']}},series:[{type:'heatmap',data:raw.map(v=>[Number(v.x),dates.indexOf(v.y),v.value])}]}
}
async function loadChart(i) {
  loading.value[i] = true; errors.value[i] = ''
  try {
    const response = await request.get(endpoints[i])
    if (disposed) return
    if (response.code !== '200') throw new Error(response.msg || '统计加载失败')
    const rows = i < 2 ? response.data : response.data?.data
    if (!Array.isArray(rows)) throw new Error('统计数据格式异常')
    empty.value[i] = rows.length === 0
    if (!empty.value[i]) { charts[i].setOption(optionFor(i,rows),true); charts[i].resize() }
  } catch(e) { if (!disposed) errors.value[i] = e.message || '加载失败，请重试' }
  finally { if (!disposed) loading.value[i] = false }
}
onMounted(() => {
  elements.forEach((el,i) => { charts[i] = init(el); loadChart(i) })
  observer = new ResizeObserver(() => charts.forEach(chart => chart.resize()))
  elements.forEach(el => observer.observe(el))
})
onBeforeUnmount(() => { disposed = true; observer?.disconnect(); charts.forEach(chart=>chart.dispose()) })
</script>
<style scoped>
.charts-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:22px}.chart-card{padding:24px;min-width:0;position:relative}.chart-card header{display:flex;align-items:center;justify-content:space-between;height:28px;margin-bottom:18px}.chart-card h2{font-size:16px;font-weight:500;margin:0}.chart{width:100%;height:330px}.chart.hidden{visibility:hidden}.chart-message{position:absolute;inset:90px 20px 20px;display:grid;place-items:center;color:var(--muted);font-size:13px}@media(max-width:1100px){.charts-grid{grid-template-columns:1fr}}@media(max-width:600px){.chart-card{padding:18px 12px}.chart{height:300px}}
</style>

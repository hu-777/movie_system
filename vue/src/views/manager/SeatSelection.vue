<template>
  <div class="seat-page" v-loading="loading">
    <header class="page-heading"><h1>{{ screening.goodsName || '选择座位' }}</h1><router-link to="/buy" class="text-link">返回选片</router-link></header>
    <p class="seat-meta" v-if="screening.id">{{ screening.hallName }} · {{ screening.startTime }} · ¥{{ screening.price }}/张</p>
    <div v-if="error" class="surface error-state"><p>{{ error }}</p><el-button @click="load">重试</el-button></div>
    <template v-else-if="screening.id">
      <section class="surface auditorium">
        <div class="screen">银幕</div>
        <div class="seat-scroll"><div class="seat-grid">
          <div v-for="row in rows" :key="row" class="seat-row"><span class="row-label">{{ row }}</span>
            <button v-for="col in cols" :key="col" class="seat" :class="seatClass(row,col)" :disabled="!available(row,col) || tooLate || submitting" :aria-label="`${row}${col} ${seatClass(row,col)}`" :aria-pressed="selected.includes(`${row}${col}`)" @click="toggle(`${row}${col}`)">{{ col }}</button>
          </div>
        </div></div>
        <div class="legend"><span>□ 可选</span><span>■ 已选</span><span class="muted">■ 已售 / 不可用</span></div>
      </section>
      <section class="surface checkout"><div><p class="muted">{{ tooLate ? '本场已停止售票' : selected.join('、') || '请选择座位' }}</p><strong>¥{{ total }}</strong></div><el-button type="primary" :loading="submitting" :disabled="!selected.length || tooLate || loading" @click="submit">确认订单</el-button></section>
    </template>
  </div>
</template>
<script setup>
import { getUser } from '@/stores/auth'
import { ref, computed, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
const route = useRoute(), router = useRouter()
const screening = ref({}), seats = ref([]), selected = ref([]), loading = ref(false), submitting = ref(false), error = ref(''), now = ref(Date.now())
let revision = 0
const timer = setInterval(() => now.value = Date.now(), 1000)
onUnmounted(() => { clearInterval(timer); revision++ })
const rows = computed(() => [...new Set(seats.value.map(s => s.seatRow))].sort())
const cols = computed(() => [...new Set(seats.value.map(s => s.seatColumn))].sort((a,b) => a-b))
const seatMap = computed(() => new Map(seats.value.map(s => [s.seatName, s])))
const tooLate = computed(() => !screening.value.startTime || dayjs(screening.value.startTime).valueOf() - now.value <= 300000)
const total = computed(() => selected.value.length * Number(screening.value.price || 0))
const available = (row,col) => seatMap.value.get(`${row}${col}`)?.status === 'AVAILABLE'
const seatClass = (row,col) => !seatMap.value.has(`${row}${col}`) ? 'missing' : selected.value.includes(`${row}${col}`) ? 'selected' : available(row,col) ? 'available' : 'sold'
const toggle = name => { selected.value = selected.value.includes(name) ? selected.value.filter(s => s !== name) : [...selected.value,name] }
async function load() {
  const current = ++revision, id = Number(route.params.id)
  loading.value = true; error.value = ''; selected.value = []; seats.value = []; screening.value = {}
  try {
    if (!Number.isInteger(id) || id <= 0) throw new Error('无效场次')
    const [show, result] = await Promise.all([request.get('/screening/getById', {params:{id}}), request.get('/seat/selectByScreeningId', {params:{screeningId:id}})])
    if (current !== revision) return
    if (show.code !== '200' || !show.data) throw new Error(show.msg || '场次不存在')
    if (result.code !== '200' || !Array.isArray(result.data) || !result.data.length) throw new Error(result.msg || '本场暂无座位')
    screening.value = show.data; seats.value = result.data
  } catch(e) { if (current === revision) error.value = e.message || '加载失败，请重试' }
  finally { if (current === revision) loading.value = false }
}
async function submit() {
  if (submitting.value || !selected.value.length || tooLate.value) return
  const user = getUser()
  if (!user.id) { router.push('/login'); return }
  submitting.value = true
  try {
    const res = await request.post('/orders/addWithSeats', {goodsId:screening.value.goodsId, screeningId:screening.value.id, seatInfo:selected.value.join(',')})
    if (res.code !== '200') throw new Error(res.msg || '订单创建失败')
    ElMessage.success('订单已创建，请完成支付'); await router.push('/orders')
  } catch(e) { ElMessage.error(e.message || '提交失败，请检查订单后重试'); await load() }
  finally { submitting.value = false }
}
watch(() => route.params.id, load, {immediate:true})
</script>
<style scoped>
.seat-page{max-width:1050px;margin:auto}.seat-meta,.muted{color:var(--muted);font-size:13px}.auditorium{padding:32px 16px;margin:24px 0}.screen{max-width:420px;margin:0 auto 36px;border-top:4px solid #d6e0e7;border-radius:50%;text-align:center;padding:14px;color:var(--muted);font-size:12px}.seat-scroll{overflow-x:auto;padding-bottom:12px}.seat-grid{width:max-content;margin:auto}.seat-row{display:flex;gap:8px;margin-bottom:9px;align-items:center}.row-label{width:20px;color:var(--muted);font-size:12px}.seat{width:32px;height:30px;border:1px solid #bacbd7;border-radius:6px;background:#f7fafc;color:#56738b;cursor:pointer}.seat.selected{background:#56738b;color:white}.seat.sold{background:#e8ecf0;color:#a4adb5;border-color:#e8ecf0}.seat.missing{visibility:hidden}.seat:disabled{cursor:default}.legend{display:flex;justify-content:center;gap:22px;font-size:12px;color:#56738b;margin-top:22px}.checkout{display:flex;align-items:center;justify-content:space-between;padding:24px;gap:20px}.checkout p{margin:0 0 8px}.checkout strong{font-size:26px;font-weight:500}
</style>

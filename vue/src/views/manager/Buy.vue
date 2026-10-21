<template>
  <div class="cinema-page movies-page">
    <header class="page-heading"><div><h1>电影片单</h1></div></header>
    <form class="catalog-toolbar surface" @submit.prevent="search"><div class="catalog-search"><el-input v-model="keyword" placeholder="搜索电影名称" aria-label="搜索电影名称" :prefix-icon="Search" clearable maxlength="50" @clear="search" /><el-button type="primary" native-type="submit">搜索</el-button><el-button @click="reset">重置</el-button></div><span class="results-count">{{ error ? '暂时无法获取片单' : loading ? '正在加载片单…' : `共 ${total} 部电影` }}</span></form>
    <div class="catalog-section-label"><span>{{ appliedKeyword ? `搜索结果 / ${appliedKeyword}` : '全部电影' }}</span></div>
    <el-skeleton v-if="loading" :rows="8" animated class="surface catalog-skeleton" />
    <div v-else-if="error" class="surface error-state"><p>电影片单加载失败，请稍后重试。</p><el-button @click="load">重新加载</el-button></div>
    <section v-else-if="movies.length" class="movie-grid" aria-label="电影列表"><article v-for="movie in movies" :key="movie.id" class="catalog-card surface"><div class="poster-wrap"><MoviePoster :src="movie.img" :name="movie.name" /><span class="poster-type">{{ movie.type || '电影' }}</span></div><div class="catalog-card-body"><div class="movie-title-row"><h2>{{ movie.name }}</h2></div><p class="movie-description" :title="movie.comment">{{ movie.comment || '故事即将开启，期待与你相遇。' }}</p><div class="movie-meta"><span><el-icon><Location /></el-icon>{{ movie.position || '影厅待定' }}</span><span>{{ movie.tickets ?? 0 }} 张余票</span></div><div class="movie-price-row"><span class="movie-price"><small>¥</small>{{ movie.price ?? '—' }}<em> / 张</em></span></div><el-button class="screenings-button" @click="showScreenings(movie)">查看场次<el-icon><Right /></el-icon></el-button></div></article></section>
    <div v-else class="surface"><el-empty :description="appliedKeyword ? '没有找到这部电影，试试其他名称' : '片单正在准备中，敬请期待'" :image-size="90"><el-button v-if="appliedKeyword" @click="reset">查看全部电影</el-button></el-empty></div>
    <footer v-if="!loading && !error && total" class="catalog-pagination"><span>第 {{ pageNum }} 页</span><el-pagination v-model:current-page="pageNum" background layout="prev, pager, next" :page-size="pageSize" :total="total" @current-change="load" /></footer>
    <el-dialog v-model="dialogVisible" title="选择观影场次" width="780px" :close-on-click-modal="false" class="screening-dialog">
      <template v-if="currentMovie"><div class="dialog-movie"><MoviePoster :src="currentMovie.img" :name="currentMovie.name" /><div><h2>{{ currentMovie.name }}</h2><p>{{ currentMovie.type }}<template v-if="currentMovie.position"> · {{ currentMovie.position }}</template></p></div></div>
      <el-skeleton v-if="screeningsLoading" :rows="4" animated />
      <div v-else-if="screeningsError" class="error-state"><p>场次加载失败</p><el-button @click="showScreenings(currentMovie)">重新加载</el-button></div>
      <el-table v-else :data="screenings" empty-text="暂无场次安排，请稍后再来" class="screenings-table"><el-table-column label="放映时间" min-width="152"><template #default="{ row }"><strong class="session-time">{{ formatTime(row.startTime, 'HH:mm') }}</strong><span class="session-date">{{ formatTime(row.startTime, 'MM月DD日') }}</span></template></el-table-column><el-table-column prop="hallName" label="影厅" min-width="92" /><el-table-column prop="screenType" label="版本" width="70" /><el-table-column label="余座" width="65"><template #default="{ row }">{{ row.availableSeats ?? '—' }}</template></el-table-column><el-table-column label="票价" width="80"><template #default="{ row }">¥{{ row.price }}</template></el-table-column><el-table-column label="操作" width="112" fixed="right"><template #default="{ row }"><el-button v-if="!isAdmin" type="primary" size="small" :disabled="!canBuyTicket(row)" @click="selectScreening(row)">{{ screeningStatus(row) }}</el-button><el-button v-else size="small" @click="router.push('/goodsStock')">管理场次</el-button></template></el-table-column></el-table>
      <p class="screening-note"><el-icon><InfoFilled /></el-icon>开场前 5 分钟停止购票，具体座位以选座页为准。</p></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Location, Right, InfoFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import MoviePoster from '@/components/MoviePoster.vue'
const route = useRoute()
const router = useRouter()
const isAdmin = JSON.parse(localStorage.getItem('system-user') || '{}').role === 'ADMIN'
const keyword = ref(typeof route.query.name === 'string' ? route.query.name : '')
const appliedKeyword = ref(keyword.value)
const movies = ref([])
const pageNum = ref(1)
const pageSize = 8
const total = ref(0)
const loading = ref(true)
const error = ref(false)
const dialogVisible = ref(false)
const currentMovie = ref(null)
const screenings = ref([])
const screeningsLoading = ref(false)
const screeningsError = ref(false)
let movieRequest = 0
let screeningRequest = 0
const load = async () => {
  const sequence = ++movieRequest
  loading.value = true; error.value = false
  try {
    const res = await request.get('/goodsStock/selectPage', { params: { pageNum: pageNum.value, pageSize, name: appliedKeyword.value } })
    if (sequence !== movieRequest) return
    if (res.code !== '200') throw new Error()
    movies.value = res.data?.list || []; total.value = res.data?.total || 0
  } catch { if (sequence === movieRequest) error.value = true }
  finally { if (sequence === movieRequest) loading.value = false }
}
const search = () => { appliedKeyword.value = keyword.value.trim(); pageNum.value = 1; load() }
const reset = () => { keyword.value = ''; search() }
watch(() => route.query.name, name => { keyword.value = typeof name === 'string' ? name : ''; search() })
const formatTime = (time, format) => time && dayjs(time).isValid() ? dayjs(time).format(format) : '待定'
const canBuyTicket = screening => Number(screening.availableSeats) > 0 && dayjs(screening.startTime).diff(dayjs(), 'minute') >= 5
const screeningStatus = screening => Number(screening.availableSeats) <= 0 ? '已售罄' : canBuyTicket(screening) ? '选座购票' : '停止售票'
const showScreenings = async movie => {
  currentMovie.value = movie; dialogVisible.value = true; screenings.value = []; screeningsLoading.value = true; screeningsError.value = false
  const sequence = ++screeningRequest
  try {
    // Existing screening records reference the upcoming-movie id used by the booking flow.
    const res = await request.get('/screening/getByGoodsId', { params: { goodsId: movie.id } })
    if (sequence !== screeningRequest) return
    if (res.code !== '200') throw new Error()
    screenings.value = Array.isArray(res.data) ? res.data : []
  } catch { if (sequence === screeningRequest) screeningsError.value = true }
  finally { if (sequence === screeningRequest) screeningsLoading.value = false }
}
const selectScreening = screening => {
  if (!canBuyTicket(screening)) return
  sessionStorage.setItem('screeningInfo', JSON.stringify({ goodsId: currentMovie.value.id, screeningId: screening.id, movieName: currentMovie.value.name, hallName: screening.hallName, startTime: screening.startTime, price: screening.price }))
  dialogVisible.value = false
  router.push(`/seatSelection/${screening.id}`)
}
onMounted(load)
</script>

<style scoped>
.catalog-label { display: flex; gap: 7px; align-items: center; color: #8c9aa5; font-size: 11px; padding-bottom: 6px; }
.catalog-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 17px 20px; border-radius: 9px; }
.catalog-search { display: flex; gap: 9px; flex: 1; max-width: 525px; }
.catalog-search .el-input { flex: 1; min-width: 110px; }.catalog-search :deep(.el-input__wrapper) { height: 36px; box-shadow: 0 0 0 1px #e5e9ed inset; }
.results-count { color: #8c98a2; font-size: 12px; white-space: nowrap; }
.catalog-section-label { display: flex; justify-content: space-between; align-items: center; padding: 25px 0 18px; color: #657480; font-size: 12px; }
.catalog-section-label span:last-child { color: #a2abb2; font-size: 11px; }
.movie-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 22px; }
.catalog-card { border-radius: 10px; overflow: hidden; transition: border-color .18s, transform .18s; }
.catalog-card:hover { border-color: #b7c7d3; transform: translateY(-3px); }
.poster-wrap { position: relative; padding: 10px 10px 0; }
.poster-wrap .movie-poster-frame { border-radius: 6px; }
.poster-type { position: absolute; top: 22px; left: 22px; padding: 4px 9px; background: #ffffffeb; border-radius: 4px; color: #40586a; font-size: 10px; backdrop-filter: blur(6px); }
.catalog-card-body { padding: 18px; }
.movie-title-row h2 { font-size: 16px; font-weight: 600; line-height: 1.5; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.movie-description { height: 36px; margin: 9px 0 18px; font-size: 11px; color: #8c98a2; line-height: 1.7; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }
.movie-meta { display: flex; justify-content: space-between; gap: 5px; color: #8e9ba6; font-size: 10px; padding-bottom: 17px; border-bottom: 1px solid #edf0f3; }
.movie-meta span:first-child { display: flex; align-items: center; gap: 5px; }
.movie-price-row { display: flex; justify-content: space-between; align-items: baseline; gap: 5px; padding: 16px 0; }
.movie-price { font-size: 23px; font-weight: 600; font-variant-numeric: tabular-nums; color: #4c687f; letter-spacing: -.6px; }
.movie-price small { font-size: 13px; margin-right: 3px; font-weight: 400; }.movie-price em { font-size: 10px; color: #9da6ae; font-style: normal; font-weight: 400; letter-spacing: 0; }.price-note { color: #a4adb4; font-size: 9px; }
.screenings-button { width: 100%; color: #5b7488; border-color: #dbe4eb; background: #f6f8fa; font-size: 12px; }
.screenings-button .el-icon { margin-left: 15px; }
.catalog-pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--line); }
.catalog-pagination > span { color: #9aa5ae; font-size: 11px; }
.catalog-skeleton { padding: 30px; }
.dialog-movie { display: flex; gap: 18px; align-items: center; padding-bottom: 24px; }.dialog-movie .movie-poster-frame { width: 60px; border-radius: 5px; flex-shrink: 0; }.dialog-movie h2 { font-size: 19px; margin: 6px 0; font-weight: 600; }.dialog-movie p { color: #8494a0; font-size: 12px; margin: 0; }.dialog-movie .eyebrow { font-size: 9px; }
.session-time { display: inline-block; font-size: 17px; font-weight: 500; margin-right: 10px; color: #425c72; }.session-date { font-size: 11px; color: #8e9ca6; }.screening-note { display: flex; gap: 7px; align-items: center; color: #96a2ac; font-size: 11px; margin: 22px 0 0; line-height: 1.7; }
@media (max-width: 1400px) { .movie-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 1000px) { .movie-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }.catalog-toolbar { flex-wrap: wrap; }.catalog-search { max-width: none; }.results-count { width: 100%; } }
@media (max-width: 480px) { .movie-grid { grid-template-columns: 1fr; gap: 22px; }.poster-wrap .movie-poster-frame { aspect-ratio: 3 / 3.8; }.catalog-card-body { padding: 20px; }.catalog-toolbar { padding: 14px; }.catalog-search { flex-wrap: wrap; }.catalog-search .el-input { flex-basis: 100%; }.catalog-section-label span:last-child { display: none; }.catalog-pagination { gap: 14px; align-items: flex-start; flex-direction: column; } }
</style>

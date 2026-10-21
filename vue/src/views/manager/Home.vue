<template>
  <div class="cinema-page home-page">
    <header class="page-heading"><div><h1>{{ isAdmin ? '影院概览' : '影院首页' }}</h1></div><span class="today-label"><el-icon><Calendar /></el-icon>{{ today }}</span></header>
    <section class="home-hero" aria-label="影院推荐">
      <div class="hero-copy"><span class="hero-kicker"><i></i>本期片单</span><h2>好故事，<br>值得一场相遇。</h2><router-link to="/buy" class="hero-link">探索电影片单 <el-icon><Right /></el-icon></router-link></div>
      <div v-if="featured" class="hero-film"><MoviePoster :src="featured.img" :name="featured.name" class="hero-poster" /><div class="hero-film-caption"><strong>{{ featured.name }}</strong><small>{{ featured.type }}<template v-if="featured.position"> · {{ featured.position }}</template></small></div></div>
      <div v-else class="hero-decoration" aria-hidden="true"><img src="/huye-mark.svg" alt="" /></div>
    </section>
    <div class="home-content">
      <section class="home-films" aria-label="影院片单">
        <div class="section-heading"><h2>影院片单 <small v-if="!filmsLoading && !filmsError">/ {{ films.length }} 部</small></h2><router-link to="/buy" class="text-link">全部电影 <el-icon><ArrowRight /></el-icon></router-link></div>
        <el-skeleton v-if="filmsLoading" :rows="6" animated class="surface home-skeleton" />
        <div v-else-if="filmsError" class="surface error-state"><p>片单暂时加载失败</p><el-button @click="loadFilms">重新加载</el-button></div>
        <div v-else-if="films.length" class="home-film-grid"><router-link v-for="movie in films.slice(0, 3)" :key="movie.id" :to="{ path: '/buy', query: { name: movie.name } }" class="home-film-card"><MoviePoster :src="movie.img" :name="movie.name" /><div class="home-film-details"><span class="film-category">{{ movie.type || '电影' }}</span><h3>{{ movie.name }}</h3><span class="film-card-bottom"><span>查看场次</span><el-icon><TopRight /></el-icon></span></div></router-link></div>
        <el-empty v-else class="surface" description="片单正在准备中，敬请期待" :image-size="70" />
        <div class="home-shortcuts"><router-link :to="isAdmin ? '/goodsStock' : '/orders'"><el-icon :size="19"><Tickets /></el-icon><div><strong>{{ isAdmin ? '排片与上架' : '我的电影票' }}</strong></div><el-icon><ArrowRight /></el-icon></router-link><router-link :to="isAdmin ? '/charts' : '/person'"><el-icon :size="19"><DataAnalysis v-if="isAdmin" /><User v-else /></el-icon><div><strong>{{ isAdmin ? '经营数据' : '个人资料' }}</strong></div><el-icon><ArrowRight /></el-icon></router-link></div>
      </section>
      <aside class="notice-panel surface"><div class="section-heading"><h2>影院公告</h2><el-icon :size="17"><Bell /></el-icon></div><el-skeleton v-if="noticesLoading" :rows="4" animated /><div v-else-if="noticesError" class="error-state"><p>公告暂时无法加载</p><el-button size="small" @click="loadNotices">重试</el-button></div><template v-else-if="notices.length"><article v-for="notice in notices.slice(0, 5)" :key="notice.id" class="notice-item"><time>{{ notice.time }}</time><h3>{{ notice.title }}</h3><p>{{ notice.content }}</p></article></template><el-empty v-else description="暂无新公告" :image-size="55" /></aside>
    </div>

  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { Calendar, Right, ArrowRight, TopRight, Tickets, DataAnalysis, User, Bell } from '@element-plus/icons-vue'
import MoviePoster from '@/components/MoviePoster.vue'
import request from '@/utils/request'
const user = JSON.parse(localStorage.getItem('system-user') || '{}')
const isAdmin = user.role === 'ADMIN'
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const films = ref([])
const notices = ref([])
const filmsLoading = ref(true)
const noticesLoading = ref(true)
const filmsError = ref(false)
const noticesError = ref(false)
const featured = computed(() => films.value[0])
const loadFilms = async () => {
  filmsLoading.value = true; filmsError.value = false
  try { const res = await request.get('/goodsStock/selectAll'); if (res.code !== '200') throw new Error(); films.value = Array.isArray(res.data) ? res.data : [] }
  catch { filmsError.value = true }
  finally { filmsLoading.value = false }
}
const loadNotices = async () => {
  noticesLoading.value = true; noticesError.value = false
  try { const res = await request.get('/notice/selectAll'); if (res.code !== '200') throw new Error(); notices.value = Array.isArray(res.data) ? res.data : [] }
  catch { noticesError.value = true }
  finally { noticesLoading.value = false }
}
onMounted(() => { loadFilms(); loadNotices() })
</script>

<style scoped>
.today-label { display: flex; align-items: center; gap: 8px; color: #8a959f; font-size: 11px; padding-bottom: 5px; white-space: nowrap; }
.home-hero { display: flex; justify-content: space-between; gap: 30px; min-height: 274px; background: #eaf0f4; border: 1px solid #dfe7ed; border-radius: 13px; padding: 30px 38px; margin-bottom: 34px; overflow: hidden; }
.hero-copy { display: flex; flex-direction: column; align-items: flex-start; justify-content: center; position: relative; z-index: 1; }
.hero-kicker { display: flex; align-items: center; gap: 7px; color: #6d879b; font-size: 9px; letter-spacing: 1.3px; }
.hero-kicker i { background: #7798ad; height: 5px; width: 5px; border-radius: 50%; }
.hero-copy h2 { margin: 19px 0 24px; font-size: 33px; line-height: 1.4; font-weight: 500; letter-spacing: -1px; color: #344f65; }
.hero-copy p { margin: 0 0 23px; color: #7a8d9c; font-size: 12px; }
.hero-link { display: inline-flex; align-items: center; gap: 21px; font-size: 12px; font-weight: 600; border-bottom: 1px solid #9aafbf; padding-bottom: 6px; }
.hero-film { display: flex; gap: 24px; align-items: center; padding-right: 15px; max-width: 50%; }
.hero-poster { width: 139px; flex-shrink: 0; border-radius: 6px; box-shadow: 0 12px 28px #314c6429; transform: rotate(5deg); }
.hero-film-caption { display: flex; flex-direction: column; gap: 9px; max-width: 150px; }
.hero-film-caption span { color: #8a9ca9; font-size: 10px; letter-spacing: 1px; }
.hero-film-caption strong { font-size: 16px; font-weight: 500; line-height: 1.7; color: #435e73; }
.hero-film-caption small { font-size: 11px; color: #7c909f; }
.hero-decoration { width: 180px; display: grid; place-items: center; opacity: .18; }.hero-decoration img { width: 110px; }
.home-content { display: grid; grid-template-columns: minmax(0, 1fr) 268px; align-items: start; gap: 28px; }
.home-films .section-heading h2 small { font-size: 12px; font-weight: 400; margin-left: 7px; }
.home-film-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.home-film-card { background: #fff; border: 1px solid var(--line); border-radius: 9px; overflow: hidden; color: var(--ink); transition: border-color .18s, transform .18s; }
.home-film-card:hover { border-color: #b3c3d0; transform: translateY(-3px); }
.home-film-card .movie-poster-frame { aspect-ratio: 3 / 3.7; }
.home-film-details { padding: 15px; }
.film-category { font-size: 10px; color: #8195a4; }
.home-film-details h3 { font-size: 13px; margin: 5px 0 7px; line-height: 1.6; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.home-film-details p { font-size: 11px; color: #929ba4; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.film-card-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 18px; color: #687f91; font-size: 11px; }
.home-skeleton { padding: 25px; }
.home-shortcuts { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 20px; }
.home-shortcuts a { padding: 18px 15px; display: flex; align-items: center; gap: 12px; border: 1px solid var(--line); border-radius: 9px; color: #748b9d; }
.home-shortcuts a:hover { background: #edf2f6; }
.home-shortcuts a > div { flex: 1; min-width: 0; }
.home-shortcuts strong { display: block; font-size: 12px; font-weight: 500; color: #4c5e6b; }
.home-shortcuts span { display: block; color: #9aa3ac; font-size: 10px; margin-top: 6px; line-height: 1.7; }
.notice-panel { padding: 23px; }
.notice-panel .section-heading { margin-bottom: 20px; }
.notice-panel .section-heading .el-icon { color: #9caab4; }
.notice-panel h2 { font-size: 15px; }
.notice-intro { margin: 0 0 25px; color: #99a3ab; font-size: 11px; }
.notice-item { border-top: 1px solid var(--line); padding: 20px 0; }
.notice-item time { color: #91a0ab; font-size: 10px; font-variant-numeric: tabular-nums; }
.notice-item h3 { margin: 9px 0 8px; font-size: 13px; font-weight: 500; }
.notice-item p { margin: 0; color: #89959f; font-size: 12px; line-height: 1.9; overflow-wrap: anywhere; }
.notice-signoff { padding-top: 27px; border-top: 1px solid var(--line); margin-top: 6px; }
.notice-signoff span { color: #9eaeb9; font-size: 9px; letter-spacing: 2px; }
.notice-signoff p { color: #9ba7af; font-size: 11px; margin: 7px 0 0; }
.page-footer { margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--line); display: flex; justify-content: space-between; color: #a4adb4; font-size: 10px; }
@media (max-width: 1200px) { .home-content { grid-template-columns: 1fr; } .hero-film-caption { display: none; } }
@media (max-width: 600px) {
  .home-hero { padding: 25px; min-height: 245px; gap: 18px; } .hero-copy h2 { font-size: 27px; } .hero-copy p { max-width: 160px; font-size: 11px; line-height: 1.8; }
  .hero-film { padding-right: 0; max-width: 30%; } .hero-poster { width: 90px; } .hero-kicker { font-size: 8px; letter-spacing: .6px; }
  .home-film-grid { gap: 12px; grid-template-columns: repeat(2, minmax(0, 1fr)); } .home-film-details { padding: 12px; }
  .home-shortcuts { grid-template-columns: 1fr; gap: 10px; }
}
</style>

<template>
  <div class="cinema-app" :class="{ 'sidebar-collapsed': collapsed }">
    <button v-if="mobileOpen" class="nav-backdrop" aria-label="关闭导航" @click="mobileOpen = false"></button>
    <aside class="cinema-sidebar" :class="{ 'mobile-open': mobileOpen }" aria-label="主导航">
      <router-link to="/home" class="sidebar-brand" aria-label="湖叶影院首页"><CinemaBrand :compact="collapsed && !mobileOpen" /></router-link>
      <div class="workspace-badge"><span class="status-dot"></span><span>{{ isAdmin ? '影院管理工作台' : '我的观影空间' }}</span></div>
      <nav class="sidebar-nav">
        <div v-for="group in navigation" :key="group.label" class="nav-group">
          <p class="nav-label">{{ group.label }}</p>
          <router-link v-for="item in group.items" :key="item.path" :to="item.path" :title="item.label" class="nav-item" :class="{ active: route.path === item.path }" :aria-current="route.path === item.path ? 'page' : undefined" @click="mobileOpen = false">
            <el-icon :size="18"><component :is="item.icon" /></el-icon><span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>
      <div class="sidebar-bottom"><button class="nav-item logout-button" @click="logout"><el-icon :size="18"><SwitchButton /></el-icon><span>退出登录</span></button></div>
    </aside>
    <div class="cinema-workspace">
      <header class="workspace-header">
        <div class="header-left"><button class="icon-button" :aria-label="collapsed ? '展开导航' : '折叠导航'" @click="toggleNavigation"><el-icon :size="19"><Fold v-if="!collapsed" /><Expand v-else /></el-icon></button><span class="breadcrumb-root">湖叶影院</span><span class="breadcrumb-divider">/</span><span>{{ pageTitle }}</span></div>
        <router-link class="profile-link" to="/person"><img v-if="user.avatar" :src="user.avatar" alt="我的头像" @error="user.avatar = ''" /><span v-else class="profile-avatar">{{ (user.name || user.username || '湖').slice(0, 1) }}</span><span class="profile-name">{{ user.name || user.username }}</span></router-link>
      </header>
      <main class="workspace-main"><router-view @updateUser="updateUser" /></main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Film, Tickets, DataAnalysis, Collection, VideoCamera, Bell, User, Setting, Lock, SwitchButton, Fold, Expand } from '@element-plus/icons-vue'
import CinemaBrand from '@/components/CinemaBrand.vue'
const router = useRouter()
const route = useRoute()
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))
const collapsed = ref(false)
const mobileOpen = ref(false)
const isAdmin = computed(() => user.value.role === 'ADMIN')
if (!user.value.id) router.replace('/login')
const navigation = computed(() => [
  { label: '影院', items: [
    { path: '/home', label: '影院首页', icon: House },
    { path: '/buy', label: '电影片单', icon: Film },
    { path: '/orders', label: isAdmin.value ? '订单管理' : '我的订单', icon: Tickets },
  ] },
  ...(isAdmin.value ? [{ label: '运营管理', items: [
    { path: '/charts', label: '数据概览', icon: DataAnalysis },
    { path: '/goods', label: '电影资料', icon: Collection },
    { path: '/goodsStock', label: '排片与上架', icon: VideoCamera },
    { path: '/category', label: '电影分类', icon: Setting },
    { path: '/notice', label: '影院公告', icon: Bell },
    { path: '/user', label: '用户管理', icon: User },
    { path: '/admin', label: '管理员', icon: Setting },
  ] }] : []),
  { label: '账户', items: [{ path: '/person', label: '个人资料', icon: User }, { path: '/password', label: '账户安全', icon: Lock }] },
])
const pageTitle = computed(() => navigation.value.flatMap(group => group.items).find(item => item.path === route.path)?.label || (route.path.startsWith('/seatSelection') ? '选择座位' : '影院'))
watch(pageTitle, title => { document.title = `${title} · 湖叶影院` }, { immediate: true })
watch(() => route.path, () => { mobileOpen.value = false })
const toggleNavigation = () => { if (window.innerWidth <= 760) mobileOpen.value = !mobileOpen.value; else collapsed.value = !collapsed.value }
const updateUser = () => { user.value = JSON.parse(localStorage.getItem('system-user') || '{}') }
const logout = () => { localStorage.removeItem('system-user'); router.push('/login') }
</script>

<style scoped>
.cinema-app { min-height: 100vh; }
.cinema-sidebar { width: 224px; position: fixed; inset: 0 auto 0 0; z-index: 50; background: #fff; border-right: 1px solid var(--line); display: flex; flex-direction: column; transition: width .2s; }
.sidebar-brand { padding: 28px 24px 22px; display: flex; }
.workspace-badge { margin: 0 18px 22px; padding: 10px 13px; border: 1px solid var(--line); border-radius: 7px; display: flex; align-items: center; gap: 8px; font-size: 11px; color: #6a7885; white-space: nowrap; }
.status-dot { width: 6px; height: 6px; background: #7897a4; border-radius: 50%; flex: 0 0 auto; }
.sidebar-nav { flex: 1; overflow-y: auto; padding: 0 12px; scrollbar-width: thin; }
.nav-group { margin-bottom: 22px; }
.nav-label { margin: 0 14px 8px; font-size: 10px; letter-spacing: 1.5px; color: #8c969f; }
.nav-item { display: flex; width: 100%; align-items: center; gap: 12px; padding: 11px 14px; margin-bottom: 3px; color: #65727f; border-radius: 7px; white-space: nowrap; font-size: 13px; border: 0; background: none; text-align: left; cursor: pointer; }
.nav-item:hover { color: var(--ink); background: #f5f7f9; }
.nav-item.active { color: #45647e; background: #ecf1f5; font-weight: 600; }
.sidebar-bottom { padding: 16px 12px; border-top: 1px solid var(--line); }
.sidebar-caption { display: block; margin: 0 14px 14px; color: #98a0a8; font-size: 10px; }
.logout-button { margin-bottom: 0; }
.cinema-workspace { margin-left: 224px; transition: margin-left .2s; }
.workspace-header { height: 72px; padding: 0 36px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line); background: rgba(255,255,255,.96); }
.header-left, .profile-link { display: flex; align-items: center; gap: 16px; font-size: 12px; color: #53616e; }
.icon-button { display: inline-flex; align-items: center; justify-content: center; height: 32px; width: 32px; border: 1px solid var(--line); border-radius: 6px; color: #6b7985; background: #fff; cursor: pointer; }
.breadcrumb-root, .breadcrumb-divider { color: #96a0a9; }
.profile-link { gap: 10px; }
.profile-link img, .profile-avatar { width: 30px; height: 30px; border-radius: 50%; object-fit: cover; }
.profile-avatar { display: grid; place-items: center; background: #e9eff3; color: #58738a; font-size: 12px; }
.role-label { color: #89949d; margin-right: 6px; font-size: 11px; }
.workspace-main { padding: 36px; }
.sidebar-collapsed .cinema-sidebar { width: 76px; }
.sidebar-collapsed .cinema-workspace { margin-left: 76px; }
.sidebar-collapsed .sidebar-brand { padding: 28px 19px; }
.sidebar-collapsed .workspace-badge, .sidebar-collapsed .nav-label, .sidebar-collapsed .nav-item span:not(.el-icon), .sidebar-collapsed .sidebar-caption { display: none; }
.sidebar-collapsed .nav-item { justify-content: center; padding: 12px; }
.nav-backdrop { display: none; }
@media (max-width: 1000px) { .workspace-main { padding: 28px 24px; } .workspace-header { padding: 0 24px; } }
@media (max-width: 760px) {
  .cinema-sidebar, .sidebar-collapsed .cinema-sidebar { width: 224px; transform: translateX(-100%); transition: transform .2s; }
  .cinema-sidebar.mobile-open { transform: translateX(0); }
  .cinema-workspace, .sidebar-collapsed .cinema-workspace { margin-left: 0; }
  .workspace-main { padding: 25px 18px; } .workspace-header { height: 62px; padding: 0 18px; }
  .nav-backdrop { display: block; position: fixed; inset: 0; background: #1c2c3b55; border: 0; z-index: 49; }
  .breadcrumb-root, .breadcrumb-divider, .role-label { display: none; }
  .mobile-open .nav-label, .mobile-open .nav-item span:not(.el-icon), .mobile-open .sidebar-caption { display: block; }
  .mobile-open .workspace-badge { display: flex; }
  .mobile-open .nav-item { justify-content: flex-start; }
}
</style>

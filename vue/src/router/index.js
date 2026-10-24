import {createRouter, createWebHistory} from 'vue-router'
import { refreshUser } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',
      children: [
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'notice', component: () => import('@/views/manager/Notice.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'goodsStock', component: () => import('@/views/manager/GoodsStock.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'buy', component: () => import('@/views/manager/Buy.vue')},
        { path: 'orders', component: () => import('@/views/manager/Orders.vue')},
        // { path: 'invoice', component: () => import('@/views/manager/Invoice.vue')},
        { path: 'charts', component: () => import('@/views/manager/Charts.vue')},
        // 修改选座页面路由，支持路径参数
        {
          path: 'seatSelection/:id', 
          component: () => import('@/views/manager/SeatSelection.vue')
        }
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue')},
    { path: '/register', component: () => import('@/views/Register.vue')},
    { path: '/forgotPassword', component: () => import('@/views/ForgotPassword.vue')},
  ]
})

const publicPaths = new Set(['/login', '/register', '/forgotPassword', '/home', '/buy'])
const adminPaths = new Set(['/admin', '/user', '/goods', '/goodsStock', '/category', '/notice', '/charts'])
router.beforeEach(async to => {
  let user
  try { user = await refreshUser() }
  catch (error) { ElMessage.error('服务暂时不可用，请稍后重试'); return publicPaths.has(to.path) ? true : '/login' }
  if (!publicPaths.has(to.path) && !user) return { path: '/login', query: { redirect: to.fullPath } }
  if (adminPaths.has(to.path) && user?.role !== 'ADMIN') { ElMessage.warning('无权访问管理页面'); return '/home' }
  if (to.path.startsWith('/seatSelection') && user?.role !== 'USER') return '/buy'
  return true
})
window.addEventListener('auth-expired', () => {
  const route = router.currentRoute.value
  if (!publicPaths.has(route.path)) router.replace({path:'/login',query:{redirect:route.fullPath}})
})
export default router

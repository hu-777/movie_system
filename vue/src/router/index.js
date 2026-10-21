import {createRouter, createWebHistory} from 'vue-router'

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

export default router
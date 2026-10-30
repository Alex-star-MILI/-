import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue')
  },
  {
    path: '/equipment/list',
    name: 'EquipmentList',
    component: () => import('@/views/equipment/list.vue')
  },
  {
    path: '/equipment/repair',
    name: 'EquipmentRepair',
    component: () => import('@/views/equipment/repair.vue')
  },
  {
    path: '/member/list',
    name: 'MemberList',
    component: () => import('@/views/member/list.vue')
  },
  {
    path: '/member/expire',
    name: 'MemberExpire',
    component: () => import('@/views/member/expire.vue')
  },
  {
    path: '/energy/list',
    name: 'EnergyList',
    component: () => import('@/views/energy/list.vue')
  },
  {
    path: '/energy/stats',
    name: 'EnergyStats',
    component: () => import('@/views/energy/stats.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router

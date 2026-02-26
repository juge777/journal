import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    name: 'List',
    component: () => import('@/views/DiaryList.vue'),
    meta: { title: '我的日记' }
  },
  {
    path: '/diary/new',
    name: 'New',
    component: () => import('@/views/DiaryEdit.vue'),
    meta: { title: '写日记' }
  },
  {
    path: '/diary/:id',
    name: 'Detail',
    component: () => import('@/views/DiaryDetail.vue'),
    meta: { title: '日记详情' }
  },
  {
    path: '/diary/:id/edit',
    name: 'Edit',
    component: () => import('@/views/DiaryEdit.vue'),
    meta: { title: '编辑日记' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated
  const isPublicRoute = to.meta.public as boolean

  document.title = (to.meta.title as string) || '我的日记'

  if (!isAuthenticated && !isPublicRoute) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (isAuthenticated && to.name === 'Login') {
    next({ name: 'List' })
  } else {
    next()
  }
})

export default router

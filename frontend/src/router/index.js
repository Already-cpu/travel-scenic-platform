import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/UserLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/user/Home.vue') },
      { path: 'scenic', name: 'scenic-list', component: () => import('../views/user/ScenicList.vue') },
      { path: 'scenic/:id', name: 'scenic-detail', component: () => import('../views/user/ScenicDetail.vue') },
      { path: 'account', name: 'account', meta: { userAuth: true }, component: () => import('../views/user/UserCenter.vue') }
    ]
  },
  { path: '/login', name: 'login', component: () => import('../views/user/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/user/Register.vue') },
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLogin.vue') },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { adminAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'scenic', name: 'admin-scenic', component: () => import('../views/admin/ScenicManage.vue') },
      { path: 'order', name: 'admin-order', component: () => import('../views/admin/OrderManage.vue') },
      { path: 'user', name: 'admin-user', component: () => import('../views/admin/UserManage.vue') },
      { path: 'comment', name: 'admin-comment', component: () => import('../views/admin/CommentManage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (to.meta.adminAuth && !sessionStorage.getItem('admin_token')) return '/admin/login'
  if (to.meta.userAuth && !sessionStorage.getItem('user_token')) return { path: '/', query: { loginPrompt: '1', redirect: to.fullPath } }
  if (to.path === '/admin/login' && sessionStorage.getItem('admin_token')) return '/admin/dashboard'
})

export default router

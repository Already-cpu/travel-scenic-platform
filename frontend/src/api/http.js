import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 12000
})

http.interceptors.request.use((config) => {
  const isAdmin = window.location.pathname.startsWith('/admin')
  const token = sessionStorage.getItem(isAdmin ? 'admin_token' : 'user_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use((response) => {
  const payload = response.data
  if (payload?.code !== 200) {
    ElMessage.error(payload?.msg || '请求失败')
    if (payload?.code === 401) {
      const isAdmin = window.location.pathname.startsWith('/admin')
      sessionStorage.removeItem(isAdmin ? 'admin_token' : 'user_token')
    }
    return Promise.reject(new Error(payload?.msg || '请求失败'))
  }
  return payload.data
}, (error) => {
  const message = error.response?.data?.msg || error.message || '网络请求失败'
  ElMessage.error(message)
  return Promise.reject(error)
})

export default http

export const scenicApi = {
  filters: () => http.get('/scenic/filters'),
  adminFilters: () => http.get('/scenic/admin/filters'),
  list: (params) => http.get('/scenic/list', { params }),
  hot: () => http.get('/scenic/hot'),
  detail: (id) => http.get(`/scenic/${id}`),
  adminList: (params) => http.get('/scenic/admin/list', { params }),
  create: (data) => http.post('/scenic', data),
  update: (data) => http.put('/scenic', data),
  status: (data) => http.put('/scenic/status', data),
  remove: (id) => http.delete(`/scenic/${id}`)
}

export const userApi = {
  register: (data) => http.post('/user/register', data),
  login: (data) => http.post('/user/login', data),
  smsLogin: (data) => http.post('/user/sms-login', data),
  qrLogin: (data) => http.post('/user/qr-login', data),
  info: () => http.get('/user/info'),
  update: (data) => http.put('/user/info', data)
}

export const adminApi = {
  login: (data) => http.post('/admin/login', data),
  info: () => http.get('/admin/info'),
  stats: {
    overview: () => http.get('/stats/overview'),
    scenicViews: () => http.get('/stats/scenic-views'),
    orderTrend: () => http.get('/stats/order-trend'),
    orders: (params) => http.get('/stats/orders', { params }),
    region: () => http.get('/stats/region'),
    hotness: () => http.get('/stats/hotness'),
    rating: () => http.get('/stats/rating')
  },
  users: (params) => http.get('/admin/user/list', { params }),
  userStatus: (data) => http.put('/admin/user/status', data),
  deleteUser: (id) => http.delete(`/admin/user/${id}`),
  orders: (params) => http.get('/order/list', { params }),
  recentOrders: () => http.get('/order/recent'),
  orderStatus: (data) => http.put('/order/status', data),
  comments: (params) => http.get('/comment/list', { params }),
  replyComment: (data) => http.put('/comment/reply', data),
  commentStatus: (data) => http.put('/comment/status', data),
  deleteComment: (id) => http.delete(`/comment/${id}`)
}

export const orderApi = {
  create: (data) => http.post('/order', data),
  mine: () => http.get('/order/my'),
  cancel: (id) => http.put(`/order/cancel/${id}`),
  pay: (id, method = 'wallet') => http.put(`/order/pay/${id}`, null, { params: { method } })
}

export const commentApi = {
  scenic: (id) => http.get(`/comment/scenic/${id}`),
  mine: () => http.get('/comment/my'),
  create: (data) => http.post('/comment', data)
}

export const favoriteApi = {
  add: (id) => http.post(`/favorite/${id}`),
  remove: (id) => http.delete(`/favorite/${id}`),
  mine: () => http.get('/favorite/my'),
  check: (id) => http.get(`/favorite/check/${id}`)
}

export const uploadApi = {
  image: (file) => { const form = new FormData(); form.append('file', file); return http.post('/upload/image', form) }
}

export const assistantApi = {
  chat: (message) => http.post('/assistant/chat', { message })
}

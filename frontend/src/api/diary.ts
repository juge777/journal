import axios from 'axios'
import type { Diary, DiaryRequest, DiaryListResponse } from '@/types/diary'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('journal_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle 401
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('journal_token')
      localStorage.removeItem('journal_user')
      window.location.href = '/login'
    }
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export const diaryApi = {
  // 获取日记列表
  getList: (page: number = 0, size: number = 20): Promise<DiaryListResponse> => {
    return api.get('/diaries', { params: { page, size } })
  },

  // 获取单篇日记
  getById: (id: number): Promise<Diary> => {
    return api.get(`/diaries/${id}`)
  },

  // 创建日记
  create: (data: DiaryRequest): Promise<Diary> => {
    return api.post('/diaries', data)
  },

  // 更新日记
  update: (id: number, data: DiaryRequest): Promise<Diary> => {
    return api.put(`/diaries/${id}`, data)
  },

  // 删除日记
  delete: (id: number): Promise<void> => {
    return api.delete(`/diaries/${id}`)
  },

  // 搜索日记
  search: (keyword: string, page: number = 0, size: number = 20): Promise<DiaryListResponse> => {
    return api.get('/diaries/search', { params: { keyword, page, size } })
  }
}

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Diary, DiaryRequest } from '@/types/diary'
import { diaryApi } from '@/api/diary'

export const useDiaryStore = defineStore('diary', () => {
  const diaries = ref<Diary[]>([])
  const currentDiary = ref<Diary | null>(null)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 获取日记列表
  const fetchDiaries = async (page: number = 0, reset: boolean = false) => {
    loading.value = true
    error.value = null
    try {
      const response = await diaryApi.getList(page, 20)
      if (reset || page === 0) {
        diaries.value = response.content
      } else {
        diaries.value.push(...response.content)
      }
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
      currentPage.value = response.number
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取日记列表失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 获取单篇日记
  const fetchDiaryById = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      currentDiary.value = await diaryApi.getById(id)
      return currentDiary.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取日记失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 创建日记
  const createDiary = async (data: DiaryRequest) => {
    loading.value = true
    error.value = null
    try {
      const newDiary = await diaryApi.create(data)
      diaries.value.unshift(newDiary)
      totalElements.value++
      return newDiary
    } catch (e) {
      error.value = e instanceof Error ? e.message : '创建日记失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 更新日记
  const updateDiary = async (id: number, data: DiaryRequest) => {
    loading.value = true
    error.value = null
    try {
      const updatedDiary = await diaryApi.update(id, data)
      const index = diaries.value.findIndex(d => d.id === id)
      if (index !== -1) {
        diaries.value[index] = updatedDiary
      }
      if (currentDiary.value?.id === id) {
        currentDiary.value = updatedDiary
      }
      return updatedDiary
    } catch (e) {
      error.value = e instanceof Error ? e.message : '更新日记失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 删除日记
  const deleteDiary = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      await diaryApi.delete(id)
      diaries.value = diaries.value.filter(d => d.id !== id)
      if (currentDiary.value?.id === id) {
        currentDiary.value = null
      }
      totalElements.value--
    } catch (e) {
      error.value = e instanceof Error ? e.message : '删除日记失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 搜索日记
  const searchDiaries = async (keyword: string, page: number = 0) => {
    loading.value = true
    error.value = null
    try {
      const response = await diaryApi.search(keyword, page, 20)
      if (page === 0) {
        diaries.value = response.content
      } else {
        diaries.value.push(...response.content)
      }
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
      currentPage.value = response.number
    } catch (e) {
      error.value = e instanceof Error ? e.message : '搜索失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 导出日记
  const exportDiaries = async () => {
    loading.value = true
    error.value = null
    try {
      const axios = (await import('axios')).default
      const response = await axios.get('/api/diaries/export', {
        responseType: 'blob',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      // 从响应头获取文件名
      let filename = `diaries-export-${new Date().toISOString().slice(0, 10)}.json`
      const contentDisposition = response.headers['content-disposition']
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="([^"]+)"/)
        if (match && match[1]) {
          filename = match[1]
        }
      }

      // 创建下载链接
      const blob = new Blob([response.data], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '导出失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  // 清空当前日记
  const clearCurrentDiary = () => {
    currentDiary.value = null
  }

  // 重置状态
  const reset = () => {
    diaries.value = []
    currentDiary.value = null
    totalElements.value = 0
    totalPages.value = 0
    currentPage.value = 0
    loading.value = false
    error.value = null
  }

  return {
    diaries,
    currentDiary,
    totalElements,
    totalPages,
    currentPage,
    loading,
    error,
    fetchDiaries,
    fetchDiaryById,
    createDiary,
    updateDiary,
    deleteDiary,
    searchDiaries,
    exportDiaries,
    clearCurrentDiary,
    reset
  }
})

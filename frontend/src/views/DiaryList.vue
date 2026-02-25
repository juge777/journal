<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDiaryStore } from '@/stores/diary'
import DiaryCard from '@/components/DiaryCard.vue'

const router = useRouter()
const diaryStore = useDiaryStore()

const searchKeyword = ref('')
const isSearching = ref(false)
const showSearch = ref(false)

const hasMore = computed(() => !diaryStore.totalPages || diaryStore.currentPage < diaryStore.totalPages - 1)

// 加载日记列表
const loadDiaries = async (reset = false) => {
  if (diaryStore.loading) return

  const page = reset ? 0 : diaryStore.currentPage + 1
  await diaryStore.fetchDiaries(page, reset)
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    isSearching.value = false
    await loadDiaries(true)
    return
  }

  isSearching.value = true
  await diaryStore.searchDiaries(searchKeyword.value.trim(), 0)
}

// 清除搜索
const clearSearch = async () => {
  searchKeyword.value = ''
  isSearching.value = false
  await loadDiaries(true)
}

// 导出
const handleExport = async () => {
  try {
    await diaryStore.exportDiaries()
  } catch (e) {
    console.error('Export failed:', e)
  }
}

// 新建日记
const goToNew = () => {
  router.push('/diary/new')
}

// 查看详情
const goToDetail = (id: number) => {
  router.push(`/diary/${id}`)
}

// 下拉刷新
const handleRefresh = async () => {
  await loadDiaries(true)
}

// 加载更多
const loadMore = async () => {
  if (hasMore.value && !diaryStore.loading) {
    if (isSearching.value) {
      await diaryStore.searchDiaries(searchKeyword.value.trim(), diaryStore.currentPage + 1)
    } else {
      await loadDiaries(false)
    }
  }
}

onMounted(() => {
  loadDiaries(true)
})
</script>

<template>
  <div class="diary-list">
    <!-- 头部 -->
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="title" v-if="!showSearch">我的日记</h1>
          <div class="search-box" v-else>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索日记..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
        </div>
        <div class="header-right">
          <button class="icon-btn" @click="showSearch = !showSearch" v-if="!isSearching">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
          </button>
          <button class="icon-btn" @click="handleExport" title="导出">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
          </button>
        </div>
      </div>
      <!-- 搜索状态提示 -->
      <div class="search-status" v-if="isSearching">
        <span>搜索结果: {{ searchKeyword }}</span>
        <button class="clear-btn" @click="clearSearch">清除</button>
      </div>
    </header>

    <!-- 日记列表 -->
    <div class="list-container" @scroll="handleScroll">
      <div class="diary-cards" v-if="diaryStore.diaries.length > 0">
        <DiaryCard
          v-for="diary in diaryStore.diaries"
          :key="diary.id"
          :diary="diary"
          @click="goToDetail(diary.id)"
        />
      </div>

      <!-- 空状态 -->
      <div class="empty" v-else-if="!diaryStore.loading">
        <div class="empty-icon">📔</div>
        <p class="empty-text">还没有日记</p>
        <p class="empty-hint">点击右下角按钮开始写第一篇日记吧</p>
      </div>

      <!-- 加载状态 -->
      <div class="loading" v-if="diaryStore.loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 加载更多提示 -->
      <div class="load-more" v-if="hasMore && !diaryStore.loading && diaryStore.diaries.length > 0">
        <p>下拉加载更多</p>
      </div>

      <!-- 没有更多了 -->
      <div class="no-more" v-if="!hasMore && diaryStore.diaries.length > 0">
        <p>没有更多了</p>
      </div>
    </div>

    <!-- 新建按钮 -->
    <button class="fab" @click="goToNew">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="12" y1="5" x2="12" y2="19"/>
        <line x1="5" y1="12" x2="19" y2="12"/>
      </svg>
    </button>

    <!-- 错误提示 -->
    <div class="toast error" v-if="diaryStore.error">
      {{ diaryStore.error }}
    </div>
  </div>
</template>

<style scoped>
.diary-list {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 80px;
}

/* 头部 */
.header {
  background: var(--bg-primary);
  padding: 1rem;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.search-box {
  display: flex;
  gap: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: var(--primary-color);
}

.search-btn {
  padding: 0.5rem 1rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
}

.header-right {
  display: flex;
  gap: 0.5rem;
  margin-left: 1rem;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 0.5rem;
  transition: background 0.2s, color 0.2s;
}

.icon-btn:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.search-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--border-color);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.clear-btn {
  padding: 0.25rem 0.75rem;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 0.375rem;
  font-size: 0.75rem;
  color: var(--text-secondary);
  cursor: pointer;
}

/* 列表容器 */
.list-container {
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  padding: 1rem;
}

.diary-cards {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 4rem 1rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.125rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 0.75rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.load-more, .no-more {
  text-align: center;
  padding: 1.5rem;
  color: var(--text-light);
  font-size: 0.875rem;
}

/* 浮动按钮 */
.fab {
  position: fixed;
  right: 1.5rem;
  bottom: 1.5rem;
  width: 56px;
  height: 56px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 50%;
  box-shadow: var(--shadow-lg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.fab:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(108, 92, 231, 0.4);
}

.fab:active {
  transform: scale(0.95);
}

/* 错误提示 */
.toast {
  position: fixed;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  color: white;
  font-size: 0.875rem;
  z-index: 100;
  max-width: 90%;
}

.toast.error {
  background: var(--danger-color);
}

/* 响应式 */
@media (max-width: 480px) {
  .title {
    font-size: 1.25rem;
  }

  .list-container {
    padding: 0.75rem;
  }

  .fab {
    right: 1rem;
    bottom: 1rem;
    width: 48px;
    height: 48px;
  }
}
</style>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDiaryStore } from '@/stores/diary'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const diaryStore = useDiaryStore()

const diaryId = computed(() => Number(route.params.id))
const showDeleteConfirm = ref(false)
const deleting = ref(false)

// 获取日记
const diary = computed(() => diaryStore.currentDiary)

// 格式化日期
const formattedDate = computed(() => {
  if (!diary.value) return ''
  return dayjs(diary.value.createdAt).format('YYYY年MM月DD日 dddd')
})

const formattedTime = computed(() => {
  if (!diary.value) return ''
  return dayjs(diary.value.createdAt).format('HH:mm')
})

// 心情映射
const moodEmojiMap: Record<string, string> = {
  happy: '😊',
  sad: '😢',
  calm: '😌',
  angry: '😤',
  anxious: '😰',
  excited: '🥳',
  tired: '😴',
  thinking: '🤔'
}

// 天气映射
const weatherEmojiMap: Record<string, string> = {
  sunny: '☀️',
  cloudy: '☁️',
  rainy: '🌧️',
  thunder: '⛈️',
  snowy: '❄️',
  foggy: '🌫️'
}

const moodEmoji = computed(() => {
  if (!diary.value?.mood) return ''
  return moodEmojiMap[diary.value.mood] || diary.value.mood
})

const weatherEmoji = computed(() => {
  if (!diary.value?.weather) return ''
  return weatherEmojiMap[diary.value.weather] || diary.value.weather
})

// 编辑
const edit = () => {
  router.push(`/diary/${diaryId.value}/edit`)
}

// 删除
const confirmDelete = () => {
  showDeleteConfirm.value = true
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
}

const doDelete = async () => {
  deleting.value = true
  try {
    await diaryStore.deleteDiary(diaryId.value)
    router.replace('/')
  } catch (e) {
    console.error('Delete failed:', e)
    deleting.value = false
    showDeleteConfirm.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(async () => {
  try {
    await diaryStore.fetchDiaryById(diaryId.value)
  } catch (e) {
    router.replace('/')
  }
})
</script>

<template>
  <div class="diary-detail" v-if="diary">
    <!-- 头部 -->
    <header class="header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h1 class="title">日记详情</h1>
      <button class="edit-btn" @click="edit">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
      </button>
    </header>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 元信息 -->
      <div class="meta">
        <div class="date-info">
          <div class="date">{{ formattedDate }}</div>
          <div class="time">{{ formattedTime }}</div>
        </div>
        <div class="tags" v-if="moodEmoji || weatherEmoji">
          <span class="tag" v-if="moodEmoji">{{ moodEmoji }}</span>
          <span class="tag" v-if="weatherEmoji">{{ weatherEmoji }}</span>
        </div>
      </div>

      <!-- 标题 -->
      <h2 class="diary-title" v-if="diary.title">{{ diary.title }}</h2>

      <!-- 内容 -->
      <div class="diary-content">{{ diary.content }}</div>

      <!-- 更新时间 -->
      <div class="updated-at" v-if="diary.updatedAt !== diary.createdAt">
        <span>编辑于 {{ dayjs(diary.updatedAt).format('YYYY-MM-DD HH:mm') }}</span>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <button class="action-btn edit" @click="edit">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          编辑
        </button>
        <button class="action-btn delete" @click="confirmDelete">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
          删除
        </button>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-overlay" v-if="showDeleteConfirm" @click="cancelDelete">
      <div class="modal" @click.stop>
        <h3 class="modal-title">确认删除</h3>
        <p class="modal-text">删除后将无法恢复，确定要删除这篇日记吗？</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="cancelDelete" :disabled="deleting">取消</button>
          <button class="modal-btn confirm" @click="doDelete" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 加载状态 -->
  <div class="loading" v-else>
    <div class="spinner"></div>
  </div>
</template>

<style scoped>
.diary-detail {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 头部 */
.header {
  background: var(--bg-primary);
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
}

.title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.edit-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  padding: 0;
}

/* 内容 */
.content {
  padding: 1.5rem 1rem;
  max-width: 800px;
  margin: 0 auto;
}

/* 元信息 */
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.time {
  font-size: 0.875rem;
  color: var(--text-light);
}

.tags {
  display: flex;
  gap: 0.5rem;
}

.tag {
  font-size: 1.5rem;
}

/* 标题 */
.diary-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
  line-height: 1.4;
}

/* 内容 */
.diary-content {
  font-size: 1rem;
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-wrap: break-word;
}

.updated-at {
  margin-top: 2rem;
  font-size: 0.75rem;
  color: var(--text-light);
}

/* 操作按钮 */
.actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.edit {
  background: var(--primary-color);
  color: white;
}

.action-btn.edit:hover {
  opacity: 0.9;
}

.action-btn.delete {
  background: rgba(214, 48, 49, 0.1);
  color: var(--danger-color);
}

.action-btn.delete:hover {
  background: rgba(214, 48, 49, 0.2);
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}

.modal {
  background: var(--bg-primary);
  border-radius: 1rem;
  padding: 1.5rem;
  width: 100%;
  max-width: 320px;
  box-shadow: var(--shadow-lg);
}

.modal-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
}

.modal-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0 0 1.5rem 0;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
}

.modal-btn {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: opacity 0.2s;
}

.modal-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-btn.cancel {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.modal-btn.confirm {
  background: var(--danger-color);
  color: white;
}

/* 加载状态 */
.loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 480px) {
  .content {
    padding: 1rem 0.75rem;
  }

  .diary-title {
    font-size: 1.25rem;
  }

  .diary-content {
    font-size: 0.9375rem;
  }
}
</style>

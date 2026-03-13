<script setup lang="ts">
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { onBeforeRouteLeave } from 'vue-router'
import { useDiaryStore } from '@/stores/diary'
import type { DiaryRequest } from '@/types/diary'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

// ==================== 草稿相关 ====================
const DRAFT_KEY_NEW = 'diary_draft_new'
const DRAFT_KEY_EDIT = (id: number) => `diary_draft_edit_${id}`

interface DraftData {
  title: string
  content: string
  mood: string
  weather: string
  diaryDate: string
  timestamp: number
}

// 获取草稿存储 key
const getDraftKey = () => {
  return isEdit.value && diaryId.value ? DRAFT_KEY_EDIT(diaryId.value) : DRAFT_KEY_NEW
}

// 保存草稿到 localStorage
const saveDraft = () => {
  const draft: DraftData = {
    title: title.value,
    content: content.value,
    mood: mood.value,
    weather: weather.value,
    diaryDate: diaryDate.value,
    timestamp: Date.now()
  }
  localStorage.setItem(getDraftKey(), JSON.stringify(draft))
}

// 加载草稿
const loadDraft = (): DraftData | null => {
  const draftStr = localStorage.getItem(getDraftKey())
  if (!draftStr) return null
  try {
    return JSON.parse(draftStr)
  } catch {
    return null
  }
}

// 清除草稿
const clearDraft = () => {
  localStorage.removeItem(getDraftKey())
}

// 防抖保存草稿
let draftSaveTimer: ReturnType<typeof setTimeout> | null = null
const debouncedSaveDraft = () => {
  if (draftSaveTimer) clearTimeout(draftSaveTimer)
  draftSaveTimer = setTimeout(saveDraft, 1000)
}

// 检查当前内容是否有变化（用于离开确认）
const hasUnsavedChanges = computed(() => {
  if (isEdit.value && diaryId.value) {
    const draft = loadDraft()
    return draft && (draft.title !== title.value ||
                    draft.content !== content.value ||
                    draft.mood !== mood.value ||
                    draft.weather !== weather.value ||
                    draft.diaryDate !== diaryDate.value)
  }
  return !!(title.value || content.value || mood.value || weather.value)
})

// ==================== 组件逻辑 ====================

const router = useRouter()
const route = useRoute()
const diaryStore = useDiaryStore()

const isEdit = computed(() => !!route.params.id)
const diaryId = computed(() => isEdit.value ? Number(route.params.id) : null)

// 草稿恢复弹窗
const showRestoreDialog = ref(false)
const restoreDraftData = ref<DraftData | null>(null)

const title = ref('')
const content = ref('')
const mood = ref('')
const weather = ref('')
const diaryDate = ref(new Date().toISOString().split('T')[0])
const saving = ref(false)

// 心情选项
const moodOptions = [
  { emoji: '😊', label: '开心', value: 'happy' },
  { emoji: '😢', label: '难过', value: 'sad' },
  { emoji: '😌', label: '平静', value: 'calm' },
  { emoji: '😤', label: '生气', value: 'angry' },
  { emoji: '😰', label: '焦虑', value: 'anxious' },
  { emoji: '🥳', label: '兴奋', value: 'excited' },
  { emoji: '😴', label: '疲惫', value: 'tired' },
  { emoji: '🤔', label: '思考', value: 'thinking' }
]

// 天气选项
const weatherOptions = [
  { emoji: '☀️', label: '晴天', value: 'sunny' },
  { emoji: '☁️', label: '多云', value: 'cloudy' },
  { emoji: '🌧️', label: '雨天', value: 'rainy' },
  { emoji: '⛈️', label: '雷阵雨', value: 'thunder' },
  { emoji: '❄️', label: '下雪', value: 'snowy' },
  { emoji: '🌫️', label: '雾天', value: 'foggy' }
]

// 加载日记内容（编辑模式）
const loadDiary = async () => {
  if (diaryId.value) {
    try {
      const diary = await diaryStore.fetchDiaryById(diaryId.value)
      title.value = diary.title || ''
      content.value = diary.content
      mood.value = diary.mood || ''
      weather.value = diary.weather || ''
      diaryDate.value = diary.diaryDate || new Date().toISOString().split('T')[0]
    } catch (e) {
      console.error('Load diary failed:', e)
      router.back()
    }
  }
}

// 恢复草稿
const restoreDraft = () => {
  if (restoreDraftData.value) {
    title.value = restoreDraftData.value.title
    content.value = restoreDraftData.value.content
    mood.value = restoreDraftData.value.mood
    weather.value = restoreDraftData.value.weather
    diaryDate.value = restoreDraftData.value.diaryDate
  }
  showRestoreDialog.value = false
  restoreDraftData.value = null
}

// 丢弃草稿
const discardDraft = () => {
  clearDraft()
  showRestoreDialog.value = false
  restoreDraftData.value = null
}

// 保存
const save = async () => {
  if (!content.value.trim()) {
    alert('请填写日记内容')
    return
  }

  saving.value = true
  try {
    const data: DiaryRequest = {
      title: title.value.trim() || null,
      content: content.value.trim(),
      mood: mood.value || null,
      weather: weather.value || null,
      diaryDate: diaryDate.value
    }

    if (isEdit.value && diaryId.value) {
      await diaryStore.updateDiary(diaryId.value, data)
    } else {
      const newDiary = await diaryStore.createDiary(data)
      clearDraft() // 新建成功后清除草稿
      router.replace(`/diary/${newDiary.id}`)
      return
    }

    clearDraft() // 编辑成功后清除草稿
    router.back()
  } catch (e) {
    console.error('Save failed:', e)
  } finally {
    saving.value = false
  }
}

// 取消
const cancel = () => {
  router.back()
}

// 获取当前心情的emoji
const currentMoodEmoji = computed(() => {
  const option = moodOptions.find(m => m.value === mood.value)
  return option?.emoji || ''
})

// 获取当前天气的emoji
const currentWeatherEmoji = computed(() => {
  const option = weatherOptions.find(w => w.value === weather.value)
  return option?.emoji || ''
})

// 格式化预览日期
const previewDate = computed(() => {
  return dayjs(diaryDate.value).format('YYYY年MM月DD日 dddd')
})

// 监听输入变化，自动保存草稿
watch([title, content, mood, weather, diaryDate], () => {
  debouncedSaveDraft()
})

// 离开页面确认
onBeforeRouteLeave((_to, _from, next) => {
  if (hasUnsavedChanges.value) {
    const answer = confirm('有未保存的内容，确定要离开吗？')
    if (answer) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  if (draftSaveTimer) clearTimeout(draftSaveTimer)
})

onMounted(async () => {
  // 先加载日记内容（编辑模式）
  if (isEdit.value) {
    await loadDiary()
  }

  // 检查是否有草稿需要恢复
  const draft = loadDraft()
  if (draft && draft.timestamp) {
    // 如果草稿内容与当前加载的内容不同，则提示恢复
    const hasDifferentContent = draft.title !== title.value ||
                               draft.content !== content.value ||
                               draft.mood !== mood.value ||
                               draft.weather !== weather.value ||
                               draft.diaryDate !== diaryDate.value

    if (hasDifferentContent && (draft.content || draft.title)) {
      restoreDraftData.value = draft
      showRestoreDialog.value = true
    }
  }
})
</script>

<template>
  <div class="diary-edit">
    <!-- 头部 -->
    <header class="header">
      <button class="back-btn" @click="cancel">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h1 class="title">{{ isEdit ? '编辑日记' : '写日记' }}</h1>
      <button class="save-btn" @click="save" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </header>

    <!-- 编辑表单 -->
    <div class="form">
      <!-- 日期选择 -->
      <div class="form-group">
        <label class="label">日记日期</label>
        <input
          v-model="diaryDate"
          type="date"
          class="input date-input"
        />
      </div>

      <!-- 标题 -->
      <div class="form-group">
        <input
          v-model="title"
          type="text"
          placeholder="标题（可选）"
          class="input title-input"
          maxlength="200"
        />
      </div>

      <!-- 内容 -->
      <div class="form-group">
        <textarea
          v-model="content"
          placeholder="今天发生了什么？写下你的心情吧..."
          class="input content-input"
          rows="15"
        ></textarea>
        <div class="char-count">{{ content.length }} 字</div>
      </div>

      <!-- 心情选择 -->
      <div class="form-group">
        <label class="label">心情</label>
        <div class="mood-grid">
          <button
            v-for="option in moodOptions"
            :key="option.value"
            class="mood-btn"
            :class="{ active: mood === option.value }"
            @click="mood = mood === option.value ? '' : option.value"
          >
            <span class="mood-emoji">{{ option.emoji }}</span>
            <span class="mood-label">{{ option.label }}</span>
          </button>
        </div>
      </div>

      <!-- 天气选择 -->
      <div class="form-group">
        <label class="label">天气</label>
        <div class="weather-grid">
          <button
            v-for="option in weatherOptions"
            :key="option.value"
            class="weather-btn"
            :class="{ active: weather === option.value }"
            @click="weather = weather === option.value ? '' : option.value"
          >
            <span class="weather-emoji">{{ option.emoji }}</span>
            <span class="weather-label">{{ option.label }}</span>
          </button>
        </div>
      </div>

      <!-- 预览卡片 -->
      <div class="preview" v-if="content || title">
        <div class="preview-header">预览</div>
        <div class="preview-card">
          <div class="preview-meta">
            <span class="preview-date">{{ previewDate }}</span>
            <span class="preview-tags" v-if="currentMoodEmoji || currentWeatherEmoji">
              {{ currentMoodEmoji }} {{ currentWeatherEmoji }}
            </span>
          </div>
          <h3 class="preview-title" v-if="title">{{ title }}</h3>
          <p class="preview-content">{{ content || '暂无内容' }}</p>
        </div>
      </div>
    </div>

    <!-- 草稿恢复弹窗 -->
    <div class="draft-dialog-overlay" v-if="showRestoreDialog" @click.self="discardDraft">
      <div class="draft-dialog">
        <div class="draft-dialog-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
          <h3>发现未保存的草稿</h3>
        </div>
        <div class="draft-dialog-content">
          <p>检测到您有未保存的日记内容，是否恢复？</p>
          <div class="draft-preview" v-if="restoreDraftData">
            <div class="draft-preview-title">{{ restoreDraftData.title || '无标题' }}</div>
            <div class="draft-preview-text">{{ restoreDraftData.content || '暂无内容' }}</div>
          </div>
        </div>
        <div class="draft-dialog-actions">
          <button class="btn-discard" @click="discardDraft">丢弃</button>
          <button class="btn-restore" @click="restoreDraft">恢复草稿</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.diary-edit {
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

.save-btn {
  padding: 0.5rem 1.25rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 表单 */
.form {
  padding: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.label {
  display: block;
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.75rem;
}

.input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  font-size: 1rem;
  background: var(--bg-primary);
  transition: border-color 0.2s;
}

.input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.title-input {
  font-weight: 600;
}

.content-input {
  resize: vertical;
  min-height: 200px;
  line-height: 1.6;
  font-family: inherit;
}

.char-count {
  text-align: right;
  font-size: 0.75rem;
  color: var(--text-light);
  margin-top: 0.5rem;
}

/* 心情网格 */
.mood-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.mood-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.75rem 0.5rem;
  background: var(--bg-primary);
  border: 2px solid transparent;
  border-radius: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.mood-btn:hover {
  background: var(--bg-secondary);
}

.mood-btn.active {
  border-color: var(--primary-color);
  background: rgba(108, 92, 231, 0.1);
}

.mood-emoji {
  font-size: 1.5rem;
}

.mood-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* 天气网格 */
.weather-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.weather-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.75rem 0.5rem;
  background: var(--bg-primary);
  border: 2px solid transparent;
  border-radius: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.weather-btn:hover {
  background: var(--bg-secondary);
}

.weather-btn.active {
  border-color: var(--primary-color);
  background: rgba(108, 92, 231, 0.1);
}

.weather-emoji {
  font-size: 1.25rem;
}

.weather-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* 预览 */
.preview {
  margin-top: 2rem;
}

.preview-header {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.75rem;
}

.preview-card {
  background: var(--bg-primary);
  border-radius: 0.75rem;
  padding: 1rem;
  box-shadow: var(--shadow-sm);
}

.preview-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.preview-date {
  font-size: 0.875rem;
  color: var(--text-light);
}

.preview-tags {
  font-size: 1rem;
}

.preview-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
}

.preview-content {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

/* 响应式 */
@media (max-width: 480px) {
  .mood-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 0.5rem;
  }

  .weather-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 0.5rem;
  }

  .mood-btn, .weather-btn {
    padding: 0.5rem 0.25rem;
  }
}

/* 草稿恢复弹窗 */
.draft-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.draft-dialog {
  background: var(--bg-primary);
  border-radius: 1rem;
  width: 100%;
  max-width: 400px;
  box-shadow: var(--shadow-lg);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.draft-dialog-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1.25rem 1rem;
}

.draft-dialog-header svg {
  flex-shrink: 0;
  color: var(--primary-color);
}

.draft-dialog-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.draft-dialog-content {
  padding: 0 1.25rem 1rem;
}

.draft-dialog-content p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0 0 1rem 0;
}

.draft-preview {
  background: var(--bg-secondary);
  border-radius: 0.5rem;
  padding: 0.75rem;
}

.draft-preview-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.draft-preview-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.draft-dialog-actions {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem 1.25rem;
}

.btn-discard,
.btn-restore {
  flex: 1;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-discard {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.btn-discard:hover {
  background: var(--bg-tertiary);
}

.btn-restore {
  background: var(--primary-color);
  color: white;
}

.btn-restore:hover {
  opacity: 0.9;
}
</style>

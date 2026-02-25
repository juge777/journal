<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDiaryStore } from '@/stores/diary'
import type { DiaryRequest } from '@/types/diary'

const router = useRouter()
const route = useRoute()
const diaryStore = useDiaryStore()

const isEdit = computed(() => !!route.params.id)
const diaryId = computed(() => isEdit.value ? Number(route.params.id) : null)

const title = ref('')
const content = ref('')
const mood = ref('')
const weather = ref('')
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
    } catch (e) {
      console.error('Load diary failed:', e)
      router.back()
    }
  }
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
      weather: weather.value || null
    }

    if (isEdit.value && diaryId.value) {
      await diaryStore.updateDiary(diaryId.value, data)
    } else {
      const newDiary = await diaryStore.createDiary(data)
      router.replace(`/diary/${newDiary.id}`)
      return
    }

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

onMounted(() => {
  if (isEdit.value) {
    loadDiary()
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
            <span class="preview-date">{{ new Date().toLocaleDateString('zh-CN') }}</span>
            <span class="preview-tags" v-if="currentMoodEmoji || currentWeatherEmoji">
              {{ currentMoodEmoji }} {{ currentWeatherEmoji }}
            </span>
          </div>
          <h3 class="preview-title" v-if="title">{{ title }}</h3>
          <p class="preview-content">{{ content || '暂无内容' }}</p>
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
</style>

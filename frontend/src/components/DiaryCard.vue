<script setup lang="ts">
import type { Diary } from '@/types/diary'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

const props = defineProps<{
  diary: Diary
}>()

const emit = defineEmits<{
  click: []
}>()

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

const moodEmoji = props.diary.mood ? (moodEmojiMap[props.diary.mood] || props.diary.mood) : ''
const weatherEmoji = props.diary.weather ? (weatherEmojiMap[props.diary.weather] || props.diary.weather) : ''

// 格式化日期
const formattedDate = dayjs(props.diary.createdAt).format('MM月DD日 dddd')
const formattedTime = dayjs(props.diary.createdAt).format('HH:mm')

// 内容预览
const contentPreview = props.diary.content.length > 100
  ? props.diary.content.slice(0, 100) + '...'
  : props.diary.content
</script>

<template>
  <div class="diary-card" @click="emit('click')">
    <div class="card-header">
      <div class="date-info">
        <span class="date">{{ formattedDate }}</span>
        <span class="time">{{ formattedTime }}</span>
      </div>
      <div class="tags" v-if="moodEmoji || weatherEmoji">
        <span class="tag" v-if="moodEmoji">{{ moodEmoji }}</span>
        <span class="tag" v-if="weatherEmoji">{{ weatherEmoji }}</span>
      </div>
    </div>

    <h3 class="card-title" v-if="diary.title">{{ diary.title }}</h3>

    <p class="card-content">{{ contentPreview }}</p>

    <div class="card-footer">
      <span class="arrow">→</span>
    </div>
  </div>
</template>

<style scoped>
.diary-card {
  background: var(--bg-card);
  border-radius: 0.75rem;
  padding: 1rem;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  overflow: hidden;
}

.diary-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.diary-card:active {
  transform: translateY(0);
}

/* 头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.date {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
}

.time {
  font-size: 0.75rem;
  color: var(--text-light);
}

.tags {
  display: flex;
  gap: 0.25rem;
}

.tag {
  font-size: 1.125rem;
}

/* 标题 */
.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 内容 */
.card-content {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 0.75rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 底部 */
.card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 0.5rem;
  border-top: 1px solid var(--border-color);
}

.arrow {
  color: var(--primary-light);
  font-size: 0.875rem;
  transition: transform 0.2s;
}

.diary-card:hover .arrow {
  transform: translateX(4px);
}

/* 响应式 */
@media (max-width: 480px) {
  .diary-card {
    padding: 0.875rem;
  }

  .date {
    font-size: 0.8125rem;
  }

  .card-title {
    font-size: 0.9375rem;
  }

  .card-content {
    font-size: 0.8125rem;
  }
}
</style>

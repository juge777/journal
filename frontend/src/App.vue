<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app">
    <RouterView />
  </div>
</template>

<style>
:root {
  --primary-color: #6c5ce7;
  --primary-light: #a29bfe;
  --success-color: #00b894;
  --danger-color: #d63031;
  --text-primary: #2d3436;
  --text-secondary: #636e72;
  --text-light: #b2bec3;
  --bg-primary: #ffffff;
  --bg-secondary: #f5f7fa;
  --bg-card: #ffffff;
  --border-color: #dfe6e9;
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.app {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 移动端适配 */
@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

/* 触摸优化 */
button, a, .clickable {
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

::-webkit-scrollbar-thumb {
  background: var(--text-light);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}
</style>

<script setup lang="ts">
const props = defineProps<{
  title?: string
}>()
</script>

<template>
  <header class="header">
    <div class="header-content">
      <h1 class="title" v-if="title">{{ title }}</h1>
      <slot></slot>
    </div>
  </header>
</template>

<style scoped>
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

.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
</style>

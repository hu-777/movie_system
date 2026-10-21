<template>
  <div class="movie-poster-frame">
    <img v-if="src && !failed" :src="src" :alt="`${name}海报`" loading="lazy" @error="failed = true" />
    <div v-else class="poster-fallback"><el-icon :size="28"><Film /></el-icon><span>{{ name || '海报待更新' }}</span></div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Film } from '@element-plus/icons-vue'
const props = defineProps({ src: String, name: String })
const failed = ref(false)
watch(() => props.src, () => { failed.value = false })
</script>

<style scoped>
.movie-poster-frame { overflow: hidden; background: #e9eef2; aspect-ratio: 2 / 3; }
.movie-poster-frame img { display: block; width: 100%; height: 100%; object-fit: cover; }
.poster-fallback { height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 15px; padding: 20px; color: #738596; text-align: center; font-size: 13px; }
</style>

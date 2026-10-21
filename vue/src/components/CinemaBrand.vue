<template>
  <span class="cinema-brand" :class="{ 'brand-compact': compact }">
    <img src="/huye-mark.svg" width="38" height="38" alt="" />
    <span v-if="!compact" class="brand-wordmark"><strong>湖叶影院</strong><small>HUYE CINEMA</small></span>
  </span>
</template>

<script setup>
defineProps({ compact: Boolean })
</script>

<style scoped>
.cinema-brand { display: inline-flex; align-items: center; gap: 11px; white-space: nowrap; color: var(--ink); }
.brand-wordmark { display: grid; gap: 2px; }
.brand-wordmark strong { font-size: 17px; letter-spacing: 2px; font-weight: 650; }
.brand-wordmark small { font-size: 9px; letter-spacing: 2.1px; color: var(--muted); }
</style>

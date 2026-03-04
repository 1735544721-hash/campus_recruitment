<template>

  <router-view/>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './store/user'

const userStore = useUserStore()


</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}


</style>

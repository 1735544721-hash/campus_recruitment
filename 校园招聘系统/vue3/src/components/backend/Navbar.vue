<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" :size="20" @click="toggleSidebar">
        <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="right-menu">
      <div class="right-menu-item" @click="toggleFullScreen">
        <el-icon :size="20">
          <component :is="isFullscreen ? Aim : FullScreen" />
        </el-icon>
      </div>
      
      <template v-if="!userInfo?.roleCode==='ADMIN'">
        <MessageNotification />
      </template>
      
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="32" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userInfo?.name || userInfo?.username || '用户' }}</span>
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessageBox } from 'element-plus'
import { Expand, Fold, ArrowDown, User, SwitchButton, FullScreen, Aim } from '@element-plus/icons-vue'
import MessageNotification from '@/components/MessageNotification.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'
const userInfo = computed(() => userStore.userInfo)
const isFullscreen = ref(false)

const toggleSidebar = () => {
  appStore.toggleSidebar()
}
const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : '';
})
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

// 监听全屏状态变化
const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement
}

document.addEventListener('fullscreenchange', fullscreenChangeHandler)

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', fullscreenChangeHandler)
})



const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
  animation: slideDown 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise' x='0' y='0'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100' height='100' filter='url(%23noise)' opacity='0.05'/%3E%3C/svg%3E");
    opacity: 0.1;
    pointer-events: none;
  }

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      color: #37474f;
      height: 32px;
      width: 32px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:hover {
        background: rgba(178, 235, 242, 0.3);
        transform: scale(1.05);
      }
    }

    :deep(.el-breadcrumb__inner) {
      color: #37474f;
      line-height: 32px;
      font-family: 'Avenir Next', sans-serif;
      
      &.is-link {
        color: #006064;
        
        &:hover {
          color: #00838f;
        }
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 12px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #37474f;
      border-radius: 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      height: 32px;
      width: 32px;
      
      &:hover {
        background: rgba(178, 235, 242, 0.3);
        transform: scale(1.05);
        color: #006064;
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 12px;
      height: 32px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: rgba(255, 255, 255, 0.6);
      
      &:hover {
        background: rgba(178, 235, 242, 0.3);
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      }
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #37474f;
        line-height: 32px;
        font-family: 'Avenir Next', sans-serif;
      }

      .el-icon {
        color: #37474f;
        display: flex;
        align-items: center;
        transition: transform 0.3s;
      }

      &:hover .el-icon {
        transform: rotate(180deg);
      }
    }
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    height: 40px;
    font-family: 'Avenir Next', sans-serif;
    color: #37474f;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(178, 235, 242, 0.3);
      color: #006064;
    }
    
    .el-icon {
      margin-right: 4px;
      display: flex;
      align-items: center;
    }
  }
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style> 
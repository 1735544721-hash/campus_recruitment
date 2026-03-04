<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="logo">校园招聘系统</div>
        <el-menu
          mode="horizontal"
          :router="true"
          :default-active="activeIndex"
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/positions">职位列表</el-menu-item>
          <el-menu-item index="/companies">企业列表</el-menu-item>
          
          <!-- 学生专属菜单 -->
          <template v-if="isLoggedIn && isStudent">
            <el-menu-item index="/resume">我的简历</el-menu-item>
            <el-menu-item index="/application">投递记录</el-menu-item>
            <el-menu-item index="/interview">我的面试</el-menu-item>
          </template>
          
          <!-- 企业专属菜单 -->
          <template v-if="isLoggedIn && isCompany">
     
            <el-menu-item class="publish-position-btn" index="/back/position/form" v-if="hasCompanyInfo">发布职位</el-menu-item>
   
            
            <!-- 如果企业信息未完善，显示提醒 -->
            <el-menu-item index="/company/profile" v-if="!hasCompanyInfo">
              <el-tag type="warning" size="small" effect="dark">企业信息</el-tag>
            </el-menu-item>
          </template>
          
          <!-- 管理员专属菜单 -->
          <template v-if="isLoggedIn && isAdmin">
            <el-menu-item index="/back/dashboard">
              <el-tag type="danger" size="small" effect="dark">后台管理</el-tag>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="user-menu">
          <template v-if="isLoggedIn">
            <MessageNotification />
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-dropdown">
                {{ userStore.userInfo?.name || userStore.userInfo?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  
                  <!-- 根据角色显示不同菜单项 -->
                  <template v-if="isStudent">
                    <el-dropdown-item command="student/info">学生信息</el-dropdown-item>
                    <el-dropdown-item command="resume">我的简历</el-dropdown-item>
                    <el-dropdown-item command="application">投递记录</el-dropdown-item>
                    <el-dropdown-item command="interview">我的面试</el-dropdown-item>
                  </template>
                  
                  <!-- 企业用户特有菜单 -->
                  <template v-else-if="isCompany">
                    <el-dropdown-item command="company/profile" v-if="!hasCompanyInfo">
                      企业信息
                      <!-- <el-tag size="small" type="warning">!</el-tag> -->
                    </el-dropdown-item>
                    <el-dropdown-item command="back/position/form" v-if="hasCompanyInfo">发布职位</el-dropdown-item>
                    <el-dropdown-item command="back/position">管理职位</el-dropdown-item>
                    <el-dropdown-item command="back/application">投递管理</el-dropdown-item>
                    <el-dropdown-item command="back/interview">面试管理</el-dropdown-item>
                  </template>
                  
                  <!-- 管理员特有菜单 -->
                  <template v-else-if="isAdmin">
                    <el-dropdown-item command="back/dashboard">后台管理</el-dropdown-item>
                  </template>
                  
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </el-header>
  
      <!-- 主要内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
  
      <!-- 页脚 -->
      <el-footer class="footer">
        <p>&copy; {{ new Date().getFullYear() }} 校园招聘系统 - 连接学生与企业的桥梁</p>
      </el-footer>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter, useRoute } from 'vue-router'
  import { ArrowDown, Plus } from '@element-plus/icons-vue'
  import MessageNotification from '@/components/MessageNotification.vue'
  
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  
  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const isStudent = computed(() => userStore.isStudent)
  const isCompany = computed(() => userStore.isCompany)
  const isAdmin = computed(() => userStore.isAdmin)
  const hasCompanyInfo = computed(() => userStore.hasCompanyInfo)
  const activeIndex = computed(() => route.path)
  
  // 处理下拉菜单命令
  const handleCommand = (command) => {
    if (command === 'logout') {
      userStore.clearUserInfo()
      router.push('/login')
    } else {
      router.push(`/${command}`)
    }
  }
  </script>
  
  <style lang="scss" scoped>
.frontend-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #F9F6F2;
  font-family: var(--font-family-base);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid #D3D3D3;
  padding: 0 2rem;
  height: 60px;
  backdrop-filter: blur(10px);
}

.logo {
  font-size: 1.5rem;
  font-weight: normal;
  color: #8B7355;
  position: relative;
  padding: 0.5rem 1rem;
  
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 40px;
    height: 40px;
    transform: translate(-50%, -50%);
    border: 1px solid rgba(139, 115, 85, 0.1);
    border-radius: 50%;
    animation: ink-spread 3s ease-out infinite;
  }
}

@keyframes ink-spread {
  0% {
    transform: translate(-50%, -50%) scale(0.95);
    opacity: 0;
  }
  50% {
    opacity: 0.1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0;
  }
}

.nav-menu {
  flex: 1;
  margin-left: 2rem;
  border-bottom: none;
  background: transparent;

  :deep(.el-menu) {
    background: transparent;
    border: none;
  }

  :deep(.el-menu-item) {
    font-family: var(--font-family-base);
    color: #9B8E83;
    height: 60px;
    line-height: 60px;
    border-bottom: 2px solid transparent;
    padding: 0 1.5rem;
    
    &:hover {
      color: #8B7355;
      background-color: transparent;
    }
    
    &.is-active {
      color: #8B7355;
      border-bottom-color: #8B7355;
      background-color: transparent;
      font-weight: normal;
    }
  }

  :deep(.el-sub-menu) {
    .el-sub-menu__title {
      font-family: var(--font-family-base);
      color: #9B8E83;
      height: 60px;
      line-height: 60px;
      padding: 0 1.5rem;
      
      &:hover {
        color: #8B7355;
        background-color: transparent;
      }
      
      .el-icon {
        display: none; /* 隐藏默认的下拉图标 */
      }
    }

    .el-menu--popup {
      background-color: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      border: 1px solid #D3D3D3;
      border-radius: 4px;
      padding: 0.5rem 0;

      .el-menu-item {
        height: 40px;
        line-height: 40px;
        padding: 0 1.5rem;

        &:hover {
          background-color: rgba(139, 115, 85, 0.05);
        }
      }
    }
  }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;

  .el-button {
    font-family: var(--font-family-base);
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;
    transition: all 0.3s ease;
    padding: 0.5rem 1rem;
    height: auto;

    &:hover {
      border-color: #8B7355;
      color: #8B7355;
      background-color: rgba(139, 115, 85, 0.05);
    }

    &.el-button--primary {
      background-color: #8B7355;
      border-color: #8B7355;
      color: #fff;

      &:hover {
        background-color: darken(#8B7355, 10%);
      }
    }
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem 1rem;
  color: #8B7355;
  font-family: var(--font-family-base);
  border: 1px solid transparent;
  border-radius: 4px;
  transition: all 0.3s ease;

  &:hover {
    border-color: #D3D3D3;
    background-color: rgba(139, 115, 85, 0.05);
  }

  .el-icon {
    margin-left: 0.5rem;
    font-size: 0.875rem;
  }
}

:deep(.el-dropdown-menu) {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid #D3D3D3;
  border-radius: 4px;
  padding: 0.5rem 0;

  .el-dropdown-menu__item {
    font-family: var(--font-family-base);
    color: #9B8E83;
    padding: 0.5rem 1.5rem;
    
    &:hover {
      color: #8B7355;
      background-color: rgba(139, 115, 85, 0.05);
    }

    &.is-disabled {
      color: #C0C4CC;
    }
  }
}

.publish-position-btn {
  margin-right: 1rem;
  color: #8B7355 !important;
  border: 1px solid #D3D3D3;
  border-radius: 4px;
  padding: 0 1.5rem;
  transition: all 0.3s ease;
  height: 60px !important;
  line-height: 60px !important;

  &:hover {
    border-color: #8B7355;
    background-color: rgba(139, 115, 85, 0.05) !important;
  }

  &.is-active {
    color: #fff !important;
    background-color: #8B7355 !important;
    border-color: #8B7355;
  }
}

.main-content {
  flex: 1;
  padding: 2rem;
  background: linear-gradient(to right, rgba(0,0,0,0.02) 1px, transparent 1px) 0 0 / 20px 20px,
              linear-gradient(to bottom, rgba(0,0,0,0.02) 1px, transparent 1px) 0 0 / 20px 20px,
              #F9F6F2;
}

.footer {
  background-color: #F5F2EE;
  color: #9B8E83;
  text-align: center;
  padding: 2rem;
  border-top: 1px solid #D3D3D3;
  font-family: var(--font-family-base);
}

:deep(.el-tag) {
  font-family: var(--font-family-base);
  border: none;
  
  &.el-tag--warning {
    background-color: rgba(230, 162, 60, 0.1);
    color: #8B7355;
  }
  
  &.el-tag--danger {
    background-color: rgba(245, 108, 108, 0.1);
    color: #8B7355;
  }
}
</style>
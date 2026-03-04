<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <span class="logo-icon">🎓</span>
      <span class="logo-text" v-show="!isCollapsed">校园招聘系统</span>
    </div>
    <div class="menu-wrapper">
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false" mode="vertical" class="sidebar-menu"
        text-color="#bfcbd9" active-text-color="#409EFF" router>
        
        <!-- 固定菜单项 - 所有后台用户都可以访问 -->
        <el-menu-item index="/back/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <!-- 用户管理 - 仅管理员可见 -->
        <el-menu-item index="/back/user" v-if="userStore.isAdmin">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <!-- 学生管理 - 仅管理员可见 -->
        <el-menu-item index="/back/student" v-if="userStore.isAdmin">
          <el-icon><Notebook /></el-icon>
          <template #title>学生管理</template>
        </el-menu-item>
        
        <!-- 企业管理 - 仅管理员可见 -->
        <el-menu-item index="/back/company" v-if="userStore.isAdmin">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>企业管理</template>
        </el-menu-item>
        
        <!-- 职位管理 - 管理员和企业用户可见 -->
        <el-menu-item index="/back/position" v-if="userStore.isAdmin||(userStore.isCompany&&userStore.hasCompanyInfo)">
          <el-icon><Suitcase /></el-icon>
          <template #title>职位管理</template>
        </el-menu-item>
        
        <!-- 简历管理 - 管理员和企业用户可见 -->
        <el-menu-item index="/back/resume" v-if="userStore.isAdmin">
          <el-icon><Document /></el-icon>
          <template #title>简历管理</template>
        </el-menu-item>
        
        <!-- 投递管理 - 管理员和企业用户可见 -->
        <el-menu-item index="/back/application" v-if="userStore.isAdmin||userStore.isCompany">
          <el-icon><Promotion /></el-icon>
          <template #title>投递管理</template>
        </el-menu-item>
        
        <!-- 面试管理 - 管理员和企业用户可见 -->
        <el-menu-item index="/back/interview" v-if="userStore.isAdmin||userStore.isCompany">
          <el-icon><Calendar /></el-icon>
          <template #title>面试管理</template>
        </el-menu-item>
        
        <!-- 消息通知管理 - 管理员和企业用户可见 -->
        <el-menu-item index="/back/message" v-if="userStore.isAdmin">
          <el-icon><Bell /></el-icon>
          <template #title>消息通知管理</template>
        </el-menu-item>
        
        <!-- 个人信息 - 所有后台用户都可以访问 -->
        <el-menu-item index="/back/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>
        
        <!-- 企业专属菜单 - 仅企业用户可见 -->
        <template v-if="userStore.isCompany">
          <el-menu-item @click="router.push('/company/profile')">
            <el-icon><Edit /></el-icon>
            <template #title>{{ userStore.hasCompanyInfo ? '修改企业信息' : '完善企业信息' }}</template>
          </el-menu-item>
        </template>
      </el-menu>
      
      <!-- 返回前台按钮 -->
      <div class="back-to-frontend">
        <el-button type="text" @click="router.push('/')" class="back-button">
          <el-icon><Back /></el-icon>
          <span v-show="!isCollapsed">返回前台</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { 
  HomeFilled, 
  User, 
  UserFilled,
  OfficeBuilding,
  Suitcase,
  Bell,
  Notebook,
  Document,
  Promotion,
  Calendar,
  Edit,
  Back
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 50%, #80deea 100%);
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);

  // 添加纸面微纹理
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
  
  &.is-collapsed {
    width: 64px;
    transform: perspective(1000px) rotateY(0deg);
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
        transform: scale(0.9);
      }
    }

    :deep(.el-menu) {
      .el-sub-menu__title span,
      .el-menu-item span {
        opacity: 0;
        transform: translateX(-10px);
      }
    }
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    line-height: 60px;
    text-align: center;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    padding: 0 16px;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    
    .logo-icon {
      font-size: 24px;
      margin-right: 8px;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .logo-text {
      color: #37474f;
      font-family: 'Avenir Next', sans-serif;
      font-size: 18px;
      font-weight: 500;
      white-space: nowrap;
      opacity: 1;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      letter-spacing: 0.5px;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.1);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 8px;

    .el-menu-item, .el-sub-menu__title {
      height: 48px;
      line-height: 48px;
      color: #37474f;
      background: transparent;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      margin: 4px 0;
      border-radius: 8px;
      font-family: 'Avenir Next', sans-serif;
      
      span {
        opacity: 1;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        transform: translateX(0);
      }
      
      &:hover {
        background: rgba(178, 235, 242, 0.3) !important;
        transform: translateX(4px);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      }
    }

    .el-menu-item.is-active {
      background: #b2ebf2 !important;
      color: #006064 !important;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transform: translateX(4px) scale(1.02);
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 3px;
        height: 100%;
        background: #006064;
        border-radius: 3px;
        transform: scaleY(0.6);
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #006064;
          background: rgba(178, 235, 242, 0.2) !important;
        }
      }

      .el-menu {
        background: rgba(255, 255, 255, 0.6);
        border-radius: 8px;
        margin: 4px 0;
        padding: 4px;
        
        .el-menu-item {
          background: transparent;
          
          &:hover {
            background: rgba(178, 235, 242, 0.3) !important;
          }
          
          &.is-active {
            background: #b2ebf2 !important;
          }
        }
      }
    }
  }

  .el-icon {
    vertical-align: middle;
    margin-right: 8px;
    width: 20px;
    text-align: center;
    color: inherit;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }

  span {
    vertical-align: middle;
    font-family: 'Avenir Next', sans-serif;
    letter-spacing: 0.3px;
  }
  
  .back-to-frontend {
    margin-top: auto;
    padding: 16px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    background: rgba(255, 255, 255, 0.9);
    
    .back-button {
      width: 100%;
      color: #37474f;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      padding: 10px;
      border-radius: 8px;
      font-family: 'Avenir Next', sans-serif;
      
      &:hover {
        color: #006064;
        background: rgba(178, 235, 242, 0.3);
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
      
      .el-icon {
        margin-right: 8px;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      }
      
      span {
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      }
    }
  }
}

// 添加折纸展开动画
@keyframes unfold {
  0% {
    transform: perspective(1000px) rotateY(-30deg);
    opacity: 0;
  }
  100% {
    transform: perspective(1000px) rotateY(0deg);
    opacity: 1;
  }
}

.sidebar-container {
  animation: unfold 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
</style> 
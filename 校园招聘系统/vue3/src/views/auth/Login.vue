<template>
  <Auth 
    :formData="loginForm" 
    :rules="rules" 
    :loading="loading"
    submitText="登录"
    @submit="handleSubmit"
  >
    <template #auth-tabs>
      <div class="auth-tabs">
        <div class="tab-item active">登录</div>
        <div class="tab-item" @click="goToRegister">注册</div>
      </div>
    </template>

    <template #welcome-text>
      <div class="welcome-title">欢迎回来</div>
      <div class="welcome-subtitle">请登录您的账号</div>
    </template>

    <template #form-items>
      <el-form-item prop="username">
        <el-input 
          v-model="loginForm.username"
          :prefix-icon="User"
          placeholder="请输入用户名">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input 
          v-model="loginForm.password"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入密码">
        </el-input>
      </el-form-item>
      <div class="form-options">
        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        <router-link to="/forget" class="forget-link">忘记密码？</router-link>
      </div>
    </template>

    <template #auth-links>
      <div>还没有账号？ <router-link to="/register">立即注册</router-link></div>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, Lock } from '@element-plus/icons-vue'
import Auth from './Auth.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleSubmit = (form) => {
  formRef.value = form.value
  loginFormRef.value = form.value
  handleLogin()
}

const goToRegister = () => {
  router.push('/register')
}

const loginFormRef = ref(null)

const handleLogin = () => {
  loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post("/user/login", loginForm, {
          successMsg: "登录成功",
          showDefaultMsg: true,
          onSuccess: async (data) => {
            userStore.setUserInfo(data)
            
            // 如果是企业用户，检查是否有企业信息
            if (data.roleCode === 'COMPANY') {
              await userStore.loadCompanyInfo()
            }
            
            // 根据返回的角色决定跳转路径
            if (data.roleCode === 'ADMIN') {
              // 直接导航到后台仪表盘
              await router.isReady()
              router.push(route.query.redirect || '/back/dashboard')
            } else {
              // 普通用户登录，直接跳转到前台
              const redirect = route.query.redirect || '/'
              router.push(redirect)
            }
          },
          onError: (error) => {
            console.error('登录失败:', error)
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.auth-tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #D3D3D3;
  font-family: var(--font-family-base);

  .tab-item {
    padding: 1rem 2rem;
    color: #9B8E83;
    cursor: pointer;
    position: relative;
    transition: color 0.3s ease;
    font-size: 1.125rem;

    &.active {
      color: #4A4A4A;

      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        width: 100%;
        height: 2px;
        background: #8B7355;
      }
    }

    &:hover {
      color: #8B7355;
    }
  }
}

.welcome-title {
  font-family: var(--font-family-base);
  font-size: 1.75rem;
  color: #4A4A4A;
  margin-bottom: 0.5rem;
  font-weight: 300;
}

.welcome-subtitle {
  font-family: var(--font-family-base);
  color: #9B8E83;
  margin-bottom: 2rem;
  font-size: 1rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
  color: #9B8E83;
  font-family: var(--font-family-base);
  
  :deep(.el-checkbox) {
    .el-checkbox__label {
      color: #9B8E83;
      font-family: var(--font-family-base);
    }
    
    .el-checkbox__input.is-checked {
      .el-checkbox__inner {
        background-color: #8B7355;
        border-color: #8B7355;
      }
    }
  }
  
  .forget-link {
    color: #8B7355;
    text-decoration: none;
    transition: all 0.3s ease;
    
    &:hover {
      color: darken(#8B7355, 10%);
    }
  }
}

.auth-links {
  text-align: center;
  margin-top: 1.5rem;
  font-family: var(--font-family-base);
  color: #9B8E83;

  a {
    color: #8B7355;
    text-decoration: none;
    transition: all 0.3s ease;

    &:hover {
      color: darken(#8B7355, 10%);
    }
  }
}
</style> 
<template>
  <Auth 
    :formData="registerForm" 
    :rules="rules" 
    :loading="loading"
    submitText="注册"
    @submit="handleSubmit"
  >
    <template #auth-tabs>
      <div class="auth-tabs">
        <div class="tab-item" @click="goToLogin">登录</div>
        <div class="tab-item active">注册</div>
      </div>
    </template>

    <template #welcome-text>
      <div class="welcome-title">创建您的账号</div>
      <div class="welcome-subtitle">开启职业新征程</div>
    </template>

    <template #form-items>
      <el-form-item prop="username">
        <el-input 
          v-model="registerForm.username"
          :prefix-icon="User"
          placeholder="请输入用户名">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input 
          v-model="registerForm.password"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入密码">
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input 
          v-model="registerForm.confirmPassword"
          :prefix-icon="Lock"
          type="password"
          placeholder="请确认密码">
        </el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input 
          v-model="registerForm.email"
          :prefix-icon="Message"
          placeholder="请输入邮箱">
        </el-input>
      </el-form-item>
      <el-form-item prop="phone">
        <el-input 
          v-model="registerForm.phone"
          :prefix-icon="Phone"
          placeholder="请输入手机号">
        </el-input>
      </el-form-item>
      <el-form-item prop="name">
        <el-input 
          v-model="registerForm.name"
          :prefix-icon="UserFilled"
          placeholder="请输入姓名">
        </el-input>
      </el-form-item>
      <el-form-item prop="roleCode">
        <el-select v-model="registerForm.roleCode" placeholder="请选择角色">
          <el-option label="学生" value="STUDENT" />
          <el-option label="公司" value="COMPANY" />
        </el-select>
      </el-form-item>

      
      <div class="agreement">
        <el-checkbox v-model="agreement">我已阅读并同意《用户协议》和《隐私政策》</el-checkbox>
      </div>
    </template>

    <template #auth-links>
      <div>已有账号？<router-link to="/login">立即登录</router-link></div>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Phone, UserFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import Auth from './Auth.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const agreement = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  roleCode:'',
  phone: '',
  name: '',
})

const validatePass2 = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  name: [
    { required: false }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleSubmit = (form) => {
  formRef.value = form.value
  registerFormRef.value = form.value
  handleRegister()
}

const goToLogin = () => {
  router.push('/login')
}

const handleRegister = () => {
  registerFormRef.value.validate(async valid => {
    if (valid) {
      if (!agreement.value) {
        ElMessage.warning('请阅读并同意用户协议和隐私政策')
        return
      }
      
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = registerForm
        await request.post("/user/add", registerData, {
          successMsg: "注册成功",
          showDefaultMsg: true,
          onSuccess: () => {
            router.push('/login')
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

.agreement {
  margin: 1.5rem 0;
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
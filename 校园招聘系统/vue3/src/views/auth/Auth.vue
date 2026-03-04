<template>
  <div class="auth-container">
    <!-- 左侧品牌宣传区域 -->
    <div class="auth-brand">
      <div class="brand-content">
        <div class="logo">
          <span>聘</span>
        </div>
        <h1 class="brand-title">校园招聘系统</h1>
        <div class="brand-image">
          <img src="@/assets/auth-illustration.svg" alt="校园招聘" />
        </div>
        <p class="brand-slogan">连接校园与企业，成就职业未来</p>
      </div>
    </div>
    
    <!-- 右侧功能区域 -->
    <div class="auth-form-container">
      <div class="auth-box">
        <slot name="auth-tabs"></slot>
        
        <div class="auth-welcome">
          <slot name="welcome-text"></slot>
        </div>
        
        <el-form :model="formData" :rules="rules" ref="formRef" class="auth-form">
          <slot name="form-items"></slot>
          
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit" class="auth-button">
              {{ submitText }}
            </el-button>
          </el-form-item>
          
          <div class="auth-links">
            <slot name="auth-links"></slot>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '提交'
  }
})

const formRef = ref(null)

const emit = defineEmits(['submit'])

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      emit('submit', formRef)
    }
  })
}

defineExpose({
  formRef
})
</script>

<style lang="scss" scoped>
@import '@/styles/auth.scss';

.auth-container {
  .auth-brand {
    .brand-content {
      .logo {
        span {
          font-family: "Songti SC", "STSong", serif;
          writing-mode: vertical-rl;
          text-orientation: upright;
          letter-spacing: 0.5em;
        }
      }
    }
  }

  .auth-form-container {
    .auth-box {
      .auth-form {
        :deep(.el-form-item) {
          margin-bottom: 1.5rem;

          .el-input {
            .el-input__wrapper {
              padding: 0.75rem;
              border-radius: 4px;
              background-color: rgba(#E8E6E1, 0.5);
              
              input {
                font-family: "Songti SC", "STSong", serif;
                color: #4A4A4A;
                
                &::placeholder {
                  color: #9B8E83;
                }
              }
            }
          }
        }

        :deep(.el-button) {
          font-family: "Songti SC", "STSong", serif;
          letter-spacing: 0.1em;
        }
      }
    }
  }
}
</style> 
<template>
  <div class="company-profile-complete">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>完善企业信息</span>
        </div>
      </template>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        class="company-form"
      >
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入企业名称" />
        </el-form-item>
        
        <el-form-item label="所属行业" prop="industry">
          <el-input v-model="form.industry" placeholder="请输入所属行业" />
        </el-form-item>
        
        <el-form-item label="企业规模" prop="companySize">
          <el-select v-model="form.companySize" placeholder="请选择企业规模" style="width: 100%;">
            <el-option label="少于50人" value="少于50人" />
            <el-option label="50-200人" value="50-200人" />
            <el-option label="200-500人" value="200-500人" />
            <el-option label="500-1000人" value="500-1000人" />
            <el-option label="1000-5000人" value="1000-5000人" />
            <el-option label="5000人以上" value="5000人以上" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="企业地址" prop="companyAddress">
          <el-input v-model="form.companyAddress" placeholder="请输入企业地址" />
        </el-form-item>
        
        <el-form-item label="企业Logo" prop="companyLogo">
          <el-upload
            class="avatar-uploader"
            action="#"
            :http-request="customUploadLogo"
            :show-file-list="false"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="logoUrl" :src="logoUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上传Logo，只能上传jpg/png格式图片，且不超过2MB</div>
        </el-form-item>
        
        <el-form-item label="企业简介" prop="companyIntro">
          <el-input 
            v-model="form.companyIntro" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入企业简介" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
          <el-button @click="skipStep">跳过此步</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  userId: null,
  companyName: '',
  industry: '',
  companySize: '',
  companyAddress: '',
  companyLogo: '',
  companyIntro: ''
})

// 计算属性：Logo完整URL
const logoUrl = computed(() => {
  if (!form.companyLogo) return ''
  const baseAPI = import.meta.env.VITE_APP_BASE_API || '/api'
  return baseAPI + form.companyLogo
})

// 表单验证规则
const rules = {
  companyName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 100, message: '企业名称长度在2到100个字符之间', trigger: 'blur' }
  ],
  industry: [
    { required: true, message: '请输入所属行业', trigger: 'blur' }
  ],
  companySize: [
    { required: true, message: '请选择企业规模', trigger: 'change' }
  ],
  companyAddress: [
    { required: true, message: '请输入企业地址', trigger: 'blur' }
  ]
}

// Logo上传前检查
const beforeLogoUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('Logo只能是JPG或PNG格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过2MB!')
    return false
  }
  return true
}

// 自定义上传Logo方法
const customUploadLogo = async (options) => {
  try {
    const { file } = options
    
    const formData = new FormData()
    formData.append('file', file)
    
    const uploadOptions = {
      headers: {
        token: localStorage.getItem('token') || ''
      },
      transformRequest: [(data) => data],
      successMsg: 'Logo上传成功',
      errorMsg: 'Logo上传失败',
      onSuccess: (data) => {
        form.companyLogo = data
        options.onSuccess()
      },
      onError: (error) => {
        console.error('Logo上传错误:', error)
        options.onError(new Error('上传失败'))
      }
    }
    
    await request.post('/file/upload/img', formData, uploadOptions)
  } catch (error) {
    options.onError(error)
    console.error('Logo上传过程发生错误:', error)
  }
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 设置用户ID
        form.userId = userStore.userInfo.id
        
        // 判断是新增还是更新
        if (form.id) {
          // 更新企业信息
          await request.put(`/company/${form.id}`, form, {
            successMsg: '企业信息更新成功',
            onSuccess: () => {
              router.push('/')
            }
          })
        } else {
          // 创建企业信息
          await request.post('/company', form, {
            successMsg: '企业信息保存成功',
            onSuccess: () => {
              router.push('/')
            }
          })
        }
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳过此步
const skipStep = () => {
  ElMessage({
    message: '您可以稍后在个人中心完善信息',
    type: 'warning'
  })
  router.push('/')
}

// 页面加载时获取当前用户信息和企业信息
onMounted(() => {
  if (!userStore.isCompany) {
    ElMessage.error('您不是企业用户，无法完善企业信息')
    router.push('/')
    return
  }
  
  // 检查是否已经有企业信息
  const userId = userStore.userInfo?.id
  if (userId) {
    form.userId = userId
    
    // 尝试获取已有的企业信息
    request.get(`/company/user/${userId}`,null, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        // 如果已有企业信息，填充表单
        Object.assign(form, data)
      },
      onError: () => {
        // 如果没有企业信息，继续使用空表单
      }
    })
  }
})
</script>

<style lang="scss" scoped>
.company-profile-complete {
  max-width: 800px;
  margin: 2rem auto;
  font-family: var(--font-family-base);
  
  :deep(.el-card) {
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid #D3D3D3;
    backdrop-filter: blur(10px);
    
    .el-card__header {
      border-bottom: 1px solid #D3D3D3;
      padding: 1.5rem;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        span {
          font-size: 1.5rem;
          color: #8B7355;
          font-weight: normal;
          position: relative;
          display: inline-block;
          
          &::after {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 60px;
            height: 60px;
            transform: translate(-50%, -50%);
            border: 1px solid rgba(139, 115, 85, 0.1);
            border-radius: 50%;
            animation: ink-spread 3s ease-out infinite;
          }
        }
      }
    }
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

.company-form {
  margin-top: 2rem;
  padding: 0 1rem;
  
  :deep(.el-form-item) {
    margin-bottom: 1.5rem;
    
    .el-form-item__label {
      color: #4A4A4A;
      font-weight: normal;
      padding-right: 1rem;
    }
    
    .el-input__wrapper {
      background: transparent;
      box-shadow: none;
      border: 1px solid #D3D3D3;
      
      &:hover {
        border-color: #9B8E83;
      }
      
      &.is-focus {
        border-color: #8B7355;
      }
      
      input {
        color: #4A4A4A;
        
        &::placeholder {
          color: #9B8E83;
        }
      }
    }
    
    .el-textarea__inner {
      background: transparent;
      border: 1px solid #D3D3D3;
      
      &:hover {
        border-color: #9B8E83;
      }
      
      &:focus {
        border-color: #8B7355;
      }
      
      &::placeholder {
        color: #9B8E83;
      }
    }
    
    .el-select {
      width: 150px !important;
      
      .el-input__wrapper {
        background: transparent;
      }
    }
  }
  
  .el-button {
    font-family: var(--font-family-base);
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #8B7355;
      background: rgba(139, 115, 85, 0.05);
    }
    
    &.el-button--primary {
      background: #8B7355;
      border-color: #8B7355;
      color: #fff;
      
      &:hover {
        background: darken(#8B7355, 10%);
      }
    }
  }
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .avatar {
    width: 150px;
    height: 150px;
    display: block;
    object-fit: contain;
    border: 1px solid #D3D3D3;
    background: rgba(255, 255, 255, 0.5);
    padding: 0.5rem;
  }
  
  .avatar-uploader-icon {
    font-size: 1.75rem;
    color: #9B8E83;
    width: 150px;
    height: 150px;
    text-align: center;
    border: 1px dashed #D3D3D3;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.5);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #8B7355;
      color: #8B7355;
      background: rgba(139, 115, 85, 0.05);
    }
  }
  
  .upload-tip {
    font-size: 0.875rem;
    color: #9B8E83;
    margin-top: 0.5rem;
    text-align: center;
  }
}
</style> 
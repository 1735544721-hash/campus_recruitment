<template>
  <div class="student-profile-complete">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>完善学生信息</span>
        </div>
      </template>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        class="student-form"
        status-icon
      >
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="20" show-word-limit />
        </el-form-item>
        
        <el-form-item label="学院" prop="college">
          <el-input v-model="form.college" placeholder="请输入学院" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="学历" prop="education">
          <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;">
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="毕业年份" prop="graduationYear">
          <el-date-picker
            v-model="graduationDate"
            type="year"
            placeholder="选择毕业年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 100%;"
            @change="handleYearChange"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" >保存</el-button>
          <el-button @click="resetForm">重置</el-button>
          <!-- <el-button @click="skipStep">跳过此步</el-button> -->
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const graduationDate = ref('')

// 表单数据
const form = reactive({
  userId: null,
  studentNo: '',
  college: '',
  major: '',
  education: '',
  graduationYear: null
})

// 表单验证规则
const rules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号长度在3到20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '学号只能包含字母和数字', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' },
    { min: 2, max: 50, message: '学院名称长度在2到50个字符之间', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' },
    { min: 2, max: 50, message: '专业名称长度在2到50个字符之间', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ],
  graduationYear: [
    { required: true, message: '请选择毕业年份', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && (value < new Date().getFullYear() - 5 || value > new Date().getFullYear() + 10)) {
          callback(new Error('毕业年份不合理，请重新选择'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

// 禁用的日期（限制毕业年份范围）
const disabledDate = (time) => {
  const currentYear = new Date().getFullYear()
  const year = new Date(time).getFullYear()
  return year < currentYear - 5 || year > currentYear + 10
}

// 年份选择处理
const handleYearChange = (val) => {
  if (val) {
    form.graduationYear = parseInt(val)
  } else {
    form.graduationYear = null
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
        
        await request.post('/student', form, {
          successMsg: '学生信息保存成功',
          onSuccess: () => {
            router.push('/')
          },
          onError: (error) => {
            console.error('保存学生信息失败', error)
          }
        })
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  ElMessageBox.confirm('确定要重置表单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formRef.value.resetFields()
    graduationDate.value = ''
    form.graduationYear = null
    ElMessage.success('表单已重置')
  }).catch(() => {})
}

// 跳过此步
// const skipStep = () => {
//   ElMessageBox.confirm('跳过此步骤后，您可以稍后在个人中心完善信息，确定要跳过吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     ElMessage({
//       message: '您可以稍后在个人中心完善信息',
//       type: 'warning'
//     })
//     router.push('/')
//   }).catch(() => {})
// }

// 页面加载时获取当前用户信息
onMounted(() => {
  if (!userStore.isStudent) {
    ElMessage.error('您不是学生用户，无法完善学生信息')
    router.push('/')
    return
  }
  
  // 检查是否已经有学生信息
  const userId = userStore.userInfo?.id
  if (userId) {
    loading.value = true
    request.get(`/student/user/${userId}`, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        // 如果已有学生信息，跳转到首页
        ElMessage.info('您已完善学生信息')
        router.push('/')
      },
      onError: () => {
        // 如果没有学生信息，继续当前页面
        form.userId = userId
      },
      complete: () => {
        loading.value = false
      }
    })
  } else {
    ElMessage.error('未获取到用户信息，请重新登录')
    router.push('/login')
  }
})
</script>

<style lang="scss" scoped>
.student-profile-complete {
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

.student-form {
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
    
    .el-select {
      width: 150px !important;
      
      .el-input__wrapper {
        background: transparent;
      }
    }
    
    .el-date-editor {
      width: 100%;
      
      .el-input__wrapper {
        background: transparent;
      }
    }
  }
  
  .el-button {
    padding: 0.75rem 1.5rem;
    margin-right: 1rem;
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

@media (max-width: 768px) {
  .student-profile-complete {
    max-width: 100%;
    margin: 1rem;
  }
}
</style> 
<template>
  <div class="student-info">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学生个人信息</span>
          <div>
              <el-button type="primary" @click="handleEdit" style="color: #fff!important;">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ student.studentNo || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userInfo.name || userInfo.username || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ student.college || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ student.major || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ student.education || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="毕业年份">{{ student.graduationYear || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="手机号码">{{ userInfo.phone || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 编辑学生信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑学生信息"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
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
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo || {});

const loading = ref(false);
const student = ref({});
const dialogVisible = ref(false);
const formRef = ref(null);
const submitLoading = ref(false);
const graduationDate = ref('');

// 表单数据
const form = reactive({
  id: null,
  userId: null,
  studentNo: '',
  college: '',
  major: '',
  education: '',
  graduationYear: null
});

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
    { required: true, message: '请选择毕业年份', trigger: 'change' }
  ]
};

// 禁用的日期
const disabledDate = (time) => {
  const currentYear = new Date().getFullYear();
  const year = new Date(time).getFullYear();
  return year < currentYear - 5 || year > currentYear + 10;
};

// 年份选择处理
const handleYearChange = (val) => {
  if (val) {
    form.graduationYear = parseInt(val);
  } else {
    form.graduationYear = null;
  }
};

// 获取学生信息
const fetchStudentInfo = async () => {
  if (!userStore.isLoggedIn || !userStore.isStudent) {
    ElMessage.error('请先登录学生账号');
    router.push('/login');
    return;
  }

  loading.value = true;
  try {
    await request.get('/student/current', null,{
      showDefaultMsg: false,
      onSuccess: (data) => {
        student.value = data || {};
      },
      onError: (error) => {
        console.error('获取学生信息失败', error);
        
        if (error?.message?.includes('未找到')) {
          ElMessageBox.confirm('您还未完善学生信息，是否现在完善？', '提示', {
            confirmButtonText: '是',
            cancelButtonText: '否',
            type: 'warning'
          }).then(() => {
            router.push('/student/profile');
          }).catch(() => {});
        }
      }
    });
  } finally {
    loading.value = false;
  }
};

// 编辑学生信息
const handleEdit = () => {
  if (!student.value.id) {
    router.push('/student/profile');
    return;
  }

  // 填充表单
  Object.assign(form, student.value);
  graduationDate.value = form.graduationYear ? form.graduationYear.toString() : '';
  
  dialogVisible.value = true;
};

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return false;
    }
    
    submitLoading.value = true;
    
    try {
      await request.put(`/student/${form.id}`, form, {
        successMsg: '更新成功',
        onSuccess: () => {
          dialogVisible.value = false;
          fetchStudentInfo(); // 重新获取学生信息
        }
      });
    } finally {
      submitLoading.value = false;
    }
  });
};

onMounted(() => {
  fetchStudentInfo();
});
</script>

<style lang="scss" scoped>
.student-info {
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
        
       .el-button {
          border: 1px solid #D3D3D3;
          background: transparent;
          color: #fff;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: #8B7355;
            background: rgba(139, 115, 85, 0.05);
          }
          
          :deep(.el-button--primary) {
            background: #8B7355;
            border-color: #8B7355;
            color: #fff;
            font-size: 12px;
            
            
            &:hover {
              background: darken(#8B7355, 10%);
            }
          }
        }
      }
    }
  }
  
  :deep(.el-descriptions) {
    margin-top: 1.5rem;
    
    .el-descriptions__header {
      margin-bottom: 1.5rem;
    }
    
    .el-descriptions__title {
      color: #4A4A4A;
      font-weight: normal;
      font-size: 1.25rem;
    }
    
    .el-descriptions__label {
      color: #4A4A4A;
      font-weight: normal;
      background: rgba(139, 115, 85, 0.05);
    }
    
    .el-descriptions__content {
      color: #9B8E83;
      background: rgba(255, 255, 255, 0.5);
    }
    
    .el-descriptions__body {
      background: transparent;
      
      .el-descriptions__table {
        border-color: #D3D3D3;
      }
      
      .el-descriptions__cell {
        border-color: #D3D3D3;
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

:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid #D3D3D3;
  
  .el-dialog__header {
    border-bottom: 1px solid #D3D3D3;
    margin-right: 0;
    padding: 1rem 1.5rem;
    
    .el-dialog__title {
      color: #4A4A4A;
      font-weight: normal;
      font-size: 1.25rem;
    }
  }
  
  .el-dialog__body {
    color: #9B8E83;
    padding: 1.5rem;
  }
  
  .el-dialog__footer {
    border-top: 1px solid #D3D3D3;
    padding: 1rem 1.5rem;
  }
  
  .el-form-item {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

@media (max-width: 768px) {
  .student-info {
    max-width: 100%;
    margin: 1rem;
  }
}
</style> 
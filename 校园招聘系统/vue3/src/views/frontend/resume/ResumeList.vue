<template>
  <div class="resume-list-page">
    <div class="page-header">
      <h1>我的简历</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">新建简历</el-button>
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="resumeList.length === 0" class="empty-container">
      <el-empty description="暂无简历，快去创建一份简历吧！">
        <el-button type="primary" @click="handleCreate">新建简历</el-button>
      </el-empty>
    </div>
    
    <div v-else class="resume-list">
      <resume-list-item
        v-for="resume in resumeList"
        :key="resume.id"
        :resume="resume"
        @view="handleView"
        @edit="handleEdit"
        @preview="handleView" 
        @setDefault="handleSetDefault"
        @polish="handlePolish"
        @delete="handleDelete"
      />
    </div>
    
    <!-- 创建简历对话框 -->
    <el-dialog
      title="新建简历"
      v-model="createDialogVisible"
      width="500px"
    >
      <el-form :model="createForm" ref="createFormRef" label-width="120px" :rules="rules">
        <el-form-item label="简历名称" prop="resumeName">
          <el-input v-model="createForm.resumeName" placeholder="请输入简历名称" />
        </el-form-item>
        <el-form-item label="是否设为默认">
          <el-switch v-model="createForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreateForm" :loading="submitting">创建</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ResumeListItem from '@/components/resume/ResumeListItem.vue'
import {
  getResumeList,
  createResume,
  deleteResume,
  setDefaultResume,

} from '@/utils/resumeApi'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const resumeList = ref([])

// 对话框显示状态
const createDialogVisible = ref(false)


// 表单数据
const createForm = ref({
  resumeName: '',
  isDefault: false
})


// 选中的模板和简历
const selectedResume = ref(null)

// 表单校验规则
const rules = {
  resumeName: [
    { required: true, message: '请输入简历名称', trigger: 'blur' },
    { max: 50, message: '简历名称不能超过50个字符', trigger: 'blur' }
  ]
}



// 页面初始化时加载简历列表
onMounted(() => {
  fetchResumeList()
})

// 获取简历列表
const fetchResumeList = () => {
  loading.value = true
  getResumeList({
    onSuccess: (res) => {
      resumeList.value = res || []
       loading.value = false
    },
    onError: () => {
      ElMessage.error('获取简历列表失败')
    },

  })
}

// 新建简历
const handleCreate = () => {
  createForm.value = {
    resumeName: '',
    isDefault: false
  }
  createDialogVisible.value = true
}

// 查看简历
const handleView = (resume) => {
  router.push(`/resume/preview/${resume.id}`)
}

// 编辑简历
const handleEdit = (resume) => {
  router.push(`/resume/edit/${resume.id}`)
}




// 设置默认简历
const handleSetDefault = (resume) => {
  setDefaultResume(resume.id, {
    successMsg: '设置成功',
    onSuccess: () => {
      fetchResumeList()
    }
  })
}


// AI润色简历
const handlePolish = (resume) => {
  router.push(`/resume/polish/${resume.id}`)
}

// 删除简历
const handleDelete = (resume) => {
  ElMessageBox.confirm('确定要删除此简历吗？该操作不可恢复', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteResume(resume.id, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchResumeList()
      }
    })
  }).catch(() => {})
}

// 提交创建简历表单
const submitCreateForm = () => {
  submitting.value = true
  createResume(createForm.value, {
    successMsg: '创建成功',
    onSuccess: (res) => {
      createDialogVisible.value = false
      router.push(`/resume/edit/${res}`)
    },
    onError: () => {
      ElMessage.error('创建简历失败')
    },
    onFinally: () => {
      submitting.value = false
    }
  })
}




</script>

<style lang="scss" scoped>
.resume-list-page {
  padding: 2rem;
  font-family: var(--font-family-base);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h1 {
  margin: 0;
    font-size: 2rem;
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

.header-actions {
  display: flex;
    gap: 1rem;

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

.loading-container, .empty-container {
  padding: 2.5rem;
  text-align: center;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  border-radius: 4px;
  backdrop-filter: blur(10px);

  :deep(.el-empty) {
    padding: 2rem;

    .el-empty__description {
      color: #9B8E83;
      margin-bottom: 1.5rem;
    }

    .el-button {
      background: #8B7355;
      border-color: #8B7355;
      color: #fff;

      &:hover {
        background: darken(#8B7355, 10%);
      }
    }
  }
}

.resume-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin: 2rem 0;

  :deep(.resume-list-item) {
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid #D3D3D3;
    border-radius: 4px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      border-color: #8B7355;
      transform: translateY(-2px);
    }

    .resume-header {
      padding: 1.5rem;
      border-bottom: 1px solid #D3D3D3;

      .resume-title {
        font-size: 1.25rem;
        color: #4A4A4A;
        font-weight: normal;
        margin-bottom: 0.5rem;
      }

      .resume-meta {
        color: #9B8E83;
        font-size: 0.875rem;
      }
    }

    .resume-content {
      padding: 1.5rem;

      .resume-info {
        margin-bottom: 1rem;
        color: #9B8E83;
        font-size: 0.875rem;
        line-height: 1.6;
      }

      .resume-actions {
        display: flex;
        gap: 0.75rem;
        flex-wrap: wrap;

        .el-button {
          flex: 1;
          min-width: 80px;
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

          &.el-button--success {
            background: #67C23A;
            border-color: #67C23A;
            color: #fff;

            &:hover {
              background: darken(#67C23A, 10%);
            }
          }

          &.el-button--danger {
            color: #F56C6C;
            border-color: #F56C6C;

            &:hover {
              background: rgba(245, 108, 108, 0.05);
            }
          }
        }
      }
    }

    .resume-status {
      position: absolute;
      top: 1rem;
      right: 1rem;

      .el-tag {
        background: transparent;
        border: 1px solid currentColor;

        &.el-tag--success {
          color: #8B7355;
        }

        &.el-tag--info {
          color: #9B8E83;
        }
      }
    }
  }
}

.expire-info {
  margin-top: 1rem;
  color: #9B8E83;
  font-size: 0.875rem;
  text-align: center;
}

@media (max-width: 768px) {
  .resume-list-page {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;

    .header-actions {
      width: 100%;
      justify-content: center;
    }
  }

  .resume-list {
    grid-template-columns: 1fr;
  }
}
</style> 
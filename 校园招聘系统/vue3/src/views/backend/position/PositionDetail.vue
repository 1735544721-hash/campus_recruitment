<template>
  <div class="position-detail-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>职位详情</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshPositionInfo">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <!-- 职位基本信息 -->
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>职位信息</span>
              <el-tag :type="getStatusType(position.status)" class="status-tag">
                {{ getStatusText(position.status) }}
              </el-tag>
            </div>
          </template>

          <div class="position-info">
            <div class="info-header">
              <div class="title-section">
                <h3 class="position-title">{{ position.positionName }}</h3>
                <div class="position-meta">
                  <span class="meta-item">{{ position.positionType }}</span>
                  <span class="meta-item">{{ position.workAddress }}</span>
                </div>
              </div>
              <div class="salary-section">
                <div class="salary">{{ position.salaryMin / 1000 }}k-{{ position.salaryMax / 1000 }}k</div>
              </div>
            </div>

            <el-divider />
            
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">职位类型：</div>
                <div class="info-value">{{ position.positionType }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">职位类别：</div>
                <div class="info-value">{{ position.positionCategory }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">招聘人数：</div>
                <div class="info-value">{{ position.positionCount }} 人</div>
              </div>
              <div class="info-item">
                <div class="info-label">学历要求：</div>
                <div class="info-value">{{ position.educationRequirement }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">经验要求：</div>
                <div class="info-value">{{ position.experienceRequirement }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">截止时间：</div>
                <div class="info-value">{{ DateUtils.formatDate(position.deadline) }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">发布时间：</div>
                <div class="info-value">{{ DateUtils.formatDate(position.createTime) }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">更新时间：</div>
                <div class="info-value">{{ DateUtils.formatDate(position.updateTime) }}</div>
              </div>
            </div>

            <el-divider />

            <div class="info-section">
              <h4>职位描述</h4>
              <div class="section-content">
                <pre>{{ position.positionDescription }}</pre>
              </div>
            </div>

            <div class="info-section">
              <h4>职位要求</h4>
              <div class="section-content">
                <pre>{{ position.positionRequirement }}</pre>
              </div>
            </div>

            <!-- 职位操作按钮 -->
            <div class="position-actions">
              <!-- 管理员可以审核职位 -->
              <template v-if="isAdmin && position.status === 2">
                <el-button type="success" @click="handleApprove">审核通过</el-button>
                <el-button type="danger" @click="handleReject">审核拒绝</el-button>
              </template>
              
              <!-- 管理员可以下线职位 -->
              <template v-if="isAdmin && position.status === 1">
                <el-button type="warning" @click="handleOffline">下线</el-button>
              </template>
              
              <!-- 企业用户可以编辑和删除自己的职位 -->
              <template v-if="isCompanyUser && userCompanyId === position.companyId">
                <el-button type="primary" @click="handleEdit" v-if="position.status !== 1">编辑</el-button>
                <el-button type="danger" @click="handleDelete">删除</el-button>
              </template>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 企业信息 -->
      <el-col :span="8">
        <el-card class="company-card">
          <template #header>
            <div class="card-header">
              <span>企业信息</span>
              <el-tag v-if="position.company?.verified" type="success" size="small">已认证</el-tag>
            </div>
          </template>

          <div class="company-info">
            <div class="company-header">
              <el-avatar :size="64" :src="getCompanyLogoUrl(position.companyLogo)" />
              <h3 class="company-name">{{ position.companyName }}</h3>
            </div>
            
            <el-descriptions :column="1" border class="company-details">
              <el-descriptions-item label="行业">
                {{ position.company?.industry || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="规模">
                {{ position.company?.companySize || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="地址">
                {{ position.company?.companyAddress || '未填写' }}
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="position.company?.companyDescription" class="company-description">
              <h4>企业介绍</h4>
              <div class="description-content">
                <p>{{ position.company.companyDescription }}</p>
              </div>
            </div>

            <div class="company-actions">
              <el-button type="primary" @click="viewCompany">查看企业详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPositionDetail, updatePositionStatus, deletePosition } from '@/api/position'
import { useUserStore } from '@/store/user'
import DateUtils from '@/utils/dateUtils'
import defaultLogo from '@/assets/logo.png'

const router = useRouter()
const route = useRoute()
const positionId = route.params.id
const userStore = useUserStore()

// 用户权限
const isAdmin = computed(() => userStore.isAdmin)
const isCompanyUser = computed(() => userStore.isCompanyUser)
const userCompanyId = computed(() => userStore.userInfo?.companyId)

// 职位数据
const loading = ref(false)
const position = reactive({
  id: '',
  companyId: '',
  companyName: '',
  companyLogo: '',
  positionName: '',
  positionType: '',
  positionCategory: '',
  workAddress: '',
  salaryMin: 0,
  salaryMax: 0,
  educationRequirement: '',
  experienceRequirement: '',
  positionCount: 0,
  deadline: '',
  positionDescription: '',
  positionRequirement: '',
  status: 0,
  createTime: '',
  updateTime: '',
  company: null
})

// 获取企业LOGO的完整URL
const baseAPI = import.meta.env.VITE_BASE_API || '/api'
const getCompanyLogoUrl = (logo) => {
  if (!logo) return defaultLogo
  return baseAPI + logo
}

// 根据状态获取标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'danger'
    case 1: return 'success'
    case 2: return 'warning'
    default: return 'info'
  }
}

// 根据状态获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '下线'
    case 1: return '上线'
    case 2: return '审核中'
    default: return '未知'
  }
}

// 获取职位详情
const fetchPositionDetail = async () => {
  loading.value = true
  try {
    await getPositionDetail(positionId, {
      onSuccess: (data) => {
        Object.keys(position).forEach(key => {
          if (data[key] !== undefined && data[key] !== null) {
            position[key] = data[key]
          }
        })
      },
      onError: () => {
        ElMessage.error('获取职位详情失败')
        goBack()
      }
    })
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.push('/back/position')
}

// 刷新职位信息
const refreshPositionInfo = () => {
  fetchPositionDetail()
}

// 查看企业详情
const viewCompany = () => {
  if (position.companyId) {
    router.push(`/back/company/detail/${position.companyId}`)
  }
}

// 编辑职位
const handleEdit = () => {
  router.push(`/back/position/form/${positionId}`)
}

// 删除职位
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定删除该职位吗？删除后不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deletePosition(positionId, {
      successMsg: '职位删除成功',
      onSuccess: () => {
        goBack()
      }
    })
  }).catch(() => {})
}

// 审核通过
const handleApprove = () => {
  updatePositionStatus(positionId, { status: 1 }, {
    successMsg: '审核通过成功',
    onSuccess: () => {
      refreshPositionInfo()
    }
  })
}

// 审核拒绝
const handleReject = () => {
  updatePositionStatus(positionId, { status: 0 }, {
    successMsg: '审核拒绝成功',
    onSuccess: () => {
      refreshPositionInfo()
    }
  })
}

// 下线职位
const handleOffline = () => {
  updatePositionStatus(positionId, { status: 0 }, {
    successMsg: '职位下线成功',
    onSuccess: () => {
      refreshPositionInfo()
    }
  })
}

// 页面加载时获取职位详情
onMounted(() => {
  fetchPositionDetail()
})
</script>

<style lang="scss" scoped>
.position-detail-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      h2 {
        margin: 0 0 0 10px;
        font-size: 20px;
      }
    }
  }
  
  .info-card {
    height: 100%;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .status-tag {
        margin-left: 10px;
      }
    }
    
    .position-info {
      .info-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .title-section {
          .position-title {
            margin: 0;
            font-size: 24px;
            font-weight: 500;
            color: #303133;
          }
          
          .position-meta {
            margin-top: 10px;
            
            .meta-item {
              margin-right: 15px;
              color: #909399;
              font-size: 14px;
            }
          }
        }
        
        .salary-section {
          .salary {
            font-size: 20px;
            color: #f56c6c;
            font-weight: 500;
          }
        }
      }
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;
        margin: 20px 0;
        
        .info-item {
          display: flex;
          
          .info-label {
            width: 80px;
            color: #909399;
            font-size: 14px;
          }
          
          .info-value {
            flex: 1;
            color: #303133;
            font-size: 14px;
          }
        }
      }
      
      .info-section {
        margin-top: 20px;
        
        h4 {
          margin: 0 0 10px 0;
          font-size: 16px;
          color: #303133;
          font-weight: 500;
        }
        
        .section-content {
          background-color: #f5f7fa;
          padding: 15px;
          border-radius: 4px;
          
          pre {
            white-space: pre-wrap;
            word-wrap: break-word;
            margin: 0;
            font-family: inherit;
            font-size: 14px;
            color: #606266;
            line-height: 1.6;
          }
        }
      }
      
      .position-actions {
        margin-top: 30px;
        display: flex;
        justify-content: flex-end;
        
        .el-button {
          margin-left: 15px;
        }
      }
    }
  }
  
  .company-card {
    height: 100%;
    
    .company-info {
      .company-header {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 20px;
        
        .company-name {
          margin: 10px 0 0;
          font-size: 18px;
          font-weight: 500;
          color: #303133;
        }
      }
      
      .company-details {
        margin-bottom: 20px;
      }
      
      .company-description {
        margin-top: 20px;
        
        h4 {
          margin: 0 0 10px 0;
          font-size: 16px;
          color: #303133;
          font-weight: 500;
        }
        
        .description-content {
          background-color: #f5f7fa;
          padding: 15px;
          border-radius: 4px;
          
          p {
            margin: 0;
            font-size: 14px;
            color: #606266;
            line-height: 1.6;
          }
        }
      }
      
      .company-actions {
        margin-top: 30px;
        text-align: center;
      }
    }
  }
}
</style> 
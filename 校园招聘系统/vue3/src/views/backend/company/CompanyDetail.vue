<template>
  <div class="company-detail-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>企业详情</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshCompanyInfo">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <!-- 企业基本信息 -->
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag :type="company.verified ? 'success' : 'danger'" class="status-tag">
                {{ company.verified ? '已认证' : '未认证' }}
              </el-tag>
            </div>
          </template>

          <div class="info-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称">{{ company.companyName || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="所属行业">{{ company.industry || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="企业规模">{{ company.companySize || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="企业地址">{{ company.companyAddress || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ DateUtils.formatDate(company.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ DateUtils.formatDate(company.updateTime) }}</el-descriptions-item>
            </el-descriptions>

            <div class="company-intro">
              <h4>企业简介</h4>
              <div class="intro-content">{{ company.companyIntro || '暂无企业简介' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 企业Logo和联系人信息 -->
      <el-col :span="8">
        <el-card class="logo-card">
          <template #header>
            <div class="card-header">
              <span>企业Logo</span>
            </div>
          </template>

          <div class="logo-container">
            <el-image
              v-if="company.companyLogo"
              :src="getImageUrl(company.companyLogo)"
              fit="contain"
              class="company-logo"
              :preview-src-list="[getImageUrl(company.companyLogo)]"
            />
            <div v-else class="no-logo">
              <el-icon><Picture /></el-icon>
              <span>暂无Logo</span>
            </div>
          </div>
        </el-card>

        <el-card class="contact-card">
          <template #header>
            <div class="card-header">
              <span>联系人信息</span>
            </div>
          </template>

          <div class="contact-content">
            <el-descriptions direction="vertical" :column="1" border>
              <el-descriptions-item label="联系人">{{ company.user?.name || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ company.user?.email || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ company.user?.phone || '暂无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button
        :type="company.verified ? 'danger' : 'success'"
        @click="toggleVerifyStatus"
      >
        {{ company.verified ? '取消认证' : '认证' }}
      </el-button>
      <el-popconfirm
        title="确定删除该企业信息吗？"
        @confirm="deleteCompany"
      >
        <template #reference>
          <el-button type="danger">删除</el-button>
        </template>
      </el-popconfirm>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const route = useRoute()
const companyId = route.params.id
const loading = ref(false)
const company = reactive({
  id: null,
  userId: null,
  companyName: '',
  industry: '',
  companySize: '',
  companyAddress: '',
  companyLogo: '',
  companyIntro: '',
  verified: false,
  createTime: null,
  updateTime: null,
  user: null
})

// 获取企业详情
const fetchCompanyDetail = async () => {
  loading.value = true
  try {
    await request.get(`/company/${companyId}`, {
      onSuccess: (data) => {
        Object.assign(company, data)
      },
      onError: () => {
        ElMessage.error('获取企业详情失败')
        router.push('/back/company')
      }
    })
  } finally {
    loading.value = false
  }
}

// 刷新企业信息
const refreshCompanyInfo = () => {
  fetchCompanyDetail()
}

// 返回上一页
const goBack = () => {
  router.push('/back/company')
}

// 切换认证状态
const toggleVerifyStatus = async () => {
  const newStatus = !company.verified
  const actionText = newStatus ? '认证' : '取消认证'
  
  try {
    await request.put(`/company/${companyId}/verify`, null, {
      params: { verified: newStatus },
      successMsg: `${actionText}成功`,
      onSuccess: () => {
        company.verified = newStatus
      }
    })
  } catch (error) {
    console.error('更新认证状态失败', error)
  }
}

// 删除企业
const deleteCompany = async () => {
  try {
    await request.delete(`/company/${companyId}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        router.push('/back/company')
      }
    })
  } catch (error) {
    console.error('删除企业失败', error)
  }
}

// 获取图片完整URL
const getImageUrl = (path) => {
  if (!path) return ''
  const baseAPI = import.meta.env.VITE_APP_BASE_API || '/api'
  return baseAPI + path
}

// 页面加载时获取企业详情
onMounted(() => {
  fetchCompanyDetail()
})
</script>

<style lang="scss" scoped>
.company-detail-container {
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
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .status-tag {
      margin-left: 10px;
    }
  }
  
  .info-card {
    margin-bottom: 20px;
    
    .info-content {
      .company-intro {
        margin-top: 20px;
        
        h4 {
          margin: 0 0 10px;
          font-size: 16px;
          font-weight: 500;
        }
        
        .intro-content {
          line-height: 1.6;
          white-space: pre-wrap;
          background-color: #f8f8f8;
          padding: 15px;
          border-radius: 4px;
          min-height: 100px;
        }
      }
    }
  }
  
  .logo-card {
    margin-bottom: 20px;
    
    .logo-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 200px;
      
      .company-logo {
        max-width: 100%;
        max-height: 180px;
        border-radius: 4px;
      }
      
      .no-logo {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 10px;
        }
      }
    }
  }
  
  .contact-card {
    margin-bottom: 20px;
  }
  
  .action-buttons {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    gap: 20px;
  }
}
</style> 
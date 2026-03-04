<template>
  <div class="company-detail-container">
    <div v-loading="loading" v-if="company">
      <!-- 返回按钮 -->
      <div style="text-align: left;" class="back-link">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回企业列表
        </el-button>
      </div>
      
      <!-- 企业基本信息卡片 -->
      <el-card class="company-header-card" shadow="never">
        <div class="company-header">
          <div class="company-logo">
            <el-image 
              :src="company.companyLogo ? baseAPI + company.companyLogo : '/default-company-logo.png'" 
              fit="cover"
              alt="公司logo"
              class="logo-image">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
              </template>
            </el-image>
            <el-tag v-if="company.verified" type="success" class="verified-tag">已认证</el-tag>
          </div>
          <div class="company-basic-info">
            <h1 class="company-name">{{ company.companyName }}</h1>
            <div class="company-meta">
              <div class="meta-item">
                <el-icon><Briefcase /></el-icon>
                <span>{{ company.industry || '未知行业' }}</span>
              </div>
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>{{ company.companySize || '未知规模' }}</span>
              </div>
              <div class="meta-item" v-if="company.companyAddress">
                <el-icon><Location /></el-icon>
                <span>{{ company.companyAddress }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 企业详情内容 -->
      <el-card class="company-content-card" shadow="never">
        <el-tabs>
          <el-tab-pane label="企业介绍">
            <div class="company-intro">
              <h3>企业简介</h3>
              <div class="intro-content">
                <pre>{{ company.companyIntro || '暂无企业简介' }}</pre>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="在招职位">
            <div class="company-positions">
              <div v-loading="positionsLoading">
                <template v-if="positions.length > 0">
                  <el-card v-for="position in positions" :key="position.id" class="position-card" shadow="never" @click="viewPositionDetail(position.id)">
                    <div class="position-header">
                      <div class="position-title">
                        <h3>{{ position.positionName }}</h3>
                        <div class="position-salary">
                          {{ formatSalary(position.salaryMin, position.salaryMax) }}
                        </div>
                      </div>
                    </div>
                    <div class="position-details">
                      <div class="detail-item">
                        <el-icon><Location /></el-icon>
                        <span>{{ position.workAddress || '地点未指定' }}</span>
                      </div>
                      <div class="detail-item">
                        <el-icon><School /></el-icon>
                        <span>{{ position.educationRequirement || '学历不限' }}</span>
                      </div>
                      <div class="detail-item">
                        <el-icon><Timer /></el-icon>
                        <span>{{ position.experienceRequirement || '经验不限' }}</span>
                      </div>
                    </div>
                    <div class="position-tags">
                      <el-tag size="small" type="info" v-if="position.positionType">{{ position.positionType }}</el-tag>
                      <el-tag size="small" type="success" v-if="position.positionCategory">{{ position.positionCategory }}</el-tag>
                      <el-tag size="small" type="warning" v-if="position.deadline">截止: {{ DateUtils.formatDate(position.deadline) }}</el-tag>
                    </div>
                    <div class="position-description">
                      {{ truncateText(position.positionDescription, 100) }}
                    </div>
                    <div class="position-footer">
                      <span class="publish-time">发布时间: {{ DateUtils.formatDateTime(position.createTime) }}</span>
                      <el-button type="primary" size="small" @click.stop="applyPosition(position)">立即申请</el-button>
                    </div>
                  </el-card>
                </template>
                <el-empty v-else description="暂无在招职位" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
    
    <el-empty v-else-if="!loading" description="企业不存在或已下线" />
    
    <!-- 申请职位对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请职位" width="500px">
      <div v-if="selectedPosition" class="apply-dialog-content">
        <div class="apply-position-info">
          <h3>{{ selectedPosition.positionName }}</h3>
          <p>{{ company.companyName }}</p>
        </div>
        <el-form :model="applyForm" label-width="100px">
          <el-form-item label="选择简历">
            <el-select v-model="applyForm.resumeId" placeholder="请选择简历" style="width: 100%">
              <el-option
                v-for="resume in resumeList"
                :key="resume.id"
                :label="resume.resumeName"
                :value="resume.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="resume-tips" v-if="!resumeList.length">
          <el-alert
            title="您还没有创建简历"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              请先<el-link type="primary" @click="goToCreateResume">创建简历</el-link>后再申请职位
            </template>
          </el-alert>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApplication" :disabled="!applyForm.resumeId">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { ArrowLeft, Location, School, Timer, OfficeBuilding, Briefcase, User } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据定义
const company = ref(null)
const loading = ref(false)
const positions = ref([])
const positionsLoading = ref(false)
const applyDialogVisible = ref(false)
const selectedPosition = ref(null)
const resumeList = ref([])
const applyForm = reactive({
  resumeId: '',
  positionId: ''
})

// 计算属性
const canApply = computed(() => {
  return userStore.isLoggedIn && userStore.isStudent
})

// 获取企业详情
const fetchCompanyDetail = () => {
  const companyId = route.params.id
  if (!companyId) {
    ElMessage.error('企业ID不能为空')
    return
  }
  
  loading.value = true
  request.get(`/company/${companyId}`, {}, {
    onSuccess: (res) => {
      company.value = res
      fetchCompanyPositions(companyId)
    },
    onError: (error) => {
      console.error('获取企业详情失败:', error)
    },
    onFinally: () => {
      loading.value = false
    }
  })
}

// 获取企业职位
const fetchCompanyPositions = (companyId) => {
  positionsLoading.value = true
  request.get(`/position/company/${companyId}`, {}, {
    onSuccess: (res) => {
      positions.value = res || []
    },
    onError: (error) => {
      console.error('获取企业职位失败:', error)
    },
    onFinally: () => {
      positionsLoading.value = false
    }
  })
}

// 获取用户的简历列表
const fetchUserResumes = () => {
  if (!userStore.isLoggedIn || !userStore.isStudent) return
  
  request.get('/resume/list', {}, {
    onSuccess: (res) => {
      resumeList.value = res || []
    },
    onError: (error) => {
      console.error('获取简历列表失败:', error)
    }
  })
}

// 查看职位详情
const viewPositionDetail = (id) => {
  router.push(`/position/detail/${id}`)
}

// 申请职位
const applyPosition = (position) => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('您需要登录后才能申请职位，是否立即登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }
  
  if (!userStore.isStudent) {
    ElMessage.warning('只有学生用户才能申请职位')
    return
  }
  
  selectedPosition.value = position
  applyForm.positionId = position.id
  applyForm.resumeId = resumeList.value.length > 0 ? resumeList.value[0].id : ''
  applyDialogVisible.value = true
}

// 提交申请
const submitApplication = () => {
  if (!applyForm.resumeId) {
    ElMessage.warning('请先选择简历')
    return
  }
  
  request.post('/application', {
    positionId: applyForm.positionId,
    resumeId: applyForm.resumeId
  }, {
    successMsg: '申请提交成功',
    onSuccess: () => {
      applyDialogVisible.value = false
      // 可以跳转到投递记录页面
      router.push('/application')
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 前往创建简历
const goToCreateResume = () => {
  applyDialogVisible.value = false
  router.push('/resume/edit/new')
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '薪资面议'
  if (min && max) return `${min}K-${max}K`
  if (min) return `${min}K以上`
  return `${max}K以下`
}

// 文本截断
const truncateText = (text, length) => {
  if (!text) return '暂无描述'
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 页面加载时获取数据
onMounted(() => {
  fetchCompanyDetail()
  fetchUserResumes()
})
</script>

<style lang="scss" scoped>
.company-detail-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  font-family: var(--font-family-base);
}

.back-link {
  margin-bottom: 2rem;

  .el-button {
    color: #8B7355;
    padding: 0;

    &:hover {
      color: darken(#8B7355, 10%);
    }
  }
}

.company-header-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  margin-bottom: 2rem;
  backdrop-filter: blur(10px);
}

.company-header {
  display: flex;
  align-items: flex-start;
  gap: 2rem;
}

.company-logo {
  position: relative;
  width: 150px;
  flex-shrink: 0;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid #D3D3D3;
  border-radius: 4px;

.logo-image {
  width: 100%;
  height: 100%;
    object-fit: contain;
    opacity: 0.9;
}

.verified-tag {
  position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    background: rgba(103, 194, 58, 0.1);
    border: none;
    color: #8B7355;
  }
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: rgba(139, 115, 85, 0.05);
  color: #9B8E83;

  .el-icon {
    font-size: 3rem;
  }
}

.company-basic-info {
  flex: 1;
}

.company-name {
  font-size: 2rem;
  margin: 0 0 1.5rem;
  color: #4A4A4A;
  font-weight: normal;
  position: relative;
  display: inline-block;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 80px;
    height: 80px;
    transform: translate(-50%, -50%);
    border: 1px solid rgba(139, 115, 85, 0.1);
    border-radius: 50%;
    animation: ink-spread 3s ease-out infinite;
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

.company-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.meta-item {
  display: flex;
  align-items: center;
  color: #9B8E83;
  gap: 0.5rem;
  font-size: 1rem;
}

.company-content-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  margin-bottom: 2rem;
  backdrop-filter: blur(10px);

  :deep(.el-tabs) {
    .el-tabs__header {
      margin-bottom: 2rem;
}

    .el-tabs__nav-wrap::after {
      background-color: #D3D3D3;
    }

    .el-tabs__item {
      color: #9B8E83;
      font-size: 1rem;
      
      &:hover {
        color: #8B7355;
      }
      
      &.is-active {
        color: #8B7355;
      }
    }

    .el-tabs__active-bar {
      background-color: #8B7355;
    }
  }
}

.company-intro {
  h3 {
    font-size: 1.25rem;
    margin: 1.5rem 0 1rem;
    color: #4A4A4A;
    font-weight: normal;
    border-left: 2px solid #8B7355;
    padding-left: 1rem;
}

.intro-content {
    color: #9B8E83;
  line-height: 1.8;

    pre {
  white-space: pre-wrap;
  font-family: inherit;
}
  }
}

.position-card {
  margin-bottom: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid #D3D3D3;

  &:hover {
    border-color: #8B7355;
  transform: translateY(-5px);
  }
}

.position-header {
  margin-bottom: 1rem;
}

.position-title {
  h3 {
    font-size: 1.25rem;
    margin: 0 0 0.5rem;
    color: #4A4A4A;
    font-weight: normal;
}

.position-salary {
    color: #8B7355;
    font-weight: normal;
    font-size: 1rem;
  }
}

.position-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.detail-item {
  display: flex;
  align-items: center;
  color: #9B8E83;
  gap: 0.25rem;
  font-size: 0.875rem;
}

.position-tags {
  margin-bottom: 1rem;

  :deep(.el-tag) {
    margin-right: 0.5rem;
    margin-bottom: 0.5rem;
    background: rgba(139, 115, 85, 0.1);
    border: none;
    color: #8B7355;

    &.el-tag--success {
      background: rgba(103, 194, 58, 0.1);
    }

    &.el-tag--warning {
      background: rgba(230, 162, 60, 0.1);
    }
  }
}

.position-description {
  color: #9B8E83;
  font-size: 0.875rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.position-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #D3D3D3;
  padding-top: 1rem;

.publish-time {
    color: #9B8E83;
    font-size: 0.75rem;
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

.apply-dialog-content {
  padding: 0 1.5rem;
}

.apply-position-info {
  margin-bottom: 1.5rem;
  text-align: center;

  h3 {
    margin: 0 0 0.5rem;
    color: #4A4A4A;
    font-weight: normal;
}

  p {
  margin: 0;
    color: #9B8E83;
  }
}

.resume-tips {
  margin-top: 1.5rem;

  :deep(.el-alert) {
    background: rgba(230, 162, 60, 0.1);
    border: none;
    color: #8B7355;

    .el-alert__title {
      color: #8B7355;
    }

    .el-alert__icon {
      color: #8B7355;
    }

    .el-link {
      color: #8B7355;
      text-decoration: underline;

      &:hover {
        color: darken(#8B7355, 10%);
      }
    }
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

  .el-button {
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;

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
</style> 
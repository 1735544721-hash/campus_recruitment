<template>
  <div class="position-detail-container">
    <div v-loading="loading" v-if="position">
      <!-- 返回按钮 -->
      <div style="text-align: left;" class="back-link">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回职位列表
        </el-button>
      </div>
      
      <!-- 职位头部信息 -->
      <el-card class="position-header-card" shadow="never">
        <div class="position-header">
          <div class="position-basic-info">
            <h1 class="position-name">{{ position.positionName }}</h1>
            <div class="position-meta">
              <div class="salary">{{ formatSalary(position.salaryMin, position.salaryMax) }}</div>
              <div class="location">
                <el-icon><Location /></el-icon>
                {{ position.workAddress || '地点未指定' }}
              </div>
              <div class="education">
                <el-icon><School /></el-icon>
                {{ position.educationRequirement || '学历不限' }}
              </div>
              <div class="experience">
                <el-icon><Timer /></el-icon>
                {{ position.experienceRequirement || '经验不限' }}
              </div>
            </div>
            <div class="position-tags">
              <el-tag size="small" type="info" v-if="position.positionType">{{ position.positionType }}</el-tag>
              <el-tag size="small" type="success" v-if="position.positionCategory">{{ position.positionCategory }}</el-tag>
              <el-tag size="small" type="warning" v-if="position.deadline">截止日期: {{ DateUtils.formatDate(position.deadline) }}</el-tag>
              <el-tag size="small" type="primary" v-if="position.positionCount">招聘人数: {{ position.positionCount }}人</el-tag>
            </div>
          </div>
          <div class="company-brief">
            <div class="company-logo">
              <el-avatar :size="80" :src="position.companyLogo ? baseAPI + position.companyLogo : ''" @error="handleAvatarError">
                <el-icon><OfficeBuilding /></el-icon>
              </el-avatar>
            </div>
            <div class="company-name">{{ position.companyName }}</div>
            <el-button type="primary" @click="viewCompanyDetail">查看企业</el-button>
          </div>
        </div>
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="applyPosition" :disabled="!canApply">立即申请</el-button>
          <!-- <el-button size="large" @click="collectPosition">
            <el-icon><Star /></el-icon> 收藏职位
          </el-button> -->
        </div>
      </el-card>
      
      <!-- 职位详情内容 -->
      <el-card class="position-content-card" shadow="never" >
        <el-tabs>
          <el-tab-pane label="职位描述">
            <div class="position-description">
              <h3>职位描述</h3>
              <div class="description-content">
                <pre>{{ position.positionDescription || '暂无职位描述' }}</pre>
              </div>
              
              <h3>职位要求</h3>
              <div class="requirement-content">
                <pre>{{ position.positionRequirement || '暂无职位要求' }}</pre>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="企业信息">
            <div class="company-info" v-if="position.company">
              <h3>企业简介</h3>
              <div class="company-intro">
                <pre>{{ position.company.companyIntro || '暂无企业简介' }}</pre>
              </div>
              
              <h3>基本信息</h3>
              <div class="company-basic-info">
                <div class="info-item">
                  <span class="label">企业名称：</span>
                  <span class="value">{{ position.company.companyName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">所属行业：</span>
                  <span class="value">{{ position.company.industry || '未指定' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">企业规模：</span>
                  <span class="value">{{ position.company.companySize || '未指定' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">企业地址：</span>
                  <span class="value">{{ position.company.companyAddress || '未指定' }}</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      
      <!-- 相似职位推荐 -->
      <div class="similar-positions">
        <h3>相似职位推荐</h3>
        <el-row :gutter="20">
          <el-col v-for="item in similarPositions" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="similar-position-card" shadow="never" @click="viewPosition(item.id)">
              <div class="similar-position-header">
                <h4>{{ item.positionName }}</h4>
                <div class="similar-position-salary">{{ formatSalary(item.salaryMin, item.salaryMax) }}</div>
              </div>
              <div class="similar-position-company">
                {{ item.companyName }}
              </div>
              <div class="similar-position-meta">
                <span>{{ item.workAddress }}</span>
                <span>{{ item.educationRequirement }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <el-empty v-else-if="!loading" description="职位不存在或已下线" />
    
    <!-- 申请职位对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请职位" width="500px">
      <div v-if="position" class="apply-dialog-content">
        <div class="apply-position-info">
          <h3>{{ position.positionName }}</h3>
          <p>{{ position.companyName }}</p>
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
import { ArrowLeft, Location, School, Timer, OfficeBuilding, Star } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据定义
const position = ref(null)
const loading = ref(false)
const similarPositions = ref([])
const applyDialogVisible = ref(false)
const resumeList = ref([])
const applyForm = reactive({
  resumeId: '',
  positionId: ''
})

// 计算属性
const canApply = computed(() => {
  return userStore.isLoggedIn && userStore.isStudent
})

// 获取职位详情
const fetchPositionDetail = () => {
  const positionId = route.params.id
  if (!positionId) {
    ElMessage.error('职位ID不能为空')
    return
  }
  
  loading.value = true
  request.get(`/position/${positionId}`, {}, {
    onSuccess: (res) => {
      position.value = res
      fetchSimilarPositions()
    },
    onError: (error) => {
      console.error('获取职位详情失败:', error)
    },
    onFinally: () => {
      loading.value = false
    }
  })
}

// 获取相似职位
const fetchSimilarPositions = () => {
  if (!position.value) return
  
  request.get('/position/page', {
    positionType: position.value.positionType,
    status: 1,
    currentPage: 1,
    size: 4
  }, {
    onSuccess: (res) => {
      // 过滤掉当前职位
      similarPositions.value = (res.records || []).filter(item => item.id !== position.value.id)
    },
    onError: (error) => {
      console.error('获取相似职位失败:', error)
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

// 申请职位
const applyPosition = () => {
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
  
  applyForm.positionId = position.value.id
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

// 收藏职位
const collectPosition = () => {
  ElMessage.success('收藏成功')
  // 实际项目中应该调用收藏接口
}

// 查看企业详情
const viewCompanyDetail = () => {
  if (position.value && position.value.companyId) {
    router.push(`/company-detail/${position.value.companyId}`)
  }
}

// 查看其他职位
const viewPosition = (id) => {
  router.push(`/position/detail/${id}`)
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

// 处理头像加载错误
const handleAvatarError = () => {
  // 使用默认图标，不需要额外处理
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '薪资面议'
  if (min && max) return `${min}K-${max}K`
  if (min) return `${min}K以上`
  return `${max}K以下`
}

// 页面加载时获取数据
onMounted(() => {
  fetchPositionDetail()
  fetchUserResumes()
})
</script>

<style lang="scss" scoped>
.position-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
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

.position-header-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  margin-bottom: 2rem;
  backdrop-filter: blur(10px);
}

.position-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.position-basic-info {
  flex: 1;
}

.position-name {
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

.position-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 1.5rem;

  > div {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #9B8E83;
  }

  .salary {
    color: #8B7355;
    font-weight: normal;
    font-size: 1.25rem;
  }
}

.position-tags {
  margin-bottom: 1.5rem;
  text-align: left;

  .el-tag {
    margin-right: 0.5rem;
    margin-bottom: 0.5rem;
    background: transparent;
    border: 1px solid currentColor;

    &.el-tag--info {
      color: #9B8E83;
    }

    &.el-tag--success {
      color: #8B7355;
    }

    &.el-tag--warning {
      color: #8B7355;
    }

    &.el-tag--primary {
      color: #8B7355;
    }
  }
}

.company-brief {
  text-align: center;
  width: 200px;
  padding: 1.5rem;
  border-left: 1px solid #D3D3D3;

  .company-logo {
    margin-bottom: 1rem;

    :deep(.el-avatar) {
      background: rgba(255, 255, 255, 0.5);
      border: 1px solid #D3D3D3;
    }
  }

  .company-name {
    font-size: 1.125rem;
    margin-bottom: 1rem;
    color: #4A4A4A;
  }

  .el-button {
    width: 100%;
    background: #8B7355;
    border-color: #8B7355;
    color: #fff;

    &:hover {
      background: darken(#8B7355, 10%);
    }
  }
}

.action-buttons {
  display: flex;
  justify-content: flex-start;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid #D3D3D3;

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

.position-content-card {
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

.position-description, .company-info {
  text-align: left;
  h3 {
    font-size: 1.25rem;
    margin: 1.5rem 0 1rem;
    color: #4A4A4A;
    font-weight: normal;
    border-left: 2px solid #8B7355;
    padding-left: 1rem;
  }

  pre {
    white-space: pre-wrap;
    font-family: var(--font-family-base);
    color: #9B8E83;
    line-height: 1.8;
  }
}

.company-basic-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
  margin-top: 1.5rem;

  .info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    .label {
      color: #4A4A4A;
      width: 80px;
      flex-shrink: 0;
    }

    .value {
      color: #9B8E83;
    }
  }
}

.similar-positions {
  margin-top: 3rem;

  h3 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
    color: #4A4A4A;
    font-weight: normal;
    text-align: center;
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

.similar-position-card {
  height: 100%;
  margin-bottom: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);

  &:hover {
    border-color: #8B7355;
    transform: translateY(-5px);
  }
}

.similar-position-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;

  h4 {
    margin: 0;
    font-size: 1.125rem;
    color: #4A4A4A;
    font-weight: normal;
  }

  .similar-position-salary {
    color: #8B7355;
    font-weight: normal;
  }
}

.similar-position-company {
  color: #9B8E83;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.similar-position-meta {
  display: flex;
  justify-content: space-between;
  color: #9B8E83;
  font-size: 0.875rem;
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
</style> 
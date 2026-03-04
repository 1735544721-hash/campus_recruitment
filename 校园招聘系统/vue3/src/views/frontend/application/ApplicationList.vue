<template>
  <div class="application-list-container">
    <div class="page-header">
      <h2>我的投递记录</h2>
      <div class="statistics">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">总投递</div>
              <div class="statistic-value">{{ statistics.totalCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">待查看</div>
              <div class="statistic-value">{{ statistics.pendingCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">已查看</div>
              <div class="statistic-value">{{ statistics.viewedCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">通过筛选</div>
              <div class="statistic-value">{{ statistics.passedCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">面试邀请</div>
              <div class="statistic-value">{{ statistics.interviewCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="statistic-card">
              <div class="statistic-title">已录用</div>
              <div class="statistic-value">{{ statistics.offeredCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <div class="filter-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="状态">
          <el-radio-group v-model="searchForm.status" @change="handleStatusChange">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="0">待查看</el-radio-button>
            <el-radio-button :label="1">已查看</el-radio-button>
            <el-radio-button :label="2">通过筛选</el-radio-button>
            <el-radio-button :label="3">面试邀请</el-radio-button>
            <el-radio-button :label="4">不合适</el-radio-button>
            <el-radio-button :label="5">已录用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="投递时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="application-list">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        :empty-text="emptyText"
      >
        <el-table-column label="企业信息" width="220">
          <template #default="{ row }">
            <div class="company-info">
              <el-avatar :size="50" :src="row.companyLogo ? baseAPI + row.companyLogo : '/default-logo.png'" />
              <div class="company-name">{{ row.companyName }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="positionName" label="职位名称" />
        
        <el-table-column prop="resumeName" label="简历名称" />
        
        <el-table-column label="投递状态" width="150">
          <template #default="{ row }">
            <el-tooltip
              :content="getStatusDescription(row.status)"
              placement="top"
              effect="light"
              :show-after="500"
            >
              <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
                <el-icon v-if="row.status === 0"><Clock /></el-icon>
                <el-icon v-else-if="row.status === 1"><View /></el-icon>
                <el-icon v-else-if="row.status === 2"><Check /></el-icon>
                <el-icon v-else-if="row.status === 3"><Bell /></el-icon>
                <el-icon v-else-if="row.status === 4"><Close /></el-icon>
                <el-icon v-else-if="row.status === 5"><CircleCheck /></el-icon>
                {{ getStatusName(row.status) }}
              </el-tag>
            </el-tooltip>
            <div class="status-progress">
              <div class="progress-bar">
                <div 
                  class="progress-step" 
                  v-for="step in 6" 
                  :key="step"
                  :class="{ 
                    'active': row.status >= step - 1, 
                    'rejected': row.status === 4 && step > 1,
                    'current': row.status === step - 1 
                  }"
                ></div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="投递时间" width="180" />
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewPosition(row.positionId)">查看职位</el-button>
            <el-button type="primary" link @click="previewResume(row.resumeId)">预览简历</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 简历预览对话框 -->
    <el-dialog
      v-model="resumeDialogVisible"
      title="简历预览"
      width="80%"
      destroy-on-close
      :before-close="handleCloseResumeDialog"
    >
      <div v-loading="resumeLoading" class="resume-preview-container">
        <div v-if="resumeLoading" class="resume-loading">
          <el-skeleton :rows="10" animated />
        </div>
        <div v-else-if="resumeData" class="resume-content">
          <div class="resume-header">
            <h2>{{ resumeData.resumeName }}</h2>
            <div class="resume-actions">
              <el-dropdown @command="handleDownloadFormat">
                <el-button type="primary">
                  下载简历<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pdf">PDF格式</el-dropdown-item>
                    <el-dropdown-item command="word">Word格式</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <el-tabs>
            <el-tab-pane label="在线预览">
              <iframe v-if="resumeUrl" :src="resumeUrl" style="width: 100%; height: 600px; border: none;"></iframe>
            </el-tab-pane>
            <el-tab-pane label="简历详情">
              <div v-if="resumeData.resumeContent" class="resume-detail">
                <!-- 基本信息 -->
                <el-card class="resume-section" shadow="hover">
                  <template #header>
                    <div class="resume-section-header">
                      <el-icon><UserFilled /></el-icon>
                      <span>基本信息</span>
                    </div>
                  </template>
                  <div v-if="resumeData.resumeContent.basicInfo">
                    <el-descriptions :column="2" border>
                      <el-descriptions-item label="姓名">{{ resumeData.resumeContent.basicInfo.name }}</el-descriptions-item>
                      <el-descriptions-item label="性别">{{ resumeData.resumeContent.basicInfo.gender }}</el-descriptions-item>
                      <el-descriptions-item label="出生日期">{{ resumeData.resumeContent.basicInfo.birthDate }}</el-descriptions-item>
                      <el-descriptions-item label="电子邮箱">{{ resumeData.resumeContent.basicInfo.email }}</el-descriptions-item>
                      <el-descriptions-item label="联系电话">{{ resumeData.resumeContent.basicInfo.phone }}</el-descriptions-item>
                      <el-descriptions-item label="地址">{{ resumeData.resumeContent.basicInfo.address }}</el-descriptions-item>
                    </el-descriptions>
                  </div>
                </el-card>
                
                <!-- 其他简历部分 -->
                <div class="resume-note">
                  <el-alert
                    title="完整简历内容请查看在线预览或下载简历"
                    type="info"
                    :closable="false"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div v-else class="resume-error">
          <el-empty description="无法加载简历内容" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, View, Check, Bell, Close, CircleCheck } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const baseAPI = import.meta.env.VITE_APP_BASE_API || '/api'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const emptyText = ref('暂无投递记录')

// 统计数据
const statistics = ref({
  totalCount: 0,
  pendingCount: 0,
  viewedCount: 0,
  passedCount: 0,
  interviewCount: 0,
  rejectedCount: 0,
  offeredCount: 0
})

// 搜索条件
const searchForm = ref({
  status: null,
  dateRange: null,
  startDate: null,
  endDate: null
})

// 简历预览
const resumeDialogVisible = ref(false)
const resumeUrl = ref('')
const resumeLoading = ref(false)
const currentResumeId = ref(null)
const resumeData = ref(null)

// 获取投递记录
const fetchApplications = async () => {
  loading.value = true
  try {
    await request.get('/application/student', {
      status: searchForm.value.status,
      startDate: searchForm.value.startDate,
      endDate: searchForm.value.endDate,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
        
        if (tableData.value.length === 0) {
          if (searchForm.value.status !== null || searchForm.value.startDate || searchForm.value.endDate) {
            emptyText.value = '没有符合条件的投递记录'
          } else {
            emptyText.value = '暂无投递记录，快去投递简历吧！'
          }
        }
      }
    })
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    await request.get('/application/statistics', {}, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        statistics.value = res || {}
      }
    })
  } catch (error) {
    console.error('获取统计信息失败', error)
  }
}

// 状态变更
const handleStatusChange = () => {
  currentPage.value = 1
  fetchApplications()
}

// 日期变更
const handleDateChange = () => {
  if (searchForm.value.dateRange) {
    searchForm.value.startDate = searchForm.value.dateRange[0]
    searchForm.value.endDate = searchForm.value.dateRange[1]
  } else {
    searchForm.value.startDate = null
    searchForm.value.endDate = null
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchApplications()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    status: null,
    dateRange: null,
    startDate: null,
    endDate: null
  }
  currentPage.value = 1
  fetchApplications()
}

// 页码变更
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchApplications()
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    0: '待查看',
    1: '已查看',
    2: '通过筛选',
    3: '面试邀请',
    4: '不合适',
    5: '已录用'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: '',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'success'
  }
  return typeMap[status] || ''
}

// 获取状态详细描述
const getStatusDescription = (status) => {
  const descriptionMap = {
    0: '您的简历已投递，等待企业查看',
    1: '企业已查看您的简历',
    2: '恭喜！您的简历已通过初步筛选',
    3: '企业已向您发送面试邀请，请注意查收',
    4: '抱歉，企业认为您与该职位不太匹配',
    5: '恭喜！企业已决定录用您'
  }
  return descriptionMap[status] || '未知状态'
}

// 查看职位
const viewPosition = (positionId) => {
  router.push(`/position/detail/${positionId}`)
}

// 查看简历
const viewResume = (resumeId) => {
  router.push(`/resume/preview/${resumeId}`)
}

// 预览简历
const previewResume = (resumeId) => {
  resumeLoading.value = true
  currentResumeId.value = resumeId
  resumeUrl.value = `${baseAPI}/resume/preview/${resumeId}`
  resumeDialogVisible.value = true
  
  // 获取简历数据
  request.get(`/resume/${resumeId}`, {}, {
    showDefaultMsg: false,
    onSuccess: (res) => {
      resumeData.value = res
      resumeLoading.value = false
    },
    onError: () => {
      resumeLoading.value = false
    }
  })
}

// 关闭简历预览对话框
const handleCloseResumeDialog = () => {
  resumeDialogVisible.value = false
  resumeUrl.value = ''
  currentResumeId.value = null
  resumeData.value = null
}

// 处理简历下载格式选择
const handleDownloadFormat = (format) => {
  if (currentResumeId.value) {
    window.open(`${baseAPI}/resume/export/${currentResumeId.value}?format=${format}`, '_blank')
    ElMessage.success(`${format.toUpperCase()}格式简历下载中...`)
  }
}

// 下载简历
const downloadResume = () => {
  if (currentResumeId.value) {
    window.open(`${baseAPI}/resume/export/${currentResumeId.value}?format=pdf`, '_blank')
    ElMessage.success('简历下载中...')
  }
}

onMounted(() => {
  fetchApplications()
  fetchStatistics()
})
</script>

<style scoped>
.application-list-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.statistics {
  margin: 20px 0;
}

.statistic-card {
  text-align: center;
  padding: 10px;
  transition: all 0.3s;
}

.statistic-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.statistic-title {
  font-size: 14px;
  color: #606266;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-top: 5px;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.company-info {
  display: flex;
  align-items: center;
}

.company-name {
  margin-left: 10px;
  font-weight: bold;
}

.status-tag {
  display: flex;
  align-items: center;
  padding: 6px 10px;
}

.status-tag .el-icon {
  margin-right: 5px;
}

.status-progress {
  margin-top: 8px;
}

.progress-bar {
  display: flex;
  height: 4px;
  width: 100%;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-step {
  flex: 1;
  height: 100%;
  margin: 0 1px;
  background-color: #e0e0e0;
  transition: all 0.3s;
}

.progress-step.active {
  background-color: #67c23a;
}

.progress-step.rejected {
  background-color: #f56c6c;
}

.progress-step.current {
  background-color: #409EFF;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.resume-preview-container {
  min-height: 600px;
}
</style> 
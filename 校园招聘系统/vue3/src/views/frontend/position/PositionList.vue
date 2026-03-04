<template>
  <div class="position-list-container">
    <div class="page-header">
      <h2>职位列表</h2>
      <p>探索合适的职业机会，开启你的职业生涯</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="职位名称">
          <el-input v-model="searchForm.positionName" placeholder="输入职位名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="职位类型">
          <el-select v-model="searchForm.positionType" placeholder="选择职位类型" clearable>
            <el-option v-for="item in positionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="searchForm.workAddress" placeholder="输入工作地点" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 职位列表展示 -->
    <div v-loading="loading" class="position-list">
      <template v-if="positionList.length > 0">
        <el-card v-for="position in positionList" :key="position.id" class="position-card" shadow="hover" @click="viewPositionDetail(position.id)">
          <div class="position-header">
            <div class="position-title">
              <h3>{{ position.positionName }}</h3>
              <div class="position-salary">
                {{ formatSalary(position.salaryMin, position.salaryMax) }}
              </div>
            </div>
            <div class="company-info">
              <el-avatar :size="50" shape="square" :src="position.companyLogo ? baseAPI + position.companyLogo : ''" @error="handleAvatarError">
                <el-icon><OfficeBuilding /></el-icon>
              </el-avatar>
              <div class="company-name">{{ position.companyName }}</div>
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
      <el-empty v-else description="暂无职位数据" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
      />
    </div>

    <!-- 申请职位对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请职位" width="500px">
      <div v-if="selectedPosition" class="apply-dialog-content">
        <div class="apply-position-info">
          <h3>{{ selectedPosition.positionName }}</h3>
          <p>{{ selectedPosition.companyName }}</p>
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
import { OfficeBuilding, Location, School, Timer } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据定义
const positionList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = reactive({
  positionName: '',
  positionType: '',
  workAddress: '',
  companyId: route.query.companyId || null
})

// 申请相关
const applyDialogVisible = ref(false)
const selectedPosition = ref(null)
const resumeList = ref([])
const applyForm = reactive({
  resumeId: '',
  positionId: ''
})

// 职位类型选项
const positionTypeOptions = [
  { value: '全职', label: '全职' },
  { value: '兼职', label: '兼职' },
  { value: '实习', label: '实习' },
  { value: '校招', label: '校招' },
  { value: '社招', label: '社招' }
]

// 获取职位列表
const fetchPositions = () => {
  loading.value = true
  request.get('/position/page', {
    positionName: searchForm.positionName,
    positionType: searchForm.positionType,
    workAddress: searchForm.workAddress,
    status: 1, // 只显示上线的职位
    companyId: searchForm.companyId,
    currentPage: currentPage.value,
    size: pageSize.value
  }, {
    onSuccess: (res) => {
      positionList.value = res.records || []
      total.value = res.total || 0
    },
    onError: (error) => {
      console.error('获取职位列表失败:', error)
    },
    onFinally: () => {
      loading.value = false
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

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchPositions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.positionName = ''
  searchForm.positionType = ''
  searchForm.workAddress = ''
  // 保留companyId，因为可能是从公司详情页跳转过来的
  currentPage.value = 1
  fetchPositions()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchPositions()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPositions()
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

// 文本截断
const truncateText = (text, length) => {
  if (!text) return '暂无描述'
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 页面加载时获取数据
onMounted(() => {
  fetchPositions()
  fetchUserResumes()
})
</script>

<style lang="scss" scoped>
.position-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: var(--font-family-base);
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
  position: relative;

  h2 {
    font-size: 2rem;
    color: #8B7355;
    margin-bottom: 1rem;
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

  p {
    color: #9B8E83;
    font-size: 1rem;
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

.search-filter-container {
  background: rgba(255, 255, 255, 0.7);
  padding: 2rem;
  border-radius: 4px;
  border: 1px solid #D3D3D3;
  margin-bottom: 2rem;
  backdrop-filter: blur(10px);

  :deep(.el-form) {
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
    }

    .el-select {
      width: 150px !important;
      .el-input__wrapper {
        background: transparent;
      }
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
}

.position-list {
  min-height: 400px;
}

.position-card {
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

.position-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.position-title {
  flex: 1;

  h3 {
    margin: 0 0 0.5rem;
    font-size: 1.25rem;
    color: #4A4A4A;
    font-weight: normal;
  }

  .position-salary {
    color: #8B7355;
    font-weight: normal;
    font-size: 1.125rem;
  }
}

.company-info {
  display: flex;
  align-items: center;
  gap: 1rem;

  :deep(.el-avatar) {
    background: rgba(255, 255, 255, 0.5);
    border: 1px solid #D3D3D3;
  }

  .company-name {
    color: #9B8E83;
    max-width: 150px;
    font-size: 0.875rem;
  }
}

.position-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;

  .detail-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #9B8E83;
    font-size: 0.875rem;
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
  }
}

.position-description {
  color: #9B8E83;
  font-size: 0.875rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  text-align: left;
}

.position-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #D3D3D3;

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

.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: center;

  :deep(.el-pagination) {
    .el-pagination__total,
    .el-pagination__jump {
      color: #9B8E83;
    }

    .el-pager li {
      background: transparent;
      color: #9B8E83;
      border: 1px solid #D3D3D3;

      &:hover {
        color: #8B7355;
      }

      &.is-active {
        background: #8B7355;
        color: #fff;
        border-color: #8B7355;
      }
    }

    .btn-prev,
    .btn-next {
      background: transparent;
      color: #9B8E83;
      border: 1px solid #D3D3D3;

      &:hover {
        color: #8B7355;
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
<template>
  <div class="company-list-container">
    <div class="page-header">
      <h2>企业列表</h2>
      <p>发现优质企业，寻找理想职位</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.companyName" placeholder="输入企业名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-select v-model="searchForm.industry" placeholder="选择行业" clearable>
            <el-option v-for="item in industryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 公司列表展示 -->
    <div v-loading="loading" class="company-list">
      <template v-if="companyList.length > 0">
        <el-row :gutter="20">
          <el-col v-for="company in companyList" :key="company.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <el-card class="company-card" shadow="hover" @click="viewCompanyDetail(company.id)">
              <div class="company-logo">
                <el-image 
                  :src="company.companyLogo ? baseAPI + company.companyLogo : '/default-company-logo.png'" 
                  fit="cover"
                  alt="公司logo"
                  :lazy="true">
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><OfficeBuilding /></el-icon>
                    </div>
                  </template>
                </el-image>
                <el-tag v-if="company.verified" type="success" size="small" class="verified-tag">已认证</el-tag>
              </div>
              <div class="company-info">
                <h3 class="company-name">{{ company.companyName }}</h3>
                <div class="company-meta">
                  <span><el-icon><Briefcase /></el-icon> {{ company.industry || '未知行业' }}</span>
                  <span><el-icon><User /></el-icon> {{ company.companySize || '未知规模' }}</span>
                </div>
                <div class="company-address" v-if="company.companyAddress">
                  <el-icon><Location /></el-icon> {{ company.companyAddress }}
                </div>
                <div class="company-intro">{{ truncateText(company.companyIntro, 50) }}</div>
                <div class="view-positions">
                  <el-button type="primary" link @click.stop="viewCompanyPositions(company.id)">
                    查看该企业职位 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      <el-empty v-else description="暂无企业数据" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { OfficeBuilding, Briefcase, User, Location, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据定义
const companyList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const searchForm = reactive({
  companyName: '',
  industry: ''
})

// 行业选项
const industryOptions = [
  { value: '互联网/IT', label: '互联网/IT' },
  { value: '金融', label: '金融' },
  { value: '教育', label: '教育' },
  { value: '医疗健康', label: '医疗健康' },
  { value: '制造业', label: '制造业' },
  { value: '房地产', label: '房地产' },
  { value: '零售/贸易', label: '零售/贸易' },
  { value: '文化/传媒', label: '文化/传媒' },
  { value: '服务业', label: '服务业' },
  { value: '其他', label: '其他' }
]

// 获取企业列表
const fetchCompanies = () => {
  loading.value = true
  request.get('/company/page', {
    companyName: searchForm.companyName,
    industry: searchForm.industry,
    verified: true, // 只显示已认证的企业
    currentPage: currentPage.value,
    size: pageSize.value
  }, {
    onSuccess: (res) => {
      companyList.value = res.records || []
      total.value = res.total || 0
            loading.value = false
    },
    onError: (error) => {
      console.error('获取企业列表失败:', error)
    }
  })
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchCompanies()
}

// 重置搜索
const resetSearch = () => {
  searchForm.companyName = ''
  searchForm.industry = ''
  currentPage.value = 1
  fetchCompanies()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCompanies()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCompanies()
}

// 查看企业详情
const viewCompanyDetail = (id) => {
  try {
    router.push(`/company-detail/${id}`)
  } catch (error) {
    console.error('导航失败:', error)
    // 可以添加一个提示
    ElMessage.error('页面导航失败，请稍后再试')
  }
}


// 查看企业职位
const viewCompanyPositions = (id) => {
  router.push(`/positions?companyId=${id}`)
}

// 文本截断
const truncateText = (text, length) => {
  if (!text) return '暂无简介'
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 页面加载时获取数据
onMounted(() => {
  fetchCompanies()
})
</script>

<style lang="scss" scoped>
.company-list-container {
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

.company-list {
  min-height: 400px;
}

.company-card {
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

.company-logo {
  position: relative;
  // height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 1rem;
  border-bottom: 1px solid #D3D3D3;
  padding: 1rem;

  .el-image {
    // max-height: 100px;
    max-width: 100%;
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
  height: 100px;
  background: rgba(139, 115, 85, 0.05);
  color: #9B8E83;

  .el-icon {
    font-size: 2.5rem;
  }
}

.company-info {
  padding: 0 1rem 1rem;
}

.company-name {
  font-size: 1.25rem;
  margin: 0 0 1rem;
  color: #4A4A4A;
  font-weight: normal;
}

.company-meta {
  display: flex;
  justify-content: space-between;
  color: #9B8E83;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;

  span {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }
}

.company-address {
  display: flex;
  align-items: center;
  color: #9B8E83;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;
  gap: 0.25rem;
}

.company-intro {
  color: #9B8E83;
  font-size: 0.875rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.view-positions {
  text-align: right;
  border-top: 1px solid #D3D3D3;
  padding-top: 0.75rem;

  .el-button {
    color: #8B7355;

    &:hover {
      color: darken(#8B7355, 10%);
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
</style> 
<template>
  <div class="home">
    <!-- 顶部banner -->
    <div class="banner">
      <div class="banner-content">
        <h1>校园招聘系统</h1>
        <p>连接学生与企业的桥梁，助力职业发展</p>
        <!-- <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索职位、公司..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div> -->
      </div>
    </div>

    <!-- 快速入口 -->
    <div class="quick-access">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="quick-card" @click="router.push('/positions')">
            <div class="quick-icon">
              <el-icon><Suitcase /></el-icon>
            </div>
            <h3>浏览职位</h3>
            <p>探索最新校园招聘职位</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="quick-card" @click="router.push('/companies')">
            <div class="quick-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <h3>查看企业</h3>
            <p>了解优质企业信息</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="quick-card" @click="handleResumeClick">
            <div class="quick-icon">
              <el-icon><Document /></el-icon>
            </div>
            <h3>管理简历</h3>
            <p>创建和管理个人简历</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热门职位 -->
    <div class="section">
      <div class="section-header">
        <h2>最新上新</h2>
        <el-button type="primary" link @click="router.push('/positions')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-row :gutter="20" v-loading="positionsLoading">
        <el-col v-for="position in latestPositions" :key="position.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="position-card" shadow="hover" @click="router.push(`/position/detail/${position.id}`)">
            <div class="position-header">
              <h3 class="position-name">{{ position.positionName }}</h3>
              <div class="position-salary">{{ formatSalary(position.salaryMin, position.salaryMax) }}</div>
            </div>
            <div class="company-info">
              <span class="company-name">{{ position.companyName }}</span>
            </div>
            <div class="position-meta">
              <span><el-icon><Location /></el-icon> {{ position.workAddress || '地点未指定' }}</span>
              <span><el-icon><School /></el-icon> {{ position.educationRequirement || '学历不限' }}</span>
            </div>
            <div class="position-tags">
              <el-tag size="small" type="info">{{ position.positionType || '全职' }}</el-tag>
              <span class="position-time"><el-icon><Timer /></el-icon> {{ formatPublishTime(position.createTime) }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 推荐企业 -->
    <!-- <div class="section">
      <div class="section-header">
        <h2>推荐企业</h2>
        <el-button type="primary" link @click="router.push('/companies')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-row :gutter="20" v-loading="companiesLoading">
        <el-col v-for="company in recommendedCompanies" :key="company.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="company-card" shadow="hover" @click="router.push(`/company-detail/${company.id}`)">
            <div class="company-logo">
              <el-image 
                :src="company.companyLogo ? baseAPI + company.companyLogo : '/default-company-logo.png'" 
                fit="cover"
                alt="公司logo">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><OfficeBuilding /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <h3 class="company-name">{{ company.companyName }}</h3>
            <div class="company-meta">
              <span>{{ company.industry || '未知行业' }}</span>
              <span>{{ company.companySize || '未知规模' }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div> -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { Search, Suitcase, OfficeBuilding, Document, ArrowRight, Location, School, Timer } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据定义
const searchKeyword = ref('')
const latestPositions = ref([])
const recommendedCompanies = ref([])
const positionsLoading = ref(false)
const companiesLoading = ref(false)

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/positions',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

// 简历点击处理
const handleResumeClick = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
  } else if (userStore.isStudent) {
    router.push('/resume')
  } else {
    router.push('/profile')
  }
}

// 获取最新职位
const fetchLatestPositions = () => {
  positionsLoading.value = true
  request.get('/position/page', {
    status: 1,
    currentPage: 1,
    size: 8,
    sortBy: 'createTime'
  }, {
    onSuccess: (res) => {
      latestPositions.value = res.records || []
    },
    onError: (error) => {
      console.error('获取最新职位失败:', error)
    },
    onFinally: () => {
      positionsLoading.value = false
    }
  })
}

// 获取推荐企业
const fetchRecommendedCompanies = () => {
  companiesLoading.value = true
  request.get('/company/page', {
    verified: true,
    currentPage: 1,
    size: 4
  }, {
    onSuccess: (res) => {
      recommendedCompanies.value = res.records || []
    },
    onError: (error) => {
      console.error('获取推荐企业失败:', error)
    },
    onFinally: () => {
      companiesLoading.value = false
    }
  })
}

// 格式化薪资
const formatSalary = (min, max) => {
  if (!min && !max) return '薪资面议'
  if (min && max) return `${min}K-${max}K`
  if (min) return `${min}K以上`
  return `${max}K以下`
}

// 格式化发布时间
const formatPublishTime = (dateTime) => {
  if (!dateTime) return '未知时间'
  try {
    const date = new Date(dateTime)
    const now = new Date()
    const diff = now - date
    
    // 小于24小时显示"x小时前"
    if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000))
      return hours > 0 ? `${hours}小时前` : '刚刚'
    }
    
    // 小于30天显示"x天前"
    if (diff < 30 * 24 * 60 * 60 * 1000) {
      const days = Math.floor(diff / (24 * 60 * 60 * 1000))
      return `${days}天前`
    }
    
    // 其他情况显示年月日
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } catch (e) {
    return dateTime
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchLatestPositions()
  fetchRecommendedCompanies()
})
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  font-family: var(--font-family-base);
}

.banner {
  background: linear-gradient(135deg, #E8E6E1 0%, #F9F6F2 100%);
  color: #4A4A4A;
  padding: 4rem 2rem;
  text-align: center;
  margin-bottom: 4rem;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      linear-gradient(to right, rgba(0,0,0,0.02) 1px, transparent 1px) 0 0 / 20px 20px,
      linear-gradient(to bottom, rgba(0,0,0,0.02) 1px, transparent 1px) 0 0 / 20px 20px;
  }
}

.banner-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;

  h1 {
    font-size: 2.5rem;
    margin-bottom: 1.5rem;
    font-weight: normal;
    color: #8B7355;
    position: relative;
    display: inline-block;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 100px;
      height: 100px;
      transform: translate(-50%, -50%);
      border: 1px solid rgba(139, 115, 85, 0.1);
      border-radius: 50%;
      animation: ink-spread 3s ease-out infinite;
    }
  }

  p {
    font-size: 1.25rem;
    margin-bottom: 2rem;
    color: #9B8E83;
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

.search-box {
  max-width: 600px;
  margin: 0 auto;

  :deep(.el-input) {
    .el-input__wrapper {
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid #D3D3D3;
      box-shadow: none;

      &:hover {
        border-color: #9B8E83;
      }

      &.is-focus {
        border-color: #8B7355;
        background: #fff;
      }
}

    .el-input-group__append {
      background: #8B7355;
      border-color: #8B7355;
      color: #fff;

      .el-button {
        color: #fff;
        
        &:hover {
          background: darken(#8B7355, 10%);
        }
      }
    }
  }
}

.quick-access {
  max-width: 1200px;
  margin: 0 auto 4rem;
  padding: 0 2rem;
}

.quick-card {
  height: 100%;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #D3D3D3;
  border-radius: 4px;

  &:hover {
    border-color: #8B7355;
  transform: translateY(-5px);
}

.quick-icon {
    font-size: 2.5rem;
    color: #8B7355;
    margin-bottom: 1rem;
    opacity: 0.8;
}

  h3 {
    font-size: 1.25rem;
    margin: 0 0 0.5rem;
    color: #4A4A4A;
    font-weight: normal;
}

  p {
    color: #9B8E83;
  margin: 0;
  }
}

.section {
  max-width: 1200px;
  margin: 0 auto 4rem;
  padding: 0 2rem;

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
    margin-bottom: 2rem;
    border-bottom: 1px solid #D3D3D3;
    padding-bottom: 1rem;

    h2 {
      font-size: 1.5rem;
      color: #4A4A4A;
  margin: 0;
      font-weight: normal;
    }

    .el-button {
      color: #8B7355;
      
      &:hover {
        color: darken(#8B7355, 10%);
      }
    }
  }
}

.position-card, .company-card {
  height: 100%;
  margin-bottom: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #D3D3D3;
  border-radius: 4px;

  &:hover {
    border-color: #8B7355;
  transform: translateY(-5px);
  }
}

.position-header {
  margin-bottom: 1rem;

.position-name {
    font-size: 1.125rem;
    margin: 0 0 0.5rem;
    color: #4A4A4A;
    font-weight: normal;
}

.position-salary {
    color: #8B7355;
    font-weight: normal;
  }
}

.company-info {
  margin-bottom: 1rem;

.company-name {
    color: #9B8E83;
  }
}

.position-meta {
  display: flex;
  justify-content: space-between;
  color: #9B8E83;
  font-size: 0.875rem;
  margin-bottom: 1rem;

  span {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }
}

.position-tags {
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;

  :deep(.el-tag) {
    background: rgba(139, 115, 85, 0.1);
    border-color: transparent;
    color: #8B7355;
}

  .position-time {
    color: #9B8E83;
    font-size: 0.75rem;
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }
}

.company-logo {
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 1rem;
  padding: 1rem;

  .el-image {
  max-height: 80px;
  max-width: 100%;
    opacity: 0.9;
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 80px;
    background-color: rgba(139, 115, 85, 0.05);
    color: #9B8E83;

    .el-icon {
      font-size: 2.5rem;
    }
  }
}

.company-meta {
  display: flex;
  justify-content: space-between;
  color: #9B8E83;
  font-size: 0.875rem;
  padding: 0.5rem 0;
  border-top: 1px solid #D3D3D3;
  margin-top: 1rem;
}
</style>



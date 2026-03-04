<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="welcome-header">
          <el-avatar :size="64" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0) }}
          </el-avatar>
          <div class="welcome-info">
            <h2>欢迎回来, {{ userInfo?.name || userInfo?.username }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>
      </template>
      <div class="role-info">
        <el-tag>{{ roleLabel }}</el-tag>
      </div>
    </el-card>

    <!-- 统计数据卡片 -->
    <div class="stats-cards">
      <el-card class="stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <i class="el-icon-user"></i>
            <span>学生总数</span>
          </div>
        </template>
        <div class="stats-number">{{ dashboardStats.totalStudents || 0 }}</div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <i class="el-icon-office-building"></i>
            <span>企业总数</span>
          </div>
        </template>
        <div class="stats-number">{{ dashboardStats.totalCompanies || 0 }}</div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <i class="el-icon-suitcase"></i>
            <span>职位总数</span>
          </div>
        </template>
        <div class="stats-number">{{ dashboardStats.totalPositions || 0 }}</div>
      </el-card>

      <el-card class="stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <i class="el-icon-document"></i>
            <span>申请总数</span>
          </div>
        </template>
        <div class="stats-number">{{ dashboardStats.totalApplications || 0 }}</div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>最近1年申请趋势</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>职位类别分布</span>
          </div>
        </template>
        <div ref="categoryChartRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import * as echarts from 'echarts'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 图表引用
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
let trendChart = null
let categoryChart = null

// 仪表盘数据
const dashboardStats = ref({
  totalStudents: 0,
  totalCompanies: 0,
  totalPositions: 0,
  totalApplications: 0,
  applicationTrend: {},
  positionCategories: {}
})

// 获取仪表盘数据
const fetchDashboardStats = async () => {
  try {
    const { data } = await request.get('/dashboard/stats',null,{
      onSuccess: (res) => {
        dashboardStats.value = res
        setTimeout(() => {
          initCharts()
        }, 0)
      },
      onError: (err) => {
        console.error('获取仪表盘数据失败:', err)
      }
    })
    
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  // 初始化趋势图表
  if (trendChartRef.value) {
    if (trendChart) {
      trendChart.dispose()
    }
    trendChart = echarts.init(trendChartRef.value)
    const trendData = dashboardStats.value.applicationTrend || {}
    const dates = Object.keys(trendData).sort()
    const values = dates.map(date => trendData[date])

    const option = {
      title: {
        text: '近一年申请趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          const data = params[0]
          return `${data.name}<br/>${data.seriesName}：${data.value}个`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '8%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          rotate: 45,
          formatter: function(value) {
            // 将 yyyy-MM 格式转换为 yyyy年MM月
            const [year, month] = value.split('-')
            return `${year}年${month}月`
          }
        }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        name: '申请数量',
        nameTextStyle: {
          padding: [0, 0, 0, 30]
        }
      },
      series: [{
        name: '申请数量',
        data: values,
        type: 'line',
        smooth: true,
        symbolSize: 8,
        areaStyle: {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(0, 188, 212, 0.5)'
            },
            {
              offset: 1,
              color: 'rgba(0, 188, 212, 0.1)'
            }
          ])
        },
        itemStyle: {
          color: '#00bcd4',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            borderWidth: 4
          }
        }
      }]
    }
    trendChart.setOption(option)
  }

  // 初始化类别分布图表
  if (categoryChartRef.value) {
    if (categoryChart) {
      categoryChart.dispose()
    }
    categoryChart = echarts.init(categoryChartRef.value)
    const categoryData = dashboardStats.value.positionCategories || {}
    const categories = Object.entries(categoryData).map(([name, value]) => ({
      name,
      value
    }))

    const option = {
      title: {
        text: '职位类别分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [{
        name: '职位类别',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}个'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        data: categories
      }]
    }
    categoryChart.setOption(option)
  }
}

// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'COMPANY': '企业用户',
    'STUDENT': '学生用户'
  }
  return roleMap[userInfo.value?.roleCode] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : ''
})

// 当前时间
const currentTime = ref('')
let timeInterval = null

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  if (trendChart) {
    trendChart.resize()
  }
  if (categoryChart) {
    categoryChart.resize()
  }
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 60000)
  fetchDashboardStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
  }
  if (categoryChart) {
    categoryChart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 24px;
  min-height: calc(100vh - 60px);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise' x='0' y='0'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100' height='100' filter='url(%23noise)' opacity='0.05'/%3E%3C/svg%3E");
    opacity: 0.1;
    pointer-events: none;
  }

  .welcome-card {
   
    margin: 0 auto 20px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    background: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    animation: unfold 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    transform-origin: top center;
    
    &:hover {
      transform: translateY(-4px) scale(1.01);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    :deep(.el-card__header) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      background: rgba(255, 255, 255, 0.5);
      padding: 20px;
    }
    
    .welcome-header {
      display: flex;
      align-items: center;
      gap: 24px;
      
      .el-avatar {
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        border: 4px solid rgba(255, 255, 255, 0.9);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        
        &:hover {
          transform: scale(1.1) rotate(5deg);
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        }
      }
      
      .welcome-info {
        h2 {
          margin: 0 0 12px 0;
          font-size: 28px;
          font-family: 'Avenir Next', sans-serif;
          font-weight: 600;
          background: linear-gradient(135deg, #006064, #00bcd4);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          letter-spacing: 0.5px;
        }
        
        p {
          margin: 0;
          color: #37474f;
          font-family: 'Avenir Next', sans-serif;
          font-size: 15px;
          opacity: 0.8;
        }
      }
    }
    
    .role-info {
      margin-top: 20px;
      display: flex;
      gap: 8px;
      
      :deep(.el-tag) {
        padding: 8px 16px;
        border-radius: 8px;
        font-family: 'Avenir Next', sans-serif;
        font-size: 14px;
        background: rgba(178, 235, 242, 0.3);
        border: 1px solid rgba(0, 96, 100, 0.1);
        color: #006064;
        transition: all 0.3s;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
          background: rgba(178, 235, 242, 0.5);
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .stats-card {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 12px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      }

      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        color: #37474f;

        i {
          font-size: 20px;
          color: #00bcd4;
        }
      }

      .stats-number {
        font-size: 36px;
        font-weight: 600;
        color: #006064;
        text-align: center;
        margin: 16px 0;
        font-family: 'Avenir Next', sans-serif;
      }
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
    gap: 24px;
    margin-top: 24px;

    .chart-card {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 12px;
      
      .chart {
        height: 400px;
        padding: 16px;
      }
    }
  }
}

@keyframes unfold {
  0% {
    transform: perspective(1000px) rotateX(-30deg) translateY(-50px);
    opacity: 0;
  }
  100% {
    transform: perspective(1000px) rotateX(0deg) translateY(0);
    opacity: 1;
  }
}
</style> 
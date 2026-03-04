<template>
  <div class="company-list-container">
    <div class="page-header">
      <h2>企业管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshCompanies">刷新</el-button>
      </div>
    </div>

    <!-- 搜索表单 - 仅管理员可见 -->
    <el-card class="search-card" v-if="userStore.isAdmin">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.companyName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-input v-model="searchForm.industry" placeholder="请输入所属行业" clearable />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="searchForm.verified" placeholder="请选择认证状态" clearable>
            <el-option label="已认证" :value="true" />
            <el-option label="未认证" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 企业列表表格 -->
    <el-card class="table-card">
      <!-- 管理员视图 - 所有企业列表 -->
      <template v-if="userStore.isAdmin">
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          style="width: 100%"
          row-key="id"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="companyName" label="企业名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="industry" label="所属行业" min-width="120" show-overflow-tooltip />
          <el-table-column prop="companySize" label="企业规模" min-width="120" show-overflow-tooltip />
          <el-table-column prop="companyAddress" label="企业地址" min-width="180" show-overflow-tooltip />
          <el-table-column label="认证状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.verified ? 'success' : 'danger'">
                {{ scope.row.verified ? '已认证' : '未认证' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="scope">
              {{ DateUtils.formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewCompanyDetail(scope.row)">查看</el-button>
              <el-button
                :type="scope.row.verified ? 'danger' : 'success'"
                size="small"
                @click="toggleVerifyStatus(scope.row)"
              >
                {{ scope.row.verified ? '取消认证' : '认证' }}
              </el-button>
              <el-popconfirm
                title="确定删除该企业信息吗？"
                @confirm="deleteCompany(scope.row)"
              >
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>

      <!-- 企业用户视图 - 仅显示自己的企业信息 -->
      <template v-else-if="userStore.isCompany">
        <div v-if="companyInfo" class="company-detail">
          <div class="company-header">
            <div class="company-logo-container">
              <img v-if="companyInfo.companyLogo" :src="getImageUrl(companyInfo.companyLogo)" class="company-logo" alt="企业Logo" />
              <div v-else class="empty-logo">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
            </div>
            <div class="company-title">
              <h3>{{ companyInfo.companyName || '未命名企业' }}</h3>
              <el-tag :type="companyInfo.verified ? 'success' : 'warning'">
                {{ companyInfo.verified ? '已认证' : '未认证' }}
              </el-tag>
            </div>
          </div>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="所属行业">{{ companyInfo.industry || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="企业规模">{{ companyInfo.companySize || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="企业地址">{{ companyInfo.companyAddress || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ DateUtils.formatDate(companyInfo.createTime) }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="company-intro">
            <h4>企业简介</h4>
            <div class="intro-content">{{ companyInfo.companyIntro || '暂无企业简介' }}</div>
          </div>
          
          <div class="company-actions">
            <el-button type="primary" @click="editCompanyInfo">编辑企业信息</el-button>
          </div>
        </div>
        
        <el-empty v-else description="您还未完善企业信息">
          <el-button type="primary" @click="router.push('/company/profile')">完善企业信息</el-button>
        </el-empty>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'
import { useUserStore } from '@/store/user'
import { OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const companyInfo = ref(null)

// 搜索表单
const searchForm = reactive({
  companyName: '',
  industry: '',
  verified: null
})

// 获取企业列表 - 管理员
const fetchCompanies = async () => {
  if (!userStore.isAdmin) {
    await fetchCompanyInfo();
    return;
  }
  
  loading.value = true
  try {
    await request.get('/company/page', {
      companyName: searchForm.companyName,
      industry: searchForm.industry,
      verified: searchForm.verified,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 获取企业信息 - 企业用户
const fetchCompanyInfo = async () => {
  if (!userStore.isCompany) return;
  
  loading.value = true
  try {
    await request.get(`/company/user/${userStore.userInfo.id}`, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        companyInfo.value = data
      },
      onError: () => {
        companyInfo.value = null
      }
    })
  } finally {
    loading.value = false
  }
}

// 查询按钮点击事件
const handleSearch = () => {
  currentPage.value = 1
  fetchCompanies()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.companyName = ''
  searchForm.industry = ''
  searchForm.verified = null
  handleSearch()
}

// 刷新企业列表
const refreshCompanies = () => {
  fetchCompanies()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCompanies()
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCompanies()
}

// 查看企业详情
const viewCompanyDetail = (row) => {
  router.push(`/back/company/detail/${row.id}`)
}

// 编辑企业信息
const editCompanyInfo = () => {
  router.push('/company/profile')
}

// 切换企业认证状态
const toggleVerifyStatus = async (row) => {
  const newStatus = !row.verified
  const actionText = newStatus ? '认证' : '取消认证'
  
  try {
    await request.put(`/company/${row.id}/verify`, null, {
      params: { verified: newStatus },
      successMsg: `${actionText}成功`,
      onSuccess: () => {
        row.verified = newStatus
      }
    })
  } catch (error) {
    console.error('更新认证状态失败', error)
  }
}

// 删除企业
const deleteCompany = async (row) => {
  try {
    await request.delete(`/company/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchCompanies()
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

// 页面加载时获取企业列表
onMounted(() => {
  fetchCompanies()
})
</script>

<style lang="scss" scoped>
.company-list-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 20px;
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  // 企业用户视图样式
  .company-detail {
    padding: 20px;
    
    .company-header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .company-logo-container {
        width: 100px;
        height: 100px;
        margin-right: 20px;
        border-radius: 8px;
        overflow: hidden;
        background-color: #f8f8f8;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .company-logo {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
        
        .empty-logo {
          font-size: 40px;
          color: #dcdcdc;
        }
      }
      
      .company-title {
        display: flex;
        align-items: center;
        
        h3 {
          margin: 0 10px 0 0;
          font-size: 24px;
        }
      }
    }
    
    .company-intro {
      margin-top: 20px;
      
      h4 {
        margin: 0 0 10px;
        font-size: 16px;
        font-weight: 500;
      }
      
      .intro-content {
        background-color: #f8f8f8;
        padding: 15px;
        border-radius: 4px;
        line-height: 1.6;
        min-height: 100px;
        white-space: pre-wrap;
      }
    }
    
    .company-actions {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 
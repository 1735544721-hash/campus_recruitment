<template>
  <div class="position-list-container">
    <!-- 头部 -->
    <div class="page-header">
      <h2>职位管理</h2>
      <div class="header-actions">
        <el-button 
          type="primary" 
          size="large" 
        
          class="publish-btn" 
          @click="handleAdd" 
          v-if="isCompanyUser"
        >
          发布新职位
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" ref="searchFormRef" inline>
        <el-form-item label="职位名称">
          <el-input v-model="searchForm.positionName" placeholder="输入职位名称" clearable />
        </el-form-item>
        <el-form-item label="职位类型">
          <el-select v-model="searchForm.positionType" placeholder="选择职位类型" clearable>
            <el-option v-for="item in positionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="searchForm.workAddress" placeholder="输入工作地点" clearable />
        </el-form-item>
        <el-form-item label="状态" v-if="isAdmin">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 职位列表 -->
    <el-card class="table-card" v-loading="loading">
      <div v-if="tableData.length === 0 && !loading && isCompanyUser" class="empty-positions">
        <el-empty description="您还没有发布任何职位">
          <template #extra>
            <el-button type="primary" @click="handleAdd">立即发布职位</el-button>
          </template>
        </el-empty>
      </div>
      
      <el-table v-else :data="tableData" stripe style="width: 100%">
        <el-table-column label="企业信息" width="200">
          <template #default="{ row }">
            <div class="company-info">
              <el-avatar :size="40" :src="getCompanyLogoUrl(row.companyLogo)" />
              <span class="company-name">{{ row.companyName || '未知企业' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="positionName" label="职位名称" min-width="120" />
        <el-table-column prop="positionType" label="类型" width="80" />
        <el-table-column label="薪资范围" width="120">
          <template #default="{ row }">
            {{ row.salaryMin / 1000 }}k-{{ row.salaryMax / 1000 }}k
          </template>
        </el-table-column>
        <el-table-column prop="workAddress" label="工作地点" min-width="120" show-overflow-tooltip />
        <el-table-column prop="educationRequirement" label="学历要求" width="80" />
        <el-table-column prop="experienceRequirement" label="经验要求" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ DateUtils.formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            
            <!-- 管理员可以审核职位 -->
            <template v-if="isAdmin && row.status === 2">
              <el-button type="success" link @click="handleApprove(row)">审核通过</el-button>
              <el-button type="danger" link @click="handleReject(row)">审核拒绝</el-button>
            </template>
            
            <!-- 管理员可以下线职位 -->
            <template v-if="isAdmin && row.status === 1">
              
              <el-button type="warning" link @click="handleOffline(row)">下线</el-button>
            </template>
            
            <!-- 企业用户可以编辑和删除自己的职位 -->
            <!-- <template v-if="isCompanyUser"> -->
              <el-button type="primary" link @click="handleEdit(row)" >编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <!-- </template> -->
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'
import { getPositionPage, updatePositionStatus, deletePosition } from '@/api/position'
import { useUserStore } from '@/store/user'
import defaultLogo from '@/assets/logo.png'

const router = useRouter()
const userStore = useUserStore()

// 用户权限
const isAdmin = computed(() => userStore.isAdmin)
const isCompanyUser = computed(() => userStore.isCompany)
const companyId = computed(() => userStore.companyInfo?.id)
// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchFormRef = ref(null)
const searchForm = reactive({
  positionName: '',
  positionType: '',
  workAddress: '',
  status: ''
})

// 选项数据
const positionTypeOptions = [
  { value: '全职', label: '全职' },
  { value: '兼职', label: '兼职' },
  { value: '实习', label: '实习' },
  { value: '校招', label: '校招' }
]

const statusOptions = [
  { value: 0, label: '下线' },
  { value: 1, label: '上线' },
  { value: 2, label: '审核中' }
]

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

// 查询职位列表
const fetchPositions = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      currentPage: currentPage.value,
      size: pageSize.value,
      companyId: isCompanyUser.value && !isAdmin.value ? companyId.value : undefined
    }
    
    await getPositionPage(params, {
      onSuccess: (data) => {
        tableData.value = data.records || []
        total.value = data.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPositions()
}

// 重置搜索
const resetSearch = () => {
  searchFormRef.value.resetFields()
  currentPage.value = 1
  fetchPositions()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPositions()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPositions()
}

// 查看职位详情
const handleView = (row) => {
  router.push(`/back/position/detail/${row.id}`)
}

// 新增职位
const handleAdd = () => {
  router.push('/back/position/form')
}

// 编辑职位
const handleEdit = (row) => {
  router.push(`/back/position/form/${row.id}`)
}

// 删除职位
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定删除该职位吗？删除后不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deletePosition(row.id, {
      successMsg: '职位删除成功',
      onSuccess: () => {
        fetchPositions()
      }
    })
  }).catch(() => {})
}

// 审核通过
const handleApprove = (row) => {
  updatePositionStatus(row.id, { status: 1 }, {
    successMsg: '审核通过成功',
    onSuccess: () => {
      fetchPositions()
    }
  })
}

// 审核拒绝
const handleReject = (row) => {
  updatePositionStatus(row.id, { status: 0 }, {
    successMsg: '审核拒绝成功',
    onSuccess: () => {
      fetchPositions()
    }
  })
}

// 下线职位
const handleOffline = (row) => {
  updatePositionStatus(row.id, { status: 0 }, {
    successMsg: '职位下线成功',
    onSuccess: () => {
      fetchPositions()
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  fetchPositions()
})
</script>

<style lang="scss" scoped>
.position-list-container {
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
    
    .header-actions {
      .publish-btn {
        font-weight: bold;
        box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
        transition: all 0.3s;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 15px 0 rgba(0,0,0,.15);
        }
      }
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .company-info {
    display: flex;
    align-items: center;
    
    .company-name {
      margin-left: 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .empty-positions {
    padding: 40px 0;
    text-align: center;
  }
}
</style> 
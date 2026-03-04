<template>
  <div class="application-management-container">
    <div class="page-header">
      <h2>简历投递管理</h2>
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
        <el-form-item label="职位">
          <el-select v-model="searchForm.positionId" placeholder="选择职位" clearable @change="handleSearch">
            <el-option
              v-for="item in positionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="待查看" :value="0" />
            <el-option label="已查看" :value="1" />
            <el-option label="通过筛选" :value="2" />
            <el-option label="面试邀请" :value="3" />
            <el-option label="不合适" :value="4" />
            <el-option label="已录用" :value="5" />
          </el-select>
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
        <el-form-item label="学生信息">
          <el-input v-model="searchForm.keyword" placeholder="姓名/学号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="application-list">
      <div class="table-operations">
        <el-button-group>
          <el-button type="primary" :disabled="!hasSelected" @click="batchMarkAsViewed">
            <el-icon><View /></el-icon>标记已查看
          </el-button>
          <el-button type="success" :disabled="!hasSelected" @click="batchMarkAsPassed">
            <el-icon><Check /></el-icon>通过筛选
          </el-button>
          <el-button type="danger" :disabled="!hasSelected" @click="batchMarkAsRejected">
            <el-icon><Close /></el-icon>标记不合适
          </el-button>
        </el-button-group>
        <span v-if="hasSelected" class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="学生信息" width="180">
          <template #default="{ row }">
            <div class="student-info" @click="viewStudentDetail(row)">
              <div class="student-name">{{ row.studentName }}</div>
              <div class="student-no">学号：{{ row.studentNo }}</div>
  
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="positionName" label="职位名称" />
        
        <el-table-column prop="resumeName" label="简历名称" />
        
        <el-table-column label="投递状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
              <el-icon v-if="row.status === 0"><Clock /></el-icon>
              <el-icon v-else-if="row.status === 1"><View /></el-icon>
              <el-icon v-else-if="row.status === 2"><Check /></el-icon>
              <el-icon v-else-if="row.status === 3"><Bell /></el-icon>
              <el-icon v-else-if="row.status === 4"><Close /></el-icon>
              <el-icon v-else-if="row.status === 5"><CircleCheck /></el-icon>
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="投递时间" width="180" />
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="previewResume(row.resumeId)">查看简历</el-button>
            <el-dropdown @command="(command) => handleStatusChange(row.id, command)">
              <el-button type="primary" link>
                更改状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1" :disabled="row.status === 1">标记已查看</el-dropdown-item>
                  <el-dropdown-item :command="2" :disabled="row.status === 2">通过筛选</el-dropdown-item>
                  <el-dropdown-item :command="3" :disabled="row.status === 3" @click.native.stop="showInterviewInvitation(row)">发送面试邀请</el-dropdown-item>
                  <el-dropdown-item :command="4" :disabled="row.status === 4">标记不合适</el-dropdown-item>
                  <el-dropdown-item :command="5" :disabled="row.status === 5">标记已录用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
            
            <!-- 求职意向 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.jobIntention">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><Aim /></el-icon>
                  <span>求职意向</span>
                </div>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="期望职位">{{ resumeData.resumeContent.jobIntention.position }}</el-descriptions-item>
                <el-descriptions-item label="期望行业">{{ resumeData.resumeContent.jobIntention.industry }}</el-descriptions-item>
                <el-descriptions-item label="期望地点">{{ resumeData.resumeContent.jobIntention.location }}</el-descriptions-item>
                <el-descriptions-item label="薪资期望">{{ resumeData.resumeContent.jobIntention.salaryExpectation }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
            
            <!-- 教育经历 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.education && resumeData.resumeContent.education.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><School /></el-icon>
                  <span>教育经历</span>
                </div>
              </template>
              <div v-for="(edu, index) in resumeData.resumeContent.education" :key="index" class="resume-item">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="学校">{{ edu.school }}</el-descriptions-item>
                  <el-descriptions-item label="专业">{{ edu.major }}</el-descriptions-item>
                  <el-descriptions-item label="学历">{{ edu.degree }}</el-descriptions-item>
                  <el-descriptions-item label="时间">{{ edu.startDate }} - {{ edu.endDate }}</el-descriptions-item>
                  <el-descriptions-item label="GPA" v-if="edu.gpa">{{ edu.gpa }}</el-descriptions-item>
                  <el-descriptions-item label="描述" :span="2" v-if="edu.description">{{ edu.description }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
            
            <!-- 工作经历 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.workExperience && resumeData.resumeContent.workExperience.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><Briefcase /></el-icon>
                  <span>工作经历</span>
                </div>
              </template>
              <div v-for="(work, index) in resumeData.resumeContent.workExperience" :key="index" class="resume-item">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="公司">{{ work.company }}</el-descriptions-item>
                  <el-descriptions-item label="职位">{{ work.position }}</el-descriptions-item>
                  <el-descriptions-item label="时间">{{ work.startDate }} - {{ work.endDate }}</el-descriptions-item>
                  <el-descriptions-item label="描述" :span="2" v-if="work.description">{{ work.description }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
            
            <!-- 项目经验 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.projectExperience && resumeData.resumeContent.projectExperience.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><Connection /></el-icon>
                  <span>项目经验</span>
                </div>
              </template>
              <div v-for="(project, index) in resumeData.resumeContent.projectExperience" :key="index" class="resume-item">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="项目名称">{{ project.name }}</el-descriptions-item>
                  <el-descriptions-item label="担任角色">{{ project.role }}</el-descriptions-item>
                  <el-descriptions-item label="时间">{{ project.startDate }} - {{ project.endDate }}</el-descriptions-item>
                  <el-descriptions-item label="技术栈" v-if="project.techStack">{{ project.techStack }}</el-descriptions-item>
                  <el-descriptions-item label="项目链接" v-if="project.link">{{ project.link }}</el-descriptions-item>
                  <el-descriptions-item label="描述" :span="2" v-if="project.description">{{ project.description }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
            
            <!-- 技能 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.skills && resumeData.resumeContent.skills.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><SetUp /></el-icon>
                  <span>专业技能</span>
                </div>
              </template>
              <el-table :data="resumeData.resumeContent.skills" border style="width: 100%">
                <el-table-column prop="name" label="技能名称" />
                <el-table-column label="熟练度" width="300">
                  <template #default="{ row }">
                    <el-progress :percentage="row.proficiency * 10" :format="p => `${p}%`" />
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" />
              </el-table>
            </el-card>
            
            <!-- 证书 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.certificates && resumeData.resumeContent.certificates.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><Medal /></el-icon>
                  <span>证书</span>
                </div>
              </template>
              <el-table :data="resumeData.resumeContent.certificates" border style="width: 100%">
                <el-table-column prop="name" label="证书名称" />
                <el-table-column prop="issueDate" label="获得日期" />
                <el-table-column prop="issuingOrganization" label="颁发机构" />
                <el-table-column prop="description" label="描述" />
              </el-table>
            </el-card>
            
            <!-- 语言能力 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.languages && resumeData.resumeContent.languages.length > 0">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>语言能力</span>
                </div>
              </template>
              <el-table :data="resumeData.resumeContent.languages" border style="width: 100%">
                <el-table-column prop="name" label="语言" />
                <el-table-column prop="proficiency" label="熟练度" />
                <el-table-column prop="description" label="描述" />
              </el-table>
            </el-card>
            
            <!-- 自我评价 -->
            <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.selfEvaluation">
              <template #header>
                <div class="resume-section-header">
                  <el-icon><User /></el-icon>
                  <span >自我评价</span>
                </div>
              </template>
              <div class="self-evaluation">
                {{ resumeData.resumeContent.selfEvaluation }}
              </div>
            </el-card>
          </div>
        </div>
        <div v-else class="resume-error">
          <el-empty description="无法加载简历内容" />
        </div>
      </div>
    </el-dialog>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      title="学生详情"
      width="50%"
      destroy-on-close
    >
      <div v-loading="studentLoading" class="student-detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentStudent.user?.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ currentStudent.college }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ currentStudent.education }}</el-descriptions-item>
          <el-descriptions-item label="毕业年份">{{ currentStudent.graduationYear }}</el-descriptions-item>
          <el-descriptions-item label="电子邮箱">{{ currentStudent.user?.email }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentStudent.user?.phone }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 面试邀请组件 -->
    <send-interview-invitation
      v-if="currentApplicationId"
      :application-id="currentApplicationId"
      :disabled="false"
      ref="interviewInvitationRef"
      @success="handleInterviewInvitationSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ArrowDown, Clock, View, Check, Bell, Close, CircleCheck, UserFilled, Aim, School, Briefcase, Connection, SetUp, Medal, ChatDotRound, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import SendInterviewInvitation from './components/SendInterviewInvitation.vue'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 多选相关
const selectedRows = ref([])
const hasSelected = computed(() => selectedRows.value.length > 0)

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
  positionId: null,
  status: null,
  dateRange: null,
  startDate: null,
  endDate: null,
  keyword: null
})

// 职位选项
const positionOptions = ref([])

// 简历预览
const resumeDialogVisible = ref(false)
const resumeLoading = ref(false)
const currentResumeId = ref(null)
const resumeData = ref(null)

// 学生详情
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const currentStudent = ref({})

// 面试邀请
const interviewInvitationRef = ref(null)
const currentApplicationId = ref(null)

// 获取职位列表
const fetchPositions = async () => {
  try {
    await request.get('/position/list', {}, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        positionOptions.value = (res || []).map(item => ({
          value: item.id,
          label: item.positionName
        }))
      }
    })
  } catch (error) {
    console.error('获取职位列表失败', error)
  }
}

// 获取投递记录
const fetchApplications = async () => {
  loading.value = true
  try {
    await request.get('/application/page', {
      positionId: searchForm.value.positionId,
      status: searchForm.value.status,
      startDate: searchForm.value.startDate,
      endDate: searchForm.value.endDate,
      keyword: searchForm.value.keyword,
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
    positionId: null,
    status: null,
    dateRange: null,
    startDate: null,
    endDate: null,
    keyword: null
  }
  handleSearch()
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

// 预览简历
const previewResume = (resumeId) => {
  resumeLoading.value = true
  currentResumeId.value = resumeId
  resumeDialogVisible.value = true
  
  // 获取简历数据
  request.get(`/resume/${resumeId}`, {}, {
    showDefaultMsg: false,
    onSuccess: (res) => {
      // 解析resumeContent字段的JSON字符串
      if (res && res.resumeContent) {
        try {
          res.resumeContent = JSON.parse(res.resumeContent)
        } catch (error) {
          console.error('简历内容解析失败:', error)
          ElMessage.error('简历内容解析失败')
        }
      }else{
        ElMessage.error('简历内容为空')
        resumeLoading.value = false
      }
      resumeData.value = res
      resumeLoading.value = false
    },
    onError: (res) => {
      resumeLoading.value = false
      ElMessage.error(res.msg||'获取简历数据失败')
    }
  })
}

// 关闭简历预览对话框
const handleCloseResumeDialog = () => {
  resumeDialogVisible.value = false
  currentResumeId.value = null
  resumeData.value = null
}

// 下载简历
const handleDownloadFormat = async (format) => {
  if (currentResumeId.value) {
    try {
      const response = await request.get(`/resume/export/${currentResumeId.value}`, {
        format
      }, {
        responseType: 'blob',
        showDefaultMsg: false
      })
      
      // 创建下载链接
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `${resumeData.value.resumeName}.${format}`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('简历下载中...')
    } catch (error) {
      console.error('简历下载失败:', error)
      ElMessage.error('简历下载失败')
    }
  }
}

// 查看学生详情
const viewStudentDetail = (row) => {
  studentLoading.value = true
  currentStudent.value = {}
  studentDialogVisible.value = true

  request.get(`/student/${row.studentId}`, {}, {
    showDefaultMsg: false,
    onSuccess: (res) => {
      currentStudent.value = res
      studentLoading.value = false
    },
    onError: () => {
      currentStudent.value = null
      studentLoading.value = false
    }
  })
}

// 表格选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 批量标记已查看
const batchMarkAsViewed = () => {
  if (selectedRows.value.length === 0) {
    return
  }
  
  ElMessageBox.confirm(
    '确定将选中的投递记录标记为"已查看"吗？',
    '批量操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      // 显示加载中
      const loadingInstance = ElMessage({
        type: 'info',
        message: '处理中...',
        duration: 0
      })
      
      try {
        // 使用Promise.all并行处理所有请求
        const promises = selectedRows.value.map(row => 
          request.put(`/application/status/${row.id}`, null, {
            params: { status: 1 },
            showDefaultMsg: false
          })
        )
        
        await Promise.all(promises)
        
        loadingInstance.close()
        ElMessage.success(`成功将${selectedRows.value.length}条记录标记为已查看`)
        
        // 刷新数据
        fetchApplications()
        fetchStatistics()
      } catch (error) {
        loadingInstance.close()
        ElMessage.error('批量操作失败，请重试')
        console.error('批量标记已查看失败', error)
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 批量通过筛选
const batchMarkAsPassed = () => {
  if (selectedRows.value.length === 0) {
    return
  }
  
  ElMessageBox.confirm(
    '确定将选中的投递记录标记为"通过筛选"吗？',
    '批量操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      // 显示加载中
      const loadingInstance = ElMessage({
        type: 'info',
        message: '处理中...',
        duration: 0
      })
      
      try {
        // 使用Promise.all并行处理所有请求
        const promises = selectedRows.value.map(row => 
          request.put(`/application/status/${row.id}`, null, {
            params: { status: 2 },
            showDefaultMsg: false
          })
        )
        
        await Promise.all(promises)
        
        loadingInstance.close()
        ElMessage.success(`成功将${selectedRows.value.length}条记录标记为通过筛选`)
        
        // 刷新数据
        fetchApplications()
        fetchStatistics()
      } catch (error) {
        loadingInstance.close()
        ElMessage.error('批量操作失败，请重试')
        console.error('批量标记通过筛选失败', error)
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 批量标记不合适
const batchMarkAsRejected = () => {
  if (selectedRows.value.length === 0) {
    return
  }
  
  ElMessageBox.confirm(
    '确定将选中的投递记录标记为"不合适"吗？',
    '批量操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      // 显示加载中
      const loadingInstance = ElMessage({
        type: 'info',
        message: '处理中...',
        duration: 0
      })
      
      try {
        // 使用Promise.all并行处理所有请求
        const promises = selectedRows.value.map(row => 
          request.put(`/application/status/${row.id}?status=4`, null, {
      
            showDefaultMsg: false
          })
        )
        
        await Promise.all(promises)
        
        loadingInstance.close()
        ElMessage.success(`成功将${selectedRows.value.length}条记录标记为不合适`)
        
        // 刷新数据
        fetchApplications()
        fetchStatistics()
      } catch (error) {
        loadingInstance.close()
        ElMessage.error('批量操作失败，请重试')
        console.error('批量标记不合适失败', error)
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 更改投递状态
const handleStatusChange = (id, status) => {
  // 如果是发送面试邀请，则显示面试邀请组件
  if (status === 3) {
    showInterviewInvitation({ id })
    return
  }
  
  // 确认更改状态
  const statusName = getStatusName(status)
  ElMessageBox.confirm(
    `确定要将此投递状态更改为"${statusName}"吗？`,
    '更改状态',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 调用接口更改状态
      request.put(`/application/status/${id}?status=${status}`, null, {
        successMsg: '状态更新成功',
        onSuccess: () => {
          // 刷新数据
          fetchApplications()
          fetchStatistics()
        }
      })
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 显示面试邀请组件
const showInterviewInvitation = (row) => {
  currentApplicationId.value = row.id
  // 使用nextTick确保组件已经渲染
  setTimeout(() => {
    if (interviewInvitationRef.value) {
      interviewInvitationRef.value.dialogVisible = true
    }
  }, 0)
}

// 处理面试邀请成功
const handleInterviewInvitationSuccess = () => {
  // 刷新数据
  fetchApplications()
  fetchStatistics()
}

onMounted(() => {
  fetchPositions()
  fetchApplications()
  fetchStatistics()
})
</script>

<style scoped>
.application-management-container {
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

.table-operations {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}
.el-table .cell{
  display: flex;
  justify-content: center;
  align-items: center;
}
.selected-count {
  margin-left: 15px;
  color: #606266;
  font-size: 14px;
}

.student-info {
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f0f2f5 60%, #e6ecfa 100%);
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
  /* transition: box-shadow 0.3s, transform 0.3s; */
  border: 1px solid #e0e6ed;
}
.student-info:hover {
  /* box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18); */
  /* transform: translateY(-2px) scale(1.02); */
  /* background: linear-gradient(135deg, #e6ecfa 60%, #f0f2f5 100%); */
  border: 1px solid #0e79f5;
  /* 过度效果 */
  transition: all 0.8s ease;
}

.student-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 16px;
  color: #303133;
  letter-spacing: 0.5px;
}

.student-no {
  font-size: 13px;
  color: #67a1ff;
  margin-bottom: 2px;
  font-style: italic;
  letter-spacing: 0.2px;
}

.status-tag {
  display: flex;
  align-items: center;
  padding: 6px 10px;
}

.status-tag .el-icon {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.resume-preview-container, .student-detail-container {
  min-height: 400px;
}

.resume-loading {
  padding: 20px;
}

.resume-content {
  padding: 20px;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.resume-actions {
  display: flex;
  align-items: center;
}

.resume-actions .el-dropdown {
  margin-left: 10px;
}

.resume-detail {
  padding: 20px;
}

.resume-section {
  margin-bottom: 20px;
}

.resume-section-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.resume-section-header .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

.resume-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #ebeef5;
}

.resume-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.self-evaluation {
  white-space: pre-line;
  line-height: 1.6;
  padding: 10px;
  text-align: left;
  background-color: #f8f9fa;
  border-radius: 4px;
  text-indent: 2em;
}
</style> 
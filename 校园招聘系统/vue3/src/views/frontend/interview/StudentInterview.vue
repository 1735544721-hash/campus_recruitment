<template>
  <div class="student-interview">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>我的面试</h2>
          <div class="filter-container">
            <el-select style="width: 100px;" v-model="searchForm.status" placeholder="面试状态" clearable @change="fetchInterviews">
              <el-option label="待确认" :value="0" />
              <el-option label="已确认" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
            <!-- <el-button type="primary" @click="fetchInterviews">查询</el-button> -->
          </div>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="companyName" label="企业名称" />
        <el-table-column prop="positionName" label="应聘职位" />
        <el-table-column prop="interviewTime" label="面试时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.interviewTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="interviewAddress" label="面试地点" />
        <el-table-column prop="interviewType" label="面试类型" />
        <el-table-column prop="interviewStatus" label="面试状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.interviewStatus)">
              {{ getStatusText(scope.row.interviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewResult" label="面试结果">
          <template #default="scope">
            <el-tag v-if="scope.row.interviewStatus === 2" :type="getResultType(scope.row.interviewResult)">
              {{ getResultText(scope.row.interviewResult) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button 
              v-if="scope.row.interviewStatus === 0" 
              type="success" 
              size="small" 
              @click="handleConfirm(scope.row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="scope.row.interviewStatus === 0 || scope.row.interviewStatus === 1" 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:page-size="val => pageSize = val"
        />
      </div>
    </el-card>
    
    <!-- 面试详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="面试详情" width="600px">
      <div v-if="currentInterview" class="interview-detail">
        <div class="detail-item">
          <span class="label">企业名称：</span>
          <span>{{ currentInterview.companyName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">应聘职位：</span>
          <span>{{ currentInterview.positionName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">面试时间：</span>
          <span>{{ formatDateTime(currentInterview.interviewTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">面试地点：</span>
          <span>{{ currentInterview.interviewAddress }}</span>
        </div>
        <div class="detail-item">
          <span class="label">面试类型：</span>
          <span>{{ currentInterview.interviewType }}</span>
        </div>
        <div class="detail-item">
          <span class="label">面试状态：</span>
          <span>
            <el-tag :type="getStatusType(currentInterview.interviewStatus)">
              {{ getStatusText(currentInterview.interviewStatus) }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">面试结果：</span>
          <span v-if="currentInterview.interviewStatus === 2">
            <el-tag :type="getResultType(currentInterview.interviewResult)">
              {{ getResultText(currentInterview.interviewResult) }}
            </el-tag>
          </span>
          <span v-else>-</span>
        </div>
        <div class="detail-item">
          <span class="label">面试备注：</span>
          <span>{{ currentInterview.interviewNote || '无' }}</span>
        </div>
        <div v-if="currentInterview.interviewStatus === 2" class="detail-item">
          <span class="label">面试反馈：</span>
          <span>{{ currentInterview.feedback || '无' }}</span>
        </div>
      </div>
    </el-dialog>
    
    <!-- 确认面试对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="确认面试" width="400px">
      <p>确认参加此次面试吗？</p>
      <p>面试时间：{{ currentInterview ? formatDateTime(currentInterview.interviewTime) : '' }}</p>
      <p>面试地点：{{ currentInterview ? currentInterview.interviewAddress : '' }}</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConfirm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

// 数据
const loading = ref(false);
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchForm = reactive({
  status: null
});

// 对话框控制
const detailDialogVisible = ref(false);
const confirmDialogVisible = ref(false);
const currentInterview = ref(null);

// 获取面试列表
const fetchInterviews = async () => {
  loading.value = true;
  try {
    await request.get('/interview/student/page', {
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || [];
        total.value = res.total || 0;
      }
    });
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchInterviews();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchInterviews();
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 获取状态文本和类型
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待确认';
    case 1: return '已确认';
    case 2: return '已完成';
    case 3: return '已取消';
    default: return '未知';
  }
};

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning';
    case 1: return 'primary';
    case 2: return 'success';
    case 3: return 'info';
    default: return 'info';
  }
};

// 获取结果文本和类型
const getResultText = (result) => {
  switch (result) {
    case 0: return '未知';
    case 1: return '通过';
    case 2: return '未通过';
    default: return '未知';
  }
};

const getResultType = (result) => {
  switch (result) {
    case 0: return 'info';
    case 1: return 'success';
    case 2: return 'danger';
    default: return 'info';
  }
};

// 查看详情
const handleDetail = (row) => {
  currentInterview.value = row;
  detailDialogVisible.value = true;
};

// 确认面试
const handleConfirm = (row) => {
  currentInterview.value = row;
  confirmDialogVisible.value = true;
};

// 提交确认面试
const submitConfirm = async () => {
  try {
    await request.put(`/interview/confirm/${currentInterview.value.id}`, null,{
      successMsg: '已确认面试',
      onSuccess: () => {
        confirmDialogVisible.value = false;
        fetchInterviews();
      }
    });
  } catch (error) {
    console.error('确认面试失败', error);
  }
};

// 取消面试
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消此次面试吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/interview/cancel/${row.id}`, null,{
        successMsg: '面试已取消',
        onSuccess: () => {
          fetchInterviews();
        }
      });
    } catch (error) {
      console.error('取消面试失败', error);
    }
  }).catch(() => {});
};

// 页面加载时获取数据
onMounted(() => {
  fetchInterviews();
});
</script>

<style lang="scss" scoped>
.student-interview {
  padding: 2rem;
  font-family: var(--font-family-base);
}

:deep(.el-card) {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);

  .el-card__header {
    border-bottom: 1px solid #D3D3D3;
    padding: 1.5rem;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h2 {
    font-size: 1.75rem;
    color: #8B7355;
    margin: 0;
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

.filter-container {
  display: flex;
  gap: 1rem;

  :deep(.el-select) {
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

:deep(.el-table) {
  background: transparent;
  
  .el-table__header-wrapper {
    th {
      background: rgba(139, 115, 85, 0.05);
      color: #4A4A4A;
      font-weight: normal;
      border-bottom: 1px solid #D3D3D3;
    }
  }
  
  .el-table__body-wrapper {
    background: transparent;
    
    td {
      background: transparent;
      border-bottom: 1px solid #D3D3D3;
      color: #9B8E83;
      transition: background 0.3s ease;
    }
    
    tr:hover td {
      background: rgba(139, 115, 85, 0.05);
    }
  }

  .el-button {
    padding: 0.25rem 0.75rem;
    font-size: 0.875rem;

    &.el-button--primary {
      color: #8B7355;
      background: transparent;
      border: none;

      &:hover {
        color: darken(#8B7355, 10%);
      }
    }

    &.el-button--success {
      color: #67C23A;
      background: transparent;
      border: none;

      &:hover {
        color: darken(#67C23A, 10%);
      }
    }

    &.el-button--danger {
      color: #F56C6C;
      background: transparent;
      border: none;

      &:hover {
        color: darken(#F56C6C, 10%);
      }
    }
  }

  .el-tag {
    background: transparent;
    border: 1px solid currentColor;
    
    &.el-tag--warning {
      color: #8B7355;
      border-color: currentColor;
    }
    
    &.el-tag--primary {
      color: #8B7355;
      border-color: currentColor;
    }
    
    &.el-tag--success {
      color: #8B7355;
      border-color: currentColor;
    }
    
    &.el-tag--info {
      color: #9B8E83;
      border-color: currentColor;
    }
  }
}

.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;

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

.interview-detail {
  padding: 1rem;

  .detail-item {
    margin-bottom: 1rem;
    display: flex;

    .label {
      font-weight: normal;
      width: 100px;
      color: #4A4A4A;
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
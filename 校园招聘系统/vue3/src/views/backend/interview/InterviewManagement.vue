<template>
  <div class="interview-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>面试管理</h2>
          <div class="filter-container">
            <el-select v-model="searchForm.status" placeholder="面试状态" clearable @change="fetchInterviews">
              <el-option label="待确认" :value="0" />
              <el-option label="已确认" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
            <el-button type="primary" @click="fetchInterviews">查询</el-button>
          </div>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" />
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
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button  
              v-if="(scope.row.interviewStatus === 0 || scope.row.interviewStatus === 1)&&userStore.isCompany&&userStore.hasCompanyInfo" 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="scope.row.interviewStatus === 1&&userStore.isCompany&&userStore.hasCompanyInfo" 
              type="success" 
              size="small" 
              @click="handleComplete(scope.row)"
            >
              完成面试
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
          <span class="label">学生姓名：</span>
          <span>{{ currentInterview.studentName }}</span>
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
    
    <!-- 完成面试对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成面试" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="面试结果">
          <el-radio-group v-model="completeForm.interviewResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">未通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试反馈">
          <el-input
            v-model="completeForm.feedback"
            type="textarea"
            rows="4"
            placeholder="请输入面试反馈"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitComplete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';
import { useUserStore } from '@/store/user';
const userStore = useUserStore();


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
const completeDialogVisible = ref(false);
const currentInterview = ref(null);
const completeForm = reactive({
  id: null,
  interviewResult: 1,
  feedback: ''
});

// 获取面试列表
const fetchInterviews = async () => {
  loading.value = true;
  try {
    await request.get('/interview/page', {
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

// 取消面试
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消此次面试吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/interview/cancel/${row.id}`, {
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

// 完成面试
const handleComplete = (row) => {
  completeForm.id = row.id;
  completeForm.interviewResult = 1;
  completeForm.feedback = '';
  completeDialogVisible.value = true;
};

// 提交完成面试
const submitComplete = async () => {
  if (!completeForm.feedback) {
    ElMessage.warning('请填写面试反馈');
    return;
  }
  
  try {
    await request.put('/interview/complete', {
      id: completeForm.id,
      interviewResult: completeForm.interviewResult,
      feedback: completeForm.feedback
    }, {
      successMsg: '面试已完成',
      onSuccess: () => {
        completeDialogVisible.value = false;
        fetchInterviews();
      }
    });
  } catch (error) {
    console.error('完成面试失败', error);
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchInterviews();
});
</script>

<style scoped>
.interview-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.interview-detail {
  padding: 10px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-item .label {
  font-weight: bold;
  width: 100px;
}
</style> 
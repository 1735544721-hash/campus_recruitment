<template>
  <div class="message-center-container">
    <div class="message-header">
      <h2>消息中心</h2>
      <div class="message-actions">
        <el-button type="primary" :disabled="selectedMessages.length === 0" @click="handleMarkAsRead">
          标记为已读
        </el-button>
        <el-button type="danger" :disabled="selectedMessages.length === 0" @click="handleDelete">
          删除
        </el-button>
      </div>
    </div>
    
    <el-card class="message-card">
      <div class="message-filter">
        <el-select v-model="messageType" placeholder="消息类型" clearable>
          <el-option
            v-for="item in messageTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select v-model="readStatus" placeholder="阅读状态" clearable>
          <el-option
            v-for="item in readStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-button type="primary" @click="fetchMessages">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="messages"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="messageTypeDesc" label="消息类型" width="120">
          <template #default="scope">
            <el-tag :type="getMessageTypeTag(scope.row.messageType)">
              {{ scope.row.messageTypeDesc }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        
        <el-table-column prop="senderName" label="发送者" width="120" />
        
        <el-table-column prop="createTime" label="发送时间" width="180">
          <template #default="scope">
            {{ DateUtils.formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="isRead" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isRead ? 'info' : 'success'">
              {{ scope.row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button 
              v-if="!scope.row.isRead" 
              link 
              type="success" 
              @click="handleMarkSingleAsRead(scope.row)"
            >
              标为已读
            </el-button>
            <el-button link type="danger" @click="handleDeleteSingle(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';
import DateUtils from '@/utils/dateUtils';

const router = useRouter();

// 状态变量
const messages = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const messageType = ref('');
const readStatus = ref('');
const selectedMessages = ref([]);

// 消息类型选项
const messageTypeOptions = [
  { value: 'SYSTEM_NOTICE', label: '系统通知' },
  { value: 'INTERVIEW_NOTICE', label: '面试通知' },
  { value: 'RESUME_STATUS', label: '简历状态通知' },
  { value: 'APPLICATION_STATUS', label: '投递状态通知' },
  { value: 'VERIFICATION_NOTICE', label: '认证通知' },
  { value: 'OTHER', label: '其他通知' }
];

// 阅读状态选项
const readStatusOptions = [
  { value: true, label: '已读' },
  { value: false, label: '未读' }
];

// 获取消息类型标签样式
const getMessageTypeTag = (type) => {
  switch (type) {
    case 'SYSTEM_NOTICE':
      return 'info';
    case 'INTERVIEW_NOTICE':
      return 'warning';
    case 'RESUME_STATUS':
      return 'success';
    case 'APPLICATION_STATUS':
      return 'primary';
    case 'VERIFICATION_NOTICE':
      return 'danger';
    default:
      return '';
  }
};

// 获取消息列表
const fetchMessages = async () => {
  console.log('fetchMessages');
  loading.value = true;
  try {
    await request.get('/message/page', {
      messageType: messageType.value,
      isRead: readStatus.value,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (data) => {
        messages.value = data.records || [];
        total.value = data.total || 0;
      }
    });
  } catch (error) {
    console.error('获取消息列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 重置筛选条件
const resetFilters = () => {
  messageType.value = '';
  readStatus.value = '';
  fetchMessages();
};

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedMessages.value = selection;
};

// 处理查看消息
const handleView = (row) => {
  router.push(`/message/${row.id}`);
};

// 标记单个消息为已读
const handleMarkSingleAsRead = async (row) => {
  try {
    await request.put(`/message/read/${row.id}`, {}, {
      successMsg: '标记成功',
      onSuccess: () => {
        row.isRead = true;
        fetchMessages();
      }
    });
  } catch (error) {
    console.error('标记消息为已读失败', error);
  }
};

// 批量标记消息为已读
const handleMarkAsRead = async () => {
  const unreadMessages = selectedMessages.value.filter(msg => !msg.isRead);
  
  if (unreadMessages.length === 0) {
    ElMessage.warning('没有选中未读消息');
    return;
  }
  
  const ids = unreadMessages.map(msg => msg.id);
  
  try {
    // 修复：使用PUT请求，与后端接口匹配
    await request.put('/message/read/batch', ids, {
      successMsg: '标记成功',
      onSuccess: () => {
        fetchMessages();
      }
    });
  } catch (error) {
    console.error('批量标记消息为已读失败', error);
  }
};

// 删除单个消息
const handleDeleteSingle = async (row) => {
  ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/message/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchMessages();
        }
      });
    } catch (error) {
      console.error('删除消息失败', error);
    }
  }).catch(() => {});
};

// 批量删除消息
const handleDelete = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请选择要删除的消息');
    return;
  }
  
  const ids = selectedMessages.value.map(msg => msg.id);
  
  ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 条消息吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 修复：使用DELETE请求，与后端接口匹配
      await request.delete('/message/batch', {
        data: ids, // 在DELETE请求中，请求体需要放在data属性中
        successMsg: '删除成功',
        onSuccess: () => {
          fetchMessages();
        }
      });
    } catch (error) {
      console.error('批量删除消息失败', error);
    }
  }).catch(() => {});
};

// 处理页面大小变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchMessages();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchMessages();
};

// 组件挂载时
onMounted(() => {
  fetchMessages();
  
  // 监听路由变化，当从消息详情页返回时刷新消息列表
  const unwatch = router.afterEach((to, from) => {
    // 如果是从消息详情页返回
    if (from.path.includes('/message/') && !from.path.includes('/message/center') && to.path.includes('/message/center')) {
      fetchMessages();
    }
  });
  
  // 组件卸载时取消监听
  onUnmounted(() => {
    unwatch();
  });
});
</script>

<style lang="scss" scoped>
.message-center-container {
  padding: 2rem;
  font-family: var(--font-family-base);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

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

.message-actions {
  display: flex;
  gap: 1rem;

  .el-button {
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;
    transition: all 0.3s ease;

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

    &.el-button--danger {
      color: #F56C6C;
      border-color: #F56C6C;

      &:hover {
        background: rgba(245, 108, 108, 0.05);
      }
    }

    &:disabled {
      color: #C0C4CC;
      border-color: #E4E7ED;
      background: transparent;
    }
  }
}

.message-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);
  margin-bottom: 2rem;
}

.message-filter {
  display: flex;
  margin-bottom: 2rem;
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
</style> 
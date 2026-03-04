<template>
  <div class="message-management-container">
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <h3>消息管理</h3>
          <el-button type="primary" @click="openSendMessageDialog">发送消息</el-button>
        </div>
      </template>
      
      <div class="message-filter">
        <el-form :inline="true" :model="searchForm" class="filter-form">
          <el-form-item label="消息类型">
            <el-select v-model="searchForm.messageType" placeholder="消息类型" clearable>
              <el-option
                v-for="item in messageTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="接收者">
            <el-input v-model="searchForm.receiverName" placeholder="接收者姓名" clearable />
          </el-form-item>
          
          <el-form-item label="阅读状态">
            <el-select v-model="searchForm.isRead" placeholder="阅读状态" clearable>
              <el-option
                v-for="item in readStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="fetchMessages">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
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
        
        <el-table-column prop="receiverName" label="接收者" width="120" />
        
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
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
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
    
    <!-- 发送消息对话框 -->
    <el-dialog
      v-model="sendMessageDialogVisible"
      title="发送消息"
      width="600px"
    >
      <el-form
        ref="messageFormRef"
        :model="messageForm"
        :rules="messageRules"
        label-width="100px"
      >
        <el-form-item label="消息类型" prop="messageType">
          <el-select v-model="messageForm.messageType" placeholder="请选择消息类型" style="width: 100%">
            <el-option
              v-for="item in messageTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="接收方式">
          <el-radio-group v-model="receiveType">
            <el-radio :label="'single'">单个用户</el-radio>
            <el-radio :label="'batch'">批量发送</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="receiveType === 'single'" label="接收者" prop="receiverId">
          <el-select
            v-model="messageForm.receiverId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名搜索"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="receiveType === 'batch'" label="接收者类型" prop="receiverType">
          <el-select v-model="messageForm.receiverType" placeholder="请选择接收者类型" style="width: 100%">
            <el-option label="学生用户" value="STUDENT" />
            <el-option label="企业用户" value="COMPANY" />
            <el-option label="所有用户" value="ALL" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="标题" prop="title">
          <el-input v-model="messageForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="sendMessageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSendMessage" :loading="sendLoading">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
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
const selectedMessages = ref([]);
const searchForm = reactive({
  messageType: '',
  receiverName: '',
  isRead: ''
});

// 发送消息相关
const sendMessageDialogVisible = ref(false);
const messageFormRef = ref(null);
const receiveType = ref('single');
const userSearchLoading = ref(false);
const sendLoading = ref(false);
const userOptions = ref([]);
const messageForm = reactive({
  messageType: '',
  receiverId: null,
  receiverType: '',
  receiverIds: [],
  title: '',
  content: ''
});

// 表单验证规则
const messageRules = {
  messageType: [
    { required: true, message: '请选择消息类型', trigger: 'change' }
  ],
  receiverId: [
    { required: true, message: '请选择接收者', trigger: 'change' }
  ],
  receiverType: [
    { required: true, message: '请选择接收者类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入消息标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' }
  ]
};

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
  loading.value = true;
  try {
    // 在实际项目中，这里应该调用后台管理专用的接口
    await request.get('/message/page/admin', {
      messageType: searchForm.messageType,
      isRead: searchForm.isRead,
      receiverName: searchForm.receiverName || undefined,
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

// 重置搜索条件
const resetSearch = () => {
  searchForm.messageType = '';
  searchForm.receiverName = '';
  searchForm.isRead = '';
  fetchMessages();
};

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedMessages.value = selection;
};

// 处理查看消息
const handleView = (row) => {
  router.push(`/back/message/${row.id}`);
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

// 打开发送消息对话框
const openSendMessageDialog = () => {
  resetMessageForm();
  sendMessageDialogVisible.value = true;
};

// 重置消息表单
const resetMessageForm = () => {
  messageForm.messageType = '';
  messageForm.receiverId = null;
  messageForm.receiverType = '';
  messageForm.receiverIds = [];
  messageForm.title = '';
  messageForm.content = '';
  receiveType.value = 'single';
  userOptions.value = [];
  if (messageFormRef.value) {
    messageFormRef.value.resetFields();
  }
};

// 搜索用户
const searchUsers = async (query) => {
  if (query.length < 2) return;
  
  userSearchLoading.value = true;
  try {
    // 这里应该调用用户搜索接口
    await request.get('/user/search', {
      keyword: query
    }, {
      onSuccess: (data) => {
        userOptions.value = (data || []).map(user => ({
          value: user.id,
          label: `${user.username} (${user.name || user.username})`
        }));
      }
    });
  } catch (error) {
    console.error('搜索用户失败', error);
  } finally {
    userSearchLoading.value = false;
  }
};

// 发送消息
const handleSendMessage = async () => {
  if (!messageFormRef.value) return;
  
  await messageFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    sendLoading.value = true;
    try {
      if (receiveType.value === 'single') {
        // 发送单个消息
        await request.post('/message', {
          messageType: messageForm.messageType,
          receiverId: messageForm.receiverId,
          title: messageForm.title,
          content: messageForm.content
        }, {
          successMsg: '消息发送成功',
          onSuccess: () => {
            sendMessageDialogVisible.value = false;
            fetchMessages();
          }
        });
      } else {
        // 批量发送消息
        await request.post('/message/batch/role', {
          messageType: messageForm.messageType,
          receiverType: messageForm.receiverType,
          title: messageForm.title,
          content: messageForm.content
        }, {
          successMsg: '消息批量发送成功',
          onSuccess: () => {
            sendMessageDialogVisible.value = false;
            fetchMessages();
          }
        });
      }
    } catch (error) {
      console.error('发送消息失败', error);
    } finally {
      sendLoading.value = false;
    }
  });
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
});
</script>

<style scoped>
.message-management-container {
  padding: 20px;
}

.message-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-filter {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 
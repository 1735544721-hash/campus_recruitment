<template>
  <div class="message-detail-container">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <div class="message-header">
          <h2 class="message-title">{{ message.title }}</h2>
          <div class="message-meta">
            <el-tag :type="getMessageTypeTag(message.messageType)">
              {{ message.messageTypeDesc }}
            </el-tag>
            <span class="message-time" v-if="message.createTime">{{ DateUtils.formatDateTime(message.createTime) }}</span>
          </div>
        </div>
      </template>
      
      <div class="message-info">
        <div class="message-sender">
          <span class="label">发送者：</span>
          <span>{{ message.senderName || '系统' }}</span>
        </div>
        
        <div class="message-status">
          <span class="label">状态：</span>
          <el-tag :type="message.isRead ? 'info' : 'success'">
            {{ message.isRead ? '已读' : '未读' }}
          </el-tag>
        </div>
      </div>
      
      <el-divider />
      
      <div class="message-content">
        {{ message.content }}
      </div>
      
      <div class="message-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import request from '@/utils/request';
import DateUtils from '@/utils/dateUtils';

const route = useRoute();
const router = useRouter();
const messageId = route.params.id;

// 状态变量
const message = ref({});
const loading = ref(false);

// 获取消息详情
const fetchMessageDetail = async () => {
  loading.value = true;
  console.log('fetchMessageDetail', messageId);
  try {
    await request.get(`/message/${messageId}`, null, {
      onSuccess: (data) => {
        console.log('消息详情数据:', data);
        message.value = data || {};
        // 后端会自动将消息标记为已读，强制设置本地状态为已读
        message.value.isRead = true;
      }
    });
  } catch (error) {
    console.error('获取消息详情失败', error);
  } finally {
    loading.value = false;
  }
};

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

// 返回上一页
const goBack = () => {
  // 修改为直接返回消息中心页面，不使用document.referrer判断
  router.push('/message/center');
};

// 删除消息
const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/message/${messageId}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          router.push('/message');
        }
      });
    } catch (error) {
      console.error('删除消息失败', error);
    }
  }).catch(() => {});
};

// 组件挂载时
onMounted(() => {
  fetchMessageDetail();
});
</script>

<style lang="scss" scoped>
.message-detail-container {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
  font-family: var(--font-family-base);

  :deep(.el-card) {
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid #D3D3D3;
    backdrop-filter: blur(10px);
  }
}

.message-header {
  display: flex;
  flex-direction: column;
  position: relative;

  .message-title {
    margin: 0 0 1.5rem;
    font-size: 1.75rem;
    color: #8B7355;
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

.message-meta {
  display: flex;
  align-items: center;
  gap: 1rem;

  .el-tag {
    background: transparent;
    border: 1px solid currentColor;
    color: #8B7355;
  }

  .message-time {
    color: #9B8E83;
    font-size: 0.875rem;
  }
}

.message-info {
  display: flex;
  justify-content: space-between;
  margin: 1.5rem 0;
  padding: 1rem 0;
  border-bottom: 1px solid #D3D3D3;

  .message-sender,
  .message-status {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    .label {
      color: #4A4A4A;
    }

    span:not(.label) {
      color: #9B8E83;
    }

    .el-tag {
      background: transparent;
      border: 1px solid currentColor;
      
      &.el-tag--info {
        color: #9B8E83;
      }
      
      &.el-tag--success {
        color: #8B7355;
      }
    }
  }
}

.message-content {
  padding: 1.5rem 0;
  line-height: 1.8;
  color: #4A4A4A;
  font-size: 1rem;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #D3D3D3;

  .el-button {
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;
    transition: all 0.3s ease;

    &:hover {
      border-color: #8B7355;
      background: rgba(139, 115, 85, 0.05);
    }

    &.el-button--danger {
      color: #F56C6C;
      border-color: #F56C6C;

      &:hover {
        background: rgba(245, 108, 108, 0.05);
      }
    }
  }
}
</style> 
<template>
  <div class="message-notification">
    <el-popover
      placement="bottom"
      :width="300"
      trigger="click"
      popper-class="message-popover"
    >
      <template #reference>
        <el-badge :value="unreadCount" :max="99" class="message-badge">
          <el-button type="text" class="message-icon">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
      </template>
      
      <div class="message-header">
        <span class="message-title">消息通知</span>
        <el-button type="text" @click="markAllAsRead" :disabled="!hasUnread">全部已读</el-button>
      </div>
      
      <el-divider />
      
      <div v-if="loading" class="message-loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="messages.length === 0" class="message-empty">
        <el-empty description="暂无消息" :image-size="60" />
      </div>
      
      <div v-else class="message-list">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ 'message-unread': !message.isRead }"
          @click="viewMessage(message)"
        >
          <div class="message-item-header">
            <span class="message-type">
              <el-tag size="small" :type="getMessageTypeTag(message.messageType)">
                {{ message.messageTypeDesc }}
              </el-tag>
            </span>
            <span class="message-time">{{ formatTime(message.createTime) }}</span>
          </div>
          <div class="message-item-title">{{ message.title }}</div>
          <div class="message-item-content">{{ truncateContent(message.content) }}</div>
        </div>
      </div>
      
      <div class="message-footer">
        <el-button type="primary" link @click="viewAllMessages">查看全部消息</el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Bell } from '@element-plus/icons-vue';
import request from '@/utils/request';
import DateUtils from '@/utils/dateUtils';

const router = useRouter();
const messages = ref([]);
const loading = ref(false);
const unreadCount = ref(0);

const hasUnread = computed(() => unreadCount.value > 0);

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

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return DateUtils.formatDateTime(time, 'MM-DD HH:mm');
};

// 截断内容
const truncateContent = (content) => {
  if (!content) return '';
  return content.length > 50 ? content.substring(0, 50) + '...' : content;
};

// 获取最新消息
const fetchLatestMessages = async () => {
  loading.value = true;
  try {
    await request.get('/message/page', {
      isRead: false,
      currentPage: 1,
      size: 5
    }, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        messages.value = data.records || [];
      }
    });
  } catch (error) {
    console.error('获取最新消息失败', error);
  } finally {
    loading.value = false;
  }
};

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    await request.get('/message/count',null, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        console.log('未读消息统计数据:', data);
        unreadCount.value = data?.unreadCount || 0;
      }
    });
  } catch (error) {
    console.error('获取未读消息数量失败', error);
  }
};

// 标记所有消息为已读
const markAllAsRead = async () => {
  try {
    await request.put('/message/read/all', {}, {
      successMsg: '已将全部消息标记为已读',
      onSuccess: () => {
        // 更新本地消息状态
        messages.value.forEach(message => {
          message.isRead = true;
        });
        // 重置未读消息数量
        unreadCount.value = 0;
        // 重新获取最新消息
        fetchLatestMessages();
      }
    });
  } catch (error) {
    console.error('标记所有消息为已读失败', error);
  }
};

// 查看消息详情
const viewMessage = (message) => {
  // 先将消息标记为已读
  markMessageAsRead(message.id);
  
  // 根据当前用户角色和路由情况，决定跳转到前台还是后台的消息详情页
  const path = router.currentRoute.value.path.includes('/back') 
    ? `/back/message/${message.id}` 
    : `/message/${message.id}`;
  router.push(path);
};

// 标记单个消息为已读
const markMessageAsRead = async (id) => {
  try {
    await request.put(`/message/read/${id}`, {}, {
      showDefaultMsg: false,
      onSuccess: () => {
        // 更新本地消息状态
        const message = messages.value.find(m => m.id === id);
        if (message && !message.isRead) {
          message.isRead = true;
          // 更新未读消息数量
          if (unreadCount.value > 0) {
            unreadCount.value -= 1;
          }
        }
      }
    });
  } catch (error) {
    console.error('标记消息为已读失败', error);
  }
};

// 查看所有消息
const viewAllMessages = () => {
  router.push('/message/center');
};

// 组件挂载时
onMounted(() => {
  fetchUnreadCount();
  fetchLatestMessages();
  
  // 定时刷新未读消息数量
  const timer = setInterval(() => {
    fetchUnreadCount();
  }, 60000); // 每分钟刷新一次
  
  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(timer);
  });
});
</script>

<style scoped>
.message-notification {
  display: inline-block;
}

.message-badge {
  margin-right: 10px;
}

.message-icon {
  color: var(--el-text-color-primary);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
}

.message-title {
  font-size: 16px;
  font-weight: 500;
}

.message-list {
  max-height: 300px;
  overflow-y: auto;
}

.message-item {
  padding: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.message-item:hover {
  background-color: var(--el-fill-color-light);
}

.message-unread {
  background-color: var(--el-color-primary-light-9);
}

.message-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-item-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.message-item-content {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-footer {
  margin-top: 10px;
  text-align: center;
}

.message-loading, .message-empty {
  padding: 20px 0;
  text-align: center;
}
</style> 
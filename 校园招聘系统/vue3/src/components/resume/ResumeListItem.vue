<template>
  <el-card class="resume-list-item" shadow="hover">
    <div class="resume-header">
      <div class="resume-title">
        <h3>{{ resume.resumeName }}</h3>
        <el-tag v-if="resume.isDefault" type="primary" size="small">默认</el-tag>
      </div>
      <div class="resume-time">
        <el-tooltip effect="dark" :content="formatDateTime(resume.updateTime || resume.createTime)" placement="top">
          <span>{{ formatTime(resume.updateTime || resume.createTime) }}</span>
        </el-tooltip>
      </div>
    </div>
    
    <div class="resume-actions">
      <el-button type="primary" @click="handleView">查看</el-button>
      <el-button @click="handleEdit">编辑</el-button>
      <el-dropdown @command="handleCommand" trigger="click">
        <el-button type="primary" >
          更多<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="preview">预览</el-dropdown-item>
        
            <el-dropdown-item v-if="!resume.isDefault" command="setDefault">设为默认</el-dropdown-item>

            <el-dropdown-item command="polish" divided>AI润色</el-dropdown-item>
            <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const props = defineProps({
  resume: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'view', 'edit', 'preview',
  'setDefault', 'polish', 'delete'
])

const handleView = () => {
  emit('view', props.resume)
}

const handleEdit = () => {
  emit('edit', props.resume)
}

const handleCommand = (command) => {
  emit(command, props.resume)
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return formatDistanceToNow(date, { addSuffix: true, locale: zhCN })
  } catch (e) {
    return dateTime
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString()
  } catch (e) {
    return dateTime
  }
}
</script>

<style lang="scss" scoped>
.resume-list-item {
  margin-bottom: 1.5rem;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  border-radius: 4px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;

  &:hover {
    border-color: #8B7355;
    transform: translateY(-2px);
  }
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  border-bottom: 1px solid #D3D3D3;
}

.resume-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;

  h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: normal;
    color: #4A4A4A;
  }

  .el-tag {
    background: transparent;
    border: 1px solid #8B7355;
    color: #8B7355;
    font-size: 0.75rem;
  }
}

.resume-time {
  color: #9B8E83;
  font-size: 0.875rem;
  cursor: pointer;

  &:hover {
    color: #8B7355;
  }
}

.resume-actions {
  display: flex;
  justify-content: flex-start;
 
  gap: 1rem;

  .el-button {
    flex: 1;
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
  }

  .el-dropdown {
    flex: 1;

    .el-button {
      width: 100%;
    }
  }
}

:deep(.el-dropdown-menu) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);

  .el-dropdown-menu__item {
    color: #4A4A4A;

    &:hover {
      background: rgba(139, 115, 85, 0.05);
      color: #8B7355;
    }

    &.is-divided {
      border-top: 1px solid #D3D3D3;
    }
  }
}
</style> 
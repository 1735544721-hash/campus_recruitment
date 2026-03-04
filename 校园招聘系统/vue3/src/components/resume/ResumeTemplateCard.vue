<template>
  <el-card class="resume-template-card" :body-style="{ padding: '0' }">
    <div class="template-preview">
      <img :src="template.templatePreview" alt="模板预览" class="preview-image">
      <div class="template-overlay">
        <el-button type="primary" @click="handleSelect">选择模板</el-button>
      </div>
    </div>
    <div class="template-info">
      <h3 class="template-name">{{ template.templateName }}</h3>
      <div class="template-type">{{ getTemplateTypeName(template.templateType) }}</div>
      <div v-if="template.isDefault" class="default-tag">默认</div>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  template: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select'])

const handleSelect = () => {
  emit('select', props.template)
}

const getTemplateTypeName = (type) => {
  const types = {
    'professional': '专业型',
    'creative': '创意型',
    'simple': '简约型'
  }
  return types[type] || type
}
</script>

<style scoped>
.resume-template-card {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.resume-template-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.template-preview {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s;
}

.template-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.template-preview:hover .template-overlay {
  opacity: 1;
}

.template-info {
  padding: 15px;
  position: relative;
}

.template-name {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 600;
}

.template-type {
  color: #666;
  font-size: 14px;
}

.default-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  background-color: #409EFF;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
</style> 
<template>
  <div class="resume-polish-page">
    <div class="page-header">
      <h1>AI智能润色简历</h1>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">应用修改</el-button>
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else class="polish-container">
      <el-card class="config-card">
        <template #header>
          <div class="card-header">
            <span>润色配置</span>
          </div>
        </template>
        
        <el-form :model="polishForm" label-width="120px">
          <el-form-item label="润色部分">
            <el-radio-group v-model="polishForm.targetPart">
              <el-radio label="selfEvaluation">自我评价</el-radio>
              <el-radio label="workExperience">工作经历</el-radio>
              <el-radio label="projectExperience">项目经历</el-radio>
              <el-radio label="skills">技能特长</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="语言风格">
            <el-select v-model="polishForm.styleType" placeholder="请选择语言风格">
              <el-option label="专业型" value="professional"></el-option>
              <el-option label="技术型" value="technical"></el-option>
              <el-option label="管理型" value="management"></el-option>
              <el-option label="创意型" value="creative"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="目标职位">
            <el-input v-model="polishForm.targetPosition" placeholder="请输入目标职位，用于关键词优化"></el-input>
          </el-form-item>
          
          <el-form-item label="自定义提示">
            <el-input 
              v-model="polishForm.customPrompt" 
              type="textarea" 
              :rows="2" 
              placeholder="可选：输入额外的润色指导，如'使用更专业的IT术语'">
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="startPolish" :loading="polishing" :disabled="!canPolish">
              开始润色
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <div v-if="polishResult" class="result-container">
        <!-- 总体评价部分 -->
        <div v-if="polishResult.overallEvaluation" class="overall-evaluation">
          <h3>总体评价</h3>
          <div class="evaluation-content">{{ polishResult.overallEvaluation }}</div>
        </div>
        
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane v-for="(part, key) in displayParts" :key="key" :label="getPartLabel(key)" :name="key">
            <div class="polish-result">
              <div class="comparison-header">
                <h3>原始内容</h3>
                <h3>润色内容</h3>
              </div>
              <div class="comparison-content">
                <div class="original-content">
                  <div v-if="Array.isArray(polishResult.originalContent[key])">
                    <div v-for="(item, index) in polishResult.originalContent[key]" :key="index" class="content-item">
                      <div class="item-title">{{ getItemTitle(item, key) }}</div>
                      <div class="item-content">{{ getItemContent(item, key) }}</div>
                    </div>
                  </div>
                  <div v-else class="content-text">{{ polishResult.originalContent[key] || '无内容' }}</div>
                </div>
                
                <div class="polished-content">
                  <div v-if="Array.isArray(polishResult.polishedContent)">
                    <div v-for="(item, index) in polishResult.polishedContent" :key="index" class="content-item">
                      <div class="item-title">{{ getItemTitle(item, key) }}</div>
                      <div class="item-content">
                        <el-input
                          v-model="polishResult.polishedContent[index].description"
                          type="textarea"
                          :rows="5"
                        ></el-input>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="applyOriginal(key, index)">恢复原内容</el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else-if="Array.isArray(polishResult.polishedContent[key])">
                    <div v-for="(item, index) in polishResult.polishedContent[key]" :key="index" class="content-item">
                      <div class="item-title">{{ getItemTitle(item, key) }}</div>
                      <div class="item-content">
                        <el-input
                          v-model="polishResult.polishedContent[key][index].description"
                          type="textarea"
                          :rows="5"
                        ></el-input>
                      </div>
                      <div class="item-actions">
                        <el-button size="small" @click="applyOriginal(key, index)">恢复原内容</el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else-if="key === 'selfEvaluation'" class="content-text">
                    <el-input
                      v-model="polishResult.polishedContent"
                      type="textarea"
                      :rows="8"
                    ></el-input>
                    <div class="text-actions">
                      <el-button size="small" @click="applyOriginalText(key)">恢复原内容</el-button>
                    </div>
                  </div>
                  <div v-else-if="typeof polishResult.polishedContent[key] === 'string'" class="content-text">
                    <el-input
                      v-model="polishResult.polishedContent[key]"
                      type="textarea"
                      :rows="8"
                    ></el-input>
                    <div class="text-actions">
                      <el-button size="small" @click="applyOriginalText(key)">恢复原内容</el-button>
                    </div>
                  </div>
                  <div v-else class="content-text">
                    <div class="error-message">
                      <p>润色内容格式不正确，无法显示</p>
                      <p>原始类型: {{ typeof polishResult.polishedContent[key] }}</p>
                      <p>值: {{ JSON.stringify(polishResult.polishedContent[key]) }}</p>
                    </div>
                    <div class="text-actions">
                      <el-button size="small" @click="applyOriginalText(key)">恢复原内容</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="suggestions">
              <h3>改进建议</h3>
              <div v-if="getPartSuggestions(key).length > 0">
                <div v-for="(suggestion, index) in getPartSuggestions(key)" :key="index" class="suggestion-item">
                  <el-tag size="small" type="info">建议</el-tag>
                  <span>{{ suggestion.suggestion }}</span>
                </div>
              </div>
              <div v-else class="no-suggestions">
                <p>暂无针对此部分的具体建议</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <div class="apply-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">应用修改</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumeDetail, polishResume, applyPolishResult } from '@/utils/resumeApi'

const route = useRoute()
const router = useRouter()
const resumeId = computed(() => route.params.id)

const loading = ref(true)
const polishing = ref(false)
const saving = ref(false)
const resume = ref(null)
const polishResult = ref(null)
const activeTab = ref('selfEvaluation')

// 润色表单
const polishForm = reactive({
  resumeId: '',
  polishType: 'partial', // 保留字段但固定为partial
  targetPart: 'selfEvaluation', // 單個目標部分
  styleType: 'professional',
  targetPosition: '',
  customPrompt: ''
})

// 是否可以开始润色
const canPolish = computed(() => {
  if (polishForm.targetPart === '') {
    return false
  }
  return polishForm.targetPosition.trim() !== ''
})

// 获取建议
const getPartSuggestions = (key) => {
  if (!polishResult.value || !polishResult.value.suggestion) {
    console.log('没有建议数据或polishResult为空')
    return []
  }
  
  // 将单个建议转换为数组格式以保持界面兼容性
  const suggestion = {
    part: key,
    suggestion: polishResult.value.suggestion
  }
  
  return [suggestion]
}

// 要显示的部分
const displayParts = computed(() => {
  console.log('计算displayParts, polishResult:', polishResult.value)
  if (!polishResult.value) {
    console.warn('polishResult为空')
    return {}
  }
  if (!polishResult.value.originalContent) {
    console.warn('originalContent为空')
    return {}
  }
  if (!polishResult.value.polishedContent) {
    console.warn('polishedContent为空')
    return {}
  }
  
  console.log('原始内容:', polishResult.value.originalContent)
  console.log('润色后内容:', polishResult.value.polishedContent)
  
  const parts = {}
  const part = polishResult.value.targetPart || polishForm.targetPart
  
  // 处理润色内容
  if (part === 'selfEvaluation') {
    // 对于自我评价，直接使用字符串内容
    parts[part] = polishResult.value.polishedContent
  } else {
    // 对于其他类型(工作经历、项目经历、技能特长)
    // 检查polishedContent是否为数组(后端直接返回的格式)
    if (Array.isArray(polishResult.value.polishedContent)) {
      parts[part] = polishResult.value.polishedContent
      console.log('使用数组格式的润色内容:', parts[part])
    } 
    // 兼容嵌套格式
    else if (polishResult.value.polishedContent && polishResult.value.polishedContent[part] !== undefined) {
      parts[part] = polishResult.value.polishedContent[part]
    } else {
      console.warn(`润色内容中缺少 ${part}`)
    }
  }
  
  // 原始内容类似处理
  if (polishResult.value.originalContent[part] !== undefined) {
    // 保持原样
  } else {
    console.warn(`原始内容中缺少 ${part}`)
  }
  return parts
})

onMounted(() => {
  console.log("ResumePolish组件已挂载")
  console.log("路由参数ID:", resumeId.value)
  
  if (resumeId.value) {
    polishForm.resumeId = resumeId.value
    console.log("设置resumeId到表单:", polishForm.resumeId)
    fetchResumeDetail()
  } else {
    loading.value = false
    ElMessage.error('未指定简历ID')
    router.push('/resume')
  }
})

// 获取简历详情
const fetchResumeDetail = () => {
  loading.value = true
  console.log("开始获取简历详情:", resumeId.value)
  
  getResumeDetail(resumeId.value, {
    onSuccess: (data) => {
      console.log("获取简历成功:", data)
      resume.value = data
       loading.value = false
    },
    onError: (error) => {
      console.error("获取简历失败:", error)
      ElMessage.error('获取简历失败')
      router.push('/resume')
    },
  })
}

// 开始润色
const startPolish = () => {
  polishing.value = true
  console.log("开始润色请求，参数:", {
    ...polishForm,
    resumeId: resumeId.value
  })
  
  // 显示友好的加载提示，增加超时提醒
  ElMessage.info({
    message: 'AI正在润色简历，这可能需要10-30秒，请耐心等待...',
    duration: 0,
    showClose: true
  })
  
  // 增加超时检测
  const timeoutTimer = setTimeout(() => {
    if (polishing.value) {
      ElMessage.warning({
        message: '润色时间较长，可能是内容过多导致。如果长时间没有响应，请尝试润色更小的部分',
        duration: 0,
        showClose: true
      })
    }
  }, 25000)
  
  polishResume({
    ...polishForm,
    resumeId: resumeId.value
  }, {
    onSuccess: (data) => {
      console.log("润色成功，返回数据:", data)
      // 关闭所有消息
      ElMessage.closeAll()
      // 清除超时定时器
      clearTimeout(timeoutTimer)
      
      // 详细记录返回的数据结构
      console.log("返回数据结构:", {
        hasPolishedContent: !!data.polishedContent,
        polishedContentType: data.polishedContent ? typeof data.polishedContent : 'undefined',
        hasOriginalContent: !!data.originalContent,
        originalContentType: data.originalContent ? typeof data.originalContent : 'undefined',
        hasSuggestion: !!data.suggestion,
        suggestionType: data.suggestion ? typeof data.suggestion : 'undefined',
        hasTargetPart: !!data.targetPart,
        targetPartValue: data.targetPart,
        hasOverallEvaluation: !!data.overallEvaluation
      })
      
      // 验证返回数据的结构
      if (!data.polishedContent) {
        console.error('润色结果缺少polishedContent字段')
        ElMessage.error('润色结果格式不正确，请重试')
        polishing.value = false
        return
      }
      
      // 后端不返回originalContent，需要从resume中提取
      try {
        // 解析简历内容
        const resumeContent = JSON.parse(resume.value.resumeContent);
        data.originalContent = {};
        data.originalContent[data.targetPart || polishForm.targetPart] = resumeContent[data.targetPart || polishForm.targetPart];
        console.log('从简历中提取的原始内容:', data.originalContent);
      } catch (error) {
        console.error('解析简历内容失败:', error);
        ElMessage.error('解析简历内容失败');
        polishing.value = false;
        return;
      }
      
      // 检查targetPart
      if (!data.targetPart) {
        console.warn('润色结果缺少targetPart字段，使用当前选择的部分');
        data.targetPart = polishForm.targetPart;
      }
      
      // 检查suggestion
      if (!data.suggestion) {
        console.warn('润色结果缺少suggestion字段，将使用默认建议')
        data.suggestion = '简历内容已优化，建议仔细检查并根据目标职位进行进一步调整'
      }
      
      polishResult.value = data
      
      // 记录polishResult的值
      console.log("设置polishResult:", polishResult.value)
      
      // 设置激活的标签页
      activeTab.value = data.targetPart || polishForm.targetPart
      console.log("设置激活标签页为目标部分:", activeTab.value)
      
      ElMessage.success('简历润色完成')
      polishing.value = false
      
      // 滚动到结果区域
      setTimeout(() => {
        const resultContainer = document.querySelector('.result-container')
        if (resultContainer) {
          resultContainer.scrollIntoView({ behavior: 'smooth' })
        }
      }, 100)
    },
    onError: (error) => {
      // 关闭所有消息
      ElMessage.closeAll()
      
      // 清除超时定时器
      clearTimeout(timeoutTimer)
      
      console.error("润色失败:", error)
      
      // 根据错误内容提供更有针对性的提示
      if (error && error.message && error.message.includes('超时')) {
        ElMessage.error({
          message: '润色超时，请尝试以下解决方案：\n1. 选择更小的内容部分润色\n2. 简化简历内容\n3. 稍后再试',
          duration: 0,
          showClose: true
        })
      } else {
        ElMessage.error('润色失败，请稍后再试')
      }
      polishing.value = false
    },
  })
}

// 获取部分标签名称
const getPartLabel = (key) => {
  const labels = {
    selfEvaluation: '自我评价',
    workExperience: '工作经历',
    projectExperience: '项目经历',
    skills: '技能特长'
  }
  return labels[key] || key
}

// 获取项目标题
const getItemTitle = (item, key) => {
  if (key === 'workExperience') {
    return `${item.company} - ${item.position}`
  } else if (key === 'projectExperience') {
    return `${item.projectName} - ${item.role || ''}`
  } else if (key === 'skills') {
    return item.name
  }
  return ''
}

// 获取项目内容
const getItemContent = (item, key) => {
  if (['workExperience', 'projectExperience', 'skills'].includes(key)) {
    return item.description || '无描述'
  }
  return ''
}

// 应用原始内容（数组项）
const applyOriginal = (key, index) => {
  if (!polishResult.value || !polishResult.value.originalContent[key]) return;
  
  // 处理后端返回的直接数组格式
  if (Array.isArray(polishResult.value.polishedContent)) {
    if (index < polishResult.value.polishedContent.length && 
        index < polishResult.value.originalContent[key].length) {
      polishResult.value.polishedContent[index].description = 
        polishResult.value.originalContent[key][index].description;
    }
  } 
  // 处理嵌套格式
  else if (polishResult.value.polishedContent[key]) {
    polishResult.value.polishedContent[key][index].description = 
      polishResult.value.originalContent[key][index].description;
  }
}

// 应用原始文本内容（非数组）
const applyOriginalText = (key) => {
  if (!polishResult.value || !polishResult.value.originalContent[key]) return;
  
  // 自我评价特殊处理
  if (key === 'selfEvaluation') {
    polishResult.value.polishedContent = polishResult.value.originalContent[key];
  } else if (polishResult.value.polishedContent[key] !== undefined) {
    polishResult.value.polishedContent[key] = polishResult.value.originalContent[key];
  }
}

// 重置润色结果
const handleReset = () => {
  ElMessageBox.confirm('确定要重置所有润色内容吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    polishResult.value = null
  }).catch(() => {})
}

// 保存润色结果
const handleSave = () => {
  if (!polishResult.value) return;
  saving.value = true;
  const targetPart = polishResult.value.targetPart || polishForm.targetPart;
  
  // 构造要提交的数据结构
  let polishedContent = {};
  
  // 对于selfEvaluation，直接使用polishedContent
  if (targetPart === 'selfEvaluation') {
    polishedContent[targetPart] = polishResult.value.polishedContent;
  } else {
    // 对于其他类型(工作经历、项目经历、技能特长)，根据实际数据结构处理
    if (Array.isArray(polishResult.value.polishedContent)) {
      // 后端直接返回的是数组格式
      polishedContent[targetPart] = polishResult.value.polishedContent;
    } else if (polishResult.value.polishedContent[targetPart]) {
      // 兼容嵌套格式
      polishedContent[targetPart] = polishResult.value.polishedContent[targetPart];
    }
  }
  
  applyPolishResult({
    resumeId: resumeId.value,
    polishedContent: polishedContent,
    targetPart: targetPart
  }, {
    successMsg: '应用润色成功',
    onSuccess: () => {
      router.push(`/resume/preview/${resumeId.value}`)
    },
    onError: () => {
      ElMessage.error('应用润色失败')
    },
   
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.resume-polish-page {
  padding: 2rem;
  font-family: var(--font-family-base);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h1 {
    margin: 0;
    font-size: 2rem;
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

.header-actions {
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
  }
}

.loading-container {
  padding: 2.5rem;
}

.polish-container {
  max-width: 1200px;
  margin: 0 auto;
}

.config-card {
  margin-bottom: 2rem;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);

  :deep(.el-card__header) {
    border-bottom: 1px solid #D3D3D3;
    padding: 1rem 1.5rem;

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

      span {
        font-size: 1.25rem;
        color: #8B7355;
        font-weight: normal;
      }
    }
  }

  :deep(.el-card__body) {
    padding: 1.5rem;
  }

  :deep(.el-form) {
    .el-form-item {
      margin-bottom: 1.5rem;

      .el-form-item__label {
        color: #4A4A4A;
        font-weight: normal;
        padding-right: 1rem;
      }

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

        input {
          color: #4A4A4A;

          &::placeholder {
            color: #9B8E83;
          }
        }
      }

      .el-textarea__inner {
        background: transparent;
        border: 1px solid #D3D3D3;

        &:hover {
          border-color: #9B8E83;
        }

        &:focus {
          border-color: #8B7355;
        }

        &::placeholder {
          color: #9B8E83;
        }
      }

      .el-radio-group {
        .el-radio {
          margin-right: 1.5rem;
          color: #4A4A4A;

          .el-radio__input.is-checked + .el-radio__label {
            color: #8B7355;
          }

          .el-radio__input.is-checked .el-radio__inner {
            background-color: #8B7355;
            border-color: #8B7355;
          }
        }
      }

      .el-select {
        width: 150px !important;

        .el-input__wrapper {
          background: transparent;
        }
      }
    }

    .el-button {
      background: #8B7355;
      border-color: #8B7355;
      color: #fff;

      &:hover {
        background: darken(#8B7355, 10%);
      }

      &:disabled {
        background: #D3D3D3;
        border-color: #D3D3D3;
      }
    }
  }
}

.result-container {
  margin-top: 2rem;
}

.overall-evaluation {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(139, 115, 85, 0.05);
  border-radius: 4px;
  border-left: 4px solid #8B7355;

  h3 {
    margin: 0 0 1rem;
    font-size: 1.25rem;
    color: #4A4A4A;
    font-weight: normal;
  }

  .evaluation-content {
    color: #9B8E83;
    line-height: 1.8;
  }
}

:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 2rem;
  }

  .el-tabs__nav-wrap::after {
    background-color: #D3D3D3;
  }

  .el-tabs__item {
    color: #9B8E83;
    font-size: 1rem;
    
    &:hover {
      color: #8B7355;
    }
    
    &.is-active {
      color: #8B7355;
    }
  }

  .el-tabs__active-bar {
    background-color: #8B7355;
  }
}

.polish-result {
  margin-top: 1.5rem;
}

.comparison-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1rem;

  h3 {
  margin: 0;
    font-size: 1.125rem;
    color: #4A4A4A;
    font-weight: normal;
  }
}

.comparison-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.original-content,
.polished-content {
  background: rgba(255, 255, 255, 0.5);
  padding: 1.5rem;
  border: 1px solid #D3D3D3;
  border-radius: 4px;
}

.content-item {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px dashed #D3D3D3;

  &:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
  }
}

.item-title {
  font-weight: normal;
  margin-bottom: 0.75rem;
  color: #4A4A4A;
}

.item-content {
  color: #9B8E83;
  line-height: 1.8;

  :deep(.el-textarea__inner) {
    background: transparent;
    border: 1px solid #D3D3D3;

    &:hover {
      border-color: #9B8E83;
    }

    &:focus {
      border-color: #8B7355;
    }
  }
}

.item-actions,
.text-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;

  .el-button {
    border: 1px solid #D3D3D3;
    background: transparent;
    color: #8B7355;

    &:hover {
      border-color: #8B7355;
      background: rgba(139, 115, 85, 0.05);
    }
  }
}

.content-text {
  color: #9B8E83;
  line-height: 1.8;
}

.error-message {
  padding: 1rem;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 4px;
  margin-bottom: 1rem;
  color: #F56C6C;
}

.suggestions {
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(139, 115, 85, 0.05);
  border-radius: 4px;

  h3 {
    margin: 0 0 1rem;
    font-size: 1.25rem;
    color: #4A4A4A;
    font-weight: normal;
}

.suggestion-item {
  display: flex;
    gap: 0.75rem;
    align-items: flex-start;
    margin-bottom: 1rem;

    &:last-child {
      margin-bottom: 0;
}

    .el-tag {
      background: transparent;
      border: 1px solid #8B7355;
      color: #8B7355;
}

    span {
      color: #9B8E83;
      line-height: 1.8;
      flex: 1;
    }
  }
}

.no-suggestions {
  padding: 1rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  color: #9B8E83;
  font-style: italic;
  text-align: center;
}

.apply-actions {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #D3D3D3;

  .el-button {
    min-width: 120px;
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
}
</style> 
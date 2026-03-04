<template>
  <div class="resume-preview-page">
    <div class="preview-header">
      <div class="title">简历预览</div>
      <div class="actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="success" @click="handleEdit" v-if="isOwner">编辑</el-button>
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="15" animated />
    </div>
    
    <div v-else-if="!resume" class="empty-container">
      <el-empty description="未找到简历或链接已过期" />
    </div>
    
    <transition name="fade">
      <div v-if="isResumeReady" class="resume-preview" :class="templateClass">
        <div class="resume-paper">
          <!-- 简历标题 -->
          <h1 class="resume-title">{{ getBasicInfoField('name') || '未命名简历' }}</h1>
          
          <!-- 基本信息 -->
          <transition-group name="fade-list" tag="div" class="resume-sections">
            <div v-if="true" key="basic-info" class="resume-section basic-info">
              <div class="info-row">
                <div v-if="getBasicInfoField('email')" class="info-item">
                  <i class="el-icon-message"></i>
                  <span>{{ getBasicInfoField('email') }}</span>
                </div>
                <div v-if="getBasicInfoField('phone')" class="info-item">
                  <i class="el-icon-phone"></i>
                  <span>{{ getBasicInfoField('phone') }}</span>
                </div>
                <div v-if="getBasicInfoField('address')" class="info-item">
                  <i class="el-icon-location"></i>
                  <span>{{ getBasicInfoField('address') }}</span>
                </div>
              </div>
            </div>
            
            <!-- 教育背景 -->
            <div v-if="getArrayLength('education') > 0" key="education" class="resume-section education">
              <h2 class="section-title">
                <i class="el-icon-school section-icon"></i>
                教育背景
              </h2>
              <transition-group name="fade-list" tag="div" class="section-items">
                <div v-for="(edu, index) in getArray('education')" :key="`edu-${index}`" class="section-item">
                  <div class="item-header">
                    <div class="item-title">
                      <h3>{{ edu?.school || '' }}</h3>
                      <p>{{ (edu?.major || '') + (edu?.major && edu?.degree ? ' | ' : '') + (edu?.degree || '') }}</p>
                    </div>
                    <div class="item-time">
                      <i class="el-icon-date time-icon"></i>
                      {{ (edu?.startDate || '') + (edu?.startDate && edu?.endDate ? ' - ' : '') + (edu?.endDate || '') }}
                    </div>
                  </div>
                  <div v-if="edu?.gpa" class="item-subtitle">GPA: {{ edu.gpa }}</div>
                  <div v-if="edu?.description" class="item-content">{{ edu.description }}</div>
                </div>
              </transition-group>
            </div>
            
            <!-- 工作经历 -->
            <div v-if="getArrayLength('workExperience') > 0" key="work" class="resume-section work">
              <h2 class="section-title">
                <i class="el-icon-office-building section-icon"></i>
                工作经历
              </h2>
              <transition-group name="fade-list" tag="div" class="section-items">
                <div v-for="(work, index) in getArray('workExperience')" :key="`work-${index}`" class="section-item">
                  <div class="item-header">
                    <div class="item-title">
                      <h3>{{ work?.company || '' }}</h3>
                      <p>{{ work?.position || '' }}</p>
                    </div>
                    <div class="item-time">
                      <i class="el-icon-date time-icon"></i>
                      {{ (work?.startDate || '') + (work?.startDate && work?.endDate ? ' - ' : '') + (work?.endDate || '') }}
                    </div>
                  </div>
                  <div v-if="work?.description" class="item-content">{{ work.description }}</div>
                </div>
              </transition-group>
            </div>
            
            <!-- 项目经历 -->
            <div v-if="getArrayLength('projectExperience') > 0" key="projects" class="resume-section projects">
              <h2 class="section-title">
                <i class="el-icon-suitcase section-icon"></i>
                项目经历
              </h2>
              <transition-group name="fade-list" tag="div" class="section-items">
                <div v-for="(project, index) in getArray('projectExperience')" :key="`project-${index}`" class="section-item">
                  <div class="item-header">
                    <div class="item-title">
                      <h3>{{ project?.name || '' }}</h3>
                      <p>{{ project?.role || '' }}</p>
                    </div>
                    <div class="item-time">
                      <i class="el-icon-date time-icon"></i>
                      {{ (project?.startDate || '') + (project?.startDate && project?.endDate ? ' - ' : '') + (project?.endDate || '') }}
                    </div>
                  </div>
                  <div v-if="project?.techStack" class="item-subtitle">
                    <i class="el-icon-cpu"></i>
                    技术栈：{{ project.techStack }}
                  </div>
                  <div v-if="project?.description" class="item-content">{{ project.description }}</div>
                  <div v-if="project?.link" class="item-link">
                    <i class="el-icon-link"></i>
                    <a :href="project.link" target="_blank" rel="noopener noreferrer">{{ project.link }}</a>
                  </div>
                </div>
              </transition-group>
            </div>
            
            <!-- 技能特长 -->
            <div v-if="getArrayLength('skills') > 0" key="skills" class="resume-section skills">
              <h2 class="section-title">
                <i class="el-icon-trophy section-icon"></i>
                技能特长
              </h2>
              <transition-group name="fade-list" tag="div" class="skills-list">
                <div v-for="(skill, index) in getArray('skills')" :key="`skill-${index}`" class="skill-item">
                  <div class="skill-header">
                    <h3>{{ skill?.name || '' }}</h3>
                    <div class="skill-level">
                      <el-rate
                        :model-value="skill?.proficiency || 0"
                        disabled
                        show-score
                        :max="5"
                        :colors="['#FFAD5A', '#FFAD5A', '#FFAD5A']"
                      />
                    </div>
                  </div>
                  <div v-if="skill?.description" class="skill-description">{{ skill.description }}</div>
                </div>
              </transition-group>
            </div>
            
            <!-- 自我评价 -->
            <div v-if="getResumeContentField('selfEvaluation')" key="self-evaluation" class="resume-section self-evaluation">
              <h2 class="section-title">
                <i class="el-icon-user section-icon"></i>
                自我评价
              </h2>
              <div class="section-content">{{ getResumeContentField('selfEvaluation') }}</div>
            </div>

            <!-- 证书 -->
            <div v-if="getArrayLength('certificates') > 0" key="certificates" class="resume-section certificates">
              <h2 class="section-title">
                <i class="el-icon-medal section-icon"></i>
                证书
              </h2>
              <transition-group name="fade-list" tag="div" class="section-items">
                <div v-for="(cert, index) in getArray('certificates')" :key="`cert-${index}`" class="section-item">
                  <div class="item-header">
                    <div class="item-title">
                      <h3>{{ cert?.name || '' }}</h3>
                      <p v-if="cert?.issuingOrganization">{{ cert.issuingOrganization }}</p>
                    </div>
                    <div class="item-time">
                      <i class="el-icon-date time-icon"></i>
                      {{ cert?.issueDate || '' }}
                    </div>
                  </div>
                  <div v-if="cert?.description" class="item-content">{{ cert.description }}</div>
                </div>
              </transition-group>
            </div>

            <!-- 语言能力 -->
            <div v-if="getArrayLength('languages') > 0" key="languages" class="resume-section languages">
              <h2 class="section-title">
                <i class="el-icon-chat-dot-round section-icon"></i>
                语言能力
              </h2>
              <transition-group name="fade-list" tag="div" class="section-items">
                <div v-for="(lang, index) in getArray('languages')" :key="`lang-${index}`" class="section-item">
                  <div class="item-header">
                    <div class="item-title">
                      <h3>{{ lang?.name || '' }}</h3>
                      <p>熟练程度: {{ lang?.proficiency || '' }}</p>
                    </div>
                  </div>
                  <div v-if="lang?.description" class="item-content">{{ lang.description }}</div>
                </div>
              </transition-group>
            </div>

            <!-- 求职意向 -->
            <div v-if="hasJobIntention" key="job-intention" class="resume-section job-intention">
              <h2 class="section-title">
                <i class="el-icon-aim section-icon"></i>
                求职意向
              </h2>
              <div class="section-item">
                <div v-if="getJobIntentionField('position')" class="item-row">
                  <strong><i class="el-icon-position"></i> 期望职位：</strong>{{ getJobIntentionField('position') }}
                </div>
                <div v-if="getJobIntentionField('industry')" class="item-row">
                  <strong><i class="el-icon-office-building"></i> 期望行业：</strong>{{ getJobIntentionField('industry') }}
                </div>
                <div v-if="getJobIntentionField('location')" class="item-row">
                  <strong><i class="el-icon-location-information"></i> 期望地点：</strong>{{ getJobIntentionField('location') }}
                </div>
                <div v-if="getJobIntentionField('salaryExpectation')" class="item-row">
                  <strong><i class="el-icon-money"></i> 薪资期望：</strong>{{ getJobIntentionField('salaryExpectation') }}
                </div>
              </div>
            </div>
          </transition-group>
        </div>
      </div>
    </transition>
    
    <div v-if="!isResumeReady && !loading" class="empty-container">
      <el-empty description="简历数据结构不完整" />
    </div>
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResumeDetail } from '@/utils/resumeApi'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const resumeId = computed(() => route.params.id)
const loading = ref(true)
const resume = ref(null)
const templateClass = ref('template-standard')
const isResumeReady = ref(false)

// 判断是否是简历所有者
const isOwner = computed(() => {
  if (!resume.value || !userStore.userInfo) return false
  return resume.value.studentId === userStore.userInfo.id
})

// 安全获取resumeContent中的字段
const getResumeContentField = (field) => {
  try {
    if (!resume.value || !resume.value.resumeContent) return ''
    return resume.value.resumeContent[field] || ''
  } catch (error) {
    console.error(`Error getting resumeContent field ${field}:`, error)
    return ''
  }
}

// 安全获取basicInfo中的字段
const getBasicInfoField = (field) => {
  try {
    if (!resume.value || !resume.value.resumeContent || !resume.value.resumeContent.basicInfo) return ''
    return resume.value.resumeContent.basicInfo[field] || ''
  } catch (error) {
    console.error(`Error getting basicInfo field ${field}:`, error)
    return ''
  }
}

// 安全获取jobIntention中的字段
const getJobIntentionField = (field) => {
  try {
    if (!resume.value || !resume.value.resumeContent || !resume.value.resumeContent.jobIntention) return ''
    return resume.value.resumeContent.jobIntention[field] || ''
  } catch (error) {
    console.error(`Error getting jobIntention field ${field}:`, error)
    return ''
  }
}

// 安全获取数组
const getArray = (arrayName) => {
  try {
    if (!resume.value || !resume.value.resumeContent || !resume.value.resumeContent[arrayName]) return []
    const array = resume.value.resumeContent[arrayName]
    return Array.isArray(array) ? array : []
  } catch (error) {
    console.error(`Error getting array ${arrayName}:`, error)
    return []
  }
}

// 安全获取数组长度
const getArrayLength = (arrayName) => {
  try {
    const array = getArray(arrayName)
    return Array.isArray(array) ? array.length : 0
  } catch (error) {
    console.error(`Error getting array length for ${arrayName}:`, error)
    return 0
  }
}

// 判断是否有求职意向
const hasJobIntention = computed(() => {
  try {
    if (!resume.value || !resume.value.resumeContent || !resume.value.resumeContent.jobIntention) return false
    const jobIntention = resume.value.resumeContent.jobIntention
    return !!(jobIntention.position || jobIntention.industry || jobIntention.location || jobIntention.salaryExpectation)
  } catch (error) {
    console.error('Error checking job intention:', error)
    return false
  }
})

// 检查简历数据结构是否完整
const checkResumeStructure = () => {
  try {
    if (!resume.value) return false
    if (typeof resume.value !== 'object') return false
    if (!resume.value.resumeContent) return false
    
    // 基本检查通过
    return true
  } catch (error) {
    console.error('Error checking resume structure:', error)
    return false
  }
}

onMounted(() => {
  fetchResumeDetail()
})

// 获取简历详情
const fetchResumeDetail = () => {
  if (!resumeId.value) return
  
  loading.value = true
  isResumeReady.value = false
  
  getResumeDetail(resumeId.value, {
    onSuccess: (data) => {
      console.log('获取到的简历数据:', data)
      
      // 确保data不为空
      if (!data) {
        console.error('获取到的简历数据为空')
        resume.value = null
        loading.value = false
        return
      }
      
      // 处理resumeContent可能是字符串的情况
      try {
        if (typeof data.resumeContent === 'string' && data.resumeContent) {
          try {
            data.resumeContent = JSON.parse(data.resumeContent)
          } catch (error) {
            console.error('解析resumeContent失败:', error)
            data.resumeContent = {}
          }
        }
        
        // 确保resumeContent存在
        if (!data.resumeContent) {
          data.resumeContent = {}
        }
        
        // 确保所有必要的对象和数组都存在
        const content = data.resumeContent
        
        if (!content.basicInfo) content.basicInfo = {}
        if (!content.education) content.education = []
        if (!content.workExperience) content.workExperience = []
        if (!content.projectExperience) content.projectExperience = []
        if (!content.skills) content.skills = []
        if (!content.certificates) content.certificates = []
        if (!content.languages) content.languages = []
        if (!content.jobIntention) content.jobIntention = {}
        if (content.selfEvaluation === undefined) content.selfEvaluation = ''
        
        resume.value = data
        
        // 设置模板样式
        if (data.templateId) {
          templateClass.value = 'template-standard'
        }
        
        // 使用nextTick确保DOM更新后再检查结构
        nextTick(() => {
          isResumeReady.value = checkResumeStructure()
        })
          loading.value = false
      } catch (error) {
        console.error('处理简历数据时出错:', error)
        resume.value = {
          resumeContent: {
            basicInfo: {},
            education: [],
            workExperience: [],
            projectExperience: [],
            skills: [],
            certificates: [],
            languages: [],
            jobIntention: {},
            selfEvaluation: ''
          }
        }
        isResumeReady.value = true
         loading.value = false
      }
    },
    onError: (error) => {
      console.error('获取简历失败:', error)
      ElMessage.error('获取简历失败')
      resume.value = null
      isResumeReady.value = false
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 编辑简历
const handleEdit = () => {
  if (!isOwner.value) {
    ElMessage.warning('只有简历所有者可以编辑')
    return
  }
  router.push(`/resume/edit/${resumeId.value}`)
}
</script>

<style lang="scss" scoped>
.resume-preview-page {
  padding: 2rem;
  background: rgba(255, 255, 255, 0.7);
  min-height: calc(100vh - 120px);
  font-family: var(--font-family-base);
}

.preview-header {
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.7);
  padding: 1.5rem;
  border-radius: 4px;
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);

  .title {
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

.actions {
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

      &.el-button--success {
        background: #67C23A;
        border-color: #67C23A;
        color: #fff;

        &:hover {
          background: darken(#67C23A, 10%);
        }
      }
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

.loading-container, .empty-container {
  max-width: 1000px;
  margin: 1.5rem auto;
  background: rgba(255, 255, 255, 0.7);
  padding: 2rem;
  border-radius: 4px;
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);
}

.resume-preview {
  max-width: 1000px;
  margin: 0 auto;
}

.resume-paper {
  background: rgba(255, 255, 255, 0.7);
  padding: 3rem;
  border-radius: 4px;
  position: relative;
  border: 1px solid #D3D3D3;
  backdrop-filter: blur(10px);
}

.resume-sections {
  margin: 0 auto;
  width: 100%;
}

.resume-section {
  margin-bottom: 2rem;
  position: relative;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  padding: 1.5rem;
  border: 1px solid #D3D3D3;
}

.resume-title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 2rem;
  color: #4A4A4A;
  font-weight: normal;
  position: relative;
  padding-bottom: 1rem;

  &::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
    height: 1px;
    background: #8B7355;
  }
}

.section-title {
  font-size: 1.5rem;
  font-weight: normal;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #D3D3D3;
  color: #4A4A4A;
  display: flex;
  align-items: center;

  &::before {
  content: '';
  display: block;
    width: 2px;
    height: 1rem;
    background: #8B7355;
    margin-right: 0.75rem;
  }
}

.section-item {
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
  border: 1px solid #D3D3D3;

  &:hover {
    border-color: #8B7355;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.item-title {
  h3 {
  margin: 0;
    font-size: 1.25rem;
    color: #4A4A4A;
    font-weight: normal;
}

  p {
    margin: 0.5rem 0 0;
    color: #9B8E83;
    font-size: 1rem;
  }
}

.item-time {
  color: #9B8E83;
  font-size: 0.875rem;
  white-space: nowrap;
  padding: 0.25rem 0.75rem;
  background: rgba(139, 115, 85, 0.05);
  border-radius: 4px;
  border: 1px solid #D3D3D3;
}

.item-subtitle {
  color: #9B8E83;
  margin: 0.75rem 0;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
}

.item-content {
  white-space: pre-line;
  color: #9B8E83;
  font-size: 1rem;
  line-height: 1.8;
  margin-top: 1rem;
}

.item-link {
  margin-top: 1rem;
  display: flex;
  align-items: center;

  a {
    color: #8B7355;
  text-decoration: none;
    font-size: 0.875rem;

    &:hover {
      color: darken(#8B7355, 10%);
  text-decoration: underline;
    }
  }
}

.item-row {
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #9B8E83;

  strong {
  display: inline-flex;
  align-items: center;
  width: 100px;
    color: #4A4A4A;
    font-weight: normal;
  }
}

.basic-info {
  text-align: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  border: 1px solid #D3D3D3;
}

.info-row {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 2rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  border: 1px solid #D3D3D3;
  font-size: 1rem;
  color: #9B8E83;

  &:hover {
    border-color: #8B7355;
    transition: all 0.3s ease;
}

  i {
    color: #8B7355;
    font-size: 1rem;
  }
}

.skills-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.skill-item {
  padding: 1.5rem;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
  border: 1px solid #D3D3D3;

  &:hover {
    border-color: #8B7355;
  }
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;

  h3 {
  margin: 0;
    font-size: 1.25rem;
    color: #4A4A4A;
    font-weight: normal;
  display: flex;
  align-items: center;
  }
}

.skill-level {
  display: flex;
  align-items: center;

  :deep(.el-rate) {
    .el-rate__icon {
      font-size: 1rem;
      margin-right: 0.25rem;
    }

    .el-rate__text {
      color: #9B8E83;
      font-size: 0.875rem;
    }
  }
}

.skill-description {
  margin-top: 0.75rem;
  font-size: 0.875rem;
  color: #9B8E83;
  line-height: 1.6;
}

.section-icon {
  margin-right: 0.75rem;
  color: #8B7355;
  font-size: 1.25rem;
}

.time-icon {
  margin-right: 0.25rem;
  color: #9B8E83;
  font-size: 0.875rem;
}

.item-row i {
  margin-right: 0.25rem;
  color: #8B7355;
}

.item-link i {
  margin-right: 0.25rem;
  color: #8B7355;
}

.item-subtitle i {
  margin-right: 0.25rem;
  color: #8B7355;
  font-size: 0.875rem;
}

.section-content {
  white-space: pre-line;
  line-height: 1.8;
  padding: 1.5rem;
  text-align: left;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  text-indent: 2em;
  font-size: 1rem;
  border: 1px solid #D3D3D3;
  color: #9B8E83;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-list-enter-active,
.fade-list-leave-active {
  transition: all 0.3s ease;
}

.fade-list-enter-from,
.fade-list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.fade-list-move {
  transition: transform 0.3s ease;
}
</style> 
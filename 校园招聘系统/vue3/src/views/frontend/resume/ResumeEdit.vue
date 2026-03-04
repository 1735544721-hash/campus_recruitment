<template>
  <div class="resume-edit-page">
    <div class="page-header">
      <h1>{{ isNewResume ? '新建简历' : '编辑简历' }}</h1>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        <el-button type="success" @click="handlePreview" :disabled="!resumeId">预览</el-button>
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else class="resume-form">
      <el-form 
        ref="resumeFormRef" 
        :model="resumeForm" 
        label-width="120px"
        :rules="rules"
      >
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>基本设置</span>
            </div>
          </template>
          
          <el-form-item label="简历名称" prop="resumeName">
            <el-input v-model="resumeForm.resumeName" placeholder="请输入简历名称" />
          </el-form-item>
          
          <el-form-item label="设为默认简历">
            <el-switch v-model="resumeForm.isDefault" />
          </el-form-item>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          
          <el-form-item label="姓名" prop="resumeContent.basicInfo.name">
            <el-input v-model="resumeForm.resumeContent.basicInfo.name" placeholder="请输入姓名" />
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="resumeForm.resumeContent.basicInfo.gender" placeholder="请选择性别">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期">
                <el-date-picker 
                  v-model="resumeForm.resumeContent.basicInfo.birthDate" 
                  type="date" 
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="resumeContent.basicInfo.email">
                <el-input v-model="resumeForm.resumeContent.basicInfo.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="resumeContent.basicInfo.phone">
                <el-input v-model="resumeForm.resumeContent.basicInfo.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="地址">
            <el-input v-model="resumeForm.resumeContent.basicInfo.address" placeholder="请输入地址" />
          </el-form-item>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>求职意向</span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="期望职位">
                <el-input v-model="resumeForm.resumeContent.jobIntention.position" placeholder="请输入期望职位" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="期望行业">
                <el-input v-model="resumeForm.resumeContent.jobIntention.industry" placeholder="请输入期望行业" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="期望地点">
                <el-input v-model="resumeForm.resumeContent.jobIntention.location" placeholder="请输入期望工作地点" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="薪资期望">
                <el-input v-model="resumeForm.resumeContent.jobIntention.salaryExpectation" placeholder="例如：8k-10k/月" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>教育背景</span>
              <el-button type="primary" link @click="addEducation">添加</el-button>
            </div>
          </template>
          
          <div v-for="(edu, index) in resumeForm.resumeContent.education" :key="index" class="education-item">
            <div class="item-header">
              <h3>教育经历 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeEducation(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'学校名称'" :prop="`resumeContent.education.${index}.school`">
                  <el-input v-model="edu.school" placeholder="请输入学校名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'专业'">
                  <el-input v-model="edu.major" placeholder="请输入专业" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'学历'">
                  <el-select v-model="edu.degree" placeholder="请选择学历">
                    <el-option label="专科" value="专科" />
                    <el-option label="本科" value="本科" />
                    <el-option label="硕士" value="硕士" />
                    <el-option label="博士" value="博士" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'GPA'">
                  <el-input v-model="edu.gpa" placeholder="例如：3.8/4.0" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'开始日期'">
                  <el-date-picker
                    v-model="edu.startDate"
                    type="month"
                    placeholder="选择开始日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'结束日期'">
                  <el-date-picker
                    v-model="edu.endDate"
                    type="month"
                    placeholder="选择结束日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'描述'">
              <el-input
                v-model="edu.description"
                type="textarea"
                placeholder="请输入教育经历描述"
                :rows="3" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.education.length === 0" class="empty-item">
            <el-empty description="暂无教育经历" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>工作/实习经历</span>
              <el-button type="primary" link @click="addWorkExperience">添加</el-button>
            </div>
          </template>
          
          <div v-for="(work, index) in resumeForm.resumeContent.workExperience" :key="index" class="work-item">
            <div class="item-header">
              <h3>工作经历 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeWorkExperience(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'公司名称'">
                  <el-input v-model="work.company" placeholder="请输入公司名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'职位'">
                  <el-input v-model="work.position" placeholder="请输入职位" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'开始日期'">
                  <el-date-picker
                    v-model="work.startDate"
                    type="month"
                    placeholder="选择开始日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'结束日期'">
                  <el-date-picker
                    v-model="work.endDate"
                    type="month"
                    placeholder="选择结束日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'工作内容'">
              <el-input
                v-model="work.description"
                type="textarea"
                placeholder="请输入工作内容描述"
                :rows="3" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.workExperience.length === 0" class="empty-item">
            <el-empty description="暂无工作经历" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>项目经历</span>
              <el-button type="primary" link @click="addProjectExperience">添加</el-button>
            </div>
          </template>
          
          <div v-for="(project, index) in resumeForm.resumeContent.projectExperience" :key="index" class="project-item">
            <div class="item-header">
              <h3>项目经历 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeProjectExperience(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'项目名称'">
                  <el-input v-model="project.name" placeholder="请输入项目名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'担任角色'">
                  <el-input v-model="project.role" placeholder="请输入担任角色" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'开始日期'">
                  <el-date-picker
                    v-model="project.startDate"
                    type="month"
                    placeholder="选择开始日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'结束日期'">
                  <el-date-picker
                    v-model="project.endDate"
                    type="month"
                    placeholder="选择结束日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'项目描述'">
              <el-input
                v-model="project.description"
                type="textarea"
                placeholder="请输入项目描述"
                :rows="3" />
            </el-form-item>
            
            <el-form-item :label="'技术栈'">
              <el-input v-model="project.techStack" placeholder="请输入使用的技术栈" />
            </el-form-item>
            
            <el-form-item :label="'项目链接'">
              <el-input v-model="project.link" placeholder="例如：GitHub链接" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.projectExperience.length === 0" class="empty-item">
            <el-empty description="暂无项目经历" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>技能特长</span>
              <el-button type="primary" link @click="addSkill">添加</el-button>
            </div>
          </template>
          
          <div v-for="(skill, index) in resumeForm.resumeContent.skills" :key="index" class="skill-item">
            <div class="item-header">
              <h3>技能 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeSkill(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'技能名称'">
                  <el-input v-model="skill.name" placeholder="请输入技能名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'熟练程度'">
                  <el-slider v-model="skill.proficiency" :min="1" :max="5" :format-tooltip="formatProficiency" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'技能描述'">
              <el-input
                v-model="skill.description"
                type="textarea"
                placeholder="请输入技能描述"
                :rows="2" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.skills.length === 0" class="empty-item">
            <el-empty description="暂无技能特长" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>证书</span>
              <el-button type="primary" link @click="addCertificate">添加</el-button>
            </div>
          </template>
          
          <div v-for="(cert, index) in resumeForm.resumeContent.certificates" :key="index" class="certificate-item">
            <div class="item-header">
              <h3>证书 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeCertificate(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'证书名称'">
                  <el-input v-model="cert.name" placeholder="请输入证书名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'获得日期'">
                  <el-date-picker
                    v-model="cert.issueDate"
                    type="month"
                    placeholder="选择获得日期"
                    format="YYYY-MM"
                    value-format="YYYY-MM" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'颁发机构'">
              <el-input v-model="cert.issuingOrganization" placeholder="请输入颁发机构" />
            </el-form-item>
            
            <el-form-item :label="'证书描述'">
              <el-input
                v-model="cert.description"
                type="textarea"
                placeholder="请输入证书描述"
                :rows="2" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.certificates.length === 0" class="empty-item">
            <el-empty description="暂无证书" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>语言能力</span>
              <el-button type="primary" link @click="addLanguage">添加</el-button>
            </div>
          </template>
          
          <div v-for="(lang, index) in resumeForm.resumeContent.languages" :key="index" class="language-item">
            <div class="item-header">
              <h3>语言 #{{ index + 1 }}</h3>
              <el-button type="danger" link @click="removeLanguage(index)">删除</el-button>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="'语言名称'">
                  <el-input v-model="lang.name" placeholder="请输入语言名称，如英语、日语等" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="'熟练程度'">
                  <el-select v-model="lang.proficiency" placeholder="请选择熟练程度">
                    <el-option label="入门" value="入门" />
                    <el-option label="初级" value="初级" />
                    <el-option label="中级" value="中级" />
                    <el-option label="高级" value="高级" />
                    <el-option label="精通/母语" value="精通/母语" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="'补充说明'">
              <el-input
                v-model="lang.description"
                type="textarea"
                placeholder="请输入语言能力补充说明，如证书等"
                :rows="2" />
            </el-form-item>
          </div>
          
          <div v-if="resumeForm.resumeContent.languages.length === 0" class="empty-item">
            <el-empty description="暂无语言能力" />
          </div>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>自我评价</span>
            </div>
          </template>
          
          <el-form-item>
            <el-input
              v-model="resumeForm.resumeContent.selfEvaluation"
              type="textarea"
              placeholder="请输入自我评价"
              :rows="5" />
          </el-form-item>
        </el-card>
        
        <div class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumeDetail, createResume, updateResume } from '@/utils/resumeApi'

const route = useRoute()
const router = useRouter()
const resumeId = computed(() => route.params.id)
const isNewResume = computed(() => resumeId.value === 'new')

const loading = ref(false)
const saving = ref(false)
const resumeFormRef = ref(null)

// 初始化一个空的简历表单
const initResumeForm = () => {
  return {
    resumeName: '',
    isDefault: false,
    resumeContent: {
      basicInfo: {
        name: '',
        gender: '',
        birthDate: '',
        email: '',
        phone: '',
        address: '',
        photo: ''
      },
      education: [],
      workExperience: [],
      projectExperience: [],
      skills: [],
      selfEvaluation: '',
      jobIntention: {},
      certificates: [],
      languages: []
    }
  }
}

const resumeForm = reactive(initResumeForm())

// 表单验证规则
const rules = {
  resumeName: [
    { required: true, message: '请输入简历名称', trigger: 'blur' },
    { max: 50, message: '简历名称不能超过50个字符', trigger: 'blur' }
  ],
  'resumeContent.basicInfo.name': [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  'resumeContent.basicInfo.email': [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  'resumeContent.basicInfo.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (!isNewResume.value) {
    fetchResumeDetail()
  } else {
    // 对于新简历，确保数据结构正确
    checkAndFixDataStructure()
  }
})

// 获取简历详情
const fetchResumeDetail = () => {
  loading.value = true
  getResumeDetail(resumeId.value, {
    onSuccess: (data) => {
      console.log('原始数据:', data)
      
      // 将后端返回的数据填充到表单
      Object.assign(resumeForm, data)
      
      // 处理resumeContent可能是字符串的情况
      if (typeof resumeForm.resumeContent === 'string') {
        try {
          resumeForm.resumeContent = JSON.parse(resumeForm.resumeContent)
          console.log('解析后的resumeContent:', resumeForm.resumeContent)
        } catch (error) {
          console.error('解析resumeContent失败:', error)
          resumeForm.resumeContent = {}
        }
      }
      
      // 确保resumeContent各个部分都存在
      if (!resumeForm.resumeContent || Object.keys(resumeForm.resumeContent).length === 0) {
        resumeForm.resumeContent = {
          basicInfo: {
            name: '',
            gender: '',
            birthDate: '',
            email: '',
            phone: '',
            address: '',
            photo: ''
          },
          education: [],
          workExperience: [],
          projectExperience: [],
          skills: [],
          selfEvaluation: '',
          jobIntention: {},
          certificates: [],
          languages: []
        }
        loading.value = false
        return 
      }
      
      const content = resumeForm.resumeContent
      
      // 处理后端返回的数据结构与前端不一致的情况
      // 处理personalInfo到basicInfo的映射
      if (content.personalInfo && !content.basicInfo) {
        content.basicInfo = {
          name: content.personalInfo.name || '',
          gender: content.personalInfo.gender || '',
          birthDate: content.personalInfo.birthDate || '',
          email: content.personalInfo.email || '',
          phone: content.personalInfo.phone || '',
          address: content.personalInfo.address || '',
          photo: content.personalInfo.photo || ''
        }
        // 删除personalInfo以避免混淆
        delete content.personalInfo;
      }
      
      // 处理experience到workExperience的映射
      if (content.experience && !content.workExperience) {
        content.workExperience = content.experience
          .filter(exp => exp.type === '工作经历')
          .map(exp => ({
            company: exp.name || '',
            position: exp.role || '',
            startDate: exp.startTime || '',
            endDate: exp.endTime || '',
            description: exp.description || ''
          }))
      }
      
      // 处理experience中的项目经历到projectExperience的映射
      if (content.experience && !content.projectExperience) {
        content.projectExperience = content.experience
          .filter(exp => exp.type === '项目经历')
          .map(exp => ({
            name: exp.name || '',
            role: exp.role || '',
            startDate: exp.startTime || '',
            endDate: exp.endTime || '',
            description: exp.description || '',
            techStack: exp.techStack || '',
            link: exp.link || ''
          }))
      }
      
      // 处理skills中的技能到skills的映射
      if (content.skills) {
        const skillItems = content.skills
          .filter(skill => skill.type === '技能')
          .map(skill => ({
            name: skill.name || '',
            proficiency: skill.level ? parseInt(skill.level) || 3 : 3,
            description: skill.description || ''
          }))
        
        if (skillItems.length > 0) {
          content.skills = skillItems
        }
      }
      
      // 处理skills中的证书到certificates的映射
      if (content.skills && !content.certificates) {
        content.certificates = content.skills
          .filter(skill => skill.type === '证书')
          .map(cert => ({
            name: cert.name || '',
            issueDate: cert.date || '',
            issuingOrganization: '',
            description: cert.description || ''
          }))
      }
      
      // 即使resumeContent不为空，也需要检查每个子对象是否存在
      if (!content.basicInfo) content.basicInfo = {
        name: '',
        gender: '',
        birthDate: '',
        email: '',
        phone: '',
        address: '',
        photo: ''
      }
      if (!content.education) content.education = []
      if (!content.workExperience) content.workExperience = []
      if (!content.projectExperience) content.projectExperience = []
      if (!content.skills) content.skills = []
      if (!content.selfEvaluation) content.selfEvaluation = ''
      if (!content.jobIntention) content.jobIntention = {}
      if (!content.certificates) content.certificates = []
      if (!content.languages) content.languages = []
      
      loading.value = false
    },
    onError: (error) => {
      console.error('获取简历详情失败:', error)
      ElMessage.error('获取简历详情失败')
      loading.value = false
      router.push('/resume')
    },
    onFinally: () => {
     
    }
  })
}

// 检查并修复数据结构
const checkAndFixDataStructure = () => {
  if (!resumeForm.resumeContent) {
    resumeForm.resumeContent = {
      basicInfo: {
        name: '',
        gender: '',
        birthDate: '',
        email: '',
        phone: '',
        address: '',
        photo: ''
      },
      education: [],
      workExperience: [],
      projectExperience: [],
      skills: [],
      selfEvaluation: '',
      jobIntention: {},
      certificates: [],
      languages: []
    }
    return false
  }

  const content = resumeForm.resumeContent
  let structureFixed = false

  // 检查并修复basicInfo
  if (!content.basicInfo) {
    content.basicInfo = {
      name: '',
      gender: '',
      birthDate: '',
      email: '',
      phone: '',
      address: '',
      photo: ''
    }
    structureFixed = true
  }

  // 确保所有必要的数组都存在
  if (!content.education) {
    content.education = []
    structureFixed = true
  }
  if (!content.workExperience) {
    content.workExperience = []
    structureFixed = true
  }
  if (!content.projectExperience) {
    content.projectExperience = []
    structureFixed = true
  }
  if (!content.skills) {
    content.skills = []
    structureFixed = true
  }
  if (!content.certificates) {
    content.certificates = []
    structureFixed = true
  }
  if (!content.languages) {
    content.languages = []
    structureFixed = true
  }

  // 确保所有必要的字段都存在
  if (content.selfEvaluation === undefined) {
    content.selfEvaluation = ''
    structureFixed = true
  }
  if (!content.jobIntention) {
    content.jobIntention = {}
    structureFixed = true
  }

  return structureFixed
}

// 保存简历
const handleSave = () => {
  // 确保resumeContent的结构是正确的
  try {
    const structureFixed = checkAndFixDataStructure()
    if (structureFixed) {
      console.log('数据结构已修复')
    }

    resumeFormRef.value.validate((valid) => {
      if (valid) {
        saving.value = true
        
        const saveAction = isNewResume.value
          ? createResume(resumeForm)
          : updateResume(resumeId.value, resumeForm)
        
        saveAction.then(res => {
          ElMessage.success('保存成功')
          router.push('/resume')
        }).catch((error) => {
          console.error('保存简历失败:', error)
          ElMessage.error('保存失败: ' + (error.message || '未知错误'))
        }).finally(() => {
          saving.value = false
        })
      } else {
        ElMessage.warning('表单验证未通过，请检查必填项')
        return false
      }
    })
  } catch (error) {
    console.error('表单验证前出错:', error)
    ElMessage.error('表单数据结构有误，请刷新页面重试')
  }
}

// 取消编辑
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消编辑吗？未保存的内容将会丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/resume')
  }).catch(() => {})
}

// 预览简历
const handlePreview = () => {
  if (!resumeId.value || isNewResume.value) {
    ElMessage.warning('请先保存简历后再预览')
    return
  }
  window.open(`/resume/preview/${resumeId.value}`, '_blank')
}

// 添加教育经历
const addEducation = () => {
  resumeForm.resumeContent.education.push({
    school: '',
    major: '',
    degree: '',
    startDate: '',
    endDate: '',
    gpa: '',
    description: ''
  })
}

// 移除教育经历
const removeEducation = (index) => {
  resumeForm.resumeContent.education.splice(index, 1)
}

// 添加工作经历
const addWorkExperience = () => {
  resumeForm.resumeContent.workExperience.push({
    company: '',
    position: '',
    startDate: '',
    endDate: '',
    description: ''
  })
}

// 移除工作经历
const removeWorkExperience = (index) => {
  resumeForm.resumeContent.workExperience.splice(index, 1)
}

// 添加项目经历
const addProjectExperience = () => {
  resumeForm.resumeContent.projectExperience.push({
    name: '',
    role: '',
    startDate: '',
    endDate: '',
    description: '',
    techStack: '',
    link: ''
  })
}

// 移除项目经历
const removeProjectExperience = (index) => {
  resumeForm.resumeContent.projectExperience.splice(index, 1)
}

// 添加技能
const addSkill = () => {
  resumeForm.resumeContent.skills.push({
    name: '',
    proficiency: 3,
    description: ''
  })
}

// 移除技能
const removeSkill = (index) => {
  resumeForm.resumeContent.skills.splice(index, 1)
}

// 添加证书
const addCertificate = () => {
  resumeForm.resumeContent.certificates.push({
    name: '',
    issueDate: '',
    issuingOrganization: '',
    description: ''
  })
}

// 移除证书
const removeCertificate = (index) => {
  resumeForm.resumeContent.certificates.splice(index, 1)
}

// 添加语言能力
const addLanguage = () => {
  resumeForm.resumeContent.languages.push({
    name: '',
    proficiency: '入门',
    description: ''
  })
}

// 移除语言能力
const removeLanguage = (index) => {
  resumeForm.resumeContent.languages.splice(index, 1)
}

// 格式化熟练程度
const formatProficiency = (val) => {
  const levels = ['入门', '初级', '中级', '高级', '精通']
  return levels[val - 1]
}
</script>

<style lang="scss" scoped>
.resume-edit-page {
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

    &.el-button--success {
      background: #67C23A;
      border-color: #67C23A;
      color: #fff;

      &:hover {
        background: darken(#67C23A, 10%);
      }

      &:disabled {
        background: #B3E19D;
        border-color: #B3E19D;
        color: #fff;
      }
    }
  }
}

.loading-container {
  padding: 2.5rem;
}

.resume-form {
  max-width: 1000px;
  margin: 0 auto;
}

.form-card {
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

      .el-button {
        color: #8B7355;
        padding: 0;

        &:hover {
          color: darken(#8B7355, 10%);
        }
      }
    }
  }

  :deep(.el-card__body) {
    padding: 1.5rem;
  }

  :deep(.el-form-item) {
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

    .el-select {  
      width: 150px !important;

      .el-input__wrapper {
        background: transparent;
      }
    }

    .el-date-editor {
      width: 100%;

      .el-input__wrapper {
        background: transparent;
      }
    }

    .el-switch {
      --el-switch-on-color: #8B7355;
    }
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px dashed #D3D3D3;

  h3 {
    margin: 0;
    font-size: 1rem;
    color: #4A4A4A;
    font-weight: normal;
  }

  .el-button {
    color: #F56C6C;
    padding: 0;

    &:hover {
      color: darken(#F56C6C, 10%);
    }
  }
}

.education-item,
.work-item,
.project-item,
.skill-item,
.certificate-item,
.language-item {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid #D3D3D3;
  border-radius: 4px;

  &:last-child {
    margin-bottom: 0;
  }
}

.empty-item {
  padding: 2rem 0;
  text-align: center;
  color: #9B8E83;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 3rem;

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
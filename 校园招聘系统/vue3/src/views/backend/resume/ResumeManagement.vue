<template>
  <div class="resume-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>简历管理</span>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="学生姓名/简历名称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="resumeList" stripe>
        <el-table-column prop="id" label="ID"></el-table-column>
        <el-table-column prop="studentName" label="学生姓名"></el-table-column>
        <el-table-column prop="resumeName" label="简历名称"></el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row.id)">查看</el-button>
            <el-button size="small" type="danger" @click="deleteResume(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </el-card>

    <el-dialog v-model="resumeDialogVisible" title="简历预览" width="80%">
      <div v-loading="resumeLoading">
        <div v-if="resumeData" class="resume-content">
          <div class="resume-header">
            <h2>{{ resumeData.resumeName }}</h2>
          </div>
          <el-card class="resume-section" shadow="hover">
            <template #header>
              <div class="resume-section-header">
                <el-icon><UserFilled /></el-icon>
                <span>基本信息</span>
              </div>
            </template>
            <div v-if="resumeData.resumeContent.basicInfo">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="姓名">{{ resumeData.resumeContent.basicInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ resumeData.resumeContent.basicInfo.gender }}</el-descriptions-item>
                <el-descriptions-item label="出生日期">{{ resumeData.resumeContent.basicInfo.birthDate }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ resumeData.resumeContent.basicInfo.email }}</el-descriptions-item>
                <el-descriptions-item label="电话">{{ resumeData.resumeContent.basicInfo.phone }}</el-descriptions-item>
                <el-descriptions-item label="地址">{{ resumeData.resumeContent.basicInfo.address }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.jobIntention">
            <template #header>
              <div class="resume-section-header">
                <el-icon><Aim /></el-icon>
                <span>求职意向</span>
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="职位">{{ resumeData.resumeContent.jobIntention.position }}</el-descriptions-item>
              <el-descriptions-item label="行业">{{ resumeData.resumeContent.jobIntention.industry }}</el-descriptions-item>
              <el-descriptions-item label="地点">{{ resumeData.resumeContent.jobIntention.location }}</el-descriptions-item>
              <el-descriptions-item label="薪资">{{ resumeData.resumeContent.jobIntention.salaryExpectation }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.education && resumeData.resumeContent.education.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><School /></el-icon>
                <span>教育经历</span>
              </div>
            </template>
            <div v-for="(edu, index) in resumeData.resumeContent.education" :key="index" class="resume-item">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="学校">{{ edu.school }}</el-descriptions-item>
                <el-descriptions-item label="专业">{{ edu.major }}</el-descriptions-item>
                <el-descriptions-item label="学位">{{ edu.degree }}</el-descriptions-item>
                <el-descriptions-item label="时间">{{ edu.startDate }} - {{ edu.endDate }}</el-descriptions-item>
                <el-descriptions-item label="GPA">{{ edu.gpa }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">{{ edu.description }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
          <!-- 添加类似卡片 for 工作经历、项目、技能、证书、语言、自我评价 -->
          <!-- 例如工作经历 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.workExperience && resumeData.resumeContent.workExperience.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><Briefcase /></el-icon>
                <span>工作经历</span>
              </div>
            </template>
            <div v-for="(work, index) in resumeData.resumeContent.workExperience" :key="index" class="resume-item">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="公司">{{ work.company }}</el-descriptions-item>
                <el-descriptions-item label="职位">{{ work.position }}</el-descriptions-item>
                <el-descriptions-item label="时间">{{ work.startDate }} - {{ work.endDate }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">{{ work.description }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
          <!-- 项目经验 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.projectExperience && resumeData.resumeContent.projectExperience.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><Connection /></el-icon>
                <span>项目经验</span>
              </div>
            </template>
            <div v-for="(project, index) in resumeData.resumeContent.projectExperience" :key="index" class="resume-item">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="项目名称">{{ project.name }}</el-descriptions-item>
                <el-descriptions-item label="角色">{{ project.role }}</el-descriptions-item>
                <el-descriptions-item label="时间">{{ project.startDate }} - {{ project.endDate }}</el-descriptions-item>
                <el-descriptions-item label="技术栈">{{ project.techStack }}</el-descriptions-item>
                <el-descriptions-item label="链接">{{ project.link }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">{{ project.description }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
          <!-- 技能 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.skills && resumeData.resumeContent.skills.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><SetUp /></el-icon>
                <span>专业技能</span>
              </div>
            </template>
            <el-table :data="resumeData.resumeContent.skills" border style="width: 100%">
              <el-table-column prop="name" label="技能名称" />
              <el-table-column label="熟练度" width="300">
                <template #default="{ row }">
                  <el-progress :percentage="row.proficiency * 10" :format="p => `${p}%`" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
            </el-table>
          </el-card>
          <!-- 证书 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.certificates && resumeData.resumeContent.certificates.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><Medal /></el-icon>
                <span>证书</span>
              </div>
            </template>
            <el-table :data="resumeData.resumeContent.certificates" border style="width: 100%">
              <el-table-column prop="name" label="证书名称" />
              <el-table-column prop="issueDate" label="获得日期" />
              <el-table-column prop="issuingOrganization" label="颁发机构" />
              <el-table-column prop="description" label="描述" />
            </el-table>
          </el-card>
          <!-- 语言能力 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.languages && resumeData.resumeContent.languages.length > 0">
            <template #header>
              <div class="resume-section-header">
                <el-icon><ChatDotRound /></el-icon>
                <span>语言能力</span>
              </div>
            </template>
            <el-table :data="resumeData.resumeContent.languages" border style="width: 100%">
              <el-table-column prop="name" label="语言" />
              <el-table-column prop="proficiency" label="熟练度" />
              <el-table-column prop="description" label="描述" />
            </el-table>
          </el-card>
          <!-- 自我评价 -->
          <el-card class="resume-section" shadow="hover" v-if="resumeData.resumeContent.selfEvaluation">
            <template #header>
              <div class="resume-section-header">
                <el-icon><User /></el-icon>
                <span>自我评价</span>
              </div>
            </template>
            <div class="self-evaluation">{{ resumeData.resumeContent.selfEvaluation }}</div>
          </el-card>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { UserFilled, Aim, School, Briefcase, Connection, SetUp, Medal, ChatDotRound, User } from '@element-plus/icons-vue'

const searchForm = ref({ keyword: '' })
const resumeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = () => {
    const params = {
        currentPage: currentPage.value,
        size: pageSize.value,
        keyword: searchForm.value.keyword
    }
    console.log(params) 
  request.get('/resume/admin/page', params,{
    onSuccess:(data)=>{
        resumeList.value = data.records||[]
        total.value = data.total||0
    },
    onError:()=>{
        ElMessage.error('加载失败')
    }
  })
}

const search = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const resumeDialogVisible = ref(false)
const resumeLoading = ref(false)
const resumeData = ref(null)

const viewDetail = (id) => {
  resumeLoading.value = true
  resumeDialogVisible.value = true
  request.get(`/resume/${id}`,null,{
    onSuccess:(data)=>{
        if(data.resumeContent && typeof data.resumeContent === 'string'){
            try{
                data.resumeContent = JSON.parse(data.resumeContent)
            }catch(e){
                ElMessage.error('简历内容解析失败')
            }
        }
        resumeData.value = data
        resumeLoading.value = false
    },
    onError:()=>{
        ElMessage.error('加载详情失败')
    }
  })
}

const deleteResume = (id) => {
  ElMessageBox.confirm('确定删除此简历吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/resume/${id}`,{
      showDefaultMsg:false,
      onSuccess:()=>{
        ElMessage.success('删除成功')
        loadData()
      },
      onError:()=>{
        ElMessage.error('删除失败')
      }
    })
  }).catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.resume-management { padding: 20px; }
.card-header { font-size: 1.5em; color: #ff69b4; font-weight: bold; text-align: center; }
.el-table { margin-top: 20px; }
.el-pagination { margin-top: 20px; justify-content: center; }
@media (max-width: 768px) { .el-form-item { width: 100%; } }
</style> 
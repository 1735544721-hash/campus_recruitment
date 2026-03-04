<template>
  <div class="position-form-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>{{ isEdit ? '编辑职位' : '发布职位' }}</h2>
      </div>
    </div>

    <el-card v-loading="loading">
      <el-form 
        ref="formRef"
        :model="positionForm"
        :rules="rules"
        label-width="100px"
        label-position="top"
      >
        <!-- 基本信息 -->
        <h3 class="form-section-title">基本信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="positionName">
              <el-input v-model="positionForm.positionName" placeholder="请输入职位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位类型" prop="positionType">
              <el-select v-model="positionForm.positionType" placeholder="请选择职位类型" style="width: 100%">
                <el-option label="全职" value="全职" />
                <el-option label="兼职" value="兼职" />
                <el-option label="实习" value="实习" />
                <el-option label="校招" value="校招" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位类别" prop="positionCategory">
              <el-select v-model="positionForm.positionCategory" placeholder="请选择职位类别" style="width: 100%">
                <el-option label="技术" value="技术" />
                <el-option label="产品" value="产品" />
                <el-option label="设计" value="设计" />
                <el-option label="运营" value="运营" />
                <el-option label="市场" value="市场" />
                <el-option label="销售" value="销售" />
                <el-option label="财务" value="财务" />
                <el-option label="人事" value="人事" />
                <el-option label="行政" value="行政" />
                <el-option label="法务" value="法务" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workAddress">
              <el-input v-model="positionForm.workAddress" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资" prop="salaryMin">
              <el-input-number v-model="positionForm.salaryMin" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资" prop="salaryMax">
              <el-input-number v-model="positionForm.salaryMax" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历要求" prop="educationRequirement">
              <el-select v-model="positionForm.educationRequirement" placeholder="请选择学历要求" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验要求" prop="experienceRequirement">
              <el-select v-model="positionForm.experienceRequirement" placeholder="请选择经验要求" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="应届生" value="应届生" />
                <el-option label="1年以内" value="1年以内" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
                <el-option label="5-10年" value="5-10年" />
                <el-option label="10年以上" value="10年以上" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="positionCount">
              <el-input-number v-model="positionForm.positionCount" :min="1" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期" prop="deadline">
              <el-date-picker 
                v-model="positionForm.deadline"
                type="date"
                placeholder="请选择截止日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 职位描述 -->
        <h3 class="form-section-title">职位描述</h3>
        <el-form-item prop="positionDescription">
          <el-input
            v-model="positionForm.positionDescription"
            type="textarea"
            :rows="6"
            placeholder="请输入职位描述，包括岗位职责等"
          />
        </el-form-item>

        <!-- 职位要求 -->
        <h3 class="form-section-title">职位要求</h3>
        <el-form-item prop="positionRequirement">
          <el-input
            v-model="positionForm.positionRequirement"
            type="textarea"
            :rows="6"
            placeholder="请输入职位要求，包括技能要求、素质要求等"
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '保存修改' : '发布职位' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createPosition, getPositionDetail, updatePosition } from '@/api/position'

// 路由
const router = useRouter()
const route = useRoute()
const positionId = route.params.id
const isEdit = computed(() => !!positionId)

// 状态
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 表单数据
const positionForm = reactive({
  positionName: '',
  positionType: '',
  positionCategory: '',
  workAddress: '',
  salaryMin: 0,
  salaryMax: 0,
  educationRequirement: '',
  experienceRequirement: '',
  positionCount: 1,
  deadline: '',
  positionDescription: '',
  positionRequirement: ''
})

// 验证规则
const rules = {
  positionName: [
    { required: true, message: '请输入职位名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  positionType: [
    { required: true, message: '请选择职位类型', trigger: 'change' }
  ],
  positionCategory: [
    { required: true, message: '请选择职位类别', trigger: 'change' }
  ],
  workAddress: [
    { required: true, message: '请输入工作地点', trigger: 'blur' }
  ],
  salaryMin: [
    { type: 'number', required: true, message: '请输入最低薪资', trigger: 'blur' }
  ],
  salaryMax: [
    { type: 'number', required: true, message: '请输入最高薪资', trigger: 'blur' }
  ],
  educationRequirement: [
    { required: true, message: '请选择学历要求', trigger: 'change' }
  ],
  experienceRequirement: [
    { required: true, message: '请选择经验要求', trigger: 'change' }
  ],
  positionCount: [
    { type: 'number', required: true, message: '请输入招聘人数', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ],
  positionDescription: [
    { required: true, message: '请输入职位描述', trigger: 'blur' }
  ]
}

// 返回上一页
const goBack = () => {
  router.push('/back/position')
}

// 重置表单
const resetForm = () => {
  formRef.value.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (submitting.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证薪资范围
    if (positionForm.salaryMin > positionForm.salaryMax) {
      ElMessage.warning('最低薪资不能大于最高薪资')
      return
    }
    
    // 验证截止日期
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    if (new Date(positionForm.deadline) < today) {
      ElMessage.warning('截止日期不能早于今天')
      return
    }
    
    submitting.value = true
    
    // 提交数据
    if (isEdit.value) {
      // 更新职位
      await updatePosition(positionId, positionForm, {
        successMsg: '职位更新成功',
        onSuccess: () => {
          router.push('/back/position')
        }
      })
    } else {
      // 发布新职位
      await createPosition(positionForm, {
        successMsg: '职位发布成功，等待审核',
        onSuccess: () => {
          router.push('/back/position')
        }
      })
    }
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    submitting.value = false
  }
}

// 获取职位详情（编辑模式）
const fetchPositionDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    await getPositionDetail(positionId, {
      onSuccess: (data) => {
        Object.keys(positionForm).forEach(key => {
          if (data[key] !== undefined && data[key] !== null) {
            positionForm[key] = data[key]
          }
        })
      },
      onError: () => {
        ElMessage.error('获取职位详情失败')
        router.push('/back/position')
      }
    })
  } finally {
    loading.value = false
  }
}

// 页面加载时获取职位详情（编辑模式）
onMounted(() => {
  fetchPositionDetail()
})
</script>

<style lang="scss" scoped>
.position-form-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      h2 {
        margin: 0 0 0 10px;
        font-size: 20px;
      }
    }
  }
  
  .form-section-title {
    margin-top: 20px;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    border-bottom: 1px solid #EBEEF5;
    padding-bottom: 10px;
    
    &:first-child {
      margin-top: 0;
    }
  }
}
</style> 
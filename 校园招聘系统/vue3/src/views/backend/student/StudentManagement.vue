<template>
  <div class="student-management">
    <el-card class="search-card">
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" ref="searchFormRef" class="demo-form-inline">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable />
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-input v-model="searchForm.college" placeholder="请输入学院" clearable />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="searchForm.major" placeholder="请输入专业" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>学生信息列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>添加学生
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="college" label="学院" min-width="150" show-overflow-tooltip />
        <el-table-column prop="major" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="education" label="学历" min-width="100" />
        <el-table-column prop="graduationYear" label="毕业年份" min-width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button type="success" size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑学生信息弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加学生' : '编辑学生'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="20" />
        </el-form-item>
        
        <el-form-item label="学院" prop="college">
          <el-input v-model="form.college" placeholder="请输入学院" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="学历" prop="education">
          <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;">
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="毕业年份" prop="graduationYear">
          <el-date-picker
            v-model="graduationDate"
            type="year"
            placeholder="选择毕业年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 100%;"
            @change="handleYearChange"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="关联用户" prop="userId" v-if="dialogType === 'add'">
          <el-select v-model="form.userId" placeholder="请选择关联用户" style="width: 100%;" filterable remote
            :remote-method="searchUsers" :loading="userLoading">
            <el-option v-for="item in userOptions" :key="item.id" :label="item.username" :value="item.id">
              <div style="display: flex; justify-content: space-between;">
                <span>{{ item.username }}</span>
                <span style="color: #999">{{ item.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看学生信息弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="学生详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ detailData.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detailData.major }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ detailData.education }}</el-descriptions-item>
        <el-descriptions-item label="毕业年份">{{ detailData.graduationYear }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Plus, Edit, Delete, View } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchFormRef = ref(null);
const searchForm = reactive({
  studentNo: '',
  college: '',
  major: ''
});

// 弹窗表单
const dialogVisible = ref(false);
const dialogType = ref('add'); // add 或 edit
const formRef = ref(null);
const submitLoading = ref(false);
const graduationDate = ref('');

// 查看详情弹窗
const viewDialogVisible = ref(false);
const detailData = ref({});

// 用户选择
const userOptions = ref([]);
const userLoading = ref(false);

const form = reactive({
  id: null,
  studentNo: '',
  college: '',
  major: '',
  education: '',
  graduationYear: null,
  userId: null
});

// 表单验证规则
const rules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号长度在3到20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '学号只能包含字母和数字', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' },
    { min: 2, max: 50, message: '学院名称长度在2到50个字符之间', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' },
    { min: 2, max: 50, message: '专业名称长度在2到50个字符之间', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ],
  graduationYear: [
    { required: true, message: '请选择毕业年份', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请选择关联用户', trigger: 'change' }
  ]
};

// 禁用的日期
const disabledDate = (time) => {
  const currentYear = new Date().getFullYear();
  const year = new Date(time).getFullYear();
  return year < currentYear - 5 || year > currentYear + 10;
};

// 年份选择处理
const handleYearChange = (val) => {
  if (val) {
    form.graduationYear = parseInt(val);
  } else {
    form.graduationYear = null;
  }
};

// 获取学生列表
const fetchStudents = async () => {
  loading.value = true;
  try {
    await request.get('/student/page', {
      studentNo: searchForm.studentNo,
      college: searchForm.college,
      major: searchForm.major,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || [];
        total.value = res.total || 0;
      }
    });
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchStudents();
};

// 重置搜索
const resetSearch = () => {
  searchFormRef.value.resetFields();
  currentPage.value = 1;
  fetchStudents();
};

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchStudents();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchStudents();
};

// 添加学生
const handleAdd = () => {
  dialogType.value = 'add';
  resetForm();
  dialogVisible.value = true;
};

// 编辑学生
const handleEdit = (row) => {
  dialogType.value = 'edit';
  resetForm();
  Object.assign(form, row);
  graduationDate.value = row.graduationYear ? row.graduationYear.toString() : '';
  dialogVisible.value = true;
};

// 查看学生详情
const handleView = (row) => {
  detailData.value = { ...row };
  viewDialogVisible.value = true;
};

// 删除学生
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该学生信息吗？此操作不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    request.delete(`/student/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchStudents();
      }
    });
  }).catch(() => {});
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  form.id = null;
  form.studentNo = '';
  form.college = '';
  form.major = '';
  form.education = '';
  form.graduationYear = null;
  form.userId = null;
  graduationDate.value = '';
};

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return false;
    }
    
    submitLoading.value = true;
    
    try {
      if (dialogType.value === 'add') {
        // 添加
        await request.post('/student', form, {
          successMsg: '添加成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchStudents();
          }
        });
      } else {
        // 编辑
        await request.put(`/student/${form.id}`, form, {
          successMsg: '更新成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchStudents();
          }
        });
      }
    } finally {
      submitLoading.value = false;
    }
  });
};

// 搜索用户
const searchUsers = async (query) => {
  if (query.length < 1) {
    userOptions.value = [];
    return;
  }
  
  userLoading.value = true;
  try {
    await request.get('/user/page', {
      username: query,
      roleCode: 'STUDENT',
      currentPage: 1,
      size: 10
    }, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        userOptions.value = res.records || [];
      }
    });
  } finally {
    userLoading.value = false;
  }
};

onMounted(() => {
  fetchStudents();
});
</script>

<style lang="scss" scoped>
.student-management {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media screen and (max-width: 768px) {
  .search-form .el-form {
    display: flex;
    flex-direction: column;
    
    .el-form-item {
      margin-right: 0;
    }
  }
}
</style> 
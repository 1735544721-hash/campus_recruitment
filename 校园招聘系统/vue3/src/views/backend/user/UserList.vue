<template>
  <div class="user-list">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleCode" placeholder="请选择角色" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="企业" value="COMPANY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
      <el-button type="danger" :disabled="selectedUsers.length === 0" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="roleCode" label="角色">
        <template #default="scope">
          <el-tag v-if="scope.row.roleCode === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.roleCode === 'STUDENT'" type="success">学生</el-tag>
          <el-tag v-else-if="scope.row.roleCode === 'COMPANY'" type="warning">企业</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
         <div class="pagination-container">
       <el-pagination
         :current-page="currentPage"
         :page-size="pageSize"
         :page-sizes="[10, 20, 50, 100]"
         layout="total, sizes, prev, pager, next, jumper"
         :total="total"
         @size-change="handleSizeChange"
         @current-change="handleCurrentChange"
       />
     </div>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="userForm.roleCode" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="企业" value="COMPANY" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedUsers = ref([])

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  roleCode: ''
})

// 重置搜索条件
const resetSearch = () => {
  searchForm.username = ''
  searchForm.name = ''
  searchForm.roleCode = ''
  fetchUsers()
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    await request.get('/user/page', {
      username: searchForm.username,
      name: searchForm.name,
      roleCode: searchForm.roleCode,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add 或 edit
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  name: '',
  email: '',
  phone: '',
  roleCode: '',
  password: '',
  status: 1
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(userForm).forEach(key => {
    if (key in row) {
      userForm[key] = row[key]
    }
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  userForm.id = null
  userForm.username = ''
  userForm.name = ''
  userForm.email = ''
  userForm.phone = ''
  userForm.roleCode = ''
  userForm.password = ''
  userForm.status = 1
  
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增用户
        await request.post('/user/add', userForm, {
          successMsg: '添加用户成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchUsers()
          }
        })
      } else {
        // 编辑用户
        await request.put(`/user/${userForm.id}`, userForm, {
          successMsg: '更新用户成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchUsers()
          }
        })
      }
    }
  })
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/user/delete/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchUsers()
      }
    })
  }).catch(() => {
    // 取消删除操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  const ids = selectedUsers.value.map(user => user.id)
  
  ElMessageBox.confirm(`确认删除选中的 ${ids.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete('/user/deleteBatch?ids=' + ids, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchUsers()
      }
    })
  }).catch(() => {
    // 取消删除操作
  })
}

// 修改用户状态
const handleStatusChange = async (row) => {
  try {
    await request.put(`/user/status/${row.id}`, null, {
      params: { status: row.status },
      successMsg: row.status === 1 ? '已启用该用户' : '已禁用该用户'
    })
  } catch (error) {
    // 如果修改失败，回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 初始化
onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-list {
  .search-form {
    margin-bottom: 20px;
  }
  
  .operation-bar {
    margin-bottom: 20px;
    display: flex;
    justify-content: flex-start;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
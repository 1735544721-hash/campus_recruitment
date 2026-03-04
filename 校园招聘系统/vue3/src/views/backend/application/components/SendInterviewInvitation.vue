<template>
  <div>
    <el-button type="primary" @click="dialogVisible = true" :disabled="disabled">发送面试邀请</el-button>
    
    <el-dialog v-model="dialogVisible" title="发送面试邀请" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="form.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
          />
        </el-form-item>
        <el-form-item label="面试地点" prop="interviewAddress">
          <el-input v-model="form.interviewAddress" placeholder="请输入面试地点" />
        </el-form-item>
        <el-form-item label="面试类型" prop="interviewType">
          <el-select v-model="form.interviewType" placeholder="请选择面试类型">
            <el-option label="现场面试" value="现场" />
            <el-option label="视频面试" value="视频" />
            <el-option label="电话面试" value="电话" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试备注" prop="interviewNote">
          <el-input
            v-model="form.interviewNote"
            type="textarea"
            rows="4"
            placeholder="请输入面试备注信息，如需要携带的资料、面试流程等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const props = defineProps({
  applicationId: {
    type: Number,
    required: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['success']);

// 表单数据
const dialogVisible = ref(false);
const formRef = ref(null);
const loading = ref(false);
const form = reactive({
  applicationId: props.applicationId,
  interviewTime: '',
  interviewAddress: '',
  interviewType: '现场',
  interviewNote: ''
});

// 表单验证规则
const rules = {
  interviewTime: [
    { required: true, message: '请选择面试时间', trigger: 'change' }
  ],
  interviewAddress: [
    { required: true, message: '请输入面试地点', trigger: 'blur' }
  ],
  interviewType: [
    { required: true, message: '请选择面试类型', trigger: 'change' }
  ]
};

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7; // 禁用今天之前的日期
};

// 禁用工作时间之外的小时
const disabledHours = () => {
  const hours = [];
  for (let i = 0; i < 24; i++) {
    if (i < 9 || i > 18) { // 假设工作时间为9:00-18:00
      hours.push(i);
    }
  }
  return hours;
};

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await request.post('/interview', {
          applicationId: props.applicationId,
          interviewTime: form.interviewTime,
          interviewAddress: form.interviewAddress,
          interviewType: form.interviewType,
          interviewNote: form.interviewNote
        }, {
          successMsg: '面试邀请已发送',
          onSuccess: () => {
            dialogVisible.value = false;
            emit('success');
          }
        });
      } catch (error) {
        console.error('发送面试邀请失败', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 
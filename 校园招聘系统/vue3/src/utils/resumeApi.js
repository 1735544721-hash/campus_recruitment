import request from './request'

// 获取简历模板列表
export function getResumeTemplateList(callback = {}) {
  return request.get('/resume/template/list', {}, callback)
}

// 获取简历模板详情
export function getResumeTemplateDetail(id, callback = {}) {
  return request.get(`/resume/template/${id}`, {}, callback)
}

// 获取默认简历模板
export function getDefaultResumeTemplate(callback = {}) {
  return request.get('/resume/template/default', {}, callback)
}

// 获取简历列表
export function getResumeList(callback = {}) {
  return request.get('/resume/list', {}, callback)
}

// 获取简历详情
export function getResumeDetail(id, callback = {}) {
  return request.get(`/resume/${id}`, {}, callback)
}

// 创建简历
export function createResume(data, callback = {}) {
  return request.post('/resume', data, callback)
}

// 更新简历
export function updateResume(id, data, callback = {}) {
  return request.put(`/resume/${id}`, data, callback)
}

// 删除简历
export function deleteResume(id, callback = {}) {
  return request.delete(`/resume/${id}`, callback)
}

// 设置默认简历
export function setDefaultResume(id, callback = {}) {
  return request.put(`/resume/default/${id}`, {}, callback)
}

// 基于模板创建简历
export function createResumeFromTemplate(data, callback = {}) {
  return request.post('/resume/create-from-template', data, callback)
}

// 简历预览
export function previewResume(id, callback = {}) {
  return request.get(`/resume/preview/${id}`, {}, callback)
}

// 简历导出
export function exportResume(id, format = 'pdf', callback = {}) {
  return request.get(`/resume/export/${id}`, { format }, callback)
}



// AI润色简历
export function polishResume(data, callback = {}) {
  return request.post('/resume/ai-polish', data, callback)
}

// 应用AI润色结果
export function applyPolishResult(data, callback = {}) {
  return request.post('/resume/apply-polish', data, callback)
}

// 上传简历附件
export function uploadResumeFile(formData, callback = {}) {
  return request.post('/resume/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...callback
  })
} 
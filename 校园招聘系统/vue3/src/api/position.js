import request from '@/utils/request'

/**
 * 获取职位分页列表
 * @param {Object} params 查询参数
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function getPositionPage(params, callbacks) {
  return request.get('/position/page', params, callbacks)
}

/**
 * 获取职位详情
 * @param {Number} id 职位ID
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function getPositionDetail(id, callbacks) {
  return request.get(`/position/${id}`, {}, callbacks)
}

/**
 * 获取企业职位列表
 * @param {Number} companyId 企业ID
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function getCompanyPositions(companyId, callbacks) {
  return request.get(`/position/company/${companyId}`, {}, callbacks)
}

/**
 * 发布职位
 * @param {Object} data 职位信息
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function createPosition(data, callbacks) {
  return request.post('/position', data, callbacks)
}

/**
 * 更新职位
 * @param {Number} id 职位ID
 * @param {Object} data 职位信息
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function updatePosition(id, data, callbacks) {
  return request.put(`/position/${id}`, data, callbacks)
}

/**
 * 删除职位
 * @param {Number} id 职位ID
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function deletePosition(id, callbacks) {
  return request.delete(`/position/${id}`, callbacks)
}

/**
 * 更新职位状态
 * @param {Number} id 职位ID
 * @param {Object|Number} statusObj 状态对象或状态值（0-下线，1-上线，2-审核中）
 * @param {Object} callbacks 回调函数
 * @returns {Promise} Promise对象
 */
export function updatePositionStatus(id, statusObj, callbacks) {
  // 处理状态参数，支持对象形式 { status: 1 } 或直接数值 1
  const status = typeof statusObj === 'object' ? statusObj.status : statusObj;
  
  // 直接在URL中添加查询参数，确保参数被正确传递
  return request.put(`/position/${id}/status?status=${status}`, null, callbacks)
} 
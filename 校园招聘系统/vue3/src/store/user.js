import { defineStore } from 'pinia'
import request from '@/utils/request'
import { login, register } from '@/api/user'
// import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
 
    companyInfo: JSON.parse(localStorage.getItem('companyInfo')) || null,
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    
    // 角色判断
    isAdmin: (state) => state.userInfo?.roleCode === 'ADMIN',
    isStudent: (state) => state.userInfo?.roleCode === 'STUDENT',
    isCompany: (state) => state.userInfo?.roleCode === 'COMPANY',
    
    // 权限相关的getter
        // 判断企业用户是否有企业关联信息
    hasCompanyInfo: (state) => state.isCompany && state.companyInfo,
    
    // // 后台访问权限
    // canAccessBackend: (state) => {
    //   return state.userInfo?.roleCode === 'ADMIN' || state.userInfo?.roleCode === 'COMPANY';
    // },
    
 
    
    // // 职位管理权限
    // canManagePositions: (state) => {
    //   return state.userInfo?.roleCode === 'ADMIN' || 
    //          (state.userInfo?.roleCode === 'COMPANY' && state.companyInfo);
    // },
    
    // // 招聘管理权限（投递、面试）
    // canManageRecruitment: (state) => {
    //   return state.userInfo?.roleCode === 'ADMIN' || 
    //           (state.userInfo?.roleCode === 'COMPANY' && state.companyInfo);
    // },
    
    // 用户角色代码
    roleCode: (state) => state.userInfo?.roleCode || '',
    
    // 角色名称
    roleName: (state) => {
      const roleCode = state.userInfo?.roleCode;
      if (roleCode === 'ADMIN') return '管理员';
      if (roleCode === 'COMPANY') return '企业用户';
      if (roleCode === 'STUDENT') return '学生用户';
      return '未知角色';
    },
    
    // 公司ID
    companyId: (state) => state.userInfo?.companyId || null,
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data.userInfo || data
      this.token = data.token
      this.role = data.roleCode
      
      // 存储到 LocalStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('role', this.role || '')
      localStorage.setItem('companyInfo', JSON.stringify(this.companyInfo))
    },
    
    // 检查企业用户是否有企业信息
    async loadCompanyInfo() {
      // 只有企业用户才需要检查
      if (!this.isCompany || !this.userInfo?.id) return false
      
      try {
        const response = await request.get(`/company/user/${this.userInfo.id}`, null, {
          showDefaultMsg: false,
          onSuccess: (response) => {
   
            this.companyInfo = response
        
            localStorage.setItem('companyInfo', JSON.stringify(this.companyInfo))
            return true
          },
          onError: () => {
            // 没有企业信息

            this.companyInfo = null
         
            return false
          }
        })
        return !!response
      } catch (error) {
        console.error('检查企业信息失败:', error)

        this.companyInfo = null
    
        return false
      }
    },
    
    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.role = ''
  
    
      
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
 

    },
    

    
    // 获取用户信息和菜单 - 从localStorage恢复
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const menus = JSON.parse(localStorage.getItem('menus'))
      
      if (userInfo && menus) {
        this.userInfo = userInfo
        this.menus = menus
        return { userInfo, menus }
      }
      
      // 如果没有缓存的数据，清除状态并抛出错误
      this.clearUserInfo()
      throw new Error('No cached user info')
    },
    
    // 登录
    async login(loginForm) {
      try {
        const res = await request.post('/user/login', loginForm)
        this.setUserInfo(res)
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    // 注册

    // 退出登录
    async logout() {
      this.clearUserInfo()
    },
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    },
    
    // 检查是否有特定权限
    hasPermission(permission) {
      // 这里可以扩展为更复杂的权限检查逻辑
      if (this.isAdmin) return true; // 管理员拥有所有权限
      
      const permissionMap = {
        'company:view': this.isCompany || this.isAdmin,
        'company:edit': this.isCompany && !!this.userInfo?.companyId,
        'company:manage': this.isAdmin,
        'position:view': true, // 所有登录用户都可以查看职位
        'position:create': this.isCompany && !!this.userInfo?.companyId,
        'position:edit': this.isCompany && !!this.userInfo?.companyId,
        'position:approve': this.isAdmin,
        'application:view': this.isStudent || this.isCompany || this.isAdmin,
        'application:manage': this.isCompany || this.isAdmin,
        'interview:view': this.isStudent || this.isCompany || this.isAdmin,
        'interview:manage': this.isCompany || this.isAdmin,
        'user:manage': this.isAdmin,
        'student:manage': this.isAdmin,
      };
      
      return permissionMap[permission] || false;
    }
  }
})
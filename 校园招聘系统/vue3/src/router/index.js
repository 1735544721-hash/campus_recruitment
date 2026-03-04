import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '控制台', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'company',
        name: 'CompanyManagement',
        component: () => import('@/views/backend/company/CompanyList.vue'),
        meta: { title: '企业管理', icon: 'OfficeBuilding' }
      },
      {
        path:'resume',
        name:'ResumeManagement',
        component:()=>import('@/views/backend/resume/ResumeManagement.vue'),
        meta:{title:'简历管理',icon:'Document'}
      },
      {
        path: 'company/detail/:id',
        name: 'CompanyDetail',
        component: () => import('@/views/backend/company/CompanyDetail.vue'),
        meta: { title: '企业详情', hidden: true }
      },
      {
        path: 'student',
        name: 'StudentManagement',
        component: () => import('@/views/backend/student/StudentManagement.vue'),
        meta: { title: '学生管理', icon: 'Notebook' }
      },
      {
        path: 'position',
        name: 'PositionManagement',
        component: () => import('@/views/backend/position/PositionList.vue'),
        meta: { title: '职位管理', icon: 'Suitcase' }
      },
      {
        path: 'position/detail/:id',
        name: 'PositionDetail',
        component: () => import('@/views/backend/position/PositionDetail.vue'),
        meta: { title: '职位详情', hidden: true }
      },
      {
        path: 'position/form/:id?',
        name: 'PositionForm',
        component: () => import('@/views/backend/position/PositionForm.vue'),
        meta: { title: '职位发布/编辑', hidden: true }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      },
      {
        path: 'application',
        name: 'ApplicationManagement',
        component: () => import('@/views/backend/application/ApplicationManagement.vue'),
        meta: { title: '投递管理', icon: 'Document' }
      },
      {
        path: 'interview',
        name: 'InterviewManagement',
        component: () => import('@/views/backend/interview/InterviewManagement.vue'),
        meta: { title: '面试管理', icon: 'Calendar' }
      },
      // 消息管理路由
      {
        path: 'message',
        name: 'MessageManagement',
        component: () => import('@/views/backend/message/MessageManagement.vue'),
        meta: { title: '消息管理', icon: 'ChatDotRound' }
      },
      {
        path: 'message/:id',
        name: 'BackendMessageDetail',
        component: () => import('@/views/frontend/message/MessageDetail.vue'),
        meta: { title: '消息详情', hidden: true }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'company/profile',
        name: 'CompanyProfileComplete',
        component: () => import('@/views/frontend/company/ProfileComplete.vue'),
        meta: { title: '完善企业信息', requiresAuth: true }
      },
      {
        path: 'student/profile',
        name: 'StudentProfileComplete',
        component: () => import('@/views/frontend/student/ProfileComplete.vue'),
        meta: { title: '完善学生信息', requiresAuth: true }
      },
      {
        path: 'student/info',
        name: 'StudentInfo',
        component: () => import('@/views/frontend/student/StudentInfo.vue'),
        meta: { title: '学生个人信息', requiresAuth: true }
      },
      // 简历管理路由
      {
        path: 'resume',
        name: 'ResumeList',
        component: () => import('@/views/frontend/resume/ResumeList.vue'),
        meta: { title: '我的简历', requiresAuth: true }
      },
      {
        path: 'resume/edit/:id',
        name: 'ResumeEdit',
        component: () => import('@/views/frontend/resume/ResumeEdit.vue'),
        meta: { title: '编辑简历', requiresAuth: true }
      },
      {
        path: 'resume/preview/:id',
        name: 'ResumePreview',
        component: () => import('@/views/frontend/resume/ResumePreview.vue'),
        meta: { title: '预览简历', requiresAuth: true }
      },
      {
        path: 'resume/polish/:id',
        name: 'ResumePolish',
        component: () => import('@/views/frontend/resume/ResumePolish.vue'),
        meta: { title: 'AI润色简历', requiresAuth: true }
      },
      // 投递记录路由
      {
        path: 'application',
        name: 'ApplicationList',
        component: () => import('@/views/frontend/application/ApplicationList.vue'),
        meta: { title: '我的投递记录', requiresAuth: true }
      },
      // 公司列表路由
      {
        path: 'companies',
        name: 'CompanyList',
        component: () => import('@/views/frontend/company/CompanyList.vue'),
        meta: { title: '企业列表' }
      },
      // 公司详情路由
      {
        path: 'company-detail/:id',
        name: 'CompanyDetailFrontend',
        component: () => import('@/views/frontend/company/CompanyDetail.vue'),
        meta: { title: '企业详情' }
      },
      // 职位列表路由
      {
        path: 'positions',
        name: 'PositionList',
        component: () => import('@/views/frontend/position/PositionList.vue'),
        meta: { title: '职位列表' }
      },
      // 职位详情路由
      {
        path: 'position/detail/:id',
        name: 'PositionDetailFrontend',
        component: () => import('@/views/frontend/position/PositionDetail.vue'),
        meta: { title: '职位详情' }
      },
      // 面试管理路由
      {
        path: 'interview',
        name: 'StudentInterview',
        component: () => import('@/views/frontend/interview/StudentInterview.vue'),
        meta: { title: '我的面试', requiresAuth: true }
      },
      // 消息中心路由
      {
        path: 'message/center',
        name: 'MessageCenter',
        component: () => import('@/views/frontend/message/MessageCenter.vue'),
        meta: { title: '消息中心', requiresAuth: true }
      },
      {
        path: 'message/:id',
        name: 'MessageDetail',
        component: () => import('@/views/frontend/message/MessageDetail.vue'),
        meta: { title: '消息详情', requiresAuth: true }
      }
    ] 
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  // 简历分享路由 - 不需要认证
  // 暂时注释掉，等待ResumeShare.vue文件创建
  /*
  {
    path: '/r/:shareCode',
    name: 'ResumeShare',
    component: () => import('@/views/frontend/resume/ResumeShare.vue'),
    meta: { title: '简历分享' }
  }
  */
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 校园招聘系统`
  }

  const userStore = useUserStore()
  console.log("Current route:", to.path)
  console.log("User status:", {
    isLoggedIn: userStore.isLoggedIn,
    role: userStore.roleCode
  })

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login' || to.path === '/register') {
      if (userStore.isAdmin) {
        next('/back/dashboard')
      } else {
        next('/')
      }
      return
    }

    // 根据角色控制路由访问
    if (userStore.isAdmin) {
      // 管理员可以访问所有路由
      next()
      return
    } else if (userStore.isCompany) {
      // 企业用户可以访问前台路由和部分后台路由
      if (to.path.startsWith('/back')) {
        // 检查企业用户是否有权限访问该后台页面
        if (to.path.includes('/back/user') || to.path.includes('/back/student') || to.path.includes('/back/company')) {
          // 企业用户不能访问用户、学生和企业管理页面
          next('/back/dashboard')
          return
        }
        // 企业用户可以访问其他后台页面
        next()
        return
      } 
      // 企业用户可以访问所有前台路由
      next()
      return
    } else if (userStore.isStudent) {
      // 学生用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
        return
      }
      next()
      return
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
  }

  next()
})

export default router

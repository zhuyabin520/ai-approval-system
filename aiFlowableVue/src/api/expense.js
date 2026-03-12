import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加 token 等认证信息
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    // 统一处理错误
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('未授权，请重新登录')
          break
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('请求资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error('未知错误')
      }
    }
    return Promise.reject(error)
  }
)

// 报销单相关 API
export const expenseApi = {
  // 获取报销单列表
  getExpenseClaims: (params = {}) => {
    return api.get('/expense/list', { params })
  },
  
  // 提交报销单
  submitExpenseClaim: (data) => {
    return api.post('/expense/submit', data)
  },
  
  // 获取报销单详情
  getExpenseClaimDetail: (id) => {
    return api.get(`/expense/detail/${id}`)
  },
  
  // 删除报销单
  deleteExpenseClaim: (id) => {
    return api.delete(`/expense/${id}`)
  },
  
  // 审批报销单
  approveExpenseClaim: (taskId, data) => {
    return api.post(`/expense/approve/${taskId}`, data)
  },
  
  // 获取用户待审批任务
  getTasksByAssignee: (assignee) => {
    return api.get(`/expense/tasks/${assignee}`)
  },
  
  // 获取报销单的审批记录
  getApprovalRecords: (claimId) => {
    return api.get(`/expense/approval-records/${claimId}`)
  },
  
  // 获取用户信息
  getUserInfo: (userId) => {
    return api.get(`/user/${userId}`)
  },
  
  // 系统设置相关 API
  getSystemSettings: () => {
    return api.get('/system/settings')
  },
  
  updateSystemSettings: (settings) => {
    return api.put('/system/settings', settings)
  },
  
  // AI 设置相关 API
  getAISettings: () => {
    return api.get('/system/ai-settings')
  },
  
  updateAISettings: (settings) => {
    return api.put('/system/ai-settings', settings)
  },
  
  // 流程配置相关 API
  getProcessDefinitions: () => {
    return api.get('/system/process-definitions')
  },
  
  deployProcess: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/system/process-deploy', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 用户相关 API
  getUsers: () => {
    return api.get('/system/users')
  },
  
  getUser: (id) => {
    return api.get(`/system/users/${id}`)
  },
  
  saveUser: (user) => {
    return api.post('/system/users', user)
  },
  
  deleteUser: (id) => {
    return api.delete(`/system/users/${id}`)
  },
  
  // 角色权限相关 API
  getRolePermissions: () => {
    return api.get('/permission/role-permissions')
  },
  
  getPermissionsByRole: (roleId) => {
    return api.get(`/permission/role-permissions/${roleId}`)
  },
  
  saveRolePermission: (permission) => {
    return api.post('/permission/role-permissions', permission)
  },
  
  deleteRolePermission: (id) => {
    return api.delete(`/permission/role-permissions/${id}`)
  },
  
  // 流程设置相关 API
  getProcessSettings: () => {
    return api.get('/permission/process-settings')
  },
  
  getStepsByProcess: (processId) => {
    return api.get(`/permission/process-settings/${processId}`)
  },
  
  saveProcessSetting: (setting) => {
    return api.post('/permission/process-settings', setting)
  },
  
  deleteProcessSetting: (id) => {
    return api.delete(`/permission/process-settings/${id}`)
  }
}

export default api
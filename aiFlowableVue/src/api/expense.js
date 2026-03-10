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
  getExpenseClaims: (status = '', userId = null) => {
    const params = {}
    if (status) params.status = status
    if (userId) params.userId = userId
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
  }
}

export default api
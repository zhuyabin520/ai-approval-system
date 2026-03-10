<template>
  <div class="app-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header height="60px" class="header">
        <div class="logo">智能审批系统</div>
        <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
          <el-menu-item index="1">首页</el-menu-item>
          <el-menu-item index="2">报销单管理</el-menu-item>
          <el-menu-item index="3">审批流程</el-menu-item>
          <el-menu-item index="4">系统设置</el-menu-item>
        </el-menu>
      </el-header>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 左侧菜单 -->
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
          >
            <el-sub-menu index="1">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>报销管理</span>
              </template>
              <el-menu-item index="1-1">提交报销</el-menu-item>
              <el-menu-item index="1-2">我的报销</el-menu-item>
              <el-menu-item index="1-3">审批任务</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="2">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="2-1">用户列表</el-menu-item>
              <el-menu-item index="2-2">角色权限</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="3">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </template>
              <el-menu-item index="3-1">流程配置</el-menu-item>
              <el-menu-item index="3-2">AI设置</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        
        <!-- 右侧内容 -->
        <el-main class="main">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>{{ getPageTitle() }}</span>
                <el-button v-if="activeMenu === '1-1'" type="primary" @click="submitExpense">
                  <el-icon><Plus /></el-icon> 提交报销
                </el-button>
                <el-button v-if="(activeMenu === '3-1' || activeMenu === '3-2') && !loading" type="primary" @click="saveSettings">
                  <el-icon><Plus /></el-icon> 保存设置
                </el-button>
              </div>
            </template>
            
            <!-- 搜索表单 -->
            <el-form v-if="activeMenu !== '1-1' && !isSystemSettingPage" :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="报销单号">
                <el-input v-model="searchForm.claimId" placeholder="请输入报销单号" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="请选择状态">
                  <el-option label="全部" value="" />
                  <el-option label="待提交" value="DRAFT" />
                  <el-option label="待审批" value="SUBMITTED" />
                  <el-option label="已通过" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-form-item>
            </el-form>
            
            <!-- 提交报销表单 -->
            <el-form v-if="activeMenu === '1-1'" :model="currentExpense" label-width="80px">
              <el-form-item label="报销类型">
                <el-select v-model="currentExpense.type" placeholder="请选择报销类型">
                  <el-option label="差旅费" value="差旅费" />
                  <el-option label="办公费" value="办公费" />
                  <el-option label="业务招待费" value="业务招待费" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="金额">
                <el-input v-model.number="currentExpense.amount" placeholder="请输入金额" />
              </el-form-item>
              <el-form-item label="报销日期">
                <el-date-picker v-model="currentExpense.claimDate" type="date" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="currentExpense.description" type="textarea" placeholder="请输入报销描述" />
              </el-form-item>
            </el-form>
            
            <!-- 报销单/任务列表 -->
            <el-table v-if="activeMenu !== '1-1' && !isSystemSettingPage" :data="expenseClaims" style="width: 100%" v-loading="loading">
              <el-table-column prop="id" label="报销单号" width="120" />
              <el-table-column prop="amount" label="金额" width="100" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="submitTime" label="提交时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                  <el-button v-if="activeMenu === '1-2'" size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                  <el-button v-if="activeMenu === '1-2'" size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
                  <el-button v-if="activeMenu === '1-3'" size="small" type="success" @click="handleApprove(scope.row, true)">通过</el-button>
                  <el-button v-if="activeMenu === '1-3'" size="small" type="danger" @click="handleApprove(scope.row, false)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 系统设置页面 -->
            <div v-if="activeMenu === '3-1'" v-loading="loading">
              <el-form :model="systemSettings" label-width="120px">
                <el-form-item label="系统名称">
                  <el-input v-model="systemSettings.systemName" placeholder="请输入系统名称" />
                </el-form-item>
                <el-form-item label="审批流程配置">
                  <el-select v-model="systemSettings.processDefinition" placeholder="请选择审批流程">
                    <el-option v-for="process in processDefinitions" :key="process.id" :label="process.name" :value="process.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批人配置">
                  <el-input v-model="systemSettings.approverConfig" type="textarea" placeholder="请配置审批人规则" />
                </el-form-item>
                <el-form-item label="部署新流程">
                  <el-upload
                    class="upload-demo"
                    action="#"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    :show-file-list="false"
                  >
                    <el-button type="primary">选择 BPMN 文件</el-button>
                  </el-upload>
                  <el-button v-if="processFile" type="success" @click="deployProcess">部署流程</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- AI 设置页面 -->
            <div v-if="activeMenu === '3-2'" v-loading="loading">
              <el-form :model="aiSettings" label-width="120px">
                <el-form-item label="AI 模型">
                  <el-select v-model="aiSettings.modelName" placeholder="请选择 AI 模型">
                    <el-option label="Qwen 3.5" value="qwen3.5-flash" />
                    <el-option label="GPT-4" value="gpt-4" />
                    <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
                  </el-select>
                </el-form-item>
                <el-form-item label="API Key">
                  <el-input v-model="aiSettings.apiKey" type="password" placeholder="请输入 API Key" />
                </el-form-item>
                <el-form-item label="API 地址">
                  <el-input v-model="aiSettings.baseUrl" placeholder="请输入 API 地址" />
                </el-form-item>
                <el-form-item label="温度">
                  <el-slider v-model="aiSettings.temperature" :min="0" :max="1" :step="0.1" />
                </el-form-item>
                <el-form-item label="最大 Token">
                  <el-input-number v-model="aiSettings.maxTokens" :min="100" :max="4096" :step="100" />
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 分页 -->
            <div v-if="activeMenu !== '1-1' && !isSystemSettingPage" class="pagination">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { Document, User, Setting, Plus } from '@element-plus/icons-vue'
import { expenseApi } from './api/expense'

export default {
  name: 'App',
  components: {
    Document,
    User,
    Setting,
    Plus
  },
  data() {
    return {
      activeIndex: '2',
      activeMenu: '1-2',
      searchForm: {
        claimId: '',
        status: ''
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      expenseClaims: [],
      currentExpense: {
        userId: 1,
        amount: '',
        claimDate: new Date(),
        type: '',
        description: '',
        items: []
      },
      // 系统设置
      systemSettings: {
        systemName: '智能审批系统',
        processDefinition: '',
        approverConfig: ''
      },
      // AI 设置
      aiSettings: {
        modelName: 'qwen3.5-flash',
        apiKey: '',
        baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
        temperature: 0.7,
        maxTokens: 2048
      },
      // 流程定义列表
      processDefinitions: [],
      // 上传的流程文件
      processFile: null
    }
  },
  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath)
    },
    handleMenuSelect(key, keyPath) {
      console.log(key, keyPath)
      this.activeMenu = key
      // 根据菜单切换加载对应数据
      if (key === '1-1') {
        // 提交报销页面
      } else if (key === '1-2') {
        // 我的报销页面
        this.loadExpenseClaims()
      } else if (key === '1-3') {
        // 审批任务页面
        this.loadApprovalTasks()
      }
    },
    // 加载报销单列表
    async loadExpenseClaims() {
      this.loading = true
      try {
        const response = await expenseApi.getExpenseClaims(this.searchForm.status, 1)
        this.expenseClaims = response || []
        this.total = this.expenseClaims.length
      } catch (error) {
        console.error('加载报销单失败:', error)
        this.$message.error('加载报销单失败：' + (error.message || '未知错误'))
        this.expenseClaims = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    // 加载审批任务
    async loadApprovalTasks() {
      this.loading = true
      try {
        const response = await expenseApi.getTasksByAssignee('admin')
        // 处理任务数据，获取关联的报销单信息
        const tasksWithClaims = []
        for (const task of response) {
          const variables = task.processVariables || {}
          const claimId = variables.claimId
          if (claimId) {
            try {
              const claimDetail = await expenseApi.getExpenseClaimDetail(claimId)
              tasksWithClaims.push({
                ...claimDetail.claim,
                taskId: task.id,
                taskName: task.name,
                assignee: task.assignee
              })
            } catch (error) {
              console.error('获取报销单详情失败:', error)
            }
          }
        }
        this.expenseClaims = tasksWithClaims
        this.total = this.expenseClaims.length
      } catch (error) {
        console.error('加载审批任务失败:', error)
        this.$message.error('加载审批任务失败：' + (error.message || '未知错误'))
        this.expenseClaims = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    // 提交报销单
    async submitExpense() {
      // 验证必填字段
      if (!this.currentExpense.type) {
        this.$message.warning('请选择报销类型')
        return
      }
      if (!this.currentExpense.amount || this.currentExpense.amount <= 0) {
        this.$message.warning('请输入有效的金额')
        return
      }
      if (!this.currentExpense.description) {
        this.$message.warning('请输入报销描述')
        return
      }
      
      this.loading = true
      try {
        // 格式化日期
        const formattedData = {
          ...this.currentExpense,
          claimDate: this.formatDate(this.currentExpense.claimDate),
          items: this.currentExpense.items || []
        }
        const response = await expenseApi.submitExpenseClaim(formattedData)
        this.$message.success('报销单提交成功，ID: ' + response)
        // 重置表单
        this.currentExpense = {
          userId: 1,
          amount: '',
          claimDate: new Date(),
          type: '',
          description: '',
          items: []
        }
        // 刷新列表
        this.loadExpenseClaims()
      } catch (error) {
        console.error('提交报销单失败:', error)
        this.$message.error('提交报销单失败：' + (error.response?.data || error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    // 审批报销单
    async handleApprove(row, approved) {
      if (!row.taskId) {
        this.$message.warning('任务 ID 不存在')
        return
      }
      this.loading = true
      try {
        await expenseApi.approveExpenseClaim(row.taskId, {
          approved: approved,
          opinion: approved ? '同意报销' : '不同意报销',
          approverId: 1,
          approverName: '管理员'
        })
        this.$message.success(approved ? '审批通过' : '审批拒绝')
        this.loadApprovalTasks()
      } catch (error) {
        console.error('审批失败:', error)
        this.$message.error('审批失败：' + (error.response?.data || error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      console.log('添加新报销')
    },
    handleSearch() {
      console.log('搜索', this.searchForm)
      if (this.activeMenu === '1-2') {
        this.loadExpenseClaims()
      } else if (this.activeMenu === '1-3') {
        this.loadApprovalTasks()
      }
    },
    resetSearch() {
      this.searchForm = {
        claimId: '',
        status: ''
      }
      if (this.activeMenu === '1-2') {
        this.loadExpenseClaims()
      } else if (this.activeMenu === '1-3') {
        this.loadApprovalTasks()
      }
    },
    handleView(row) {
      console.log('查看', row)
      this.$alert('报销单详情：' + JSON.stringify(row, null, 2), '详情', {
        confirmButtonText: '确定'
      })
    },
    handleEdit(row) {
      console.log('编辑', row)
      this.currentExpense = { ...row }
      this.activeMenu = '1-1'
    },
    handleDelete(row) {
      console.log('删除', row)
      this.$confirm('确认删除该报销单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // TODO: 调用删除 API
          this.$message.success('删除成功')
          this.loadExpenseClaims()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.loadExpenseClaims()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadExpenseClaims()
    },
    // 格式化日期为 YYYY-MM-DD
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    getStatusType(status) {
      switch (status) {
        case 'SUBMITTED':
          return 'warning'
        case 'APPROVED':
          return 'success'
        case 'REJECTED':
          return 'danger'
        default:
          return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'DRAFT':
          return '待提交'
        case 'SUBMITTED':
          return '待审批'
        case 'APPROVED':
          return '已通过'
        case 'REJECTED':
          return '已拒绝'
        default:
          return status
      }
    },
    // 获取页面标题
    getPageTitle() {
      switch (this.activeMenu) {
        case '1-1': return '提交报销'
        case '1-2': return '我的报销'
        case '1-3': return '审批任务'
        case '2-1': return '用户列表'
        case '2-2': return '角色权限'
        case '3-1': return '流程配置'
        case '3-2': return 'AI设置'
        default: return '智能审批系统'
      }
    },
    // 加载系统设置
    async loadSystemSettings() {
      this.loading = true
      try {
        const settings = await expenseApi.getSystemSettings()
        if (settings) {
          this.systemSettings = { ...this.systemSettings, ...settings }
        }
      } catch (error) {
        console.error('加载系统设置失败:', error)
        this.$message.error('加载系统设置失败')
      } finally {
        this.loading = false
      }
    },
    // 加载 AI 设置
    async loadAISettings() {
      this.loading = true
      try {
        const settings = await expenseApi.getAISettings()
        if (settings) {
          this.aiSettings = { ...this.aiSettings, ...settings }
        }
      } catch (error) {
        console.error('加载 AI 设置失败:', error)
        this.$message.error('加载 AI 设置失败')
      } finally {
        this.loading = false
      }
    },
    // 加载流程定义
    async loadProcessDefinitions() {
      this.loading = true
      try {
        const processes = await expenseApi.getProcessDefinitions()
        this.processDefinitions = processes || []
      } catch (error) {
        console.error('加载流程定义失败:', error)
        this.$message.error('加载流程定义失败')
        this.processDefinitions = []
      } finally {
        this.loading = false
      }
    },
    // 保存设置
    async saveSettings() {
      this.loading = true
      try {
        if (this.activeMenu === '3-1') {
          await expenseApi.updateSystemSettings(this.systemSettings)
          this.$message.success('系统设置保存成功')
        } else if (this.activeMenu === '3-2') {
          await expenseApi.updateAISettings(this.aiSettings)
          this.$message.success('AI 设置保存成功')
        }
      } catch (error) {
        console.error('保存设置失败:', error)
        this.$message.error('保存设置失败')
      } finally {
        this.loading = false
      }
    },
    // 处理文件上传
    handleFileChange(file) {
      if (file.raw.type === 'application/xml' || file.name.endsWith('.bpmn20.xml') || file.name.endsWith('.bpmn')) {
        this.processFile = file.raw
        this.$message.success('文件选择成功')
      } else {
        this.$message.error('请选择 BPMN 文件')
        this.processFile = null
      }
    },
    // 部署流程
    async deployProcess() {
      if (!this.processFile) {
        this.$message.warning('请选择流程文件')
        return
      }
      this.loading = true
      try {
        await expenseApi.deployProcess(this.processFile)
        this.$message.success('流程部署成功')
        this.processFile = null
        this.loadProcessDefinitions()
      } catch (error) {
        console.error('流程部署失败:', error)
        this.$message.error('流程部署失败')
      } finally {
        this.loading = false
      }
    }
  },
  computed: {
    // 判断是否为系统设置页面
    isSystemSettingPage() {
      return this.activeMenu === '3-1' || this.activeMenu === '3-2'
    }
  },
  mounted() {
    // 页面加载时默认加载我的报销
    this.loadExpenseClaims()
  }
}
</script>

<style>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.header .el-menu {
  background-color: transparent;
  border-bottom: none;
}

.header .el-menu .el-menu-item {
  color: white;
}

.header .el-menu .el-menu-item.is-active {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
}

.aside {
  background-color: #f0f2f5;
  border-right: 1px solid #e4e7ed;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
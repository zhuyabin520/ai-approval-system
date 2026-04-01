<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><Document /></el-icon>
          <span class="logo-text">智能审批系统</span>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown>
          <span class="user-info">
            <el-avatar size="small" :src="userAvatar"></el-avatar>
            <span class="user-name">{{ userName }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item>修改密码</el-dropdown-item>
              <el-dropdown-item divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <div class="app-body">
      <!-- 左侧菜单 -->
      <aside class="app-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#409EFF"
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
          <el-sub-menu index="4">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>AI知识库</span>
            </template>
            <el-menu-item index="4-1">知识库管理</el-menu-item>
            <el-menu-item index="4-2">智能问答</el-menu-item>
            <el-menu-item index="4-3">SQL查询助手</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </aside>
      
      <!-- 右侧内容 -->
      <main class="app-content">
        <!-- AI知识库组件 -->
        <component v-if="currentAIComponent" :is="currentAIComponent"></component>
        
        <!-- 原有内容 -->
        <div v-else>
          <div class="content-header">
            <h2 class="page-title">{{ getPageTitle() }}</h2>
            <div class="header-actions">
              <el-button v-if="activeMenu === '1-1'" type="primary" @click="submitExpense" :loading="loading">
                <el-icon><Plus /></el-icon> 提交报销
              </el-button>
              <el-button v-if="activeMenu === '2-1' && !loading" type="primary" @click="handleAddUser">
                <el-icon><Plus /></el-icon> 添加用户
              </el-button>
              <el-button v-if="(activeMenu === '3-1' || activeMenu === '3-2') && !loading" type="primary" @click="saveSettings">
                <el-icon><Plus /></el-icon> 保存设置
              </el-button>
            </div>
          </div>
          
          <el-card class="content-card">
            
            <!-- 搜索表单 -->
            <el-form v-if="activeMenu !== '1-1' && !isSystemSettingPage && activeMenu !== '2-1'" :inline="true" :model="searchForm" class="search-form" label-position="left">
              <el-form-item label="报销单号" label-width="80px">
                <el-input v-model="searchForm.claimId" placeholder="请输入报销单号" clearable style="width: 200px" />
              </el-form-item>
              <el-form-item label="状态" label-width="60px">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                  <el-option label="全部" value="" />
                  <el-option label="待提交" value="DRAFT" />
                  <el-option label="待审批" value="SUBMITTED" />
                  <el-option label="已通过" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch" :loading="loading">
                  <el-icon><Search /></el-icon> 查询
                </el-button>
                <el-button @click="resetSearch">
                  <el-icon><Refresh /></el-icon> 重置
                </el-button>
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
            <el-table 
              v-if="activeMenu !== '1-1' && !isSystemSettingPage && activeMenu !== '2-1'" 
              :data="expenseClaims" 
              style="width: 100%" 
              v-loading="loading"
              element-loading-text="加载中..."
              empty-text="暂无数据"
              border
              stripe
              :fit="true"
            >
              <el-table-column prop="id" label="报销单号" min-width="100" />
              <el-table-column prop="amount" label="金额" min-width="80" />
              <el-table-column prop="type" label="类型" min-width="80" />
              <el-table-column prop="submitTime" label="提交时间" min-width="150" />
              <el-table-column prop="status" label="状态" min-width="80">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">{{ getStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="200" fixed="right">
                <template #default="scope">
                  <div class="action-buttons">
                    <el-button size="small" type="info" @click="handleView(scope.row)" plain>
                      <el-icon><View /></el-icon> 查看
                    </el-button>
                    <el-button v-if="activeMenu === '1-2'" size="small" type="primary" @click="handleEdit(scope.row)" plain>
                      <el-icon><Edit /></el-icon> 编辑
                    </el-button>
                    <el-button v-if="activeMenu === '1-2'" size="small" type="danger" @click="handleDelete(scope.row)" plain>
                      <el-icon><Delete /></el-icon> 删除
                    </el-button>
                    <el-button v-if="activeMenu === '1-3'" size="small" type="success" @click="handleApprove(scope.row, true)" plain>
                      <el-icon><Check /></el-icon> 通过
                    </el-button>
                    <el-button v-if="activeMenu === '1-3'" size="small" type="danger" @click="handleApprove(scope.row, false)" plain>
                      <el-icon><Close /></el-icon> 拒绝
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 用户列表 -->
            <el-table v-if="activeMenu === '2-1'" :data="users" style="width: 100%" v-loading="loading" element-loading-text="加载中..." border stripe>
              <el-table-column prop="id" label="用户ID" width="100" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="scope">
                  <el-tag :type="getRoleType(scope.row.role)" size="small">{{ getRoleText(scope.row.role) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditUser(scope.row)" plain>
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button size="small" type="danger" @click="handleDeleteUser(scope.row)" plain>
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 角色权限管理 -->
            <div v-if="activeMenu === '2-2'" v-loading="loading" element-loading-text="加载中...">
              <el-button type="primary" @click="handleAddRolePermission" style="margin-bottom: 20px">
                <el-icon><Plus /></el-icon> 添加权限
              </el-button>
              <el-table 
                :data="rolePermissions" 
                style="width: 100%"
                empty-text="暂无角色权限数据"
                border
                stripe
              >
                <el-table-column prop="jaRoleName" label="角色名称" width="120" />
                <el-table-column prop="jaPermissionName" label="权限名称" width="120" />
                <el-table-column prop="jaModule" label="模块" width="100" />
                <el-table-column prop="jaAction" label="操作" width="100" />
                <el-table-column prop="jaPermissionDesc" label="描述" min-width="200" />
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEditRolePermission(scope.row)" plain>
                      <el-icon><Edit /></el-icon> 编辑
                    </el-button>
                    <el-button size="small" type="danger" @click="handleDeleteRolePermission(scope.row)" plain>
                      <el-icon><Delete /></el-icon> 删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 系统设置页面 -->
            <div v-if="activeMenu === '3-1'" v-loading="loading" element-loading-text="加载中...">
              <el-form :model="systemSettings" label-width="120px" class="settings-form">
                <el-form-item label="系统名称">
                  <el-input v-model="systemSettings.systemName" placeholder="请输入系统名称" clearable />
                </el-form-item>
                <el-form-item label="审批流程配置">
                  <el-select v-model="systemSettings.processDefinition" placeholder="请选择审批流程" clearable style="width: 100%">
                    <el-option v-for="process in processDefinitions" :key="process.id" :label="process.name" :value="process.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批人配置">
                  <el-input v-model="systemSettings.approverConfig" type="textarea" placeholder="请配置审批人规则" :rows="4" />
                </el-form-item>
                <el-form-item label="部署新流程">
                  <el-upload
                    class="upload-demo"
                    action="#"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    :show-file-list="false"
                  >
                    <el-button type="primary">
                      <el-icon><Upload /></el-icon> 选择 BPMN 文件
                    </el-button>
                  </el-upload>
                  <el-button v-if="processFile" type="success" @click="deployProcess" style="margin-left: 10px">
                    <el-icon><Check /></el-icon> 部署流程
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- AI 设置页面 -->
            <div v-if="activeMenu === '3-2'" v-loading="loading" element-loading-text="加载中...">
              <el-form :model="aiSettings" label-width="120px" class="settings-form">
                <el-form-item label="AI 模型">
                  <el-select v-model="aiSettings.modelName" placeholder="请选择 AI 模型" style="width: 100%">
                    <el-option label="Qwen 3.5" value="qwen3.5-flash" />
                    <el-option label="GPT-4" value="gpt-4" />
                    <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
                  </el-select>
                </el-form-item>
                <el-form-item label="API Key">
                  <el-input v-model="aiSettings.apiKey" type="password" placeholder="请输入 API Key" show-password />
                </el-form-item>
                <el-form-item label="API 地址">
                  <el-input v-model="aiSettings.baseUrl" placeholder="请输入 API 地址" clearable />
                </el-form-item>
                <el-form-item label="温度">
                  <el-slider v-model="aiSettings.temperature" :min="0" :max="1" :step="0.1" />
                </el-form-item>
                <el-form-item label="最大 Token">
                  <el-input-number v-model="aiSettings.maxTokens" :min="100" :max="4096" :step="100" style="width: 100%" />
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 分页 -->
            <div v-if="activeMenu !== '1-1' && !isSystemSettingPage && activeMenu !== '2-1'" class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                background
                prev-text="上一页"
                next-text="下一页"
              />
            </div>
          </el-card>
        </div>
        </main>
      </div>
    
    <!-- 用户编辑对话框 -->
    <el-dialog v-model="showUserDialog" :title="currentUser.id ? '编辑用户' : '添加用户'" width="500px">
      <el-form :model="currentUser" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="currentUser.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="currentUser.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="currentUser.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="部门经理" value="manager" />
            <el-option label="普通员工" value="employee" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUserDialog = false">取消</el-button>
          <el-button type="primary" @click="saveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 角色权限编辑对话框 -->
    <el-dialog v-model="showRolePermissionDialog" :title="currentRolePermission.jaUnid ? '编辑权限' : '添加权限'" width="600px">
      <el-form :model="currentRolePermission" label-width="120px">
        <el-form-item label="角色ID">
          <el-input v-model="currentRolePermission.jaRoleId" placeholder="请输入角色ID" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="currentRolePermission.jaRoleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限ID">
          <el-input v-model="currentRolePermission.jaPermissionId" placeholder="请输入权限ID" />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model="currentRolePermission.jaPermissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="currentRolePermission.jaModule" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="操作">
          <el-input v-model="currentRolePermission.jaAction" placeholder="请输入操作类型" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentRolePermission.jaPermissionDesc" type="textarea" placeholder="请输入权限描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRolePermissionDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRolePermission">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 流程设置编辑对话框 -->
    <el-dialog v-model="showProcessSettingDialog" :title="currentProcessSetting.leUnid ? '编辑流程设置' : '添加流程设置'" width="600px">
      <el-form :model="currentProcessSetting" label-width="120px">
        <el-form-item label="流程ID">
          <el-input v-model="currentProcessSetting.leProcessId" placeholder="请输入流程ID" />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="currentProcessSetting.leProcessName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="步骤ID">
          <el-input v-model="currentProcessSetting.leStepId" placeholder="请输入步骤ID" />
        </el-form-item>
        <el-form-item label="步骤名称">
          <el-input v-model="currentProcessSetting.leStepName" placeholder="请输入步骤名称" />
        </el-form-item>
        <el-form-item label="步骤顺序">
          <el-input-number v-model="currentProcessSetting.leStepOrder" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="负责人角色">
          <el-input v-model="currentProcessSetting.leResponsibleRole" placeholder="请输入负责人角色" />
        </el-form-item>
        <el-form-item label="处理时限(小时)">
          <el-input-number v-model="currentProcessSetting.leDuration" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="条件">
          <el-input v-model="currentProcessSetting.leCondition" type="textarea" placeholder="请输入条件表达式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProcessSettingDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProcessSetting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { 
  Document, 
  User, 
  Setting, 
  Plus, 
  Search, 
  Refresh, 
  View, 
  Edit, 
  Delete, 
  Check, 
  Close, 
  Upload, 
  ArrowDown 
} from '@element-plus/icons-vue'
import { expenseApi } from './api/expense'
import KnowledgeBase from './components/KnowledgeBase.vue'
import RAGQuery from './components/RAGQuery.vue'
import TextToSQL from './components/TextToSQL.vue'

export default {
  name: 'App',
  components: {
    Document,
    User,
    Setting,
    Plus,
    Search,
    Refresh,
    View,
    Edit,
    Delete,
    Check,
    Close,
    Upload,
    ArrowDown,
    KnowledgeBase,
    RAGQuery,
    TextToSQL
  },
  data() {
    return {
      activeMenu: '1-2',
      currentAIComponent: null,
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
      processFile: null,
      // 用户列表
      users: [],
      // 当前编辑的用户
      currentUser: {
        id: null,
        username: '',
        password: '',
        name: '',
        role: 'employee'
      },
      // 用户对话框显示状态
      showUserDialog: false,
      // 角色权限相关
      rolePermissions: [],
      currentRolePermission: {
        jaUnid: null,
        jaRoleId: '',
        jaRoleName: '',
        jaPermissionId: '',
        jaPermissionName: '',
        jaPermissionDesc: '',
        jaModule: '',
        jaAction: ''
      },
      showRolePermissionDialog: false,
      // 流程设置相关
      processSettings: [],
      currentProcessSetting: {
        leUnid: null,
        leProcessId: '',
        leProcessName: '',
        leStepId: '',
        leStepName: '',
        leStepOrder: 1,
        leResponsibleRole: '',
        leDuration: 24
      },
      showProcessSettingDialog: false,
      // 用户信息
      userName: '管理员',
      userAvatar: ''
    }
  },
  methods: {
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
      } else if (key === '2-1') {
        // 用户列表页面
        this.loadUsers()
      } else if (key === '2-2') {
        // 角色权限页面
        this.loadRolePermissions()
      } else if (key === '3-1') {
        // 流程设置页面
        this.loadProcessSettings()
      } else if (key === '3-2') {
        // AI 设置页面
        this.loadAISettings()
      } else if (key === '4-1') {
        // 知识库管理页面
        this.currentAIComponent = 'KnowledgeBase'
      } else if (key === '4-2') {
        // 智能问答页面
        this.currentAIComponent = 'RAGQuery'
      } else if (key === '4-3') {
        // SQL查询助手页面
        this.currentAIComponent = 'TextToSQL'
      }
    },
    // 加载报销单列表
    async loadExpenseClaims() {
      this.loading = true
      try {
        // 构建查询参数
        const params = {
          status: this.searchForm.status,
          claimId: this.searchForm.claimId,
          page: this.currentPage,
          size: this.pageSize
        }
        const response = await expenseApi.getExpenseClaims(params)
        
        // 处理后端返回的数据格式
        if (Array.isArray(response)) {
          // 后端直接返回数组
          this.expenseClaims = response
          this.total = response.length
        } else {
          // 后端返回对象格式
          this.expenseClaims = response.claims || response.data || []
          this.total = response.total || this.expenseClaims.length
        }
        
        // 显示空数据提示
        if (this.expenseClaims.length === 0) {
          this.$message.info('未找到符合条件的报销单')
        }
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
        
        // 处理后端返回的数据格式
        const tasks = Array.isArray(response) ? response : (response.tasks || response.data || [])
        
        // 处理任务数据，获取关联的报销单信息
        const tasksWithClaims = []
        for (const task of tasks) {
          const variables = task.processVariables || task.variables || {}
          const claimId = variables.claimId
          if (claimId) {
            try {
              const claimDetail = await expenseApi.getExpenseClaimDetail(claimId)
              // 处理报销单详情的返回格式
              const claimData = claimDetail.claim || claimDetail.data || claimDetail
              tasksWithClaims.push({
                ...claimData,
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
        
        // 显示空数据提示
        if (this.expenseClaims.length === 0) {
          this.$message.info('暂无审批任务')
        }
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
      this.currentPage = 1 // 搜索时重置到第一页
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
      this.currentPage = 1 // 重置搜索时也重置到第一页
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
      this.$confirm('确认删除该报销单吗？此操作不可恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(async () => {
        this.loading = true
        try {
          // 调用删除 API
          await expenseApi.deleteExpenseClaim(row.id)
          this.$message.success('删除成功')
          this.loadExpenseClaims()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败：' + (error.response?.data || error.message || '未知错误'))
        } finally {
          this.loading = false
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
    },
    // 加载用户列表
    async loadUsers() {
      this.loading = true
      try {
        const response = await expenseApi.getUsers()
        // 处理后端返回的数据格式
        this.users = Array.isArray(response) ? response : (response.users || response.data || [])
        
        // 显示空数据提示
        if (this.users.length === 0) {
          this.$message.info('暂无用户数据')
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        this.$message.error('加载用户列表失败：' + (error.message || '未知错误'))
        this.users = []
      } finally {
        this.loading = false
      }
    },
    // 添加用户
    handleAddUser() {
      this.currentUser = {
        id: null,
        username: '',
        password: '',
        name: '',
        role: 'employee'
      }
      this.showUserDialog = true
    },
    // 编辑用户
    handleEditUser(user) {
      this.currentUser = { ...user }
      this.showUserDialog = true
    },
    // 删除用户
    handleDeleteUser(user) {
      this.$confirm('确定要删除用户 ' + user.name + ' 吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          await expenseApi.deleteUser(user.id)
          this.$message.success('删除用户成功')
          this.loadUsers()
        } catch (error) {
          console.error('删除用户失败:', error)
          this.$message.error('删除用户失败')
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    },
    // 保存用户
    async saveUser() {
      if (!this.currentUser.username || !this.currentUser.password || !this.currentUser.name) {
        this.$message.warning('请填写完整的用户信息')
        return
      }
      this.loading = true
      try {
        await expenseApi.saveUser(this.currentUser)
        this.$message.success('保存用户成功')
        this.showUserDialog = false
        this.loadUsers()
      } catch (error) {
        console.error('保存用户失败:', error)
        this.$message.error('保存用户失败')
      } finally {
        this.loading = false
      }
    },
    // 获取角色类型
    getRoleType(role) {
      switch (role) {
        case 'admin': return 'danger'
        case 'manager': return 'warning'
        case 'employee': return 'info'
        default: return 'info'
      }
    },
    // 获取角色文本
    getRoleText(role) {
      switch (role) {
        case 'admin': return '管理员'
        case 'manager': return '部门经理'
        case 'employee': return '普通员工'
        default: return role
      }
    },
    // 加载角色权限
    async loadRolePermissions() {
      this.loading = true
      try {
        const response = await expenseApi.getRolePermissions()
        // 处理后端返回的数据格式
        this.rolePermissions = Array.isArray(response) ? response : (response.permissions || response.data || [])
        
        // 显示空数据提示
        if (this.rolePermissions.length === 0) {
          this.$message.info('暂无角色权限数据')
        }
      } catch (error) {
        console.error('加载角色权限失败:', error)
        this.$message.error('加载角色权限失败：' + (error.message || '未知错误'))
        this.rolePermissions = []
      } finally {
        this.loading = false
      }
    },
    // 添加角色权限
    handleAddRolePermission() {
      this.currentRolePermission = {
        jaUnid: null,
        jaRoleId: '',
        jaRoleName: '',
        jaPermissionId: '',
        jaPermissionName: '',
        jaPermissionDesc: '',
        jaModule: '',
        jaAction: ''
      }
      this.showRolePermissionDialog = true
    },
    // 编辑角色权限
    handleEditRolePermission(permission) {
      this.currentRolePermission = { ...permission }
      this.showRolePermissionDialog = true
    },
    // 删除角色权限
    handleDeleteRolePermission(permission) {
      this.$confirm('确定要删除该权限吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          await expenseApi.deleteRolePermission(permission.jaUnid)
          this.$message.success('删除权限成功')
          this.loadRolePermissions()
        } catch (error) {
          console.error('删除权限失败:', error)
          this.$message.error('删除权限失败')
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    },
    // 保存角色权限
    async saveRolePermission() {
      if (!this.currentRolePermission.jaRoleId || !this.currentRolePermission.jaPermissionId) {
        this.$message.warning('请填写完整的权限信息')
        return
      }
      this.loading = true
      try {
        await expenseApi.saveRolePermission(this.currentRolePermission)
        this.$message.success('保存权限成功')
        this.showRolePermissionDialog = false
        this.loadRolePermissions()
      } catch (error) {
        console.error('保存权限失败:', error)
        this.$message.error('保存权限失败')
      } finally {
        this.loading = false
      }
    },
    // 加载流程设置
    async loadProcessSettings() {
      this.loading = true
      try {
        const response = await expenseApi.getProcessSettings()
        // 处理后端返回的数据格式
        this.processSettings = Array.isArray(response) ? response : (response.settings || response.data || [])
        
        // 显示空数据提示
        if (this.processSettings.length === 0) {
          this.$message.info('暂无流程设置数据')
        }
      } catch (error) {
        console.error('加载流程设置失败:', error)
        this.$message.error('加载流程设置失败：' + (error.message || '未知错误'))
        this.processSettings = []
      } finally {
        this.loading = false
      }
    },
    // 添加流程设置
    handleAddProcessSetting() {
      this.currentProcessSetting = {
        leUnid: null,
        leProcessId: '',
        leProcessName: '',
        leStepId: '',
        leStepName: '',
        leStepOrder: 1,
        leResponsibleRole: '',
        leDuration: 24
      }
      this.showProcessSettingDialog = true
    },
    // 编辑流程设置
    handleEditProcessSetting(setting) {
      this.currentProcessSetting = { ...setting }
      this.showProcessSettingDialog = true
    },
    // 删除流程设置
    handleDeleteProcessSetting(setting) {
      this.$confirm('确定要删除该流程步骤吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          await expenseApi.deleteProcessSetting(setting.leUnid)
          this.$message.success('删除流程步骤成功')
          this.loadProcessSettings()
        } catch (error) {
          console.error('删除流程步骤失败:', error)
          this.$message.error('删除流程步骤失败')
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    },
    // 保存流程设置
    async saveProcessSetting() {
      if (!this.currentProcessSetting.leProcessId || !this.currentProcessSetting.leStepId) {
        this.$message.warning('请填写完整的流程信息')
        return
      }
      this.loading = true
      try {
        await expenseApi.saveProcessSetting(this.currentProcessSetting)
        this.$message.success('保存流程设置成功')
        this.showProcessSettingDialog = false
        this.loadProcessSettings()
      } catch (error) {
        console.error('保存流程设置失败:', error)
        this.$message.error('保存流程设置失败')
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
  background-color: #f5f7fa;
}

/* 顶部导航栏 */
.app-header {
  height: 60px;
  background-color: #001529;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.header-left .logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #409EFF;
}

.logo-text {
  color: white;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.header-right .user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
}

/* 主内容区 */
.app-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧菜单 */
.app-sidebar {
  width: 200px;
  background-color: #001529;
  overflow-y: auto;
  transition: all 0.3s;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
  margin: 0;
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  margin: 0;
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.1);
  color: #409EFF;
}

/* 右侧内容 */
.app-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 卡片样式 */
.content-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 搜索表单 */
.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
}

/* 设置表单 */
.settings-form {
  max-width: 600px;
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 操作按钮容器 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 4px 0;
}

.action-buttons .el-button {
  white-space: nowrap;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    width: 64px;
  }
  
  .sidebar-menu .el-sub-menu__title span,
  .sidebar-menu .el-menu-item span {
    display: none;
  }
  
  .app-content {
    padding: 16px;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .search-form .el-form-item:last-child {
    align-self: flex-start;
  }
  
  /* 移动端操作按钮调整 */
  .action-buttons {
    gap: 4px;
  }
  
  .action-buttons .el-button {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .action-buttons .el-button .el-icon {
    font-size: 12px;
  }
}
</style>
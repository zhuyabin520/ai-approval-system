# AI 智能报销审批系统

基于 Spring Boot + Flowable + LangChain4j 的智能报销审批系统，集成 AI 能力实现报销单的自动合规检查、智能审批建议与多级审批流程管理。

## 核心特性

### AI 驱动的智能审批

系统深度融合 AI 技术，在报销审批流程的各个环节提供智能化支持：

- **RAG 知识库检索**：基于公司报销制度文档（如差旅费标准、招待费规定等），通过向量检索获取相关条款，确保审核依据准确
- **AI 合规性检查**：自动分析报销单内容，检测是否符合公司制度（如住宿费是否超标、招待费是否需事前申请等）
- **结构化输出**：使用 JSON Schema 强制 AI 输出标准化审核结果，包含审批决定、判断理由、违反条款、建议审批人等
- **智能审批建议**：基于历史审批模式和报销单特征，推荐合适的审批路径

### 报销审批流程

```
提交报销单 → AI 合规检查 → 部门经理审批 → 财务审批 → 审批完成
                ↓
            [AI 拒绝] → 直接驳回
```

1. **AI 合规检查**：作为流程第一关，AI 基于知识库文档进行自动审核
   - 通过：进入人工审批环节
   - 拒绝：直接驳回并给出具体原因

2. **部门经理审批**：对通过 AI 检查的报销单进行业务合理性审核

3. **财务审批**：最终财务合规确认

## 技术架构

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| Flowable | 7.0.0 | 工作流引擎 |
| LangChain4j | 0.35.0 | AI 能力集成 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.x | 数据存储 |

### AI 技术组件

| 组件 | 说明 |
|------|------|
| 大语言模型 | 通义千问 (Qwen) via 阿里云 DashScope API |
| 嵌入模型 | text-embedding-v1（千问向量模型） |
| 向量存储 | InMemoryEmbeddingStore（开发环境）/ PGVector（生产环境可选） |
| RAG 框架 | LangChain4j 的 ContentRetriever + EmbeddingStore |

### 前端技术栈

- Vue 3 + Vite
- Element Plus UI 组件库
- Axios HTTP 客户端

## 项目结构

```
aiFlowable/
├── src/main/java/com/tmccloud/aiapproval/
│   ├── config/                 # 配置类
│   │   ├── RagConfig.java      # RAG 向量检索配置
│   │   └── AiServiceConfig.java # AI 服务配置
│   ├── controller/             # REST API 接口
│   │   ├── ExpenseClaimController.java  # 报销单管理
│   │   └── ApprovalController.java      # AI 审批接口
│   ├── service/                # 业务逻辑层
│   │   ├── ExpenseClaimService.java     # 报销单服务
│   │   ├── ExpenseApprovalAssistant.java # AI 审批助手接口
│   │   ├── AIService.java               # AI 通用服务
│   │   └── DocumentService.java         # 文档向量化处理
│   ├── entity/                 # 实体类
│   │   ├── ExpenseClaim.java   # 报销单实体
│   │   └── ExpenseCheckResult.java # AI 审核结果DTO
│   ├── listener/               # Flowable 监听器
│   │   └── AICheckTaskListener.java # AI 检查任务监听器
│   └── mapper/                 # 数据访问层
├── src/main/resources/
│   ├── processes/
│   │   └── expense_approval.bpmn20.xml  # 报销审批流程定义
│   └── db/init.sql             # 数据库初始化脚本
└── aiFlowableVue/              # 前端项目
    └── src/
```

## AI 功能详解

### 1. RAG 知识库

系统将公司报销制度文档转换为向量存储，支持语义检索：

- **文档加载**：启动时自动加载 `knowledge-base-path` 目录下的制度文档
- **文档切分**：使用递归切分器（500字符/段，100字符重叠）保证上下文连贯
- **向量化**：使用千问 text-embedding-v1 模型生成文档向量
- **语义检索**：根据报销单内容检索最相关的制度条款

### 2. AI 审批助手 (ExpenseApprovalAssistant)

通过 LangChain4j 的 `@AiService` 注解定义，核心能力：

```java
@SystemMessage("你是一个专业的财务审批助手...")
@UserMessage("请审核以下报销单：...")
ExpenseCheckResult checkExpense(
    @V("employeeName") String employeeName,
    @V("expenseType") String expenseType,
    @V("totalAmount") BigDecimal totalAmount,
    ...
);
```

**输出结构 (ExpenseCheckResult)**：
- `decision`: APPROVED / REJECTED / NEED_MORE_INFO
- `reason`: 判断理由
- `violatedRules`: 违反的制度条款列表
- `suggestedApprover`: 建议的审批人
- `confidence`: 判断置信度 (0-1)

### 3. 流程集成

在 Flowable 流程定义中，AI 检查作为独立的 UserTask：

```xml
<userTask id="aiCheckTask" name="AI 合规性检查">
    <extensionElements>
        <activiti:taskListener event="create" 
            class="com.tmccloud.aiapproval.listener.AICheckTaskListener" />
    </extensionElements>
</userTask>
```

AI 检查结果 (`aiCheckResult`) 作为流程变量控制分支走向。

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Node.js 16+（前端）

### 配置

1. **数据库配置**：修改 `application.yml` 中的数据库连接信息

2. **AI API 配置**：配置阿里云 DashScope API Key
   ```yaml
   langchain4j:
     open-ai:
       chat-model:
         base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
         api-key: your-api-key
         model-name: qwen3.5-flash
   ```

3. **知识库路径**：配置制度文档存放目录
   ```yaml
   app:
     knowledge-base-path: D:/knowledge-base
   
   ``` src/main/resources/db/expense-policy.txt

### 启动

```bash
# 后端
mvn spring-boot:run

# 前端
cd aiFlowableVue
npm install
npm run dev
```

## API 接口

### 报销单管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/expense/submit` | POST | 提交报销单 |
| `/api/expense/tasks/{assignee}` | GET | 获取待审批任务 |
| `/api/expense/approve/{taskId}` | POST | 审批任务 |

### AI 审批

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/approval/check` | POST | AI 审核报销单 |
| `/api/approval/health` | GET | 服务健康检查 |

## 知识库文档示例

系统支持将公司制度文档（如 `expense-policy.txt`）作为 AI 审批的知识来源：

```
## 第二章 差旅费报销标准
2.3 住宿费标准：
   - 一线城市（北上广深）：不超过800元/天
   - 省会城市：不超过500元/天
   - 其他城市：不超过350元/天

## 第三章 招待费规定
3.1 招待费需事先申请，注明招待对象、人数、预算。
3.3 单次招待费超过3000元需总经理审批。
```

AI 会自动检索相关条款并引用到审核结果中。

## 许可证

MIT License

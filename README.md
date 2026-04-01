# AI 智能报销审批系统

基于 Spring Boot + Flowable + LangChain4j + 千问大模型的智能报销审批系统，集成 AI 能力实现报销单的自动合规检查、智能审批建议与多级审批流程管理，同时提供知识库管理、智能问答和SQL查询助手功能。

## 项目概述

本系统旨在通过人工智能技术提升企业报销审批效率，确保审批过程的合规性和准确性。系统融合了现代工作流引擎与先进的大语言模型技术，实现了从报销单提交到审批完成的全流程智能化管理。

### 核心功能

#### 1. 智能报销审批
- **AI 合规性检查**：基于公司报销制度文档自动分析报销单内容，检测是否符合公司制度
- **多级审批流程**：部门经理审批 → 财务审批的完整流程
- **智能审批建议**：基于历史审批模式和报销单特征，推荐合适的审批路径
- **审批记录追踪**：完整记录审批过程和决策依据

#### 2. 知识库管理系统
- **文档上传与管理**：支持上传PDF、Word等格式的文档
- **文档解析与向量化**：自动解析文档内容并转换为向量存储
- **向量数据库集成**：使用Milvus向量数据库存储和检索文档向量
- **文档标签管理**：支持为文档添加标签，方便分类和检索

#### 3. RAG（检索增强生成）智能问答
- **基于知识库的问答**：用户可以针对知识库内容提问，系统会检索相关信息并生成准确回答
- **上下文理解**：支持带上下文的连续对话
- **历史记录管理**：保存用户的问答历史，方便后续查询

#### 4. Text-to-SQL智能查询助手
- **自然语言转SQL**：将用户的自然语言查询转换为SQL语句
- **SQL执行与结果展示**：自动执行生成的SQL并以表格形式展示结果
- **SQL解释**：为生成的SQL语句提供详细解释，帮助用户理解

## 技术栈

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| Flowable | 7.0.0 | 工作流引擎 |
| LangChain4j | 0.35.0 | AI 能力集成 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.x | 数据存储 |
| Milvus | 2.4.4 | 向量数据库 |

### AI 技术组件

| 组件 | 说明 |
|------|------|
| 大语言模型 | 通义千问 (Qwen3.5-Flash) via 阿里云 DashScope API |
| 嵌入模型 | 通义千问 (Qwen3-VL-Embedding) |
| 向量存储 | Milvus 向量数据库 |
| RAG 框架 | LangChain4j 的 ContentRetriever + EmbeddingStore |

### 前端技术栈

- Vue 3 + Vite
- Element Plus UI 组件库
- Axios HTTP 客户端

## 目录结构

```
ai-approval-system/
├── ai-approval-java/            # 后端项目
│   ├── src/main/java/com/tmccloud/aiapproval/
│   │   ├── config/              # 配置类
│   │   │   ├── AIConfig.java
│   │   │   ├── AiServiceConfig.java
│   │   │   ├── EmbeddingConfig.java
│   │   │   ├── JsonSchemaConfig.java
│   │   │   ├── MilvusInitializer.java
│   │   │   ├── MilvusVectorStoreConfig.java
│   │   │   ├── RagConfig.java
│   │   │   └── VectorStoreCheckConfig.java
│   │   ├── controller/          # REST API 接口
│   │   │   ├── ApprovalController.java
│   │   │   ├── ExpenseClaimController.java
│   │   │   ├── KnowledgeBaseController.java
│   │   │   ├── PermissionController.java
│   │   │   ├── RAGController.java
│   │   │   ├── SystemController.java
│   │   │   └── TextToSQLController.java
│   │   ├── entity/              # 实体类
│   │   │   ├── AISettings.java
│   │   │   ├── ApprovalRecord.java
│   │   │   ├── ExpenseCheckResult.java
│   │   │   ├── ExpenseClaim.java
│   │   │   ├── ExpenseItem.java
│   │   │   ├── KnowledgeBase.java
│   │   │   ├── ProcessSetting.java
│   │   │   ├── RolePermission.java
│   │   │   └── User.java
│   │   ├── listener/            # Flowable 监听器
│   │   │   ├── AICheckTaskListener.java
│   │   │   ├── FinanceApprovalTaskListener.java
│   │   │   └── ManagerApprovalTaskListener.java
│   │   ├── mapper/              # 数据访问层
│   │   │   ├── AISettingsMapper.java
│   │   │   ├── ApprovalRecordMapper.java
│   │   │   ├── ExpenseClaimMapper.java
│   │   │   ├── ExpenseItemMapper.java
│   │   │   ├── KnowledgeBaseMapper.java
│   │   │   ├── ProcessSettingMapper.java
│   │   │   ├── RolePermissionMapper.java
│   │   │   └── UserMapper.java
│   │   ├── service/             # 业务逻辑层
│   │   │   ├── AIService.java
│   │   │   ├── AISettingsService.java
│   │   │   ├── DocumentChunkingService.java
│   │   │   ├── DocumentParserService.java
│   │   │   ├── DocumentService.java
│   │   │   ├── EmbeddingTestService.java
│   │   │   ├── ExpenseApprovalAssistant.java
│   │   │   ├── ExpenseClaimService.java
│   │   │   ├── KnowledgeBaseService.java
│   │   │   ├── ProcessSettingService.java
│   │   │   ├── RAGService.java
│   │   │   ├── RolePermissionService.java
│   │   │   ├── TextToSQLService.java
│   │   │   └── UserService.java
│   │   ├── util/                # 工具类
│   │   │   └── SafeMetadataBuilder.java
│   │   └── AiApprovalSystemApplication.java  # 应用入口
│   ├── src/main/resources/
│   │   ├── db/                  # 数据库脚本
│   │   │   ├── ai_settings.sql
│   │   │   ├── expense-policy.txt
│   │   │   ├── init.sql
│   │   │   ├── knowledge_base.sql
│   │   │   └── vector_store_init.sql
│   │   ├── processes/           # 流程定义
│   │   │   └── expense_approval.bpmn20.xml
│   │   └── application.yml      # 应用配置
│   └── pom.xml                  # Maven 依赖
├── aiFlowableVue/               # 前端项目
│   ├── src/
│   │   ├── api/                 # API 调用
│   │   │   ├── expense.js
│   │   │   └── knowledgeBase.js
│   │   ├── components/          # 组件
│   │   │   ├── KnowledgeBase.vue
│   │   │   ├── RAGQuery.vue
│   │   │   └── TextToSQL.vue
│   │   ├── App.vue              # 应用入口组件
│   │   └── main.js              # 前端入口
│   ├── index.html               # HTML 模板
│   ├── package.json             # npm 依赖
│   └── vite.config.js           # Vite 配置
├── .gitignore                   # Git 忽略文件
└── README.md                    # 项目说明文档
```

## 环境配置

### 1. 后端环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Milvus 2.4.4+

### 2. 前端环境要求
- Node.js 16+
- npm 7+

### 3. 配置文件修改

#### 3.1 数据库配置
修改 `ai-approval-java/src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_approval_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 3.2 AI API 配置
配置阿里云 DashScope API Key：

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: your-api-key
      model-name: qwen3.5-flash
```

#### 3.3 Milvus 向量数据库配置

```yaml
milvus:
  host: localhost
  port: 19530
  database-name: default
  username: root
  password: Milvus
  collection:
    name: knowledge_base
```

#### 3.4 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

## 安装与启动

### 1. 数据库初始化

1. 创建数据库 `ai_approval_system`
2. 执行 `ai-approval-java/src/main/resources/db/init.sql` 脚本初始化数据库结构

### 2. 后端启动

```bash
# 进入后端目录
cd ai-approval-java

# 编译并启动服务
mvn spring-boot:run
```

### 3. 前端启动

```bash
# 进入前端目录
cd aiFlowableVue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 4. 访问系统

- 前端地址：http://localhost:3000
- 后端API地址：http://localhost:8080/api

## API 接口说明

### 1. 报销单管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/expense/list` | GET | 获取报销单列表 |
| `/api/expense/submit` | POST | 提交报销单 |
| `/api/expense/detail/{id}` | GET | 获取报销单详情 |
| `/api/expense/{id}` | DELETE | 删除报销单 |
| `/api/expense/approve/{taskId}` | POST | 审批任务 |
| `/api/expense/tasks/{assignee}` | GET | 获取待审批任务 |
| `/api/expense/approval-records/{claimId}` | GET | 获取审批记录 |

### 2. 知识库管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/knowledge-base/upload` | POST | 上传文档 |
| `/api/knowledge-base/all` | GET | 获取所有文档 |
| `/api/knowledge-base/{id}` | GET | 获取单个文档 |
| `/api/knowledge-base/{id}` | DELETE | 删除文档 |
| `/api/knowledge-base/search-by-tag` | GET | 按标签搜索文档 |
| `/api/knowledge-base/init` | POST | 初始化Milvus集合 |

### 3. RAG 智能问答

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/rag/query` | POST | 执行RAG查询 |
| `/api/rag/query-with-context` | POST | 执行带上下文的RAG查询 |

### 4. Text-to-SQL 智能查询

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/text-to-sql/query` | POST | 执行Text-to-SQL查询 |
| `/api/text-to-sql/explain` | POST | 解释SQL语句 |

### 5. 系统管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/system/settings` | GET | 获取系统设置 |
| `/api/system/settings` | PUT | 更新系统设置 |
| `/api/system/ai-settings` | GET | 获取AI设置 |
| `/api/system/ai-settings` | PUT | 更新AI设置 |
| `/api/system/process-definitions` | GET | 获取流程定义 |
| `/api/system/process-deploy` | POST | 部署流程 |

## 使用指南

### 1. 知识库管理

1. **上传文档**：在"知识库管理"页面点击"选择文件"按钮，选择要上传的文档，填写描述和标签，然后点击"上传"按钮。
2. **查看文档**：在文档列表中点击"查看"按钮，可以查看文档的详细信息。
3. **删除文档**：在文档列表中点击"删除"按钮，可以删除不需要的文档。
4. **按标签搜索**：在搜索框中输入标签，点击"搜索"按钮，可以按标签搜索文档。
5. **初始化向量库**：点击"初始化向量库"按钮，可以初始化Milvus向量数据库。

### 2. 智能问答

1. **提问**：在"智能问答"页面的输入框中输入问题，然后点击"提交查询"按钮。
2. **查看回答**：系统会根据知识库内容生成回答，并显示在回答区域。
3. **历史记录**：系统会保存问答历史，点击历史记录可以重新加载之前的问题和回答。

### 3. SQL查询助手

1. **输入查询**：在"SQL查询助手"页面的输入框中输入自然语言查询，然后点击"生成并执行SQL"按钮。
2. **查看结果**：系统会将自然语言转换为SQL语句并执行，显示生成的SQL语句和查询结果。
3. **解释SQL**：点击"解释SQL"按钮，可以查看SQL语句的详细解释。
4. **历史记录**：系统会保存查询历史，点击历史记录可以重新加载之前的查询。

### 4. 报销审批

1. **提交报销单**：在"报销管理"页面填写报销单信息，点击"提交"按钮。
2. **AI 检查**：系统会自动进行AI合规性检查，检查是否符合公司制度。
3. **审批流程**：通过AI检查的报销单会进入部门经理审批 → 财务审批的流程。
4. **查看审批状态**：在"报销管理"页面可以查看报销单的审批状态和历史记录。

## 贡献指南

1. **代码风格**：遵循项目现有的代码风格和命名规范。
2. **提交规范**：提交代码时，使用清晰的提交信息，说明修改的内容和原因。
3. **测试**：在提交代码前，确保代码通过测试。
4. **文档**：更新相关文档，确保文档与代码保持一致。

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系项目维护者。

---

**版本**：1.0.0
**最后更新**：2026-04-01
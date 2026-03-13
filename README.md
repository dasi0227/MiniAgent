# Dasi MiniAgent



## 项目简介

### 概述

Dasi MiniAgent 是一个集成了 AI 对话、多角色 Agent 工作流、RAG 知识库和 MCP 工具调用的全栈项目，包含用户端与管理后台两套前端页面，以及基于 DDD 架构的完整后端服务。

- 前端：Vue 3、Vite、TailwindCSS、Axios、Pinia、vue-router、vue-flow
- 后端：Spring Boot 3、Java 17、Spring AI、MyBatis、MySQL、PostgreSQL、Redis、Docker、OneAPI

### 目录结构

```
.
├── backend                     # 后端工程
│   ├── ai-agent-api            # 网络接口层
│   ├── ai-agent-app            # 应用启动与配置层
│   ├── ai-agent-domain         # 服务领域层
│   ├── ai-agent-infrastructure # 基础设施层
│   ├── ai-agent-trigger        # 接口适配层
│   ├── ai-agent-types          # 公共对象层
│   ├── docs/mysql              # 数据库脚本
│   ├── docs/docker             # docker compose
│   └── pom.xml                 # Maven 聚合配置
│
├── frontend                    # 前端工程
│   ├── index.html              # 入口 HTML
│   ├── public                  # 公共静态资源
│   ├── src                     # 前端源码目录
│   │   ├── App.vue             # 根组件
│   │   ├── main.js             # 应用入口
│   │   ├── style.css           # 全局样式与主题变量
│   │   ├── assets              # 图片图标等静态素材
│   │   ├── components          # 页面与通用组件
│   │   ├── request             # 请求封装、接口方法与鉴权数据处理
│   │   ├── router              # 路由配置与 Pinia 状态管理
│   │   └── utils               # 通用工具函数
│   ├── tailwind.config.js      # TailwindCSS 配置
│   └── vite.config.js          # Vite 构建配置
│
└── mcp                         # MCP 自建服务集合
    ├── docker-build.sh         # MCP 构建脚本
    ├── start-mcp.sh         		# MCP 运行脚本
    ├── mcp-server-amap         # 高德地图 MCP 服务
    ├── mcp-server-bocha        # 博查搜索 MCP 服务
    ├── mcp-server-csdn         # CSDN MCP 服务
    ├── mcp-server-email        # 邮件发送 MCP 服务
    └── mcp-server-wecom        # 企业微信 MCP 服务
```

## 核心功能

### Chat Client

- **对话增强**：通过 `AugmentService` 注入 RAG 检索上下文与 MCP 工具调用能力。
- **会话记忆**：基于 `sessionId` 透传到 `ChatMemory Advisor`，实现多轮上下文。
- **消息持久化**：设置埋点，将用户消息和助手消息按有效输出持久。
- **回答可控**：可传入 `temperature`、`presencePenalty`、`maxCompletionTokens`、`mcp tools`、`rag tag`，支持非流式对话和流式对话。

### Work Agent

- **阶段性输出**：节点输出按 `sectionType` 包装为统一结构对象，利用 SSE 消息事件输出执行过程中的分段结果。
- **动态策略**：通过 `DispatchService` + `ExecuteStrategyFactory`，使用工厂设计模式，按 `agentId` 动态选择执行策略。
- **灵活配置**：智能体的提示词、MCP 工具和 API 接入支持用户进行自定义修改。
- **loop 策略**：`Analyzer -> Performer -> Supervisor` 多轮执行，按需求循环执行直到完成或达到 `maxRound`。
- **step 策略**：`Inspector -> Planner -> Runner -> Replier` 顺序执行，按步骤重复执行直到完成或达到 `maxRetry`。
- **react 策略**：`Observer -> Reasoner -> Actor -> Evaluator` 单步滚动，按现状逐步执行直到完成或达到 `maxPace`。

### MiniAgent Workspace

- **Studio**：按任务描述组合模型、MCP 和执行策略，一键生成可运行的 MiniAgent。
- **Plaza**：面向社区的模板广场，支持检索筛选、点赞收藏评论，并可进入详情一键 Fork。
- **Repository**：个人仓库统一管理“已创建/已添加/已收藏”的 MiniAgent，支持查看与维护。
- **Setting**：统一管理个人资料、模型/API、MCP 与定时 Task 配置，支持新增、编辑和启停。

### MCP Server

- **mcp-server-csdn**：默认端口 9001，工具 `saveArticle`，发布文章到 CSDN；关键入参 `title`、`markdownContent`。
- **mcp-server-wecom**：默认端口 9002，工具 `sendText`、`sendTextCard`，发送企业微信应用消息；关键入参 `content` 或 `title/description/url`。
- **mcp-server-amap**：默认端口 9003，工具 `checkWeather`，根据地址查询天气；关键入参 `address`
- **mcp-server-email**：默认端口 9004，工具 `sendEmail`，发送邮件通知；关键入参 `to`、`subject`、`content`、`html`。
- **mcp-server-bocha**：默认端口 9005，工具 `webSearch`，联网搜索；关键入参 `query`、`freshness`。

### RAG

- **文件支持**：支持纯文本文件上传和 Git 仓库导入，Embedding 成向量后写入  `PgVectorStore` 。
- **标签隔离**：每个文档块写入 `knowledge=ragTag` 元数据，实现按知识库标签检索。
- **检索增强**：对话时默认 `topK=5`，命中片段注入系统提示后与用户问题一起发给模型。


### Panel

- **仪表盘  `Dashboard` **：总量统计、消息趋势（7d/30d）、chat/work 使用分布、Top 使用排行。
- **库表操作 `Table`**：支持管理员在线对核心配置做 CRUD 与状态切换（启用/禁用）。
- **客户端绑定 `Config`**：按 `clientId` 分组管理配置项（`prompt/advisor/mcp` 等）。
- **智能体工作流 `Flow`**：按 Agent 类型（loop/step/react）维护角色链路与顺序。
- **配置画布 `Canvas`**：基于 `vue-flow` 的可视化流程画布，展示 Agent 与 Client、Model 等配置项的连接关系。
- **会话审计 `Session`**：按会话类型查看 `chat/work` 类型的历史消息卡片。

## 页面展示

### User

![image-20260313173756223](./assets/image-20260313173756223.png)

![image-20260313174905274](./assets/image-20260313174905274.png)

![image-20260313174920603](./assets/image-20260313174920603.png)

![image-20260313174930894](./assets/image-20260313174930894.png)

![image-20260313175007028](./assets/image-20260313175007028.png)

![image-20260313175018380](./assets/image-20260313175018380.png)

![image-20260313175059730](./assets/image-20260313175059730.png)

![image-20260313175948054](./assets/image-20260313175948054.png)

![image-20260313173810710](./assets/image-20260313173810710.png)

### Admin

![image-20260313180008675](./assets/image-20260313180008675.png)

![image-20260313180018281](./assets/image-20260313180018281.png)

![image-20260313180053814](./assets/image-20260313180053814.png)

![image-20260313180035388](./assets/image-20260313180035388.png)

![image-20260313180108915](./assets/image-20260313180108915.png)

## 联系方式

- 📧 Email：1740929297@qq.com

- 📕 小红书：dasi0227


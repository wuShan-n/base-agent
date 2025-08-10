- # Manus：一个可扩展的、具备工具调用和RAG能力的AI助手

  

  Manus 是一个功能强大的后端项目，旨在构建一个高度可扩展的、能够与用户进行智能对话的AI助手。它深度整合了 **LangChain4j** 框架，并结合了 **Spring Boot** 的稳定性和高效性。本项目的核心特色是其 **RAG (Retrieval-Augmented Generation)** 架构，使AI能够基于外部知识库提供更准确、更具上下文的回答。此外，它还内置了多种工具（如计算器、网页抓取器和终端命令执行器），赋予了AI在对话中执行具体任务的能力。

  该项目不仅仅是一个简单的聊天机器人，而是一个完整的、可投入生产的智能代理（Agent）解决方案，包含了从前端交互、后端逻辑、模型服务到数据持久化的全链路实现。

  

  ## ✨ 项目特色

  

  - **先进的RAG架构**：通过与本地知识库的集成，AI能够检索和利用外部文档来回答用户问题，极大地减少了模型幻觉，并能回答特定领域的问题。
  - **强大的工具调用能力**：AI助手被赋予了多种实用工具，使其能够在对话中完成多样化的任务：
    - **网页抓取**：从指定的URL中提取并总结网页内容。
    - **数学计算**：执行从简单到复杂的数学表达式运算。
    - **终端命令**：安全地执行只读的本地终端命令，用于系统交互和信息查询。
  - **流式响应与前端交互**：后端采用流式API，前端可以实时接收并显示AI生成的每一个字，提供了流畅的用户体验。
  - **本地与远程模型混合使用**：
    - 通过独立的Python服务部署了本地的文本嵌入模型 (`text2vec-base-chinese`)，保证了数据处理的私密性和低成本。
    - 支持接入符合OpenAI标准的远程大语言模型（如 Gemini-2.5-Flash），兼顾了性能与灵活性。
  - **持久化对话记忆**：用户的对话历史会被自动存储到 PostgreSQL 数据库中，确保了跨会话的上下文连续性。
  - **模块化和可扩展性**：基于Spring Boot和LangChain4j的设计，项目的各个组件（如工具、模型、知识库）都易于扩展和替换。

  

  ## 🛠️ 技术架构

  

  本项目采用微服务思想，由三个核心部分组成：

  1. **Java后端 (Manus-Server)**:
     - **框架**: Spring Boot 3.2.5。
     - **核心引擎**: LangChain4j，用于构建和管理AI Agent、工具以及与模型的交互。
     - **数据库**: PostgreSQL，通过 MyBatis-Plus 进行数据持久化，存储对话历史和用户信息。
     - **向量存储**: PgVector，用于存储文档的向量化表示，是RAG功能的核心。
     - **API**: 提供RESTful API用于聊天、文档上传等。
  2. **Python模型服务 (Embedding-Service)**:
     - **框架**: FastAPI & Uvicorn。
     - **模型**: `shibing624/text2vec-base-chinese`，通过 `sentence-transformers` 库加载，用于将文本转换为向量。
     - **API**: 提供与OpenAI兼容的 `/v1/embeddings` 接口，方便Java后端调用。
  3. **前端界面**:
     - 一个简单的 `index.html` 页面，展示了如何通过JavaScript调用后端的流式聊天API并实时显示结果。

  

  ## 🚀 如何开始

  

  

  ### 环境准备

  

  - Java 21
  - Maven 3.x
  - Python 3.x
  - PostgreSQL 数据库
  - Git

  

  ### 步骤1: 启动Python嵌入模型服务

  

  此服务为RAG功能提供文本向量化能力。

  Bash

  ```
  # 切换到 main.py 所在目录
  # cd wushan-n/manus/wuShan-n-manus-757b6a27eb8f5ee4b178ee50e62348eef04f08d0/
  
  # 安装依赖
  pip install uvicorn fastapi "pydantic<2" sentence-transformers torch
  
  # 启动服务
  uvicorn main:app --host 127.0.0.1 --port 8000
  ```

  服务将在 `http://127.0.0.1:8000` 上运行。

  

  ### 步骤2: 设置PostgreSQL数据库

  

  1. 确保您的PostgreSQL服务正在运行。
  2. 在 `src/main/resources/application.yml` 文件中，更新您的数据库连接信息（URL, 用户名, 密码）。
  3. 执行 `src/main/resources/V1.sql` 脚本，创建所需的表和函数。

  

  ### 步骤3: 启动Java后端服务

  

  1. 使用IDE（如IntelliJ IDEA）打开 `pom.xml` 文件，加载Maven项目。
  2. 运行 `src/main/java/com/example/manus/ManusApplication.java` 的 `main` 方法来启动Spring Boot应用。

  

  ### 步骤4: 与AI助手互动

  

  1. 服务启动后，在浏览器中打开 `src/main/resources/static/index.html` 文件。
  2. 在输入框中输入您的问题，然后点击“发送”即可开始对话。

  

  ## 📂 项目结构概览

  

  ```
  .
  ├── main.py                   # Python 嵌入模型服务
  ├── pom.xml                   # Maven 配置文件
  └── src
      ├── main
      │   ├── java
      │   │   └── com/example/manus
      │   │       ├── agent         # AI Agent定义和工厂
      │   │       ├── config        # Spring Boot 配置 (模型, RAG, 数据库)
      │   │       ├── controller    # API 控制器 (聊天, 文档上传)
      │   │       ├── persistence   # 数据库实体和Mapper
      │   │       ├── service       # 业务逻辑服务
      │   │       ├── tool          # AI可调用的工具
      │   │       └── util          # 通用工具类 (如消息转换)
      │   └── resources
      │       ├── static
      │       │   └── index.html    # 前端交互页面
      │       ├── V1.sql            # 数据库初始化脚本
      │       └── application.yml   # Spring Boot 配置文件
      └── test                      # 测试代码
  ```

  

  ## 📜 API端点

  

  - `POST /api/assistant/chat`:
    - **功能**: 发送消息给AI助手并获取流式响应。
    - **请求体**: `{"message": "你的问题"}`
    - **响应**: `text/event-stream` 格式的流式数据。
  - `POST /api/documents/upload`:
    - **功能**: 上传一个文档（如.txt, .pdf），系统会自动将其处理并加入到知识库中。
    - **请求**: `multipart/form-data`，包含一个名为 `file` 的文件。
    - **响应**: `{"code": 200, "message": "文件处理成功", "data": ...}`。

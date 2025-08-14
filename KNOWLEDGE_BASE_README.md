# 用户自建知识库系统

基于Spring Boot + LangChain4j + PgVector实现的完整用户知识库管理系统，支持文档上传、向量化、智能检索和AI对话集成。

## 🚀 核心功能

### 1. 知识库管理
- **创建知识库**: 用户可创建多个独立的知识库
- **权限控制**: 支持私有/公开知识库，精确权限管理
- **统计信息**: 自动统计文档数量、总大小等信息
- **搜索筛选**: 支持按关键词、用户、公开状态搜索

### 2. 文档管理
- **多格式支持**: PDF、TXT、DOCX等多种文档格式
- **智能处理**: 自动文档解析、分片、向量化
- **处理状态**: 实时跟踪文档处理进度
- **批量操作**: 支持批量上传和管理

### 3. 向量搜索
- **语义搜索**: 基于向量相似度的智能搜索
- **多级检索**: 召回+重排序的多层检索架构
- **精确过滤**: 按知识库、用户等维度精确搜索
- **结果排序**: 智能相关性排序和阈值控制

### 4. AI对话集成
- **个人知识库**: AI可访问用户私有知识库
- **指定搜索**: 支持搜索特定知识库内容
- **公开资源**: 可搜索公开知识库资源
- **上下文感知**: 结合用户权限的智能检索

## 📊 数据模型

### 核心实体关系
```
User (用户)
  ↓ 1:N
KnowledgeBase (知识库)
  ↓ 1:N  
KnowledgeDocument (文档)
  ↓ 1:N
DocumentChunk (文档分片)
  ↓ 1:1
DocumentEmbedding (向量嵌入)
```

### 数据表说明
- **knowledge_bases**: 知识库基本信息
- **knowledge_documents**: 文档元数据和处理状态
- **document_chunks**: 文档分片内容
- **document_embeddings**: 向量嵌入数据(PgVector)

## 🔧 技术架构

### 后端技术栈
- **Spring Boot 3.2.5**: 核心框架
- **LangChain4j 1.3.0**: LLM集成和RAG能力
- **PgVector**: 向量数据库扩展
- **MyBatis Plus**: ORM框架
- **Sa-Token**: 权限认证
- **Redis**: 缓存和会话存储

### AI能力集成
- **文档解析**: Apache Tika多格式支持
- **文本分片**: 智能段落分割算法
- **向量嵌入**: 支持多种嵌入模型
- **检索增强**: MultiQuery + Reranking
- **对话工具**: 专用知识库搜索工具

## 📋 API接口

### 知识库管理 (/knowledge-bases)
```
POST   /knowledge-bases              # 创建知识库
GET    /knowledge-bases/my           # 获取我的知识库  
GET    /knowledge-bases/public       # 获取公开知识库
GET    /knowledge-bases/search       # 搜索知识库
PUT    /knowledge-bases/{id}         # 更新知识库
DELETE /knowledge-bases/{id}         # 删除知识库
```

### 文档管理 (/documents)
```
POST   /documents/upload             # 上传文档
GET    /documents/{id}               # 获取文档详情
DELETE /documents/{id}               # 删除文档
GET    /documents/knowledge-base/{kbId}  # 获取知识库文档
GET    /documents/search             # 搜索文档
POST   /documents/{id}/reprocess     # 重新处理文档
GET    /documents/knowledge-base/{kbId}/search  # 搜索知识库内容
```

## 🔐 权限设计

### 知识库权限
- **私有知识库**: 仅拥有者可访问
- **公开知识库**: 所有用户可访问
- **权限检查**: 所有操作都进行权限验证

### 用户权限
- `KNOWLEDGE_BASE_MANAGE`: 知识库管理权限
- `DOCUMENT_MANAGE`: 文档管理权限

## 🚀 使用指南

### 1. 数据库初始化
```sql
-- 执行知识库表结构
\i src/main/resources/V3_knowledge.sql
```

### 2. 配置文件设置
```yaml
# application.yml
app:
  file:
    upload-dir: ./uploads  # 文档上传目录

langchain4j:
  embedding-model:
    open-ai:
      base-url: http://127.0.0.1:8000/v1
      model-name: shibing624/text2vec-base-chinese
  reranker:
    http:
      url: http://127.0.0.1:8001/rerank
```

### 3. 基本操作流程

#### 创建知识库
```json
POST /knowledge-bases
{
  "name": "我的技术文档",
  "description": "收集技术相关文档",
  "isPublic": 0
}
```

#### 上传文档
```bash
curl -X POST /documents/upload \
  -F "knowledgeBaseId=kb-123" \
  -F "file=@document.pdf"
```

#### 搜索知识库
```bash
GET /documents/knowledge-base/kb-123/search?query=如何使用Spring Boot
```

### 4. AI对话集成

系统提供三个专用工具供AI使用：

- **个人知识库搜索**: 搜索当前用户的所有知识库
- **指定知识库搜索**: 搜索特定知识库内容  
- **公开知识库搜索**: 搜索公开可访问的知识库

## 📝 开发示例

### 创建知识库服务
```java
@Service
public class MyKnowledgeService {
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    public void createMyKnowledgeBase() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName("技术文档库");
        kb.setDescription("存储技术相关文档");
        kb.setIsPublic(0);
        
        knowledgeBaseService.createKnowledgeBase(kb);
    }
}
```

### 文档搜索示例
```java
@RestController
public class SearchController {
    
    @Autowired
    private KnowledgeSearchService searchService;
    
    @GetMapping("/search")
    public List<Content> search(@RequestParam String query) {
        String userId = StpUtil.getLoginIdAsString();
        return searchService.searchByUser(userId, query, 10);
    }
}
```

## 🔍 监控和维护

### 处理状态监控
- **0**: 待处理
- **1**: 处理中
- **2**: 已完成  
- **3**: 处理失败

### 统计信息
- 文档数量自动统计
- 存储空间实时计算
- 处理状态实时跟踪

### 数据维护
```sql
-- 清理失败的处理任务
UPDATE knowledge_documents SET process_status = 0 WHERE process_status = 3;

-- 更新知识库统计
SELECT * FROM knowledge_bases WHERE document_count != 
  (SELECT COUNT(*) FROM knowledge_documents WHERE knowledge_base_id = knowledge_bases.id);
```

## 🚨 注意事项

1. **文件上传限制**: 根据服务器配置调整文件大小限制
2. **向量维度**: 确保嵌入模型维度与数据库配置一致
3. **权限验证**: 所有操作都会进行用户权限检查
4. **异步处理**: 文档向量化采用异步处理，避免阻塞
5. **错误处理**: 完善的异常处理和用户友好的错误信息

## 📞 技术支持

- **API文档**: http://localhost:8080/doc.html
- **数据库**: PostgreSQL + PgVector扩展
- **向量搜索**: 支持余弦相似度和欧氏距离
- **文档格式**: PDF、Word、TXT、Markdown等

系统已集成到Knife4j文档中，可通过Web界面进行完整的API测试。
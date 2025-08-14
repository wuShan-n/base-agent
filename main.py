import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, CrossEncoder
import torch
from typing import List, Union

# --- 1. 定义新的请求和响应体 ---
class RerankRequest(BaseModel):
    query: str
    documents: List[str]

class RerankResult(BaseModel):
    document: str
    index: int
    relevance_score: float

# 创建一个 FastAPI 应用
app = FastAPI()

# 定义OpenAI兼容的请求体数据结构
class EmbeddingRequest(BaseModel):
    model: str
    input: Union[str, List[str]]
    encoding_format: str = "float"

class EmbeddingData(BaseModel):
    object: str = "embedding"
    embedding: List[float]
    index: int

class EmbeddingResponse(BaseModel):
    object: str = "list"
    data: List[EmbeddingData]
    model: str
    usage: dict
# --------------------------------------------------------------------------

# --- 2. 加载 Embedding 和 Re-ranking 模型 ---
print("正在加载模型，请稍候...")
device = 'cuda' if torch.cuda.is_available() else 'cpu'

embedding_model = SentenceTransformer('shibing624/text2vec-base-chinese', device=device)
print(f"Embedding 模型 'text2vec-base-chinese' 加载成功，运行在: {device}")

rerank_model = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2', max_length=512, device=device)
print(f"Re-ranking 模型 'ms-marco-MiniLM-L-6-v2' 加载成功，运行在: {device}")


# OpenAI兼容的API端点
@app.post("/v1/embeddings", response_model=EmbeddingResponse)
def create_embeddings(request: EmbeddingRequest):
    """
    OpenAI兼容的嵌入API端点
    """
    try:
        if isinstance(request.input, str):
            texts = [request.input]
        else:
            texts = request.input

        # --- FIX: 使用正确的变量名 embedding_model ---
        embeddings = embedding_model.encode(texts, convert_to_numpy=True)
        # ----------------------------------------------

        data = []
        for i, embedding in enumerate(embeddings):
            data.append(EmbeddingData(
                embedding=embedding.tolist(),
                index=i
            ))

        total_tokens = sum(len(text.split()) for text in texts)
        response = EmbeddingResponse(
            data=data,
            model=request.model,
            usage={
                "prompt_tokens": total_tokens,
                "total_tokens": total_tokens
            }
        )
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# --- Re-ranking API 端点 (保持不变) ---
@app.post("/v1/rerank", response_model=List[RerankResult])
def rerank_documents(request: RerankRequest):
    """
    接收一个查询和一组文档，返回经过重排的、带有相关性分数的文档列表。
    """
    try:
        sentence_pairs = [[request.query, doc] for doc in request.documents]
        scores = rerank_model.predict(sentence_pairs)
        results = []
        for i, score in enumerate(scores):
            results.append({
                "document": request.documents[i],
                "index": i,
                "relevance_score": score
            })
        sorted_results = sorted(results, key=lambda x: x['relevance_score'], reverse=True)
        return [RerankResult(**res) for res in sorted_results]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 启动服务
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000, http="h11")

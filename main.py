import uvicorn
from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
import torch
from typing import List, Union

# 创建一个 FastAPI 应用
app = FastAPI()

# 定义OpenAI兼容的请求体数据结构
class EmbeddingRequest(BaseModel):
    model: str
    input: Union[str, List[str]]
    encoding_format: str = "float"

# 定义OpenAI兼容的响应数据结构
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
# --- 在这里加载您的本地模型 ---
# --- 第一次运行时，它可能会从Hugging Face下载模型文件到本地缓存 ---
# --- 如果您已经有文件，可以指定确切的文件夹路径 ---
# --------------------------------------------------------------------------
print("正在加载模型，请稍候...")
# 确保模型在支持的设备上运行（优先使用GPU）
device = 'cuda' if torch.cuda.is_available() else 'cpu'
model = SentenceTransformer('shibing624/text2vec-base-chinese', device=device)
print(f"模型加载成功，运行在: {device}")

# OpenAI兼容的API端点
@app.post("/v1/embeddings", response_model=EmbeddingResponse)
def create_embeddings(request: EmbeddingRequest):
    """
    OpenAI兼容的嵌入API端点
    """
    try:
        # 处理输入，确保是列表格式
        if isinstance(request.input, str):
            texts = [request.input]
        else:
            texts = request.input

        # 生成嵌入向量
        embeddings = model.encode(texts, convert_to_numpy=True)

        # 构建响应数据
        data = []
        for i, embedding in enumerate(embeddings):
            data.append(EmbeddingData(
                embedding=embedding.tolist(),
                index=i
            ))

        # 计算token使用量（简单估算）
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
        return {"error": {"message": str(e), "type": "internal_error"}}

# 启动服务
if __name__ == "__main__":
    # 服务将运行在 http://127.0.0.1:8000
    uvicorn.run(app, host="127.0.0.1", port=8000,http="h11")


-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建向量存储表
CREATE TABLE IF NOT EXISTS embedding_store (
    id SERIAL PRIMARY KEY,
    embedding vector(1536),  -- 1536 维向量
    content TEXT NOT NULL,   -- 原始文本内容
    metadata JSONB,          -- 元数据
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建向量索引（提高查询性能）
CREATE INDEX IF NOT EXISTS embedding_store_embedding_idx 
    ON embedding_store USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- 插入示例数据（可选）
-- INSERT INTO embedding_store (embedding, content, metadata) VALUES
-- ('[0.1, 0.2, 0.3, ...]', '报销管理制度第1条：差旅费报销标准', '{"type": "policy", "section": "travel"}');

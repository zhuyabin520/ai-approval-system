USE ai_approval_system;

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL COMMENT '文档名称',
  `description` TEXT COMMENT '文档描述',
  `document_path` VARCHAR(500) NOT NULL COMMENT '文档存储路径',
  `document_type` VARCHAR(100) COMMENT '文档类型',
  `file_size` BIGINT COMMENT '文件大小',
  `content` TEXT COMMENT '文档内容',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `vectorization_time` DATETIME COMMENT '向量化时间',
  `status` VARCHAR(50) DEFAULT 'UPLOADED' COMMENT '状态：UPLOADED, VECTORIZED, ERROR',
  `user_id` BIGINT COMMENT '上传用户ID',
  `tags` VARCHAR(255) COMMENT '文档标签，多个标签用逗号分隔',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
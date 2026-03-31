-- AI 设置表
CREATE TABLE IF NOT EXISTS `ai_settings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `model_name` VARCHAR(50) NOT NULL DEFAULT 'qwen3.5-flash' COMMENT '模型名称',
  `api_key` VARCHAR(255) NOT NULL COMMENT 'API Key',
  `base_url` VARCHAR(255) NOT NULL DEFAULT 'https://dashscope.aliyuncs.com/compatible-mode/v1' COMMENT 'API 地址',
  `temperature` DECIMAL(3,2) NOT NULL DEFAULT 0.7 COMMENT '温度参数',
  `max_tokens` INT NOT NULL DEFAULT 2000 COMMENT '最大 Token 数',
  `timeout` INT NOT NULL DEFAULT 60 COMMENT '超时时间（秒）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化默认AI设置
INSERT INTO `ai_settings` (`model_name`, `api_key`, `base_url`, `temperature`, `max_tokens`, `timeout`, `enabled`) VALUES
('qwen3.5-flash', 'sk-53fbaf58b9c14289841f576c586f76b9', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0.7, 2000, 60, 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
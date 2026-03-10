CREATE DATABASE IF NOT EXISTS ai_approval_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_approval_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `role` VARCHAR(20) NOT NULL COMMENT '角色：admin, manager, employee',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 报销单表
CREATE TABLE IF NOT EXISTS `expense_claim` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `claim_date` DATE NOT NULL,
  `type` VARCHAR(50) NOT NULL COMMENT '报销类型：差旅费、办公费、餐饮费等',
  `description` TEXT,
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED, PENDING, APPROVED, REJECTED',
  `ai_check_result` VARCHAR(255) COMMENT 'AI 检查结果',
  `ai_opinion` TEXT COMMENT 'AI 审批意见',
  `process_instance_id` VARCHAR(100) COMMENT 'Flowable 流程实例ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 报销明细项表
CREATE TABLE IF NOT EXISTS `expense_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `claim_id` BIGINT NOT NULL,
  `item_name` VARCHAR(100) NOT NULL,
  `item_amount` DECIMAL(10,2) NOT NULL,
  `item_date` DATE NOT NULL,
  `item_description` TEXT,
  FOREIGN KEY (`claim_id`) REFERENCES `expense_claim`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审批记录表
CREATE TABLE IF NOT EXISTS `approval_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `claim_id` BIGINT NOT NULL,
  `approver_id` BIGINT NOT NULL,
  `approver_name` VARCHAR(50) NOT NULL,
  `approval_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `approval_status` VARCHAR(20) NOT NULL COMMENT '审批状态：APPROVED, REJECTED',
  `approval_opinion` TEXT,
  FOREIGN KEY (`claim_id`) REFERENCES `expense_claim`(`id`),
  FOREIGN KEY (`approver_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化数据
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('admin', '123456', '管理员', 'admin'),
('manager', '123456', '部门经理', 'manager'),
('employee', '123456', '普通员工', 'employee');

-- 初始化流程定义表（由 Flowable 自动创建）
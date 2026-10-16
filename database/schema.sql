-- 独立 MySQL 数据库结构（非破坏性：不删除表、不覆盖已有账号）。
-- 使用方式：在 MySQL 8.x 中执行本文件，然后启动 backend。
-- 管理员账号由后端首次启动时创建：admin / admin123。

CREATE DATABASE IF NOT EXISTS `travel_website`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `travel_website`;

SET NAMES utf8mb4;


CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `real_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '旅客真实姓名',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
  `wallet_balance` DECIMAL(10,2) NOT NULL DEFAULT 1280.00 COMMENT '钱包余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员编号',
  `username` VARCHAR(50) NOT NULL COMMENT '管理员账号',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `nickname` VARCHAR(50) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS `scenic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '景点编号',
  `name` VARCHAR(120) NOT NULL COMMENT '景点名称',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `address` VARCHAR(255) NOT NULL COMMENT '地址',
  `type` VARCHAR(50) NOT NULL COMMENT '景点类型',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '门票价格',
  `intro` TEXT NOT NULL COMMENT '景点介绍',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '景点图片',
  `open_time` VARCHAR(100) DEFAULT NULL COMMENT '开放时间',
  `views` BIGINT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scenic_status` (`status`),
  KEY `idx_scenic_province` (`province`),
  KEY `idx_scenic_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

CREATE TABLE IF NOT EXISTS `scenic_ticket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '票种编号',
  `scenic_id` BIGINT NOT NULL COMMENT '景点编号',
  `name` VARCHAR(40) NOT NULL COMMENT '票种名称',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前售价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1在售 0停售',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_scenic_name` (`scenic_id`, `name`),
  KEY `idx_ticket_scenic_status` (`scenic_id`, `status`),
  CONSTRAINT `fk_ticket_scenic` FOREIGN KEY (`scenic_id`) REFERENCES `scenic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点票种与库存表';

CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `order_no` VARCHAR(40) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL,
  `scenic_id` BIGINT NOT NULL,
  `scenic_name` VARCHAR(120) NOT NULL,
  `visit_date` DATE NOT NULL COMMENT '游览日期',
  `ticket_id` BIGINT DEFAULT NULL COMMENT '票种编号',
  `ticket_type` VARCHAR(30) NOT NULL DEFAULT '成人票' COMMENT '票种',
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '下单时票种单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '票数',
  `total_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `contact_name` VARCHAR(50) NOT NULL,
  `contact_phone` VARCHAR(20) NOT NULL,
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT 'wallet/scan',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CANCELLED/COMPLETED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_scenic` (`scenic_id`),
  KEY `idx_order_status` (`status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_scenic` FOREIGN KEY (`scenic_id`) REFERENCES `scenic` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门票订单表';

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价编号',
  `user_id` BIGINT NOT NULL,
  `scenic_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL COMMENT '1-5分',
  `content` VARCHAR(1000) NOT NULL,
  `reply` VARCHAR(1000) DEFAULT NULL COMMENT '管理员回复',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1显示 0隐藏',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user_scenic` (`user_id`, `scenic_id`),
  KEY `idx_comment_scenic` (`scenic_id`),
  KEY `idx_comment_status` (`status`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_scenic` FOREIGN KEY (`scenic_id`) REFERENCES `scenic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE IF NOT EXISTS `comment_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comment_id` BIGINT NOT NULL,
  `image_url` VARCHAR(500) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_comment_image_comment` (`comment_id`),
  CONSTRAINT `fk_comment_image_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价图片表';

CREATE TABLE IF NOT EXISTS `comment_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comment_id` BIGINT NOT NULL,
  `admin_id` BIGINT NOT NULL,
  `admin_name` VARCHAR(50) NOT NULL COMMENT '回复时管理员名称快照',
  `content` VARCHAR(1000) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_reply_comment` (`comment_id`, `create_time`),
  CONSTRAINT `fk_comment_reply_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_reply_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价多次回复表';

CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏编号',
  `user_id` BIGINT NOT NULL,
  `scenic_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_scenic` (`user_id`, `scenic_id`),
  KEY `idx_favorite_scenic` (`scenic_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_scenic` FOREIGN KEY (`scenic_id`) REFERENCES `scenic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

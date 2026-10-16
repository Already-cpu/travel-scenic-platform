-- 2026-10-07 功能升级：多票种库存、带图评价、多次回复。
-- 适用于已经导入旧版数据库的项目，只需执行一次。不会删除旧数据。
USE `travel_website`;
SET NAMES utf8mb4;

ALTER TABLE `order`
  ADD COLUMN `ticket_id` BIGINT DEFAULT NULL COMMENT '票种编号' AFTER `visit_date`,
  ADD COLUMN `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '下单时票种单价' AFTER `ticket_type`;

CREATE TABLE IF NOT EXISTS `scenic_ticket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `scenic_id` BIGINT NOT NULL,
  `name` VARCHAR(40) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `stock` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_scenic_name` (`scenic_id`, `name`),
  KEY `idx_ticket_scenic_status` (`scenic_id`, `status`),
  CONSTRAINT `fk_ticket_scenic` FOREIGN KEY (`scenic_id`) REFERENCES `scenic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点票种与库存表';

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
  `admin_name` VARCHAR(50) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_reply_comment` (`comment_id`, `create_time`),
  CONSTRAINT `fk_comment_reply_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_reply_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价多次回复表';

INSERT IGNORE INTO `scenic_ticket` (`scenic_id`, `name`, `price`, `stock`, `status`)
SELECT `id`, '成人票', `price`, 500, 1 FROM `scenic`;
INSERT IGNORE INTO `scenic_ticket` (`scenic_id`, `name`, `price`, `stock`, `status`)
SELECT `id`, '学生票', ROUND(`price` * 0.65, 2), 300, 1 FROM `scenic`;
INSERT IGNORE INTO `scenic_ticket` (`scenic_id`, `name`, `price`, `stock`, `status`)
SELECT `id`, '优待票', ROUND(`price` * 0.50, 2), 200, 1 FROM `scenic`;

UPDATE `order` o
JOIN `scenic_ticket` t ON t.scenic_id = o.scenic_id AND t.name = o.ticket_type
SET o.ticket_id = t.id,
    o.unit_price = CASE WHEN o.quantity > 0 THEN ROUND(o.total_price / o.quantity, 2) ELSE t.price END
WHERE o.ticket_id IS NULL;

INSERT INTO `comment_reply` (`comment_id`, `admin_id`, `admin_name`, `content`, `create_time`)
SELECT c.id, a.id, a.nickname, c.reply, c.create_time
FROM `comment` c
JOIN `admin` a ON a.id = (SELECT MIN(id) FROM `admin`)
WHERE c.reply IS NOT NULL AND c.reply <> ''
  AND NOT EXISTS (SELECT 1 FROM `comment_reply` r WHERE r.comment_id = c.id);

-- 每个景点选择一条演示评价挂接对应的实景图，便于直接展示图文评价效果。
INSERT INTO `comment_image` (`comment_id`, `image_url`, `sort_order`)
SELECT featured.comment_id, featured.image_url, 0
FROM (
  SELECT MIN(c.id) AS comment_id, s.image AS image_url
  FROM `comment` c
  JOIN `scenic` s ON s.id = c.scenic_id
  WHERE c.status = 1 AND s.image IS NOT NULL AND s.image <> ''
  GROUP BY c.scenic_id, s.image
) featured
WHERE NOT EXISTS (
  SELECT 1 FROM `comment_image` ci
  WHERE ci.comment_id = featured.comment_id AND ci.image_url = featured.image_url
);

-- 给部分已有回复的评价增加第二条跟进，演示同一评价可连续回复。
INSERT INTO `comment_reply` (`comment_id`, `admin_id`, `admin_name`, `content`, `create_time`)
SELECT first_reply.comment_id, a.id, a.nickname,
       '补充说明：如需进一步帮助，可以继续在评价区留言，我们会持续跟进。',
       DATE_ADD(first_reply.first_time, INTERVAL 1 DAY)
FROM (
  SELECT MIN(r.comment_id) AS comment_id, MIN(r.create_time) AS first_time
  FROM `comment_reply` r
  JOIN `comment` c ON c.id = r.comment_id
  GROUP BY c.scenic_id
) first_reply
JOIN `admin` a ON a.id = (SELECT MIN(id) FROM `admin`)
WHERE NOT EXISTS (
  SELECT 1 FROM `comment_reply` r2
  WHERE r2.comment_id = first_reply.comment_id
    AND r2.content = '补充说明：如需进一步帮助，可以继续在评价区留言，我们会持续跟进。'
);

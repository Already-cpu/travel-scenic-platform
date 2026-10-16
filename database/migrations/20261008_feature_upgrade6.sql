-- 新增修改意见6：记录订单支付方式。
-- 非破坏性迁移，不删除或覆盖现有业务数据。
SET @has_payment_method = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order' AND COLUMN_NAME = 'payment_method'
);
SET @add_payment_method = IF(
  @has_payment_method = 0,
  'ALTER TABLE `order` ADD COLUMN `payment_method` VARCHAR(20) DEFAULT NULL COMMENT ''wallet/scan'' AFTER `contact_phone`',
  'SELECT 1'
);
PREPARE payment_stmt FROM @add_payment_method;
EXECUTE payment_stmt;
DEALLOCATE PREPARE payment_stmt;

USE `travel_website`;

SET @has_real_name = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'real_name'
);
SET @add_real_name = IF(
  @has_real_name = 0,
  'ALTER TABLE `user` ADD COLUMN `real_name` VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''旅客真实姓名'' AFTER `nickname`',
  'SELECT 1'
);
PREPARE real_name_stmt FROM @add_real_name;
EXECUTE real_name_stmt;
DEALLOCATE PREPARE real_name_stmt;

SET @has_wallet_balance = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'wallet_balance'
);
SET @add_wallet_balance = IF(
  @has_wallet_balance = 0,
  'ALTER TABLE `user` ADD COLUMN `wallet_balance` DECIMAL(10,2) NOT NULL DEFAULT 1280.00 COMMENT ''钱包余额'' AFTER `avatar`',
  'SELECT 1'
);
PREPARE wallet_balance_stmt FROM @add_wallet_balance;
EXECUTE wallet_balance_stmt;
DEALLOCATE PREPARE wallet_balance_stmt;

UPDATE `user` SET `real_name` = `nickname` WHERE `real_name` IS NULL OR `real_name` = '';

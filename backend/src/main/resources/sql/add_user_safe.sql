-- 安全版本：自动检查字段是否存在再添加
DELIMITER $$

DROP PROCEDURE IF EXISTS add_user_column$$

CREATE PROCEDURE add_user_column()
BEGIN
    -- 检查 user_id 列是否存在
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'diary'
        AND column_name = 'user_id'
    ) THEN
        ALTER TABLE diary ADD COLUMN user_id BIGINT NOT NULL AFTER diary_date;
    END IF;

    -- 检查索引是否存在
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'diary'
        AND index_name = 'idx_user_id'
    ) THEN
        ALTER TABLE diary ADD INDEX idx_user_id (user_id);
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL add_user_column();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_user_column;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL
);

-- 创建示例用户
-- 用户名: admin, 密码: admin123
INSERT INTO user (username, password, created_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 用户名: user, 密码: user123
INSERT INTO user (username, password, created_at) VALUES
('user', '$2a$10$VT9gHUzWMR0tIwP9OdQ6uO9R7mGqYQYzW6zPqZ5tQG3hV1xPKK7e', NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 为现有日记分配给 admin 用户
UPDATE diary SET user_id = 1 WHERE user_id IS NULL;

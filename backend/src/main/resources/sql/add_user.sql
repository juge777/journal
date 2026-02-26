-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL
);

-- 给 diary 表添加 user_id 字段
-- 注意：如果字段已存在，执行会报错，可以忽略该错误
ALTER TABLE diary ADD COLUMN user_id BIGINT NOT NULL AFTER diary_date;
ALTER TABLE diary ADD INDEX idx_user_id (user_id);

-- 创建示例用户
-- 用户名: admin
-- 密码: admin123
-- 密码已使用 BCrypt 加密
INSERT INTO user (username, password, created_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 创建另一个示例用户
-- 用户名: user
-- 密码: user123
INSERT INTO user (username, password, created_at) VALUES
('user', '$2a$10$VT9gHUzWMR0tIwP9OdQ6uO9R7mGqYQYzW6zPqZ5tQG3hV1xPKK7e', NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 为现有日记分配给 admin 用户（如果没有 user_id 的话）
UPDATE diary SET user_id = 1 WHERE user_id IS NULL;

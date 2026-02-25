# 个人日记 Web 应用

一个简洁的个人日记 Web 应用，支持创建、编辑、搜索和导出日记功能。

## 技术栈

### 后端
- JDK 21
- Spring Boot 3.2.5
- Spring Data JPA
- MySQL 8

### 前端
- Vue 3 + Vite
- TypeScript
- Pinia (状态管理)
- Vue Router
- Axios

## 功能特性

- ✅ 创建、编辑、删除日记
- ✅ 按时间倒序展示日记列表
- ✅ 按关键词搜索日记
- ✅ 选择心情和天气标签
- ✅ 导出所有日记为 JSON 备份
- ✅ 响应式设计，适配手机端

## 项目结构

```
journal/
├── backend/           # Spring Boot 后端
└── frontend/          # Vue 3 前端
```

## 快速开始

### 后端

1. 配置数据库（修改 `backend/src/main/resources/application.yml`）:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/journal
    username: your_username
    password: your_password
```

2. 启动后端:
```bash
cd backend
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:8080/api`

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在 `http://localhost:5173`

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/diaries | 获取日记列表（分页） |
| GET | /api/diaries/{id} | 获取单篇日记详情 |
| POST | /api/diaries | 创建新日记 |
| PUT | /api/diaries/{id} | 更新日记 |
| DELETE | /api/diaries/{id} | 删除日记 |
| GET | /api/diaries/search | 按关键词搜索日记 |
| GET | /api/diaries/export | 导出所有日记为 JSON |

## 数据库

日记表结构：

```sql
CREATE TABLE diary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200),
    content TEXT NOT NULL,
    mood VARCHAR(50),
    weather VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 截图

![首页](screenshots/home.png)
![编辑](screenshots/edit.png)

# 个人日记 Web 项目实现计划

## Context
创建一个个人日记的 web 应用，主要面向手机端用户。采用前后端分离架构，后端使用 JDK 21 + Spring Boot + MySQL 8，前端使用 Vue 3。要求功能简单易用，页面简洁大方，并支持 JSON 格式的导出备份功能。

---

## 技术栈

### 后端
- JDK 21
- Spring Boot 3.2+
- Spring Data JPA
- MySQL 8
- Spring Security (可选，用于基础认证)

### 前端
- Vue 3 + Vite
- TypeScript
- Axios
- Day.js (日期处理)
- 移动端适配 (viewport + rem/vw)

---

## 项目结构

```
journal/
├── backend/
│   ├── src/main/java/com/journal/
│   │   ├── JournalApplication.java
│   │   ├── controller/
│   │   │   └── DiaryController.java
│   │   ├── service/
│   │   │   └── DiaryService.java
│   │   ├── repository/
│   │   │   └── DiaryRepository.java
│   │   ├── entity/
│   │   │   └── Diary.java
│   │   ├── dto/
│   │   │   ├── DiaryRequest.java
│   │   │   └── DiaryResponse.java
│   │   └── config/
│   │       └── CorsConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── data.sql
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   ├── views/
    │   │   ├── DiaryList.vue
    │   │   ├── DiaryDetail.vue
    │   │   └── DiaryEdit.vue
    │   ├── components/
    │   │   ├── DiaryCard.vue
    │   │   └── Header.vue
    │   ├── api/
    │   │   └── diary.ts
    │   ├── router/
    │   │   └── index.ts
    │   ├── stores/
    │   │   └── diary.ts
    │   ├── App.vue
    │   └── main.ts
    ├── index.html
    ├── vite.config.ts
    └── package.json
```

---

## 数据库设计

### diary 表
```sql
CREATE TABLE diary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200),
    content TEXT NOT NULL,
    mood VARCHAR(50) COMMENT '心情：开心、难过、平静等',
    weather VARCHAR(50) COMMENT '天气：晴天、雨天、阴天等',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at)
);
```

---

## 后端 API 设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/diaries | 获取日记列表（分页） |
| GET | /api/diaries/{id} | 获取单篇日记详情 |
| POST | /api/diaries | 创建新日记 |
| PUT | /api/diaries/{id} | 更新日记 |
| DELETE | /api/diaries/{id} | 删除日记 |
| GET | /api/diaries/search | 按关键词搜索日记 |
| GET | /api/diaries/export | 导出所有日记为 JSON |

---

## 前端页面设计

### 1. 日记列表页 (DiaryList.vue)
- 按时间倒序展示日记列表
- 每条显示：日期、标题（可选）、内容预览、心情图标
- 顶部有搜索框和导出按钮
- 底部有新增按钮（FAB）
- 下拉刷新、上拉加载更多

### 2. 日记编辑页 (DiaryEdit.vue)
- 标题输入（可选）
- 内容输入（textarea，支持多行）
- 心情选择（emoji）
- 天气选择（可选）
- 保存按钮

### 3. 日记详情页 (DiaryDetail.vue)
- 显示完整日记内容
- 编辑、删除按钮

---

## 实现步骤

### Phase 1: 后端项目初始化
1. 创建 Spring Boot 项目结构
2. 配置 pom.xml 依赖
3. 配置 application.yml（数据库连接、JPA）
4. 创建实体类 Diary
5. 创建 Repository、Service、Controller
6. 配置 CORS 允许前端跨域

### Phase 2: 后端功能实现
1. 实现基础 CRUD 接口
2. 实现分页查询
3. 实现搜索功能
4. 实现 JSON 导出功能
5. 添加异常处理

### Phase 3: 前端项目初始化
1. 创建 Vue 3 + Vite 项目
2. 配置 TypeScript
3. 配置路由（Vue Router）
4. 配置状态管理（Pinia）
5. 配置 Axios 封装 API 请求

### Phase 4: 前端页面实现
1. 实现日记列表页（响应式设计）
2. 实现日记编辑页
3. 实现日记详情页
4. 实现搜索功能
5. 实现导出功能

### Phase 5: 样式优化
1. 移动端适配（viewport、触摸优化）
2. 简洁大方的配色方案
3. 流畅的过渡动画
4. 加载状态提示

---

## 关键文件清单

### 后端
- `backend/src/main/resources/application.yml` - 数据库配置
- `backend/src/main/java/com/journal/entity/Diary.java` - 日记实体
- `backend/src/main/java/com/journal/controller/DiaryController.java` - REST API
- `backend/pom.xml` - Maven 依赖

### 前端
- `frontend/src/api/diary.ts` - API 请求封装
- `frontend/src/views/DiaryList.vue` - 列表页
- `frontend/src/views/DiaryEdit.vue` - 编辑页
- `frontend/src/views/DiaryDetail.vue` - 详情页
- `frontend/package.json` - NPM 依赖

---

## 验证步骤

1. 启动 MySQL 数据库，创建 journal 数据库
2. 启动后端服务，确认表自动创建
3. 启动前端开发服务器
4. 测试功能：
   - 创建新日记
   - 查看日记列表
   - 编辑日记
   - 删除日记
   - 搜索日记
   - 导出 JSON 备份
5. 在手机浏览器中测试响应式效果

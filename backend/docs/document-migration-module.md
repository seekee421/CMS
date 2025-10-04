# 文档迁移功能模块技术文档

## 1. 概述

### 1.1 功能目标
实现从ECO达梦在线文档（https://eco.dameng.com）到CMS系统的自动化文档迁移功能，支持批量导入、内容解析、分类管理和质量控制。

### 1.2 核心特性
- 自动化网页爬虫和内容抓取
- 智能内容解析和格式转换
- 分类标签自动识别
- 迁移进度跟踪和日志记录
- 手动审核和优化工具
- 批量操作和回滚机制

## 2. 系统架构

### 2.1 模块组成
```
文档迁移模块
├── 爬虫引擎 (Crawler Engine)
│   ├── 网页抓取器 (Web Scraper)
│   ├── 内容解析器 (Content Parser)
│   └── 链接发现器 (Link Discoverer)
├── 数据处理器 (Data Processor)
│   ├── 内容清洗器 (Content Cleaner)
│   ├── 格式转换器 (Format Converter)
│   └── 分类识别器 (Category Classifier)
├── 迁移管理器 (Migration Manager)
│   ├── 任务调度器 (Task Scheduler)
│   ├── 进度跟踪器 (Progress Tracker)
│   └── 质量控制器 (Quality Controller)
└── 用户界面 (User Interface)
    ├── 迁移配置面板
    ├── 进度监控面板
    └── 内容审核面板
```

### 2.2 技术栈
- **后端**: Spring Boot, Spring Data JPA, Jsoup, Selenium
- **数据库**: MySQL (扩展现有表结构)
- **队列**: Redis (任务队列和缓存)
- **前端**: React (管理界面)
- **工具**: Maven, podman

## 3. 数据库设计

### 3.1 扩展现有表结构

#### 3.1.1 扩展 documents 表
```sql
ALTER TABLE documents ADD COLUMN source_url VARCHAR(500) COMMENT '原始文档URL';
ALTER TABLE documents ADD COLUMN category VARCHAR(100) COMMENT '文档分类';
ALTER TABLE documents ADD COLUMN tags JSON COMMENT '文档标签';
ALTER TABLE documents ADD COLUMN version VARCHAR(50) COMMENT '文档版本';
ALTER TABLE documents ADD COLUMN original_id VARCHAR(100) COMMENT '原始文档ID';
ALTER TABLE documents ADD COLUMN migration_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REVIEWED') DEFAULT 'PENDING' COMMENT '迁移状态';
ALTER TABLE documents ADD COLUMN migration_date TIMESTAMP COMMENT '迁移时间';
```

#### 3.1.2 新增 document_categories 表
```sql
CREATE TABLE document_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES document_categories(id)
);
```

#### 3.1.3 新增 migration_tasks 表
```sql
CREATE TABLE migration_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    source_url VARCHAR(500) NOT NULL COMMENT '源URL',
    task_type ENUM('SINGLE_PAGE', 'SITE_CRAWL', 'CATEGORY_CRAWL') NOT NULL COMMENT '任务类型',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'PAUSED') DEFAULT 'PENDING' COMMENT '任务状态',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    total_pages INT DEFAULT 0 COMMENT '总页面数',
    processed_pages INT DEFAULT 0 COMMENT '已处理页面数',
    success_pages INT DEFAULT 0 COMMENT '成功页面数',
    failed_pages INT DEFAULT 0 COMMENT '失败页面数',
    config JSON COMMENT '任务配置',
    error_message TEXT COMMENT '错误信息',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    started_at TIMESTAMP COMMENT '开始时间',
    completed_at TIMESTAMP COMMENT '完成时间',
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### 3.1.4 新增 migration_logs 表
```sql
CREATE TABLE migration_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    document_id BIGINT COMMENT '文档ID',
    source_url VARCHAR(500) NOT NULL COMMENT '源URL',
    action ENUM('FETCH', 'PARSE', 'SAVE', 'ERROR') NOT NULL COMMENT '操作类型',
    status ENUM('SUCCESS', 'FAILED', 'WARNING') NOT NULL COMMENT '状态',
    message TEXT COMMENT '日志信息',
    details JSON COMMENT '详细信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES migration_tasks(id),
    FOREIGN KEY (document_id) REFERENCES documents(id)
);
```

## 4. 核心组件设计

### 4.1 爬虫引擎

#### 4.1.1 网页抓取器 (WebScraper)
```java
@Component
public class WebScraper {
    // 使用Jsoup进行静态内容抓取
    // 使用Selenium处理JavaScript渲染页面
    // 支持代理、延迟、重试机制
    // 遵守robots.txt和爬虫礼仪
}
```

#### 4.1.2 内容解析器 (ContentParser)
```java
@Component
public class ContentParser {
    // HTML到Markdown转换
    // 元数据提取（标题、作者、时间等）
    // 图片和附件处理
    // 链接关系分析
}
```

### 4.2 数据处理器

#### 4.2.1 内容清洗器 (ContentCleaner)
```java
@Component
public class ContentCleaner {
    // 移除广告和导航元素
    // 格式化文本内容
    // 处理特殊字符和编码
    // 内容去重检测
}
```

#### 4.2.2 分类识别器 (CategoryClassifier)
```java
@Component
public class CategoryClassifier {
    // 基于URL路径的分类
    // 基于内容关键词的分类
    // 基于页面结构的分类
    // 手动分类规则配置
}
```

### 4.3 迁移管理器

#### 4.3.1 任务调度器 (TaskScheduler)
```java
@Service
public class MigrationTaskScheduler {
    // 任务队列管理
    // 并发控制和限流
    // 任务优先级处理
    // 失败重试机制
}
```

## 5. API 设计

### 5.1 迁移任务管理 API

#### 5.1.1 创建迁移任务
```
POST /api/migration/tasks
Content-Type: application/json

{
    "taskName": "ECO文档迁移",
    "sourceUrl": "https://eco.dameng.com",
    "taskType": "SITE_CRAWL",
    "config": {
        "maxDepth": 3,
        "includePatterns": ["*/docs/*", "*/manual/*"],
        "excludePatterns": ["*/download/*"],
        "delayMs": 1000,
        "maxConcurrency": 3
    }
}
```

#### 5.1.2 查询任务列表
```
GET /api/migration/tasks?page=0&size=10&status=RUNNING
```

#### 5.1.3 查询任务详情
```
GET /api/migration/tasks/{taskId}
```

#### 5.1.4 控制任务执行
```
POST /api/migration/tasks/{taskId}/start
POST /api/migration/tasks/{taskId}/pause
POST /api/migration/tasks/{taskId}/stop
```

### 5.2 文档管理 API

#### 5.2.1 查询迁移文档
```
GET /api/migration/documents?category=manual&status=COMPLETED&page=0&size=20
```

#### 5.2.2 批量审核文档
```
POST /api/migration/documents/batch-review
Content-Type: application/json

{
    "documentIds": [1, 2, 3],
    "action": "APPROVE",
    "comment": "内容质量良好，批准发布"
}
```

### 5.3 分类管理 API

#### 5.3.1 查询分类树
```
GET /api/migration/categories/tree
```

#### 5.3.2 创建分类
```
POST /api/migration/categories
Content-Type: application/json

{
    "name": "产品手册",
    "description": "DM8产品相关手册",
    "parentId": null,
    "sortOrder": 1
}
```

## 6. 实现计划

### 6.1 第一阶段：基础架构 (1-2天)
1. 扩展数据库表结构
2. 创建基础实体类和Repository
3. 搭建基本的Service层架构
4. 配置任务队列和缓存

### 6.2 第二阶段：爬虫引擎 (2-3天)
1. 实现WebScraper组件
2. 实现ContentParser组件
3. 实现LinkDiscoverer组件
4. 添加错误处理和重试机制

### 6.3 第三阶段：数据处理 (1-2天)
1. 实现ContentCleaner组件
2. 实现CategoryClassifier组件
3. 实现格式转换器
4. 添加内容质量检测

### 6.4 第四阶段：任务管理 (2天)
1. 实现TaskScheduler
2. 实现ProgressTracker
3. 实现任务控制API
4. 添加日志记录功能

### 6.5 第五阶段：用户界面 (2-3天)
1. 创建任务管理界面
2. 创建进度监控界面
3. 创建内容审核界面
4. 添加批量操作功能

### 6.6 第六阶段：测试和优化 (1-2天)
1. 单元测试和集成测试
2. 性能测试和优化
3. 错误处理测试
4. 用户体验优化

## 7. 风险控制

### 7.1 技术风险
- **反爬虫机制**: 实现智能延迟、代理轮换、User-Agent轮换
- **内容解析失败**: 多种解析策略，人工审核机制
- **性能瓶颈**: 异步处理、队列管理、资源限制

### 7.2 数据质量
- **内容完整性检查**: 字数统计、图片检测、链接验证
- **重复内容检测**: 基于内容哈希的去重机制
- **格式一致性**: 统一的Markdown格式标准

### 7.3 法律合规
- **版权保护**: 保留原始来源信息，添加版权声明
- **爬虫礼仪**: 遵守robots.txt，合理控制访问频率
- **数据安全**: 敏感信息过滤，访问权限控制

## 8. 监控和维护

### 8.1 监控指标
- 任务执行成功率
- 内容抓取速度
- 错误率和错误类型
- 系统资源使用情况

### 8.2 日志管理
- 详细的操作日志
- 错误日志和堆栈跟踪
- 性能日志和统计
- 用户操作审计日志

### 8.3 维护策略
- 定期清理临时数据
- 更新爬虫规则和解析器
- 监控目标网站结构变化
- 优化性能和资源使用

## 9. 扩展性设计

### 9.1 插件化架构
- 可插拔的内容解析器
- 可配置的分类规则
- 可扩展的数据源支持

### 9.2 多站点支持
- 通用的爬虫框架
- 站点特定的配置文件
- 统一的内容格式标准

### 9.3 API集成
- 支持第三方API数据源
- 标准化的数据交换格式
- 灵活的认证和授权机制
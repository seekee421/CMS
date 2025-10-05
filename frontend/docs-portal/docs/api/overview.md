# API 接口文档

本文档详细介绍CMS系统的REST API接口规范和使用方法。

## API 概述

### 基本信息

- **API版本**: v1.0
- **基础URL**: `https://api.cms.example.com/api/v1`
- **协议**: HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8

### 认证方式

CMS API使用JWT（JSON Web Token）进行身份认证：

```http
Authorization: Bearer <your-jwt-token>
```

### 请求格式

所有API请求都应该包含以下头部信息：

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <token>
```

### 响应格式

API统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### 状态码说明

| 状态码 | 说明 | 描述 |
|--------|------|------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未授权访问 |
| 403 | Forbidden | 权限不足 |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器内部错误 |

## 认证接口

### 用户登录

获取访问令牌。

```http
POST /auth/login
```

**请求参数**:

```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ADMIN"]
    }
  }
}
```

### 刷新令牌

使用刷新令牌获取新的访问令牌。

```http
POST /auth/refresh
```

**请求参数**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 用户登出

注销当前用户会话。

```http
POST /auth/logout
```

## 文档管理接口

### 获取文档列表

获取文档列表，支持分页和筛选。

```http
GET /documents
```

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码，默认1 |
| size | integer | 否 | 每页数量，默认10 |
| categoryId | integer | 否 | 分类ID |
| status | string | 否 | 状态筛选 |
| keyword | string | 否 | 关键词搜索 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "快速开始指南",
        "summary": "CMS系统快速开始指南",
        "content": "# 快速开始\n\n欢迎使用CMS系统...",
        "categoryId": 1,
        "categoryName": "用户指南",
        "status": "PUBLISHED",
        "author": "admin",
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z",
        "viewCount": 100,
        "downloadCount": 50
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
}
```

### 获取文档详情

根据ID获取文档详细信息。

```http
GET /documents/{id}
```

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 文档ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "快速开始指南",
    "summary": "CMS系统快速开始指南",
    "content": "# 快速开始\n\n欢迎使用CMS系统...",
    "categoryId": 1,
    "categoryName": "用户指南",
    "status": "PUBLISHED",
    "author": "admin",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "viewCount": 100,
    "downloadCount": 50,
    "tags": ["指南", "入门"],
    "attachments": [
      {
        "id": 1,
        "name": "示例文件.pdf",
        "url": "/files/example.pdf",
        "size": 1024000
      }
    ]
  }
}
```

### 创建文档

创建新的文档。

```http
POST /documents
```

**请求参数**:

```json
{
  "title": "新文档标题",
  "summary": "文档摘要",
  "content": "# 文档内容\n\n这是文档内容...",
  "categoryId": 1,
  "status": "DRAFT",
  "tags": ["标签1", "标签2"]
}
```

**响应示例**:

```json
{
  "code": 201,
  "message": "文档创建成功",
  "data": {
    "id": 123,
    "title": "新文档标题",
    "status": "DRAFT",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### 更新文档

更新现有文档。

```http
PUT /documents/{id}
```

**请求参数**:

```json
{
  "title": "更新后的标题",
  "summary": "更新后的摘要",
  "content": "# 更新后的内容\n\n...",
  "categoryId": 1,
  "status": "PUBLISHED",
  "tags": ["新标签"]
}
```

### 删除文档

删除指定文档。

```http
DELETE /documents/{id}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "文档删除成功"
}
```

## 分类管理接口

### 获取分类树

获取完整的分类树结构。

```http
GET /categories/tree
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "用户指南",
      "description": "用户使用指南",
      "parentId": null,
      "sort": 1,
      "children": [
        {
          "id": 2,
          "name": "快速开始",
          "description": "快速开始指南",
          "parentId": 1,
          "sort": 1,
          "children": []
        }
      ]
    }
  ]
}
```

### 创建分类

创建新的文档分类。

```http
POST /categories
```

**请求参数**:

```json
{
  "name": "新分类",
  "description": "分类描述",
  "parentId": 1,
  "sort": 1
}
```

## 用户管理接口

### 获取用户列表

获取系统用户列表。

```http
GET /users
```

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页数量 |
| keyword | string | 否 | 搜索关键词 |
| status | string | 否 | 用户状态 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "realName": "管理员",
        "status": "ACTIVE",
        "roles": ["ADMIN"],
        "createdAt": "2024-01-15T10:30:00Z",
        "lastLoginAt": "2024-01-15T10:30:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0
  }
}
```

### 创建用户

创建新用户账户。

```http
POST /users
```

**请求参数**:

```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "realName": "新用户",
  "password": "password123",
  "roles": ["EDITOR"]
}
```

## 搜索接口

### 全文搜索

在文档中进行全文搜索。

```http
GET /search
```

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 搜索关键词 |
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页数量 |
| categoryId | integer | 否 | 分类筛选 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "query": "快速开始",
    "total": 10,
    "results": [
      {
        "id": 1,
        "title": "快速开始指南",
        "summary": "CMS系统快速开始指南",
        "categoryName": "用户指南",
        "highlights": [
          "这是一个<em>快速开始</em>的指南",
          "帮助用户<em>快速</em>上手系统"
        ],
        "score": 0.95
      }
    ]
  }
}
```

## 文件管理接口

### 文件上传

上传文件到系统。

```http
POST /files/upload
```

**请求格式**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件 |
| category | string | 否 | 文件分类 |

**响应示例**:

```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "id": 123,
    "filename": "document.pdf",
    "originalName": "文档.pdf",
    "size": 1024000,
    "mimeType": "application/pdf",
    "url": "/files/123/document.pdf",
    "uploadedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 文件下载

下载指定文件。

```http
GET /files/{id}/download
```

## 统计接口

### 获取系统统计

获取系统整体统计信息。

```http
GET /statistics/overview
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDocuments": 1000,
    "totalUsers": 50,
    "totalCategories": 20,
    "todayViews": 500,
    "todayDownloads": 100,
    "recentDocuments": [
      {
        "id": 1,
        "title": "最新文档",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ]
  }
}
```

### 获取文档统计

获取文档访问统计数据。

```http
GET /statistics/documents
```

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |
| documentId | integer | 否 | 文档ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-15"
    },
    "totalViews": 10000,
    "totalDownloads": 2000,
    "dailyStats": [
      {
        "date": "2024-01-15",
        "views": 500,
        "downloads": 100
      }
    ],
    "topDocuments": [
      {
        "id": 1,
        "title": "热门文档",
        "views": 1000,
        "downloads": 200
      }
    ]
  }
}
```

## 反馈接口

### 提交反馈

提交文档反馈信息。

```http
POST /feedback
```

**请求参数**:

```json
{
  "documentId": 1,
  "feedbackType": "CONTENT_INCORRECT",
  "description": "文档内容有误，需要更新",
  "contactInfo": "user@example.com"
}
```

**响应示例**:

```json
{
  "code": 201,
  "message": "反馈提交成功",
  "data": {
    "id": 123,
    "status": "PENDING",
    "submittedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 获取反馈列表

获取反馈列表（管理员权限）。

```http
GET /feedback
```

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页数量 |
| status | string | 否 | 反馈状态 |
| documentId | integer | 否 | 文档ID |

## 错误处理

### 错误响应格式

当API请求出现错误时，会返回以下格式的错误信息：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": [
    {
      "field": "title",
      "message": "标题不能为空"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1001 | 用户名或密码错误 | 检查登录凭据 |
| 1002 | 令牌已过期 | 使用刷新令牌获取新令牌 |
| 1003 | 权限不足 | 联系管理员分配权限 |
| 2001 | 文档不存在 | 检查文档ID是否正确 |
| 2002 | 分类不存在 | 检查分类ID是否正确 |
| 3001 | 文件上传失败 | 检查文件格式和大小 |
| 3002 | 文件不存在 | 检查文件ID是否正确 |

## 限流和配额

### 请求限制

为了保护API服务，系统实施以下限制：

- **请求频率**: 每分钟最多100次请求
- **并发连接**: 每个IP最多10个并发连接
- **文件上传**: 单个文件最大20MB
- **批量操作**: 单次最多处理100条记录

### 限流响应

当触发限流时，API会返回429状态码：

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "retryAfter": 60
}
```

## SDK和示例

### JavaScript SDK

```javascript
// 安装
npm install cms-api-sdk

// 使用示例
import CmsApi from 'cms-api-sdk';

const api = new CmsApi({
  baseURL: 'https://api.cms.example.com/api/v1',
  token: 'your-jwt-token'
});

// 获取文档列表
const documents = await api.documents.list({
  page: 1,
  size: 10
});

// 创建文档
const newDoc = await api.documents.create({
  title: '新文档',
  content: '文档内容',
  categoryId: 1
});
```

### Python SDK

```python
# 安装
pip install cms-api-client

# 使用示例
from cms_api import CmsClient

client = CmsClient(
    base_url='https://api.cms.example.com/api/v1',
    token='your-jwt-token'
)

# 获取文档列表
documents = client.documents.list(page=1, size=10)

# 创建文档
new_doc = client.documents.create({
    'title': '新文档',
    'content': '文档内容',
    'categoryId': 1
})
```

## 版本更新

### 版本兼容性

- **v1.0**: 当前版本
- **向后兼容**: 保证向后兼容性
- **废弃通知**: 提前3个月通知API废弃

### 更新日志

#### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持文档、用户、分类管理
- 支持全文搜索和统计功能

## 支持和联系

如果您在使用API过程中遇到问题，可以通过以下方式获取帮助：

- **技术文档**: [https://docs.cms.example.com](https://docs.cms.example.com)
- **问题反馈**: [support@cms.example.com](mailto:support@cms.example.com)
- **开发者社区**: [https://community.cms.example.com](https://community.cms.example.com)
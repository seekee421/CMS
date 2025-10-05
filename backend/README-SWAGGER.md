# CMS 权限管理系统 Swagger API 文档指南

## 📋 概述

本系统集成了完整的Swagger API文档功能，为开发者和API用户提供详细的接口说明和在线测试能力。

## 🚀 访问Swagger UI

启动应用后，可以通过以下URL访问Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

或者使用新的UI路径：

```
http://localhost:8080/swagger-ui/index.html
```

## 📡 API文档端点

系统提供以下API文档端点：

1. **Swagger UI界面**: `http://localhost:8080/swagger-ui.html`
2. **OpenAPI JSON格式**: `http://localhost:8080/v3/api-docs`
3. **OpenAPI YAML格式**: `http://localhost:8080/v3/api-docs.yaml`

## 🔐 认证说明

大部分API端点都需要JWT认证：

1. 首先通过认证接口获取JWT令牌：
   ```
   POST /api/auth/login
   {
     "username": "your_username",
     "password": "your_password"
   }
   ```

2. 在Swagger UI右上角点击"Authorize"按钮
3. 输入格式为：`Bearer your_jwt_token_here`
4. 点击"Authorize"完成认证

## 📚 API模块分类

Swagger UI将API按功能模块进行分类：

- **权限管理**: 权限相关的CRUD操作
- **角色管理**: 角色相关的CRUD操作和权限分配
- **用户管理**: 用户相关的CRUD操作和角色分配
- **缓存管理**: 缓存监控、性能分析和管理操作
- **审计日志**: 系统操作审计和日志查询
- **文档备份**: 文档备份和恢复功能
- **文档迁移**: 文档导入和迁移功能

## 🧪 在线测试

Swagger UI支持直接在线测试API：

1. 展开任意API端点
2. 点击"Try it out"按钮
3. 填入所需参数
4. 点击"Execute"执行请求
5. 查看响应结果和状态码

## 🛠️ 技术实现

### 依赖组件
- **Springdoc OpenAPI**: `springdoc-openapi-starter-webmvc-ui` v2.6.0
- **Spring Boot**: 3.5.6
- **Swagger UI**: 集成的Web界面

### 配置文件
- `SwaggerConfig.java`: 主要配置类
- `application.properties`: 相关配置参数

### 安全集成
- JWT Bearer Token认证
- 基于角色的访问控制
- 自动化的权限验证

## 🎯 最佳实践

### 开发阶段
1. 使用Swagger UI验证API接口
2. 在线测试各种边界条件
3. 验证请求/响应数据格式

### 生产环境
1. 确保API文档的安全性
2. 定期更新文档以匹配代码变更
3. 使用API文档进行团队协作

## 📈 性能监控

系统还集成了API性能监控：

- **健康检查**: `GET /actuator/health`
- **指标监控**: `GET /actuator/metrics`
- **缓存监控**: `GET /actuator/cache`

## 🆘 故障排除

### 常见问题

1. **无法访问Swagger UI**
   - 确保应用已成功启动
   - 检查端口是否正确（默认8080）
   - 确认没有防火墙阻止访问

2. **认证失败**
   - 确保使用了正确的JWT令牌格式
   - 检查令牌是否已过期
   - 验证用户是否具有相应权限

3. **API调用失败**
   - 检查请求参数是否符合要求
   - 确认用户具有相应权限
   - 查看响应错误信息

## 📞 支持

如遇到问题，请联系开发团队或查看相关文档。
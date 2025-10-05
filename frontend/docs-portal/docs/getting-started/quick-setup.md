# 快速配置

本指南将帮助您在几分钟内完成CMS系统的基本配置。

## 前提条件

在开始之前，请确保您已经完成了[安装步骤](./installation.md)。

## 基本配置步骤

### 1. 数据库配置

编辑 `application.properties` 文件：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/cms_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. 管理员账户设置

首次启动系统后，使用以下默认管理员账户登录：

- **用户名**: admin
- **密码**: admin123

⚠️ **重要**: 请在首次登录后立即修改默认密码。

### 3. 基本系统设置

1. 登录管理后台
2. 进入 **系统设置** → **基本配置**
3. 配置以下基本信息：
   - 系统名称
   - 系统描述
   - 联系邮箱
   - 时区设置

### 4. 创建第一个文档分类

1. 进入 **内容管理** → **分类管理**
2. 点击 **新建分类**
3. 填写分类信息：
   - 分类名称
   - 分类描述
   - 排序权重

### 5. 发布第一篇文档

1. 进入 **内容管理** → **文档管理**
2. 点击 **新建文档**
3. 填写文档信息并发布

## 验证配置

完成上述步骤后，您可以：

1. 访问前台文档页面验证显示效果
2. 测试搜索功能
3. 检查用户权限设置

## 下一步

- [用户管理指南](../user-guide/user-management.md)
- [内容管理指南](../user-guide/content-management.md)
- [权限配置指南](../user-guide/permissions.md)

## 常见问题

### Q: 忘记管理员密码怎么办？

A: 可以通过以下方式重置：
1. 直接修改数据库中的用户密码
2. 使用密码重置功能（如果已配置邮箱）

### Q: 如何修改系统端口？

A: 在 `application.properties` 中添加：
```properties
server.port=8080
```

### Q: 如何启用HTTPS？

A: 配置SSL证书：
```properties
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password
server.ssl.key-store-type=PKCS12
```
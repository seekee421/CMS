#!/bin/bash

# 数据库验证脚本
# 验证本地MySQL数据库的表结构和数据

echo "=== CMS数据库验证报告 ==="
echo "时间: $(date)"
echo "数据库: cms_db"
echo ""

# 数据库连接参数
DB_USER="root"
DB_PASS="yf421421"
DB_NAME="cms_db"

# 验证数据库连接
echo "1. 验证数据库连接..."
mysql -u $DB_USER -p$DB_PASS -e "SELECT 'MySQL连接成功' as status;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 数据库连接成功"
else
    echo "❌ 数据库连接失败"
    exit 1
fi
echo ""

# 检查表结构
echo "2. 检查表结构..."
echo "数据库中的表:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null
echo ""

# 验证关键表的结构
echo "3. 验证关键表结构..."

echo "3.1 document_category表结构:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; DESCRIBE document_category;" 2>/dev/null
echo ""

echo "3.2 document表结构:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; DESCRIBE document;" 2>/dev/null
echo ""

echo "3.3 migration_log表结构:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; DESCRIBE migration_log;" 2>/dev/null
echo ""

echo "3.4 media_resources表结构:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; DESCRIBE media_resources;" 2>/dev/null
echo ""

# 检查数据统计
echo "4. 数据统计..."
echo "4.1 各表数据量:"
mysql -u $DB_USER -p$DB_PASS -e "
USE $DB_NAME;
SELECT 'document_category' as table_name, COUNT(*) as count FROM document_category
UNION ALL
SELECT 'document' as table_name, COUNT(*) as count FROM document
UNION ALL
SELECT 'migration_log' as table_name, COUNT(*) as count FROM migration_log
UNION ALL
SELECT 'media_resources' as table_name, COUNT(*) as count FROM media_resources
UNION ALL
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'role' as table_name, COUNT(*) as count FROM role
UNION ALL
SELECT 'permission' as table_name, COUNT(*) as count FROM permission;
" 2>/dev/null
echo ""

# 检查示例数据
echo "5. 示例数据检查..."
echo "5.1 文档分类示例数据:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SELECT id, name, description, parent_id FROM document_category LIMIT 5;" 2>/dev/null
echo ""

echo "5.2 文档示例数据:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SELECT id, title, category_id, created_at FROM document LIMIT 5;" 2>/dev/null
echo ""

echo "5.3 迁移日志示例数据:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SELECT id, document_id, source_category_id, target_category_id, status, created_at FROM migration_log LIMIT 5;" 2>/dev/null
echo ""

# 检查索引
echo "6. 索引检查..."
echo "6.1 document表索引:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SHOW INDEX FROM document;" 2>/dev/null
echo ""

echo "6.2 document_category表索引:"
mysql -u $DB_USER -p$DB_PASS -e "USE $DB_NAME; SHOW INDEX FROM document_category;" 2>/dev/null
echo ""

echo "=== 数据库验证完成 ==="
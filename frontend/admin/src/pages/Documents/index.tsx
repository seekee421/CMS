import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Modal,
  Form,
  Upload,
  message,
  Popconfirm,
  Drawer,
  Typography,
  Row,
  Col,
  DatePicker,
  Tooltip,
  Badge,
  Dropdown,
  Menu,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  DownloadOutlined,
  UploadOutlined,
  FilterOutlined,
  ExportOutlined,
  ImportOutlined,
  MoreOutlined,
  FileTextOutlined,
  FolderOutlined,
  UserOutlined,
  CalendarOutlined,
  TagOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { UploadProps } from 'antd/es/upload';
import './index.less';

const { Search } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;
const { Title, Text } = Typography;

interface Document {
  id: string;
  title: string;
  content: string;
  summary: string;
  categoryId: string;
  categoryName: string;
  status: 'draft' | 'published' | 'archived';
  author: string;
  authorId: string;
  tags: string[];
  viewCount: number;
  downloadCount: number;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
  version: string;
  fileSize: number;
  fileType: string;
}

interface DocumentFilter {
  keyword?: string;
  categoryId?: string;
  status?: string;
  authorId?: string;
  tags?: string[];
  dateRange?: [string, string];
}

const Documents: React.FC = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [filters, setFilters] = useState<DocumentFilter>({});
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  // 受控排序状态（表头箭头显示）
  const [sortInfo, setSortInfo] = useState<{ columnKey: string; order: 'ascend' | 'descend' | null } | null>(null);

  // 读取查询参数以确定默认排序
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const sortParam = searchParams.get('sort');
  const isPopularSort = sortParam === 'popular';
  const isDownloadsSort = sortParam === 'downloads';

  // 根据查询参数初始化受控排序箭头状态
  useEffect(() => {
    if (isPopularSort || isDownloadsSort) {
      setSortInfo({ columnKey: 'stats', order: 'descend' });
    } else {
      setSortInfo(null);
    }
  }, [isPopularSort, isDownloadsSort]);

  // 模态框状态
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [previewDrawerVisible, setPreviewDrawerVisible] = useState(false);
  const [importModalVisible, setImportModalVisible] = useState(false);
  const [currentDocument, setCurrentDocument] = useState<Document | null>(null);

  // 表单实例
  const [form] = Form.useForm();

  // 模拟数据
  const mockDocuments: Document[] = [
    {
      id: '1',
      title: 'DM8数据库安装指南',
      content: '# DM8数据库安装指南\n\n本文档详细介绍了DM8数据库的安装步骤...',
      summary: '详细介绍DM8数据库的安装步骤和配置方法',
      categoryId: '1',
      categoryName: '安装部署',
      status: 'published',
      author: '张三',
      authorId: '1',
      tags: ['安装', '配置', 'DM8'],
      viewCount: 2345,
      downloadCount: 567,
      createdAt: '2024-01-15 10:30:00',
      updatedAt: '2024-01-16 14:20:00',
      publishedAt: '2024-01-16 14:20:00',
      version: '1.2',
      fileSize: 2048,
      fileType: 'markdown',
    },
    {
      id: '2',
      title: 'SQL语法参考手册',
      content: '# SQL语法参考手册\n\n本手册包含了DM8支持的所有SQL语法...',
      summary: 'DM8数据库SQL语法的完整参考手册',
      categoryId: '2',
      categoryName: '开发指南',
      status: 'published',
      author: '李四',
      authorId: '2',
      tags: ['SQL', '语法', '参考'],
      viewCount: 1876,
      downloadCount: 432,
      createdAt: '2024-01-14 09:15:00',
      updatedAt: '2024-01-15 16:45:00',
      publishedAt: '2024-01-15 16:45:00',
      version: '2.1',
      fileSize: 5120,
      fileType: 'markdown',
    },
    {
      id: '3',
      title: '性能优化最佳实践',
      content: '# 性能优化最佳实践\n\n本文档总结了数据库性能优化的最佳实践...',
      summary: '数据库性能优化的实用技巧和最佳实践',
      categoryId: '3',
      categoryName: '性能调优',
      status: 'draft',
      author: '王五',
      authorId: '3',
      tags: ['性能', '优化', '调优'],
      viewCount: 1654,
      downloadCount: 398,
      createdAt: '2024-01-13 11:20:00',
      updatedAt: '2024-01-14 13:30:00',
      version: '1.0',
      fileSize: 3072,
      fileType: 'markdown',
    },
  ];

  const categories = [
    { id: '1', name: '安装部署' },
    { id: '2', name: '开发指南' },
    { id: '3', name: '性能调优' },
    { id: '4', name: '运维管理' },
    { id: '5', name: '故障排查' },
  ];

  const authors = [
    { id: '1', name: '张三' },
    { id: '2', name: '李四' },
    { id: '3', name: '王五' },
    { id: '4', name: '赵六' },
  ];

  // 获取文档列表
  const fetchDocuments = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const data = [...mockDocuments];
      if (isPopularSort) {
        data.sort((a, b) => b.viewCount - a.viewCount);
      } else if (isDownloadsSort) {
        data.sort((a, b) => b.downloadCount - a.downloadCount);
      }
      setDocuments(data);
      setPagination(prev => ({ ...prev, total: data.length }));
    } catch (error) {
      message.error('获取文档列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 处理搜索
  const handleSearch = (value: string) => {
    setFilters(prev => ({ ...prev, keyword: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  // 处理筛选
  const handleFilter = (key: string, value: any) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  // 处理表格变化
  const handleTableChange = (paginationConfig: any, filters: any, sorter: any) => {
    setPagination(paginationConfig);
    if (sorter && sorter.columnKey) {
      setSortInfo({ columnKey: sorter.columnKey, order: sorter.order });
    }
  };

  // 处理行选择
  const handleRowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => {
      setSelectedRowKeys(keys);
    },
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const statusMap = {
      draft: { color: 'orange', text: '草稿' },
      published: { color: 'green', text: '已发布' },
      archived: { color: 'gray', text: '已归档' },
    };
    const config = statusMap[status as keyof typeof statusMap];
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // 处理编辑
  const handleEdit = (record: Document) => {
    setCurrentDocument(record);
    form.setFieldsValue({
      title: record.title,
      summary: record.summary,
      categoryId: record.categoryId,
      tags: record.tags,
      status: record.status,
    });
    setEditModalVisible(true);
  };

  // 处理预览
  const handlePreview = (record: Document) => {
    setCurrentDocument(record);
    setPreviewDrawerVisible(true);
  };

  // 处理删除
  const handleDelete = async (id: string) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      message.success('删除成功');
      fetchDocuments();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 处理批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的文档');
      return;
    }

    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success(`成功删除 ${selectedRowKeys.length} 个文档`);
      setSelectedRowKeys([]);
      fetchDocuments();
    } catch (error) {
      message.error('批量删除失败');
    }
  };

  // 处理批量发布
  const handleBatchPublish = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的文档');
      return;
    }

    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success(`成功发布 ${selectedRowKeys.length} 个文档`);
      setSelectedRowKeys([]);
      fetchDocuments();
    } catch (error) {
      message.error('批量发布失败');
    }
  };

  // 处理导出
  const handleExport = () => {
    message.info('导出功能开发中...');
  };

  // 处理导入
  const handleImport = () => {
    setImportModalVisible(true);
  };

  // 上传配置
  const uploadProps: UploadProps = {
    name: 'file',
    multiple: true,
    action: '/api/documents/import',
    onChange(info) {
      const { status } = info.file;
      if (status === 'done') {
        message.success(`${info.file.name} 导入成功`);
        setImportModalVisible(false);
        fetchDocuments();
      } else if (status === 'error') {
        message.error(`${info.file.name} 导入失败`);
      }
    },
  };

  // 更多操作菜单
  const getMoreMenu = (record: Document) => (
    <Menu>
      <Menu.Item key="duplicate" icon={<FileTextOutlined />}>
        复制文档
      </Menu.Item>
      <Menu.Item key="move" icon={<FolderOutlined />}>
        移动到分类
      </Menu.Item>
      <Menu.Item key="history" icon={<CalendarOutlined />}>
        版本历史
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="archive" icon={<TagOutlined />}>
        归档文档
      </Menu.Item>
    </Menu>
  );

  // 表格列配置
  const columns: ColumnsType<Document> = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      width: 300,
      ellipsis: true,
      render: (text: string, record: Document) => (
        <Space direction="vertical" size={0}>
          <Button
            type="link"
            style={{ padding: 0, height: 'auto', fontWeight: 500 }}
            onClick={() => handlePreview(record)}
          >
            {text}
          </Button>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {record.summary}
          </Text>
        </Space>
      ),
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 120,
      render: (text: string) => (
        <Tag color="blue" icon={<FolderOutlined />}>
          {text}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
      width: 100,
      render: (text: string) => (
        <Space>
          <UserOutlined />
          {text}
        </Space>
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      width: 200,
      render: (tags: string[]) => (
        <Space wrap>
          {tags.map(tag => (
            <Tag key={tag}>
              {tag}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: '统计',
      key: 'stats',
      width: 120,
      sorter: (a: Document, b: Document) => (isDownloadsSort ? a.downloadCount - b.downloadCount : a.viewCount - b.viewCount),
      sortDirections: ['descend', 'ascend'],
      defaultSortOrder: isPopularSort || isDownloadsSort ? 'descend' : undefined,
      sortOrder: sortInfo?.columnKey === 'stats' ? sortInfo.order ?? null : undefined,
      render: (_, record: Document) => (
        <Space direction="vertical" size={0}>
          <Space size={4}>
            <EyeOutlined />
            <Text style={{ fontSize: '12px' }}>{record.viewCount}</Text>
          </Space>
          <Space size={4}>
            <DownloadOutlined />
            <Text style={{ fontSize: '12px' }}>{record.downloadCount}</Text>
          </Space>
        </Space>
      ),
    },
    {
      title: '文件信息',
      key: 'fileInfo',
      width: 120,
      render: (_, record: Document) => (
        <Space direction="vertical" size={0}>
          <Text style={{ fontSize: '12px' }}>v{record.version}</Text>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {formatFileSize(record.fileSize)}
          </Text>
        </Space>
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 150,
      render: (text: string) => (
        <Text style={{ fontSize: '12px' }}>{text}</Text>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, record: Document) => (
        <Space size="small">
          <Tooltip title="预览">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handlePreview(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="删除">
            <Popconfirm
              title="确定要删除这个文档吗？"
              onConfirm={() => handleDelete(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button
                type="text"
                size="small"
                icon={<DeleteOutlined />}
                danger
              />
            </Popconfirm>
          </Tooltip>
          <Dropdown
            menu={{
              items: [
                { key: 'duplicate', icon: <FileTextOutlined />, label: '复制文档' },
                { key: 'move', icon: <FolderOutlined />, label: '移动到分类' },
                { key: 'history', icon: <CalendarOutlined />, label: '版本历史' },
                { type: 'divider' as const },
                { key: 'archive', icon: <TagOutlined />, label: '归档文档' },
              ],
              onClick: ({ key }) => {
                switch (key) {
                  case 'duplicate':
                    message.info(`正在复制文档：${record.title}`);
                    break;
                  case 'move':
                    message.info(`准备将文档移动到分类：${record.categoryName}`);
                    break;
                  case 'history':
                    message.info(`打开文档版本历史：${record.title}`);
                    break;
                  case 'archive':
                    message.info(`正在归档文档：${record.title}`);
                    break;
                  default:
                    break;
                }
              },
            }}
            trigger={["click"]}
          >
            <Button
              type="text"
              size="small"
              icon={<MoreOutlined />}
            />
          </Dropdown>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    fetchDocuments();
  }, [filters, pagination.current, pagination.pageSize]);

  return (
    <div className="documents-page">
      <Card>
        {/* 页面头部 */}
        <div className="page-header">
          <div className="header-left">
            <Title level={3} style={{ margin: 0 }}>
              文档管理
            </Title>
            <Text type="secondary">
              共 {pagination.total} 个文档
            </Text>
          </div>
          <div className="header-right">
            <Space>
              <Button
                icon={<ImportOutlined />}
                onClick={handleImport}
              >
                导入
              </Button>
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出
              </Button>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  setCurrentDocument(null);
                  form.resetFields();
                  setEditModalVisible(true);
                }}
              >
                新建文档
              </Button>
            </Space>
          </div>
        </div>

        {/* 筛选区域 */}
        <div className="filter-section">
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} md={8} lg={6}>
              <Search
                placeholder="搜索文档标题或内容"
                allowClear
                onSearch={handleSearch}
                style={{ width: '100%' }}
              />
            </Col>
            <Col xs={24} sm={12} md={8} lg={6}>
              <Select
                placeholder="选择分类"
                allowClear
                style={{ width: '100%' }}
                onChange={(value) => handleFilter('categoryId', value)}
              >
                {categories.map(category => (
                  <Option key={category.id} value={category.id}>
                    {category.name}
                  </Option>
                ))}
              </Select>
            </Col>
            <Col xs={24} sm={12} md={8} lg={6}>
              <Select
                placeholder="选择状态"
                allowClear
                style={{ width: '100%' }}
                onChange={(value) => handleFilter('status', value)}
              >
                <Option value="draft">草稿</Option>
                <Option value="published">已发布</Option>
                <Option value="archived">已归档</Option>
              </Select>
            </Col>
            <Col xs={24} sm={12} md={8} lg={6}>
              <Select
                placeholder="选择作者"
                allowClear
                style={{ width: '100%' }}
                onChange={(value) => handleFilter('authorId', value)}
              >
                {authors.map(author => (
                  <Option key={author.id} value={author.id}>
                    {author.name}
                  </Option>
                ))}
              </Select>
            </Col>
          </Row>
        </div>

        {/* 批量操作 */}
        {selectedRowKeys.length > 0 && (
          <div className="batch-actions">
            <Space>
              <Text>已选择 {selectedRowKeys.length} 项</Text>
              <Button size="small" onClick={handleBatchPublish}>
                批量发布
              </Button>
              <Popconfirm
                title={`确定要删除选中的 ${selectedRowKeys.length} 个文档吗？`}
                onConfirm={handleBatchDelete}
                okText="确定"
                cancelText="取消"
              >
                <Button size="small" danger>
                  批量删除
                </Button>
              </Popconfirm>
              <Button
                size="small"
                onClick={() => setSelectedRowKeys([])}
              >
                取消选择
              </Button>
            </Space>
          </div>
        )}

        {/* 数据表格 */}
        <Table
          columns={columns}
          dataSource={documents}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
          }}
          rowSelection={handleRowSelection}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 编辑模态框 */}
      <Modal
        title={currentDocument ? '编辑文档' : '新建文档'}
        open={editModalVisible}
        onCancel={() => setEditModalVisible(false)}
        footer={null}
        width={800}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            try {
              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1000));
              message.success(currentDocument ? '更新成功' : '创建成功');
              setEditModalVisible(false);
              fetchDocuments();
            } catch (error) {
              message.error('操作失败');
            }
          }}
        >
          <Form.Item
            name="title"
            label="文档标题"
            rules={[{ required: true, message: '请输入文档标题' }]}
          >
            <Input placeholder="请输入文档标题" />
          </Form.Item>

          <Form.Item
            name="summary"
            label="文档摘要"
            rules={[{ required: true, message: '请输入文档摘要' }]}
          >
            <Input.TextArea
              rows={3}
              placeholder="请输入文档摘要"
              maxLength={200}
              showCount
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="categoryId"
                label="文档分类"
                rules={[{ required: true, message: '请选择文档分类' }]}
              >
                <Select placeholder="请选择文档分类">
                  {categories.map(category => (
                    <Option key={category.id} value={category.id}>
                      {category.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="status"
                label="文档状态"
                rules={[{ required: true, message: '请选择文档状态' }]}
              >
                <Select placeholder="请选择文档状态">
                  <Option value="draft">草稿</Option>
                  <Option value="published">已发布</Option>
                  <Option value="archived">已归档</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="tags"
            label="文档标签"
          >
            <Select
              mode="tags"
              placeholder="请输入文档标签"
              style={{ width: '100%' }}
            >
              <Option value="安装">安装</Option>
              <Option value="配置">配置</Option>
              <Option value="开发">开发</Option>
              <Option value="运维">运维</Option>
              <Option value="性能">性能</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {currentDocument ? '更新' : '创建'}
              </Button>
              <Button onClick={() => setEditModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 预览抽屉 */}
      <Drawer
        title="文档预览"
        placement="right"
        size="large"
        open={previewDrawerVisible}
        onClose={() => setPreviewDrawerVisible(false)}
      >
        {currentDocument && (
          <div className="document-preview">
            <div className="preview-header">
              <Title level={2}>{currentDocument.title}</Title>
              <Space wrap>
                {getStatusTag(currentDocument.status)}
                <Tag color="blue">{currentDocument.categoryName}</Tag>
                <Text type="secondary">
                  作者：{currentDocument.author}
                </Text>
                <Text type="secondary">
                  更新时间：{currentDocument.updatedAt}
                </Text>
              </Space>
            </div>
            <div className="preview-content">
              <Text>{currentDocument.content}</Text>
            </div>
            <div className="preview-footer">
              <Space>
                <Badge count={currentDocument.viewCount} showZero>
                  <EyeOutlined /> 浏览量
                </Badge>
                <Badge count={currentDocument.downloadCount} showZero>
                  <DownloadOutlined /> 下载量
                </Badge>
              </Space>
            </div>
          </div>
        )}
      </Drawer>

      {/* 导入模态框 */}
      <Modal
        title="导入文档"
        open={importModalVisible}
        onCancel={() => setImportModalVisible(false)}
        footer={null}
      >
        <Upload.Dragger {...uploadProps}>
          <p className="ant-upload-drag-icon">
            <UploadOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint">
            支持单个或批量上传。支持 .md, .txt, .docx 格式文件
          </p>
        </Upload.Dragger>
      </Modal>
    </div>
  );
};

export default Documents;
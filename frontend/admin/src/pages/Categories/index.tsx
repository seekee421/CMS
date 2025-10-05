import React, { useState, useEffect } from 'react';
import {
  Card,
  Tree,
  Button,
  Input,
  Modal,
  Form,
  Select,
  Switch,
  Space,
  Typography,
  Row,
  Col,
  message,
  Popconfirm,
  Dropdown,
  Tag,
  Tooltip,
  Badge,
  Divider,
  Empty,
  Spin
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  MoreOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  FileTextOutlined,
  DragOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  SettingOutlined,
  ReloadOutlined,
  SearchOutlined,
  ClearOutlined,
  SortAscendingOutlined,
  SortDescendingOutlined,
  AppstoreOutlined,
  UnorderedListOutlined
} from '@ant-design/icons';
import type { DataNode, TreeProps } from 'antd/es/tree';
import './index.less';

const { Search } = Input;
const { Title, Text } = Typography;
const { Option } = Select;

interface Category {
  id: string;
  name: string;
  code: string;
  description?: string;
  parentId?: string;
  level: number;
  sort: number;
  status: 'active' | 'inactive';
  isVisible: boolean;
  icon?: string;
  color?: string;
  documentCount: number;
  children?: Category[];
  createdAt: string;
  updatedAt: string;
}

interface TreeNode extends DataNode {
  key: string;
  title: React.ReactNode;
  children?: TreeNode[];
  category: Category;
  isLeaf?: boolean;
}

const Categories: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [treeData, setTreeData] = useState<TreeNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const [autoExpandParent, setAutoExpandParent] = useState(true);
  
  // 模态框状态
  const [modalVisible, setModalVisible] = useState(false);
  const [currentCategory, setCurrentCategory] = useState<Category | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [viewMode, setViewMode] = useState<'tree' | 'list'>('tree');
  
  const [form] = Form.useForm();

  // 模拟数据
  const mockCategories: Category[] = [
    {
      id: '1',
      name: '技术文档',
      code: 'tech',
      description: '技术相关文档分类',
      level: 1,
      sort: 1,
      status: 'active',
      isVisible: true,
      icon: 'code',
      color: '#1890ff',
      documentCount: 25,
      createdAt: '2024-01-01 00:00:00',
      updatedAt: '2024-01-15 10:30:00',
      children: [
        {
          id: '1-1',
          name: '前端开发',
          code: 'frontend',
          description: '前端开发技术文档',
          parentId: '1',
          level: 2,
          sort: 1,
          status: 'active',
          isVisible: true,
          icon: 'html5',
          color: '#52c41a',
          documentCount: 12,
          createdAt: '2024-01-01 00:00:00',
          updatedAt: '2024-01-15 10:30:00',
          children: [
            {
              id: '1-1-1',
              name: 'React',
              code: 'react',
              description: 'React框架相关文档',
              parentId: '1-1',
              level: 3,
              sort: 1,
              status: 'active',
              isVisible: true,
              documentCount: 8,
              createdAt: '2024-01-01 00:00:00',
              updatedAt: '2024-01-15 10:30:00'
            },
            {
              id: '1-1-2',
              name: 'Vue',
              code: 'vue',
              description: 'Vue框架相关文档',
              parentId: '1-1',
              level: 3,
              sort: 2,
              status: 'active',
              isVisible: true,
              documentCount: 4,
              createdAt: '2024-01-01 00:00:00',
              updatedAt: '2024-01-15 10:30:00'
            }
          ]
        },
        {
          id: '1-2',
          name: '后端开发',
          code: 'backend',
          description: '后端开发技术文档',
          parentId: '1',
          level: 2,
          sort: 2,
          status: 'active',
          isVisible: true,
          icon: 'database',
          color: '#faad14',
          documentCount: 13,
          createdAt: '2024-01-01 00:00:00',
          updatedAt: '2024-01-15 10:30:00',
          children: [
            {
              id: '1-2-1',
              name: 'Java',
              code: 'java',
              description: 'Java开发相关文档',
              parentId: '1-2',
              level: 3,
              sort: 1,
              status: 'active',
              isVisible: true,
              documentCount: 8,
              createdAt: '2024-01-01 00:00:00',
              updatedAt: '2024-01-15 10:30:00'
            },
            {
              id: '1-2-2',
              name: 'Python',
              code: 'python',
              description: 'Python开发相关文档',
              parentId: '1-2',
              level: 3,
              sort: 2,
              status: 'active',
              isVisible: true,
              documentCount: 5,
              createdAt: '2024-01-01 00:00:00',
              updatedAt: '2024-01-15 10:30:00'
            }
          ]
        }
      ]
    },
    {
      id: '2',
      name: '产品文档',
      code: 'product',
      description: '产品相关文档分类',
      level: 1,
      sort: 2,
      status: 'active',
      isVisible: true,
      icon: 'product',
      color: '#722ed1',
      documentCount: 18,
      createdAt: '2024-01-01 00:00:00',
      updatedAt: '2024-01-15 10:30:00',
      children: [
        {
          id: '2-1',
          name: '需求文档',
          code: 'requirements',
          description: '产品需求文档',
          parentId: '2',
          level: 2,
          sort: 1,
          status: 'active',
          isVisible: true,
          documentCount: 8,
          createdAt: '2024-01-01 00:00:00',
          updatedAt: '2024-01-15 10:30:00'
        },
        {
          id: '2-2',
          name: '设计文档',
          code: 'design',
          description: '产品设计文档',
          parentId: '2',
          level: 2,
          sort: 2,
          status: 'active',
          isVisible: true,
          documentCount: 10,
          createdAt: '2024-01-01 00:00:00',
          updatedAt: '2024-01-15 10:30:00'
        }
      ]
    },
    {
      id: '3',
      name: '运营文档',
      code: 'operation',
      description: '运营相关文档分类',
      level: 1,
      sort: 3,
      status: 'inactive',
      isVisible: false,
      icon: 'operation',
      color: '#f5222d',
      documentCount: 5,
      createdAt: '2024-01-01 00:00:00',
      updatedAt: '2024-01-15 10:30:00'
    }
  ];

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    const treeNodes = buildTreeData(categories);
    setTreeData(treeNodes);
    
    // 默认展开第一级
    const firstLevelKeys = categories
      .filter(cat => cat.level === 1)
      .map(cat => cat.id);
    setExpandedKeys(firstLevelKeys);
  }, [categories]);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setCategories(mockCategories);
    } catch (error) {
      message.error('获取分类数据失败');
    } finally {
      setLoading(false);
    }
  };

  const buildTreeData = (categories: Category[]): TreeNode[] => {
    const buildNode = (category: Category): TreeNode => {
      const hasChildren = category.children && category.children.length > 0;
      
      return {
        key: category.id,
        title: renderTreeNodeTitle(category),
        children: hasChildren ? category.children!.map(buildNode) : undefined,
        category,
        isLeaf: !hasChildren,
        icon: hasChildren ? 
          (expandedKeys.includes(category.id) ? <FolderOpenOutlined /> : <FolderOutlined />) :
          <FileTextOutlined />
      };
    };

    return categories.map(buildNode);
  };

  const renderTreeNodeTitle = (category: Category) => {
    const isSearchMatch = searchText && 
      category.name.toLowerCase().includes(searchText.toLowerCase());

    return (
      <div className="tree-node-title">
        <div className="node-info">
          <span className={`node-name ${isSearchMatch ? 'highlight' : ''}`}>
            {category.name}
          </span>
          <Space size={4}>
            <Badge count={category.documentCount} color="blue" size="small" />
            <Tag 
              color={category.status === 'active' ? 'success' : 'default'}
              size="small"
            >
              {category.status === 'active' ? '启用' : '禁用'}
            </Tag>
            {!category.isVisible && (
              <Tooltip title="隐藏分类">
                <EyeInvisibleOutlined style={{ color: '#ff4d4f' }} />
              </Tooltip>
            )}
          </Space>
        </div>
        <div className="node-actions" onClick={(e) => e.stopPropagation()}>
          <Space size={4}>
            <Tooltip title="添加子分类">
              <Button
                type="text"
                size="small"
                icon={<PlusOutlined />}
                onClick={() => handleAddChild(category)}
              />
            </Tooltip>
            <Tooltip title="编辑">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(category)}
              />
            </Tooltip>
            <Dropdown
              menu={{
                items: [
                  {
                    key: 'view',
                    label: '查看详情',
                    icon: <EyeOutlined />
                  },
                  {
                    key: 'toggle-status',
                    label: category.status === 'active' ? '禁用' : '启用',
                    icon: category.status === 'active' ? <EyeInvisibleOutlined /> : <EyeOutlined />
                  },
                  {
                    key: 'toggle-visibility',
                    label: category.isVisible ? '隐藏' : '显示',
                    icon: category.isVisible ? <EyeInvisibleOutlined /> : <EyeOutlined />
                  },
                  {
                    type: 'divider'
                  },
                  {
                    key: 'delete',
                    label: '删除',
                    icon: <DeleteOutlined />,
                    danger: true
                  }
                ],
                onClick: ({ key }) => handleMenuClick(key, category)
              }}
              trigger={['click']}
            >
              <Button
                type="text"
                size="small"
                icon={<MoreOutlined />}
              />
            </Dropdown>
          </Space>
        </div>
      </div>
    );
  };

  const handleMenuClick = (key: string, category: Category) => {
    switch (key) {
      case 'view':
        // 查看详情
        message.info(`查看分类：${category.name}`);
        break;
      case 'toggle-status':
        handleToggleStatus(category);
        break;
      case 'toggle-visibility':
        handleToggleVisibility(category);
        break;
      case 'delete':
        handleDelete(category);
        break;
    }
  };

  const handleToggleStatus = (category: Category) => {
    const newStatus = category.status === 'active' ? 'inactive' : 'active';
    updateCategoryInTree(category.id, { status: newStatus });
    message.success(`分类已${newStatus === 'active' ? '启用' : '禁用'}`);
  };

  const handleToggleVisibility = (category: Category) => {
    const newVisibility = !category.isVisible;
    updateCategoryInTree(category.id, { isVisible: newVisibility });
    message.success(`分类已${newVisibility ? '显示' : '隐藏'}`);
  };

  const handleDelete = (category: Category) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除分类"${category.name}"吗？删除后该分类下的所有子分类和文档都将被移动到其他分类。`,
      onOk: () => {
        removeCategoryFromTree(category.id);
        message.success('分类删除成功');
      }
    });
  };

  const updateCategoryInTree = (categoryId: string, updates: Partial<Category>) => {
    const updateCategory = (categories: Category[]): Category[] => {
      return categories.map(category => {
        if (category.id === categoryId) {
          return { ...category, ...updates };
        }
        if (category.children) {
          return {
            ...category,
            children: updateCategory(category.children)
          };
        }
        return category;
      });
    };

    setCategories(updateCategory(categories));
  };

  const removeCategoryFromTree = (categoryId: string) => {
    const removeCategory = (categories: Category[]): Category[] => {
      return categories
        .filter(category => category.id !== categoryId)
        .map(category => ({
          ...category,
          children: category.children ? removeCategory(category.children) : undefined
        }));
    };

    setCategories(removeCategory(categories));
  };

  const handleAddChild = (parentCategory: Category) => {
    setCurrentCategory(parentCategory);
    setIsEditing(false);
    setModalVisible(true);
    form.resetFields();
    form.setFieldsValue({
      parentId: parentCategory.id,
      level: parentCategory.level + 1,
      status: 'active',
      isVisible: true
    });
  };

  const handleEdit = (category: Category) => {
    setCurrentCategory(category);
    setIsEditing(true);
    setModalVisible(true);
    form.setFieldsValue(category);
  };

  const handleCreate = () => {
    setCurrentCategory(null);
    setIsEditing(false);
    setModalVisible(true);
    form.resetFields();
    form.setFieldsValue({
      level: 1,
      status: 'active',
      isVisible: true,
      sort: categories.filter(cat => cat.level === 1).length + 1
    });
  };

  const handleSubmit = async (values: any) => {
    try {
      if (isEditing && currentCategory) {
        // 更新分类
        updateCategoryInTree(currentCategory.id, {
          ...values,
          updatedAt: new Date().toISOString()
        });
        message.success('分类更新成功');
      } else {
        // 创建分类
        const newCategory: Category = {
          id: Date.now().toString(),
          ...values,
          documentCount: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };

        if (values.parentId) {
          // 添加到父分类
          addCategoryToParent(values.parentId, newCategory);
        } else {
          // 添加为根分类
          setCategories([...categories, newCategory]);
        }
        message.success('分类创建成功');
      }
      setModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const addCategoryToParent = (parentId: string, newCategory: Category) => {
    const addToParent = (categories: Category[]): Category[] => {
      return categories.map(category => {
        if (category.id === parentId) {
          return {
            ...category,
            children: [...(category.children || []), newCategory]
          };
        }
        if (category.children) {
          return {
            ...category,
            children: addToParent(category.children)
          };
        }
        return category;
      });
    };

    setCategories(addToParent(categories));
  };

  const handleSearch = (value: string) => {
    setSearchText(value);
    if (value) {
      // 展开所有包含搜索结果的节点
      const expandKeys = findExpandKeys(categories, value);
      setExpandedKeys(expandKeys);
      setAutoExpandParent(true);
    }
  };

  const findExpandKeys = (categories: Category[], searchValue: string): string[] => {
    const keys: string[] = [];
    
    const findKeys = (categories: Category[], parentKeys: string[] = []) => {
      categories.forEach(category => {
        const currentKeys = [...parentKeys, category.id];
        
        if (category.name.toLowerCase().includes(searchValue.toLowerCase())) {
          keys.push(...currentKeys);
        }
        
        if (category.children) {
          findKeys(category.children, currentKeys);
        }
      });
    };
    
    findKeys(categories);
    return [...new Set(keys)];
  };

  const onExpand = (expandedKeysValue: React.Key[]) => {
    setExpandedKeys(expandedKeysValue);
    setAutoExpandParent(false);
  };

  const onSelect = (selectedKeysValue: React.Key[]) => {
    setSelectedKeys(selectedKeysValue);
  };

  const flattenCategories = (categories: Category[]): Category[] => {
    const result: Category[] = [];
    
    const flatten = (categories: Category[]) => {
      categories.forEach(category => {
        result.push(category);
        if (category.children) {
          flatten(category.children);
        }
      });
    };
    
    flatten(categories);
    return result;
  };

  const filteredFlatCategories = flattenCategories(categories).filter(category =>
    !searchText || category.name.toLowerCase().includes(searchText.toLowerCase())
  );

  const renderListView = () => (
    <div className="category-list">
      {filteredFlatCategories.map(category => (
        <Card key={category.id} className="category-card" size="small">
          <div className="category-card-content">
            <div className="category-info">
              <div className="category-header">
                <Space>
                  <span className="category-level">
                    {'—'.repeat(category.level - 1)}
                  </span>
                  <span className="category-name">{category.name}</span>
                  <Tag color={category.status === 'active' ? 'success' : 'default'}>
                    {category.status === 'active' ? '启用' : '禁用'}
                  </Tag>
                  {!category.isVisible && (
                    <Tag color="error">隐藏</Tag>
                  )}
                </Space>
              </div>
              <div className="category-meta">
                <Text type="secondary">{category.description}</Text>
                <div className="category-stats">
                  <Space>
                    <Text type="secondary">文档数: {category.documentCount}</Text>
                    <Text type="secondary">代码: {category.code}</Text>
                    <Text type="secondary">排序: {category.sort}</Text>
                  </Space>
                </div>
              </div>
            </div>
            <div className="category-actions">
              <Space>
                <Button
                  type="text"
                  size="small"
                  icon={<PlusOutlined />}
                  onClick={() => handleAddChild(category)}
                >
                  添加子分类
                </Button>
                <Button
                  type="text"
                  size="small"
                  icon={<EditOutlined />}
                  onClick={() => handleEdit(category)}
                >
                  编辑
                </Button>
                <Dropdown
                  menu={{
                    items: [
                      {
                        key: 'toggle-status',
                        label: category.status === 'active' ? '禁用' : '启用',
                        icon: category.status === 'active' ? <EyeInvisibleOutlined /> : <EyeOutlined />
                      },
                      {
                        key: 'toggle-visibility',
                        label: category.isVisible ? '隐藏' : '显示',
                        icon: category.isVisible ? <EyeInvisibleOutlined /> : <EyeOutlined />
                      },
                      {
                        type: 'divider'
                      },
                      {
                        key: 'delete',
                        label: '删除',
                        icon: <DeleteOutlined />,
                        danger: true
                      }
                    ],
                    onClick: ({ key }) => handleMenuClick(key, category)
                  }}
                >
                  <Button type="text" size="small" icon={<MoreOutlined />} />
                </Dropdown>
              </Space>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );

  return (
    <div className="categories-page">
      <Card>
        {/* 页面头部 */}
        <div className="page-header">
          <div className="header-left">
            <Title level={4}>分类管理</Title>
            <Text type="secondary">管理文档分类结构和层级关系</Text>
          </div>
          <div className="header-right">
            <Space>
              <Button icon={<ReloadOutlined />} onClick={fetchCategories}>
                刷新
              </Button>
              <Button.Group>
                <Button
                  type={viewMode === 'tree' ? 'primary' : 'default'}
                  icon={<AppstoreOutlined />}
                  onClick={() => setViewMode('tree')}
                >
                  树形视图
                </Button>
                <Button
                  type={viewMode === 'list' ? 'primary' : 'default'}
                  icon={<UnorderedListOutlined />}
                  onClick={() => setViewMode('list')}
                >
                  列表视图
                </Button>
              </Button.Group>
              <Button 
                type="primary" 
                icon={<PlusOutlined />}
                onClick={handleCreate}
              >
                新建分类
              </Button>
            </Space>
          </div>
        </div>

        {/* 搜索区域 */}
        <div className="search-section">
          <Row gutter={16}>
            <Col span={8}>
              <Search
                placeholder="搜索分类名称"
                allowClear
                onSearch={handleSearch}
                style={{ width: '100%' }}
              />
            </Col>
            <Col span={16}>
              <Space>
                <Text type="secondary">
                  共 {flattenCategories(categories).length} 个分类
                </Text>
                <Divider type="vertical" />
                <Text type="secondary">
                  启用 {flattenCategories(categories).filter(c => c.status === 'active').length} 个
                </Text>
                <Divider type="vertical" />
                <Text type="secondary">
                  文档总数 {flattenCategories(categories).reduce((sum, c) => sum + c.documentCount, 0)} 个
                </Text>
              </Space>
            </Col>
          </Row>
        </div>

        {/* 内容区域 */}
        <div className="content-section">
          {loading ? (
            <div style={{ textAlign: 'center', padding: '50px 0' }}>
              <Spin size="large" />
            </div>
          ) : categories.length === 0 ? (
            <Empty
              description="暂无分类数据"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            >
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                创建第一个分类
              </Button>
            </Empty>
          ) : viewMode === 'tree' ? (
            <Tree
              className="category-tree"
              treeData={treeData}
              expandedKeys={expandedKeys}
              selectedKeys={selectedKeys}
              autoExpandParent={autoExpandParent}
              onExpand={onExpand}
              onSelect={onSelect}
              showIcon
              draggable
              blockNode
            />
          ) : (
            renderListView()
          )}
        </div>
      </Card>

      {/* 分类编辑模态框 */}
      <Modal
        title={isEditing ? '编辑分类' : '新建分类'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="分类名称"
                rules={[{ required: true, message: '请输入分类名称' }]}
              >
                <Input placeholder="请输入分类名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="code"
                label="分类代码"
                rules={[{ required: true, message: '请输入分类代码' }]}
              >
                <Input placeholder="请输入分类代码" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="分类描述"
          >
            <Input.TextArea 
              rows={3} 
              placeholder="请输入分类描述" 
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="sort"
                label="排序"
                rules={[{ required: true, message: '请输入排序值' }]}
              >
                <Input type="number" placeholder="排序值" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="status"
                label="状态"
                valuePropName="checked"
                getValueFromEvent={(checked) => checked ? 'active' : 'inactive'}
                getValueProps={(value) => ({ checked: value === 'active' })}
              >
                <Switch checkedChildren="启用" unCheckedChildren="禁用" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="isVisible"
                label="是否显示"
                valuePropName="checked"
              >
                <Switch checkedChildren="显示" unCheckedChildren="隐藏" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="icon"
                label="图标"
              >
                <Input placeholder="图标名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="color"
                label="颜色"
              >
                <Input type="color" />
              </Form.Item>
            </Col>
          </Row>

          {!isEditing && currentCategory && (
            <Form.Item
              name="parentId"
              label="父分类"
            >
              <Input disabled value={currentCategory.name} />
            </Form.Item>
          )}

          <Form.Item>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button onClick={() => setModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit">
                {isEditing ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Categories;
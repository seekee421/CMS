import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  Avatar,
  Modal,
  Form,
  Row,
  Col,
  Typography,
  Dropdown,
  message,
  Popconfirm,
  Switch,
  DatePicker,
  Upload,
  Divider,
  Tabs,
  Transfer,
  Tree,
  Badge,
  Tooltip,
  Progress,
  Statistic
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  MoreOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined,
  LockOutlined,
  UnlockOutlined,
  ExportOutlined,
  ImportOutlined,
  ReloadOutlined,
  SettingOutlined,
  TeamOutlined,
  SafetyOutlined,
  EyeOutlined,
  DownloadOutlined,
  UploadOutlined,
  FilterOutlined,
  ClearOutlined
} from '@ant-design/icons';
import type { ColumnsType, TableProps } from 'antd/es/table';
import type { UploadProps } from 'antd/es/upload';
import './index.less';

const { Search } = Input;
const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { TreeNode } = Tree;
const { RangePicker } = DatePicker;

interface User {
  id: string;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  realName: string;
  status: 'active' | 'inactive' | 'locked';
  roles: string[];
  department?: string;
  position?: string;
  lastLoginTime?: string;
  createdAt: string;
  updatedAt: string;
  loginCount: number;
  isOnline: boolean;
}

interface Role {
  id: string;
  name: string;
  code: string;
  description?: string;
  permissions: string[];
  userCount: number;
  status: 'active' | 'inactive';
  createdAt: string;
}

interface Permission {
  id: string;
  name: string;
  code: string;
  type: 'menu' | 'button' | 'api';
  parentId?: string;
  description?: string;
  children?: Permission[];
}

const Users: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [departmentFilter, setDepartmentFilter] = useState<string>('');
  const [dateRange, setDateRange] = useState<any[]>([]);
  
  // 模态框状态
  const [userModalVisible, setUserModalVisible] = useState(false);
  const [roleModalVisible, setRoleModalVisible] = useState(false);
  const [permissionModalVisible, setPermissionModalVisible] = useState(false);
  const [importModalVisible, setImportModalVisible] = useState(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [currentRole, setCurrentRole] = useState<Role | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [activeTab, setActiveTab] = useState('users');

  const [userForm] = Form.useForm();
  const [roleForm] = Form.useForm();

  // 模拟数据
  const mockUsers: User[] = [
    {
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      phone: '13800138000',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
      realName: '系统管理员',
      status: 'active',
      roles: ['admin', 'editor'],
      department: '技术部',
      position: '系统管理员',
      lastLoginTime: '2024-01-15 10:30:00',
      createdAt: '2024-01-01 00:00:00',
      updatedAt: '2024-01-15 10:30:00',
      loginCount: 156,
      isOnline: true
    },
    {
      id: '2',
      username: 'editor',
      email: 'editor@example.com',
      phone: '13800138001',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=editor',
      realName: '内容编辑',
      status: 'active',
      roles: ['editor'],
      department: '内容部',
      position: '高级编辑',
      lastLoginTime: '2024-01-15 09:15:00',
      createdAt: '2024-01-02 00:00:00',
      updatedAt: '2024-01-15 09:15:00',
      loginCount: 89,
      isOnline: false
    },
    {
      id: '3',
      username: 'viewer',
      email: 'viewer@example.com',
      realName: '普通用户',
      status: 'inactive',
      roles: ['viewer'],
      department: '市场部',
      position: '市场专员',
      lastLoginTime: '2024-01-10 16:45:00',
      createdAt: '2024-01-03 00:00:00',
      updatedAt: '2024-01-10 16:45:00',
      loginCount: 23,
      isOnline: false
    }
  ];

  const mockRoles: Role[] = [
    {
      id: '1',
      name: '超级管理员',
      code: 'admin',
      description: '拥有系统所有权限',
      permissions: ['user:read', 'user:write', 'user:delete', 'role:read', 'role:write'],
      userCount: 1,
      status: 'active',
      createdAt: '2024-01-01 00:00:00'
    },
    {
      id: '2',
      name: '编辑员',
      code: 'editor',
      description: '可以编辑和管理内容',
      permissions: ['document:read', 'document:write', 'category:read'],
      userCount: 5,
      status: 'active',
      createdAt: '2024-01-01 00:00:00'
    },
    {
      id: '3',
      name: '查看员',
      code: 'viewer',
      description: '只能查看内容',
      permissions: ['document:read'],
      userCount: 12,
      status: 'active',
      createdAt: '2024-01-01 00:00:00'
    }
  ];

  const mockPermissions: Permission[] = [
    {
      id: '1',
      name: '用户管理',
      code: 'user',
      type: 'menu',
      description: '用户管理模块',
      children: [
        { id: '1-1', name: '查看用户', code: 'user:read', type: 'button', parentId: '1' },
        { id: '1-2', name: '创建用户', code: 'user:write', type: 'button', parentId: '1' },
        { id: '1-3', name: '删除用户', code: 'user:delete', type: 'button', parentId: '1' }
      ]
    },
    {
      id: '2',
      name: '角色管理',
      code: 'role',
      type: 'menu',
      description: '角色管理模块',
      children: [
        { id: '2-1', name: '查看角色', code: 'role:read', type: 'button', parentId: '2' },
        { id: '2-2', name: '创建角色', code: 'role:write', type: 'button', parentId: '2' }
      ]
    },
    {
      id: '3',
      name: '文档管理',
      code: 'document',
      type: 'menu',
      description: '文档管理模块',
      children: [
        { id: '3-1', name: '查看文档', code: 'document:read', type: 'button', parentId: '3' },
        { id: '3-2', name: '编辑文档', code: 'document:write', type: 'button', parentId: '3' }
      ]
    }
  ];

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setUsers(mockUsers);
      setRoles(mockRoles);
      setPermissions(mockPermissions);
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (value: string) => {
    setSearchText(value);
  };

  const handleStatusChange = (userId: string, status: string) => {
    setUsers(users.map(user => 
      user.id === userId ? { ...user, status: status as any } : user
    ));
    message.success('用户状态更新成功');
  };

  const handleDeleteUser = (userId: string) => {
    setUsers(users.filter(user => user.id !== userId));
    message.success('用户删除成功');
  };

  const handleBatchDelete = () => {
    setUsers(users.filter(user => !selectedRowKeys.includes(user.id)));
    setSelectedRowKeys([]);
    message.success('批量删除成功');
  };

  const handleEditUser = (user: User) => {
    setCurrentUser(user);
    setIsEditing(true);
    setUserModalVisible(true);
    userForm.setFieldsValue({
      ...user,
      roles: user.roles
    });
  };

  const handleCreateUser = () => {
    setCurrentUser(null);
    setIsEditing(false);
    setUserModalVisible(true);
    userForm.resetFields();
  };

  const handleUserSubmit = async (values: any) => {
    try {
      if (isEditing && currentUser) {
        // 更新用户
        setUsers(users.map(user => 
          user.id === currentUser.id 
            ? { ...user, ...values, updatedAt: new Date().toISOString() }
            : user
        ));
        message.success('用户更新成功');
      } else {
        // 创建用户
        const newUser: User = {
          id: Date.now().toString(),
          ...values,
          status: 'active',
          loginCount: 0,
          isOnline: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        setUsers([...users, newUser]);
        message.success('用户创建成功');
      }
      setUserModalVisible(false);
      userForm.resetFields();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleRoleSubmit = async (values: any) => {
    try {
      if (currentRole) {
        // 更新角色
        setRoles(roles.map(role => 
          role.id === currentRole.id 
            ? { ...role, ...values }
            : role
        ));
        message.success('角色更新成功');
      } else {
        // 创建角色
        const newRole: Role = {
          id: Date.now().toString(),
          ...values,
          userCount: 0,
          status: 'active',
          createdAt: new Date().toISOString()
        };
        setRoles([...roles, newRole]);
        message.success('角色创建成功');
      }
      setRoleModalVisible(false);
      roleForm.resetFields();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleExport = () => {
    // 模拟导出
    const dataStr = JSON.stringify(users, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'users.json';
    link.click();
    URL.revokeObjectURL(url);
    message.success('导出成功');
  };

  const uploadProps: UploadProps = {
    name: 'file',
    accept: '.xlsx,.xls,.csv',
    beforeUpload: (file) => {
      const isValidType = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                         file.type === 'application/vnd.ms-excel' ||
                         file.type === 'text/csv';
      if (!isValidType) {
        message.error('只能上传 Excel 或 CSV 文件！');
      }
      return isValidType;
    },
    onChange: (info) => {
      if (info.file.status === 'done') {
        message.success('导入成功');
        setImportModalVisible(false);
        fetchData();
      } else if (info.file.status === 'error') {
        message.error('导入失败');
      }
    }
  };

  const userColumns: ColumnsType<User> = [
    {
      title: '用户信息',
      key: 'userInfo',
      width: 200,
      render: (_, record) => (
        <Space>
          <Badge dot={record.isOnline} color="green">
            <Avatar 
              src={record.avatar} 
              icon={<UserOutlined />}
              size={40}
            />
          </Badge>
          <div>
            <div style={{ fontWeight: 500 }}>{record.realName}</div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              @{record.username}
            </Text>
          </div>
        </Space>
      )
    },
    {
      title: '联系方式',
      key: 'contact',
      width: 180,
      render: (_, record) => (
        <div>
          <div style={{ marginBottom: 4 }}>
            <MailOutlined style={{ marginRight: 4, color: '#1890ff' }} />
            <Text style={{ fontSize: 12 }}>{record.email}</Text>
          </div>
          {record.phone && (
            <div>
              <PhoneOutlined style={{ marginRight: 4, color: '#52c41a' }} />
              <Text style={{ fontSize: 12 }}>{record.phone}</Text>
            </div>
          )}
        </div>
      )
    },
    {
      title: '部门职位',
      key: 'department',
      width: 150,
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4 }}>{record.department}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.position}</Text>
        </div>
      )
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      width: 150,
      render: (roles: string[]) => (
        <Space wrap>
          {roles.map(role => (
            <Tag key={role} color="blue">{role}</Tag>
          ))}
        </Space>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusConfig = {
          active: { color: 'success', text: '正常' },
          inactive: { color: 'default', text: '禁用' },
          locked: { color: 'error', text: '锁定' }
        };
        const config = statusConfig[status as keyof typeof statusConfig];
        return <Tag color={config.color}>{config.text}</Tag>;
      }
    },
    {
      title: '登录统计',
      key: 'loginStats',
      width: 120,
      render: (_, record) => (
        <div>
          <div style={{ marginBottom: 4 }}>
            <Text style={{ fontSize: 12 }}>登录次数: {record.loginCount}</Text>
          </div>
          {record.lastLoginTime && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              最后登录: {record.lastLoginTime.split(' ')[0]}
            </Text>
          )}
        </div>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => (
        <Text style={{ fontSize: 12 }}>{date.split(' ')[0]}</Text>
      )
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Tooltip title="编辑">
            <Button 
              type="text" 
              icon={<EditOutlined />} 
              size="small"
              onClick={() => handleEditUser(record)}
            />
          </Tooltip>
          <Tooltip title="查看详情">
            <Button 
              type="text" 
              icon={<EyeOutlined />} 
              size="small"
            />
          </Tooltip>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'resetPassword',
                  label: '重置密码',
                  icon: <LockOutlined />
                },
                {
                  key: 'changeStatus',
                  label: record.status === 'active' ? '禁用用户' : '启用用户',
                  icon: record.status === 'active' ? <LockOutlined /> : <UnlockOutlined />
                },
                {
                  type: 'divider'
                },
                {
                  key: 'delete',
                  label: '删除用户',
                  icon: <DeleteOutlined />,
                  danger: true
                }
              ],
              onClick: ({ key }) => {
                if (key === 'delete') {
                  Modal.confirm({
                    title: '确认删除',
                    content: `确定要删除用户 ${record.realName} 吗？`,
                    onOk: () => handleDeleteUser(record.id)
                  });
                } else if (key === 'changeStatus') {
                  const newStatus = record.status === 'active' ? 'inactive' : 'active';
                  handleStatusChange(record.id, newStatus);
                } else if (key === 'resetPassword') {
                  message.success('密码重置邮件已发送');
                }
              }
            }}
          >
            <Button type="text" icon={<MoreOutlined />} size="small" />
          </Dropdown>
        </Space>
      )
    }
  ];

  const roleColumns: ColumnsType<Role> = [
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
      width: 150
    },
    {
      title: '角色代码',
      dataIndex: 'code',
      key: 'code',
      width: 120,
      render: (code: string) => <Tag color="blue">{code}</Tag>
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '用户数量',
      dataIndex: 'userCount',
      key: 'userCount',
      width: 100,
      render: (count: number) => (
        <Badge count={count} color="blue" />
      )
    },
    {
      title: '权限数量',
      key: 'permissionCount',
      width: 100,
      render: (_, record) => (
        <Badge count={record.permissions.length} color="green" />
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={status === 'active' ? 'success' : 'default'}>
          {status === 'active' ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => date.split(' ')[0]
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Space>
          <Button 
            type="text" 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => {
              setCurrentRole(record);
              setRoleModalVisible(true);
              roleForm.setFieldsValue(record);
            }}
          />
          <Button 
            type="text" 
            icon={<SettingOutlined />} 
            size="small"
            onClick={() => setPermissionModalVisible(true)}
          />
          <Popconfirm
            title="确定删除这个角色吗？"
            onConfirm={() => {
              setRoles(roles.filter(role => role.id !== record.id));
              message.success('角色删除成功');
            }}
          >
            <Button 
              type="text" 
              icon={<DeleteOutlined />} 
              size="small"
              danger
            />
          </Popconfirm>
        </Space>
      )
    }
  ];

  const filteredUsers = users.filter(user => {
    const matchesSearch = !searchText || 
      user.realName.toLowerCase().includes(searchText.toLowerCase()) ||
      user.username.toLowerCase().includes(searchText.toLowerCase()) ||
      user.email.toLowerCase().includes(searchText.toLowerCase());
    
    const matchesStatus = !statusFilter || user.status === statusFilter;
    const matchesRole = !roleFilter || user.roles.includes(roleFilter);
    const matchesDepartment = !departmentFilter || user.department === departmentFilter;
    
    return matchesSearch && matchesStatus && matchesRole && matchesDepartment;
  });

  const rowSelection: TableProps<User>['rowSelection'] = {
    selectedRowKeys,
    onChange: setSelectedRowKeys,
    selections: [
      Table.SELECTION_ALL,
      Table.SELECTION_INVERT,
      Table.SELECTION_NONE
    ]
  };

  const renderUserTab = () => (
    <div>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={users.length}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="在线用户"
              value={users.filter(u => u.isOnline).length}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={users.filter(u => u.status === 'active').length}
              prefix={<SafetyOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="本月新增"
              value={2}
              prefix={<PlusOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        {/* 页面头部 */}
        <div className="page-header">
          <div className="header-left">
            <Title level={4}>用户管理</Title>
            <Text type="secondary">管理系统用户账户和权限</Text>
          </div>
          <div className="header-right">
            <Space>
              <Button icon={<ReloadOutlined />} onClick={fetchData}>
                刷新
              </Button>
              <Button icon={<ExportOutlined />} onClick={handleExport}>
                导出
              </Button>
              <Button 
                icon={<ImportOutlined />} 
                onClick={() => setImportModalVisible(true)}
              >
                导入
              </Button>
              <Button 
                type="primary" 
                icon={<PlusOutlined />}
                onClick={handleCreateUser}
              >
                新建用户
              </Button>
            </Space>
          </div>
        </div>

        {/* 筛选区域 */}
        <div className="filter-section">
          <Row gutter={16}>
            <Col span={6}>
              <Search
                placeholder="搜索用户名、姓名、邮箱"
                allowClear
                onSearch={handleSearch}
                style={{ width: '100%' }}
              />
            </Col>
            <Col span={4}>
              <Select
                placeholder="状态"
                allowClear
                style={{ width: '100%' }}
                value={statusFilter}
                onChange={setStatusFilter}
              >
                <Option value="active">正常</Option>
                <Option value="inactive">禁用</Option>
                <Option value="locked">锁定</Option>
              </Select>
            </Col>
            <Col span={4}>
              <Select
                placeholder="角色"
                allowClear
                style={{ width: '100%' }}
                value={roleFilter}
                onChange={setRoleFilter}
              >
                {roles.map(role => (
                  <Option key={role.code} value={role.code}>
                    {role.name}
                  </Option>
                ))}
              </Select>
            </Col>
            <Col span={4}>
              <Select
                placeholder="部门"
                allowClear
                style={{ width: '100%' }}
                value={departmentFilter}
                onChange={setDepartmentFilter}
              >
                <Option value="技术部">技术部</Option>
                <Option value="内容部">内容部</Option>
                <Option value="市场部">市场部</Option>
              </Select>
            </Col>
            <Col span={6}>
              <RangePicker
                placeholder={['开始日期', '结束日期']}
                style={{ width: '100%' }}
                value={dateRange}
                onChange={setDateRange}
              />
            </Col>
          </Row>
        </div>

        {/* 批量操作 */}
        {selectedRowKeys.length > 0 && (
          <div className="batch-actions">
            <Text>已选择 {selectedRowKeys.length} 项</Text>
            <Space>
              <Button size="small">批量启用</Button>
              <Button size="small">批量禁用</Button>
              <Button size="small" danger onClick={handleBatchDelete}>
                批量删除
              </Button>
            </Space>
          </div>
        )}

        {/* 用户表格 */}
        <Table
          columns={userColumns}
          dataSource={filteredUsers}
          rowKey="id"
          loading={loading}
          rowSelection={rowSelection}
          scroll={{ x: 1200 }}
          pagination={{
            total: filteredUsers.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条/总共 ${total} 条`
          }}
        />
      </Card>
    </div>
  );

  const renderRoleTab = () => (
    <Card>
      <div className="page-header">
        <div className="header-left">
          <Title level={4}>角色管理</Title>
          <Text type="secondary">管理系统角色和权限配置</Text>
        </div>
        <div className="header-right">
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchData}>
              刷新
            </Button>
            <Button 
              type="primary" 
              icon={<PlusOutlined />}
              onClick={() => {
                setCurrentRole(null);
                setRoleModalVisible(true);
                roleForm.resetFields();
              }}
            >
              新建角色
            </Button>
          </Space>
        </div>
      </div>

      <Table
        columns={roleColumns}
        dataSource={roles}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total, range) => 
            `第 ${range[0]}-${range[1]} 条/总共 ${total} 条`
        }}
      />
    </Card>
  );

  return (
    <div className="users-page">
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab="用户管理" key="users">
          {renderUserTab()}
        </TabPane>
        <TabPane tab="角色管理" key="roles">
          {renderRoleTab()}
        </TabPane>
      </Tabs>

      {/* 用户编辑模态框 */}
      <Modal
        title={isEditing ? '编辑用户' : '新建用户'}
        open={userModalVisible}
        onCancel={() => setUserModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={userForm}
          layout="vertical"
          onFinish={handleUserSubmit}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="username"
                label="用户名"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="请输入用户名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="realName"
                label="真实姓名"
                rules={[{ required: true, message: '请输入真实姓名' }]}
              >
                <Input placeholder="请输入真实姓名" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="email"
                label="邮箱"
                rules={[
                  { required: true, message: '请输入邮箱' },
                  { type: 'email', message: '请输入有效的邮箱地址' }
                ]}
              >
                <Input placeholder="请输入邮箱" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="phone"
                label="手机号"
              >
                <Input placeholder="请输入手机号" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="department"
                label="部门"
              >
                <Select placeholder="请选择部门">
                  <Option value="技术部">技术部</Option>
                  <Option value="内容部">内容部</Option>
                  <Option value="市场部">市场部</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="position"
                label="职位"
              >
                <Input placeholder="请输入职位" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="roles"
            label="角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择角色"
              style={{ width: '100%' }}
            >
              {roles.map(role => (
                <Option key={role.code} value={role.code}>
                  {role.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          {!isEditing && (
            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          )}

          <Form.Item>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button onClick={() => setUserModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit">
                {isEditing ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 角色编辑模态框 */}
      <Modal
        title={currentRole ? '编辑角色' : '新建角色'}
        open={roleModalVisible}
        onCancel={() => setRoleModalVisible(false)}
        footer={null}
        width={500}
      >
        <Form
          form={roleForm}
          layout="vertical"
          onFinish={handleRoleSubmit}
        >
          <Form.Item
            name="name"
            label="角色名称"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>

          <Form.Item
            name="code"
            label="角色代码"
            rules={[{ required: true, message: '请输入角色代码' }]}
          >
            <Input placeholder="请输入角色代码" />
          </Form.Item>

          <Form.Item
            name="description"
            label="角色描述"
          >
            <Input.TextArea 
              rows={3} 
              placeholder="请输入角色描述" 
            />
          </Form.Item>

          <Form.Item
            name="permissions"
            label="权限配置"
          >
            <Tree
              checkable
              defaultExpandAll
              treeData={permissions.map(perm => ({
                title: perm.name,
                key: perm.code,
                children: perm.children?.map(child => ({
                  title: child.name,
                  key: child.code
                }))
              }))}
            />
          </Form.Item>

          <Form.Item>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button onClick={() => setRoleModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit">
                {currentRole ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 权限配置模态框 */}
      <Modal
        title="权限配置"
        open={permissionModalVisible}
        onCancel={() => setPermissionModalVisible(false)}
        footer={null}
        width={800}
      >
        <Tree
          checkable
          defaultExpandAll
          treeData={permissions.map(perm => ({
            title: perm.name,
            key: perm.code,
            children: perm.children?.map(child => ({
              title: child.name,
              key: child.code
            }))
          }))}
        />
      </Modal>

      {/* 导入模态框 */}
      <Modal
        title="导入用户"
        open={importModalVisible}
        onCancel={() => setImportModalVisible(false)}
        footer={null}
        width={500}
      >
        <Upload.Dragger {...uploadProps}>
          <p className="ant-upload-drag-icon">
            <UploadOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint">
            支持 Excel (.xlsx, .xls) 和 CSV 格式文件
          </p>
        </Upload.Dragger>
        
        <Divider />
        
        <div style={{ textAlign: 'center' }}>
          <Button 
            type="link" 
            icon={<DownloadOutlined />}
            onClick={() => {
              // 下载模板
              message.info('模板下载功能开发中');
            }}
          >
            下载导入模板
          </Button>
        </div>
      </Modal>
    </div>
  );
};

export default Users;
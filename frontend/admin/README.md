# CMS管理后台开发计划

## 项目概述

CMS管理后台是一个基于React + Ant Design的企业级管理系统，提供用户管理、权限控制、文档管理、系统监控等功能。

## 技术栈

### 核心技术
- **React 18+** - 使用函数组件和Hooks
- **TypeScript** - 类型安全的JavaScript
- **Ant Design 5.x** - 企业级UI组件库
- **React Router 6** - 路由管理
- **Redux Toolkit** - 状态管理
- **React Query** - 服务端状态管理
- **Axios** - HTTP客户端

### 开发工具
- **Vite** - 构建工具
- **ESLint + Prettier** - 代码规范
- **Husky** - Git钩子
- **Jest + Testing Library** - 测试框架

### 特殊组件
- **Monaco Editor** - 代码编辑器
- **React DnD** - 拖拽功能
- **Recharts** - 图表组件
- **React Helmet** - 页面头部管理

## 项目结构

```
admin/
├── public/                   # 静态资源
│   ├── index.html
│   ├── favicon.ico
│   └── manifest.json
├── src/
│   ├── components/           # 通用组件
│   │   ├── Layout/          # 布局组件
│   │   │   ├── Header/      # 顶部导航
│   │   │   ├── Sidebar/     # 侧边栏
│   │   │   ├── Footer/      # 底部
│   │   │   └── index.tsx
│   │   ├── Common/          # 通用组件
│   │   │   ├── LoadingSpinner/
│   │   │   ├── ErrorBoundary/
│   │   │   ├── ConfirmModal/
│   │   │   ├── SearchInput/
│   │   │   ├── TableActions/
│   │   │   └── PageHeader/
│   │   ├── Editor/          # 编辑器组件
│   │   │   ├── MarkdownEditor/
│   │   │   ├── CodeEditor/
│   │   │   └── RichTextEditor/
│   │   └── Charts/          # 图表组件
│   │       ├── LineChart/
│   │       ├── BarChart/
│   │       ├── PieChart/
│   │       └── StatCard/
│   ├── pages/               # 页面组件
│   │   ├── Login/           # 登录页面
│   │   ├── Dashboard/       # 仪表板
│   │   ├── Users/           # 用户管理
│   │   │   ├── UserList/
│   │   │   ├── UserForm/
│   │   │   └── UserDetail/
│   │   ├── Roles/           # 角色管理
│   │   │   ├── RoleList/
│   │   │   ├── RoleForm/
│   │   │   └── PermissionMatrix/
│   │   ├── Documents/       # 文档管理
│   │   │   ├── DocumentList/
│   │   │   ├── DocumentEditor/
│   │   │   ├── DocumentPreview/
│   │   │   ├── CategoryManager/
│   │   │   └── BatchOperations/
│   │   ├── System/          # 系统管理
│   │   │   ├── AuditLogs/
│   │   │   ├── CacheManager/
│   │   │   ├── BackupManager/
│   │   │   └── SystemSettings/
│   │   └── Profile/         # 个人中心
│   ├── hooks/               # 自定义Hooks
│   │   ├── useAuth.ts       # 认证Hook
│   │   ├── usePermission.ts # 权限Hook
│   │   ├── useTable.ts      # 表格Hook
│   │   ├── useModal.ts      # 弹窗Hook
│   │   └── useDebounce.ts   # 防抖Hook
│   ├── store/               # Redux状态管理
│   │   ├── slices/          # Redux Slices
│   │   │   ├── authSlice.ts
│   │   │   ├── userSlice.ts
│   │   │   ├── documentSlice.ts
│   │   │   └── uiSlice.ts
│   │   ├── api/             # RTK Query API
│   │   │   ├── authApi.ts
│   │   │   ├── usersApi.ts
│   │   │   ├── documentsApi.ts
│   │   │   └── systemApi.ts
│   │   └── index.ts         # Store配置
│   ├── utils/               # 工具函数
│   │   ├── constants.ts     # 常量定义
│   │   ├── helpers.ts       # 辅助函数
│   │   ├── validators.ts    # 验证函数
│   │   └── permissions.ts   # 权限工具
│   ├── styles/              # 样式文件
│   │   ├── globals.css      # 全局样式
│   │   ├── variables.css    # CSS变量
│   │   └── antd-theme.ts    # Ant Design主题
│   ├── types/               # 类型定义
│   │   ├── api.ts           # API类型
│   │   ├── components.ts    # 组件类型
│   │   └── store.ts         # Store类型
│   ├── App.tsx              # 根组件
│   ├── main.tsx             # 入口文件
│   └── vite-env.d.ts        # Vite类型声明
├── tests/                   # 测试文件
│   ├── __mocks__/           # Mock文件
│   ├── components/          # 组件测试
│   ├── pages/               # 页面测试
│   └── utils/               # 工具测试
├── .env.example             # 环境变量示例
├── .eslintrc.js             # ESLint配置
├── .prettierrc              # Prettier配置
├── package.json
├── tsconfig.json            # TypeScript配置
├── vite.config.ts           # Vite配置
└── README.md
```

## 功能模块设计

### 1. 登录模块 (Login)

#### 功能特性
- 用户名/密码登录
- 记住登录状态
- 登录失败提示
- 自动跳转到目标页面

#### 组件设计
```typescript
// src/pages/Login/index.tsx
interface LoginFormData {
  username: string;
  password: string;
  remember: boolean;
}

const LoginPage: React.FC = () => {
  const [form] = Form.useForm<LoginFormData>();
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleSubmit = async (values: LoginFormData) => {
    try {
      await login(values.username, values.password);
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    } catch (error) {
      message.error('登录失败，请检查用户名和密码');
    }
  };

  return (
    <div className="login-container">
      <Card className="login-card">
        <div className="login-header">
          <img src="/logo.svg" alt="CMS" />
          <h1>CMS管理后台</h1>
        </div>
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Form.Item name="remember" valuePropName="checked">
            <Checkbox>记住登录状态</Checkbox>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
```

### 2. 仪表板模块 (Dashboard)

#### 功能特性
- 系统概览统计
- 最近活动日志
- 快速操作入口
- 性能监控图表

#### 组件设计
```typescript
// src/pages/Dashboard/index.tsx
const Dashboard: React.FC = () => {
  const { data: overview } = useQuery({
    queryKey: ['dashboard-overview'],
    queryFn: systemApi.getOverview,
  });

  const { data: activities } = useQuery({
    queryKey: ['recent-activities'],
    queryFn: () => auditApi.getRecentActivities({ limit: 10 }),
  });

  return (
    <div className="dashboard">
      <PageHeader title="仪表板" />
      
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className="stats-row">
        <Col span={6}>
          <StatCard
            title="总用户数"
            value={overview?.userCount}
            icon={<UserOutlined />}
            color="#1890ff"
          />
        </Col>
        <Col span={6}>
          <StatCard
            title="文档总数"
            value={overview?.documentCount}
            icon={<FileTextOutlined />}
            color="#52c41a"
          />
        </Col>
        <Col span={6}>
          <StatCard
            title="今日访问"
            value={overview?.todayViews}
            icon={<EyeOutlined />}
            color="#faad14"
          />
        </Col>
        <Col span={6}>
          <StatCard
            title="系统状态"
            value="正常"
            icon={<CheckCircleOutlined />}
            color="#f5222d"
          />
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="content-row">
        {/* 访问趋势图表 */}
        <Col span={16}>
          <Card title="访问趋势" extra={<Button>查看详情</Button>}>
            <LineChart data={overview?.visitTrend} />
          </Card>
        </Col>

        {/* 快速操作 */}
        <Col span={8}>
          <Card title="快速操作">
            <Space direction="vertical" style={{ width: '100%' }}>
              <Button type="primary" icon={<PlusOutlined />} block>
                创建文档
              </Button>
              <Button icon={<UserAddOutlined />} block>
                添加用户
              </Button>
              <Button icon={<SettingOutlined />} block>
                系统设置
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 最近活动 */}
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="最近活动">
            <Timeline>
              {activities?.map((activity) => (
                <Timeline.Item key={activity.id}>
                  <div>
                    <strong>{activity.username}</strong> {activity.operation}
                    <span className="activity-time">
                      {formatDate(activity.timestamp)}
                    </span>
                  </div>
                </Timeline.Item>
              ))}
            </Timeline>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
```

### 3. 用户管理模块 (Users)

#### 功能特性
- 用户列表展示和搜索
- 用户创建和编辑
- 角色分配
- 批量操作
- 用户状态管理

#### 组件设计
```typescript
// src/pages/Users/UserList/index.tsx
const UserList: React.FC = () => {
  const [searchParams, setSearchParams] = useState({
    page: 0,
    size: 10,
    search: '',
  });

  const { data: users, isLoading } = useQuery({
    queryKey: ['users', searchParams],
    queryFn: () => usersApi.getUsers(searchParams),
  });

  const deleteUserMutation = useMutation({
    mutationFn: usersApi.deleteUser,
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries(['users']);
    },
  });

  const columns: ColumnsType<User> = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      sorter: true,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: Role[]) => (
        <>
          {roles.map((role) => (
            <Tag key={role.id} color="blue">
              {role.name}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: UserStatus) => (
        <Badge
          status={status === UserStatus.ACTIVE ? 'success' : 'error'}
          text={status === UserStatus.ACTIVE ? '活跃' : '禁用'}
        />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => formatDate(date),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <TableActions
          items={[
            {
              label: '编辑',
              icon: <EditOutlined />,
              onClick: () => handleEdit(record),
            },
            {
              label: '删除',
              icon: <DeleteOutlined />,
              danger: true,
              onClick: () => handleDelete(record),
            },
          ]}
        />
      ),
    },
  ];

  return (
    <div className="user-list">
      <PageHeader
        title="用户管理"
        extra={[
          <Button key="create" type="primary" icon={<PlusOutlined />}>
            创建用户
          </Button>,
        ]}
      />

      <Card>
        <div className="table-toolbar">
          <SearchInput
            placeholder="搜索用户名或邮箱"
            onSearch={(value) => setSearchParams({ ...searchParams, search: value })}
          />
          <Space>
            <Button icon={<ExportOutlined />}>导出</Button>
            <Button icon={<ImportOutlined />}>导入</Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={users?.content}
          loading={isLoading}
          pagination={{
            current: searchParams.page + 1,
            pageSize: searchParams.size,
            total: users?.totalElements,
            onChange: (page, size) => setSearchParams({ ...searchParams, page: page - 1, size }),
          }}
          rowSelection={{
            type: 'checkbox',
            onChange: (selectedRowKeys) => {
              // 处理批量选择
            },
          }}
        />
      </Card>
    </div>
  );
};
```

### 4. 文档管理模块 (Documents)

#### 功能特性
- 文档列表和搜索
- 在线编辑器 (Monaco Editor)
- 文档预览
- 分类管理
- 版本历史
- 批量操作

#### 组件设计
```typescript
// src/pages/Documents/DocumentEditor/index.tsx
const DocumentEditor: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [form] = Form.useForm();
  const [content, setContent] = useState('');
  const [previewMode, setPreviewMode] = useState(false);

  const { data: document } = useQuery({
    queryKey: ['document', id],
    queryFn: () => documentsApi.getDocumentById(Number(id)),
    enabled: !!id,
  });

  const saveDocumentMutation = useMutation({
    mutationFn: documentsApi.updateDocument,
    onSuccess: () => {
      message.success('保存成功');
    },
  });

  const handleSave = async () => {
    const values = await form.validateFields();
    await saveDocumentMutation.mutateAsync({
      id: Number(id),
      ...values,
      content,
    });
  };

  return (
    <div className="document-editor">
      <div className="editor-header">
        <Space>
          <Button
            type={previewMode ? 'default' : 'primary'}
            onClick={() => setPreviewMode(false)}
          >
            编辑
          </Button>
          <Button
            type={previewMode ? 'primary' : 'default'}
            onClick={() => setPreviewMode(true)}
          >
            预览
          </Button>
          <Divider type="vertical" />
          <Button icon={<SaveOutlined />} onClick={handleSave}>
            保存
          </Button>
          <Button icon={<EyeOutlined />}>发布</Button>
        </Space>
      </div>

      <div className="editor-content">
        {previewMode ? (
          <div className="preview-container">
            <MarkdownPreview content={content} />
          </div>
        ) : (
          <div className="editor-container">
            <div className="editor-sidebar">
              <Form form={form} layout="vertical">
                <Form.Item name="title" label="标题">
                  <Input placeholder="请输入文档标题" />
                </Form.Item>
                <Form.Item name="categoryId" label="分类">
                  <CategorySelect />
                </Form.Item>
                <Form.Item name="tags" label="标签">
                  <TagInput />
                </Form.Item>
                <Form.Item name="isPublic" valuePropName="checked">
                  <Checkbox>公开文档</Checkbox>
                </Form.Item>
              </Form>
            </div>
            <div className="editor-main">
              <MonacoEditor
                language="markdown"
                value={content}
                onChange={setContent}
                options={{
                  minimap: { enabled: false },
                  wordWrap: 'on',
                  lineNumbers: 'on',
                }}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
```

### 5. 系统管理模块 (System)

#### 功能特性
- 审计日志查看
- 缓存管理
- 备份管理
- 系统设置

#### 组件设计
```typescript
// src/pages/System/AuditLogs/index.tsx
const AuditLogs: React.FC = () => {
  const [filters, setFilters] = useState({
    username: '',
    operationType: '',
    startDate: null,
    endDate: null,
  });

  const { data: logs, isLoading } = useQuery({
    queryKey: ['audit-logs', filters],
    queryFn: () => auditApi.getAuditLogs(filters),
  });

  const columns: ColumnsType<AuditLog> = [
    {
      title: '时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (date: string) => formatDate(date),
      sorter: true,
    },
    {
      title: '用户',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '操作类型',
      dataIndex: 'operationType',
      key: 'operationType',
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '资源类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
    },
    {
      title: '资源ID',
      dataIndex: 'resourceId',
      key: 'resourceId',
    },
    {
      title: '操作结果',
      dataIndex: 'success',
      key: 'success',
      render: (success: boolean) => (
        <Badge
          status={success ? 'success' : 'error'}
          text={success ? '成功' : '失败'}
        />
      ),
    },
    {
      title: '详情',
      dataIndex: 'details',
      key: 'details',
      render: (details: string) => (
        <Button size="small" onClick={() => showDetails(details)}>
          查看
        </Button>
      ),
    },
  ];

  return (
    <div className="audit-logs">
      <PageHeader title="审计日志" />
      
      <Card>
        <div className="filter-form">
          <Form layout="inline">
            <Form.Item label="用户名">
              <Input
                placeholder="请输入用户名"
                value={filters.username}
                onChange={(e) => setFilters({ ...filters, username: e.target.value })}
              />
            </Form.Item>
            <Form.Item label="操作类型">
              <Select
                placeholder="请选择操作类型"
                style={{ width: 120 }}
                value={filters.operationType}
                onChange={(value) => setFilters({ ...filters, operationType: value })}
              >
                <Select.Option value="">全部</Select.Option>
                <Select.Option value="CREATE">创建</Select.Option>
                <Select.Option value="UPDATE">更新</Select.Option>
                <Select.Option value="DELETE">删除</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item label="时间范围">
              <DatePicker.RangePicker
                onChange={(dates) => setFilters({
                  ...filters,
                  startDate: dates?.[0],
                  endDate: dates?.[1],
                })}
              />
            </Form.Item>
            <Form.Item>
              <Button type="primary" icon={<SearchOutlined />}>
                查询
              </Button>
            </Form.Item>
          </Form>
        </div>

        <Table
          columns={columns}
          dataSource={logs?.content}
          loading={isLoading}
          pagination={{
            total: logs?.totalElements,
            showSizeChanger: true,
            showQuickJumper: true,
          }}
        />
      </Card>
    </div>
  );
};
```

## 状态管理设计

### Redux Store结构
```typescript
// src/store/index.ts
export interface RootState {
  auth: AuthState;
  users: UsersState;
  documents: DocumentsState;
  ui: UIState;
}

// Auth Slice
interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

// Users Slice
interface UsersState {
  list: User[];
  current: User | null;
  loading: boolean;
  filters: UserFilters;
  pagination: PaginationState;
}

// Documents Slice
interface DocumentsState {
  list: Document[];
  current: Document | null;
  categories: DocumentCategory[];
  loading: boolean;
  filters: DocumentFilters;
}
```

## 路由设计

```typescript
// src/App.tsx
const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<ProtectedRoute />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/users" element={<UserList />} />
          <Route path="/users/create" element={<UserForm />} />
          <Route path="/users/:id" element={<UserDetail />} />
          <Route path="/roles" element={<RoleList />} />
          <Route path="/documents" element={<DocumentList />} />
          <Route path="/documents/create" element={<DocumentEditor />} />
          <Route path="/documents/:id" element={<DocumentEditor />} />
          <Route path="/system/audit" element={<AuditLogs />} />
          <Route path="/system/cache" element={<CacheManager />} />
          <Route path="/profile" element={<Profile />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};
```

## 开发计划

### 第一周：项目初始化
- [ ] 项目脚手架搭建
- [ ] 基础配置 (ESLint, Prettier, TypeScript)
- [ ] 共享代码库集成
- [ ] 基础布局组件开发

### 第二周：认证和权限
- [ ] 登录页面开发
- [ ] 认证状态管理
- [ ] 权限控制组件
- [ ] 路由守卫实现

### 第三周：用户管理
- [ ] 用户列表页面
- [ ] 用户创建/编辑表单
- [ ] 角色权限管理
- [ ] 批量操作功能

### 第四周：文档管理基础
- [ ] 文档列表页面
- [ ] 文档分类管理
- [ ] 基础编辑器集成
- [ ] 文档预览功能

### 第五周：高级编辑功能
- [ ] Monaco Editor深度集成
- [ ] 实时预览功能
- [ ] 文档版本管理
- [ ] 媒体文件上传

### 第六周：系统管理
- [ ] 仪表板开发
- [ ] 审计日志页面
- [ ] 缓存管理界面
- [ ] 系统设置页面

### 第七周：优化和测试
- [ ] 性能优化
- [ ] 单元测试编写
- [ ] 端到端测试
- [ ] 代码审查和重构

## 部署配置

### 环境变量
```bash
# .env.production
VITE_API_BASE_URL=https://api.cms.example.com
VITE_APP_TITLE=CMS管理后台
VITE_UPLOAD_MAX_SIZE=20971520
```

### 构建配置
```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          antd: ['antd'],
          editor: ['@monaco-editor/react'],
        },
      },
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```
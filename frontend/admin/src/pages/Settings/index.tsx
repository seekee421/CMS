import React, { useState, useEffect } from 'react';
import {
  Card,
  Tabs,
  Form,
  Input,
  Button,
  Switch,
  Select,
  Upload,
  Avatar,
  Row,
  Col,
  Divider,
  Typography,
  Space,
  message,
  Modal,
  Table,
  Tag,
  Popconfirm,
  InputNumber,
  Radio,
  Slider,
  ColorPicker,
  TimePicker,
  DatePicker,
  Tooltip,
  Alert,
  Progress,
  List,
  Badge,
  Descriptions,
  Statistic,
  Empty,
  Spin
} from 'antd';
import {
  UserOutlined,
  SettingOutlined,
  SecurityScanOutlined,
  DatabaseOutlined,
  MailOutlined,
  BellOutlined,
  FileTextOutlined,
  CloudUploadOutlined,
  LockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SaveOutlined,
  ReloadOutlined,
  ExportOutlined,
  ImportOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  WarningOutlined
} from '@ant-design/icons';
import type { UploadProps, TabsProps } from 'antd';
import './index.less';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;
const { Option } = Select;
const { TabPane } = Tabs;
const { RangePicker } = DatePicker;

interface UserProfile {
  id: string;
  username: string;
  email: string;
  phone: string;
  avatar: string;
  nickname: string;
  department: string;
  position: string;
  description: string;
  lastLoginTime: string;
  createTime: string;
}

interface SystemConfig {
  siteName: string;
  siteDescription: string;
  siteLogo: string;
  siteIcon: string;
  defaultLanguage: string;
  timezone: string;
  dateFormat: string;
  timeFormat: string;
  pageSize: number;
  maxFileSize: number;
  allowedFileTypes: string[];
  enableRegistration: boolean;
  enableEmailVerification: boolean;
  enableSMS: boolean;
  maintenanceMode: boolean;
  debugMode: boolean;
}

interface SecurityConfig {
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireNumbers: boolean;
  passwordRequireSymbols: boolean;
  sessionTimeout: number;
  maxLoginAttempts: number;
  lockoutDuration: number;
  enableTwoFactor: boolean;
  enableCaptcha: boolean;
  ipWhitelist: string[];
  enableAuditLog: boolean;
}

interface EmailConfig {
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword: string;
  smtpSecurity: string;
  fromEmail: string;
  fromName: string;
  enableEmail: boolean;
  testEmail: string;
}

interface NotificationConfig {
  enableEmailNotification: boolean;
  enableSMSNotification: boolean;
  enablePushNotification: boolean;
  notificationTypes: string[];
  quietHours: {
    enabled: boolean;
    startTime: string;
    endTime: string;
  };
}

interface BackupConfig {
  enableAutoBackup: boolean;
  backupFrequency: string;
  backupTime: string;
  retentionDays: number;
  backupLocation: string;
  enableCloudBackup: boolean;
  cloudProvider: string;
  cloudConfig: any;
}

const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [userProfile, setUserProfile] = useState<UserProfile>({
    id: '1',
    username: 'admin',
    email: 'admin@example.com',
    phone: '13800138000',
    avatar: '',
    nickname: '系统管理员',
    department: '技术部',
    position: '高级工程师',
    description: '负责系统架构设计和开发工作',
    lastLoginTime: '2024-01-15 10:30:00',
    createTime: '2023-01-01 00:00:00'
  });

  const [systemConfig, setSystemConfig] = useState<SystemConfig>({
    siteName: 'CMS文档管理系统',
    siteDescription: '专业的企业文档管理解决方案',
    siteLogo: '',
    siteIcon: '',
    defaultLanguage: 'zh-CN',
    timezone: 'Asia/Shanghai',
    dateFormat: 'YYYY-MM-DD',
    timeFormat: 'HH:mm:ss',
    pageSize: 20,
    maxFileSize: 100,
    allowedFileTypes: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md'],
    enableRegistration: false,
    enableEmailVerification: true,
    enableSMS: false,
    maintenanceMode: false,
    debugMode: false
  });

  const [securityConfig, setSecurityConfig] = useState<SecurityConfig>({
    passwordMinLength: 8,
    passwordRequireUppercase: true,
    passwordRequireNumbers: true,
    passwordRequireSymbols: false,
    sessionTimeout: 30,
    maxLoginAttempts: 5,
    lockoutDuration: 15,
    enableTwoFactor: false,
    enableCaptcha: true,
    ipWhitelist: [],
    enableAuditLog: true
  });

  const [emailConfig, setEmailConfig] = useState<EmailConfig>({
    smtpHost: 'smtp.example.com',
    smtpPort: 587,
    smtpUsername: '',
    smtpPassword: '',
    smtpSecurity: 'TLS',
    fromEmail: 'noreply@example.com',
    fromName: 'CMS系统',
    enableEmail: false,
    testEmail: ''
  });

  const [notificationConfig, setNotificationConfig] = useState<NotificationConfig>({
    enableEmailNotification: true,
    enableSMSNotification: false,
    enablePushNotification: true,
    notificationTypes: ['document_published', 'user_registered', 'system_alert'],
    quietHours: {
      enabled: false,
      startTime: '22:00',
      endTime: '08:00'
    }
  });

  const [backupConfig, setBackupConfig] = useState<BackupConfig>({
    enableAutoBackup: true,
    backupFrequency: 'daily',
    backupTime: '02:00',
    retentionDays: 30,
    backupLocation: '/backup',
    enableCloudBackup: false,
    cloudProvider: 'aliyun',
    cloudConfig: {}
  });

  const [passwordVisible, setPasswordVisible] = useState(false);
  const [testEmailLoading, setTestEmailLoading] = useState(false);
  const [backupLoading, setBackupLoading] = useState(false);

  // 模拟数据
  const auditLogs = [
    {
      id: '1',
      action: '用户登录',
      user: 'admin',
      ip: '192.168.1.100',
      time: '2024-01-15 10:30:00',
      status: 'success'
    },
    {
      id: '2',
      action: '修改系统配置',
      user: 'admin',
      ip: '192.168.1.100',
      time: '2024-01-15 10:25:00',
      status: 'success'
    },
    {
      id: '3',
      action: '删除文档',
      user: 'editor',
      ip: '192.168.1.101',
      time: '2024-01-15 10:20:00',
      status: 'success'
    }
  ];

  const backupHistory = [
    {
      id: '1',
      type: '自动备份',
      size: '256MB',
      time: '2024-01-15 02:00:00',
      status: 'success',
      location: '/backup/auto_20240115_020000.sql'
    },
    {
      id: '2',
      type: '手动备份',
      size: '245MB',
      time: '2024-01-14 15:30:00',
      status: 'success',
      location: '/backup/manual_20240114_153000.sql'
    },
    {
      id: '3',
      type: '自动备份',
      size: '238MB',
      time: '2024-01-14 02:00:00',
      status: 'success',
      location: '/backup/auto_20240114_020000.sql'
    }
  ];

  const systemStatus = {
    cpu: 45,
    memory: 68,
    disk: 32,
    network: 'normal',
    database: 'connected',
    cache: 'running',
    queue: 'running'
  };

  useEffect(() => {
    // 模拟加载数据
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  }, []);

  const handleSaveProfile = async (values: any) => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setUserProfile({ ...userProfile, ...values });
      message.success('个人信息保存成功！');
    } catch (error) {
      message.error('保存失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSystemConfig = async (values: any) => {
    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSystemConfig({ ...systemConfig, ...values });
      message.success('系统配置保存成功！');
    } catch (error) {
      message.error('保存失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSecurityConfig = async (values: any) => {
    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSecurityConfig({ ...securityConfig, ...values });
      message.success('安全配置保存成功！');
    } catch (error) {
      message.error('保存失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveEmailConfig = async (values: any) => {
    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      setEmailConfig({ ...emailConfig, ...values });
      message.success('邮件配置保存成功！');
    } catch (error) {
      message.error('保存失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleTestEmail = async () => {
    if (!emailConfig.testEmail) {
      message.warning('请输入测试邮箱地址');
      return;
    }

    setTestEmailLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 2000));
      message.success('测试邮件发送成功！');
    } catch (error) {
      message.error('测试邮件发送失败');
    } finally {
      setTestEmailLoading(false);
    }
  };

  const handleBackupNow = async () => {
    setBackupLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 3000));
      message.success('数据备份完成！');
    } catch (error) {
      message.error('备份失败，请重试');
    } finally {
      setBackupLoading(false);
    }
  };

  const handleExportConfig = () => {
    const config = {
      system: systemConfig,
      security: securityConfig,
      email: emailConfig,
      notification: notificationConfig,
      backup: backupConfig
    };
    
    const blob = new Blob([JSON.stringify(config, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'system-config.json';
    a.click();
    URL.revokeObjectURL(url);
    message.success('配置导出成功！');
  };

  const uploadProps: UploadProps = {
    name: 'file',
    action: '/api/upload',
    headers: {
      authorization: 'authorization-text',
    },
    onChange(info) {
      if (info.file.status === 'done') {
        message.success(`${info.file.name} 文件上传成功`);
      } else if (info.file.status === 'error') {
        message.error(`${info.file.name} 文件上传失败`);
      }
    },
  };

  const auditLogColumns = [
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
    },
    {
      title: '用户',
      dataIndex: 'user',
      key: 'user',
    },
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
    },
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'success' ? 'green' : 'red'}>
          {status === 'success' ? '成功' : '失败'}
        </Tag>
      ),
    },
  ];

  const backupColumns = [
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
    },
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'success' ? 'green' : 'red'}>
          {status === 'success' ? '成功' : '失败'}
        </Tag>
      ),
    },
    {
      title: '位置',
      dataIndex: 'location',
      key: 'location',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any) => (
        <Space>
          <Button type="link" size="small" icon={<CloudUploadOutlined />}>
            下载
          </Button>
          <Popconfirm title="确定删除此备份？" onConfirm={() => message.success('删除成功')}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const tabItems: TabsProps['items'] = [
    {
      key: 'profile',
      label: (
        <span>
          <UserOutlined />
          个人信息
        </span>
      ),
      children: (
        <Card>
          <Form
            layout="vertical"
            initialValues={userProfile}
            onFinish={handleSaveProfile}
          >
            <Row gutter={24}>
              <Col span={24} md={8}>
                <div className="avatar-section">
                  <Avatar size={120} src={userProfile.avatar} icon={<UserOutlined />} />
                  <Upload {...uploadProps} showUploadList={false}>
                    <Button icon={<CloudUploadOutlined />} style={{ marginTop: 16 }}>
                      更换头像
                    </Button>
                  </Upload>
                </div>
              </Col>
              <Col span={24} md={16}>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      label="用户名"
                      name="username"
                      rules={[{ required: true, message: '请输入用户名' }]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label="昵称"
                      name="nickname"
                      rules={[{ required: true, message: '请输入昵称' }]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label="邮箱"
                      name="email"
                      rules={[
                        { required: true, message: '请输入邮箱' },
                        { type: 'email', message: '请输入有效的邮箱地址' }
                      ]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label="手机号"
                      name="phone"
                      rules={[{ required: true, message: '请输入手机号' }]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label="部门"
                      name="department"
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label="职位"
                      name="position"
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item
                      label="个人描述"
                      name="description"
                    >
                      <TextArea rows={4} />
                    </Form.Item>
                  </Col>
                </Row>
              </Col>
            </Row>
            <Divider />
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存修改
                </Button>
                <Button icon={<ReloadOutlined />}>
                  重置
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'system',
      label: (
        <span>
          <SettingOutlined />
          系统配置
        </span>
      ),
      children: (
        <Card>
          <Form
            layout="vertical"
            initialValues={systemConfig}
            onFinish={handleSaveSystemConfig}
          >
            <Title level={4}>基本信息</Title>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="站点名称"
                  name="siteName"
                  rules={[{ required: true, message: '请输入站点名称' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="默认语言"
                  name="defaultLanguage"
                >
                  <Select>
                    <Option value="zh-CN">简体中文</Option>
                    <Option value="en-US">English</Option>
                    <Option value="ja-JP">日本語</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item
                  label="站点描述"
                  name="siteDescription"
                >
                  <TextArea rows={3} />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>显示设置</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="时区"
                  name="timezone"
                >
                  <Select>
                    <Option value="Asia/Shanghai">Asia/Shanghai</Option>
                    <Option value="UTC">UTC</Option>
                    <Option value="America/New_York">America/New_York</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="日期格式"
                  name="dateFormat"
                >
                  <Select>
                    <Option value="YYYY-MM-DD">YYYY-MM-DD</Option>
                    <Option value="MM/DD/YYYY">MM/DD/YYYY</Option>
                    <Option value="DD/MM/YYYY">DD/MM/YYYY</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="每页显示"
                  name="pageSize"
                >
                  <InputNumber min={10} max={100} />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>文件设置</Title>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="最大文件大小 (MB)"
                  name="maxFileSize"
                >
                  <InputNumber min={1} max={1000} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="允许的文件类型"
                  name="allowedFileTypes"
                >
                  <Select mode="tags" placeholder="输入文件扩展名">
                    <Option value="pdf">pdf</Option>
                    <Option value="doc">doc</Option>
                    <Option value="docx">docx</Option>
                    <Option value="xls">xls</Option>
                    <Option value="xlsx">xlsx</Option>
                    <Option value="ppt">ppt</Option>
                    <Option value="pptx">pptx</Option>
                    <Option value="txt">txt</Option>
                    <Option value="md">md</Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>功能开关</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="允许用户注册"
                  name="enableRegistration"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="邮箱验证"
                  name="enableEmailVerification"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="短信功能"
                  name="enableSMS"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="维护模式"
                  name="maintenanceMode"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="调试模式"
                  name="debugMode"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存配置
                </Button>
                <Button icon={<ExportOutlined />} onClick={handleExportConfig}>
                  导出配置
                </Button>
                <Upload {...uploadProps} showUploadList={false}>
                  <Button icon={<ImportOutlined />}>
                    导入配置
                  </Button>
                </Upload>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'security',
      label: (
        <span>
          <SecurityScanOutlined />
          安全设置
        </span>
      ),
      children: (
        <Card>
          <Form
            layout="vertical"
            initialValues={securityConfig}
            onFinish={handleSaveSecurityConfig}
          >
            <Title level={4}>密码策略</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="最小长度"
                  name="passwordMinLength"
                >
                  <InputNumber min={6} max={20} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="要求大写字母"
                  name="passwordRequireUppercase"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="要求数字"
                  name="passwordRequireNumbers"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="要求特殊字符"
                  name="passwordRequireSymbols"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>会话管理</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="会话超时 (分钟)"
                  name="sessionTimeout"
                >
                  <InputNumber min={5} max={480} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="最大登录尝试次数"
                  name="maxLoginAttempts"
                >
                  <InputNumber min={3} max={10} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="锁定时长 (分钟)"
                  name="lockoutDuration"
                >
                  <InputNumber min={5} max={60} />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>安全功能</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="双因子认证"
                  name="enableTwoFactor"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="验证码"
                  name="enableCaptcha"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="审计日志"
                  name="enableAuditLog"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>IP白名单</Title>
            <Form.Item
              label="允许的IP地址"
              name="ipWhitelist"
              tooltip="每行一个IP地址或IP段，支持CIDR格式"
            >
              <TextArea
                rows={4}
                placeholder="192.168.1.0/24&#10;10.0.0.1&#10;172.16.0.0/16"
              />
            </Form.Item>

            <Divider />
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存配置
                </Button>
                <Button icon={<ReloadOutlined />}>
                  重置
                </Button>
              </Space>
            </Form.Item>
          </Form>

          <Divider />
          <Title level={4}>审计日志</Title>
          <Table
            columns={auditLogColumns}
            dataSource={auditLogs}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            size="small"
          />
        </Card>
      ),
    },
    {
      key: 'email',
      label: (
        <span>
          <MailOutlined />
          邮件配置
        </span>
      ),
      children: (
        <Card>
          <Form
            layout="vertical"
            initialValues={emailConfig}
            onFinish={handleSaveEmailConfig}
          >
            <Row gutter={16}>
              <Col span={24}>
                <Form.Item
                  label="启用邮件功能"
                  name="enableEmail"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Title level={4}>SMTP配置</Title>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="SMTP服务器"
                  name="smtpHost"
                  rules={[{ required: true, message: '请输入SMTP服务器地址' }]}
                >
                  <Input placeholder="smtp.example.com" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  label="端口"
                  name="smtpPort"
                  rules={[{ required: true, message: '请输入端口号' }]}
                >
                  <InputNumber min={1} max={65535} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  label="安全类型"
                  name="smtpSecurity"
                >
                  <Select>
                    <Option value="NONE">无</Option>
                    <Option value="TLS">TLS</Option>
                    <Option value="SSL">SSL</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="用户名"
                  name="smtpUsername"
                  rules={[{ required: true, message: '请输入用户名' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="密码"
                  name="smtpPassword"
                  rules={[{ required: true, message: '请输入密码' }]}
                >
                  <Input.Password
                    visibilityToggle={{
                      visible: passwordVisible,
                      onVisibleChange: setPasswordVisible,
                    }}
                  />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>发件人信息</Title>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="发件人邮箱"
                  name="fromEmail"
                  rules={[
                    { required: true, message: '请输入发件人邮箱' },
                    { type: 'email', message: '请输入有效的邮箱地址' }
                  ]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="发件人名称"
                  name="fromName"
                  rules={[{ required: true, message: '请输入发件人名称' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>邮件测试</Title>
            <Row gutter={16}>
              <Col span={16}>
                <Form.Item
                  label="测试邮箱"
                  name="testEmail"
                >
                  <Input
                    placeholder="输入测试邮箱地址"
                    value={emailConfig.testEmail}
                    onChange={(e) => setEmailConfig({ ...emailConfig, testEmail: e.target.value })}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label=" ">
                  <Button
                    type="default"
                    loading={testEmailLoading}
                    onClick={handleTestEmail}
                    icon={<MailOutlined />}
                  >
                    发送测试邮件
                  </Button>
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存配置
                </Button>
                <Button icon={<ReloadOutlined />}>
                  重置
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'notification',
      label: (
        <span>
          <BellOutlined />
          通知设置
        </span>
      ),
      children: (
        <Card>
          <Form
            layout="vertical"
            initialValues={notificationConfig}
            onFinish={(values) => {
              setLoading(true);
              setTimeout(() => {
                setNotificationConfig({ ...notificationConfig, ...values });
                message.success('通知配置保存成功！');
                setLoading(false);
              }, 1000);
            }}
          >
            <Title level={4}>通知方式</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="邮件通知"
                  name="enableEmailNotification"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="短信通知"
                  name="enableSMSNotification"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="推送通知"
                  name="enablePushNotification"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Title level={4}>通知类型</Title>
            <Form.Item
              label="启用的通知类型"
              name="notificationTypes"
            >
              <Select mode="multiple" placeholder="选择通知类型">
                <Option value="document_published">文档发布</Option>
                <Option value="document_updated">文档更新</Option>
                <Option value="user_registered">用户注册</Option>
                <Option value="user_login">用户登录</Option>
                <Option value="system_alert">系统警报</Option>
                <Option value="backup_completed">备份完成</Option>
                <Option value="maintenance_mode">维护模式</Option>
              </Select>
            </Form.Item>

            <Divider />
            <Title level={4}>免打扰时间</Title>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="启用免打扰"
                  name={['quietHours', 'enabled']}
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="开始时间"
                  name={['quietHours', 'startTime']}
                >
                  <TimePicker format="HH:mm" style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="结束时间"
                  name={['quietHours', 'endTime']}
                >
                  <TimePicker format="HH:mm" style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Divider />
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存配置
                </Button>
                <Button icon={<ReloadOutlined />}>
                  重置
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'backup',
      label: (
        <span>
          <DatabaseOutlined />
          备份管理
        </span>
      ),
      children: (
        <div>
          <Card title="备份配置" style={{ marginBottom: 24 }}>
            <Form
              layout="vertical"
              initialValues={backupConfig}
              onFinish={(values) => {
                setLoading(true);
                setTimeout(() => {
                  setBackupConfig({ ...backupConfig, ...values });
                  message.success('备份配置保存成功！');
                  setLoading(false);
                }, 1000);
              }}
            >
              <Row gutter={16}>
                <Col span={8}>
                  <Form.Item
                    label="自动备份"
                    name="enableAutoBackup"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    label="备份频率"
                    name="backupFrequency"
                  >
                    <Select>
                      <Option value="daily">每日</Option>
                      <Option value="weekly">每周</Option>
                      <Option value="monthly">每月</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    label="备份时间"
                    name="backupTime"
                  >
                    <TimePicker format="HH:mm" style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    label="保留天数"
                    name="retentionDays"
                  >
                    <InputNumber min={1} max={365} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={16}>
                  <Form.Item
                    label="备份位置"
                    name="backupLocation"
                  >
                    <Input />
                  </Form.Item>
                </Col>
              </Row>

              <Divider />
              <Title level={5}>云备份</Title>
              <Row gutter={16}>
                <Col span={8}>
                  <Form.Item
                    label="启用云备份"
                    name="enableCloudBackup"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Col>
                <Col span={16}>
                  <Form.Item
                    label="云服务商"
                    name="cloudProvider"
                  >
                    <Select>
                      <Option value="aliyun">阿里云</Option>
                      <Option value="tencent">腾讯云</Option>
                      <Option value="aws">AWS</Option>
                      <Option value="azure">Azure</Option>
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                    保存配置
                  </Button>
                  <Button
                    type="default"
                    loading={backupLoading}
                    onClick={handleBackupNow}
                    icon={<DatabaseOutlined />}
                  >
                    立即备份
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>

          <Card title="备份历史">
            <Table
              columns={backupColumns}
              dataSource={backupHistory}
              rowKey="id"
              pagination={{ pageSize: 10 }}
            />
          </Card>
        </div>
      ),
    },
    {
      key: 'monitor',
      label: (
        <span>
          <InfoCircleOutlined />
          系统监控
        </span>
      ),
      children: (
        <div>
          <Row gutter={24}>
            <Col span={24} lg={12}>
              <Card title="系统状态" style={{ marginBottom: 24 }}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div className="status-item">
                    <Text>CPU使用率</Text>
                    <Progress
                      percent={systemStatus.cpu}
                      status={systemStatus.cpu > 80 ? 'exception' : systemStatus.cpu > 60 ? 'active' : 'success'}
                      format={(percent) => `${percent}%`}
                    />
                  </div>
                  <div className="status-item">
                    <Text>内存使用率</Text>
                    <Progress
                      percent={systemStatus.memory}
                      status={systemStatus.memory > 80 ? 'exception' : systemStatus.memory > 60 ? 'active' : 'success'}
                      format={(percent) => `${percent}%`}
                    />
                  </div>
                  <div className="status-item">
                    <Text>磁盘使用率</Text>
                    <Progress
                      percent={systemStatus.disk}
                      status={systemStatus.disk > 80 ? 'exception' : systemStatus.disk > 60 ? 'active' : 'success'}
                      format={(percent) => `${percent}%`}
                    />
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={24} lg={12}>
              <Card title="服务状态" style={{ marginBottom: 24 }}>
                <List
                  dataSource={[
                    { name: '数据库', status: systemStatus.database, icon: <DatabaseOutlined /> },
                    { name: '缓存服务', status: systemStatus.cache, icon: <CloudUploadOutlined /> },
                    { name: '消息队列', status: systemStatus.queue, icon: <BellOutlined /> },
                    { name: '网络连接', status: systemStatus.network, icon: <InfoCircleOutlined /> },
                  ]}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={item.icon}
                        title={item.name}
                        description={
                          <Badge
                            status={item.status === 'connected' || item.status === 'running' || item.status === 'normal' ? 'success' : 'error'}
                            text={item.status === 'connected' ? '已连接' : item.status === 'running' ? '运行中' : item.status === 'normal' ? '正常' : '异常'}
                          />
                        }
                      />
                    </List.Item>
                  )}
                />
              </Card>
            </Col>
          </Row>

          <Card title="系统信息">
            <Descriptions column={2} bordered>
              <Descriptions.Item label="操作系统">Linux Ubuntu 20.04</Descriptions.Item>
              <Descriptions.Item label="Java版本">OpenJDK 11.0.16</Descriptions.Item>
              <Descriptions.Item label="Spring Boot版本">2.7.0</Descriptions.Item>
              <Descriptions.Item label="数据库版本">MySQL 8.0.30</Descriptions.Item>
              <Descriptions.Item label="Redis版本">6.2.7</Descriptions.Item>
              <Descriptions.Item label="Nginx版本">1.18.0</Descriptions.Item>
              <Descriptions.Item label="系统启动时间">2024-01-15 08:00:00</Descriptions.Item>
              <Descriptions.Item label="运行时长">7天 2小时 30分钟</Descriptions.Item>
            </Descriptions>
          </Card>
        </div>
      ),
    },
  ];

  if (loading) {
    return (
      <div className="settings-page">
        <Spin size="large" style={{ display: 'block', textAlign: 'center', marginTop: 100 }} />
      </div>
    );
  }

  return (
    <div className="settings-page">
      <div className="page-header">
        <Title level={3}>系统设置</Title>
        <Text type="secondary">管理系统配置、安全设置和个人信息</Text>
      </div>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
        size="large"
      />
    </div>
  );
};

export default Settings;
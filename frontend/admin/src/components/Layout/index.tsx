import React, { useState, useEffect } from 'react';
import {
  Layout,
  Menu,
  Avatar,
  Dropdown,
  Badge,
  Button,
  Drawer,
  Switch,
  Space,
  Typography,
  Divider,
  message,
  Breadcrumb,
  theme,
  ConfigProvider,
} from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  FileTextOutlined,
  UserOutlined,
  FolderOutlined,
  BarChartOutlined,
  SettingOutlined,
  BellOutlined,
  LogoutOutlined,
  SunOutlined,
  MoonOutlined,
  GlobalOutlined,
  QuestionCircleOutlined,
  GithubOutlined,
  MailOutlined,
  PhoneOutlined,
  HomeOutlined,
} from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import './index.less';

const { Header, Sider, Content, Footer } = Layout;
const { Text } = Typography;

interface LayoutProps {
  children: React.ReactNode;
}

interface MenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
  path: string;
  children?: MenuItem[];
}

interface UserInfo {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: string;
  department: string;
}

interface Notification {
  id: string;
  title: string;
  content: string;
  type: 'info' | 'warning' | 'error' | 'success';
  time: string;
  read: boolean;
}

const AdminLayout: React.FC<LayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [notificationDrawer, setNotificationDrawer] = useState(false);
  const [userDrawer, setUserDrawer] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [currentUser, setCurrentUser] = useState<UserInfo | null>(null);
  const [breadcrumbItems, setBreadcrumbItems] = useState<any[]>([]);

  const location = useLocation();
  const navigate = useNavigate();

  const {
    token: { colorBgContainer },
  } = theme.useToken();

  // 菜单配置
  const menuItems: MenuItem[] = [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: '仪表板',
      path: '/admin/dashboard',
    },
    {
      key: 'documents',
      icon: <FileTextOutlined />,
      label: '文档管理',
      path: '/admin/documents',
    },
    {
      key: 'categories',
      icon: <FolderOutlined />,
      label: '分类管理',
      path: '/admin/categories',
    },
    {
      key: 'users',
      icon: <UserOutlined />,
      label: '用户管理',
      path: '/admin/users',
    },
    {
      key: 'statistics',
      icon: <BarChartOutlined />,
      label: '统计分析',
      path: '/admin/statistics',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
      path: '/admin/settings',
    },
  ];

  // 模拟数据
  useEffect(() => {
    // 模拟用户信息
    setCurrentUser({
      id: '1',
      name: '管理员',
      email: 'admin@example.com',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
      role: '超级管理员',
      department: '技术部',
    });

    // 模拟通知数据
    const mockNotifications: Notification[] = [
      {
        id: '1',
        title: '系统更新',
        content: '系统将在今晚23:00进行维护更新，预计耗时2小时',
        type: 'info',
        time: '2024-01-15 14:30',
        read: false,
      },
      {
        id: '2',
        title: '新用户注册',
        content: '用户"张三"已成功注册，等待审核',
        type: 'success',
        time: '2024-01-15 13:45',
        read: false,
      },
      {
        id: '3',
        title: '存储空间警告',
        content: '系统存储空间使用率已达85%，请及时清理',
        type: 'warning',
        time: '2024-01-15 12:20',
        read: true,
      },
      {
        id: '4',
        title: '登录异常',
        content: '检测到异常登录尝试，请检查系统安全',
        type: 'error',
        time: '2024-01-15 11:15',
        read: false,
      },
    ];

    setNotifications(mockNotifications);
    setUnreadCount(mockNotifications.filter(n => !n.read).length);
  }, []);

  // 更新面包屑
  useEffect(() => {
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const items = [
      {
        title: (
          <span>
            <HomeOutlined />
            <span style={{ marginLeft: 4 }}>首页</span>
          </span>
        ),
      },
    ];

    pathSegments.forEach((segment, index) => {
      const path = '/' + pathSegments.slice(0, index + 1).join('/');
      const menuItem = findMenuItemByPath(path);
      
      if (menuItem) {
        items.push({
          title: menuItem.label,
        });
      }
    });

    setBreadcrumbItems(items);
  }, [location.pathname]);

  // 查找菜单项
  const findMenuItemByPath = (path: string): MenuItem | null => {
    for (const item of menuItems) {
      if (item.path === path) {
        return item;
      }
      if (item.children) {
        const found = item.children.find(child => child.path === path);
        if (found) return found;
      }
    }
    return null;
  };

  // 获取当前选中的菜单键
  const getSelectedKeys = () => {
    const currentPath = location.pathname;
    const menuItem = findMenuItemByPath(currentPath);
    return menuItem ? [menuItem.key] : [];
  };

  // 菜单点击处理
  const handleMenuClick = ({ key }: { key: string }) => {
    const menuItem = menuItems.find(item => item.key === key);
    if (menuItem) {
      navigate(menuItem.path);
    }
  };

  // 用户菜单
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
      onClick: () => {
        setUserDrawer(true);
      },
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '账户设置',
      onClick: () => {
        navigate('/admin/settings');
      },
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'help',
      icon: <QuestionCircleOutlined />,
      label: '帮助中心',
      onClick: () => {
        window.open('https://help.example.com', '_blank');
      },
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        message.success('已退出登录');
        navigate('/login');
      },
    },
  ];

  // 标记通知为已读
  const markAsRead = (id: string) => {
    setNotifications(prev => 
      prev.map(notification => 
        notification.id === id 
          ? { ...notification, read: true }
          : notification
      )
    );
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  // 标记所有通知为已读
  const markAllAsRead = () => {
    setNotifications(prev => 
      prev.map(notification => ({ ...notification, read: true }))
    );
    setUnreadCount(0);
  };

  // 主题切换
  const toggleTheme = () => {
    setDarkMode(!darkMode);
    message.success(`已切换到${!darkMode ? '暗色' : '亮色'}主题`);
  };

  // 通知类型图标
  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'success':
        return '✅';
      case 'warning':
        return '⚠️';
      case 'error':
        return '❌';
      default:
        return 'ℹ️';
    }
  };

  return (
    <ConfigProvider
      theme={{
        algorithm: darkMode ? theme.darkAlgorithm : theme.defaultAlgorithm,
      }}
    >
      <Layout className={`admin-layout ${darkMode ? 'dark-theme' : ''}`}>
        {/* 侧边栏 */}
        <Sider
          trigger={null}
          collapsible
          collapsed={collapsed}
          width={240}
          className="layout-sider"
        >
          <div className="logo">
            <img 
              src="/logo.svg" 
              alt="Logo" 
              style={{ width: collapsed ? 32 : 40, height: collapsed ? 32 : 40 }}
            />
            {!collapsed && (
              <span className="logo-text">CMS管理后台</span>
            )}
          </div>
          
          <Menu
            theme={darkMode ? 'dark' : 'light'}
            mode="inline"
            selectedKeys={getSelectedKeys()}
            items={menuItems.map(item => ({
              key: item.key,
              icon: item.icon,
              label: item.label,
            }))}
            onClick={handleMenuClick}
            className="layout-menu"
          />
        </Sider>

        {/* 主要内容区域 */}
        <Layout className="layout-main">
          {/* 顶部导航 */}
          <Header className="layout-header" style={{ background: colorBgContainer }}>
            <div className="header-left">
              <Button
                type="text"
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed(!collapsed)}
                className="trigger"
              />
              
              <Breadcrumb items={breadcrumbItems} className="breadcrumb" />
            </div>

            <div className="header-right">
              <Space size="middle">
                {/* 主题切换 */}
                <Button
                  type="text"
                  icon={darkMode ? <SunOutlined /> : <MoonOutlined />}
                  onClick={toggleTheme}
                  title={`切换到${darkMode ? '亮色' : '暗色'}主题`}
                />

                {/* 语言切换 */}
                <Button
                  type="text"
                  icon={<GlobalOutlined />}
                  title="语言切换"
                />

                {/* 帮助 */}
                <Button
                  type="text"
                  icon={<QuestionCircleOutlined />}
                  onClick={() => window.open('https://help.example.com', '_blank')}
                  title="帮助中心"
                />

                {/* 通知 */}
                <Badge count={unreadCount} size="small">
                  <Button
                    type="text"
                    icon={<BellOutlined />}
                    onClick={() => setNotificationDrawer(true)}
                    title="通知"
                  />
                </Badge>

                {/* 用户信息 */}
                <Dropdown
                  menu={{ items: userMenuItems }}
                  placement="bottomRight"
                  arrow
                >
                  <div className="user-info">
                    <Avatar
                      size="small"
                      src={currentUser?.avatar}
                      icon={<UserOutlined />}
                    />
                    <span className="user-name">{currentUser?.name}</span>
                  </div>
                </Dropdown>
              </Space>
            </div>
          </Header>

          {/* 内容区域 */}
          <Content className="layout-content">
            {children}
          </Content>

          {/* 底部 */}
          <Footer className="layout-footer">
            <div className="footer-content">
              <div className="footer-left">
                <Text type="secondary">
                  © 2024 CMS文档管理系统. All rights reserved.
                </Text>
              </div>
              <div className="footer-right">
                <Space split={<Divider type="vertical" />}>
                  <a href="mailto:support@example.com">
                    <MailOutlined /> 技术支持
                  </a>
                  <a href="tel:400-123-4567">
                    <PhoneOutlined /> 400-123-4567
                  </a>
                  <a href="https://github.com/example/cms" target="_blank" rel="noopener noreferrer">
                    <GithubOutlined /> GitHub
                  </a>
                </Space>
              </div>
            </div>
          </Footer>
        </Layout>

        {/* 通知抽屉 */}
        <Drawer
          title={
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>通知中心</span>
              <Button type="link" size="small" onClick={markAllAsRead}>
                全部已读
              </Button>
            </div>
          }
          placement="right"
          onClose={() => setNotificationDrawer(false)}
          open={notificationDrawer}
          width={400}
        >
          <div className="notification-list">
            {notifications.map(notification => (
              <div
                key={notification.id}
                className={`notification-item ${notification.read ? 'read' : 'unread'}`}
                onClick={() => markAsRead(notification.id)}
              >
                <div className="notification-header">
                  <span className="notification-icon">
                    {getNotificationIcon(notification.type)}
                  </span>
                  <span className="notification-title">{notification.title}</span>
                  <span className="notification-time">{notification.time}</span>
                </div>
                <div className="notification-content">
                  {notification.content}
                </div>
                {!notification.read && <div className="unread-dot" />}
              </div>
            ))}
          </div>
        </Drawer>

        {/* 用户信息抽屉 */}
        <Drawer
          title="个人资料"
          placement="right"
          onClose={() => setUserDrawer(false)}
          open={userDrawer}
          width={400}
        >
          {currentUser && (
            <div className="user-profile">
              <div className="profile-header">
                <Avatar size={80} src={currentUser.avatar} icon={<UserOutlined />} />
                <div className="profile-info">
                  <h3>{currentUser.name}</h3>
                  <Text type="secondary">{currentUser.role}</Text>
                </div>
              </div>
              
              <Divider />
              
              <div className="profile-details">
                <div className="detail-item">
                  <Text strong>邮箱：</Text>
                  <Text>{currentUser.email}</Text>
                </div>
                <div className="detail-item">
                  <Text strong>部门：</Text>
                  <Text>{currentUser.department}</Text>
                </div>
                <div className="detail-item">
                  <Text strong>角色：</Text>
                  <Text>{currentUser.role}</Text>
                </div>
              </div>
              
              <Divider />
              
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" block onClick={() => navigate('/admin/settings')}>
                  编辑资料
                </Button>
                <Button block onClick={() => navigate('/admin/settings')}>
                  账户设置
                </Button>
                <Button danger block onClick={() => {
                  message.success('已退出登录');
                  navigate('/login');
                }}>
                  退出登录
                </Button>
              </Space>
            </div>
          )}
        </Drawer>
      </Layout>
    </ConfigProvider>
  );
};

export default AdminLayout;
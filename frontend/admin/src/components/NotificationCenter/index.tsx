import React, { useState, useEffect } from 'react';
import { Badge, Dropdown, List, Button, Typography, Empty, Tabs, Spin } from 'antd';
import { BellOutlined, DeleteOutlined, CheckOutlined, EyeOutlined } from '@ant-design/icons';
import './index.less';

const { Text, Title } = Typography;
const { TabPane } = Tabs;

interface Notification {
  id: string;
  type: 'info' | 'warning' | 'error' | 'success';
  title: string;
  content: string;
  time: string;
  read: boolean;
  category: 'system' | 'document' | 'user' | 'feedback';
}

interface NotificationCenterProps {
  className?: string;
}

const NotificationCenter: React.FC<NotificationCenterProps> = ({ className }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('all');
  const [visible, setVisible] = useState(false);
  
  // 模拟通知数据
  const mockNotifications: Notification[] = [
    {
      id: '1',
      type: 'info',
      title: '新文档发布',
      content: 'API接口文档已更新，请查看最新版本',
      time: '2024-01-15 10:30',
      read: false,
      category: 'document'
    },
    {
      id: '2',
      type: 'warning',
      title: '系统维护通知',
      content: '系统将于今晚22:00-24:00进行维护，请提前保存工作',
      time: '2024-01-15 09:15',
      read: false,
      category: 'system'
    },
    {
      id: '3',
      type: 'success',
      title: '用户注册成功',
      content: '新用户"李四"已成功注册并通过审核',
      time: '2024-01-15 08:45',
      read: true,
      category: 'user'
    },
    {
      id: '4',
      type: 'error',
      title: '文档反馈',
      content: '用户反馈文档内容有误，请及时处理',
      time: '2024-01-14 16:20',
      read: false,
      category: 'feedback'
    },
  ];
  
  // 加载通知
  const loadNotifications = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      setNotifications(mockNotifications);
    } catch (error) {
      console.error('加载通知失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    loadNotifications();
  }, []);
  
  // 获取未读通知数量
  const getUnreadCount = () => {
    return notifications.filter(n => !n.read).length;
  };
  
  // 获取过滤后的通知
  const getFilteredNotifications = () => {
    if (activeTab === 'all') {
      return notifications;
    }
    if (activeTab === 'unread') {
      return notifications.filter(n => !n.read);
    }
    return notifications.filter(n => n.category === activeTab);
  };
  
  // 标记为已读
  const markAsRead = (id: string) => {
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, read: true } : n)
    );
  };
  
  // 删除通知
  const deleteNotification = (id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };
  
  // 全部标记为已读
  const markAllAsRead = () => {
    setNotifications(prev =>
      prev.map(n => ({ ...n, read: true }))
    );
  };
  
  // 清空所有通知
  const clearAll = () => {
    setNotifications([]);
  };
  
  // 获取通知类型图标
  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'info':
        return '🔵';
      case 'warning':
        return '🟡';
      case 'error':
        return '🔴';
      case 'success':
        return '🟢';
      default:
        return '🔵';
    }
  };
  
  // 获取分类名称
  const getCategoryName = (category: string) => {
    switch (category) {
      case 'system':
        return '系统';
      case 'document':
        return '文档';
      case 'user':
        return '用户';
      case 'feedback':
        return '反馈';
      default:
        return '其他';
    }
  };
  
  const filteredNotifications = getFilteredNotifications();
  
  const dropdownContent = (
    <div className="notification-dropdown">
      <div className="notification-header">
        <Title level={5}>通知中心</Title>
        <div className="header-actions">
          <Button
            type="text"
            size="small"
            icon={<CheckOutlined />}
            onClick={markAllAsRead}
            disabled={getUnreadCount() === 0}
          >
            全部已读
          </Button>
          <Button
            type="text"
            size="small"
            icon={<DeleteOutlined />}
            onClick={clearAll}
            disabled={notifications.length === 0}
          >
            清空
          </Button>
        </div>
      </div>
      
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        size="small"
        className="notification-tabs"
      >
        <TabPane tab="全部" key="all" />
        <TabPane tab={`未读(${getUnreadCount()})`} key="unread" />
        <TabPane tab="系统" key="system" />
        <TabPane tab="文档" key="document" />
        <TabPane tab="用户" key="user" />
        <TabPane tab="反馈" key="feedback" />
      </Tabs>
      
      <div className="notification-content">
        {loading ? (
          <div className="notification-loading">
            <Spin size="small" />
            <Text type="secondary">加载中...</Text>
          </div>
        ) : filteredNotifications.length > 0 ? (
          <List
            dataSource={filteredNotifications}
            renderItem={(item) => (
              <List.Item
                className={`notification-item ${!item.read ? 'unread' : ''}`}
                actions={[
                  <Button
                    type="text"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => markAsRead(item.id)}
                    disabled={item.read}
                  />,
                  <Button
                    type="text"
                    size="small"
                    icon={<DeleteOutlined />}
                    onClick={() => deleteNotification(item.id)}
                    danger
                  />
                ]}
              >
                <div className="notification-body">
                  <div className="notification-meta">
                    <span className="type-icon">{getTypeIcon(item.type)}</span>
                    <Text strong className="notification-title">
                      {item.title}
                    </Text>
                    <Text type="secondary" className="notification-category">
                      [{getCategoryName(item.category)}]
                    </Text>
                  </div>
                  <Text type="secondary" className="notification-content">
                    {item.content}
                  </Text>
                  <Text type="secondary" className="notification-time">
                    {item.time}
                  </Text>
                </div>
              </List.Item>
            )}
          />
        ) : (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="暂无通知"
            className="notification-empty"
          />
        )}
      </div>
    </div>
  );
  
  return (
    <Dropdown
      overlay={dropdownContent}
      trigger={['click']}
      placement="bottomRight"
      overlayClassName="notification-dropdown-overlay"
      open={visible}
      onOpenChange={setVisible}
    >
      <Badge count={getUnreadCount()} size="small" className={className}>
        <Button
          type="text"
          icon={<BellOutlined />}
          className="notification-trigger"
        />
      </Badge>
    </Dropdown>
  );
};

export default NotificationCenter;
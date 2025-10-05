import React, { useState } from 'react';
import { Avatar, Dropdown, Menu, Modal, Form, Input, Button, Upload, message, Divider } from 'antd';
import { UserOutlined, SettingOutlined, LogoutOutlined, EditOutlined, CameraOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import './index.less';

interface UserInfo {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  nickname?: string;
  phone?: string;
  department?: string;
  role: string;
}

interface UserProfileProps {
  user: UserInfo;
  onLogout: () => void;
  onUpdateProfile?: (data: Partial<UserInfo>) => void;
  className?: string;
}

const UserProfile: React.FC<UserProfileProps> = ({
  user,
  onLogout,
  onUpdateProfile,
  className
}) => {
  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [settingsModalVisible, setSettingsModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  
  // 处理头像上传
  const handleAvatarUpload = (info: any) => {
    if (info.file.status === 'uploading') {
      setLoading(true);
      return;
    }
    if (info.file.status === 'done') {
      // 获取上传结果
      const avatarUrl = info.file.response?.url || URL.createObjectURL(info.file.originFileObj);
      onUpdateProfile?.({ avatar: avatarUrl });
      setLoading(false);
      message.success('头像上传成功');
    }
    if (info.file.status === 'error') {
      setLoading(false);
      message.error('头像上传失败');
    }
  };
  
  // 上传前验证
  const beforeUpload = (file: File) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
      message.error('只能上传 JPG/PNG 格式的图片!');
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('图片大小不能超过 2MB!');
      return false;
    }
    return true;
  };
  
  // 保存个人资料
  const handleSaveProfile = async (values: any) => {
    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000)); // 模拟API调用
      onUpdateProfile?.(values);
      setProfileModalVisible(false);
      message.success('个人资料更新成功');
    } catch (error) {
      message.error('更新失败，请重试');
    } finally {
      setLoading(false);
    }
  };
  
  // 菜单项
  const menuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
      onClick: () => {
        form.setFieldsValue(user);
        setProfileModalVisible(true);
      }
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '账户设置',
      onClick: () => setSettingsModalVisible(true)
    },
    {
      type: 'divider'
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        Modal.confirm({
          title: '确认退出',
          content: '确定要退出登录吗？',
          onOk: onLogout
        });
      }
    }
  ];
  
  return (
    <>
      <Dropdown
        menu={{ items: menuItems }}
        trigger={['click']}
        placement="bottomRight"
        overlayClassName="user-profile-dropdown"
      >
        <div className={`user-profile-trigger ${className || ''}`}>
          <Avatar
            size={32}
            src={user.avatar}
            icon={<UserOutlined />}
            className="user-avatar"
          />
          <span className="user-name">{user.nickname || user.username}</span>
        </div>
      </Dropdown>
      
      {/* 个人资料模态框 */}
      <Modal
        title="个人资料"
        open={profileModalVisible}
        onCancel={() => setProfileModalVisible(false)}
        footer={null}
        width={500}
        className="profile-modal"
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSaveProfile}
          initialValues={user}
        >
          <div className="avatar-section">
            <div className="avatar-wrapper">
              <Avatar
                size={80}
                src={user.avatar}
                icon={<UserOutlined />}
                className="profile-avatar"
              />
              <Upload
                name="avatar"
                showUploadList={false}
                action="/api/upload/avatar"
                beforeUpload={beforeUpload}
                onChange={handleAvatarUpload}
                className="avatar-upload"
              >
                <Button
                  type="text"
                  icon={<CameraOutlined />}
                  className="avatar-upload-btn"
                  loading={loading}
                >
                  更换头像
                </Button>
              </Upload>
            </div>
          </div>
          
          <Form.Item
            name="nickname"
            label="昵称"
            rules={[{ required: true, message: '请输入昵称' }]}
          >
            <Input placeholder="请输入昵称" />
          </Form.Item>
          
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
          
          <Form.Item
            name="phone"
            label="手机号"
            rules={[
              { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
            ]}
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>
          
          <Form.Item
            name="department"
            label="部门"
          >
            <Input placeholder="请输入部门" />
          </Form.Item>
          
          <Divider />
          
          <Form.Item className="form-actions">
            <Button onClick={() => setProfileModalVisible(false)}>
              取消
            </Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              保存
            </Button>
          </Form.Item>
        </Form>
      </Modal>
      
      {/* 账户设置模态框 */}
      <Modal
        title="账户设置"
        open={settingsModalVisible}
        onCancel={() => setSettingsModalVisible(false)}
        footer={null}
        width={500}
        className="settings-modal"
      >
        <Form layout="vertical">
          <Form.Item label="修改密码">
            <Input.Password placeholder="当前密码" />
          </Form.Item>
          <Form.Item>
            <Input.Password placeholder="新密码" />
          </Form.Item>
          <Form.Item>
            <Input.Password placeholder="确认新密码" />
          </Form.Item>
          
          <Divider />
          
          <Form.Item className="form-actions">
            <Button onClick={() => setSettingsModalVisible(false)}>
              取消
            </Button>
            <Button type="primary">
              保存
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default UserProfile;
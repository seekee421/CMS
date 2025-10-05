import React, { useState } from 'react';
import { Card, Form, Input, Button, Checkbox, Typography, Alert } from 'antd';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import type { RootState } from '../../store';
import { loginStart, loginSuccess, loginFailure } from '../../store/slices/authSlice';
import ApiService from '../../services/api';

const { Title, Text } = Typography;

const Login: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { loading, error } = useSelector((state: RootState) => state.auth);
  const [rememberMe, setRememberMe] = useState(true);

  const onFinish = async (values: { username: string; password: string }) => {
    dispatch(loginStart());
    try {
      // 后端优先登录，失败回退到 mock
      const result = await ApiService.login(values.username, values.password);
      dispatch(loginSuccess(result));
      const from = (location.state as any)?.from?.pathname || '/admin/dashboard';
      navigate(from, { replace: true });
    } catch (e: any) {
      dispatch(loginFailure(e.message || '登录失败'));
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
      <Card style={{ width: 420 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>CMS 管理后台登录</Title>
        {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} />}
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}> 
            <Input placeholder="admin / editor" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}> 
            <Input.Password placeholder="••••••" />
          </Form.Item>
          <Form.Item>
            <Checkbox checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)}>记住我</Checkbox>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
          <Text type="secondary">测试账户：admin / editor（任意密码）</Text>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
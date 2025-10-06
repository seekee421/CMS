import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { useSelector } from 'react-redux';
import type { RootState } from './store';
import AdminLayout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Documents from './pages/Documents';
import Users from './pages/Users';
import Categories from './pages/Categories';
import Statistics from './pages/Statistics';
import Settings from './pages/Settings';
import Login from './pages/Login';
import './App.css';

// 简易鉴权守卫
const RequireAuth: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return <>{children}</>;
};

// 角色守卫：仅允许指定角色访问
const RoleGuard: React.FC<{ allowRoles: string[]; children: React.ReactNode }> = ({ allowRoles, children }) => {
  const roles = useSelector((state: RootState) => state.auth.user?.roles ?? []);
  const location = useLocation();

  const hasAllowRole = Array.isArray(roles) && roles.some(r => allowRoles.includes(r));
  if (!hasAllowRole) {
    return <Navigate to="/admin/dashboard" state={{ from: location }} replace />;
  }
  return <>{children}</>;
};

const App: React.FC = () => {
  const roles = useSelector((state: RootState) => state.auth.user?.roles ?? []);


  return (
    <ConfigProvider locale={zhCN}>
      <AntdApp>
        <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Routes>
          <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
          <Route
            path="/admin"
            element={
              <RequireAuth>
                <AdminLayout />
              </RequireAuth>
            }
          >
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="documents" element={<Documents />} />
            <Route path="categories" element={<Categories />} />
            {/* 管理员专属页面 */}
            <Route
              path="users"
              element={
                <RoleGuard allowRoles={['ROLE_ADMIN']}>
                  <Users />
                </RoleGuard>
              }
            />
            <Route
              path="statistics"
              element={
                <RoleGuard allowRoles={['ROLE_ADMIN']}>
                  <Statistics />
                </RoleGuard>
              }
            />
            <Route
              path="settings"
              element={
                <RoleGuard allowRoles={['ROLE_ADMIN']}>
                  <Settings />
                </RoleGuard>
              }
            />
          </Route>
          <Route path="/login" element={<Login />} />
          <Route path="*" element={<div>404 页面未找到</div>} />
        </Routes>
      </BrowserRouter>
      </AntdApp>
    </ConfigProvider>
  );
};

export default App;
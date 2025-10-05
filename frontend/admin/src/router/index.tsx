import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from '@/components/AdminLayout';
import Dashboard from '@/pages/Dashboard';
import Documents from '@/pages/Documents';
import Users from '@/pages/Users';
import Categories from '@/pages/Categories';
import Statistics from '@/pages/Statistics';
import Settings from '@/pages/Settings';

// 路由组件
export const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AdminLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="documents" element={<Documents />} />
          <Route path="users" element={<Users />} />
          <Route path="categories" element={<Categories />} />
          <Route path="statistics" element={<Statistics />} />
          <Route path="settings" element={<Settings />} />
        </Route>
        <Route path="/login" element={<div>登录页面</div>} />
        <Route path="*" element={<div>404 页面未找到</div>} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
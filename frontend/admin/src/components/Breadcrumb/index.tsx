import React from 'react';
import { Breadcrumb as AntBreadcrumb } from 'antd';
import { Link, useLocation } from 'react-router-dom';
import { HomeOutlined } from '@ant-design/icons';
import './index.less';

interface BreadcrumbItem {
  title: string;
  path?: string;
  icon?: React.ReactNode;
}

interface BreadcrumbProps {
  items?: BreadcrumbItem[];
  separator?: string;
  showHome?: boolean;
}

// 路由到面包屑的映射
const routeToBreadcrumb: Record<string, BreadcrumbItem[]> = {
  '/dashboard': [
    { title: '仪表板', icon: <HomeOutlined /> }
  ],
  '/documents': [
    { title: '文档管理' }
  ],
  '/documents/list': [
    { title: '文档管理', path: '/documents' },
    { title: '文档列表' }
  ],
  '/documents/create': [
    { title: '文档管理', path: '/documents' },
    { title: '创建文档' }
  ],
  '/documents/edit': [
    { title: '文档管理', path: '/documents' },
    { title: '文档列表', path: '/documents/list' },
    { title: '编辑文档' }
  ],
  '/documents/categories': [
    { title: '文档管理', path: '/documents' },
    { title: '分类管理' }
  ],
  '/documents/versions': [
    { title: '文档管理', path: '/documents' },
    { title: '版本管理' }
  ],
  '/users': [
    { title: '用户管理' }
  ],
  '/users/list': [
    { title: '用户管理', path: '/users' },
    { title: '用户列表' }
  ],
  '/users/create': [
    { title: '用户管理', path: '/users' },
    { title: '创建用户' }
  ],
  '/users/edit': [
    { title: '用户管理', path: '/users' },
    { title: '用户列表', path: '/users/list' },
    { title: '编辑用户' }
  ],
  '/users/roles': [
    { title: '用户管理', path: '/users' },
    { title: '角色管理' }
  ],
  '/users/permissions': [
    { title: '用户管理', path: '/users' },
    { title: '权限管理' }
  ],
  '/feedback': [
    { title: '反馈管理' }
  ],
  '/feedback/list': [
    { title: '反馈管理', path: '/feedback' },
    { title: '反馈列表' }
  ],
  '/feedback/statistics': [
    { title: '反馈管理', path: '/feedback' },
    { title: '反馈统计' }
  ],
  '/statistics': [
    { title: '数据统计' }
  ],
  '/statistics/overview': [
    { title: '数据统计', path: '/statistics' },
    { title: '概览统计' }
  ],
  '/statistics/documents': [
    { title: '数据统计', path: '/statistics' },
    { title: '文档统计' }
  ],
  '/statistics/users': [
    { title: '数据统计', path: '/statistics' },
    { title: '用户统计' }
  ],
  '/system': [
    { title: '系统管理' }
  ],
  '/system/settings': [
    { title: '系统管理', path: '/system' },
    { title: '系统设置' }
  ],
  '/system/logs': [
    { title: '系统管理', path: '/system' },
    { title: '系统日志' }
  ],
  '/system/backup': [
    { title: '系统管理', path: '/system' },
    { title: '数据备份' }
  ],
};

const Breadcrumb: React.FC<BreadcrumbProps> = ({
  items,
  separator = '/',
  showHome = true
}) => {
  const location = useLocation();
  
  // 获取当前路径的面包屑
  const getBreadcrumbItems = (): BreadcrumbItem[] => {
    if (items) {
      return items;
    }
    
    const pathname = location.pathname;
    const breadcrumbItems = routeToBreadcrumb[pathname] || [];
    
    // 如果显示首页且当前不在首页，添加首页链接
    if (showHome && pathname !== '/dashboard') {
      return [
        { title: '首页', path: '/dashboard', icon: <HomeOutlined /> },
        ...breadcrumbItems
      ];
    }
    
    return breadcrumbItems;
  };
  
  const breadcrumbItems = getBreadcrumbItems();
  
  if (breadcrumbItems.length === 0) {
    return null;
  }
  
  return (
    <div className="custom-breadcrumb">
      <AntBreadcrumb separator={separator}>
        {breadcrumbItems.map((item, index) => {
          const isLast = index === breadcrumbItems.length - 1;
          
          return (
            <AntBreadcrumb.Item key={index}>
              {item.icon && <span className="breadcrumb-icon">{item.icon}</span>}
              {item.path && !isLast ? (
                <Link to={item.path} className="breadcrumb-link">
                  {item.title}
                </Link>
              ) : (
                <span className={isLast ? 'breadcrumb-current' : ''}>
                  {item.title}
                </span>
              )}
            </AntBreadcrumb.Item>
          );
        })}
      </AntBreadcrumb>
    </div>
  );
};

export default Breadcrumb;
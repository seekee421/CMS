// 共享类型定义
export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  status: 'active' | 'inactive';
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: number;
  title: string;
  content: string;
  categoryId: number;
  authorId: number;
  status: 'draft' | 'published' | 'archived';
  version: string;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: number;
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  level: number;
  sort: number;
  status: 'active' | 'inactive';
  isVisible: boolean;
  icon?: string;
  color?: string;
  createdAt: string;
  updatedAt: string;
}

// 共享常量
export const API_ENDPOINTS = {
  AUTH: '/api/auth',
  USERS: '/api/users',
  DOCUMENTS: '/api/documents',
  CATEGORIES: '/api/categories',
  STATISTICS: '/api/statistics',
  SETTINGS: '/api/settings',
} as const;

export const USER_ROLES = {
  ADMIN: 'admin',
  EDITOR: 'editor',
  VIEWER: 'viewer',
} as const;

export const DOCUMENT_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  ARCHIVED: 'archived',
} as const;

// 共享工具函数
export const formatDate = (date: string | Date): string => {
  return new Date(date).toLocaleDateString('zh-CN');
};

export const formatDateTime = (date: string | Date): string => {
  return new Date(date).toLocaleString('zh-CN');
};

export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
};
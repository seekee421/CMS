// 用户相关类型
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface UserRole {
  id: number;
  name: string;
  description: string;
  permissions: Permission[];
}

export interface Permission {
  id: number;
  name: string;
  resource: string;
  action: string;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// 文档相关类型
export interface Document {
  id: number;
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  category?: Category;
  authorId: number;
  author?: User;
  status: DocumentStatus;
  version: string;
  tags: string[];
  viewCount: number;
  downloadCount: number;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
}

export enum DocumentStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

// 分类相关类型
export interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  parent?: Category;
  children?: Category[];
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// 反馈相关类型
export interface Feedback {
  id: number;
  documentId: number;
  document?: Document;
  type: FeedbackType;
  description: string;
  contactInfo?: string;
  status: FeedbackStatus;
  createdAt: string;
  updatedAt: string;
}

export enum FeedbackType {
  CONTENT_INCORRECT = 'CONTENT_INCORRECT',
  CONTENT_MISSING = 'CONTENT_MISSING',
  DESCRIPTION_UNCLEAR = 'DESCRIPTION_UNCLEAR',
  OTHER_SUGGESTION = 'OTHER_SUGGESTION'
}

export enum FeedbackStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  RESOLVED = 'RESOLVED',
  REJECTED = 'REJECTED'
}

// 统计相关类型
export interface Statistics {
  totalDocuments: number;
  totalUsers: number;
  totalViews: number;
  totalDownloads: number;
  recentActivity: ActivityLog[];
}

export interface ActivityLog {
  id: number;
  userId: number;
  user?: User;
  action: string;
  resource: string;
  resourceId: number;
  details?: string;
  createdAt: string;
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T = any> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// 表单相关类型
export interface LoginForm {
  username: string;
  password: string;
  remember?: boolean;
}

export interface DocumentForm {
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  tags: string[];
  status: DocumentStatus;
}

export interface CategoryForm {
  name: string;
  description?: string;
  parentId?: number;
  sortOrder: number;
  isActive: boolean;
}

export interface UserForm {
  username: string;
  email: string;
  fullName: string;
  password?: string;
  roleId: number;
  status: UserStatus;
}

// 搜索相关类型
export interface SearchParams {
  query?: string;
  categoryId?: number;
  status?: DocumentStatus;
  authorId?: number;
  tags?: string[];
  dateRange?: [string, string];
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface SearchResult {
  documents: Document[];
  total: number;
  suggestions: string[];
  facets: SearchFacets;
}

export interface SearchFacets {
  categories: { id: number; name: string; count: number }[];
  authors: { id: number; name: string; count: number }[];
  tags: { name: string; count: number }[];
  statuses: { status: DocumentStatus; count: number }[];
}

// 路由相关类型
export interface RouteConfig {
  path: string;
  component: React.ComponentType;
  exact?: boolean;
  title?: string;
  icon?: string;
  children?: RouteConfig[];
  permission?: string;
}

// 主题相关类型
export interface ThemeConfig {
  primaryColor: string;
  layout: 'side' | 'top';
  theme: 'light' | 'dark';
  locale: 'zh-CN' | 'en-US';
}

// 应用状态类型
export interface AppState {
  user: User | null;
  theme: ThemeConfig;
  loading: boolean;
  error: string | null;
}
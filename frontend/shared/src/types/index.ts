// 基础类型定义
export interface BaseEntity {
  id: number;
  createdAt: string;
  updatedAt: string;
  createdBy?: number;
  updatedBy?: number;
}

// 用户相关类型
export interface User extends BaseEntity {
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  status: UserStatus;
  roles: Role[];
  lastLoginAt?: string;
  loginCount: number;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  PENDING = 'PENDING'
}

export interface Role extends BaseEntity {
  name: string;
  code: string;
  description?: string;
  permissions: Permission[];
}

export interface Permission extends BaseEntity {
  name: string;
  code: string;
  resource: string;
  action: string;
  description?: string;
}

// 文档相关类型
export interface Document extends BaseEntity {
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  category?: Category;
  status: DocumentStatus;
  version: string;
  tags: string[];
  author: User;
  viewCount: number;
  downloadCount: number;
  isPublic: boolean;
  publishedAt?: string;
  metadata?: DocumentMetadata;
}

export enum DocumentStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED',
  DELETED = 'DELETED'
}

export interface DocumentMetadata {
  fileSize?: number;
  mimeType?: string;
  downloadUrl?: string;
  thumbnailUrl?: string;
  language?: string;
  keywords?: string[];
}

export interface DocumentVersion extends BaseEntity {
  documentId: number;
  version: string;
  content: string;
  changeLog?: string;
  author: User;
}

// 分类相关类型
export interface Category extends BaseEntity {
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  parent?: Category;
  children?: Category[];
  level: number;
  sortOrder: number;
  isActive: boolean;
  documentCount: number;
}

// 评论和反馈类型
export interface Comment extends BaseEntity {
  content: string;
  documentId: number;
  document?: Document;
  author: User;
  parentId?: number;
  parent?: Comment;
  replies?: Comment[];
  status: CommentStatus;
}

export enum CommentStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface DocumentFeedback extends BaseEntity {
  documentId: number;
  document?: Document;
  feedbackType: FeedbackType;
  description: string;
  contactInfo?: string;
  status: FeedbackStatus;
  response?: string;
  respondedBy?: User;
  respondedAt?: string;
}

export enum FeedbackType {
  CONTENT_INCORRECT = 'CONTENT_INCORRECT',
  CONTENT_MISSING = 'CONTENT_MISSING',
  DESCRIPTION_UNCLEAR = 'DESCRIPTION_UNCLEAR',
  OTHER_SUGGESTION = 'OTHER_SUGGESTION'
}

export enum FeedbackStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

// 统计相关类型
export interface DocumentStatistics extends BaseEntity {
  documentId: number;
  viewCount: number;
  downloadCount: number;
  statisticsDate: string;
  timeRange: StatisticsTimeRange;
}

export enum StatisticsTimeRange {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY'
}

export interface SystemStatistics {
  totalUsers: number;
  activeUsers: number;
  totalDocuments: number;
  publishedDocuments: number;
  totalCategories: number;
  totalViews: number;
  totalDownloads: number;
  totalFeedbacks: number;
  pendingFeedbacks: number;
}

// 搜索相关类型
export interface SearchRequest {
  query: string;
  categoryId?: number;
  tags?: string[];
  status?: DocumentStatus;
  authorId?: number;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface SearchResult {
  documents: Document[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  suggestions?: string[];
  facets?: SearchFacet[];
}

export interface SearchFacet {
  field: string;
  values: SearchFacetValue[];
}

export interface SearchFacetValue {
  value: string;
  count: number;
}

export interface SearchHighlight {
  field: string;
  fragments: string[];
}

// 版本相关类型
export interface ProductVersion extends BaseEntity {
  productName: string;
  version: string;
  description?: string;
  isActive: boolean;
  releaseDate: string;
  documentCount: number;
}

// 文件上传类型
export interface FileUpload {
  file: File;
  progress: number;
  status: UploadStatus;
  error?: string;
  url?: string;
}

export enum UploadStatus {
  PENDING = 'PENDING',
  UPLOADING = 'UPLOADING',
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR'
}

// 分页类型
export interface PageRequest {
  page: number;
  size: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface ErrorResponse {
  code: number;
  message: string;
  details?: string;
  timestamp: number;
  path?: string;
}

// 认证相关类型
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

// 表单类型
export interface DocumentForm {
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  tags: string[];
  isPublic: boolean;
  metadata?: Partial<DocumentMetadata>;
}

export interface CategoryForm {
  name: string;
  code: string;
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
  status: UserStatus;
  roleIds: number[];
}

export interface FeedbackForm {
  documentId: number;
  feedbackType: FeedbackType;
  description: string;
  contactInfo?: string;
}

// 配置类型
export interface AppConfig {
  apiBaseUrl: string;
  appTitle: string;
  version: string;
  environment: 'development' | 'staging' | 'production';
  features: {
    enableAnalytics: boolean;
    enableFeedback: boolean;
    enableSearch: boolean;
    enableVersionSelector: boolean;
    enableDownloadTracking: boolean;
  };
  ui: {
    theme: 'light' | 'dark' | 'auto';
    locale: string;
    pageSize: number;
    enableDarkMode: boolean;
  };
  editor: {
    theme: string;
    autoSaveInterval: number;
    maxFileSize: number;
  };
  search: {
    type: 'algolia' | 'local';
    algolia?: {
      appId: string;
      apiKey: string;
      indexName: string;
    };
  };
}

// 事件类型
export interface AppEvent {
  type: string;
  payload?: any;
  timestamp: number;
}

export interface DocumentViewEvent extends AppEvent {
  type: 'document:view';
  payload: {
    documentId: number;
    userId?: number;
    sessionId: string;
  };
}

export interface DocumentDownloadEvent extends AppEvent {
  type: 'document:download';
  payload: {
    documentId: number;
    userId?: number;
    sessionId: string;
  };
}

export interface SearchEvent extends AppEvent {
  type: 'search:query';
  payload: {
    query: string;
    resultCount: number;
    userId?: number;
    sessionId: string;
  };
}

// 导出命名空间，避免与本文件中类型重名导致重复导出错误
export * as apiTypes from './api';
export * as uiTypes from './ui';
export * as utilTypes from './utils';
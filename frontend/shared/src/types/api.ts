// API请求和响应类型

// 基础API响应
export interface BaseApiResponse {
  code: number;
  message: string;
  timestamp: number;
}

export interface ApiResponse<T = any> extends BaseApiResponse {
  data: T;
}

export interface ApiError extends BaseApiResponse {
  details?: string;
  path?: string;
  errors?: ValidationError[];
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: any;
}

// 分页相关
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
  sort: SortInfo;
}

export interface SortInfo {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

// 文档API类型
export interface DocumentListRequest extends PageRequest {
  categoryId?: number;
  status?: string;
  authorId?: number;
  keyword?: string;
  tags?: string[];
  dateFrom?: string;
  dateTo?: string;
}

export interface DocumentCreateRequest {
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  tags: string[];
  isPublic: boolean;
  metadata?: Record<string, any>;
}

export interface DocumentUpdateRequest extends Partial<DocumentCreateRequest> {
  id: number;
}

export interface DocumentBatchRequest {
  ids: number[];
  action: 'publish' | 'archive' | 'delete';
}

// 用户API类型
export interface UserListRequest extends PageRequest {
  status?: string;
  roleId?: number;
  keyword?: string;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  fullName: string;
  password: string;
  status: string;
  roleIds: number[];
}

export interface UserUpdateRequest extends Partial<Omit<UserCreateRequest, 'password'>> {
  id: number;
  password?: string;
}

export interface UserPasswordChangeRequest {
  id: number;
  oldPassword: string;
  newPassword: string;
}

// 分类API类型
export interface CategoryListRequest {
  parentId?: number;
  isActive?: boolean;
  keyword?: string;
}

export interface CategoryCreateRequest {
  name: string;
  code: string;
  description?: string;
  parentId?: number;
  sortOrder: number;
  isActive: boolean;
}

export interface CategoryUpdateRequest extends Partial<CategoryCreateRequest> {
  id: number;
}

export interface CategoryMoveRequest {
  id: number;
  targetParentId?: number;
  targetPosition: number;
}

// 搜索API类型
export interface SearchRequest {
  query: string;
  categoryId?: number;
  tags?: string[];
  status?: string;
  authorId?: number;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  highlight?: boolean;
}

export interface SearchResponse {
  documents: SearchDocumentResult[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  suggestions?: string[];
  facets?: SearchFacet[];
  took: number;
}

export interface SearchDocumentResult {
  id: number;
  title: string;
  content: string;
  summary?: string;
  categoryName: string;
  authorName: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  highlights?: SearchHighlight[];
  score: number;
}

export interface SearchHighlight {
  field: string;
  fragments: string[];
}

export interface SearchFacet {
  field: string;
  values: SearchFacetValue[];
}

export interface SearchFacetValue {
  value: string;
  count: number;
  selected?: boolean;
}

export interface SearchSuggestionRequest {
  query: string;
  size?: number;
}

export interface SearchSuggestionResponse {
  suggestions: string[];
}

// 反馈API类型
export interface FeedbackCreateRequest {
  documentId: number;
  feedbackType: string;
  description: string;
  contactInfo?: string;
}

export interface FeedbackListRequest extends PageRequest {
  documentId?: number;
  feedbackType?: string;
  status?: string;
  dateFrom?: string;
  dateTo?: string;
}

export interface FeedbackUpdateRequest {
  id: number;
  status: string;
  response?: string;
}

// 统计API类型
export interface StatisticsRequest {
  dateFrom?: string;
  dateTo?: string;
  timeRange?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  documentId?: number;
  categoryId?: number;
}

export interface DocumentStatisticsResponse {
  documentId: number;
  title: string;
  viewCount: number;
  downloadCount: number;
  feedbackCount: number;
  averageRating?: number;
  trend: StatisticsTrend;
}

export interface StatisticsTrend {
  viewTrend: number;
  downloadTrend: number;
  period: string;
}

export interface SystemStatisticsResponse {
  totalUsers: number;
  activeUsers: number;
  totalDocuments: number;
  publishedDocuments: number;
  totalCategories: number;
  totalViews: number;
  totalDownloads: number;
  totalFeedbacks: number;
  pendingFeedbacks: number;
  chartData: ChartDataPoint[];
}

export interface ChartDataPoint {
  date: string;
  views: number;
  downloads: number;
  users: number;
}

// 版本API类型
export interface VersionListRequest extends PageRequest {
  productName?: string;
  isActive?: boolean;
}

export interface VersionCreateRequest {
  productName: string;
  version: string;
  description?: string;
  isActive: boolean;
  releaseDate: string;
}

export interface VersionUpdateRequest extends Partial<VersionCreateRequest> {
  id: number;
}

export interface DocumentVersionListRequest extends PageRequest {
  documentId: number;
}

export interface DocumentVersionCompareRequest {
  documentId: number;
  fromVersion: string;
  toVersion: string;
}

export interface DocumentVersionCompareResponse {
  documentId: number;
  fromVersion: string;
  toVersion: string;
  differences: VersionDifference[];
}

export interface VersionDifference {
  type: 'added' | 'removed' | 'modified';
  lineNumber: number;
  content: string;
  oldContent?: string;
}

// 文件上传API类型
export interface FileUploadRequest {
  file: File;
  category?: string;
  description?: string;
}

export interface FileUploadResponse {
  id: number;
  filename: string;
  originalName: string;
  size: number;
  mimeType: string;
  url: string;
  thumbnailUrl?: string;
  uploadedAt: string;
}

export interface FileListRequest extends PageRequest {
  category?: string;
  mimeType?: string;
  keyword?: string;
  dateFrom?: string;
  dateTo?: string;
}

// 认证API类型
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
  captcha?: string;
  captchaId?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
  expiresIn: number;
  permissions: string[];
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  roles: RoleInfo[];
  lastLoginAt?: string;
}

export interface RoleInfo {
  id: number;
  name: string;
  code: string;
  permissions: string[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LogoutRequest {
  refreshToken?: string;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirmRequest {
  token: string;
  newPassword: string;
}

// 系统配置API类型
export interface SystemConfigResponse {
  appName: string;
  appVersion: string;
  apiVersion: string;
  features: FeatureConfig;
  limits: SystemLimits;
  ui: UIConfig;
}

export interface FeatureConfig {
  enableRegistration: boolean;
  enableFeedback: boolean;
  enableAnalytics: boolean;
  enableSearch: boolean;
  enableVersioning: boolean;
  enableMultiLanguage: boolean;
}

export interface SystemLimits {
  maxFileSize: number;
  maxFilesPerUpload: number;
  maxDocumentSize: number;
  maxCategoriesPerDocument: number;
  maxTagsPerDocument: number;
  sessionTimeout: number;
}

export interface UIConfig {
  defaultTheme: string;
  availableThemes: string[];
  defaultLocale: string;
  availableLocales: LocaleInfo[];
  defaultPageSize: number;
  availablePageSizes: number[];
}

export interface LocaleInfo {
  code: string;
  name: string;
  nativeName: string;
  flag?: string;
}

// 导出类型
export type ApiMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export interface ApiEndpoint {
  method: ApiMethod;
  path: string;
  description?: string;
}

// API端点常量
export const API_ENDPOINTS = {
  // 认证
  AUTH: {
    LOGIN: { method: 'POST' as const, path: '/auth/login' },
    LOGOUT: { method: 'POST' as const, path: '/auth/logout' },
    REFRESH: { method: 'POST' as const, path: '/auth/refresh' },
    PROFILE: { method: 'GET' as const, path: '/auth/profile' },
    PASSWORD_RESET: { method: 'POST' as const, path: '/auth/password-reset' },
    PASSWORD_RESET_CONFIRM: { method: 'POST' as const, path: '/auth/password-reset-confirm' },
  },
  
  // 用户管理
  USERS: {
    LIST: { method: 'GET' as const, path: '/users' },
    CREATE: { method: 'POST' as const, path: '/users' },
    GET: { method: 'GET' as const, path: '/users/{id}' },
    UPDATE: { method: 'PUT' as const, path: '/users/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/users/{id}' },
    CHANGE_PASSWORD: { method: 'PUT' as const, path: '/users/{id}/password' },
  },
  
  // 文档管理
  DOCUMENTS: {
    LIST: { method: 'GET' as const, path: '/documents' },
    CREATE: { method: 'POST' as const, path: '/documents' },
    GET: { method: 'GET' as const, path: '/documents/{id}' },
    UPDATE: { method: 'PUT' as const, path: '/documents/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/documents/{id}' },
    BATCH: { method: 'POST' as const, path: '/documents/batch' },
    VERSIONS: { method: 'GET' as const, path: '/documents/{id}/versions' },
    VERSION_COMPARE: { method: 'POST' as const, path: '/documents/{id}/versions/compare' },
  },
  
  // 分类管理
  CATEGORIES: {
    LIST: { method: 'GET' as const, path: '/categories' },
    TREE: { method: 'GET' as const, path: '/categories/tree' },
    CREATE: { method: 'POST' as const, path: '/categories' },
    GET: { method: 'GET' as const, path: '/categories/{id}' },
    UPDATE: { method: 'PUT' as const, path: '/categories/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/categories/{id}' },
    MOVE: { method: 'PUT' as const, path: '/categories/{id}/move' },
  },
  
  // 搜索
  SEARCH: {
    DOCUMENTS: { method: 'GET' as const, path: '/search/documents' },
    SUGGESTIONS: { method: 'GET' as const, path: '/search/suggestions' },
  },
  
  // 反馈
  FEEDBACK: {
    LIST: { method: 'GET' as const, path: '/feedback' },
    CREATE: { method: 'POST' as const, path: '/feedback' },
    GET: { method: 'GET' as const, path: '/feedback/{id}' },
    UPDATE: { method: 'PUT' as const, path: '/feedback/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/feedback/{id}' },
  },
  
  // 统计
  STATISTICS: {
    SYSTEM: { method: 'GET' as const, path: '/statistics/system' },
    DOCUMENTS: { method: 'GET' as const, path: '/statistics/documents' },
    TRACK_VIEW: { method: 'POST' as const, path: '/statistics/track/view' },
    TRACK_DOWNLOAD: { method: 'POST' as const, path: '/statistics/track/download' },
  },
  
  // 文件管理
  FILES: {
    LIST: { method: 'GET' as const, path: '/files' },
    UPLOAD: { method: 'POST' as const, path: '/files/upload' },
    GET: { method: 'GET' as const, path: '/files/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/files/{id}' },
    DOWNLOAD: { method: 'GET' as const, path: '/files/{id}/download' },
  },
  
  // 版本管理
  VERSIONS: {
    LIST: { method: 'GET' as const, path: '/versions' },
    CREATE: { method: 'POST' as const, path: '/versions' },
    GET: { method: 'GET' as const, path: '/versions/{id}' },
    UPDATE: { method: 'PUT' as const, path: '/versions/{id}' },
    DELETE: { method: 'DELETE' as const, path: '/versions/{id}' },
  },
  
  // 系统配置
  SYSTEM: {
    CONFIG: { method: 'GET' as const, path: '/system/config' },
    HEALTH: { method: 'GET' as const, path: '/system/health' },
    INFO: { method: 'GET' as const, path: '/system/info' },
  },
} as const;
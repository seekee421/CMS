// 应用常量定义

// API相关常量
export const API_CONSTANTS = {
  BASE_URL: (((import.meta as any)?.env?.REACT_APP_API_BASE_URL) ?? ((globalThis as any)?.process?.env?.REACT_APP_API_BASE_URL) ?? 'http://localhost:8080'),
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
} as const;

// 存储相关常量
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'cms_auth_token',
  REFRESH_TOKEN: 'cms_refresh_token',
  USER_INFO: 'cms_user_info',
  THEME: 'cms_theme',
  LANGUAGE: 'cms_language',
  SIDEBAR_COLLAPSED: 'cms_sidebar_collapsed',
  RECENT_DOCUMENTS: 'cms_recent_documents',
  SEARCH_HISTORY: 'cms_search_history',
  EDITOR_SETTINGS: 'cms_editor_settings',
} as const;

// 文件相关常量
export const FILE_CONSTANTS = {
  MAX_SIZE: 20 * 1024 * 1024, // 20MB
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  ALLOWED_DOCUMENT_TYPES: [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'text/plain',
    'text/markdown',
  ],
  ALLOWED_VIDEO_TYPES: ['video/mp4', 'video/webm', 'video/ogg'],
  CHUNK_SIZE: 1024 * 1024, // 1MB chunks for upload
} as const;

// 分页相关常量
export const PAGINATION_CONSTANTS = {
  DEFAULT_PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
  MAX_PAGE_SIZE: 100,
} as const;

// 搜索相关常量
export const SEARCH_CONSTANTS = {
  MIN_QUERY_LENGTH: 2,
  MAX_QUERY_LENGTH: 100,
  DEBOUNCE_DELAY: 300,
  MAX_SUGGESTIONS: 10,
  MAX_HISTORY_ITEMS: 20,
} as const;

// 编辑器相关常量
export const EDITOR_CONSTANTS = {
  AUTO_SAVE_INTERVAL: 30000, // 30 seconds
  SUPPORTED_LANGUAGES: [
    'markdown',
    'javascript',
    'typescript',
    'java',
    'python',
    'sql',
    'shell',
    'json',
    'xml',
    'html',
    'css',
    'yaml',
  ],
  DEFAULT_THEME: 'vs-dark',
  THEMES: ['vs', 'vs-dark', 'hc-black'],
} as const;

// 主题相关常量
export const THEME_CONSTANTS = {
  MODES: ['light', 'dark', 'auto'],
  DEFAULT_MODE: 'light',
  STORAGE_KEY: 'cms_theme_mode',
} as const;

// 语言相关常量
export const LANGUAGE_CONSTANTS = {
  DEFAULT_LOCALE: 'zh-CN',
  SUPPORTED_LOCALES: ['zh-CN', 'en-US'],
  FALLBACK_LOCALE: 'en-US',
} as const;

// 缓存相关常量
export const CACHE_CONSTANTS = {
  DEFAULT_TTL: 5 * 60 * 1000, // 5 minutes
  MAX_SIZE: 100,
  STRATEGIES: ['lru', 'fifo', 'lfu'],
  DEFAULT_STRATEGY: 'lru',
} as const;

// 日志相关常量
export const LOG_CONSTANTS = {
  LEVELS: ['trace', 'debug', 'info', 'warn', 'error', 'fatal'],
  DEFAULT_LEVEL: 'info',
  MAX_ENTRIES: 1000,
} as const;

// 验证相关常量
export const VALIDATION_CONSTANTS = {
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE_REGEX: /^1[3-9]\d{9}$/,
  PASSWORD_MIN_LENGTH: 8,
  PASSWORD_MAX_LENGTH: 128,
  USERNAME_MIN_LENGTH: 3,
  USERNAME_MAX_LENGTH: 50,
  TITLE_MAX_LENGTH: 200,
  DESCRIPTION_MAX_LENGTH: 2000,
  CONTENT_MAX_LENGTH: 1000000, // 1MB
} as const;

// 状态相关常量
export const STATUS_CONSTANTS = {
  DOCUMENT_STATUS: ['draft', 'published', 'archived'],
  USER_STATUS: ['active', 'inactive', 'suspended'],
  COMMENT_STATUS: ['pending', 'approved', 'rejected'],
  FEEDBACK_STATUS: ['open', 'in_progress', 'resolved', 'closed'],
} as const;

// 权限相关常量
export const PERMISSION_CONSTANTS = {
  ACTIONS: ['create', 'read', 'update', 'delete'],
  RESOURCES: ['document', 'category', 'user', 'comment', 'feedback', 'statistics'],
  ROLES: ['admin', 'editor', 'viewer'],
} as const;

// 通知相关常量
export const NOTIFICATION_CONSTANTS = {
  TYPES: ['success', 'info', 'warning', 'error'],
  DEFAULT_DURATION: 4500,
  MAX_NOTIFICATIONS: 5,
  POSITIONS: ['topLeft', 'topRight', 'bottomLeft', 'bottomRight'],
  DEFAULT_POSITION: 'topRight',
} as const;

// 动画相关常量
export const ANIMATION_CONSTANTS = {
  DURATION: {
    FAST: 150,
    NORMAL: 300,
    SLOW: 500,
  },
  EASING: {
    EASE: 'ease',
    EASE_IN: 'ease-in',
    EASE_OUT: 'ease-out',
    EASE_IN_OUT: 'ease-in-out',
    LINEAR: 'linear',
  },
} as const;

// 布局相关常量
export const LAYOUT_CONSTANTS = {
  HEADER_HEIGHT: 64,
  SIDEBAR_WIDTH: 256,
  SIDEBAR_COLLAPSED_WIDTH: 80,
  FOOTER_HEIGHT: 48,
  CONTENT_PADDING: 24,
} as const;

// 响应式断点常量
export const BREAKPOINT_CONSTANTS = {
  XS: 480,
  SM: 576,
  MD: 768,
  LG: 992,
  XL: 1200,
  XXL: 1600,
} as const;

// 图表相关常量
export const CHART_CONSTANTS = {
  COLORS: [
    '#1890ff',
    '#52c41a',
    '#faad14',
    '#f5222d',
    '#722ed1',
    '#fa8c16',
    '#13c2c2',
    '#eb2f96',
    '#a0d911',
    '#fa541c',
  ],
  DEFAULT_HEIGHT: 400,
  ANIMATION_DURATION: 1000,
} as const;

// 错误代码常量
export const ERROR_CODES = {
  // 通用错误
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR',
  
  // 认证错误
  UNAUTHORIZED: 'UNAUTHORIZED',
  FORBIDDEN: 'FORBIDDEN',
  TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  
  // 验证错误
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  REQUIRED_FIELD: 'REQUIRED_FIELD',
  INVALID_FORMAT: 'INVALID_FORMAT',
  
  // 业务错误
  RESOURCE_NOT_FOUND: 'RESOURCE_NOT_FOUND',
  RESOURCE_ALREADY_EXISTS: 'RESOURCE_ALREADY_EXISTS',
  OPERATION_NOT_ALLOWED: 'OPERATION_NOT_ALLOWED',
  
  // 文件错误
  FILE_TOO_LARGE: 'FILE_TOO_LARGE',
  INVALID_FILE_TYPE: 'INVALID_FILE_TYPE',
  UPLOAD_FAILED: 'UPLOAD_FAILED',
} as const;

// HTTP状态码常量
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
} as const;

// 正则表达式常量
export const REGEX_PATTERNS = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^1[3-9]\d{9}$/,
  URL: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/,
  IPV4: /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
  MAC_ADDRESS: /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/,
  HEX_COLOR: /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/,
  SLUG: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
  VERSION: /^\d+\.\d+\.\d+(?:-[a-zA-Z0-9]+)?$/,
} as const;

// 日期格式常量
export const DATE_FORMATS = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  DATETIME_WITH_TIMEZONE: 'YYYY-MM-DD HH:mm:ss Z',
  ISO: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
  DISPLAY_DATE: 'YYYY年MM月DD日',
  DISPLAY_DATETIME: 'YYYY年MM月DD日 HH:mm',
  RELATIVE: 'relative',
} as const;

// 键盘快捷键常量
export const KEYBOARD_SHORTCUTS = {
  SAVE: 'Ctrl+S',
  COPY: 'Ctrl+C',
  PASTE: 'Ctrl+V',
  CUT: 'Ctrl+X',
  UNDO: 'Ctrl+Z',
  REDO: 'Ctrl+Y',
  FIND: 'Ctrl+F',
  REPLACE: 'Ctrl+H',
  NEW: 'Ctrl+N',
  OPEN: 'Ctrl+O',
  PRINT: 'Ctrl+P',
  REFRESH: 'F5',
  FULLSCREEN: 'F11',
  ESCAPE: 'Escape',
} as const;

// 导出所有常量
export const CONSTANTS = {
  API: API_CONSTANTS,
  STORAGE: STORAGE_KEYS,
  FILE: FILE_CONSTANTS,
  PAGINATION: PAGINATION_CONSTANTS,
  SEARCH: SEARCH_CONSTANTS,
  EDITOR: EDITOR_CONSTANTS,
  THEME: THEME_CONSTANTS,
  LANGUAGE: LANGUAGE_CONSTANTS,
  CACHE: CACHE_CONSTANTS,
  LOG: LOG_CONSTANTS,
  VALIDATION: VALIDATION_CONSTANTS,
  STATUS: STATUS_CONSTANTS,
  PERMISSION: PERMISSION_CONSTANTS,
  NOTIFICATION: NOTIFICATION_CONSTANTS,
  ANIMATION: ANIMATION_CONSTANTS,
  LAYOUT: LAYOUT_CONSTANTS,
  BREAKPOINT: BREAKPOINT_CONSTANTS,
  CHART: CHART_CONSTANTS,
  ERROR_CODES,
  HTTP_STATUS,
  REGEX_PATTERNS,
  DATE_FORMATS,
  KEYBOARD_SHORTCUTS,
} as const;
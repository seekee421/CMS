# 共享代码库设计

## 概述

共享代码库包含管理后台和文档门户共同使用的代码，包括API客户端、类型定义、工具函数等。

## 目录结构

```
shared/
├── src/
│   ├── api/                 # API客户端
│   │   ├── client.ts        # HTTP客户端配置
│   │   ├── auth.ts          # 认证相关API
│   │   ├── users.ts         # 用户管理API
│   │   ├── roles.ts         # 角色管理API
│   │   ├── permissions.ts   # 权限管理API
│   │   ├── documents.ts     # 文档管理API
│   │   ├── categories.ts    # 分类管理API
│   │   ├── audit.ts         # 审计日志API
│   │   ├── cache.ts         # 缓存管理API
│   │   ├── backup.ts        # 备份管理API
│   │   └── index.ts         # API导出
│   ├── types/               # TypeScript类型定义
│   │   ├── auth.ts          # 认证相关类型
│   │   ├── user.ts          # 用户相关类型
│   │   ├── role.ts          # 角色相关类型
│   │   ├── permission.ts    # 权限相关类型
│   │   ├── document.ts      # 文档相关类型
│   │   ├── category.ts      # 分类相关类型
│   │   ├── audit.ts         # 审计相关类型
│   │   ├── common.ts        # 通用类型
│   │   └── index.ts         # 类型导出
│   ├── utils/               # 工具函数
│   │   ├── auth.ts          # 认证工具
│   │   ├── storage.ts       # 存储工具
│   │   ├── format.ts        # 格式化工具
│   │   ├── validation.ts    # 验证工具
│   │   ├── constants.ts     # 常量定义
│   │   └── index.ts         # 工具导出
│   ├── hooks/               # 共享React Hooks
│   │   ├── useAuth.ts       # 认证Hook
│   │   ├── useApi.ts        # API调用Hook
│   │   ├── useLocalStorage.ts # 本地存储Hook
│   │   └── index.ts         # Hook导出
│   └── index.ts             # 主导出文件
├── package.json
├── tsconfig.json
└── README.md
```

## API客户端设计

### HTTP客户端配置

```typescript
// src/api/client.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { getAuthToken, removeAuthToken } from '../utils/auth';

export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  code?: string;
}

class ApiClient {
  private instance: AxiosInstance;

  constructor(baseURL: string = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080') {
    this.instance = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        const token = getAuthToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeAuthToken();
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.instance.get<T>(url, config);
    return response.data;
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.instance.post<T>(url, data, config);
    return response.data;
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.instance.put<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.instance.delete<T>(url, config);
    return response.data;
  }
}

export const apiClient = new ApiClient();
```

### 认证API

```typescript
// src/api/auth.ts
import { apiClient } from './client';
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../types';

export const authApi = {
  login: (credentials: LoginRequest): Promise<LoginResponse> =>
    apiClient.post('/api/auth/login', credentials),

  register: (userData: RegisterRequest): Promise<User> =>
    apiClient.post('/api/auth/register', userData),

  logout: (): Promise<void> =>
    apiClient.get('/api/auth/logout'),

  refreshToken: (): Promise<LoginResponse> =>
    apiClient.get('/api/auth/refresh'),

  getCurrentUser: (): Promise<User> =>
    apiClient.get('/api/auth/me'),
};
```

### 用户管理API

```typescript
// src/api/users.ts
import { apiClient } from './client';
import { User, CreateUserRequest, UpdateUserRequest, PaginatedResponse } from '../types';

export const usersApi = {
  getUsers: (params?: {
    page?: number;
    size?: number;
    search?: string;
  }): Promise<PaginatedResponse<User>> =>
    apiClient.get('/api/users', { params }),

  getUserById: (id: number): Promise<User> =>
    apiClient.get(`/api/users/${id}`),

  createUser: (userData: CreateUserRequest): Promise<User> =>
    apiClient.post('/api/users', userData),

  updateUser: (id: number, userData: UpdateUserRequest): Promise<User> =>
    apiClient.put(`/api/users/${id}`, userData),

  updateUserRoles: (id: number, roleNames: string[]): Promise<User> =>
    apiClient.put(`/api/users/${id}/roles`, roleNames),

  deleteUser: (id: number): Promise<void> =>
    apiClient.delete(`/api/users/${id}`),
};
```

## 类型定义

### 通用类型

```typescript
// src/types/common.ts
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  message: string;
  code?: string;
  details?: any;
}

export interface SelectOption {
  label: string;
  value: string | number;
  disabled?: boolean;
}

export interface TreeNode {
  key: string | number;
  title: string;
  children?: TreeNode[];
  disabled?: boolean;
  selectable?: boolean;
}
```

### 用户相关类型

```typescript
// src/types/user.ts
export interface User {
  id: number;
  username: string;
  email: string;
  status: UserStatus;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
}

export interface CreateUserRequest {
  username: string;
  password: string;
  email: string;
  roleNames?: string[];
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  status?: UserStatus;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  token: string;
  user: User;
  expiresIn: number;
}
```

### 文档相关类型

```typescript
// src/types/document.ts
export interface Document {
  id: number;
  title: string;
  content: string;
  status: DocumentStatus;
  isPublic: boolean;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
  category?: DocumentCategory;
  tags: string[];
  version: string;
  sourceUrl?: string;
  originalId?: string;
  migrationStatus?: MigrationStatus;
  migrationDate?: string;
}

export enum DocumentStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED',
}

export enum MigrationStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface CreateDocumentRequest {
  title: string;
  content: string;
  categoryId?: number;
  tags?: string[];
  isPublic?: boolean;
}

export interface UpdateDocumentRequest {
  title?: string;
  content?: string;
  categoryId?: number;
  tags?: string[];
  isPublic?: boolean;
  status?: DocumentStatus;
}
```

## 工具函数

### 认证工具

```typescript
// src/utils/auth.ts
const TOKEN_KEY = 'cms_auth_token';
const USER_KEY = 'cms_user_info';

export const getAuthToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

export const setAuthToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

export const removeAuthToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

export const isAuthenticated = (): boolean => {
  const token = getAuthToken();
  if (!token) return false;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
};

export const getUserInfo = (): any | null => {
  const userInfo = localStorage.getItem(USER_KEY);
  return userInfo ? JSON.parse(userInfo) : null;
};

export const setUserInfo = (user: any): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
};
```

### 格式化工具

```typescript
// src/utils/format.ts
export const formatDate = (date: string | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
  // 使用dayjs或date-fns实现日期格式化
  return new Date(date).toLocaleString();
};

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
```

## React Hooks

### 认证Hook

```typescript
// src/hooks/useAuth.ts
import { useState, useEffect, useContext, createContext } from 'react';
import { User } from '../types';
import { authApi } from '../api';
import { getAuthToken, setAuthToken, removeAuthToken, getUserInfo, setUserInfo } from '../utils/auth';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = getAuthToken();
      if (token) {
        try {
          const userInfo = getUserInfo();
          if (userInfo) {
            setUser(userInfo);
          } else {
            const currentUser = await authApi.getCurrentUser();
            setUser(currentUser);
            setUserInfo(currentUser);
          }
        } catch (error) {
          removeAuthToken();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (username: string, password: string) => {
    const response = await authApi.login({ username, password });
    setAuthToken(response.token);
    setUser(response.user);
    setUserInfo(response.user);
  };

  const logout = () => {
    removeAuthToken();
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

## 构建配置

### package.json

```json
{
  "name": "@cms/shared",
  "version": "1.0.0",
  "description": "CMS前端共享代码库",
  "main": "dist/index.js",
  "types": "dist/index.d.ts",
  "scripts": {
    "build": "tsc",
    "dev": "tsc --watch",
    "lint": "eslint src --ext .ts,.tsx",
    "type-check": "tsc --noEmit"
  },
  "dependencies": {
    "axios": "^1.6.0",
    "react": "^18.2.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "typescript": "^5.0.0",
    "eslint": "^8.0.0"
  },
  "peerDependencies": {
    "react": ">=18.0.0"
  }
}
```

### TypeScript配置

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["DOM", "DOM.Iterable", "ES6"],
    "allowJs": true,
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "module": "ESNext",
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": false,
    "declaration": true,
    "outDir": "dist",
    "jsx": "react-jsx"
  },
  "include": ["src"],
  "exclude": ["node_modules", "dist"]
}
```

## 使用示例

### 在管理后台中使用

```typescript
// admin/src/pages/Users/index.tsx
import { useQuery, useMutation } from '@tanstack/react-query';
import { usersApi, User } from '@cms/shared';

const UsersPage = () => {
  const { data: users, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => usersApi.getUsers(),
  });

  const createUserMutation = useMutation({
    mutationFn: usersApi.createUser,
    onSuccess: () => {
      // 刷新用户列表
    },
  });

  // 组件实现...
};
```

### 在文档门户中使用

```typescript
// docs-portal/src/components/DocumentViewer.tsx
import { documentsApi, Document } from '@cms/shared';

const DocumentViewer = ({ documentId }: { documentId: string }) => {
  const [document, setDocument] = useState<Document | null>(null);

  useEffect(() => {
    documentsApi.getDocumentById(Number(documentId))
      .then(setDocument)
      .catch(console.error);
  }, [documentId]);

  // 组件实现...
};
```
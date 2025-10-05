// 简化的API服务文件
export interface SimpleUser {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  role: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface SimpleDocument {
  id: number;
  title: string;
  content: string;
  summary?: string;
  categoryId: number;
  authorId: number;
  status: string;
  version: string;
  tags: string[];
  viewCount: number;
  downloadCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface SimpleCategory {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SimpleStatistics {
  totalDocuments: number;
  totalUsers: number;
  totalCategories: number;
  totalViews: number;
  todayViews: number;
  weeklyViews: number;
  monthlyViews: number;
  popularDocuments: Array<{ id: number; title: string; views: number }>;
  recentActivities: Array<{ 
    id: number; 
    type: string; 
    description: string; 
    time: string; 
  }>;
}

// 登录结果类型（前后端通用结构）
export interface LoginResult {
  user: {
    id: string;
    username: string;
    email?: string;
    avatar?: string;
    roles: string[];
    permissions: string[];
  };
  token: string;
  refreshToken: string;
  expiresAt: string;
}

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// 模拟数据
const mockUsers: SimpleUser[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    fullName: '管理员',
    role: 'admin',
    status: 'active',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: 2,
    username: 'editor',
    email: 'editor@example.com',
    fullName: '编辑员',
    role: 'editor',
    status: 'active',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=editor',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z'
  }
];

const mockDocuments: SimpleDocument[] = [
  {
    id: 1,
    title: '快速开始指南',
    content: '# 快速开始指南\n\n这是一个快速开始指南...',
    summary: '帮助用户快速上手系统',
    categoryId: 1,
    authorId: 1,
    status: 'published',
    version: '1.0',
    tags: ['指南', '入门'],
    viewCount: 1250,
    downloadCount: 89,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 2,
    title: 'API 参考文档',
    content: '# API 参考文档\n\n## 概述\n\n本文档描述了系统的API接口...',
    summary: '系统API接口的详细说明',
    categoryId: 2,
    authorId: 1,
    status: 'published',
    version: '2.1',
    tags: ['API', '开发'],
    viewCount: 2340,
    downloadCount: 156,
    createdAt: '2024-01-05T00:00:00Z',
    updatedAt: '2024-01-20T00:00:00Z'
  }
];

const mockCategories: SimpleCategory[] = [
  {
    id: 1,
    name: '用户指南',
    description: '面向最终用户的使用指南',
    parentId: undefined,
    sortOrder: 1,
    isActive: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: 2,
    name: '开发文档',
    description: '面向开发者的技术文档',
    parentId: undefined,
    sortOrder: 2,
    isActive: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }
];

const mockStatistics: SimpleStatistics = {
  totalDocuments: 156,
  totalUsers: 89,
  totalCategories: 12,
  totalViews: 45678,
  todayViews: 234,
  weeklyViews: 1567,
  monthlyViews: 8901,
  popularDocuments: [
    { id: 1, title: 'API 参考文档', views: 2340 },
    { id: 2, title: '快速开始指南', views: 1250 },
    { id: 3, title: '部署指南', views: 890 }
  ],
  recentActivities: [
    { id: 1, type: 'create', description: '创建了新文档《安装指南》', time: '2024-01-20T10:30:00Z' },
    { id: 2, type: 'update', description: '更新了文档《API 参考》', time: '2024-01-20T09:15:00Z' },
    { id: 3, type: 'delete', description: '删除了过期文档', time: '2024-01-19T16:45:00Z' }
  ]
};

// API 服务类
export class ApiService {
  static getAuthHeaders(): HeadersInit {
    try {
      const raw = localStorage.getItem('auth');
      if (!raw) return {};
      const { token } = JSON.parse(raw) as { token?: string };
      return token ? { Authorization: `Bearer ${token}` } : {};
    } catch {
      return {};
    }
  }
  // 用户相关API
  static async getUsers() {
    await delay(500);
    return {
      data: mockUsers,
      total: mockUsers.length,
      page: 1,
      pageSize: 10
    };
  }

  static async getUserById(id: number) {
    await delay(300);
    const user = mockUsers.find(u => u.id === id);
    if (!user) throw new Error('用户不存在');
    return user;
  }

  // 文档相关API
  static async getDocuments() {
    await delay(600);
    return {
      data: mockDocuments,
      total: mockDocuments.length,
      page: 1,
      pageSize: 10
    };
  }

  static async getDocumentById(id: number) {
    await delay(400);
    const document = mockDocuments.find(d => d.id === id);
    if (!document) throw new Error('文档不存在');
    return document;
  }

  // 分类相关API
  static async getCategories() {
    await delay(400);
    return mockCategories;
  }

  // 统计相关API
  static async getStatistics() {
    await delay(800);
    return mockStatistics;
  }

  // 搜索API
  static async searchDocuments(query: string) {
    await delay(600);
    const results = mockDocuments.filter(doc => 
      doc.title.includes(query) || 
      doc.content.includes(query) ||
      (doc.summary && doc.summary.includes(query))
    );
    return {
      data: results,
      total: results.length,
      query
    };
  }

  // 登录 API：后端优先，失败回退到 mock
  static async login(username: string, password: string): Promise<LoginResult> {
    const baseUrl = (import.meta as any)?.env?.VITE_API_BASE_URL || '';
    try {
      const resp = await fetch(`${baseUrl}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      if (resp.ok) {
        const data = await resp.json();
        return data as LoginResult;
      }
    } catch (err) {
      // 忽略网络/解析错误，回退到 mock
    }

    // mock 回退
    await delay(400);
    const found = mockUsers.find(u => u.username === username);
    if (!found) {
      throw new Error('用户名或密码错误');
    }
    const token = 'mock-token-' + Date.now();
    const refreshToken = 'mock-refresh-' + Date.now();
    const expiresAt = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
    // 将 mock 角色统一为 ROLE_ 前缀，便于与路由守卫一致
    const normalizedRole = found.role?.startsWith('ROLE_')
      ? found.role
      : `ROLE_${found.role?.toUpperCase()}`;
    return {
      user: {
        id: String(found.id),
        username: found.username,
        email: found.email,
        avatar: found.avatar,
        roles: [normalizedRole],
        permissions: []
      },
      token,
      refreshToken,
      expiresAt
    };
  }
}

export default ApiService;
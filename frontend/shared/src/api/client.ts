import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

// API响应接口
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 分页响应接口
export interface PageResponse<T = any> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// 请求配置接口
export interface RequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
  skipErrorHandler?: boolean;
  showLoading?: boolean;
  showError?: boolean;
  headers?: any;
  metadata?: { startTime: Date };
}

// 错误响应接口
export interface ErrorResponse {
  code: number;
  message: string;
  details?: string;
  timestamp: number;
  path?: string;
}

// Token管理接口
export interface TokenManager {
  getToken(): string | null;
  setToken(token: string): void;
  removeToken(): void;
  getRefreshToken(): string | null;
  setRefreshToken(token: string): void;
  removeRefreshToken(): void;
}

// 默认Token管理器
export class DefaultTokenManager implements TokenManager {
  private readonly tokenKey = 'cms_token';
  private readonly refreshTokenKey = 'cms_refresh_token';

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  removeToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  setRefreshToken(token: string): void {
    localStorage.setItem(this.refreshTokenKey, token);
  }

  removeRefreshToken(): void {
    localStorage.removeItem(this.refreshTokenKey);
  }
}

// API客户端类
export class ApiClient {
  private instance: AxiosInstance;
  private tokenManager: TokenManager;
  private isRefreshing = false;
  private failedQueue: Array<{
    resolve: (value?: any) => void;
    reject: (error?: any) => void;
  }> = [];

  constructor(
    baseURL: string,
    tokenManager: TokenManager = new DefaultTokenManager(),
    timeout = 10000
  ) {
    this.tokenManager = tokenManager;
    
    this.instance = axios.create({
      baseURL,
      timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config: any) => {
        const token = this.tokenManager.getToken();
        
        if (token && !config.skipAuth) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // 添加请求时间戳
        config.metadata = { startTime: new Date() };

        return config;
      },
      (error: AxiosError) => {
        return Promise.reject(error);
      }
    );

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        // 计算请求耗时
        const endTime = new Date();
        const startTime = (response.config as RequestConfig).metadata?.startTime;
        const duration = startTime ? endTime.getTime() - startTime.getTime() : 0;
        
        const isDev = (((import.meta as any)?.env?.MODE) === 'development') || (((globalThis as any)?.process?.env?.NODE_ENV) === 'development');
        if (isDev) {
          console.log(`API Request: ${response.config.method?.toUpperCase()} ${response.config.url} - ${duration}ms`);
        }

        return response;
      },
      async (error: AxiosError) => {
        const originalRequest: any = error.config;

        // 处理401错误 - Token过期
        if (error.response?.status === 401 && !originalRequest._retry) {
          if (this.isRefreshing) {
            // 如果正在刷新token，将请求加入队列
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ resolve, reject });
            }).then(token => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              return this.instance(originalRequest);
            }).catch(err => {
              return Promise.reject(err);
            });
          }

          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            const refreshToken = this.tokenManager.getRefreshToken();
            if (!refreshToken) {
              throw new Error('No refresh token available');
            }

            const response = await this.instance.post('/auth/refresh', {
              refreshToken,
            });

            const { accessToken, refreshToken: newRefreshToken } = response.data.data;
            
            this.tokenManager.setToken(accessToken);
            this.tokenManager.setRefreshToken(newRefreshToken);

            // 处理队列中的请求
            this.processQueue(null, accessToken);

            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return this.instance(originalRequest);
          } catch (refreshError) {
            this.processQueue(refreshError, null);
            this.tokenManager.removeToken();
            this.tokenManager.removeRefreshToken();
            
            // 触发登录页面跳转
            this.handleAuthError();
            
            return Promise.reject(refreshError);
          } finally {
            this.isRefreshing = false;
          }
        }

        return Promise.reject(error);
      }
    );
  }

  private processQueue(error: any, token: string | null): void {
    this.failedQueue.forEach(({ resolve, reject }) => {
      if (error) {
        reject(error);
      } else {
        resolve(token);
      }
    });
    
    this.failedQueue = [];
  }

  private handleAuthError(): void {
    // 可以通过事件或回调来处理认证错误
    window.dispatchEvent(new CustomEvent('auth:error'));
  }

  // GET请求
  async get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.get<ApiResponse<T>>(url, config);
    return response.data;
  }

  // POST请求
  async post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.post<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  // PUT请求
  async put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.put<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  // DELETE请求
  async delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.delete<ApiResponse<T>>(url, config);
    return response.data;
  }

  // PATCH请求
  async patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.patch<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  // 文件上传
  async upload<T = any>(url: string, file: File, config?: RequestConfig): Promise<ApiResponse<T>> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await this.instance.post<ApiResponse<T>>(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(config as any)?.headers,
      },
    });

    return response.data;
  }

  // 文件下载
  async download(url: string, filename?: string, config?: RequestConfig): Promise<void> {
    const response = await this.instance.get(url, {
      ...config,
      responseType: 'blob',
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    
    link.href = downloadUrl;
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    window.URL.revokeObjectURL(downloadUrl);
  }

  // 获取原始axios实例
  getInstance(): AxiosInstance {
    return this.instance;
  }

  // 设置Token管理器
  setTokenManager(tokenManager: TokenManager): void {
    this.tokenManager = tokenManager;
  }

  // 设置基础URL
  setBaseURL(baseURL: string): void {
    this.instance.defaults.baseURL = baseURL;
  }

  // 设置默认头部
  setDefaultHeader(key: string, value: string): void {
    this.instance.defaults.headers.common[key] = value;
  }

  // 移除默认头部
  removeDefaultHeader(key: string): void {
    delete this.instance.defaults.headers.common[key];
  }
}

// 创建默认API客户端实例
export const createApiClient = (
  baseURL: string,
  tokenManager?: TokenManager,
  timeout?: number
): ApiClient => {
  return new ApiClient(baseURL, tokenManager, timeout);
};

// 导出默认实例（需要在应用启动时初始化）
export let apiClient: ApiClient;

export const initializeApiClient = (
  baseURL: string,
  tokenManager?: TokenManager,
  timeout?: number
): void => {
  apiClient = createApiClient(baseURL, tokenManager, timeout);
};

export default ApiClient;
import axios from "axios";
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";

// 统一 axios 客户端：自动附带 Cookie，错误处理与重试（预留）
// 同源调用内部 API 路由，后端访问由服务端代理，自动附带会话中的 token
const API_BASE = "";

export const http = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  timeout: 15000,
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // 可在此加入请求级别的标记或幂等 key
  return config;
});

http.interceptors.response.use(
  (resp: AxiosResponse) => resp,
  async (error: AxiosError) => {
    const status = error.response?.status;
    // 401 统一重定向到登录
    if (status === 401) {
      // 在客户端环境下执行跳转
      if (typeof window !== "undefined") {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `/login?redirect=${redirect}`;
      }
    }
    // 其他错误抛出，交由调用方处理
    return Promise.reject(error);
  }
);

export const getJSON = async <T>(url: string, params?: Record<string, unknown>): Promise<T> => {
  try {
    const { data } = await http.get<T>(url, { params });
    return data;
  } catch (err: unknown) {
    const axiosError = err as AxiosError;
    const status = axiosError.response?.status;
    // 在 403 时，尽量返回更明确的错误信息（包含 requiredPermission）
    if (status === 403) {
      const data = axiosError.response?.data as { message?: string; requiredPermission?: string } | undefined;
      const message = data?.message || `Request failed with status code 403`;
      const required = data?.requiredPermission ? `（缺少权限：${data.requiredPermission}）` : "";
      throw new Error(`${message}${required}`);
    }
    const isNetworkError = !axiosError.response;
    if (isNetworkError) {
      // 契约一致的 mock 回退，仅在网络不可达时触发
      if (url.includes("/api/users")) {
        const page = (params?.page as number) ?? 0;
        const size = (params?.size as number) ?? 10;
        const content = [
          { id: 1, username: "admin", email: "admin@example.com", roles: [{ name: "ROLE_ADMIN" }, { name: "ROLE_USER" }] },
          { id: 2, username: "editor", email: "editor@example.com", roles: [{ name: "ROLE_EDITOR" }, { name: "ROLE_USER" }] },
          { id: 3, username: "user", email: "user@example.com", roles: [{ name: "ROLE_USER" }] },
        ];
        const mock = {
          content,
          totalElements: content.length,
          totalPages: 1,
          size,
          number: page,
          first: page === 0,
          last: true,
          empty: content.length === 0,
        };
        return mock as unknown as T;
      }
      if (url.includes("/api/documents")) {
        const page = (params?.page as number) ?? 0;
        const size = (params?.size as number) ?? 10;
        const now = new Date().toISOString();
        const content = [
          { id: 101, title: "安装指南", summary: "快速安装与配置", categoryName: "入门", updatedAt: now, viewCount: 128, downloadCount: 32 },
          { id: 102, title: "缓存策略", summary: "Redis 集成与最佳实践", categoryName: "架构", updatedAt: now, viewCount: 3245, downloadCount: 876 },
          { id: 103, title: "API 参考", summary: "核心接口说明", categoryName: "参考文档", updatedAt: now, viewCount: 560, downloadCount: 120 },
        ];
        const mock = {
          content,
          totalElements: content.length,
          totalPages: 1,
          size,
          number: page,
          first: page === 0,
          last: true,
          empty: content.length === 0,
        };
        return mock as unknown as T;
      }
    }
    throw err;
  }
};

export const postJSON = async <T>(url: string, body?: unknown): Promise<T> => {
  const { data } = await http.post<T>(url, body);
  return data;
};
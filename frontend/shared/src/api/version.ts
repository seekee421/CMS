import { ApiResponse, PageResponse, apiClient } from './client';

// 产品版本相关接口
export interface ProductVersionRequest {
  productName: string;
  version: string;
  displayName?: string;
  description?: string;
  isActive?: boolean;
  isDefault?: boolean;
  isLatest?: boolean;
  versionStatus?: 'DEVELOPMENT' | 'ALPHA' | 'BETA' | 'RC' | 'STABLE' | 'DEPRECATED' | 'EOL';
  releaseDate?: string;
  endOfLifeDate?: string;
  sortOrder?: number;
  versionTag?: string;
  branch?: string;
  changelog?: string;
  downloadUrl?: string;
  documentationUrl?: string;
  apiDocUrl?: string;
  releaseNotesUrl?: string;
  compatibilityInfo?: string;
  systemRequirements?: string;
}

export interface ProductVersionResponse {
  id: number;
  productName: string;
  version: string;
  displayName: string;
  description: string;
  isActive: boolean;
  isDefault: boolean;
  isLatest: boolean;
  versionStatus: string;
  versionStatusDisplayName: string;
  releaseDate: string;
  endOfLifeDate: string;
  sortOrder: number;
  versionTag: string;
  branch: string;
  changelog: string;
  downloadUrl: string;
  documentationUrl: string;
  apiDocUrl: string;
  releaseNotesUrl: string;
  compatibilityInfo: string;
  systemRequirements: string;
  createdBy: number;
  createdByName: string;
  updatedBy: number;
  updatedByName: string;
  createdAt: string;
  updatedAt: string;
  fullVersion: string;
  displayText: string;
  isReleased: boolean;
  isPreRelease: boolean;
  isExpired: boolean;
  documentVersionCount: number;
}

// 文档版本相关接口
export interface DocumentVersionRequest {
  documentId: number;
  productVersionId?: number;
  versionNumber?: string;
  title: string;
  content: string;
  summary?: string;
  changeLog?: string;
  changeType?: 'MAJOR' | 'MINOR' | 'PATCH' | 'HOTFIX' | 'CONTENT' | 'TRANSLATION' | 'CORRECTION';
  status?: 'DRAFT' | 'REVIEW' | 'APPROVED' | 'PUBLISHED' | 'ARCHIVED' | 'REJECTED';
  versionType?: 'MAJOR' | 'MINOR' | 'PATCH' | 'HOTFIX';
  isCurrent?: boolean;
  isPublished?: boolean;
  reviewNotes?: string;
  tags?: string;
  metadata?: string;
  notes?: string;
}

export interface DocumentVersionResponse {
  id: number;
  documentId: number;
  documentTitle: string;
  productVersionId: number;
  productVersionName: string;
  versionNumber: string;
  title: string;
  content: string;
  summary: string;
  changeLog: string;
  changeType: string;
  changeTypeDisplayName: string;
  status: string;
  statusDisplayName: string;
  versionType: string;
  versionTypeDisplayName: string;
  isCurrent: boolean;
  isPublished: boolean;
  reviewNotes: string;
  tags: string;
  metadata: string;
  notes: string;
  contentLength: number;
  wordCount: number;
  createdBy: number;
  createdByName: string;
  publishedBy: number;
  publishedByName: string;
  reviewedBy: number;
  reviewedByName: string;
  createdAt: string;
  publishedAt: string;
  reviewedAt: string;
  updatedAt: string;
  canEdit: boolean;
  canPublish: boolean;
  canReview: boolean;
}

// 版本比较结果接口
export interface VersionComparisonResponse {
  oldVersion: DocumentVersionResponse;
  newVersion: DocumentVersionResponse;
  differences: {
    title: boolean;
    content: boolean;
    summary: boolean;
    tags: boolean;
    metadata: boolean;
  };
  contentDiff?: string;
}

// 版本统计接口
export interface VersionStatisticsResponse {
  totalProductVersions: number;
  activeProductVersions: number;
  totalDocumentVersions: number;
  publishedDocumentVersions: number;
  draftDocumentVersions: number;
  versionsByStatus: Record<string, number>;
  versionsByChangeType: Record<string, number>;
  versionsByVersionType: Record<string, number>;
  recentVersions: DocumentVersionResponse[];
}

/**
 * 产品版本API服务
 */
export class ProductVersionApi {
  /**
   * 创建产品版本
   */
  static async create(request: ProductVersionRequest): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.post('/api/versions/product', request);
  }

  /**
   * 更新产品版本
   */
  static async update(id: number, request: ProductVersionRequest): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}`, request);
  }

  /**
   * 获取产品版本详情
   */
  static async getById(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.get(`/api/versions/product/${id}`);
  }

  /**
   * 获取产品版本列表
   */
  static async getList(page = 0, size = 20): Promise<ApiResponse<PageResponse<ProductVersionResponse>>> {
    return apiClient.get(`/api/versions/product?page=${page}&size=${size}`);
  }

  /**
   * 根据产品名称获取版本列表
   */
  static async getByProduct(productName: string, page = 0, size = 20): Promise<ApiResponse<PageResponse<ProductVersionResponse>>> {
    return apiClient.get(`/api/versions/product/by-product/${productName}?page=${page}&size=${size}`);
  }

  /**
   * 获取默认版本
   */
  static async getDefault(productName: string): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.get(`/api/versions/product/default/${productName}`);
  }

  /**
   * 获取最新版本
   */
  static async getLatest(productName: string): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.get(`/api/versions/product/latest/${productName}`);
  }

  /**
   * 设置默认版本
   */
  static async setDefault(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}/set-default`);
  }

  /**
   * 设置最新版本
   */
  static async setLatest(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}/set-latest`);
  }

  /**
   * 发布版本
   */
  static async release(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}/release`);
  }

  /**
   * 废弃版本
   */
  static async deprecate(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}/deprecate`);
  }

  /**
   * 结束生命周期
   */
  static async endOfLife(id: number): Promise<ApiResponse<ProductVersionResponse>> {
    return apiClient.put(`/api/versions/product/${id}/end-of-life`);
  }

  /**
   * 删除产品版本
   */
  static async delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete(`/api/versions/product/${id}`);
  }

  /**
   * 批量更新状态
   */
  static async batchUpdateStatus(ids: number[], status: string): Promise<ApiResponse<number>> {
    return apiClient.put('/api/versions/product/batch/status', { ids, status });
  }

  /**
   * 批量更新激活状态
   */
  static async batchUpdateActive(ids: number[], isActive: boolean): Promise<ApiResponse<number>> {
    return apiClient.put('/api/versions/product/batch/active', { ids, isActive });
  }

  /**
   * 获取统计信息
   */
  static async getStatistics(): Promise<ApiResponse<any>> {
    return apiClient.get('/api/versions/product/statistics');
  }

  /**
   * 清理过期版本
   */
  static async cleanupExpired(): Promise<ApiResponse<number>> {
    return apiClient.delete('/api/versions/product/cleanup/expired');
  }
}

/**
 * 文档版本API服务
 */
export class DocumentVersionApi {
  /**
   * 创建文档版本
   */
  static async create(request: DocumentVersionRequest): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.post('/api/versions/document', request);
  }

  /**
   * 更新文档版本
   */
  static async update(id: number, request: DocumentVersionRequest): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.put(`/api/versions/document/${id}`, request);
  }

  /**
   * 获取文档版本详情
   */
  static async getById(id: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.get(`/api/versions/document/${id}`);
  }

  /**
   * 获取文档版本列表
   */
  static async getList(page = 0, size = 20): Promise<ApiResponse<PageResponse<DocumentVersionResponse>>> {
    return apiClient.get(`/api/versions/document?page=${page}&size=${size}`);
  }

  /**
   * 获取文档的版本列表
   */
  static async getByDocument(documentId: number, page = 0, size = 20): Promise<ApiResponse<PageResponse<DocumentVersionResponse>>> {
    return apiClient.get(`/api/versions/document/by-document/${documentId}?page=${page}&size=${size}`);
  }

  /**
   * 获取当前版本
   */
  static async getCurrent(documentId: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.get(`/api/versions/document/current/${documentId}`);
  }

  /**
   * 获取最新版本
   */
  static async getLatest(documentId: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.get(`/api/versions/document/latest/${documentId}`);
  }

  /**
   * 比较版本
   */
  static async compare(oldVersionId: number, newVersionId: number): Promise<ApiResponse<VersionComparisonResponse>> {
    return apiClient.get(`/api/versions/document/compare/${oldVersionId}/${newVersionId}`);
  }

  /**
   * 发布版本
   */
  static async publish(id: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.put(`/api/versions/document/${id}/publish`);
  }

  /**
   * 设置为当前版本
   */
  static async setCurrent(id: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.put(`/api/versions/document/${id}/set-current`);
  }

  /**
   * 归档版本
   */
  static async archive(id: number): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.put(`/api/versions/document/${id}/archive`);
  }

  /**
   * 审核版本
   */
  static async review(id: number, approved: boolean, reviewNotes?: string): Promise<ApiResponse<DocumentVersionResponse>> {
    return apiClient.put(`/api/versions/document/${id}/review`, { approved, reviewNotes });
  }

  /**
   * 删除文档版本
   */
  static async delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete(`/api/versions/document/${id}`);
  }

  /**
   * 批量更新状态
   */
  static async batchUpdateStatus(ids: number[], status: string): Promise<ApiResponse<number>> {
    return apiClient.put('/api/versions/document/batch/status', { ids, status });
  }

  /**
   * 批量发布
   */
  static async batchPublish(ids: number[]): Promise<ApiResponse<number>> {
    return apiClient.put('/api/versions/document/batch/publish', { ids });
  }

  /**
   * 批量归档
   */
  static async batchArchive(ids: number[]): Promise<ApiResponse<number>> {
    return apiClient.put('/api/versions/document/batch/archive', { ids });
  }

  /**
   * 清理旧版本
   */
  static async cleanupOld(documentId: number, keepCount = 10): Promise<ApiResponse<number>> {
    return apiClient.delete(`/api/versions/document/cleanup/old/${documentId}?keepCount=${keepCount}`);
  }

  /**
   * 获取统计信息
   */
  static async getStatistics(): Promise<ApiResponse<VersionStatisticsResponse>> {
    return apiClient.get('/api/versions/document/statistics');
  }
}

// 导出所有API
export const VersionApi = {
  ProductVersion: ProductVersionApi,
  DocumentVersion: DocumentVersionApi
};

export default VersionApi;
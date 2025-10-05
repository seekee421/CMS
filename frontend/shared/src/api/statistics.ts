import { ApiResponse, PageResponse, apiClient } from './client';

// 统计数据相关接口
export interface DocumentStatisticsRequest {
  documentId: number;
  statisticsDate?: string;
  timeRange?: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'TOTAL';
  viewCount?: number;
  downloadCount?: number;
  uniqueVisitors?: number;
  avgDuration?: number;
  bounceRate?: number;
  searchCount?: number;
  feedbackCount?: number;
  shareCount?: number;
  favoriteCount?: number;
  ratingSum?: number;
  ratingCount?: number;
}

export interface DocumentStatisticsResponse {
  id: number;
  documentId: number;
  documentTitle: string;
  statisticsDate: string;
  timeRange: string;
  timeRangeDisplayName: string;
  viewCount: number;
  downloadCount: number;
  uniqueVisitors: number;
  avgDuration: number;
  avgDurationDisplay: string;
  bounceRate: number;
  bounceRateDisplay: string;
  searchCount: number;
  feedbackCount: number;
  shareCount: number;
  favoriteCount: number;
  ratingSum: number;
  ratingCount: number;
  avgRating: number;
  avgRatingDisplay: string;
  totalInteractions: number;
  engagementScore: number;
  engagementLevel: string;
  isPopular: boolean;
  createdAt: string;
  updatedAt: string;
}

// 系统统计接口
export interface SystemStatisticsResponse {
  totalViews: number;
  totalDownloads: number;
  totalUniqueVisitors: number;
  totalDocuments: number;
  totalFeedback: number;
  totalShares: number;
  totalFavorites: number;
  avgRating: number;
  avgDuration: number;
  avgBounceRate: number;
  engagementScore: number;
  popularDocuments: number;
  activeDocuments: number;
}

// 趋势数据接口
export interface TrendDataResponse {
  date: string;
  views: number;
  downloads: number;
  uniqueVisitors: number;
  feedback: number;
  shares: number;
  favorites: number;
  avgRating: number;
  engagementScore: number;
}

// 排行榜数据接口
export interface RankingDataResponse {
  documentId: number;
  documentTitle: string;
  value: number;
  rank: number;
  change: number; // 排名变化
  percentage: number; // 占比
}

/**
 * 文档统计API服务
 */
export class DocumentStatisticsApi {
  /**
   * 创建统计记录
   */
  static async create(request: DocumentStatisticsRequest): Promise<ApiResponse<DocumentStatisticsResponse>> {
    return apiClient.post('/api/statistics', request);
  }

  /**
   * 更新统计记录
   */
  static async update(id: number, request: DocumentStatisticsRequest): Promise<ApiResponse<DocumentStatisticsResponse>> {
    return apiClient.put(`/api/statistics/${id}`, request);
  }

  /**
   * 获取统计记录详情
   */
  static async getById(id: number): Promise<ApiResponse<DocumentStatisticsResponse>> {
    return apiClient.get(`/api/statistics/${id}`);
  }

  /**
   * 删除统计记录
   */
  static async delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete(`/api/statistics/${id}`);
  }

  /**
   * 获取文档的统计记录列表
   */
  static async getByDocument(documentId: number): Promise<ApiResponse<DocumentStatisticsResponse[]>> {
    return apiClient.get(`/api/statistics/document/${documentId}`);
  }

  /**
   * 分页获取文档的统计记录
   */
  static async getByDocumentPage(documentId: number, page = 0, size = 20): Promise<ApiResponse<PageResponse<DocumentStatisticsResponse>>> {
    return apiClient.get(`/api/statistics/document/${documentId}/page?page=${page}&size=${size}`);
  }

  /**
   * 获取文档总浏览次数
   */
  static async getTotalViews(documentId: number): Promise<ApiResponse<number>> {
    return apiClient.get(`/api/statistics/document/${documentId}/total-views`);
  }

  /**
   * 获取文档总下载次数
   */
  static async getTotalDownloads(documentId: number): Promise<ApiResponse<number>> {
    return apiClient.get(`/api/statistics/document/${documentId}/total-downloads`);
  }

  /**
   * 获取文档总独立访客数
   */
  static async getTotalVisitors(documentId: number): Promise<ApiResponse<number>> {
    return apiClient.get(`/api/statistics/document/${documentId}/total-visitors`);
  }

  /**
   * 获取指定时间范围的浏览次数
   */
  static async getViewsByDateRange(documentId: number, startDate: string, endDate: string): Promise<ApiResponse<number>> {
    return apiClient.get(`/api/statistics/document/${documentId}/views?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取指定时间范围的下载次数
   */
  static async getDownloadsByDateRange(documentId: number, startDate: string, endDate: string): Promise<ApiResponse<number>> {
    return apiClient.get(`/api/statistics/document/${documentId}/downloads?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取文档趋势数据
   */
  static async getTrendData(documentId: number, startDate: string, endDate: string): Promise<ApiResponse<TrendDataResponse[]>> {
    return apiClient.get(`/api/statistics/document/${documentId}/trend?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取文档简要统计信息
   */
  static async getDocumentSummary(documentId: number): Promise<ApiResponse<any>> {
    return apiClient.get(`/api/statistics/document/${documentId}/summary`);
  }
}

/**
 * 统计操作API服务
 */
export class StatisticsActionApi {
  /**
   * 记录文档浏览
   */
  static async recordView(documentId: number, count = 1): Promise<ApiResponse<void>> {
    if (count === 1) {
      return apiClient.post(`/api/statistics/document/${documentId}/view`);
    } else {
      return apiClient.post(`/api/statistics/document/${documentId}/view/${count}`);
    }
  }

  /**
   * 记录文档下载
   */
  static async recordDownload(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/download`);
  }

  /**
   * 记录独立访客
   */
  static async recordUniqueVisitor(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/visitor`);
  }

  /**
   * 记录搜索
   */
  static async recordSearch(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/search`);
  }

  /**
   * 记录反馈
   */
  static async recordFeedback(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/feedback`);
  }

  /**
   * 记录分享
   */
  static async recordShare(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/share`);
  }

  /**
   * 记录收藏
   */
  static async recordFavorite(documentId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/favorite`);
  }

  /**
   * 记录评分
   */
  static async recordRating(documentId: number, rating: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/rating?rating=${rating}`);
  }

  /**
   * 更新停留时间
   */
  static async updateDuration(documentId: number, duration: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/duration?duration=${duration}`);
  }

  /**
   * 更新跳出率
   */
  static async updateBounceRate(documentId: number, bounceRate: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/api/statistics/document/${documentId}/bounce-rate?bounceRate=${bounceRate}`);
  }
}

/**
 * 排行榜和热门内容API服务
 */
export class RankingApi {
  /**
   * 获取最受欢迎的文档
   */
  static async getMostViewed(startDate: string, endDate: string, limit = 10): Promise<ApiResponse<RankingDataResponse[]>> {
    return apiClient.get(`/api/statistics/most-viewed?startDate=${startDate}&endDate=${endDate}&limit=${limit}`);
  }

  /**
   * 获取最多下载的文档
   */
  static async getMostDownloaded(startDate: string, endDate: string, limit = 10): Promise<ApiResponse<RankingDataResponse[]>> {
    return apiClient.get(`/api/statistics/most-downloaded?startDate=${startDate}&endDate=${endDate}&limit=${limit}`);
  }

  /**
   * 获取参与度最高的文档
   */
  static async getMostEngaged(startDate: string, endDate: string, limit = 10): Promise<ApiResponse<RankingDataResponse[]>> {
    return apiClient.get(`/api/statistics/most-engaged?startDate=${startDate}&endDate=${endDate}&limit=${limit}`);
  }

  /**
   * 获取评分最高的文档
   */
  static async getTopRated(startDate: string, endDate: string, minRatings = 5, limit = 10): Promise<ApiResponse<RankingDataResponse[]>> {
    return apiClient.get(`/api/statistics/top-rated?startDate=${startDate}&endDate=${endDate}&minRatings=${minRatings}&limit=${limit}`);
  }

  /**
   * 获取最近7天的热门文档
   */
  static async getRecentHot(): Promise<ApiResponse<RankingDataResponse[]>> {
    return apiClient.get('/api/statistics/hot/recent');
  }
}

/**
 * 系统统计API服务
 */
export class SystemStatisticsApi {
  /**
   * 获取系统总体统计
   */
  static async getSystemStatistics(startDate: string, endDate: string): Promise<ApiResponse<SystemStatisticsResponse>> {
    return apiClient.get(`/api/statistics/system?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取日统计数据
   */
  static async getDailyStatistics(startDate: string, endDate: string): Promise<ApiResponse<TrendDataResponse[]>> {
    return apiClient.get(`/api/statistics/daily?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取文档统计数据
   */
  static async getDocumentStatistics(startDate: string, endDate: string): Promise<ApiResponse<any[]>> {
    return apiClient.get(`/api/statistics/documents?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * 获取今日统计概览
   */
  static async getTodayStatistics(): Promise<ApiResponse<SystemStatisticsResponse>> {
    return apiClient.get('/api/statistics/today');
  }

  /**
   * 获取本周统计概览
   */
  static async getThisWeekStatistics(): Promise<ApiResponse<SystemStatisticsResponse>> {
    return apiClient.get('/api/statistics/this-week');
  }

  /**
   * 获取本月统计概览
   */
  static async getThisMonthStatistics(): Promise<ApiResponse<SystemStatisticsResponse>> {
    return apiClient.get('/api/statistics/this-month');
  }

  /**
   * 获取本年统计概览
   */
  static async getThisYearStatistics(): Promise<ApiResponse<SystemStatisticsResponse>> {
    return apiClient.get('/api/statistics/this-year');
  }
}

/**
 * 数据管理API服务
 */
export class StatisticsManagementApi {
  /**
   * 生成周统计数据
   */
  static async generateWeeklyStatistics(): Promise<ApiResponse<void>> {
    return apiClient.post('/api/statistics/generate/weekly');
  }

  /**
   * 生成月统计数据
   */
  static async generateMonthlyStatistics(): Promise<ApiResponse<void>> {
    return apiClient.post('/api/statistics/generate/monthly');
  }

  /**
   * 生成年统计数据
   */
  static async generateYearlyStatistics(): Promise<ApiResponse<void>> {
    return apiClient.post('/api/statistics/generate/yearly');
  }

  /**
   * 清理过期统计数据
   */
  static async cleanupExpiredStatistics(retentionDays = 365): Promise<ApiResponse<number>> {
    return apiClient.delete(`/api/statistics/cleanup?retentionDays=${retentionDays}`);
  }

  /**
   * 清理指定文档的统计数据
   */
  static async cleanupDocumentStatistics(documentId: number): Promise<ApiResponse<number>> {
    return apiClient.delete(`/api/statistics/document/${documentId}/cleanup`);
  }
}

// 导出所有API
export const StatisticsApi = {
  DocumentStatistics: DocumentStatisticsApi,
  Action: StatisticsActionApi,
  Ranking: RankingApi,
  System: SystemStatisticsApi,
  Management: StatisticsManagementApi
};

export default StatisticsApi;
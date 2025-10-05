import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface DocumentStatistics {
  documentId: string;
  documentTitle: string;
  viewCount: number;
  downloadCount: number;
  likeCount: number;
  commentCount: number;
  lastViewedAt: string;
}

export interface UserStatistics {
  userId: string;
  username: string;
  loginCount: number;
  documentViews: number;
  documentsCreated: number;
  lastLoginAt: string;
}

export interface SystemStatistics {
  totalDocuments: number;
  totalUsers: number;
  totalCategories: number;
  totalFeedbacks: number;
  activeUsers: number;
  publishedDocuments: number;
  pendingFeedbacks: number;
}

export interface ChartData {
  labels: string[];
  datasets: Array<{
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    borderWidth?: number;
  }>;
}

export interface StatisticsState {
  // 系统概览
  systemStats: SystemStatistics;
  systemStatsLoading: boolean;
  
  // 文档统计
  documentStats: DocumentStatistics[];
  documentStatsLoading: boolean;
  documentStatsTotal: number;
  
  // 用户统计
  userStats: UserStatistics[];
  userStatsLoading: boolean;
  userStatsTotal: number;
  
  // 图表数据
  charts: {
    documentViews: ChartData | null;
    userActivity: ChartData | null;
    categoryDistribution: ChartData | null;
    feedbackTrends: ChartData | null;
  };
  chartsLoading: boolean;
  
  // 时间范围
  dateRange: {
    start: string;
    end: string;
  };
  
  // 筛选条件
  filters: {
    timeRange: 'today' | 'week' | 'month' | 'quarter' | 'year' | 'custom';
    categoryId?: string;
    userId?: string;
  };
  
  // 分页
  pagination: {
    documentStats: {
      current: number;
      pageSize: number;
    };
    userStats: {
      current: number;
      pageSize: number;
    };
  };
  
  error: string | null;
}

const initialState: StatisticsState = {
  systemStats: {
    totalDocuments: 0,
    totalUsers: 0,
    totalCategories: 0,
    totalFeedbacks: 0,
    activeUsers: 0,
    publishedDocuments: 0,
    pendingFeedbacks: 0,
  },
  systemStatsLoading: false,
  documentStats: [],
  documentStatsLoading: false,
  documentStatsTotal: 0,
  userStats: [],
  userStatsLoading: false,
  userStatsTotal: 0,
  charts: {
    documentViews: null,
    userActivity: null,
    categoryDistribution: null,
    feedbackTrends: null,
  },
  chartsLoading: false,
  dateRange: {
    start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    end: new Date().toISOString().split('T')[0],
  },
  filters: {
    timeRange: 'month',
  },
  pagination: {
    documentStats: {
      current: 1,
      pageSize: 20,
    },
    userStats: {
      current: 1,
      pageSize: 20,
    },
  },
  error: null,
};

const statisticsSlice = createSlice({
  name: 'statistics',
  initialState,
  reducers: {
    // 系统统计
    fetchSystemStatsStart: (state) => {
      state.systemStatsLoading = true;
      state.error = null;
    },
    
    fetchSystemStatsSuccess: (state, action: PayloadAction<SystemStatistics>) => {
      state.systemStatsLoading = false;
      state.systemStats = action.payload;
    },
    
    fetchSystemStatsFailure: (state, action: PayloadAction<string>) => {
      state.systemStatsLoading = false;
      state.error = action.payload;
    },
    
    // 文档统计
    fetchDocumentStatsStart: (state) => {
      state.documentStatsLoading = true;
      state.error = null;
    },
    
    fetchDocumentStatsSuccess: (state, action: PayloadAction<{
      stats: DocumentStatistics[];
      total: number;
    }>) => {
      state.documentStatsLoading = false;
      state.documentStats = action.payload.stats;
      state.documentStatsTotal = action.payload.total;
    },
    
    fetchDocumentStatsFailure: (state, action: PayloadAction<string>) => {
      state.documentStatsLoading = false;
      state.error = action.payload;
    },
    
    // 用户统计
    fetchUserStatsStart: (state) => {
      state.userStatsLoading = true;
      state.error = null;
    },
    
    fetchUserStatsSuccess: (state, action: PayloadAction<{
      stats: UserStatistics[];
      total: number;
    }>) => {
      state.userStatsLoading = false;
      state.userStats = action.payload.stats;
      state.userStatsTotal = action.payload.total;
    },
    
    fetchUserStatsFailure: (state, action: PayloadAction<string>) => {
      state.userStatsLoading = false;
      state.error = action.payload;
    },
    
    // 图表数据
    fetchChartsStart: (state) => {
      state.chartsLoading = true;
      state.error = null;
    },
    
    fetchChartsSuccess: (state, action: PayloadAction<{
      documentViews?: ChartData;
      userActivity?: ChartData;
      categoryDistribution?: ChartData;
      feedbackTrends?: ChartData;
    }>) => {
      state.chartsLoading = false;
      const { documentViews, userActivity, categoryDistribution, feedbackTrends } = action.payload;
      
      if (documentViews) state.charts.documentViews = documentViews;
      if (userActivity) state.charts.userActivity = userActivity;
      if (categoryDistribution) state.charts.categoryDistribution = categoryDistribution;
      if (feedbackTrends) state.charts.feedbackTrends = feedbackTrends;
    },
    
    fetchChartsFailure: (state, action: PayloadAction<string>) => {
      state.chartsLoading = false;
      state.error = action.payload;
    },
    
    // 设置时间范围
    setDateRange: (state, action: PayloadAction<{
      start: string;
      end: string;
    }>) => {
      state.dateRange = action.payload;
    },
    
    // 设置筛选条件
    setFilters: (state, action: PayloadAction<Partial<StatisticsState['filters']>>) => {
      state.filters = { ...state.filters, ...action.payload };
      
      // 重置分页
      state.pagination.documentStats.current = 1;
      state.pagination.userStats.current = 1;
    },
    
    // 设置分页
    setDocumentStatsPagination: (state, action: PayloadAction<{
      current: number;
      pageSize: number;
    }>) => {
      state.pagination.documentStats = action.payload;
    },
    
    setUserStatsPagination: (state, action: PayloadAction<{
      current: number;
      pageSize: number;
    }>) => {
      state.pagination.userStats = action.payload;
    },
    
    // 更新单个文档统计
    updateDocumentStats: (state, action: PayloadAction<{
      documentId: string;
      viewCount?: number;
      downloadCount?: number;
      likeCount?: number;
      commentCount?: number;
    }>) => {
      const { documentId, ...updates } = action.payload;
      const stats = state.documentStats.find(s => s.documentId === documentId);
      
      if (stats) {
        Object.assign(stats, updates);
        stats.lastViewedAt = new Date().toISOString();
      }
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
    
    // 重置状态
    resetStatistics: (state) => {
      Object.assign(state, initialState);
    },
  },
});

export const {
  fetchSystemStatsStart,
  fetchSystemStatsSuccess,
  fetchSystemStatsFailure,
  fetchDocumentStatsStart,
  fetchDocumentStatsSuccess,
  fetchDocumentStatsFailure,
  fetchUserStatsStart,
  fetchUserStatsSuccess,
  fetchUserStatsFailure,
  fetchChartsStart,
  fetchChartsSuccess,
  fetchChartsFailure,
  setDateRange,
  setFilters,
  setDocumentStatsPagination,
  setUserStatsPagination,
  updateDocumentStats,
  clearError,
  resetStatistics,
} = statisticsSlice.actions;

export default statisticsSlice.reducer;
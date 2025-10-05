import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Feedback {
  id: string;
  documentId: string;
  documentTitle: string;
  feedbackType: 'CONTENT_INCORRECT' | 'CONTENT_MISSING' | 'DESCRIPTION_UNCLEAR' | 'OTHER_SUGGESTION';
  description: string;
  contactInfo: string;
  status: 'pending' | 'processing' | 'resolved' | 'rejected';
  priority: 'low' | 'medium' | 'high';
  assignee?: {
    id: string;
    name: string;
  };
  response?: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
}

export interface FeedbackFilter {
  keyword?: string;
  feedbackType?: Feedback['feedbackType'];
  status?: Feedback['status'];
  priority?: Feedback['priority'];
  assigneeId?: string;
  dateRange?: [string, string];
}

export interface FeedbackState {
  // 反馈列表
  feedbacks: Feedback[];
  total: number;
  loading: boolean;
  error: string | null;
  
  // 当前反馈
  currentFeedback: Feedback | null;
  currentLoading: boolean;
  
  // 筛选和分页
  filters: FeedbackFilter;
  pagination: {
    current: number;
    pageSize: number;
  };
  
  // 编辑状态
  editing: {
    feedback: Feedback | null;
    loading: boolean;
    saving: boolean;
  };
  
  // 统计数据
  statistics: {
    total: number;
    pending: number;
    processing: number;
    resolved: number;
    rejected: number;
    byType: Record<Feedback['feedbackType'], number>;
    byPriority: Record<Feedback['priority'], number>;
  };
  statisticsLoading: boolean;
}

const initialState: FeedbackState = {
  feedbacks: [],
  total: 0,
  loading: false,
  error: null,
  currentFeedback: null,
  currentLoading: false,
  filters: {},
  pagination: {
    current: 1,
    pageSize: 20,
  },
  editing: {
    feedback: null,
    loading: false,
    saving: false,
  },
  statistics: {
    total: 0,
    pending: 0,
    processing: 0,
    resolved: 0,
    rejected: 0,
    byType: {
      CONTENT_INCORRECT: 0,
      CONTENT_MISSING: 0,
      DESCRIPTION_UNCLEAR: 0,
      OTHER_SUGGESTION: 0,
    },
    byPriority: {
      low: 0,
      medium: 0,
      high: 0,
    },
  },
  statisticsLoading: false,
};

const feedbackSlice = createSlice({
  name: 'feedback',
  initialState,
  reducers: {
    // 反馈列表
    fetchFeedbacksStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    
    fetchFeedbacksSuccess: (state, action: PayloadAction<{
      feedbacks: Feedback[];
      total: number;
    }>) => {
      state.loading = false;
      state.feedbacks = action.payload.feedbacks;
      state.total = action.payload.total;
    },
    
    fetchFeedbacksFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },
    
    // 当前反馈
    fetchFeedbackStart: (state) => {
      state.currentLoading = true;
      state.error = null;
    },
    
    fetchFeedbackSuccess: (state, action: PayloadAction<Feedback>) => {
      state.currentLoading = false;
      state.currentFeedback = action.payload;
    },
    
    fetchFeedbackFailure: (state, action: PayloadAction<string>) => {
      state.currentLoading = false;
      state.error = action.payload;
    },
    
    // 筛选和分页
    setFilters: (state, action: PayloadAction<FeedbackFilter>) => {
      state.filters = action.payload;
      state.pagination.current = 1;
    },
    
    setPagination: (state, action: PayloadAction<{
      current: number;
      pageSize: number;
    }>) => {
      state.pagination = action.payload;
    },
    
    // 编辑状态
    startEditingFeedback: (state, action: PayloadAction<Feedback>) => {
      state.editing.feedback = action.payload;
    },
    
    saveFeedbackStart: (state) => {
      state.editing.saving = true;
    },
    
    saveFeedbackSuccess: (state, action: PayloadAction<Feedback>) => {
      state.editing.saving = false;
      state.editing.feedback = null;
      
      // 更新列表中的反馈
      const index = state.feedbacks.findIndex(feedback => feedback.id === action.payload.id);
      if (index !== -1) {
        state.feedbacks[index] = action.payload;
      }
      
      // 更新当前反馈
      if (state.currentFeedback?.id === action.payload.id) {
        state.currentFeedback = action.payload;
      }
    },
    
    saveFeedbackFailure: (state, action: PayloadAction<string>) => {
      state.editing.saving = false;
      state.error = action.payload;
    },
    
    stopEditingFeedback: (state) => {
      state.editing.feedback = null;
    },
    
    // 反馈操作
    updateFeedbackStatus: (state, action: PayloadAction<{
      id: string;
      status: Feedback['status'];
      response?: string;
      assigneeId?: string;
    }>) => {
      const { id, status, response, assigneeId } = action.payload;
      
      const feedback = state.feedbacks.find(f => f.id === id);
      if (feedback) {
        feedback.status = status;
        feedback.updatedAt = new Date().toISOString();
        
        if (response) {
          feedback.response = response;
        }
        
        if (assigneeId) {
          // 这里需要根据实际情况设置assignee信息
          // feedback.assignee = { id: assigneeId, name: '...' };
        }
        
        if (status === 'resolved') {
          feedback.resolvedAt = new Date().toISOString();
        }
      }
      
      // 更新当前反馈
      if (state.currentFeedback?.id === id) {
        state.currentFeedback.status = status;
        state.currentFeedback.updatedAt = new Date().toISOString();
        
        if (response) {
          state.currentFeedback.response = response;
        }
        
        if (status === 'resolved') {
          state.currentFeedback.resolvedAt = new Date().toISOString();
        }
      }
    },
    
    updateFeedbackPriority: (state, action: PayloadAction<{
      id: string;
      priority: Feedback['priority'];
    }>) => {
      const { id, priority } = action.payload;
      
      const feedback = state.feedbacks.find(f => f.id === id);
      if (feedback) {
        feedback.priority = priority;
        feedback.updatedAt = new Date().toISOString();
      }
      
      if (state.currentFeedback?.id === id) {
        state.currentFeedback.priority = priority;
        state.currentFeedback.updatedAt = new Date().toISOString();
      }
    },
    
    deleteFeedback: (state, action: PayloadAction<string>) => {
      state.feedbacks = state.feedbacks.filter(feedback => feedback.id !== action.payload);
      state.total -= 1;
      
      if (state.currentFeedback?.id === action.payload) {
        state.currentFeedback = null;
      }
    },
    
    // 统计数据
    fetchStatisticsStart: (state) => {
      state.statisticsLoading = true;
    },
    
    fetchStatisticsSuccess: (state, action: PayloadAction<FeedbackState['statistics']>) => {
      state.statisticsLoading = false;
      state.statistics = action.payload;
    },
    
    fetchStatisticsFailure: (state, action: PayloadAction<string>) => {
      state.statisticsLoading = false;
      state.error = action.payload;
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
  },
});

export const {
  fetchFeedbacksStart,
  fetchFeedbacksSuccess,
  fetchFeedbacksFailure,
  fetchFeedbackStart,
  fetchFeedbackSuccess,
  fetchFeedbackFailure,
  setFilters,
  setPagination,
  startEditingFeedback,
  saveFeedbackStart,
  saveFeedbackSuccess,
  saveFeedbackFailure,
  stopEditingFeedback,
  updateFeedbackStatus,
  updateFeedbackPriority,
  deleteFeedback,
  fetchStatisticsStart,
  fetchStatisticsSuccess,
  fetchStatisticsFailure,
  clearError,
} = feedbackSlice.actions;

export default feedbackSlice.reducer;
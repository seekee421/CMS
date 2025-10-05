import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Document {
  id: string;
  title: string;
  content: string;
  summary?: string;
  categoryId: string;
  categoryName?: string;
  status: 'draft' | 'published' | 'archived';
  version: string;
  author: {
    id: string;
    name: string;
    avatar?: string;
  };
  tags: string[];
  attachments: Array<{
    id: string;
    name: string;
    url: string;
    size: number;
    type: string;
  }>;
  metadata: {
    viewCount: number;
    downloadCount: number;
    likeCount: number;
    commentCount: number;
  };
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
}

export interface DocumentFilter {
  keyword?: string;
  categoryId?: string;
  status?: Document['status'];
  author?: string;
  tags?: string[];
  dateRange?: [string, string];
}

export interface DocumentState {
  // 文档列表
  documents: Document[];
  total: number;
  loading: boolean;
  error: string | null;
  
  // 当前文档
  currentDocument: Document | null;
  currentLoading: boolean;
  
  // 筛选和分页
  filters: DocumentFilter;
  pagination: {
    current: number;
    pageSize: number;
  };
  
  // 编辑状态
  editing: {
    document: Document | null;
    loading: boolean;
    saving: boolean;
    hasChanges: boolean;
  };
  
  // 版本历史
  versions: Array<{
    id: string;
    version: string;
    content: string;
    author: string;
    createdAt: string;
    changeLog?: string;
  }>;
  versionsLoading: boolean;
  
  // 搜索状态
  searchResults: Document[];
  searchLoading: boolean;
  searchKeyword: string;
}

const initialState: DocumentState = {
  documents: [],
  total: 0,
  loading: false,
  error: null,
  currentDocument: null,
  currentLoading: false,
  filters: {},
  pagination: {
    current: 1,
    pageSize: 20,
  },
  editing: {
    document: null,
    loading: false,
    saving: false,
    hasChanges: false,
  },
  versions: [],
  versionsLoading: false,
  searchResults: [],
  searchLoading: false,
  searchKeyword: '',
};

const documentSlice = createSlice({
  name: 'document',
  initialState,
  reducers: {
    // 文档列表
    fetchDocumentsStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    
    fetchDocumentsSuccess: (state, action: PayloadAction<{
      documents: Document[];
      total: number;
    }>) => {
      state.loading = false;
      state.documents = action.payload.documents;
      state.total = action.payload.total;
    },
    
    fetchDocumentsFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },
    
    // 当前文档
    fetchDocumentStart: (state) => {
      state.currentLoading = true;
      state.error = null;
    },
    
    fetchDocumentSuccess: (state, action: PayloadAction<Document>) => {
      state.currentLoading = false;
      state.currentDocument = action.payload;
    },
    
    fetchDocumentFailure: (state, action: PayloadAction<string>) => {
      state.currentLoading = false;
      state.error = action.payload;
    },
    
    // 筛选和分页
    setFilters: (state, action: PayloadAction<DocumentFilter>) => {
      state.filters = action.payload;
      state.pagination.current = 1; // 重置页码
    },
    
    setPagination: (state, action: PayloadAction<{
      current: number;
      pageSize: number;
    }>) => {
      state.pagination = action.payload;
    },
    
    // 编辑状态
    startEditing: (state, action: PayloadAction<Document>) => {
      state.editing.document = action.payload;
      state.editing.hasChanges = false;
    },
    
    updateEditingDocument: (state, action: PayloadAction<Partial<Document>>) => {
      if (state.editing.document) {
        state.editing.document = { ...state.editing.document, ...action.payload };
        state.editing.hasChanges = true;
      }
    },
    
    saveDocumentStart: (state) => {
      state.editing.saving = true;
    },
    
    saveDocumentSuccess: (state, action: PayloadAction<Document>) => {
      state.editing.saving = false;
      state.editing.document = action.payload;
      state.editing.hasChanges = false;
      
      // 更新列表中的文档
      const index = state.documents.findIndex(doc => doc.id === action.payload.id);
      if (index !== -1) {
        state.documents[index] = action.payload;
      }
      
      // 更新当前文档
      if (state.currentDocument?.id === action.payload.id) {
        state.currentDocument = action.payload;
      }
    },
    
    saveDocumentFailure: (state, action: PayloadAction<string>) => {
      state.editing.saving = false;
      state.error = action.payload;
    },
    
    stopEditing: (state) => {
      state.editing.document = null;
      state.editing.hasChanges = false;
    },
    
    // 版本历史
    fetchVersionsStart: (state) => {
      state.versionsLoading = true;
    },
    
    fetchVersionsSuccess: (state, action: PayloadAction<DocumentState['versions']>) => {
      state.versionsLoading = false;
      state.versions = action.payload;
    },
    
    fetchVersionsFailure: (state, action: PayloadAction<string>) => {
      state.versionsLoading = false;
      state.error = action.payload;
    },
    
    // 搜索
    searchDocumentsStart: (state, action: PayloadAction<string>) => {
      state.searchLoading = true;
      state.searchKeyword = action.payload;
    },
    
    searchDocumentsSuccess: (state, action: PayloadAction<Document[]>) => {
      state.searchLoading = false;
      state.searchResults = action.payload;
    },
    
    searchDocumentsFailure: (state, action: PayloadAction<string>) => {
      state.searchLoading = false;
      state.error = action.payload;
    },
    
    clearSearchResults: (state) => {
      state.searchResults = [];
      state.searchKeyword = '';
    },
    
    // 文档操作
    deleteDocument: (state, action: PayloadAction<string>) => {
      state.documents = state.documents.filter(doc => doc.id !== action.payload);
      state.total -= 1;
      
      if (state.currentDocument?.id === action.payload) {
        state.currentDocument = null;
      }
    },
    
    updateDocumentStatus: (state, action: PayloadAction<{
      id: string;
      status: Document['status'];
    }>) => {
      const document = state.documents.find(doc => doc.id === action.payload.id);
      if (document) {
        document.status = action.payload.status;
      }
      
      if (state.currentDocument?.id === action.payload.id) {
        state.currentDocument.status = action.payload.status;
      }
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
  },
});

export const {
  fetchDocumentsStart,
  fetchDocumentsSuccess,
  fetchDocumentsFailure,
  fetchDocumentStart,
  fetchDocumentSuccess,
  fetchDocumentFailure,
  setFilters,
  setPagination,
  startEditing,
  updateEditingDocument,
  saveDocumentStart,
  saveDocumentSuccess,
  saveDocumentFailure,
  stopEditing,
  fetchVersionsStart,
  fetchVersionsSuccess,
  fetchVersionsFailure,
  searchDocumentsStart,
  searchDocumentsSuccess,
  searchDocumentsFailure,
  clearSearchResults,
  deleteDocument,
  updateDocumentStatus,
  clearError,
} = documentSlice.actions;

export default documentSlice.reducer;
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface UIState {
  // 侧边栏状态
  sidebarCollapsed: boolean;
  sidebarVisible: boolean;
  
  // 主题设置
  theme: 'light' | 'dark';
  primaryColor: string;
  
  // 语言设置
  locale: 'zh-CN' | 'en-US';
  
  // 布局设置
  layout: 'side' | 'top' | 'mix';
  contentWidth: 'fluid' | 'fixed';
  fixedHeader: boolean;
  fixedSidebar: boolean;
  
  // 加载状态
  globalLoading: boolean;
  pageLoading: boolean;
  
  // 模态框状态
  modals: {
    searchVisible: boolean;
    profileVisible: boolean;
    settingsVisible: boolean;
  };
  
  // 通知状态
  notifications: {
    visible: boolean;
    unreadCount: number;
  };
  
  // 面包屑
  breadcrumb: {
    visible: boolean;
    items: Array<{
      title: string;
      path?: string;
    }>;
  };
  
  // 页面设置
  pageSettings: {
    showPageHeader: boolean;
    showBreadcrumb: boolean;
    showFooter: boolean;
  };
  
  // 响应式状态
  device: 'desktop' | 'tablet' | 'mobile';
  screenSize: {
    width: number;
    height: number;
  };
}

const initialState: UIState = {
  sidebarCollapsed: false,
  sidebarVisible: true,
  theme: 'light',
  primaryColor: '#1890ff',
  locale: 'zh-CN',
  layout: 'side',
  contentWidth: 'fluid',
  fixedHeader: true,
  fixedSidebar: true,
  globalLoading: false,
  pageLoading: false,
  modals: {
    searchVisible: false,
    profileVisible: false,
    settingsVisible: false,
  },
  notifications: {
    visible: false,
    unreadCount: 0,
  },
  breadcrumb: {
    visible: true,
    items: [],
  },
  pageSettings: {
    showPageHeader: true,
    showBreadcrumb: true,
    showFooter: true,
  },
  device: 'desktop',
  screenSize: {
    width: 1920,
    height: 1080,
  },
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    // 侧边栏控制
    toggleSidebar: (state) => {
      state.sidebarCollapsed = !state.sidebarCollapsed;
    },
    
    setSidebarCollapsed: (state, action: PayloadAction<boolean>) => {
      state.sidebarCollapsed = action.payload;
    },
    
    setSidebarVisible: (state, action: PayloadAction<boolean>) => {
      state.sidebarVisible = action.payload;
    },
    
    // 主题控制
    setTheme: (state, action: PayloadAction<'light' | 'dark'>) => {
      state.theme = action.payload;
    },
    
    setPrimaryColor: (state, action: PayloadAction<string>) => {
      state.primaryColor = action.payload;
    },
    
    // 语言设置
    setLocale: (state, action: PayloadAction<'zh-CN' | 'en-US'>) => {
      state.locale = action.payload;
    },
    
    // 布局设置
    setLayout: (state, action: PayloadAction<'side' | 'top' | 'mix'>) => {
      state.layout = action.payload;
    },
    
    setContentWidth: (state, action: PayloadAction<'fluid' | 'fixed'>) => {
      state.contentWidth = action.payload;
    },
    
    setFixedHeader: (state, action: PayloadAction<boolean>) => {
      state.fixedHeader = action.payload;
    },
    
    setFixedSidebar: (state, action: PayloadAction<boolean>) => {
      state.fixedSidebar = action.payload;
    },
    
    // 加载状态
    setGlobalLoading: (state, action: PayloadAction<boolean>) => {
      state.globalLoading = action.payload;
    },
    
    setPageLoading: (state, action: PayloadAction<boolean>) => {
      state.pageLoading = action.payload;
    },
    
    // 模态框控制
    setSearchVisible: (state, action: PayloadAction<boolean>) => {
      state.modals.searchVisible = action.payload;
    },
    
    setProfileVisible: (state, action: PayloadAction<boolean>) => {
      state.modals.profileVisible = action.payload;
    },
    
    setSettingsVisible: (state, action: PayloadAction<boolean>) => {
      state.modals.settingsVisible = action.payload;
    },
    
    // 通知控制
    setNotificationsVisible: (state, action: PayloadAction<boolean>) => {
      state.notifications.visible = action.payload;
    },
    
    setUnreadCount: (state, action: PayloadAction<number>) => {
      state.notifications.unreadCount = action.payload;
    },
    
    // 面包屑控制
    setBreadcrumbVisible: (state, action: PayloadAction<boolean>) => {
      state.breadcrumb.visible = action.payload;
    },
    
    setBreadcrumbItems: (state, action: PayloadAction<Array<{
      title: string;
      path?: string;
    }>>) => {
      state.breadcrumb.items = action.payload;
    },
    
    // 页面设置
    setPageSettings: (state, action: PayloadAction<Partial<UIState['pageSettings']>>) => {
      state.pageSettings = { ...state.pageSettings, ...action.payload };
    },
    
    // 响应式设置
    setDevice: (state, action: PayloadAction<'desktop' | 'tablet' | 'mobile'>) => {
      state.device = action.payload;
    },
    
    setScreenSize: (state, action: PayloadAction<{ width: number; height: number }>) => {
      state.screenSize = action.payload;
    },
    
    // 重置UI状态
    resetUI: (state) => {
      Object.assign(state, initialState);
    },
  },
});

export const {
  toggleSidebar,
  setSidebarCollapsed,
  setSidebarVisible,
  setTheme,
  setPrimaryColor,
  setLocale,
  setLayout,
  setContentWidth,
  setFixedHeader,
  setFixedSidebar,
  setGlobalLoading,
  setPageLoading,
  setSearchVisible,
  setProfileVisible,
  setSettingsVisible,
  setNotificationsVisible,
  setUnreadCount,
  setBreadcrumbVisible,
  setBreadcrumbItems,
  setPageSettings,
  setDevice,
  setScreenSize,
  resetUI,
} = uiSlice.actions;

export default uiSlice.reducer;
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
  lastLoginTime?: string;
  profile?: {
    nickname?: string;
    phone?: string;
    department?: string;
    position?: string;
  };
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
  loginTime?: string;
  expiresAt?: string;
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: null,
  refreshToken: null,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // 登录开始
    loginStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    
    // 登录成功
    loginSuccess: (state, action: PayloadAction<{
      user: User;
      token: string;
      refreshToken: string;
      expiresAt: string;
    }>) => {
      state.loading = false;
      state.isAuthenticated = true;
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.refreshToken = action.payload.refreshToken;
      state.expiresAt = action.payload.expiresAt;
      state.loginTime = new Date().toISOString();
      state.error = null;
    },
    
    // 登录失败
    loginFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      state.error = action.payload;
    },
    
    // 登出
    logout: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      state.loginTime = undefined;
      state.expiresAt = undefined;
      state.error = null;
    },
    
    // 更新用户信息
    updateUser: (state, action: PayloadAction<Partial<User>>) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
      }
    },
    
    // 更新用户头像
    updateAvatar: (state, action: PayloadAction<string>) => {
      if (state.user) {
        state.user.avatar = action.payload;
      }
    },
    
    // 更新用户资料
    updateProfile: (state, action: PayloadAction<User['profile']>) => {
      if (state.user) {
        state.user.profile = { ...state.user.profile, ...action.payload };
      }
    },
    
    // 刷新Token
    refreshTokenStart: (state) => {
      state.loading = true;
    },
    
    refreshTokenSuccess: (state, action: PayloadAction<{
      token: string;
      expiresAt: string;
    }>) => {
      state.loading = false;
      state.token = action.payload.token;
      state.expiresAt = action.payload.expiresAt;
    },
    
    refreshTokenFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
      // Token刷新失败，清除认证状态
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.refreshToken = null;
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
    
    // 设置加载状态
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
  },
});

export const {
  loginStart,
  loginSuccess,
  loginFailure,
  logout,
  updateUser,
  updateAvatar,
  updateProfile,
  refreshTokenStart,
  refreshTokenSuccess,
  refreshTokenFailure,
  clearError,
  setLoading,
} = authSlice.actions;

export default authSlice.reducer;
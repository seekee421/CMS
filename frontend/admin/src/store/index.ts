import { configureStore } from '@reduxjs/toolkit';
import authReducer, { AuthState } from './slices/authSlice';
import uiSlice from './slices/uiSlice';
import documentSlice from './slices/documentSlice';
import userSlice from './slices/userSlice';
import categorySlice from './slices/categorySlice';
import feedbackSlice from './slices/feedbackSlice';
import statisticsSlice from './slices/statisticsSlice';

function loadAuthState(): AuthState | null {
  try {
    const raw = localStorage.getItem('auth');
    if (!raw) return null;
    const parsed = JSON.parse(raw) as AuthState;
    if (!parsed?.token) return null;

    // 当后端未返回 expiresAt 时，尝试从 JWT 的 exp 字段解析；失败则保持原值
    if (!parsed.expiresAt) {
      try {
        const parts = parsed.token.split('.');
        if (parts.length >= 2) {
          const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
          const json = JSON.parse(atob(payload));
          if (typeof json.exp === 'number') {
            parsed.expiresAt = new Date(json.exp * 1000).toISOString();
          }
        }
      } catch {
        // ignore decode errors
      }
    }

    if (!parsed?.expiresAt) {
      // 默认会话有效期：2小时（仅用于前端持久化，后端仍以自身的 Token 校验为准）
      parsed.expiresAt = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
    }
    const expiresMs = new Date(parsed.expiresAt).getTime();
    if (isNaN(expiresMs) || expiresMs <= Date.now()) {
      localStorage.removeItem('auth');
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

const preloadedAuth = loadAuthState();
const preloadedState: { auth: AuthState } | undefined =
  preloadedAuth ? { auth: preloadedAuth } : undefined;

export const store = configureStore({
  reducer: {
    auth: authReducer,
    ui: uiSlice,
    document: documentSlice,
    user: userSlice,
    category: categorySlice,
    feedback: feedbackSlice,
    statistics: statisticsSlice,
  },
  preloadedState,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
  devTools: (((import.meta as any)?.env?.MODE) !== 'production'),
});

store.subscribe(() => {
  try {
    const state = store.getState() as { auth: AuthState };
    if (state.auth?.isAuthenticated && state.auth?.token) {
      localStorage.setItem('auth', JSON.stringify(state.auth));
    } else {
      localStorage.removeItem('auth');
    }
  } catch {
    // ignore storage errors
  }
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
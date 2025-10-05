import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import uiSlice from './slices/uiSlice';
import documentSlice from './slices/documentSlice';
import userSlice from './slices/userSlice';
import categorySlice from './slices/categorySlice';
import feedbackSlice from './slices/feedbackSlice';
import statisticsSlice from './slices/statisticsSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    ui: uiSlice,
    document: documentSlice,
    user: userSlice,
    category: categorySlice,
    feedback: feedbackSlice,
    statistics: statisticsSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
  devTools: process.env.NODE_ENV !== 'production',
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
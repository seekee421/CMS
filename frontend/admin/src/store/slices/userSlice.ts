import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Role {
  id: string;
  name: string;
  code: string;
  description?: string;
  permissions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  id: string;
  name: string;
  code: string;
  resource: string;
  action: string;
  description?: string;
}

export interface UserInfo {
  id: string;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  nickname?: string;
  department?: string;
  position?: string;
  status: 'active' | 'inactive' | 'locked';
  roles: Role[];
  lastLoginTime?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserFilter {
  keyword?: string;
  status?: UserInfo['status'];
  roleId?: string;
  department?: string;
  dateRange?: [string, string];
}

export interface UserState {
  // 用户列表
  users: UserInfo[];
  total: number;
  loading: boolean;
  error: string | null;
  
  // 当前用户
  currentUser: UserInfo | null;
  currentLoading: boolean;
  
  // 筛选和分页
  filters: UserFilter;
  pagination: {
    current: number;
    pageSize: number;
  };
  
  // 角色管理
  roles: Role[];
  rolesLoading: boolean;
  
  // 权限管理
  permissions: Permission[];
  permissionsLoading: boolean;
  
  // 编辑状态
  editing: {
    user: UserInfo | null;
    loading: boolean;
    saving: boolean;
  };
}

const initialState: UserState = {
  users: [],
  total: 0,
  loading: false,
  error: null,
  currentUser: null,
  currentLoading: false,
  filters: {},
  pagination: {
    current: 1,
    pageSize: 20,
  },
  roles: [],
  rolesLoading: false,
  permissions: [],
  permissionsLoading: false,
  editing: {
    user: null,
    loading: false,
    saving: false,
  },
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    // 用户列表
    fetchUsersStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    
    fetchUsersSuccess: (state, action: PayloadAction<{
      users: UserInfo[];
      total: number;
    }>) => {
      state.loading = false;
      state.users = action.payload.users;
      state.total = action.payload.total;
    },
    
    fetchUsersFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },
    
    // 当前用户
    fetchUserStart: (state) => {
      state.currentLoading = true;
      state.error = null;
    },
    
    fetchUserSuccess: (state, action: PayloadAction<UserInfo>) => {
      state.currentLoading = false;
      state.currentUser = action.payload;
    },
    
    fetchUserFailure: (state, action: PayloadAction<string>) => {
      state.currentLoading = false;
      state.error = action.payload;
    },
    
    // 筛选和分页
    setFilters: (state, action: PayloadAction<UserFilter>) => {
      state.filters = action.payload;
      state.pagination.current = 1;
    },
    
    setPagination: (state, action: PayloadAction<{
      current: number;
      pageSize: number;
    }>) => {
      state.pagination = action.payload;
    },
    
    // 角色管理
    fetchRolesStart: (state) => {
      state.rolesLoading = true;
    },
    
    fetchRolesSuccess: (state, action: PayloadAction<Role[]>) => {
      state.rolesLoading = false;
      state.roles = action.payload;
    },
    
    fetchRolesFailure: (state, action: PayloadAction<string>) => {
      state.rolesLoading = false;
      state.error = action.payload;
    },
    
    // 权限管理
    fetchPermissionsStart: (state) => {
      state.permissionsLoading = true;
    },
    
    fetchPermissionsSuccess: (state, action: PayloadAction<Permission[]>) => {
      state.permissionsLoading = false;
      state.permissions = action.payload;
    },
    
    fetchPermissionsFailure: (state, action: PayloadAction<string>) => {
      state.permissionsLoading = false;
      state.error = action.payload;
    },
    
    // 编辑状态
    startEditingUser: (state, action: PayloadAction<UserInfo>) => {
      state.editing.user = action.payload;
    },
    
    saveUserStart: (state) => {
      state.editing.saving = true;
    },
    
    saveUserSuccess: (state, action: PayloadAction<UserInfo>) => {
      state.editing.saving = false;
      state.editing.user = null;
      
      // 更新列表中的用户
      const index = state.users.findIndex(user => user.id === action.payload.id);
      if (index !== -1) {
        state.users[index] = action.payload;
      } else {
        // 新增用户
        state.users.unshift(action.payload);
        state.total += 1;
      }
      
      // 更新当前用户
      if (state.currentUser?.id === action.payload.id) {
        state.currentUser = action.payload;
      }
    },
    
    saveUserFailure: (state, action: PayloadAction<string>) => {
      state.editing.saving = false;
      state.error = action.payload;
    },
    
    stopEditingUser: (state) => {
      state.editing.user = null;
    },
    
    // 用户操作
    deleteUser: (state, action: PayloadAction<string>) => {
      state.users = state.users.filter(user => user.id !== action.payload);
      state.total -= 1;
      
      if (state.currentUser?.id === action.payload) {
        state.currentUser = null;
      }
    },
    
    updateUserStatus: (state, action: PayloadAction<{
      id: string;
      status: UserInfo['status'];
    }>) => {
      const user = state.users.find(user => user.id === action.payload.id);
      if (user) {
        user.status = action.payload.status;
      }
      
      if (state.currentUser?.id === action.payload.id) {
        state.currentUser.status = action.payload.status;
      }
    },
    
    // 角色操作
    addRole: (state, action: PayloadAction<Role>) => {
      state.roles.unshift(action.payload);
    },
    
    updateRole: (state, action: PayloadAction<Role>) => {
      const index = state.roles.findIndex(role => role.id === action.payload.id);
      if (index !== -1) {
        state.roles[index] = action.payload;
      }
    },
    
    deleteRole: (state, action: PayloadAction<string>) => {
      state.roles = state.roles.filter(role => role.id !== action.payload);
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
  },
});

export const {
  fetchUsersStart,
  fetchUsersSuccess,
  fetchUsersFailure,
  fetchUserStart,
  fetchUserSuccess,
  fetchUserFailure,
  setFilters,
  setPagination,
  fetchRolesStart,
  fetchRolesSuccess,
  fetchRolesFailure,
  fetchPermissionsStart,
  fetchPermissionsSuccess,
  fetchPermissionsFailure,
  startEditingUser,
  saveUserStart,
  saveUserSuccess,
  saveUserFailure,
  stopEditingUser,
  deleteUser,
  updateUserStatus,
  addRole,
  updateRole,
  deleteRole,
  clearError,
} = userSlice.actions;

export default userSlice.reducer;
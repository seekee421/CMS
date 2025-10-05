import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Category {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  level: number;
  sort: number;
  icon?: string;
  color?: string;
  status: 'active' | 'inactive';
  documentCount: number;
  children?: Category[];
  createdAt: string;
  updatedAt: string;
}

export interface CategoryState {
  // 分类列表
  categories: Category[];
  flatCategories: Category[]; // 扁平化的分类列表，用于搜索和筛选
  loading: boolean;
  error: string | null;
  
  // 当前分类
  currentCategory: Category | null;
  currentLoading: boolean;
  
  // 编辑状态
  editing: {
    category: Category | null;
    loading: boolean;
    saving: boolean;
  };
  
  // 拖拽排序状态
  dragging: {
    isDragging: boolean;
    draggedItem: Category | null;
  };
}

const initialState: CategoryState = {
  categories: [],
  flatCategories: [],
  loading: false,
  error: null,
  currentCategory: null,
  currentLoading: false,
  editing: {
    category: null,
    loading: false,
    saving: false,
  },
  dragging: {
    isDragging: false,
    draggedItem: null,
  },
};

const categorySlice = createSlice({
  name: 'category',
  initialState,
  reducers: {
    // 分类列表
    fetchCategoriesStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    
    fetchCategoriesSuccess: (state, action: PayloadAction<Category[]>) => {
      state.loading = false;
      state.categories = action.payload;
      // 生成扁平化列表
      state.flatCategories = flattenCategories(action.payload);
    },
    
    fetchCategoriesFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },
    
    // 当前分类
    fetchCategoryStart: (state) => {
      state.currentLoading = true;
      state.error = null;
    },
    
    fetchCategorySuccess: (state, action: PayloadAction<Category>) => {
      state.currentLoading = false;
      state.currentCategory = action.payload;
    },
    
    fetchCategoryFailure: (state, action: PayloadAction<string>) => {
      state.currentLoading = false;
      state.error = action.payload;
    },
    
    // 编辑状态
    startEditingCategory: (state, action: PayloadAction<Category | null>) => {
      state.editing.category = action.payload;
    },
    
    saveCategoryStart: (state) => {
      state.editing.saving = true;
    },
    
    saveCategorySuccess: (state, action: PayloadAction<Category>) => {
      state.editing.saving = false;
      state.editing.category = null;
      
      const savedCategory = action.payload;
      
      if (savedCategory.parentId) {
        // 更新或添加子分类
        updateCategoryInTree(state.categories, savedCategory);
      } else {
        // 更新或添加根分类
        const index = state.categories.findIndex(cat => cat.id === savedCategory.id);
        if (index !== -1) {
          state.categories[index] = savedCategory;
        } else {
          state.categories.push(savedCategory);
        }
      }
      
      // 重新生成扁平化列表
      state.flatCategories = flattenCategories(state.categories);
      
      // 更新当前分类
      if (state.currentCategory?.id === savedCategory.id) {
        state.currentCategory = savedCategory;
      }
    },
    
    saveCategoryFailure: (state, action: PayloadAction<string>) => {
      state.editing.saving = false;
      state.error = action.payload;
    },
    
    stopEditingCategory: (state) => {
      state.editing.category = null;
    },
    
    // 分类操作
    deleteCategory: (state, action: PayloadAction<string>) => {
      const categoryId = action.payload;
      
      // 从树结构中删除
      state.categories = removeCategoryFromTree(state.categories, categoryId);
      
      // 重新生成扁平化列表
      state.flatCategories = flattenCategories(state.categories);
      
      // 清除当前分类
      if (state.currentCategory?.id === categoryId) {
        state.currentCategory = null;
      }
    },
    
    updateCategoryStatus: (state, action: PayloadAction<{
      id: string;
      status: Category['status'];
    }>) => {
      const { id, status } = action.payload;
      
      // 更新树结构中的状态
      updateCategoryStatusInTree(state.categories, id, status);
      
      // 重新生成扁平化列表
      state.flatCategories = flattenCategories(state.categories);
      
      // 更新当前分类
      if (state.currentCategory?.id === id) {
        state.currentCategory.status = status;
      }
    },
    
    // 拖拽排序
    startDragging: (state, action: PayloadAction<Category>) => {
      state.dragging.isDragging = true;
      state.dragging.draggedItem = action.payload;
    },
    
    stopDragging: (state) => {
      state.dragging.isDragging = false;
      state.dragging.draggedItem = null;
    },
    
    reorderCategories: (state, action: PayloadAction<{
      draggedId: string;
      targetId: string;
      position: 'before' | 'after' | 'inside';
    }>) => {
      const { draggedId, targetId, position } = action.payload;
      
      // 执行重排序逻辑
      state.categories = reorderCategoriesInTree(
        state.categories,
        draggedId,
        targetId,
        position
      );
      
      // 重新生成扁平化列表
      state.flatCategories = flattenCategories(state.categories);
    },
    
    // 清除错误
    clearError: (state) => {
      state.error = null;
    },
  },
});

// 辅助函数：扁平化分类树
function flattenCategories(categories: Category[]): Category[] {
  const result: Category[] = [];
  
  function traverse(cats: Category[]) {
    cats.forEach(cat => {
      result.push(cat);
      if (cat.children && cat.children.length > 0) {
        traverse(cat.children);
      }
    });
  }
  
  traverse(categories);
  return result;
}

// 辅助函数：在树结构中更新分类
function updateCategoryInTree(categories: Category[], updatedCategory: Category): void {
  for (let i = 0; i < categories.length; i++) {
    if (categories[i].id === updatedCategory.id) {
      categories[i] = updatedCategory;
      return;
    }
    
    if (categories[i].children) {
      updateCategoryInTree(categories[i].children!, updatedCategory);
    }
  }
}

// 辅助函数：从树结构中删除分类
function removeCategoryFromTree(categories: Category[], categoryId: string): Category[] {
  return categories.filter(cat => {
    if (cat.id === categoryId) {
      return false;
    }
    
    if (cat.children) {
      cat.children = removeCategoryFromTree(cat.children, categoryId);
    }
    
    return true;
  });
}

// 辅助函数：更新分类状态
function updateCategoryStatusInTree(
  categories: Category[],
  categoryId: string,
  status: Category['status']
): void {
  categories.forEach(cat => {
    if (cat.id === categoryId) {
      cat.status = status;
    }
    
    if (cat.children) {
      updateCategoryStatusInTree(cat.children, categoryId, status);
    }
  });
}

// 辅助函数：重排序分类
function reorderCategoriesInTree(
  categories: Category[],
  _draggedId: string,
  _targetId: string,
  _position: 'before' | 'after' | 'inside'
): Category[] {
  // 这里实现复杂的拖拽重排序逻辑
  // 为了简化，这里返回原数组
  // 实际实现需要根据具体的拖拽逻辑来处理
  return categories;
}

export const {
  fetchCategoriesStart,
  fetchCategoriesSuccess,
  fetchCategoriesFailure,
  fetchCategoryStart,
  fetchCategorySuccess,
  fetchCategoryFailure,
  startEditingCategory,
  saveCategoryStart,
  saveCategorySuccess,
  saveCategoryFailure,
  stopEditingCategory,
  deleteCategory,
  updateCategoryStatus,
  startDragging,
  stopDragging,
  reorderCategories,
  clearError,
} = categorySlice.actions;

export default categorySlice.reducer;
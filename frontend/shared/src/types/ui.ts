// UI组件相关类型定义

// 基础UI类型
export type Size = 'small' | 'medium' | 'large';
export type Variant = 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
export type Theme = 'light' | 'dark' | 'auto';
export type Direction = 'ltr' | 'rtl';

// 颜色类型
export interface ColorPalette {
  primary: string;
  secondary: string;
  success: string;
  warning: string;
  error: string;
  info: string;
  background: string;
  surface: string;
  text: {
    primary: string;
    secondary: string;
    disabled: string;
  };
  border: string;
  divider: string;
}

// 主题配置
export interface ThemeConfig {
  mode: Theme;
  colors: {
    light: ColorPalette;
    dark: ColorPalette;
  };
  typography: TypographyConfig;
  spacing: SpacingConfig;
  breakpoints: BreakpointConfig;
  shadows: ShadowConfig;
  borderRadius: BorderRadiusConfig;
  zIndex: ZIndexConfig;
}

export interface TypographyConfig {
  fontFamily: {
    primary: string;
    secondary: string;
    monospace: string;
  };
  fontSize: {
    xs: string;
    sm: string;
    base: string;
    lg: string;
    xl: string;
    '2xl': string;
    '3xl': string;
    '4xl': string;
  };
  fontWeight: {
    light: number;
    normal: number;
    medium: number;
    semibold: number;
    bold: number;
  };
  lineHeight: {
    tight: number;
    normal: number;
    relaxed: number;
  };
}

export interface SpacingConfig {
  xs: string;
  sm: string;
  md: string;
  lg: string;
  xl: string;
  '2xl': string;
  '3xl': string;
  '4xl': string;
}

export interface BreakpointConfig {
  xs: string;
  sm: string;
  md: string;
  lg: string;
  xl: string;
  '2xl': string;
}

export interface ShadowConfig {
  sm: string;
  md: string;
  lg: string;
  xl: string;
  '2xl': string;
}

export interface BorderRadiusConfig {
  none: string;
  sm: string;
  md: string;
  lg: string;
  xl: string;
  full: string;
}

export interface ZIndexConfig {
  dropdown: number;
  sticky: number;
  fixed: number;
  modal: number;
  popover: number;
  tooltip: number;
  toast: number;
}

// 表单相关类型
export interface FormField {
  name: string;
  label: string;
  type: FormFieldType;
  required?: boolean;
  disabled?: boolean;
  placeholder?: string;
  helperText?: string;
  validation?: FormValidation;
  options?: FormOption[];
  defaultValue?: any;
}

export type FormFieldType = 
  | 'text' 
  | 'email' 
  | 'password' 
  | 'number' 
  | 'tel' 
  | 'url' 
  | 'textarea' 
  | 'select' 
  | 'multiselect' 
  | 'checkbox' 
  | 'radio' 
  | 'switch' 
  | 'date' 
  | 'datetime' 
  | 'time' 
  | 'file' 
  | 'image' 
  | 'editor' 
  | 'tags';

export interface FormOption {
  label: string;
  value: any;
  disabled?: boolean;
  description?: string;
}

export interface FormValidation {
  required?: boolean;
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  custom?: (value: any) => string | null;
}

export interface FormError {
  field: string;
  message: string;
}

export interface FormState {
  values: Record<string, any>;
  errors: Record<string, string>;
  touched: Record<string, boolean>;
  isSubmitting: boolean;
  isValid: boolean;
}

// 表格相关类型
export interface TableColumn<T = any> {
  key: string;
  title: string;
  dataIndex?: keyof T;
  width?: number | string;
  fixed?: 'left' | 'right';
  sortable?: boolean;
  filterable?: boolean;
  searchable?: boolean;
  render?: (value: any, record: T, index: number) => React.ReactNode;
  align?: 'left' | 'center' | 'right';
  ellipsis?: boolean;
  filters?: TableFilter[];
}

export interface TableFilter {
  text: string;
  value: any;
}

export interface TablePagination {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger?: boolean;
  showQuickJumper?: boolean;
  showTotal?: (total: number, range: [number, number]) => string;
  pageSizeOptions?: string[];
}

export interface TableSelection<T = any> {
  selectedRowKeys: React.Key[];
  selectedRows: T[];
  onChange: (selectedRowKeys: React.Key[], selectedRows: T[]) => void;
  onSelect?: (record: T, selected: boolean, selectedRows: T[], nativeEvent: Event) => void;
  onSelectAll?: (selected: boolean, selectedRows: T[], changeRows: T[]) => void;
}

export interface TableSort {
  field: string;
  order: 'ascend' | 'descend' | null;
}

// 导航相关类型
export interface MenuItem {
  key: string;
  label: string;
  icon?: React.ReactNode;
  path?: string;
  children?: MenuItem[];
  disabled?: boolean;
  hidden?: boolean;
  badge?: string | number;
  permissions?: string[];
}

export interface BreadcrumbItem {
  title: string;
  path?: string;
  icon?: React.ReactNode;
}

export interface TabItem {
  key: string;
  label: string;
  content: React.ReactNode;
  icon?: React.ReactNode;
  disabled?: boolean;
  closable?: boolean;
}

// 模态框相关类型
export interface ModalProps {
  open: boolean;
  title?: string;
  content?: React.ReactNode;
  footer?: React.ReactNode;
  width?: number | string;
  height?: number | string;
  centered?: boolean;
  closable?: boolean;
  maskClosable?: boolean;
  keyboard?: boolean;
  destroyOnClose?: boolean;
  onOk?: () => void | Promise<void>;
  onCancel?: () => void;
  onClose?: () => void;
  confirmLoading?: boolean;
  okText?: string;
  cancelText?: string;
  okButtonProps?: any;
  cancelButtonProps?: any;
}

// 通知相关类型
export interface NotificationConfig {
  type: 'success' | 'info' | 'warning' | 'error';
  title: string;
  message?: string;
  duration?: number;
  placement?: 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight';
  icon?: React.ReactNode;
  action?: React.ReactNode;
  onClose?: () => void;
}

export interface ToastConfig {
  type: 'success' | 'info' | 'warning' | 'error';
  message: string;
  duration?: number;
  position?: 'top' | 'bottom' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
  action?: {
    label: string;
    onClick: () => void;
  };
}

// 加载状态类型
export interface LoadingState {
  loading: boolean;
  error?: string | null;
  data?: any;
}

export interface AsyncState<T = any> extends LoadingState {
  data?: T;
}

// 图表相关类型
export interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'area' | 'scatter' | 'radar';
  data: ChartDataPoint[];
  xAxis?: ChartAxis;
  yAxis?: ChartAxis;
  legend?: ChartLegend;
  tooltip?: ChartTooltip;
  colors?: string[];
  responsive?: boolean;
  animation?: boolean;
}

export interface ChartDataPoint {
  [key: string]: any;
}

export interface ChartAxis {
  label?: string;
  type?: 'category' | 'value' | 'time';
  min?: number;
  max?: number;
  format?: (value: any) => string;
}

export interface ChartLegend {
  show?: boolean;
  position?: 'top' | 'bottom' | 'left' | 'right';
}

export interface ChartTooltip {
  show?: boolean;
  format?: (value: any, label: string) => string;
}

// 编辑器相关类型
export interface EditorConfig {
  theme: 'light' | 'dark';
  language: string;
  readOnly?: boolean;
  lineNumbers?: boolean;
  wordWrap?: boolean;
  minimap?: boolean;
  autoSave?: boolean;
  autoSaveInterval?: number;
  fontSize?: number;
  tabSize?: number;
  insertSpaces?: boolean;
}

export interface EditorAction {
  id: string;
  label: string;
  keybinding?: string;
  contextMenuGroupId?: string;
  run: () => void;
}

// 搜索相关类型
export interface SearchConfig {
  placeholder?: string;
  allowClear?: boolean;
  showSuggestions?: boolean;
  maxSuggestions?: number;
  debounceTime?: number;
  minLength?: number;
  highlightMatches?: boolean;
}

export interface SearchSuggestion {
  value: string;
  label?: string;
  category?: string;
  count?: number;
}

// 文件上传相关类型
export interface UploadConfig {
  accept?: string;
  multiple?: boolean;
  maxSize?: number;
  maxCount?: number;
  directory?: boolean;
  showUploadList?: boolean;
  listType?: 'text' | 'picture' | 'picture-card';
  beforeUpload?: (file: File) => boolean | Promise<boolean>;
  onProgress?: (percent: number, file: File) => void;
  onSuccess?: (response: any, file: File) => void;
  onError?: (error: Error, file: File) => void;
}

export interface UploadFile {
  uid: string;
  name: string;
  status: 'uploading' | 'done' | 'error' | 'removed';
  url?: string;
  thumbUrl?: string;
  size?: number;
  type?: string;
  percent?: number;
  error?: Error;
  response?: any;
  originFileObj?: File;
}

// 拖拽相关类型
export interface DragDropConfig {
  accept?: string[];
  multiple?: boolean;
  disabled?: boolean;
  onDrop?: (files: File[]) => void;
  onDragEnter?: () => void;
  onDragLeave?: () => void;
  onDragOver?: () => void;
}

// 虚拟滚动相关类型
export interface VirtualScrollConfig {
  itemHeight: number;
  containerHeight: number;
  overscan?: number;
  scrollToIndex?: number;
  onScroll?: (scrollTop: number) => void;
}

// 响应式相关类型
export interface ResponsiveConfig {
  xs?: any;
  sm?: any;
  md?: any;
  lg?: any;
  xl?: any;
  xxl?: any;
}

// 动画相关类型
export interface AnimationConfig {
  duration?: number;
  easing?: string;
  delay?: number;
  direction?: 'normal' | 'reverse' | 'alternate' | 'alternate-reverse';
  fillMode?: 'none' | 'forwards' | 'backwards' | 'both';
  iterationCount?: number | 'infinite';
}

// 国际化相关类型
export interface LocaleConfig {
  code: string;
  name: string;
  nativeName: string;
  flag?: string;
  rtl?: boolean;
  messages: Record<string, string>;
}

export interface I18nConfig {
  defaultLocale: string;
  locales: LocaleConfig[];
  fallbackLocale?: string;
  interpolation?: {
    prefix?: string;
    suffix?: string;
  };
}

// 键盘快捷键类型
export interface KeyboardShortcut {
  key: string;
  ctrlKey?: boolean;
  altKey?: boolean;
  shiftKey?: boolean;
  metaKey?: boolean;
  action: () => void;
  description?: string;
  disabled?: boolean;
}

// 布局相关类型
export interface LayoutConfig {
  header?: {
    height: number;
    fixed?: boolean;
    background?: string;
  };
  sidebar?: {
    width: number;
    collapsedWidth?: number;
    collapsible?: boolean;
    collapsed?: boolean;
    background?: string;
  };
  footer?: {
    height: number;
    fixed?: boolean;
    background?: string;
  };
  content?: {
    padding?: string;
    background?: string;
  };
}

// 导出React相关类型
export type ReactNode = React.ReactNode;
export type ReactElement = React.ReactElement;
export type ReactComponent<P = {}> = React.ComponentType<P>;
export type ReactFC<P = {}> = React.FC<P>;
export type ReactRef<T = any> = React.Ref<T>;
export type ReactKey = React.Key;
export type ReactEvent<T = Element> = React.SyntheticEvent<T>;
export type ReactMouseEvent<T = Element> = React.MouseEvent<T>;
export type ReactKeyboardEvent<T = Element> = React.KeyboardEvent<T>;
export type ReactChangeEvent<T = Element> = React.ChangeEvent<T>;
export type ReactFormEvent<T = Element> = React.FormEvent<T>;
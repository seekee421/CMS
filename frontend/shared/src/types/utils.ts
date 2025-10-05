// 工具类型定义

// 基础工具类型
export type Nullable<T> = T | null;
export type Optional<T> = T | undefined;
export type Maybe<T> = T | null | undefined;

// 对象工具类型
export type Partial<T> = {
  [P in keyof T]?: T[P];
};

export type Required<T> = {
  [P in keyof T]-?: T[P];
};

export type Readonly<T> = {
  readonly [P in keyof T]: T[P];
};

export type Pick<T, K extends keyof T> = {
  [P in K]: T[P];
};

export type Omit<T, K extends keyof any> = Pick<T, Exclude<keyof T, K>>;

export type Record<K extends keyof any, T> = {
  [P in K]: T;
};

// 函数工具类型
export type Func<T extends any[] = any[], R = any> = (...args: T) => R;
export type AsyncFunc<T extends any[] = any[], R = any> = (...args: T) => Promise<R>;
export type VoidFunc<T extends any[] = any[]> = (...args: T) => void;
export type AsyncVoidFunc<T extends any[] = any[]> = (...args: T) => Promise<void>;

// 事件处理器类型
export type EventHandler<T = any> = (event: T) => void;
export type AsyncEventHandler<T = any> = (event: T) => Promise<void>;

// 回调函数类型
export type Callback<T = void> = () => T;
export type AsyncCallback<T = void> = () => Promise<T>;
export type CallbackWithParam<P, T = void> = (param: P) => T;
export type AsyncCallbackWithParam<P, T = void> = (param: P) => Promise<T>;

// 数组工具类型
export type ArrayElement<T> = T extends readonly (infer U)[] ? U : never;
export type NonEmptyArray<T> = [T, ...T[]];
export type ReadonlyArray<T> = readonly T[];

// 字符串工具类型
export type StringKeys<T> = Extract<keyof T, string>;
export type NumberKeys<T> = Extract<keyof T, number>;
export type SymbolKeys<T> = Extract<keyof T, symbol>;

// 深度工具类型
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

export type DeepRequired<T> = {
  [P in keyof T]-?: T[P] extends object ? DeepRequired<T[P]> : T[P];
};

export type DeepReadonly<T> = {
  readonly [P in keyof T]: T[P] extends object ? DeepReadonly<T[P]> : T[P];
};

// 条件类型
export type If<C extends boolean, T, F> = C extends true ? T : F;
export type IsEqual<T, U> = T extends U ? (U extends T ? true : false) : false;
export type IsNever<T> = [T] extends [never] ? true : false;
export type IsAny<T> = 0 extends (1 & T) ? true : false;
export type IsUnknown<T> = IsAny<T> extends true ? false : unknown extends T ? true : false;

// 联合类型工具
export type UnionToIntersection<U> = (U extends any ? (k: U) => void : never) extends (k: infer I) => void ? I : never;
export type LastOfUnion<T> = UnionToIntersection<T extends any ? () => T : never> extends () => infer R ? R : never;
export type UnionToTuple<T, L = LastOfUnion<T>, N = [T] extends [never] ? true : false> = true extends N ? [] : [...UnionToTuple<Exclude<T, L>>, L];

// 键值对工具类型
export type KeyValuePair<K = string, V = any> = {
  key: K;
  value: V;
};

export type Dictionary<T = any> = Record<string, T>;
export type NumericDictionary<T = any> = Record<number, T>;

// 时间相关类型
export type Timestamp = number;
export type DateString = string; // ISO 8601 format
export type TimeString = string; // HH:mm:ss format

// ID类型
export type ID = string | number;
export type UUID = string;

// 状态类型
export type Status = 'idle' | 'loading' | 'success' | 'error';
export type AsyncStatus = 'pending' | 'fulfilled' | 'rejected';

// 排序类型
export type SortOrder = 'asc' | 'desc';
export type SortDirection = 'ascending' | 'descending';

// 比较函数类型
export type Comparator<T> = (a: T, b: T) => number;
export type Predicate<T> = (item: T) => boolean;
export type AsyncPredicate<T> = (item: T) => Promise<boolean>;

// 映射函数类型
export type Mapper<T, U> = (item: T, index?: number, array?: T[]) => U;
export type AsyncMapper<T, U> = (item: T, index?: number, array?: T[]) => Promise<U>;

// 过滤器类型
export type Filter<T> = Predicate<T>;
export type AsyncFilter<T> = AsyncPredicate<T>;

// 归约函数类型
export type Reducer<T, U> = (accumulator: U, current: T, index?: number, array?: T[]) => U;
export type AsyncReducer<T, U> = (accumulator: U, current: T, index?: number, array?: T[]) => Promise<U>;

// 验证相关类型
export type Validator<T> = (value: T) => boolean;
export type AsyncValidator<T> = (value: T) => Promise<boolean>;
export type ValidatorWithMessage<T> = (value: T) => string | null;
export type AsyncValidatorWithMessage<T> = (value: T) => Promise<string | null>;

// 序列化类型
export type Serializable = string | number | boolean | null | undefined | Serializable[] | { [key: string]: Serializable };
export type JSONValue = string | number | boolean | null | JSONValue[] | { [key: string]: JSONValue };

// 错误类型
export type ErrorCode = string | number;
export type ErrorMessage = string;
export type ErrorDetails = Record<string, any>;

export interface BaseError {
  code: ErrorCode;
  message: ErrorMessage;
  details?: ErrorDetails;
  timestamp?: Timestamp;
  stack?: string;
}

export interface ValidationError extends BaseError {
  field?: string;
  value?: any;
}

export interface NetworkError extends BaseError {
  status?: number;
  statusText?: string;
  url?: string;
}

export interface BusinessError extends BaseError {
  category?: string;
  severity?: 'low' | 'medium' | 'high' | 'critical';
}

// 配置类型
export interface BaseConfig {
  enabled?: boolean;
  debug?: boolean;
  timeout?: number;
  retries?: number;
}

export interface CacheConfig extends BaseConfig {
  ttl?: number;
  maxSize?: number;
  strategy?: 'lru' | 'fifo' | 'lfu';
}

export interface RetryConfig extends BaseConfig {
  maxRetries?: number;
  backoff?: 'linear' | 'exponential';
  delay?: number;
  maxDelay?: number;
}

// 日志类型
export type LogLevel = 'trace' | 'debug' | 'info' | 'warn' | 'error' | 'fatal';

export interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: Timestamp;
  context?: Record<string, any>;
  error?: Error;
}

// 度量类型
export interface Metrics {
  count: number;
  duration?: number;
  memory?: number;
  cpu?: number;
  timestamp: Timestamp;
}

export interface PerformanceMetrics extends Metrics {
  startTime: number;
  endTime: number;
  operations: number;
  throughput: number;
}

// 分页工具类型
export interface PaginationMeta {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface CursorPagination {
  cursor?: string;
  limit: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// 搜索工具类型
export interface SearchMeta {
  query: string;
  total: number;
  took: number;
  maxScore?: number;
}

export interface SearchHighlight {
  field: string;
  fragments: string[];
}

// 文件工具类型
export type FileSize = number; // bytes
export type MimeType = string;
export type FileExtension = string;

export interface FileInfo {
  name: string;
  size: FileSize;
  type: MimeType;
  extension: FileExtension;
  lastModified: Timestamp;
}

// 地理位置类型
export interface Coordinates {
  latitude: number;
  longitude: number;
  altitude?: number;
}

export interface Location extends Coordinates {
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
}

// 颜色类型
export type HexColor = string; // #RRGGBB
export type RgbColor = string; // rgb(r, g, b)
export type RgbaColor = string; // rgba(r, g, b, a)
export type HslColor = string; // hsl(h, s, l)
export type HslaColor = string; // hsla(h, s, l, a)
export type Color = HexColor | RgbColor | RgbaColor | HslColor | HslaColor;

// 尺寸类型
export interface Dimensions {
  width: number;
  height: number;
}

export interface Position {
  x: number;
  y: number;
}

export interface Rectangle extends Position, Dimensions {}

// 范围类型
export interface Range<T = number> {
  min: T;
  max: T;
}

export interface DateRange {
  start: Date;
  end: Date;
}

export interface TimeRange {
  start: TimeString;
  end: TimeString;
}

// 版本类型
export type Version = string; // semver format
export interface VersionInfo {
  major: number;
  minor: number;
  patch: number;
  prerelease?: string;
  build?: string;
}

// 环境类型
export type Environment = 'development' | 'staging' | 'production' | 'test';

// 平台类型
export type Platform = 'web' | 'mobile' | 'desktop' | 'server';
export type Browser = 'chrome' | 'firefox' | 'safari' | 'edge' | 'ie' | 'opera';
export type OperatingSystem = 'windows' | 'macos' | 'linux' | 'ios' | 'android';

// 设备类型
export interface DeviceInfo {
  platform: Platform;
  browser?: Browser;
  os?: OperatingSystem;
  version?: string;
  mobile?: boolean;
  tablet?: boolean;
  desktop?: boolean;
}

// 网络类型
export type NetworkStatus = 'online' | 'offline';
export type ConnectionType = 'wifi' | 'cellular' | 'ethernet' | 'bluetooth' | 'unknown';

export interface NetworkInfo {
  status: NetworkStatus;
  type?: ConnectionType;
  speed?: number; // Mbps
  latency?: number; // ms
}

// 权限类型
export type Permission = string;
export type Role = string;

export interface PermissionSet {
  permissions: Permission[];
  roles: Role[];
}

// 国际化工具类型
export type LocaleCode = string; // ISO 639-1
export type CountryCode = string; // ISO 3166-1
export type CurrencyCode = string; // ISO 4217
export type TimezoneCode = string; // IANA timezone

export interface Locale {
  code: LocaleCode;
  name: string;
  nativeName: string;
  country?: CountryCode;
  currency?: CurrencyCode;
  timezone?: TimezoneCode;
  rtl?: boolean;
}

// 主题工具类型
export type ThemeMode = 'light' | 'dark' | 'auto';
export type ColorScheme = 'light' | 'dark';

// 动画工具类型
export type AnimationDirection = 'normal' | 'reverse' | 'alternate' | 'alternate-reverse';
export type AnimationFillMode = 'none' | 'forwards' | 'backwards' | 'both';
export type AnimationTimingFunction = 'ease' | 'ease-in' | 'ease-out' | 'ease-in-out' | 'linear';

// 事件工具类型
export interface BaseEvent {
  type: string;
  timestamp: Timestamp;
  source?: string;
  metadata?: Record<string, any>;
}

export interface UserEvent extends BaseEvent {
  userId?: ID;
  sessionId?: string;
  action: string;
  target?: string;
}

// 存储工具类型
export type StorageType = 'localStorage' | 'sessionStorage' | 'indexedDB' | 'memory';

export interface StorageConfig {
  type: StorageType;
  prefix?: string;
  encryption?: boolean;
  compression?: boolean;
  ttl?: number;
}

// 队列工具类型
export interface QueueItem<T = any> {
  id: ID;
  data: T;
  priority?: number;
  timestamp: Timestamp;
  retries?: number;
  maxRetries?: number;
}

export interface QueueConfig {
  maxSize?: number;
  concurrency?: number;
  retryDelay?: number;
  maxRetries?: number;
}

// 缓存工具类型
export interface CacheItem<T = any> {
  key: string;
  value: T;
  timestamp: Timestamp;
  ttl?: number;
  size?: number;
}

export interface CacheStats {
  hits: number;
  misses: number;
  size: number;
  maxSize: number;
  hitRate: number;
}

// 工具函数类型
export type DeepClone<T> = T extends object ? { [K in keyof T]: DeepClone<T[K]> } : T;
export type Flatten<T> = T extends any[] ? T[number] : T;
export type Head<T extends any[]> = T extends readonly [any, ...any[]] ? T[0] : never;
export type Tail<T extends any[]> = T extends readonly [any, ...infer U] ? U : [];
export type Length<T extends any[]> = T['length'];
export type Reverse<T extends any[]> = T extends [...infer Rest, infer Last] ? [Last, ...Reverse<Rest>] : [];

// 类型守卫
export type TypeGuard<T> = (value: any) => boolean;
export type AsyncTypeGuard<T> = (value: any) => Promise<boolean>;

// 构造函数类型
export type Constructor<T = {}> = new (...args: any[]) => T;
export type AbstractConstructor<T = {}> = abstract new (...args: any[]) => T;

// 混入类型
export type Mixin<T extends Constructor> = T & Constructor;

// 装饰器类型
export type ClassDecorator = <TFunction extends Constructor>(target: TFunction) => TFunction | void;
export type PropertyDecorator = (target: any, propertyKey: string | symbol) => void;
export type MethodDecorator = <T>(target: any, propertyKey: string | symbol, descriptor: TypedPropertyDescriptor<T>) => TypedPropertyDescriptor<T> | void;
export type ParameterDecorator = (target: any, propertyKey: string | symbol | undefined, parameterIndex: number) => void;
// 导出API客户端
export * from './client';

// 导出版本控制API
export * from './version';

// 导出统计API
export * from './statistics';

// 重新导出主要的API类
export { VersionApi } from './version';
export { StatisticsApi } from './statistics';

// 默认导出
import { VersionApi } from './version';
import { StatisticsApi } from './statistics';

export default {
  Version: VersionApi,
  Statistics: StatisticsApi
};
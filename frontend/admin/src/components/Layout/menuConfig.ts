// 菜单配置

export interface MenuItem {
  key: string;
  label: string;
  icon?: any;
  children?: MenuItem[];
  permissions?: string[];
}

// 菜单项配置
export const menuItems: MenuItem[] = [
  {
    key: '/admin/dashboard',
    label: '仪表板',
    icon: 'DashboardOutlined',
  },
  {
    key: '/admin/documents',
    label: '文档管理',
    icon: 'FileTextOutlined',
    children: [
      {
        key: '/admin/documents/list',
        label: '文档列表',
        permissions: ['document:read'],
      },
      {
        key: '/admin/documents/create',
        label: '创建文档',
        permissions: ['document:create'],
      },
      {
        key: '/admin/documents/categories',
        label: '分类管理',
        permissions: ['category:read'],
      },
      {
        key: '/admin/documents/versions',
        label: '版本管理',
        permissions: ['document:version'],
      },
    ],
  },
  {
    key: '/admin/users',
    label: '用户管理',
    icon: 'UserOutlined',
    children: [
      {
        key: '/admin/users/list',
        label: '用户列表',
        permissions: ['user:read'],
      },
      {
        key: '/admin/users/roles',
        label: '角色管理',
        permissions: ['role:read'],
      },
      {
        key: '/admin/users/permissions',
        label: '权限管理',
        permissions: ['permission:read'],
      },
    ],
  },
  {
    key: '/admin/feedback',
    label: '反馈管理',
    icon: 'MessageOutlined',
    children: [
      {
        key: '/admin/feedback/list',
        label: '反馈列表',
        permissions: ['feedback:read'],
      },
      {
        key: '/admin/feedback/statistics',
        label: '反馈统计',
        permissions: ['feedback:statistics'],
      },
    ],
  },
  {
    key: '/admin/statistics',
    label: '数据统计',
    icon: 'BarChartOutlined',
    children: [
      {
        key: '/admin/statistics/overview',
        label: '概览统计',
        permissions: ['statistics:read'],
      },
      {
        key: '/admin/statistics/documents',
        label: '文档统计',
        permissions: ['statistics:document'],
      },
      {
        key: '/admin/statistics/users',
        label: '用户统计',
        permissions: ['statistics:user'],
      },
    ],
  },
  {
    key: '/admin/system',
    label: '系统管理',
    icon: 'SettingOutlined',
    children: [
      {
        key: '/admin/system/settings',
        label: '系统设置',
        permissions: ['system:settings'],
      },
      {
        key: '/admin/system/logs',
        label: '系统日志',
        permissions: ['system:logs'],
      },
      {
        key: '/admin/system/backup',
        label: '数据备份',
        permissions: ['system:backup'],
      },
    ],
  },
];
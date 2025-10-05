import React, { useEffect, useState } from 'react';
import {
  Row,
  Col,
  Card,
  Statistic,
  Table,
  List,
  Avatar,
  Tag,
  Progress,
  Typography,
  Space,
  Button,
  DatePicker,
  Select,
  Spin,
  Empty,
} from 'antd';
import {
  UserOutlined,
  FileTextOutlined,
  EyeOutlined,
  DownloadOutlined,
  TrophyOutlined,
  ClockCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Line, Column, Pie } from '@ant-design/plots';
import './index.less';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

interface StatisticCardProps {
  title: string;
  value: number;
  prefix?: React.ReactNode;
  suffix?: string;
  precision?: number;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  loading?: boolean;
}

const StatisticCard: React.FC<StatisticCardProps> = ({
  title,
  value,
  prefix,
  suffix,
  precision,
  trend,
  loading = false,
}) => (
  <Card>
    <Statistic
      title={title}
      value={value}
      prefix={prefix}
      suffix={suffix}
      precision={precision}
      loading={loading}
    />
    {trend && (
      <div className="statistic-trend">
        <Space>
          {trend.isPositive ? (
            <ArrowUpOutlined style={{ color: '#52c41a' }} />
          ) : (
            <ArrowDownOutlined style={{ color: '#ff4d4f' }} />
          )}
          <Text type={trend.isPositive ? 'success' : 'danger'}>
            {Math.abs(trend.value)}%
          </Text>
          <Text type="secondary">较上期</Text>
        </Space>
      </div>
    )}
  </Card>
);

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState<[string, string]>(['2024-01-01', '2024-12-31']);
  const [timeRange, setTimeRange] = useState<string>('month');

  // 模拟数据
  const [systemStats] = useState({
    totalDocuments: 1248,
    totalUsers: 356,
    totalViews: 45678,
    totalDownloads: 12345,
  });

  const [trendData] = useState({
    documents: { value: 12.5, isPositive: true },
    users: { value: 8.3, isPositive: true },
    views: { value: 15.7, isPositive: true },
    downloads: { value: 3.2, isPositive: false },
  });

  // 热门文档数据
  const [popularDocuments] = useState([
    {
      id: '1',
      title: 'DM8数据库安装指南',
      category: '安装部署',
      views: 2345,
      downloads: 567,
      author: '张三',
      updatedAt: '2024-01-15',
    },
    {
      id: '2',
      title: 'SQL语法参考手册',
      category: '开发指南',
      views: 1876,
      downloads: 432,
      author: '李四',
      updatedAt: '2024-01-14',
    },
    {
      id: '3',
      title: '性能优化最佳实践',
      category: '性能调优',
      views: 1654,
      downloads: 398,
      author: '王五',
      updatedAt: '2024-01-13',
    },
    {
      id: '4',
      title: '备份恢复操作手册',
      category: '运维管理',
      views: 1432,
      downloads: 321,
      author: '赵六',
      updatedAt: '2024-01-12',
    },
    {
      id: '5',
      title: '集群部署配置指南',
      category: '安装部署',
      views: 1298,
      downloads: 287,
      author: '孙七',
      updatedAt: '2024-01-11',
    },
  ]);

  // 最近活动数据
  const [recentActivities] = useState([
    {
      id: '1',
      type: 'document',
      action: '创建',
      title: 'DM8新特性介绍',
      user: '张三',
      time: '2小时前',
      avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=1',
    },
    {
      id: '2',
      type: 'user',
      action: '注册',
      title: '新用户李四注册',
      user: '李四',
      time: '3小时前',
      avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=2',
    },
    {
      id: '3',
      type: 'feedback',
      action: '反馈',
      title: '文档内容建议',
      user: '王五',
      time: '5小时前',
      avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=3',
    },
    {
      id: '4',
      type: 'document',
      action: '更新',
      title: 'API参考文档',
      user: '赵六',
      time: '1天前',
      avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=4',
    },
    {
      id: '5',
      type: 'document',
      action: '发布',
      title: '故障排查指南',
      user: '孙七',
      time: '2天前',
      avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=5',
    },
  ]);

  // 图表数据
  const [chartData] = useState({
    // 文档访问趋势
    viewTrend: [
      { date: '2024-01-01', views: 1200, downloads: 300 },
      { date: '2024-01-02', views: 1350, downloads: 320 },
      { date: '2024-01-03', views: 1180, downloads: 280 },
      { date: '2024-01-04', views: 1420, downloads: 350 },
      { date: '2024-01-05', views: 1680, downloads: 420 },
      { date: '2024-01-06', views: 1550, downloads: 380 },
      { date: '2024-01-07', views: 1750, downloads: 450 },
    ],
    // 分类分布
    categoryDistribution: [
      { category: '安装部署', count: 45, percentage: 25 },
      { category: '开发指南', count: 38, percentage: 21 },
      { category: '性能调优', count: 32, percentage: 18 },
      { category: '运维管理', count: 28, percentage: 16 },
      { category: '故障排查', count: 22, percentage: 12 },
      { category: '其他', count: 15, percentage: 8 },
    ],
  });

  const handleRefresh = () => {
    setLoading(true);
    // 模拟数据刷新
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'document':
        return <FileTextOutlined style={{ color: '#1890ff' }} />;
      case 'user':
        return <UserOutlined style={{ color: '#52c41a' }} />;
      case 'feedback':
        return <ClockCircleOutlined style={{ color: '#faad14' }} />;
      default:
        return <FileTextOutlined />;
    }
  };

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'document':
        return 'blue';
      case 'user':
        return 'green';
      case 'feedback':
        return 'orange';
      default:
        return 'default';
    }
  };

  // 热门文档表格列配置
  const documentColumns = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => (
        <Text strong style={{ color: '#1890ff', cursor: 'pointer' }}>
          {text}
        </Text>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag color="blue">{category}</Tag>,
    },
    {
      title: '浏览量',
      dataIndex: 'views',
      key: 'views',
      render: (views: number) => (
        <Space>
          <EyeOutlined />
          {views.toLocaleString()}
        </Space>
      ),
    },
    {
      title: '下载量',
      dataIndex: 'downloads',
      key: 'downloads',
      render: (downloads: number) => (
        <Space>
          <DownloadOutlined />
          {downloads.toLocaleString()}
        </Space>
      ),
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
    },
  ];

  // 访问趋势图配置
  const lineConfig = {
    data: chartData.viewTrend,
    xField: 'date',
    yField: 'views',
    seriesField: 'type',
    smooth: true,
    animation: {
      appear: {
        animation: 'path-in',
        duration: 1000,
      },
    },
  };

  // 分类分布图配置
  const pieConfig = {
    data: chartData.categoryDistribution,
    angleField: 'count',
    colorField: 'category',
    radius: 0.8,
    label: {
      type: 'outer',
      content: '{name} {percentage}%',
    },
    interactions: [
      {
        type: 'element-active',
      },
    ],
  };

  useEffect(() => {
    // 组件挂载时加载数据
    handleRefresh();
  }, []);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div className="header-content">
          <Title level={2}>仪表板</Title>
          <Space>
            <Select
              value={timeRange}
              onChange={setTimeRange}
              style={{ width: 120 }}
            >
              <Option value="today">今天</Option>
              <Option value="week">本周</Option>
              <Option value="month">本月</Option>
              <Option value="quarter">本季度</Option>
              <Option value="year">本年</Option>
            </Select>
            <RangePicker
              value={dateRange as any}
              onChange={(dates) => {
                if (dates) {
                  setDateRange([
                    dates[0]?.format('YYYY-MM-DD') || '',
                    dates[1]?.format('YYYY-MM-DD') || '',
                  ]);
                }
              }}
            />
            <Button
              icon={<ReloadOutlined />}
              onClick={handleRefresh}
              loading={loading}
            >
              刷新
            </Button>
          </Space>
        </div>
      </div>

      <Spin spinning={loading}>
        {/* 统计卡片 */}
        <Row gutter={[16, 16]} className="statistics-cards">
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              title="总文档数"
              value={systemStats.totalDocuments}
              prefix={<FileTextOutlined />}
              trend={trendData.documents}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              title="总用户数"
              value={systemStats.totalUsers}
              prefix={<UserOutlined />}
              trend={trendData.users}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              title="总浏览量"
              value={systemStats.totalViews}
              prefix={<EyeOutlined />}
              trend={trendData.views}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatisticCard
              title="总下载量"
              value={systemStats.totalDownloads}
              prefix={<DownloadOutlined />}
              trend={trendData.downloads}
            />
          </Col>
        </Row>

        {/* 图表区域 */}
        <Row gutter={[16, 16]} className="charts-section">
          <Col xs={24} lg={16}>
            <Card title="访问趋势" className="chart-card">
              <Line {...lineConfig} height={300} />
            </Card>
          </Col>
          <Col xs={24} lg={8}>
            <Card title="分类分布" className="chart-card">
              <Pie {...pieConfig} height={300} />
            </Card>
          </Col>
        </Row>

        {/* 数据表格和活动列表 */}
        <Row gutter={[16, 16]} className="data-section">
          <Col xs={24} lg={14}>
            <Card
              title={
                <Space>
                  <TrophyOutlined />
                  热门文档
                </Space>
              }
              extra={
                <Button type="link" size="small">
                  查看更多
                </Button>
              }
            >
              <Table
                columns={documentColumns}
                dataSource={popularDocuments}
                pagination={false}
                size="small"
                rowKey="id"
              />
            </Card>
          </Col>
          <Col xs={24} lg={10}>
            <Card
              title={
                <Space>
                  <ClockCircleOutlined />
                  最近活动
                </Space>
              }
              extra={
                <Button type="link" size="small">
                  查看更多
                </Button>
              }
            >
              <List
                dataSource={recentActivities}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={
                        <Avatar
                          src={item.avatar}
                          icon={getActivityIcon(item.type)}
                        />
                      }
                      title={
                        <Space>
                          <Text strong>{item.user}</Text>
                          <Text>{item.action}</Text>
                          <Tag color={getActivityColor(item.type)} size="small">
                            {item.type === 'document' && '文档'}
                            {item.type === 'user' && '用户'}
                            {item.type === 'feedback' && '反馈'}
                          </Tag>
                        </Space>
                      }
                      description={
                        <div>
                          <Text>{item.title}</Text>
                          <br />
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            {item.time}
                          </Text>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            </Card>
          </Col>
        </Row>

        {/* 系统状态 */}
        <Row gutter={[16, 16]} className="system-status">
          <Col xs={24} lg={12}>
            <Card title="系统状态">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text>CPU使用率</Text>
                  <Progress percent={45} status="active" />
                </div>
                <div>
                  <Text>内存使用率</Text>
                  <Progress percent={67} status="active" />
                </div>
                <div>
                  <Text>磁盘使用率</Text>
                  <Progress percent={23} />
                </div>
                <div>
                  <Text>网络带宽</Text>
                  <Progress percent={89} status="active" />
                </div>
              </Space>
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="快速操作">
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" block icon={<FileTextOutlined />}>
                  创建新文档
                </Button>
                <Button block icon={<UserOutlined />}>
                  添加用户
                </Button>
                <Button block icon={<ClockCircleOutlined />}>
                  查看反馈
                </Button>
                <Button block>
                  系统设置
                </Button>
              </Space>
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  );
};

export default Dashboard;
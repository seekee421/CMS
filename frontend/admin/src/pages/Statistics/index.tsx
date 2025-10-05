import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  DatePicker,
  Select,
  Button,
  Table,
  Typography,
  Space,
  Tabs,
  Progress,
  Tag,
  Tooltip,
  Empty,
  Spin,
  Radio,
  Switch,
  Divider
} from 'antd';
import {
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  AreaChartOutlined,
  DownloadOutlined,
  ReloadOutlined,
  EyeOutlined,
  FileTextOutlined,
  UserOutlined,
  ClockCircleOutlined,
  RiseOutlined,
  FallOutlined,
  CalendarOutlined,
  FilterOutlined
} from '@ant-design/icons';
import { Line, Column, Pie, Area } from '@ant-design/plots';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import './index.less';

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

interface DocumentStatistic {
  id: string;
  title: string;
  category: string;
  author: string;
  viewCount: number;
  downloadCount: number;
  lastViewTime: string;
  createdAt: string;
  trend: 'up' | 'down' | 'stable';
  trendValue: number;
}

interface ChartData {
  date: string;
  value: number;
  type?: string;
  category?: string;
}

interface SystemMetric {
  name: string;
  value: number;
  unit: string;
  status: 'normal' | 'warning' | 'error';
  trend: number;
}

const Statistics: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(30, 'day'),
    dayjs()
  ]);
  const [timeGranularity, setTimeGranularity] = useState<'day' | 'week' | 'month'>('day');
  const [activeTab, setActiveTab] = useState('overview');
  const [chartType, setChartType] = useState<'line' | 'column' | 'area'>('line');
  const [autoRefresh, setAutoRefresh] = useState(false);

  // 模拟数据
  const [overviewStats] = useState({
    totalDocuments: 1234,
    totalViews: 45678,
    totalDownloads: 12345,
    activeUsers: 567,
    documentsGrowth: 12.5,
    viewsGrowth: 8.3,
    downloadsGrowth: -2.1,
    usersGrowth: 15.7
  });

  const [documentStats, setDocumentStats] = useState<DocumentStatistic[]>([]);
  const [viewTrendData, setViewTrendData] = useState<ChartData[]>([]);
  const [categoryData, setCategoryData] = useState<ChartData[]>([]);
  const [systemMetrics, setSystemMetrics] = useState<SystemMetric[]>([]);

  useEffect(() => {
    fetchStatistics();
  }, [dateRange, timeGranularity]);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (autoRefresh) {
      interval = setInterval(() => {
        fetchStatistics();
      }, 30000); // 30秒刷新一次
    }
    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [autoRefresh, dateRange, timeGranularity]);

  const fetchStatistics = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // 生成模拟数据
      generateMockData();
    } catch (error) {
      console.error('获取统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const generateMockData = () => {
    // 生成文档统计数据
    const mockDocuments: DocumentStatistic[] = [
      {
        id: '1',
        title: 'React 开发指南',
        category: '前端开发',
        author: '张三',
        viewCount: 1234,
        downloadCount: 567,
        lastViewTime: '2024-01-15 14:30:00',
        createdAt: '2024-01-01 09:00:00',
        trend: 'up',
        trendValue: 12.5
      },
      {
        id: '2',
        title: 'Java 最佳实践',
        category: '后端开发',
        author: '李四',
        viewCount: 987,
        downloadCount: 432,
        lastViewTime: '2024-01-15 13:45:00',
        createdAt: '2024-01-02 10:30:00',
        trend: 'down',
        trendValue: -5.2
      },
      {
        id: '3',
        title: '数据库设计规范',
        category: '数据库',
        author: '王五',
        viewCount: 756,
        downloadCount: 298,
        lastViewTime: '2024-01-15 12:15:00',
        createdAt: '2024-01-03 11:00:00',
        trend: 'stable',
        trendValue: 0.8
      }
    ];

    // 生成趋势数据
    const mockTrendData: ChartData[] = [];
    const days = dayjs(dateRange[1]).diff(dayjs(dateRange[0]), 'day');
    for (let i = 0; i <= days; i++) {
      const date = dayjs(dateRange[0]).add(i, 'day').format('YYYY-MM-DD');
      mockTrendData.push({
        date,
        value: Math.floor(Math.random() * 1000) + 500,
        type: 'views'
      });
      mockTrendData.push({
        date,
        value: Math.floor(Math.random() * 300) + 100,
        type: 'downloads'
      });
    }

    // 生成分类数据
    const mockCategoryData: ChartData[] = [
      {
        category: '前端开发', value: 35,
        date: ''
      },
      {
        category: '后端开发', value: 28,
        date: ''
      },
      {
        category: '数据库', value: 18,
        date: ''
      },
      {
        category: '运维部署', value: 12,
        date: ''
      },
      {
        category: '产品设计', value: 7,
        date: ''
      }
    ];

    // 生成系统指标
    const mockSystemMetrics: SystemMetric[] = [
      {
        name: 'CPU使用率',
        value: 65,
        unit: '%',
        status: 'normal',
        trend: 2.3
      },
      {
        name: '内存使用率',
        value: 78,
        unit: '%',
        status: 'warning',
        trend: -1.5
      },
      {
        name: '磁盘使用率',
        value: 45,
        unit: '%',
        status: 'normal',
        trend: 0.8
      },
      {
        name: '网络延迟',
        value: 23,
        unit: 'ms',
        status: 'normal',
        trend: -0.5
      }
    ];

    setDocumentStats(mockDocuments);
    setViewTrendData(mockTrendData);
    setCategoryData(mockCategoryData);
    setSystemMetrics(mockSystemMetrics);
  };

  const documentColumns: ColumnsType<DocumentStatistic> = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => (
        <Space>
          <FileTextOutlined />
          <span>{text}</span>
        </Space>
      )
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (text: string) => <Tag color="blue">{text}</Tag>
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
      render: (text: string) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
        </Space>
      )
    },
    {
      title: '浏览次数',
      dataIndex: 'viewCount',
      key: 'viewCount',
      sorter: (a, b) => a.viewCount - b.viewCount,
      render: (value: number, record: DocumentStatistic) => (
        <Space>
          <Statistic
            value={value}
            valueStyle={{ fontSize: '14px' }}
            prefix={<EyeOutlined />}
          />
          {record.trend === 'up' && (
            <Tooltip title={`增长 ${record.trendValue}%`}>
              <RiseOutlined style={{ color: '#52c41a' }} />
            </Tooltip>
          )}
          {record.trend === 'down' && (
            <Tooltip title={`下降 ${Math.abs(record.trendValue)}%`}>
              <FallOutlined style={{ color: '#ff4d4f' }} />
            </Tooltip>
          )}
        </Space>
      )
    },
    {
      title: '下载次数',
      dataIndex: 'downloadCount',
      key: 'downloadCount',
      sorter: (a, b) => a.downloadCount - b.downloadCount,
      render: (value: number) => (
        <Statistic
          value={value}
          valueStyle={{ fontSize: '14px' }}
          prefix={<DownloadOutlined />}
        />
      )
    },
    {
      title: '最后浏览',
      dataIndex: 'lastViewTime',
      key: 'lastViewTime',
      render: (text: string) => (
        <Space>
          <ClockCircleOutlined />
          <span>{text}</span>
        </Space>
      )
    }
  ];

  const renderTrendChart = () => {
    const config = {
      data: viewTrendData,
      xField: 'date',
      yField: 'value',
      seriesField: 'type',
      smooth: true,
      animation: {
        appear: {
          animation: 'path-in',
          duration: 1000,
        },
      },
      color: ['#1890ff', '#52c41a'],
      legend: {
        position: 'top' as const,
      },
      tooltip: {
        formatter: (datum: any) => {
          return {
            name: datum.type === 'views' ? '浏览量' : '下载量',
            value: datum.value,
          };
        },
      },
    };

    switch (chartType) {
      case 'column':
        return <Column {...config} />;
      case 'area':
        return <Area {...config} />;
      default:
        return <Line {...config} />;
    }
  };

  const renderCategoryChart = () => {
    const total = categoryData.reduce((sum, d) => sum + (d.value || 0), 0);
    const config = {
      data: categoryData,
      angleField: 'value',
      colorField: 'category',
      radius: 0.8,
      label: {
        type: 'inner',
        content: (datum: any) => {
          const percent = typeof datum.percent === 'number'
            ? datum.percent
            : typeof datum.percentage === 'number'
              ? datum.percentage / 100
              : total ? (datum.value || 0) / total : 0;
          const pctText = Math.round(percent * 100) + '%';
          const name = datum.category || datum.name || '';
          return `${name} ${pctText}`;
        },
      },
      interactions: [
        {
          type: 'pie-legend-active',
        },
        {
          type: 'element-active',
        },
      ],
    };

    return <Pie {...config} />;
  };

  const exportData = () => {
    // 导出数据逻辑
    const data = {
      overview: overviewStats,
      documents: documentStats,
      trends: viewTrendData,
      categories: categoryData,
      exportTime: new Date().toISOString()
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json'
    });
    
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `statistics-${dayjs().format('YYYY-MM-DD')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="statistics-page">
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-left">
          <Title level={4}>统计分析</Title>
          <Text type="secondary">查看系统使用情况和数据分析</Text>
        </div>
        <div className="header-right">
          <Space>
            <Text type="secondary">自动刷新</Text>
            <Switch
              checked={autoRefresh}
              onChange={setAutoRefresh}
              checkedChildren="开"
              unCheckedChildren="关"
            />
            <Button icon={<ReloadOutlined />} onClick={fetchStatistics}>
              刷新
            </Button>
            <Button icon={<DownloadOutlined />} onClick={exportData}>
              导出数据
            </Button>
          </Space>
        </div>
      </div>

      {/* 筛选区域 */}
      <Card className="filter-section">
        <Row gutter={16} align="middle">
          <Col>
            <Space>
              <CalendarOutlined />
              <Text>时间范围:</Text>
            </Space>
          </Col>
          <Col>
            <RangePicker
              value={dateRange}
              onChange={(dates) => {
                if (dates && dates[0] && dates[1]) {
                  setDateRange([dates[0], dates[1]]);
                }
              }}
              format="YYYY-MM-DD"
            />
          </Col>
          <Col>
            <Space>
              <FilterOutlined />
              <Text>时间粒度:</Text>
            </Space>
          </Col>
          <Col>
            <Select
              value={timeGranularity}
              onChange={setTimeGranularity}
              style={{ width: 100 }}
            >
              <Option value="day">按天</Option>
              <Option value="week">按周</Option>
              <Option value="month">按月</Option>
            </Select>
          </Col>
          <Col>
            <Space>
              <BarChartOutlined />
              <Text>图表类型:</Text>
            </Space>
          </Col>
          <Col>
            <Radio.Group
              value={chartType}
              onChange={(e) => setChartType(e.target.value)}
              buttonStyle="solid"
              size="small"
            >
              <Radio.Button value="line">
                <LineChartOutlined />
              </Radio.Button>
              <Radio.Button value="column">
                <BarChartOutlined />
              </Radio.Button>
              <Radio.Button value="area">
                <AreaChartOutlined />
              </Radio.Button>
            </Radio.Group>
          </Col>
        </Row>
      </Card>

      {/* 主要内容 */}
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab="概览统计" key="overview">
          <Spin spinning={loading}>
            {/* 概览统计卡片 */}
            <Row gutter={[16, 16]} className="overview-stats">
              <Col xs={24} sm={12} lg={6}>
                <Card>
                  <Statistic
                    title="文档总数"
                    value={overviewStats.totalDocuments}
                    prefix={<FileTextOutlined />}
                    suffix={
                      <Space>
                        {overviewStats.documentsGrowth > 0 ? (
                          <RiseOutlined style={{ color: '#52c41a' }} />
                        ) : (
                          <FallOutlined style={{ color: '#ff4d4f' }} />
                        )}
                        <Text
                          type={overviewStats.documentsGrowth > 0 ? 'success' : 'danger'}
                          style={{ fontSize: '12px' }}
                        >
                          {Math.abs(overviewStats.documentsGrowth)}%
                        </Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card>
                  <Statistic
                    title="总浏览量"
                    value={overviewStats.totalViews}
                    prefix={<EyeOutlined />}
                    suffix={
                      <Space>
                        {overviewStats.viewsGrowth > 0 ? (
                          <RiseOutlined style={{ color: '#52c41a' }} />
                        ) : (
                          <FallOutlined style={{ color: '#ff4d4f' }} />
                        )}
                        <Text
                          type={overviewStats.viewsGrowth > 0 ? 'success' : 'danger'}
                          style={{ fontSize: '12px' }}
                        >
                          {Math.abs(overviewStats.viewsGrowth)}%
                        </Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card>
                  <Statistic
                    title="总下载量"
                    value={overviewStats.totalDownloads}
                    prefix={<DownloadOutlined />}
                    suffix={
                      <Space>
                        {overviewStats.downloadsGrowth > 0 ? (
                          <RiseOutlined style={{ color: '#52c41a' }} />
                        ) : (
                          <FallOutlined style={{ color: '#ff4d4f' }} />
                        )}
                        <Text
                          type={overviewStats.downloadsGrowth > 0 ? 'success' : 'danger'}
                          style={{ fontSize: '12px' }}
                        >
                          {Math.abs(overviewStats.downloadsGrowth)}%
                        </Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card>
                  <Statistic
                    title="活跃用户"
                    value={overviewStats.activeUsers}
                    prefix={<UserOutlined />}
                    suffix={
                      <Space>
                        {overviewStats.usersGrowth > 0 ? (
                          <RiseOutlined style={{ color: '#52c41a' }} />
                        ) : (
                          <FallOutlined style={{ color: '#ff4d4f' }} />
                        )}
                        <Text
                          type={overviewStats.usersGrowth > 0 ? 'success' : 'danger'}
                          style={{ fontSize: '12px' }}
                        >
                          {Math.abs(overviewStats.usersGrowth)}%
                        </Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
            </Row>

            {/* 趋势图表 */}
            <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
              <Col xs={24} lg={16}>
                <Card title="访问趋势" extra={
                  <Space>
                    <Text type="secondary">
                      {dayjs(dateRange[0]).format('MM/DD')} - {dayjs(dateRange[1]).format('MM/DD')}
                    </Text>
                  </Space>
                }>
                  <div style={{ height: 300 }}>
                    {renderTrendChart()}
                  </div>
                </Card>
              </Col>
              <Col xs={24} lg={8}>
                <Card title="分类分布">
                  <div style={{ height: 300 }}>
                    {renderCategoryChart()}
                  </div>
                </Card>
              </Col>
            </Row>

            {/* 系统指标 */}
            <Card title="系统指标" style={{ marginTop: 24 }}>
              <Row gutter={[16, 16]}>
                {systemMetrics.map((metric, index) => (
                  <Col xs={24} sm={12} lg={6} key={index}>
                    <div className="metric-item">
                      <div className="metric-header">
                        <Text strong>{metric.name}</Text>
                        <Space>
                          {metric.trend > 0 ? (
                            <RiseOutlined style={{ color: '#52c41a' }} />
                          ) : metric.trend < 0 ? (
                            <FallOutlined style={{ color: '#ff4d4f' }} />
                          ) : null}
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            {metric.trend > 0 ? '+' : ''}{metric.trend}%
                          </Text>
                        </Space>
                      </div>
                      <div className="metric-content">
                        <Progress
                          percent={metric.value}
                          status={metric.status === 'error' ? 'exception' : 
                                 metric.status === 'warning' ? 'active' : 'normal'}
                          format={() => `${metric.value}${metric.unit}`}
                        />
                      </div>
                    </div>
                  </Col>
                ))}
              </Row>
            </Card>
          </Spin>
        </TabPane>

        <TabPane tab="文档统计" key="documents">
          <Spin spinning={loading}>
            <Card>
              <Table
                columns={documentColumns}
                dataSource={documentStats}
                rowKey="id"
                pagination={{
                  total: documentStats.length,
                  pageSize: 10,
                  showSizeChanger: true,
                  showQuickJumper: true,
                  showTotal: (total, range) =>
                    `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
                }}
              />
            </Card>
          </Spin>
        </TabPane>

        <TabPane tab="用户行为" key="users">
          <Spin spinning={loading}>
            <Empty description="用户行为分析功能开发中..." />
          </Spin>
        </TabPane>

        <TabPane tab="性能监控" key="performance">
          <Spin spinning={loading}>
            <Empty description="性能监控功能开发中..." />
          </Spin>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default Statistics;
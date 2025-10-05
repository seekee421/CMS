import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, List, Typography, Tag, Spin } from 'antd';
import { EyeOutlined, DownloadOutlined, HeartOutlined, ShareAltOutlined } from '@ant-design/icons';
import { createApiClient } from '@shared/api/client';

const { Title } = Typography;

interface SystemStats {
  totalViews: number;
  totalDownloads: number;
  totalDocuments: number;
  totalUsers: number;
}

interface PopularDocument {
  id: string;
  title: string;
  viewCount: number;
  downloadCount: number;
  category: string;
}

interface StatisticsDashboardProps {
  timeRange?: 'today' | 'week' | 'month' | 'year';
}

const StatisticsDashboard: React.FC<StatisticsDashboardProps> = ({
  timeRange = 'today'
}) => {
  const [systemStats, setSystemStats] = useState<SystemStats | null>(null);
  const [popularDocs, setPopularDocs] = useState<PopularDocument[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStatistics();
  }, [timeRange]);

  const fetchStatistics = async () => {
    setLoading(true);
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      
      // 获取系统统计
      const statsResponse = await apiClient.get(`/api/statistics/quick/${timeRange}`);
      setSystemStats(statsResponse.data);

      // 获取热门文档
      const popularResponse = await apiClient.get('/api/statistics/rankings/recent-hot?limit=5');
      setPopularDocs(popularResponse.data);

    } catch (error) {
      console.error('Failed to fetch statistics:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTimeRangeTitle = () => {
    switch (timeRange) {
      case 'today': return '今日统计';
      case 'week': return '本周统计';
      case 'month': return '本月统计';
      case 'year': return '本年统计';
      default: return '统计概览';
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="statistics-dashboard">
      <Title level={3} style={{ marginBottom: 24 }}>
        {getTimeRangeTitle()}
      </Title>

      {systemStats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总浏览量"
                value={systemStats.totalViews}
                prefix={<EyeOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总下载量"
                value={systemStats.totalDownloads}
                prefix={<DownloadOutlined />}
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="文档数量"
                value={systemStats.totalDocuments}
                prefix={<HeartOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="用户数量"
                value={systemStats.totalUsers}
                prefix={<ShareAltOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card title="热门文档" style={{ marginBottom: 24 }}>
        <List
          dataSource={popularDocs}
          renderItem={(doc, index) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ 
                      backgroundColor: index < 3 ? '#ff4d4f' : '#d9d9d9',
                      color: 'white',
                      borderRadius: '50%',
                      width: 20,
                      height: 20,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: 12,
                      fontWeight: 'bold'
                    }}>
                      {index + 1}
                    </span>
                    <span>{doc.title}</span>
                    <Tag color="blue">{doc.category}</Tag>
                  </div>
                }
                description={
                  <div style={{ display: 'flex', gap: 16 }}>
                    <span>
                      <EyeOutlined /> {doc.viewCount} 次浏览
                    </span>
                    <span>
                      <DownloadOutlined /> {doc.downloadCount} 次下载
                    </span>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Card>
    </div>
  );
};

export default StatisticsDashboard;
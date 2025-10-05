import React from 'react';
import Layout from '@theme/Layout';
import { Row, Col, Card, Typography, Space, Divider } from 'antd';
import FeedbackButton from '../components/FeedbackButton';
import VersionSelector from '../components/VersionSelector';
import SearchComponent from '../components/SearchComponent';
import StatisticsDashboard from '../components/StatisticsDashboard';
import StatisticsTracker from '../components/StatisticsTracker';

const { Title, Paragraph } = Typography;

export default function Demo(): JSX.Element {
  return (
    <Layout
      title="组件演示"
      description="CMS文档中心组件功能演示">
      <main style={{ padding: '2rem' }}>
        <div className="container">
          <Title level={1}>CMS文档中心组件演示</Title>
          <Paragraph>
            这个页面展示了我们为文档中心开发的各种React组件的功能。
          </Paragraph>

          <StatisticsTracker documentId="demo-page">
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              
              {/* 版本选择器演示 */}
              <Card title="版本选择器" bordered={false}>
                <Paragraph>
                  选择不同的产品版本来查看对应的文档：
                </Paragraph>
                <VersionSelector />
              </Card>

              <Divider />

              {/* 搜索组件演示 */}
              <Card title="搜索功能" bordered={false}>
                <Paragraph>
                  全文搜索文档内容，支持实时建议和结果高亮：
                </Paragraph>
                <SearchComponent />
              </Card>

              <Divider />

              {/* 统计仪表板演示 */}
              <Card title="统计仪表板" bordered={false}>
                <Paragraph>
                  查看系统统计数据和热门文档：
                </Paragraph>
                <StatisticsDashboard timeRange="week" />
              </Card>

              <Divider />

              {/* 反馈按钮演示 */}
              <Card title="文档反馈" bordered={false}>
                <Paragraph>
                  用户可以对文档内容提供反馈和建议：
                </Paragraph>
                <FeedbackButton 
                  documentId="demo-doc-001" 
                  documentTitle="组件演示页面" 
                />
              </Card>

              <Divider />

              {/* 功能说明 */}
              <Card title="功能说明" bordered={false}>
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={12}>
                    <Card size="small" title="🔍 搜索功能">
                      <ul>
                        <li>全文搜索文档内容</li>
                        <li>搜索结果高亮显示</li>
                        <li>实时搜索建议</li>
                        <li>搜索统计记录</li>
                      </ul>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card size="small" title="📊 统计功能">
                      <ul>
                        <li>文档访问统计</li>
                        <li>下载次数统计</li>
                        <li>热门文档排行</li>
                        <li>系统概览数据</li>
                      </ul>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card size="small" title="🔄 版本管理">
                      <ul>
                        <li>产品版本选择</li>
                        <li>版本间切换</li>
                        <li>版本历史记录</li>
                        <li>版本比较功能</li>
                      </ul>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card size="small" title="💬 反馈系统">
                      <ul>
                        <li>问题类型分类</li>
                        <li>详细意见描述</li>
                        <li>联系方式记录</li>
                        <li>反馈状态跟踪</li>
                      </ul>
                    </Card>
                  </Col>
                </Row>
              </Card>

            </Space>
          </StatisticsTracker>
        </div>
      </main>
    </Layout>
  );
}
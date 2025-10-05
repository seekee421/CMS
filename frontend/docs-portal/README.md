# CMS文档门户开发计划

## 项目概述

基于Docusaurus 3.x构建的现代化文档门户，提供优秀的用户体验和强大的文档管理功能。支持多版本、多语言、全文搜索、用户反馈等企业级功能。

## 技术栈

### 核心技术
- **Docusaurus 3.x** - 静态站点生成器
- **React 18+** - 组件开发
- **TypeScript** - 类型安全
- **MDX** - Markdown + React组件
- **Algolia Search** - 搜索功能
- **Prism** - 代码高亮

### 扩展功能
- **React Query** - 数据获取
- **Axios** - HTTP客户端
- **Ant Design** - UI组件库
- **React Hook Form** - 表单管理
- **Recharts** - 图表组件

## 项目结构

```
docs-portal/
├── docs/                     # 文档内容
│   ├── intro.md             # 介绍页面
│   ├── getting-started/     # 快速开始
│   │   ├── installation.md
│   │   ├── configuration.md
│   │   └── first-steps.md
│   ├── user-guide/          # 用户指南
│   │   ├── basic-usage.md
│   │   ├── advanced-features.md
│   │   └── troubleshooting.md
│   ├── api-reference/       # API参考
│   │   ├── authentication.md
│   │   ├── users.md
│   │   ├── documents.md
│   │   └── permissions.md
│   └── tutorials/           # 教程
│       ├── tutorial-basics/
│       └── tutorial-extras/
├── blog/                    # 博客文章
│   ├── 2024-01-01-welcome.md
│   └── authors.yml
├── src/                     # 自定义代码
│   ├── components/          # React组件
│   │   ├── FeedbackButton/  # 反馈按钮
│   │   │   ├── index.tsx
│   │   │   ├── FeedbackModal.tsx
│   │   │   └── styles.module.css
│   │   ├── VersionSelector/ # 版本选择器
│   │   │   ├── index.tsx
│   │   │   └── styles.module.css
│   │   ├── DownloadTracker/ # 下载统计
│   │   │   ├── index.tsx
│   │   │   └── DownloadButton.tsx
│   │   ├── SearchHighlight/ # 搜索高亮
│   │   │   ├── index.tsx
│   │   │   └── SearchResults.tsx
│   │   ├── DocRating/       # 文档评分
│   │   │   ├── index.tsx
│   │   │   ├── RatingStars.tsx
│   │   │   └── styles.module.css
│   │   ├── TableOfContents/ # 目录导航
│   │   │   ├── index.tsx
│   │   │   └── styles.module.css
│   │   └── BackToTop/       # 返回顶部
│   │       ├── index.tsx
│   │       └── styles.module.css
│   ├── pages/               # 自定义页面
│   │   ├── index.tsx        # 首页
│   │   ├── feedback.tsx     # 反馈页面
│   │   ├── search.tsx       # 搜索页面
│   │   └── download.tsx     # 下载页面
│   ├── hooks/               # 自定义Hooks
│   │   ├── useApi.ts        # API调用Hook
│   │   ├── useFeedback.ts   # 反馈Hook
│   │   ├── useAnalytics.ts  # 统计Hook
│   │   └── useSearch.ts     # 搜索Hook
│   ├── services/            # API服务
│   │   ├── api.ts           # API客户端
│   │   ├── feedback.ts      # 反馈服务
│   │   ├── analytics.ts     # 统计服务
│   │   └── search.ts        # 搜索服务
│   ├── utils/               # 工具函数
│   │   ├── constants.ts     # 常量定义
│   │   ├── helpers.ts       # 辅助函数
│   │   └── formatters.ts    # 格式化函数
│   ├── types/               # 类型定义
│   │   ├── api.ts           # API类型
│   │   ├── feedback.ts      # 反馈类型
│   │   └── analytics.ts     # 统计类型
│   ├── css/                 # 样式文件
│   │   ├── custom.css       # 自定义样式
│   │   └── variables.css    # CSS变量
│   └── theme/               # 主题定制
│       ├── DocItem/         # 文档项主题
│       ├── SearchBar/       # 搜索栏主题
│       └── Footer/          # 页脚主题
├── static/                  # 静态资源
│   ├── img/                 # 图片资源
│   │   ├── logo.svg
│   │   ├── favicon.ico
│   │   └── screenshots/
│   ├── files/               # 下载文件
│   │   ├── guides/
│   │   └── examples/
│   └── videos/              # 视频资源
├── versioned_docs/          # 版本化文档
│   ├── version-2.0/
│   └── version-1.0/
├── versioned_sidebars/      # 版本化侧边栏
│   ├── version-2.0-sidebars.json
│   └── version-1.0-sidebars.json
├── i18n/                    # 国际化
│   ├── zh-CN/
│   │   ├── docusaurus-plugin-content-docs/
│   │   └── docusaurus-theme-classic/
│   └── en/
├── docusaurus.config.js     # 主配置文件
├── sidebars.js              # 侧边栏配置
├── babel.config.js          # Babel配置
├── package.json
├── tsconfig.json
└── README.md
```

## Docusaurus配置

### 主配置文件
```javascript
// docusaurus.config.js
const config = {
  title: 'CMS文档中心',
  tagline: '专业的内容管理系统文档',
  favicon: 'img/favicon.ico',
  url: 'https://docs.cms.example.com',
  baseUrl: '/',
  organizationName: 'cms-team',
  projectName: 'cms-docs',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // 国际化配置
  i18n: {
    defaultLocale: 'zh-CN',
    locales: ['zh-CN', 'en'],
    localeConfigs: {
      'zh-CN': {
        label: '简体中文',
        direction: 'ltr',
        htmlLang: 'zh-CN',
      },
      en: {
        label: 'English',
        direction: 'ltr',
        htmlLang: 'en-US',
      },
    },
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/cms-team/cms-docs/tree/main/',
          showLastUpdateAuthor: true,
          showLastUpdateTime: true,
          // 版本配置
          versions: {
            current: {
              label: '3.0 (开发中)',
              path: 'next',
            },
            '2.0': {
              label: '2.0 (稳定版)',
              path: '2.0',
            },
            '1.0': {
              label: '1.0 (维护版)',
              path: '1.0',
            },
          },
        },
        blog: {
          showReadingTime: true,
          editUrl: 'https://github.com/cms-team/cms-docs/tree/main/',
          blogSidebarTitle: '最新文章',
          blogSidebarCount: 'ALL',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        gtag: {
          trackingID: 'G-XXXXXXXXXX',
          anonymizeIP: true,
        },
      },
    ],
  ],

  themeConfig: {
    image: 'img/cms-social-card.jpg',
    navbar: {
      title: 'CMS文档',
      logo: {
        alt: 'CMS Logo',
        src: 'img/logo.svg',
        srcDark: 'img/logo-dark.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: '文档',
        },
        {
          to: '/blog',
          label: '博客',
          position: 'left'
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          dropdownActiveClassDisabled: true,
        },
        {
          type: 'localeDropdown',
          position: 'right',
        },
        {
          href: 'https://github.com/cms-team/cms',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },

    footer: {
      style: 'dark',
      links: [
        {
          title: '文档',
          items: [
            {
              label: '快速开始',
              to: '/docs/getting-started/installation',
            },
            {
              label: '用户指南',
              to: '/docs/user-guide/basic-usage',
            },
            {
              label: 'API参考',
              to: '/docs/api-reference/authentication',
            },
          ],
        },
        {
          title: '社区',
          items: [
            {
              label: '讨论区',
              href: 'https://github.com/cms-team/cms/discussions',
            },
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/cms',
            },
            {
              label: 'Discord',
              href: 'https://discordapp.com/invite/cms',
            },
          ],
        },
        {
          title: '更多',
          items: [
            {
              label: '博客',
              to: '/blog',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/cms-team/cms',
            },
            {
              label: '反馈',
              to: '/feedback',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} CMS Team. Built with Docusaurus.`,
    },

    prism: {
      theme: require('prism-react-renderer/themes/github'),
      darkTheme: require('prism-react-renderer/themes/dracula'),
      additionalLanguages: ['java', 'php', 'sql', 'bash', 'json', 'yaml'],
    },

    // 搜索配置
    algolia: {
      appId: 'YOUR_APP_ID',
      apiKey: 'YOUR_SEARCH_API_KEY',
      indexName: 'cms_docs',
      contextualSearch: true,
      searchPagePath: 'search',
      searchParameters: {},
      facetFilters: [],
    },

    // 公告栏
    announcementBar: {
      id: 'support_us',
      content: '⭐️ 如果您喜欢CMS，请在 <a target="_blank" rel="noopener noreferrer" href="https://github.com/cms-team/cms">GitHub</a> 上给我们一个星标！',
      backgroundColor: '#fafbfc',
      textColor: '#091E42',
      isCloseable: false,
    },

    // 颜色模式配置
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
  },

  plugins: [
    // 自定义插件
    './src/plugins/backend-integration',
    './src/plugins/analytics-plugin',
    './src/plugins/feedback-plugin',
    
    // PWA插件
    [
      '@docusaurus/plugin-pwa',
      {
        debug: true,
        offlineModeActivationStrategies: [
          'appInstalled',
          'standalone',
          'queryString',
        ],
        pwaHead: [
          {
            tagName: 'link',
            rel: 'icon',
            href: '/img/logo.png',
          },
          {
            tagName: 'link',
            rel: 'manifest',
            href: '/manifest.json',
          },
          {
            tagName: 'meta',
            name: 'theme-color',
            content: 'rgb(37, 194, 160)',
          },
        ],
      },
    ],
  ],

  scripts: [
    // 自定义脚本
    {
      src: '/js/analytics.js',
      async: true,
    },
  ],

  stylesheets: [
    // 自定义样式表
    'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap',
  ],
};

module.exports = config;
```

### 侧边栏配置
```javascript
// sidebars.js
const sidebars = {
  tutorialSidebar: [
    'intro',
    {
      type: 'category',
      label: '快速开始',
      items: [
        'getting-started/installation',
        'getting-started/configuration',
        'getting-started/first-steps',
      ],
    },
    {
      type: 'category',
      label: '用户指南',
      items: [
        'user-guide/basic-usage',
        'user-guide/advanced-features',
        'user-guide/troubleshooting',
      ],
    },
    {
      type: 'category',
      label: 'API参考',
      items: [
        'api-reference/authentication',
        'api-reference/users',
        'api-reference/documents',
        'api-reference/permissions',
      ],
    },
    {
      type: 'category',
      label: '教程',
      items: [
        {
          type: 'autogenerated',
          dirName: 'tutorials',
        },
      ],
    },
  ],
};

module.exports = sidebars;
```

## 核心组件设计

### 1. 反馈按钮组件
```typescript
// src/components/FeedbackButton/index.tsx
import React, { useState } from 'react';
import { Modal, Form, Select, Input, Button, message, Rate } from 'antd';
import { useFeedback } from '@site/src/hooks/useFeedback';
import styles from './styles.module.css';

interface FeedbackButtonProps {
  documentId?: string;
  documentTitle?: string;
  className?: string;
}

const FeedbackButton: React.FC<FeedbackButtonProps> = ({
  documentId,
  documentTitle,
  className,
}) => {
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm();
  const { submitFeedback, loading } = useFeedback();

  const handleSubmit = async (values: any) => {
    try {
      await submitFeedback({
        documentId,
        documentTitle,
        feedbackType: values.type,
        description: values.description,
        contactInfo: values.contact,
        rating: values.rating,
      });
      message.success('反馈提交成功，感谢您的建议！');
      setVisible(false);
      form.resetFields();
    } catch (error) {
      message.error('提交失败，请重试');
    }
  };

  return (
    <>
      <Button
        type="primary"
        className={`${styles.feedbackButton} ${className}`}
        onClick={() => setVisible(true)}
      >
        📝 文档反馈
      </Button>

      <Modal
        title={`反馈：${documentTitle || '当前页面'}`}
        open={visible}
        onCancel={() => setVisible(false)}
        footer={null}
        width={600}
        className={styles.feedbackModal}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="rating"
            label="文档评分"
            rules={[{ required: true, message: '请为文档评分' }]}
          >
            <Rate allowHalf />
          </Form.Item>

          <Form.Item
            name="type"
            label="问题类型"
            rules={[{ required: true, message: '请选择问题类型' }]}
          >
            <Select placeholder="请选择问题类型">
              <Select.Option value="CONTENT_INCORRECT">内容不正确</Select.Option>
              <Select.Option value="CONTENT_MISSING">没有找到需要的内容</Select.Option>
              <Select.Option value="DESCRIPTION_UNCLEAR">描述不清晰</Select.Option>
              <Select.Option value="SUGGESTION">改进建议</Select.Option>
              <Select.Option value="OTHER">其他问题</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="description"
            label="详细描述"
            rules={[
              { required: true, message: '请输入详细描述' },
              { max: 2000, message: '描述不能超过2000字符' },
            ]}
          >
            <Input.TextArea
              rows={4}
              maxLength={2000}
              showCount
              placeholder="请详细描述您遇到的问题或建议..."
            />
          </Form.Item>

          <Form.Item
            name="contact"
            label="联系方式（可选）"
            rules={[
              { type: 'email', message: '请输入有效的邮箱地址' },
            ]}
          >
            <Input placeholder="您的邮箱地址，方便我们回复您" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              提交反馈
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default FeedbackButton;
```

### 2. 版本选择器组件
```typescript
// src/components/VersionSelector/index.tsx
import React from 'react';
import { Select } from 'antd';
import { useHistory, useLocation } from '@docusaurus/router';
import { useVersions, useActiveVersion } from '@docusaurus/plugin-content-docs/client';
import styles from './styles.module.css';

const VersionSelector: React.FC = () => {
  const history = useHistory();
  const location = useLocation();
  const versions = useVersions();
  const activeVersion = useActiveVersion();

  const handleVersionChange = (version: string) => {
    const versionPath = version === 'current' ? '/docs/next' : `/docs/${version}`;
    const currentPath = location.pathname.replace(/^\/docs\/[^\/]*/, '');
    history.push(`${versionPath}${currentPath}`);
  };

  return (
    <div className={styles.versionSelector}>
      <Select
        value={activeVersion.name}
        onChange={handleVersionChange}
        className={styles.select}
        dropdownClassName={styles.dropdown}
      >
        {versions.map((version) => (
          <Select.Option key={version.name} value={version.name}>
            {version.label}
          </Select.Option>
        ))}
      </Select>
    </div>
  );
};

export default VersionSelector;
```

### 3. 下载统计组件
```typescript
// src/components/DownloadTracker/index.tsx
import React from 'react';
import { Button, Tooltip } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { useAnalytics } from '@site/src/hooks/useAnalytics';
import styles from './styles.module.css';

interface DownloadTrackerProps {
  fileUrl: string;
  fileName: string;
  fileSize?: string;
  documentId?: string;
  children?: React.ReactNode;
}

const DownloadTracker: React.FC<DownloadTrackerProps> = ({
  fileUrl,
  fileName,
  fileSize,
  documentId,
  children,
}) => {
  const { trackDownload } = useAnalytics();

  const handleDownload = async () => {
    try {
      // 记录下载统计
      if (documentId) {
        await trackDownload(documentId, fileName);
      }

      // 触发下载
      const link = document.createElement('a');
      link.href = fileUrl;
      link.download = fileName;
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('下载失败:', error);
    }
  };

  if (children) {
    return (
      <div onClick={handleDownload} className={styles.downloadWrapper}>
        {children}
      </div>
    );
  }

  return (
    <Tooltip title={fileSize ? `文件大小: ${fileSize}` : '点击下载'}>
      <Button
        type="primary"
        icon={<DownloadOutlined />}
        onClick={handleDownload}
        className={styles.downloadButton}
      >
        下载 {fileName}
      </Button>
    </Tooltip>
  );
};

export default DownloadTracker;
```

### 4. 搜索高亮组件
```typescript
// src/components/SearchHighlight/index.tsx
import React from 'react';
import { useHistory } from '@docusaurus/router';
import { useSearch } from '@site/src/hooks/useSearch';
import styles from './styles.module.css';

interface SearchHighlightProps {
  content: string;
  searchTerm?: string;
  maxLength?: number;
}

const SearchHighlight: React.FC<SearchHighlightProps> = ({
  content,
  searchTerm,
  maxLength = 200,
}) => {
  const { highlightText } = useSearch();

  if (!searchTerm) {
    return <span>{content.slice(0, maxLength)}...</span>;
  }

  const highlightedContent = highlightText(content, searchTerm, maxLength);

  return (
    <span
      className={styles.searchHighlight}
      dangerouslySetInnerHTML={{ __html: highlightedContent }}
    />
  );
};

export default SearchHighlight;
```

## 自定义页面

### 1. 首页
```typescript
// src/pages/index.tsx
import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import styles from './index.module.css';

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <h1 className="hero__title">{siteConfig.title}</h1>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg"
            to="/docs/getting-started/installation"
          >
            快速开始 - 5分钟 ⏱️
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): JSX.Element {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="专业的内容管理系统文档"
    >
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
```

### 2. 反馈页面
```typescript
// src/pages/feedback.tsx
import React, { useState } from 'react';
import Layout from '@theme/Layout';
import { Card, Form, Select, Input, Button, message, Rate, Timeline } from 'antd';
import { useFeedback } from '@site/src/hooks/useFeedback';
import styles from './feedback.module.css';

export default function FeedbackPage(): JSX.Element {
  const [form] = Form.useForm();
  const { submitFeedback, getFeedbackHistory, loading } = useFeedback();
  const [feedbackHistory, setFeedbackHistory] = useState([]);

  const handleSubmit = async (values: any) => {
    try {
      await submitFeedback(values);
      message.success('反馈提交成功！');
      form.resetFields();
      // 刷新反馈历史
      const history = await getFeedbackHistory();
      setFeedbackHistory(history);
    } catch (error) {
      message.error('提交失败，请重试');
    }
  };

  return (
    <Layout title="文档反馈" description="为我们的文档提供宝贵建议">
      <div className="container margin-vert--lg">
        <div className="row">
          <div className="col col--8">
            <Card title="📝 文档反馈" className={styles.feedbackCard}>
              <p>您的反馈对我们非常重要，帮助我们不断改进文档质量。</p>
              
              <Form form={form} onFinish={handleSubmit} layout="vertical">
                <Form.Item
                  name="rating"
                  label="整体评分"
                  rules={[{ required: true, message: '请为文档评分' }]}
                >
                  <Rate allowHalf />
                </Form.Item>

                <Form.Item
                  name="type"
                  label="反馈类型"
                  rules={[{ required: true, message: '请选择反馈类型' }]}
                >
                  <Select placeholder="请选择反馈类型">
                    <Select.Option value="CONTENT_INCORRECT">内容错误</Select.Option>
                    <Select.Option value="CONTENT_MISSING">内容缺失</Select.Option>
                    <Select.Option value="DESCRIPTION_UNCLEAR">描述不清</Select.Option>
                    <Select.Option value="SUGGESTION">改进建议</Select.Option>
                    <Select.Option value="PRAISE">表扬鼓励</Select.Option>
                    <Select.Option value="OTHER">其他</Select.Option>
                  </Select>
                </Form.Item>

                <Form.Item
                  name="description"
                  label="详细描述"
                  rules={[
                    { required: true, message: '请输入详细描述' },
                    { max: 2000, message: '描述不能超过2000字符' },
                  ]}
                >
                  <Input.TextArea
                    rows={6}
                    maxLength={2000}
                    showCount
                    placeholder="请详细描述您的问题、建议或意见..."
                  />
                </Form.Item>

                <Form.Item
                  name="contact"
                  label="联系方式（可选）"
                  rules={[{ type: 'email', message: '请输入有效的邮箱地址' }]}
                >
                  <Input placeholder="您的邮箱地址，方便我们回复您" />
                </Form.Item>

                <Form.Item>
                  <Button type="primary" htmlType="submit" loading={loading} size="large">
                    提交反馈
                  </Button>
                </Form.Item>
              </Form>
            </Card>
          </div>

          <div className="col col--4">
            <Card title="💡 反馈指南" className={styles.guideCard}>
              <Timeline>
                <Timeline.Item>
                  <strong>具体描述</strong>
                  <br />
                  请详细描述遇到的问题或建议
                </Timeline.Item>
                <Timeline.Item>
                  <strong>提供上下文</strong>
                  <br />
                  说明在哪个页面或章节遇到问题
                </Timeline.Item>
                <Timeline.Item>
                  <strong>建设性建议</strong>
                  <br />
                  提供改进的具体建议
                </Timeline.Item>
                <Timeline.Item>
                  <strong>留下联系方式</strong>
                  <br />
                  方便我们与您进一步沟通
                </Timeline.Item>
              </Timeline>
            </Card>
          </div>
        </div>
      </div>
    </Layout>
  );
}
```

## 自定义插件

### 1. 后端集成插件
```javascript
// src/plugins/backend-integration/index.js
const path = require('path');

module.exports = function(context, options) {
  return {
    name: 'backend-integration',
    
    async loadContent() {
      // 从后端API获取数据
      try {
        const response = await fetch(`${process.env.API_BASE_URL}/api/documents/public`);
        const documents = await response.json();
        return { documents };
      } catch (error) {
        console.warn('Failed to load backend data:', error);
        return { documents: [] };
      }
    },

    async contentLoaded({ content, actions }) {
      const { createData, addRoute } = actions;
      
      // 创建数据文件
      await createData('documents.json', JSON.stringify(content.documents));
      
      // 添加自定义路由
      addRoute({
        path: '/api-explorer',
        component: '@site/src/components/ApiExplorer',
        exact: true,
      });
    },

    getClientModules() {
      return [path.resolve(__dirname, './client')];
    },
  };
};
```

### 2. 统计插件
```javascript
// src/plugins/analytics-plugin/index.js
module.exports = function(context, options) {
  return {
    name: 'analytics-plugin',
    
    injectHtmlTags() {
      return {
        headTags: [
          {
            tagName: 'script',
            innerHTML: `
              window.analytics = {
                track: function(event, properties) {
                  // 发送统计数据到后端
                  fetch('/api/analytics/track', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ event, properties })
                  });
                }
              };
            `,
          },
        ],
      };
    },

    getClientModules() {
      return [require.resolve('./analytics-client')];
    },
  };
};
```

## 开发计划

### 第一阶段：基础搭建（第1-2周）
- [ ] Docusaurus项目初始化
- [ ] 基础配置和主题定制
- [ ] 文档结构规划
- [ ] 基础组件开发

### 第二阶段：核心功能（第3-4周）
- [ ] 反馈系统集成
- [ ] 搜索功能配置
- [ ] 版本管理设置
- [ ] 多语言支持

### 第三阶段：高级功能（第5-6周）
- [ ] 后端API集成
- [ ] 统计分析功能
- [ ] 自定义插件开发
- [ ] 性能优化

### 第四阶段：内容和测试（第7-8周）
- [ ] 文档内容迁移
- [ ] 用户体验测试
- [ ] 性能测试
- [ ] 部署配置

## 部署配置

### 环境变量
```bash
# .env
API_BASE_URL=https://api.cms.example.com
ALGOLIA_APP_ID=your_app_id
ALGOLIA_API_KEY=your_api_key
ALGOLIA_INDEX_NAME=cms_docs
GOOGLE_ANALYTICS_ID=G-XXXXXXXXXX
```

### 构建脚本
```json
{
  "scripts": {
    "start": "docusaurus start",
    "build": "docusaurus build",
    "swizzle": "docusaurus swizzle",
    "deploy": "docusaurus deploy",
    "clear": "docusaurus clear",
    "serve": "docusaurus serve",
    "write-translations": "docusaurus write-translations",
    "write-heading-ids": "docusaurus write-heading-ids"
  }
}
```

### Docker配置
```dockerfile
# Dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=0 /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```
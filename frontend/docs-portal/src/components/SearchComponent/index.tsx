import React, { useState, useEffect } from 'react';
import { Input, List, Card, Typography, Tag, Spin } from 'antd';
import { SearchOutlined, FileTextOutlined } from '@ant-design/icons';
import { createApiClient } from '@shared/api/client';

const { Search } = Input;
const { Title, Text, Paragraph } = Typography;

interface SearchResult {
  id: string;
  title: string;
  content: string;
  category: string;
  url: string;
  score: number;
  highlights: string[];
}

interface SearchComponentProps {
  placeholder?: string;
  onResultClick?: (result: SearchResult) => void;
}

const SearchComponent: React.FC<SearchComponentProps> = ({
  placeholder = '搜索文档...',
  onResultClick
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);

  // 搜索防抖
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm.trim()) {
        performSearch(searchTerm);
      } else {
        setResults([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const performSearch = async (query: string) => {
    setLoading(true);
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      
      // 记录搜索统计
      await apiClient.post('/api/statistics/actions/search', {
        query,
        timestamp: new Date().toISOString()
      });

      // 执行搜索
      const response = await apiClient.get(`/api/search?q=${encodeURIComponent(query)}&page=0&size=10`);
      
      // 转换搜索结果
      const searchResults = response.data.content.map((item: any) => ({
        id: item.id,
        title: item.title,
        content: item.content || item.summary,
        category: item.categoryName || '未分类',
        url: `/docs/${item.id}`,
        score: item.score || 1,
        highlights: item.highlights || []
      }));

      setResults(searchResults);
    } catch (error) {
      console.error('Search failed:', error);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleResultClick = (result: SearchResult) => {
    if (onResultClick) {
      onResultClick(result);
    } else {
      // 默认跳转行为
      window.location.href = result.url;
    }
  };

  const highlightText = (text: string, highlights: string[]) => {
    if (!highlights.length) return text;
    
    let highlightedText = text;
    highlights.forEach(highlight => {
      const regex = new RegExp(`(${highlight})`, 'gi');
      highlightedText = highlightedText.replace(regex, '<mark>$1</mark>');
    });
    
    return highlightedText;
  };

  return (
    <div className="search-component">
      <Search
        placeholder={placeholder}
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        onSearch={performSearch}
        enterButton={<SearchOutlined />}
        size="large"
        style={{ marginBottom: 16 }}
      />

      {loading && (
        <div style={{ textAlign: 'center', padding: 20 }}>
          <Spin size="large" />
        </div>
      )}

      {results.length > 0 && (
        <List
          dataSource={results}
          renderItem={(result) => (
            <List.Item
              onClick={() => handleResultClick(result)}
              style={{ cursor: 'pointer' }}
            >
              <Card
                hoverable
                style={{ width: '100%' }}
                bodyStyle={{ padding: 16 }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start' }}>
                  <FileTextOutlined style={{ marginRight: 12, marginTop: 4, color: '#1890ff' }} />
                  <div style={{ flex: 1 }}>
                    <Title level={5} style={{ margin: 0, marginBottom: 8 }}>
                      <span 
                        dangerouslySetInnerHTML={{ 
                          __html: highlightText(result.title, result.highlights) 
                        }} 
                      />
                    </Title>
                    <Paragraph 
                      ellipsis={{ rows: 2 }}
                      style={{ margin: 0, marginBottom: 8, color: '#666' }}
                    >
                      <span 
                        dangerouslySetInnerHTML={{ 
                          __html: highlightText(result.content, result.highlights) 
                        }} 
                      />
                    </Paragraph>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Tag color="blue">{result.category}</Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        相关度: {Math.round(result.score * 100)}%
                      </Text>
                    </div>
                  </div>
                </div>
              </Card>
            </List.Item>
          )}
        />
      )}

      {searchTerm && !loading && results.length === 0 && (
        <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
          <FileTextOutlined style={{ fontSize: 48, marginBottom: 16 }} />
          <div>未找到相关文档</div>
          <div style={{ fontSize: 14, marginTop: 8 }}>
            尝试使用不同的关键词或检查拼写
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchComponent;
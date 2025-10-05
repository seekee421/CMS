import React, { useState, useEffect, useRef } from 'react';
import { Modal, Input, List, Empty, Spin, Tag, Typography } from 'antd';
import { SearchOutlined, FileTextOutlined, UserOutlined, FolderOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useDebounce } from '../../hooks/useDebounce';
import './index.less';

const { Text } = Typography;

interface SearchResult {
  id: string;
  type: 'document' | 'user' | 'category';
  title: string;
  description?: string;
  path: string;
  tags?: string[];
  highlight?: string;
}

interface SearchModalProps {
  visible: boolean;
  onClose: () => void;
}

const SearchModal: React.FC<SearchModalProps> = ({ visible, onClose }) => {
  const [searchValue, setSearchValue] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<any>(null);
  const navigate = useNavigate();
  
  // 防抖搜索
  const debouncedSearchValue = useDebounce(searchValue, 300);
  
  // 模拟搜索数据
  const mockSearchResults: SearchResult[] = [
    {
      id: '1',
      type: 'document',
      title: 'API 接口文档',
      description: '系统API接口的详细说明文档',
      path: '/documents/1',
      tags: ['API', '接口'],
      highlight: 'API接口的详细说明'
    },
    {
      id: '2',
      type: 'document',
      title: '用户管理指南',
      description: '如何管理系统用户的操作指南',
      path: '/documents/2',
      tags: ['用户', '管理'],
      highlight: '管理系统用户的操作'
    },
    {
      id: '3',
      type: 'user',
      title: '张三',
      description: '系统管理员',
      path: '/users/3',
      tags: ['管理员'],
    },
    {
      id: '4',
      type: 'category',
      title: '技术文档',
      description: '技术相关的文档分类',
      path: '/documents/categories/4',
      tags: ['分类'],
    },
  ];
  
  // 搜索函数
  const performSearch = async (query: string) => {
    if (!query.trim()) {
      setResults([]);
      return;
    }
    
    setLoading(true);
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const filteredResults = mockSearchResults.filter(item =>
        item.title.toLowerCase().includes(query.toLowerCase()) ||
        item.description?.toLowerCase().includes(query.toLowerCase()) ||
        item.tags?.some(tag => tag.toLowerCase().includes(query.toLowerCase()))
      );
      
      setResults(filteredResults);
      setSelectedIndex(0);
    } catch (error) {
      console.error('搜索失败:', error);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };
  
  // 监听搜索值变化
  useEffect(() => {
    performSearch(debouncedSearchValue);
  }, [debouncedSearchValue]);
  
  // 模态框打开时聚焦输入框
  useEffect(() => {
    if (visible && inputRef.current) {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    }
  }, [visible]);
  
  // 键盘导航
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (results.length === 0) return;
    
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex(prev => (prev + 1) % results.length);
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex(prev => (prev - 1 + results.length) % results.length);
        break;
      case 'Enter':
        e.preventDefault();
        if (results[selectedIndex]) {
          handleResultClick(results[selectedIndex]);
        }
        break;
      case 'Escape':
        onClose();
        break;
    }
  };
  
  // 点击搜索结果
  const handleResultClick = (result: SearchResult) => {
    navigate(result.path);
    onClose();
    setSearchValue('');
    setResults([]);
  };
  
  // 获取图标
  const getIcon = (type: string) => {
    switch (type) {
      case 'document':
        return <FileTextOutlined />;
      case 'user':
        return <UserOutlined />;
      case 'category':
        return <FolderOutlined />;
      default:
        return <FileTextOutlined />;
    }
  };
  
  // 获取类型标签颜色
  const getTypeColor = (type: string) => {
    switch (type) {
      case 'document':
        return 'blue';
      case 'user':
        return 'green';
      case 'category':
        return 'orange';
      default:
        return 'default';
    }
  };
  
  // 获取类型名称
  const getTypeName = (type: string) => {
    switch (type) {
      case 'document':
        return '文档';
      case 'user':
        return '用户';
      case 'category':
        return '分类';
      default:
        return '未知';
    }
  };
  
  return (
    <Modal
      title={null}
      open={visible}
      onCancel={onClose}
      footer={null}
      width={600}
      className="search-modal"
      destroyOnClose
    >
      <div className="search-modal-content">
        <div className="search-input-wrapper">
          <Input
            ref={inputRef}
            size="large"
            placeholder="搜索文档、用户、分类..."
            prefix={<SearchOutlined />}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onKeyDown={handleKeyDown}
            allowClear
          />
        </div>
        
        <div className="search-results">
          {loading ? (
            <div className="search-loading">
              <Spin size="small" />
              <Text type="secondary">搜索中...</Text>
            </div>
          ) : results.length > 0 ? (
            <List
              dataSource={results}
              renderItem={(item, index) => (
                <List.Item
                  className={`search-result-item ${index === selectedIndex ? 'selected' : ''}`}
                  onClick={() => handleResultClick(item)}
                >
                  <div className="result-content">
                    <div className="result-header">
                      <span className="result-icon">{getIcon(item.type)}</span>
                      <Text strong className="result-title">{item.title}</Text>
                      <Tag color={getTypeColor(item.type)}>
                        {getTypeName(item.type)}
                      </Tag>
                    </div>
                    {item.description && (
                      <Text type="secondary" className="result-description">
                        {item.description}
                      </Text>
                    )}
                    {item.highlight && (
                      <Text type="secondary" className="result-highlight">
                        ...{item.highlight}...
                      </Text>
                    )}
                    {item.tags && item.tags.length > 0 && (
                      <div className="result-tags">
                        {item.tags.map(tag => (
                          <Tag key={tag}>{tag}</Tag>
                        ))}
                      </div>
                    )}
                  </div>
                </List.Item>
              )}
            />
          ) : searchValue.trim() ? (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="未找到相关结果"
            />
          ) : (
            <div className="search-tips">
              <Text type="secondary">
                输入关键词搜索文档、用户或分类
              </Text>
              <div className="search-shortcuts">
                <Text type="secondary" className="shortcut-tip">
                  快捷键：↑↓ 选择，Enter 确认，Esc 关闭
                </Text>
              </div>
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
};

export default SearchModal;
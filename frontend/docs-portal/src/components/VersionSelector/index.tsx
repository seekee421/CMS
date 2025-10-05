import React, { useState, useEffect } from 'react';
import { Select } from 'antd';
import { TagOutlined } from '@ant-design/icons';
import { createApiClient } from '@shared/api/client';

interface Version {
  id: string;
  name: string;
  description: string;
  isActive: boolean;
  releaseDate: string;
}

interface VersionSelectorProps {
  currentVersion?: string;
  onVersionChange?: (version: string) => void;
  className?: string;
}

const VersionSelector: React.FC<VersionSelectorProps> = ({
  currentVersion = 'v1.0.0',
  onVersionChange,
  className = ''
}) => {
  const [versions, setVersions] = useState<Version[]>([]);
  const [loading, setLoading] = useState(false);

  // 获取版本数据
  useEffect(() => {
    const fetchVersions = async () => {
      setLoading(true);
      try {
        const apiClient = createApiClient('http://localhost:8080', null);
        const response = await apiClient.get('/api/versions/products');
        
        // 转换API响应为组件需要的格式
        const versionData = response.data.map((item: any) => ({
          id: item.version,
          name: item.version,
          description: item.description || '产品版本',
          isActive: item.isActive,
          releaseDate: item.releaseDate
        }));
        
        setVersions(versionData);
      } catch (error) {
        console.error('Failed to fetch versions:', error);
        // 使用默认版本作为fallback
        setVersions([{
          id: 'v1.0.0',
          name: 'v1.0.0',
          description: '默认版本',
          isActive: true,
          releaseDate: new Date().toISOString()
        }]);
      } finally {
        setLoading(false);
      }
    };

    fetchVersions();
  }, []);

  const handleVersionChange = (value: string) => {
    onVersionChange?.(value);
    // 这里可以添加路由跳转逻辑
    console.log('Version changed to:', value);
  };

  return (
    <div className={`version-selector ${className}`}>
      <Select
        value={currentVersion}
        onChange={handleVersionChange}
        loading={loading}
        placeholder="选择版本"
        suffixIcon={<TagOutlined />}
        style={{ width: 200 }}
        size="large"
        optionLabelProp="label"
      >
        {versions.map(version => (
          <Select.Option 
            key={version.id} 
            value={version.id}
            label={version.name}
          >
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontWeight: 'bold' }}>
                {version.name}
                {version.isActive && (
                  <span style={{ 
                    marginLeft: 8, 
                    color: '#52c41a', 
                    fontSize: '12px' 
                  }}>
                    (当前)
                  </span>
                )}
              </span>
              <span style={{ 
                fontSize: '12px', 
                color: '#666',
                marginTop: '2px'
              }}>
                {version.description} • {version.releaseDate}
              </span>
            </div>
          </Select.Option>
        ))}
      </Select>
    </div>
  );
};

export default VersionSelector;
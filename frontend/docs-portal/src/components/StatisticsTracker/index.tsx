import React, { useEffect } from 'react';
import { createApiClient } from '@shared/api/client';

interface StatisticsTrackerProps {
  documentId: string;
  children: React.ReactNode;
}

const StatisticsTracker: React.FC<StatisticsTrackerProps> = ({
  documentId,
  children
}) => {
  useEffect(() => {
    // 记录页面访问
    const trackView = async () => {
      try {
        const apiClient = createApiClient('http://localhost:8080', null);
        await apiClient.post(`/api/statistics/actions/view/${documentId}`);
      } catch (error) {
        console.warn('Failed to track view:', error);
      }
    };

    trackView();
  }, [documentId]);

  const handleDownload = async (downloadUrl: string) => {
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      await apiClient.post(`/api/statistics/actions/download/${documentId}`);
      
      // 打开下载链接
      window.open(downloadUrl, '_blank');
    } catch (error) {
      console.warn('Failed to track download:', error);
      // 即使统计失败也要继续下载
      window.open(downloadUrl, '_blank');
    }
  };

  const handleShare = async () => {
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      await apiClient.post(`/api/statistics/actions/share/${documentId}`);
    } catch (error) {
      console.warn('Failed to track share:', error);
    }
  };

  const handleFavorite = async () => {
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      await apiClient.post(`/api/statistics/actions/favorite/${documentId}`);
    } catch (error) {
      console.warn('Failed to track favorite:', error);
    }
  };

  // 通过context提供统计方法
  const contextValue = {
    trackDownload: handleDownload,
    trackShare: handleShare,
    trackFavorite: handleFavorite
  };

  return (
    <div data-document-id={documentId} data-statistics-context={JSON.stringify(contextValue)}>
      {children}
    </div>
  );
};

export default StatisticsTracker;
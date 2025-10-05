// 分析客户端代码

// 页面访问统计
export function trackPageView(path) {
  if (typeof window !== 'undefined' && window.cmsApi) {
    // 发送页面访问统计
    console.log('Page view tracked:', path);
    // 这里可以调用后端API记录访问
  }
}

// 文档查看统计
export function trackDocumentView(documentId) {
  if (typeof window !== 'undefined' && window.cmsApi) {
    window.cmsApi.trackView(documentId).catch(error => {
      console.warn('Failed to track document view:', error);
    });
  }
}

// 下载统计
export function trackDownload(documentId) {
  if (typeof window !== 'undefined' && window.cmsApi) {
    window.cmsApi.trackDownload(documentId).catch(error => {
      console.warn('Failed to track download:', error);
    });
  }
}

// 搜索统计
export function trackSearch(query, results) {
  if (typeof window !== 'undefined') {
    console.log('Search tracked:', { query, resultCount: results.length });
    // 这里可以调用后端API记录搜索
  }
}

// 反馈统计
export function trackFeedback(type) {
  if (typeof window !== 'undefined') {
    console.log('Feedback tracked:', type);
    // 这里可以调用后端API记录反馈
  }
}

// 路由变化监听
if (typeof window !== 'undefined') {
  // 监听路由变化
  window.addEventListener('popstate', () => {
    trackPageView(window.location.pathname);
  });
  
  // 初始页面加载
  document.addEventListener('DOMContentLoaded', () => {
    trackPageView(window.location.pathname);
  });
}
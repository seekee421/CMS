// 客户端代码，在浏览器中运行

// 全局API客户端
window.cmsApi = {
  baseUrl: process.env.NODE_ENV === 'production' 
    ? 'https://api.cms.example.com' 
    : 'http://localhost:8080',
    
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    };
    
    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  },
  
  // 文档相关API
  async getDocuments(params = {}) {
    return this.request('/api/documents', {
      method: 'GET',
      ...params
    });
  },
  
  // 反馈相关API
  async submitFeedback(feedback) {
    return this.request('/api/feedback', {
      method: 'POST',
      body: JSON.stringify(feedback)
    });
  },
  
  // 统计相关API
  async trackView(documentId) {
    return this.request('/api/statistics/view', {
      method: 'POST',
      body: JSON.stringify({ documentId })
    });
  },
  
  async trackDownload(documentId) {
    return this.request('/api/statistics/download', {
      method: 'POST',
      body: JSON.stringify({ documentId })
    });
  }
};

// 页面加载时初始化
if (typeof window !== 'undefined') {
  console.log('CMS API client initialized');
}
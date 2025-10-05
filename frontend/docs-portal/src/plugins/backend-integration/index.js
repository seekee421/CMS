module.exports = function(context, options) {
  return {
    name: 'backend-integration',
    
    // 构建时数据获取
    async loadContent() {
      try {
        // 从后端API获取文档数据
        // const response = await fetch(`${process.env.API_BASE_URL}/api/documents`);
        // const documents = await response.json();
        
        // 暂时使用模拟数据
        const documents = [
          {
            id: 1,
            title: '快速开始',
            content: '欢迎使用CMS文档中心',
            category: '入门指南'
          }
        ];
        
        return { documents };
      } catch (error) {
        console.warn('Failed to load content from backend:', error);
        return { documents: [] };
      }
    },
    
    // 客户端代码注入
    getClientModules() {
      return [require.resolve('./client')];
    },
    
    // 路由配置
    async contentLoaded({content, actions}) {
      const {createData, addRoute} = actions;
      
      try {
        // 创建数据文件
        await createData('documents.json', JSON.stringify(content.documents));
        
        // 添加自定义路由
        addRoute({
          path: '/feedback',
          component: '@site/src/components/FeedbackPage',
          exact: true,
        });
      } catch (error) {
        console.warn('Failed to create data or routes:', error);
      }
    },
  };
};
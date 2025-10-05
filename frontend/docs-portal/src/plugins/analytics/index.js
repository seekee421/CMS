module.exports = function(context, options) {
  return {
    name: 'analytics',
    
    // 客户端代码注入
    getClientModules() {
      return [require.resolve('./client')];
    },
    
    // 在HTML头部注入脚本
    injectHtmlTags() {
      if (process.env.NODE_ENV !== 'production') {
        return {};
      }
      
      return {
        headTags: [
          {
            tagName: 'script',
            innerHTML: `
              // 这里可以添加Google Analytics或其他分析工具的代码
              console.log('Analytics initialized');
            `,
          },
        ],
      };
    },
  };
};
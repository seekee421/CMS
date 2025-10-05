import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */
const sidebars: SidebarsConfig = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [
    'intro',
    {
      type: 'category',
      label: '快速开始',
      items: [
        'getting-started/quick-setup',
      ],
    },
    {
      type: 'category',
      label: '用户指南',
      items: [
        'user-guide/overview',
        'user-guide/content-management',
        'user-guide/user-management',
        'user-guide/permissions',
        'user-guide/categories',
        'user-guide/search',
      ],
    },
    {
      type: 'category',
      label: '部署指南',
      items: [
        'deployment/installation',
      ],
    },
    {
      type: 'category',
      label: 'API文档',
      items: [
        'api/overview',
      ],
    },
  ],

  // But you can create a sidebar manually
  /*
  tutorialSidebar: [
    'intro',
    'hello',
    {
      type: 'category',
      label: 'Tutorial',
      items: ['tutorial-basics/create-a-document'],
    },
  ],
   */
};

export default sidebars;
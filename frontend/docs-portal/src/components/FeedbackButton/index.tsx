import React, { useState } from 'react';
import { Modal, Form, Select, Input, Button, message } from 'antd';
import { MessageOutlined } from '@ant-design/icons';
import { createApiClient } from '@shared/api/client';
import './styles.css';

interface FeedbackButtonProps {
  documentId?: string;
  documentTitle?: string;
}

interface FeedbackForm {
  type: string;
  description: string;
  contact: string;
}

const FeedbackButton: React.FC<FeedbackButtonProps> = ({ 
  documentId = 'current-doc', 
  documentTitle = '当前文档' 
}) => {
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm<FeedbackForm>();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: FeedbackForm) => {
    setLoading(true);
    try {
      const apiClient = createApiClient('http://localhost:8080', null);
      
      // 调用反馈API
      await apiClient.post('/api/feedback', {
        documentId,
        feedbackType: values.type,
        description: values.description,
        contactInfo: values.contact,
        documentTitle
      });
      
      message.success('反馈提交成功！感谢您的建议。');
      setVisible(false);
      form.resetFields();
    } catch (error) {
      message.error('提交失败，请重试');
      console.error('Feedback submission error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button 
        type="primary" 
        icon={<MessageOutlined />}
        onClick={() => setVisible(true)}
        className="feedback-button"
        size="large"
      >
        文档反馈
      </Button>
      
      <Modal
        title={`反馈：${documentTitle}`}
        open={visible}
        onCancel={() => setVisible(false)}
        footer={null}
        width={600}
        destroyOnClose
      >
        <Form 
          form={form} 
          onFinish={handleSubmit} 
          layout="vertical"
          requiredMark={false}
        >
          <Form.Item
            name="type"
            label="问题类型"
            rules={[{ required: true, message: '请选择问题类型' }]}
          >
            <Select placeholder="请选择问题类型" size="large">
              <Select.Option value="CONTENT_INCORRECT">内容不正确</Select.Option>
              <Select.Option value="CONTENT_MISSING">没有找到需要的内容</Select.Option>
              <Select.Option value="DESCRIPTION_UNCLEAR">描述不清晰</Select.Option>
              <Select.Option value="OTHER_SUGGESTION">其他建议</Select.Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name="description"
            label="意见描述"
            rules={[
              { required: true, message: '请输入意见描述' },
              { max: 2000, message: '描述不能超过2000字符' }
            ]}
          >
            <Input.TextArea 
              rows={6} 
              maxLength={2000}
              showCount
              placeholder="请详细描述您的问题或建议，这将帮助我们改进文档质量..."
              size="large"
            />
          </Form.Item>
          
          <Form.Item
            name="contact"
            label="联系方式"
            rules={[
              { required: true, message: '请输入联系方式' },
              { 
                pattern: /^[\w\.-]+@[\w\.-]+\.\w+$|^1[3-9]\d{9}$/,
                message: '请输入有效的邮箱地址或手机号码'
              }
            ]}
          >
            <Input 
              placeholder="请输入您的邮箱或手机号码，以便我们回复您"
              size="large"
            />
          </Form.Item>
          
          <Form.Item style={{ marginBottom: 0, marginTop: '2rem' }}>
            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
              <Button 
                onClick={() => setVisible(false)}
                size="large"
              >
                取消
              </Button>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                size="large"
              >
                提交反馈
              </Button>
            </div>
          </Form.Item>
        </Form>
        
        <div style={{ 
          marginTop: '1rem', 
          padding: '12px', 
          backgroundColor: '#f6f8fa', 
          borderRadius: '6px',
          fontSize: '14px',
          color: '#666'
        }}>
          💡 您的反馈对我们非常重要！我们会认真对待每一条建议，并在1-2个工作日内回复您。
        </div>
      </Modal>
    </>
  );
};

export default FeedbackButton;
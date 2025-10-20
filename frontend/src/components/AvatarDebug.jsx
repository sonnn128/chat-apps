import React, { useState } from 'react';
import { Button, Card, Space, Typography, message } from 'antd';
import { avatarService } from '../services/avatarService';

const { Title, Text } = Typography;

const AvatarDebug = () => {
  const [testUserId] = useState('123e4567-e89b-12d3-a456-426614174000');
  const [results, setResults] = useState({});

  const testGetAvatar = async () => {
    try {
      const result = await avatarService.getAvatar(testUserId);
      setResults(prev => ({ ...prev, getAvatar: result }));
      message.success('Get avatar test completed');
    } catch (error) {
      message.error('Get avatar test failed');
      console.error('Get avatar error:', error);
    }
  };

  const testFileValidation = () => {
    const mockFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const validation = avatarService.validateFile(mockFile);
    setResults(prev => ({ ...prev, validation }));
    message.success('File validation test completed');
  };

  const testFileValidationError = () => {
    const mockFile = new File(['test'], 'test.txt', { type: 'text/plain' });
    const validation = avatarService.validateFile(mockFile);
    setResults(prev => ({ ...prev, validationError: validation }));
    message.success('File validation error test completed');
  };

  const clearResults = () => {
    setResults({});
    message.info('Results cleared');
  };

  return (
    <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
      <Title level={2}>Avatar Service Debug</Title>
      
      <Card title="Test Functions" style={{ marginBottom: 24 }}>
        <Space wrap>
          <Button onClick={testGetAvatar} type="primary">
            Test Get Avatar
          </Button>
          <Button onClick={testFileValidation}>
            Test File Validation (Valid)
          </Button>
          <Button onClick={testFileValidationError}>
            Test File Validation (Invalid)
          </Button>
          <Button onClick={clearResults} danger>
            Clear Results
          </Button>
        </Space>
      </Card>

      <Card title="Test Results">
        <Space direction="vertical" style={{ width: '100%' }}>
          {Object.entries(results).map(([key, value]) => (
            <div key={key}>
              <Text strong>{key}:</Text>
              <pre style={{ 
                background: '#f5f5f5', 
                padding: 8, 
                borderRadius: 4,
                marginTop: 4,
                fontSize: 12,
                overflow: 'auto'
              }}>
                {JSON.stringify(value, null, 2)}
              </pre>
            </div>
          ))}
        </Space>
      </Card>
    </div>
  );
};

export default AvatarDebug;

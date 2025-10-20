import React, { useState } from 'react';
import { Card, Button, Space, Typography, Divider } from 'antd';
import { useAvatar } from '../hooks/useAvatar';
import AvatarUpload from './AvatarUpload';

const { Title, Text } = Typography;

const AvatarTest = () => {
  const [testUserId] = useState('123e4567-e89b-12d3-a456-426614174000');
  const { avatarUrl, isLoading, error, loadAvatar, clearError } = useAvatar(testUserId);

  const handleAvatarChange = (newAvatarUrl) => {
    console.log('Avatar changed:', newAvatarUrl);
  };

  return (
    <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
      <Title level={2}>Avatar Upload Test</Title>
      
      <Card title="Test Avatar Upload" style={{ marginBottom: 24 }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Text strong>Current Avatar URL:</Text>
            <div style={{ 
              marginTop: 8, 
              padding: 8, 
              backgroundColor: '#f5f5f5', 
              borderRadius: 4,
              fontFamily: 'monospace',
              fontSize: 12,
              wordBreak: 'break-all'
            }}>
              {avatarUrl || 'No avatar set'}
            </div>
          </div>

          <div>
            <Text strong>Status:</Text>
            <div style={{ marginTop: 8 }}>
              {isLoading && <Text type="warning">Loading...</Text>}
              {error && <Text type="danger">Error: {error}</Text>}
              {!isLoading && !error && <Text type="success">Ready</Text>}
            </div>
          </div>

          <Space>
            <Button onClick={loadAvatar} loading={isLoading}>
              Load Avatar
            </Button>
            <Button onClick={clearError} disabled={!error}>
              Clear Error
            </Button>
          </Space>
        </Space>
      </Card>

      <Card title="Avatar Upload Component">
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Text strong>Small Size:</Text>
            <div style={{ marginTop: 8 }}>
              <AvatarUpload
                userId={testUserId}
                currentAvatarUrl={avatarUrl}
                onAvatarChange={handleAvatarChange}
                size="small"
                showDeleteButton={true}
              />
            </div>
          </div>

          <Divider />

          <div>
            <Text strong>Medium Size:</Text>
            <div style={{ marginTop: 8 }}>
              <AvatarUpload
                userId={testUserId}
                currentAvatarUrl={avatarUrl}
                onAvatarChange={handleAvatarChange}
                size="medium"
                showDeleteButton={true}
              />
            </div>
          </div>

          <Divider />

          <div>
            <Text strong>Large Size:</Text>
            <div style={{ marginTop: 8 }}>
              <AvatarUpload
                userId={testUserId}
                currentAvatarUrl={avatarUrl}
                onAvatarChange={handleAvatarChange}
                size="large"
                showDeleteButton={true}
              />
            </div>
          </div>

          <Divider />

          <div>
            <Text strong>Extra Large Size:</Text>
            <div style={{ marginTop: 8 }}>
              <AvatarUpload
                userId={testUserId}
                currentAvatarUrl={avatarUrl}
                onAvatarChange={handleAvatarChange}
                size="xlarge"
                showDeleteButton={true}
              />
            </div>
          </div>

          <Divider />

          <div>
            <Text strong>Disabled State:</Text>
            <div style={{ marginTop: 8 }}>
              <AvatarUpload
                userId={testUserId}
                currentAvatarUrl={avatarUrl}
                onAvatarChange={handleAvatarChange}
                size="medium"
                showDeleteButton={true}
                disabled={true}
              />
            </div>
          </div>
        </Space>
      </Card>
    </div>
  );
};

export default AvatarTest;

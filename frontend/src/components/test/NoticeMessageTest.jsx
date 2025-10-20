import React from 'react';
import NoticeMessage from '../chat/messageType/NoticeMessage';

/**
 * Test component để kiểm tra notice message hiển thị đúng
 * Có thể xóa sau khi test xong
 */
const NoticeMessageTest = () => {
  const testNoticeMessage = {
    key: {
      channelId: "test-channel-id",
      messageId: "test-message-id"
    },
    userId: "test-user-id",
    content: "Kênh Test Channel đã được tạo thành công",
    type: "NOTICE",
    timestamp: new Date().toISOString(),
    senderName: "Test User",
    senderAvatar: null
  };

  return (
    <div className="p-4 border rounded-lg">
      <h3 className="text-lg font-semibold mb-4">Notice Message Test</h3>
      
      <div className="space-y-4">
        <div>
          <h4 className="font-medium mb-2">Test Notice Message:</h4>
          <NoticeMessage content={testNoticeMessage.content} />
        </div>
        
        <div>
          <h4 className="font-medium mb-2">Raw Data:</h4>
          <pre className="bg-gray-100 p-2 rounded text-xs overflow-auto">
            {JSON.stringify(testNoticeMessage, null, 2)}
          </pre>
        </div>
      </div>
    </div>
  );
};

export default NoticeMessageTest;

// Test file for avatar service
import { avatarService } from './services/avatarService';

// Test function
export const testAvatarService = async () => {
  console.log('Testing Avatar Service...');
  
  const testUserId = '123e4567-e89b-12d3-a456-426614174000';
  
  try {
    // Test get avatar
    console.log('1. Testing get avatar...');
    const getResult = await avatarService.getAvatar(testUserId);
    console.log('Get avatar result:', getResult);
    
    // Test file validation
    console.log('2. Testing file validation...');
    const mockFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const validation = avatarService.validateFile(mockFile);
    console.log('File validation result:', validation);
    
    console.log('Avatar service test completed!');
  } catch (error) {
    console.error('Avatar service test failed:', error);
  }
};

// Export for use in console
window.testAvatarService = testAvatarService;

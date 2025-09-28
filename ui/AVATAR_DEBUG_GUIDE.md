# Avatar Debug Guide

## Lỗi đã sửa

### 1. Import Error
**Lỗi**: `The requested module '/src/utils/httpRequest.js' does not provide an export named 'httpRequest'`

**Nguyên nhân**: File `httpRequest.js` export `httpRequest` làm default export, không phải named export.

**Giải pháp**: 
- Thay đổi từ `import { httpRequest }` thành `import httpRequest`
- Hoặc sử dụng axios trực tiếp với config riêng

### 2. API URL Configuration
**Vấn đề**: `httpRequest.js` có baseURL cố định `http://localhost:8888/api/v1`

**Giải pháp**: Tạo axios instance riêng cho avatar API với URL đúng

## Cách debug

### 1. Sử dụng Debug Component
Truy cập: `http://localhost:5173/debug/avatar`

### 2. Test trong Console
```javascript
// Import test function
import { testAvatarService } from './src/test-avatar-service.js';

// Chạy test
testAvatarService();
```

### 3. Test API trực tiếp
Mở file `ui/test-avatar.html` trong browser

## Các file đã tạo/sửa

### 1. Services
- `ui/src/services/avatarService.js` - Service chính cho avatar
- `ui/src/config/api.js` - Cấu hình API URLs

### 2. Components
- `ui/src/components/AvatarUpload.jsx` - Component upload avatar
- `ui/src/components/AvatarDebug.jsx` - Component debug
- `ui/src/components/AvatarTest.jsx` - Component test

### 3. Hooks
- `ui/src/hooks/useAvatar.js` - Custom hook quản lý avatar

### 4. Styles
- `ui/src/components/AvatarUpload.css` - CSS cho component

### 5. Test Files
- `ui/test-avatar.html` - Test API trực tiếp
- `ui/src/test-avatar-service.js` - Test service

## Cách test từng bước

### 1. Test Service
```bash
# Mở browser console
# Import và test
import { avatarService } from './src/services/avatarService.js';
await avatarService.getAvatar('test-user-id');
```

### 2. Test Component
```jsx
// Trong component
import AvatarUpload from '@/components/AvatarUpload';

<AvatarUpload
  userId="test-user-id"
  onAvatarChange={(url) => console.log(url)}
/>
```

### 3. Test API
```bash
# Test upload
curl -X POST http://localhost:9005/api/v1/users/test-user-id/avatar \
  -F "file=@test.jpg"

# Test get
curl http://localhost:9005/api/v1/users/test-user-id/avatar

# Test delete
curl -X DELETE http://localhost:9005/api/v1/users/test-user-id/avatar
```

## Troubleshooting

### 1. CORS Error
- Kiểm tra backend có enable CORS không
- Thêm CORS config trong Spring Boot

### 2. 404 Error
- Kiểm tra API endpoint có đúng không
- Kiểm tra service có chạy không

### 3. 500 Error
- Kiểm tra logs backend
- Kiểm tra Cloudinary config

### 4. File Upload Error
- Kiểm tra file size (max 10MB)
- Kiểm tra file type (image only)
- Kiểm tra network connection

## Environment Variables

Tạo file `.env` trong thư mục `ui/`:

```env
VITE_API_BASE_URL=http://localhost:9005
VITE_MEDIA_SERVICE_URL=http://localhost:9006
```

## Next Steps

1. Test avatar upload trong Settings
2. Kiểm tra avatar hiển thị đúng
3. Test delete avatar
4. Test error handling
5. Remove debug routes khi hoàn thành

# Avatar Upload Feature - React Frontend

## Tổng quan

Tính năng upload avatar cho React frontend sử dụng API backend để xử lý file upload và hiển thị avatar.

## Components

### 1. AvatarUpload
Component chính để upload và hiển thị avatar.

```jsx
import AvatarUpload from '@/components/AvatarUpload';

<AvatarUpload
  userId="user-id"
  currentAvatarUrl="https://example.com/avatar.jpg"
  onAvatarChange={(newUrl) => console.log(newUrl)}
  size="large"
  showDeleteButton={true}
  disabled={false}
/>
```

#### Props:
- `userId` (string, required): ID của user
- `currentAvatarUrl` (string, optional): URL avatar hiện tại
- `onAvatarChange` (function, optional): Callback khi avatar thay đổi
- `size` (string, optional): Kích thước - 'small', 'medium', 'large', 'xlarge'
- `showDeleteButton` (boolean, optional): Hiển thị nút xóa avatar
- `disabled` (boolean, optional): Vô hiệu hóa component

### 2. useAvatar Hook
Custom hook để quản lý avatar state và operations.

```jsx
import { useAvatar } from '@/hooks/useAvatar';

const { 
  avatarUrl, 
  isLoading, 
  error, 
  uploadAvatar, 
  deleteAvatar, 
  loadAvatar, 
  clearError 
} = useAvatar(userId);
```

#### Return values:
- `avatarUrl`: URL avatar hiện tại
- `isLoading`: Trạng thái loading
- `error`: Lỗi nếu có
- `uploadAvatar(file)`: Upload file mới
- `deleteAvatar()`: Xóa avatar
- `loadAvatar()`: Load avatar từ server
- `clearError()`: Xóa lỗi

## Services

### avatarService
Service để gọi API avatar.

```jsx
import { avatarService } from '@/services/avatarService';

// Upload avatar
const result = await avatarService.uploadAvatar(userId, file);

// Get avatar
const result = await avatarService.getAvatar(userId);

// Delete avatar
const result = await avatarService.deleteAvatar(userId);

// Validate file
const validation = avatarService.validateFile(file);
```

## Cách sử dụng trong Settings

### 1. Import components
```jsx
import { useAvatar } from '@/hooks/useAvatar';
import AvatarUpload from '@/components/AvatarUpload';
```

### 2. Sử dụng hook
```jsx
const { 
  avatarUrl, 
  isLoading: avatarLoading, 
  uploadAvatar, 
  deleteAvatar, 
  loadAvatar 
} = useAvatar(user?.data?.id);
```

### 3. Load avatar khi component mount
```jsx
useEffect(() => {
  if (user) {
    loadAvatar();
  }
}, [user, loadAvatar]);
```

### 4. Render component
```jsx
<AvatarUpload
  userId={user?.data?.id}
  currentAvatarUrl={avatarUrl}
  onAvatarChange={handleAvatarChange}
  size="large"
  showDeleteButton={true}
  disabled={!isEditing}
/>
```

## File Validation

Avatar service tự động validate file:
- **File size**: Tối đa 10MB
- **File type**: JPEG, PNG, GIF, WebP
- **File empty**: Không được rỗng

## Error Handling

- **File validation errors**: Hiển thị toast error
- **Upload errors**: Hiển thị toast error
- **Network errors**: Hiển thị toast error
- **Service unavailable**: Fallback message

## Loading States

- **Upload loading**: Spinner trong component
- **Delete loading**: Disable delete button
- **Load loading**: Spinner trong component

## Styling

Component sử dụng CSS classes:
- `.avatar-upload`: Container chính
- `.avatar-upload__container`: Container avatar
- `.avatar-upload__content`: Nội dung avatar
- `.avatar-upload__overlay`: Overlay khi hover
- `.avatar-upload__loading`: Loading state
- `.avatar-upload__error`: Error message

## Testing

Sử dụng `AvatarTest` component để test:

```jsx
import AvatarTest from '@/components/AvatarTest';

// Trong App.jsx hoặc test page
<AvatarTest />
```

## API Endpoints

- `POST /api/v1/users/{userId}/avatar` - Upload avatar
- `GET /api/v1/users/{userId}/avatar` - Get avatar URL
- `DELETE /api/v1/users/{userId}/avatar` - Delete avatar

## Environment Variables

```env
VITE_API_BASE_URL=http://localhost:9005
```

## Dependencies

- React
- Ant Design
- Custom hooks
- Avatar service
- Toast notifications

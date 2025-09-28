# Media Service

Stateless service xử lý upload media files với Cloudinary. Service này không lưu trữ metadata trong database, chỉ upload file và trả về URL.

## Tính năng

- Upload files (images, videos, audio, documents) lên Cloudinary
- Trả về URL và publicId cho các microservice khác
- Generate URL từ publicId khi cần
- Xóa files từ Cloudinary
- Hỗ trợ các loại file: IMAGE, VIDEO, AUDIO, DOCUMENT

## Cấu hình

### 1. Cloudinary Setup

1. Tạo tài khoản tại [Cloudinary](https://cloudinary.com)
2. Lấy thông tin từ Dashboard:
   - Cloud Name
   - API Key
   - API Secret

### 2. Environment Variables

Tạo file `.env` trong thư mục gốc với nội dung:

```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

## API Endpoints

### Upload File
```
POST /api/v1/media/upload
Content-Type: multipart/form-data

Body: file (multipart file)

Response:
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "publicId": "abc123def456",
    "secureUrl": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/abc123def456.jpg",
    "originalFileName": "image.jpg",
    "fileType": ".jpg",
    "fileSize": 1024000,
    "mimeType": "image/jpeg",
    "mediaType": "IMAGE"
  }
}
```

### Get File URL
```
GET /api/v1/media/url/{publicId}

Response:
{
  "success": true,
  "data": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/abc123def456.jpg"
}
```

### Delete File
```
DELETE /api/v1/media/{publicId}

Response:
{
  "success": true,
  "message": "File deleted successfully"
}
```

## Sử dụng trong các Microservice khác

Các microservice khác có thể gọi media-service để upload file và lưu URL vào object của họ:

```java
// Upload file
UploadResponse response = mediaServiceClient.uploadFile(file);
String fileUrl = response.getSecureUrl();
String publicId = response.getPublicId();

// Lưu vào object của microservice khác
user.setAvatarUrl(fileUrl);
user.setAvatarPublicId(publicId);
```

## Chạy Service

### Local Development
```bash
./run-media-service.sh
```

### Docker
```bash
docker-compose up media-service
```

## Port

Service chạy trên port **9006**.

## Lưu ý

- Service này là stateless, không lưu trữ metadata
- Các microservice khác cần lưu URL và publicId vào database của họ
- URL được generate từ publicId mỗi khi cần
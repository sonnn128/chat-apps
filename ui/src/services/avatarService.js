import axios from 'axios';
import { errorToast } from '../utils/toast';
import { API_CONFIG } from '../config/api';

// Use API Gateway instead of direct service URL
const API_BASE_URL = 'http://localhost:8888';

// Create axios instance for avatar API
const avatarApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// Add request interceptor for auth token if needed
avatarApi.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
avatarApi.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Avatar API Error:', error);
    errorToast(error.response?.data?.message || 'Something went wrong');
    return Promise.reject(error);
  }
);

class AvatarService {
  // Upload avatar
  async uploadAvatar(userId, file) {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await avatarApi.post(
        `/api/v1/users/${userId}/avatar`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      console.error('Error uploading avatar:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to upload avatar',
        statusCode: error.response?.status || 500,
      };
    }
  }

  // Get avatar URL
  async getAvatar(userId) {
    try {
      const response = await avatarApi.get(
        `/api/v1/users/${userId}/avatar`
      );

      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      console.error('Error getting avatar:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to get avatar',
        statusCode: error.response?.status || 500,
      };
    }
  }

  // Delete avatar
  async deleteAvatar(userId) {
    try {
      const response = await avatarApi.delete(
        `/api/v1/users/${userId}/avatar`
      );

      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      console.error('Error deleting avatar:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to delete avatar',
        statusCode: error.response?.status || 500,
      };
    }
  }

  // Validate file
  validateFile(file) {
    const errors = [];

    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      errors.push('File size must be less than 10MB');
    }

    // Check file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      errors.push('File type must be JPEG, PNG, GIF, or WebP');
    }

    // Check if file is empty
    if (file.size === 0) {
      errors.push('File cannot be empty');
    }

    return {
      isValid: errors.length === 0,
      errors,
    };
  }

  // Get file preview URL
  getFilePreview(file) {
    return URL.createObjectURL(file);
  }

  // Revoke preview URL
  revokePreview(url) {
    URL.revokeObjectURL(url);
  }
}

export const avatarService = new AvatarService();
export default avatarService;

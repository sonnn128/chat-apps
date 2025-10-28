// API Configuration
export const API_CONFIG = {
  USER_SERVICE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9006',
  MEDIA_SERVICE_URL: import.meta.env.VITE_MEDIA_SERVICE_URL || 'http://localhost:9004',
  TIMEOUT: 10000,
};

export default API_CONFIG;

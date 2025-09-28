import { useState, useCallback } from 'react';
import { avatarService } from '../services/avatarService';
import { errorToast, successToast } from '../utils/toast';

export const useAvatar = (userId) => {
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Upload avatar
  const uploadAvatar = useCallback(async (file) => {
    if (!userId) {
      errorToast('User ID is required');
      return { success: false };
    }

    // Validate file
    const validation = avatarService.validateFile(file);
    if (!validation.isValid) {
      const errorMessage = validation.errors.join(', ');
      errorToast(errorMessage);
      setError(errorMessage);
      return { success: false, error: errorMessage };
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await avatarService.uploadAvatar(userId, file);
      
      if (result.success) {
        const newAvatarUrl = result.data.data?.avatarUrl;
        setAvatarUrl(newAvatarUrl);
        successToast('Avatar uploaded successfully');
        return { success: true, avatarUrl: newAvatarUrl };
      } else {
        const errorMessage = result.error || 'Failed to upload avatar';
        setError(errorMessage);
        errorToast(errorMessage);
        return { success: false, error: errorMessage };
      }
    } catch (error) {
      const errorMessage = 'An unexpected error occurred';
      setError(errorMessage);
      errorToast(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  // Delete avatar
  const deleteAvatar = useCallback(async () => {
    if (!userId) {
      errorToast('User ID is required');
      return { success: false };
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await avatarService.deleteAvatar(userId);
      
      if (result.success) {
        setAvatarUrl(null);
        successToast('Avatar deleted successfully');
        return { success: true };
      } else {
        const errorMessage = result.error || 'Failed to delete avatar';
        setError(errorMessage);
        errorToast(errorMessage);
        return { success: false, error: errorMessage };
      }
    } catch (error) {
      const errorMessage = 'An unexpected error occurred';
      setError(errorMessage);
      errorToast(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  // Load avatar
  const loadAvatar = useCallback(async () => {
    if (!userId) return;

    setIsLoading(true);
    setError(null);

    try {
      const result = await avatarService.getAvatar(userId);
      
      if (result.success) {
        const avatarUrl = result.data;
        setAvatarUrl(avatarUrl);
        return { success: true, avatarUrl };
      } else {
        // Avatar not found is not an error
        if (result.statusCode === 404) {
          setAvatarUrl(null);
          return { success: true, avatarUrl: null };
        }
        
        const errorMessage = result.error || 'Failed to load avatar';
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }
    } catch (error) {
      const errorMessage = 'An unexpected error occurred';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  // Clear error
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    avatarUrl,
    isLoading,
    error,
    uploadAvatar,
    deleteAvatar,
    loadAvatar,
    clearError,
  };
};

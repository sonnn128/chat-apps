import { useState, useEffect } from 'react';
import userCacheService from '@/services/userCacheService';

/**
 * Hook to get user information with caching
 * @param {string} userId - User ID
 * @returns {Object} - { userInfo, loading, error }
 */
export const useUserInfo = (userId) => {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setUserInfo(null);
      setLoading(false);
      return;
    }

    const fetchUserInfo = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const info = await userCacheService.getUserInfo(userId);
        setUserInfo(info);
      } catch (err) {
        console.error('Error fetching user info:', err);
        setError(err);
        setUserInfo({
          firstname: 'Unknown',
          lastname: 'User',
          avatarUrl: null,
          email: 'unknown@example.com'
        });
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [userId]);

  return { userInfo, loading, error };
};

/**
 * Hook to get display name for a user
 * @param {string} userId - User ID
 * @returns {Object} - { displayName, loading, error }
 */
export const useDisplayName = (userId) => {
  const [displayName, setDisplayName] = useState('Loading...');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setDisplayName('Unknown User');
      setLoading(false);
      return;
    }

    const fetchDisplayName = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const name = await userCacheService.getDisplayName(userId);
        setDisplayName(name);
      } catch (err) {
        console.error('Error fetching display name:', err);
        setError(err);
        setDisplayName('Unknown User');
      } finally {
        setLoading(false);
      }
    };

    fetchDisplayName();
  }, [userId]);

  return { displayName, loading, error };
};

/**
 * Hook to get avatar URL for a user
 * @param {string} userId - User ID
 * @returns {Object} - { avatarUrl, loading, error }
 */
export const useAvatarUrl = (userId) => {
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setAvatarUrl(null);
      setLoading(false);
      return;
    }

    const fetchAvatarUrl = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const url = await userCacheService.getAvatarUrl(userId);
        setAvatarUrl(url);
      } catch (err) {
        console.error('Error fetching avatar URL:', err);
        setError(err);
        setAvatarUrl(null);
      } finally {
        setLoading(false);
      }
    };

    fetchAvatarUrl();
  }, [userId]);

  return { avatarUrl, loading, error };
};

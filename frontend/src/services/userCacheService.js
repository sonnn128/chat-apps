import userService from './userService';

/**
 * User Cache Service
 * Caches user information to avoid repeated API calls
 * Automatically handles avatar and name updates
 */
class UserCacheService {
  constructor() {
    this.cache = new Map(); // userId -> { firstname, lastname, avatarUrl, email }
    this.pendingRequests = new Map(); // userId -> Promise to avoid duplicate requests
  }

  /**
   * Get user info from cache or fetch from API
   * @param {string} userId - User ID
   * @returns {Promise<{firstname, lastname, avatarUrl, email}>}
   */
  async getUserInfo(userId) {
    // Return cached data if available
    if (this.cache.has(userId)) {
      return this.cache.get(userId);
    }

    // Avoid duplicate requests
    if (this.pendingRequests.has(userId)) {
      return this.pendingRequests.get(userId);
    }

    // Fetch from API
    const requestPromise = this.fetchUserInfo(userId);
    this.pendingRequests.set(userId, requestPromise);

    try {
      const userInfo = await requestPromise;
      this.cache.set(userId, userInfo);
      return userInfo;
    } finally {
      this.pendingRequests.delete(userId);
    }
  }

  /**
   * Fetch user info from API
   * @param {string} userId - User ID
   * @returns {Promise<{firstname, lastname, avatarUrl, email}>}
   */
  async fetchUserInfo(userId) {
    try {
      console.log(`🔍 UserCacheService: Fetching user info for ${userId}`);
      const response = await userService.getUserById(userId);
      
      if (response.success && response.data) {
        const userInfo = {
          firstname: response.data.firstname || 'Unknown',
          lastname: response.data.lastname || 'User',
          avatarUrl: response.data.avatarUrl || null,
          email: response.data.email || 'unknown@example.com'
        };
        
        console.log(`✅ UserCacheService: Cached user info for ${userId}:`, userInfo);
        return userInfo;
      } else {
        throw new Error('Invalid response format');
      }
    } catch (error) {
      console.error(`❌ UserCacheService: Failed to fetch user info for ${userId}:`, error);
      
      // Return fallback data
      return {
        firstname: 'Unknown',
        lastname: 'User',
        avatarUrl: null,
        email: 'unknown@example.com'
      };
    }
  }

  /**
   * Get display name for user
   * @param {string} userId - User ID
   * @returns {Promise<string>} - Display name
   */
  async getDisplayName(userId) {
    const userInfo = await this.getUserInfo(userId);
    return `${userInfo.firstname} ${userInfo.lastname}`;
  }

  /**
   * Get avatar URL for user
   * @param {string} userId - User ID
   * @returns {Promise<string|null>} - Avatar URL
   */
  async getAvatarUrl(userId) {
    const userInfo = await this.getUserInfo(userId);
    return userInfo.avatarUrl;
  }

  /**
   * Invalidate cache for specific user (call when user updates their profile)
   * @param {string} userId - User ID
   */
  invalidateUser(userId) {
    console.log(`🔄 UserCacheService: Invalidating cache for user ${userId}`);
    this.cache.delete(userId);
  }

  /**
   * Clear all cache
   */
  clearCache() {
    console.log('🗑️ UserCacheService: Clearing all cache');
    this.cache.clear();
    this.pendingRequests.clear();
  }

  /**
   * Preload user info for multiple users
   * @param {string[]} userIds - Array of user IDs
   */
  async preloadUsers(userIds) {
    console.log(`📦 UserCacheService: Preloading ${userIds.length} users`);
    const promises = userIds.map(userId => this.getUserInfo(userId));
    await Promise.all(promises);
  }
}

// Export singleton instance
export default new UserCacheService();

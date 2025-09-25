import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  cacheChannelMessages
} from '@/stores/slices/channelSlice';
import chatService from '@/services/chatService';

/**
 * Simplified Message Strategy
 * - WebSocket: Cho tin nhắn real-time mới
 * - API: Tin nhắn cũ đã được load cùng với channels
 * - Real-time messages được thêm vào cache hiện có
 */
export const useHybridMessageStrategy = (channelId) => {
  const dispatch = useDispatch();
  const { 
    messageCache, 
    preloadedChannels 
  } = useSelector((state) => state.channel);

  const cachedMessages = messageCache[channelId] || [];
  const isPreloaded = preloadedChannels[channelId] || false;

  /**
   * Load additional historical messages for pagination
   * Only called when user scrolls to top and needs more messages
   */
  const loadMoreMessages = useCallback(async (page = 0, pageSize = 20) => {
    if (!channelId) return;

    try {
      console.log(`📚 Loading more messages for channel ${channelId}, page: ${page}`);
      
      const response = await chatService.getChannelMessages(channelId, {
        page,
        pageSize
      });

      if (response.success && response.data.length > 0) {
        // Prepend older messages to existing cache
        const currentMessages = messageCache[channelId] || [];
        const newMessages = [...response.data, ...currentMessages];
        
        dispatch(cacheChannelMessages({ 
          channelId, 
          messages: newMessages 
        }));
        
        console.log(`✅ Loaded ${response.data.length} more messages for channel ${channelId}`);
        return response.data;
      }
      
      return [];
    } catch (error) {
      console.error(`❌ Error loading more messages for channel ${channelId}:`, error);
      return [];
    }
  }, [channelId, dispatch, messageCache]);


  /**
   * Add real-time message from WebSocket
   */
  const addRealtimeMessage = useCallback((message) => {
    if (!channelId) return;

    const currentMessages = messageCache[channelId] || [];
    const newMessages = [...currentMessages, message];
    
    dispatch(cacheChannelMessages({ 
      channelId, 
      messages: newMessages 
    }));
    
    console.log(`📨 Added real-time message to channel ${channelId}, total: ${newMessages.length}`);
  }, [channelId, messageCache, dispatch]);

  /**
   * Get message preview for channel list
   */
  const getMessagePreview = useCallback(() => {
    if (!channelId || !cachedMessages.length) return null;
    
    const lastMessage = cachedMessages[cachedMessages.length - 1];
    return {
      content: lastMessage.content,
      timestamp: lastMessage.timestamp,
      type: lastMessage.type,
      senderName: lastMessage.senderName
    };
  }, [channelId, cachedMessages]);


  return {
    messages: cachedMessages,
    isPreloaded,
    messagePreview: getMessagePreview(),
    addRealtimeMessage,
    loadMoreMessages
  };
};

export default useHybridMessageStrategy;

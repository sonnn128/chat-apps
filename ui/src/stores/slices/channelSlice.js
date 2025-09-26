import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  addMembersToChannel,
  sendChannelMessage,
  fetchDeleteChannel,
} from "@/stores/middlewares/channelMiddleware";

const initialState = {
  channels: [],
  currentChannelId: null,
  currentChannel: null,
  error: null,
  status: "idle",
  // Message cache for each channel
  messageCache: {}, // Cache messages for each channel
  preloadedChannels: {}, // Track which channels have been preloaded (channelId: true)
};

const channelSlice = createSlice({
  name: "channels",
  initialState,
  reducers: {
    createChannel: (state, action) => {
      state.channels.push(action.payload);
    },
    addChannel: (state, action) => {
      const channelData = action.payload;
      const newChannel = {
        id: channelData.channelId,
        channelName: channelData.channelName,
        createdAt: channelData.createdAt,
        messages: [],
        memberIds: channelData.memberIds || [],
        participants: channelData.participants || (channelData.memberIds ? 
          channelData.memberIds.map(memberId => ({
            userId: memberId,
            firstname: `User`,
            lastname: memberId.substring(0, 8),
            name: `User ${memberId.substring(0, 8)}`, // Fallback for backward compatibility
            email: `${memberId.substring(0, 8)}@example.com`, // Temporary email
            avatar: null,
            role: 'MEMBER' // Default role in uppercase
          })) : [])
      };
      
      // Check if channel already exists to avoid duplicates
      const existingChannel = state.channels.find(ch => ch.id === channelData.channelId);
      if (!existingChannel) {
        state.channels.push(newChannel);
        console.log("✅ ChannelSlice: Added new channel from real-time event:", newChannel);
      } else {
        console.log("⚠️ ChannelSlice: Channel already exists, skipping:", channelData.channelId);
      }
    },
    setChannels: (state, action) => {
      state.channels = action.payload;
    },
    selectChannel: (state, action) => {
      state.currentChannel = action.payload;
    },
    removeChannel: (state, action) => {
      state.channels = state.channels.filter(
        (channel) => channel.id !== action.payload
      );
      if (state.currentChannel?.id === action.payload) {
        state.currentChannel = null;
      }
    },
    removeCurrentChannel: (state) => {
      state.currentChannel = null;
      state.currentChannelId = null;
      state.messagesOfCurrentChannel = [];
    },

    setCurrentChannel: (state, action) => {
      const channel = action.payload;
      state.currentChannel = channel;
      state.currentChannelId = channel?.id || null;
    },
    receiveMessage: (state, action) => {
      const channelId = action.payload.key.channelId;
      const channelFind = state.channels.find((item) => item.id == channelId);
      
      // Add senderName for real-time messages
      const messageWithSender = {
        ...action.payload,
        senderName: action.payload.senderName || "Unknown User",
        senderAvatar: action.payload.senderAvatar || null
      };
      
      if (channelFind) {
        if (!channelFind.messages) {
          channelFind.messages = [];
        }
        channelFind.messages.push(messageWithSender);
        console.log("✅ Channel: Message added to channel", channelId, "Total messages:", channelFind.messages.length);
      } else {
        console.warn("⚠️ Channel: Channel not found for message:", channelId);
      }
      
      // Also update messageCache for real-time UI updates
      if (!state.messageCache[channelId]) {
        state.messageCache[channelId] = [];
      }
      state.messageCache[channelId].push(messageWithSender);
      
      // Sort messages by timestamp (oldest first)
      state.messageCache[channelId].sort((a, b) => {
        const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
        const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
        return timestampA - timestampB;
      });
      
      console.log("✅ Channel: Message added to messageCache", channelId, "Total cached messages:", state.messageCache[channelId].length);
    },
    
    // Message cache actions
    cacheChannelMessages: (state, action) => {
      const { channelId, messages } = action.payload;
      state.messageCache[channelId] = messages;
      state.preloadedChannels[channelId] = true;
      console.log(`✅ Channel: Cached ${messages.length} messages for channel ${channelId}`);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCreateChannel.fulfilled, (state, action) => {
        state.loading = false;
        console.log("✅ Channel: Channel created successfully:", action.payload);

        const channelCreate = {
          ...action.payload,
          participants: [],
          messages: [],
        };
        console.log("✅ Channel: Channel data prepared:", channelCreate);

        state.channels.push(channelCreate);
        state.currentChannel = channelCreate;
        state.currentChannelId = channelCreate?.id || null;
        console.log("✅ Channel: Channel added to state and set as current");
      })
      .addCase(fetchCreateChannel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
        console.error("❌ Channel: Failed to create channel:", action.error);
      })
      .addCase(fetchAllChannels.fulfilled, (state, action) => {
        state.loading = false;
        console.log("📋 ChannelSlice: fetchAllChannels.fulfilled - action.payload:", action.payload);
        console.log("📋 ChannelSlice: action.payload type:", typeof action.payload);
        console.log("📋 ChannelSlice: action.payload length:", action.payload?.length);
        
        const channels = action.payload || [];
        state.channels = channels;
        
        // Cache messages and participants for each channel if they exist
        channels.forEach(channel => {
          // Cache messages
          if (channel.messages && channel.messages.length > 0) {
            // Debug: Log message structure from API
            console.log(`🔍 Channel: Messages from API for channel ${channel.id}:`, channel.messages);
            console.log(`🔍 Channel: First message structure:`, channel.messages[0]);
            console.log(`🔍 Channel: First message keys:`, Object.keys(channel.messages[0] || {}));
            
            // Sort messages by timestamp (oldest first)
            const sortedMessages = [...channel.messages].sort((a, b) => {
              const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
              const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
              return timestampA - timestampB;
            });
            
            state.messageCache[channel.id] = sortedMessages;
            state.preloadedChannels[channel.id] = true;
            console.log(`✅ Channel: Cached ${sortedMessages.length} messages for channel ${channel.id} (sorted by timestamp)`);
          } else {
            // Initialize empty message cache for channels without messages
            if (!state.messageCache[channel.id]) {
              state.messageCache[channel.id] = [];
            }
            console.log(`✅ Channel: Initialized empty message cache for channel ${channel.id}`);
          }
          
          // Handle participants data - check if we have detailed member info or just memberIds
          if (channel.participants && channel.participants.length > 0) {
            // If participants already have detailed info, use them
            console.log(`✅ Channel: Cached ${channel.participants.length} participants for channel ${channel.id}:`, channel.participants);
          } else if (channel.memberIds && channel.memberIds.length > 0) {
            // If we only have memberIds, convert them to participant objects
            channel.participants = channel.memberIds.map(memberId => ({
              userId: memberId,
              firstname: `User`,
              lastname: memberId.substring(0, 8),
              name: `User ${memberId.substring(0, 8)}`, // Fallback for backward compatibility
              email: `${memberId.substring(0, 8)}@example.com`, // Temporary email
              avatar: null,
              role: 'MEMBER' // Default role in uppercase
            }));
            console.log(`✅ Channel: Converted ${channel.memberIds.length} memberIds to participants for channel ${channel.id}`);
          } else {
            // Initialize empty participants if not provided
            if (!channel.participants) {
              channel.participants = [];
            }
            console.log(`✅ Channel: Initialized empty participants for channel ${channel.id}`);
          }
        });
        
        console.log("✅ Channel: All channels with messages and participants loaded:", state.channels.length, "channels");
        console.log("✅ Channel: Channels data:", state.channels);
      })
      .addCase(fetchAllChannels.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
        console.error("❌ Channel: Failed to load channels:", action.error);
      })
      .addCase(addMembersToChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.joinedChannels = action.payload;
      })
      .addCase(sendChannelMessage.fulfilled, (state, action) => {
        const channelFind = state.channels.find(
          (item) => item.id == action.payload.key.channelId
        );
        
        if (channelFind) {
          if (!channelFind.messages) {
            channelFind.messages = [];
          }
          channelFind.messages.push(action.payload);
          console.log("✅ Channel: Message sent and added to channel:", action.payload.key.channelId);
        } else {
          console.warn("⚠️ Channel: Channel not found for sent message:", action.payload.key.channelId);
        }
      })
      .addCase(sendChannelMessage.rejected, (state, action) => {
        console.error("❌ Channel: Failed to send message:", action.error);
      })
      .addCase(fetchDeleteChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.channels = state.channels.filter(
          (channel) => channel.id !== action.payload.data
        );
        if (state.currentChannelId === action.payload) {
          state.currentChannelId = null;
          state.currentChannel = null;
        }
        state.currentChannelId = null;
        state.currentChannel = null;
      })
      ;
  },
});

export const {
  createChannel,
  addChannel,
  setChannels,
  selectChannel,
  removeChannel,
  removeCurrentChannel,
  setCurrentChannel,
  receiveMessage,
  cacheChannelMessages,
} = channelSlice.actions;
export default channelSlice.reducer;

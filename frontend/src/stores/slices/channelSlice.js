import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  addMembersToChannel,
  addPeopleToChannel,
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
      const channelData = action.payload;
      const newChannel = {
        id: channelData.id,
        channelName: channelData.channelName,
        createdAt: channelData.createdAt,
        messages: channelData.message ? [channelData.message] : [],
        memberIds: channelData.memberIds || [],
        participants: channelData.participants || []
      };
      state.channels.push(newChannel);
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
      const messageId = action.payload.key.messageId;
      let channelFind = state.channels.find((item) => item.id == channelId);
      
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
        // Check if message already exists (deduplication)
        const messageExists = channelFind.messages.some(msg => msg.key.messageId === messageId);
        if (!messageExists) {
          channelFind.messages.push(messageWithSender);
          console.log("✅ Channel: Message added to channel", channelId, "Total messages:", channelFind.messages.length);
        } else {
          console.log("⚠️ Channel: Message already exists, skipping duplicate:", messageId);
        }
      } else {
        console.warn("⚠️ Channel: Channel not found for message:", channelId);
        
        // If this is a notice message (server-created notice such as friend-connect), create a basic channel
        // This covers different notice text/locales (e.g., "You are connected on messenger")
        if (action.payload.type === "NOTICE") {
          console.log("📨 Channel: Creating basic channel for notice message:", channelId);
          const newChannel = {
            id: channelId,
            channelName: "New Channel", // Will be updated when user clicks on it or when channel info arrives
            createdAt: new Date().toISOString(),
            messages: [messageWithSender],
            memberIds: [],
            participants: [],
            isNewChannel: true
          };
          state.channels.push(newChannel);
          console.log("✅ Channel: Basic channel created for notice message:", channelId);
        }
      }
      
      // Also update messageCache for real-time UI updates
      if (!state.messageCache[channelId]) {
        state.messageCache[channelId] = [];
      }
      
      // Check if message already exists in cache (deduplication)
      const cacheMessageExists = state.messageCache[channelId].some(msg => msg.key.messageId === messageId);
      if (!cacheMessageExists) {
        state.messageCache[channelId].push(messageWithSender);
      }
      
      // Sort messages by timestamp (oldest first)
      state.messageCache[channelId].sort((a, b) => {
        const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
        const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
        return timestampA - timestampB;
      });
      
      console.log("✅ Channel: Message added to messageCache", channelId, "Total cached messages:", state.messageCache[channelId].length);
    },
    
    // Handle notification when user is added to a channel
    receiveChannelAddedNotification: (state, action) => {
      const event = action.payload;
      console.log("📨 ChannelSlice: Received channel added notification:", event);
      
      // Create new channel object for the added user
      const newChannel = {
        id: event.channelId,
        channelName: event.channelName,
        createdAt: event.addedAt,
        messages: [],
        memberIds: event.newMemberIds,
        participants: event.newMemberNames?.map((name, index) => ({
          userId: event.newMemberIds[index],
          name: name,
          firstname: name.split(' ')[0] || 'User',
          lastname: name.split(' ').slice(1).join(' ') || 'Unknown',
          email: `${event.newMemberIds[index]?.substring(0, 8) || 'unknown'}@example.com`,
          avatar: null,
          avatarUrl: null,
          role: 'MEMBER'
        })) || [],
        isNewChannel: true // Flag to show notification
      };
      
      // Add to channels list if not already there
      const existingChannel = state.channels.find(ch => ch.id === event.channelId);
      if (!existingChannel) {
        state.channels.push(newChannel);
        console.log("✅ ChannelSlice: Added new channel from notification:", event.channelId);
      } else {
        console.log("ℹ️ ChannelSlice: Channel already exists, skipping:", event.channelId);
      }
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
          participants: action.payload.participants || [],
          messages: action.payload.message ? [action.payload.message] : [],
        };
        console.log("✅ Channel: Channel data prepared with notice message:", channelCreate);

        // Check if channel already exists
        const existingChannel = state.channels.find(ch => ch.id === channelCreate.id);
        if (!existingChannel) {
            state.channels.push(channelCreate);
            console.log("✅ Channel: Channel added to state");
        } else {
            console.log("ℹ️ Channel: Channel already exists, updating data:", channelCreate.id);
            // Update properties that might have been placeholder values
            existingChannel.channelName = channelCreate.channelName;
            existingChannel.participants = channelCreate.participants;
            existingChannel.memberIds = channelCreate.memberIds;
            existingChannel.createdAt = channelCreate.createdAt;
            // We don't overwrite messages here to preserve any real-time messages received
        }

        state.currentChannel = channelCreate;
        state.currentChannelId = channelCreate?.id || null;
        
        // Cache notice message for the new channel
        if (action.payload.message) {
          state.messageCache[action.payload.id] = [action.payload.message];
          state.preloadedChannels[action.payload.id] = true;
          console.log("✅ Channel: Notice message cached for new channel:", action.payload.id);
        }
        
        console.log("✅ Channel: Channel set as current");
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
      .addCase(addPeopleToChannel.fulfilled, (state, action) => {
        state.loading = false;
        console.log("✅ Channel: People added to channel successfully:", action.payload);
        
        const responseData = action.payload.data;
        
        // Update participants in current channel if it matches
        if (state.currentChannel && responseData.channelId === state.currentChannel.id) {
          // Add new members to participants
          if (responseData.newMembers && responseData.newMembers.length > 0) {
            const newParticipants = responseData.newMembers.map(member => ({
              userId: member.id,
              firstname: member.firstname || 'User',
              lastname: member.lastname || 'Unknown',
              name: `${member.firstname || 'User'} ${member.lastname || 'Unknown'}`,
              email: member.email || `${member.id?.substring(0, 8) || 'unknown'}@example.com`,
              avatar: member.avatarUrl || null,
              avatarUrl: member.avatarUrl || null,
              role: 'MEMBER'
            }));
            
            state.currentChannel.participants = [...(state.currentChannel.participants || []), ...newParticipants];
            console.log("✅ Channel: Updated current channel participants:", state.currentChannel.participants.length);
          }
          
          // Add notice message to current channel messages
          if (responseData.message) {
            if (!state.currentChannel.messages) {
              state.currentChannel.messages = [];
            }
            state.currentChannel.messages.push(responseData.message);
            console.log("✅ Channel: Added notice message to current channel");
            
            // Also cache notice message for real-time UI updates
            if (!state.messageCache[responseData.channelId]) {
              state.messageCache[responseData.channelId] = [];
            }
            state.messageCache[responseData.channelId].push(responseData.message);
            
            // Sort messages by timestamp (oldest first)
            state.messageCache[responseData.channelId].sort((a, b) => {
              const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
              const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
              return timestampA - timestampB;
            });
            
            console.log("✅ Channel: Notice message cached for real-time UI:", responseData.channelId, "Total cached messages:", state.messageCache[responseData.channelId].length);
          }
        }
        
        // Also update in channels list
        const channelIndex = state.channels.findIndex(ch => ch.id === responseData.channelId);
        if (channelIndex !== -1) {
          if (responseData.newMembers && responseData.newMembers.length > 0) {
            const newParticipants = responseData.newMembers.map(member => ({
              userId: member.id,
              firstname: member.firstname || 'User',
              lastname: member.lastname || 'Unknown',
              name: `${member.firstname || 'User'} ${member.lastname || 'Unknown'}`,
              email: member.email || `${member.id?.substring(0, 8) || 'unknown'}@example.com`,
              avatar: member.avatarUrl || null,
              avatarUrl: member.avatarUrl || null,
              role: 'MEMBER'
            }));
            
            state.channels[channelIndex].participants = [...(state.channels[channelIndex].participants || []), ...newParticipants];
            console.log("✅ Channel: Updated channel in list participants:", state.channels[channelIndex].participants.length);
          }
          
          // Add notice message to channel messages
          if (responseData.message) {
            if (!state.channels[channelIndex].messages) {
              state.channels[channelIndex].messages = [];
            }
            state.channels[channelIndex].messages.push(responseData.message);
            console.log("✅ Channel: Added notice message to channel in list");
          }
        }
      })
      .addCase(sendChannelMessage.fulfilled, (state, action) => {
        const message = action.payload;
        const channelId = message.key.channelId;
        
        // Add message to channel messages
        const channelFind = state.channels.find(
          (item) => item.id == channelId
        );
        
        if (channelFind) {
          if (!channelFind.messages) {
            channelFind.messages = [];
          }
          channelFind.messages.push(message);
          console.log("✅ Channel: Message sent and added to channel:", channelId);
        } else {
          console.warn("⚠️ Channel: Channel not found for sent message:", channelId);
        }
        
        // Also cache message for real-time UI updates
        if (!state.messageCache[channelId]) {
          state.messageCache[channelId] = [];
        }
        state.messageCache[channelId].push(message);
        
        // Sort messages by timestamp (oldest first)
        state.messageCache[channelId].sort((a, b) => {
          const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
          const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
          return timestampA - timestampB;
        });
        
        console.log("✅ Channel: Message cached for real-time UI:", channelId, "Total cached messages:", state.messageCache[channelId].length);
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
  receiveChannelAddedNotification,
  cacheChannelMessages,
} = channelSlice.actions;
export default channelSlice.reducer;

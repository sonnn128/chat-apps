import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  fetchAllMembersOfChannel,
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
      state.currentChannel = channel;
    },
    receiveMessage: (state, action) => {
      const channelId = action.payload.key.channelId;
      const channelFind = state.channels.find((item) => item.id == channelId);
      
      if (channelFind) {
        if (!channelFind.messages) {
          channelFind.messages = [];
        }
        channelFind.messages.push(action.payload);
        console.log("✅ Channel: Message added to channel", channelId, "Total messages:", channelFind.messages.length);
      } else {
        console.warn("⚠️ Channel: Channel not found for message:", channelId);
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
        
        // Cache messages for each channel if they exist
        channels.forEach(channel => {
          if (channel.messages && channel.messages.length > 0) {
            state.messageCache[channel.id] = channel.messages;
            state.preloadedChannels[channel.id] = true;
            console.log(`✅ Channel: Cached ${channel.messages.length} messages for channel ${channel.id}`);
          } else {
            // Initialize empty message cache for channels without messages
            if (!state.messageCache[channel.id]) {
              state.messageCache[channel.id] = [];
            }
            console.log(`✅ Channel: Initialized empty message cache for channel ${channel.id}`);
          }
        });
        
        console.log("✅ Channel: All channels with messages loaded:", state.channels.length, "channels");
        console.log("✅ Channel: Channels data:", state.channels);
      })
      .addCase(fetchAllChannels.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
        console.error("❌ Channel: Failed to load channels:", action.error);
      })
      .addCase(fetchAllMembersOfChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.joinedChannels = action.payload;
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
  setChannels,
  selectChannel,
  removeChannel,
  removeCurrentChannel,
  setCurrentChannel,
  receiveMessage,
  cacheChannelMessages,
} = channelSlice.actions;
export default channelSlice.reducer;

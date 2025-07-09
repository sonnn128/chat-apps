import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  fetchAllMembersOfChannel,
  addMembersToChannel,
  sendChannelMessage,
} from "@/stores/middlewares/channelMiddleware";

const initialState = {
  channels: [],
  currentChannelId: null,
  currentChannel: null,
  error: null,
  status: "idle",
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
      channelFind.messages.push(action.payload);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCreateChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.channels.push(action.payload);
        state.currentChannel = action.payload;
        state.currentChannelId = action.payload?.id || null;
        state.messagesOfCurrentChannel = action.payload?.messages || [];
      })
      .addCase(fetchAllChannels.fulfilled, (state, action) => {
        state.loading = false;
        state.channels = action.payload;
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
        channelFind.messages.push(action.payload);
      });
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
} = channelSlice.actions;
export default channelSlice.reducer;

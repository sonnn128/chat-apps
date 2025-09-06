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

        const channelCreate = {
          ...action.payload.channel,
          participants: action.payload.participants,
          messages: action.payload.messages || [],
        };
        console.log("channelCreate: ", channelCreate);

        state.channels.push(channelCreate);
        state.currentChannel = channelCreate;
        state.currentChannelId = channelCreate?.id || null;

        const messageReceived = action.payload.message;
        if (messageReceived) {
          const channelId = messageReceived.key.channelId;
          const channelFind = state.channels.find(
            (item) => item.id === channelId
          );

          if (channelFind) {
            channelFind.messages.push(messageReceived);
          }
        }
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
} = channelSlice.actions;
export default channelSlice.reducer;

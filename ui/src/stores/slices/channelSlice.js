import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  fetchAllMembersOfChannel,
  addMembersToChannel,
} from "@/stores/middlewares/channelMiddleware";

const initialState = {
  channels: [],
  currentChannel: null,
  loading: false,
  error: null,
  currentChannelId: null,
  joinedChannels: [],
  messagesByChannel: {},
  messagesOfCurrentChannel: [],
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
    receiveMessage: (state, action) => {
      const message = action.payload;
      if (message && typeof message === "object" && state.currentChannelId) {
        const channelId = state.currentChannelId;
        state.messagesByChannel[channelId] =
          state.messagesByChannel[channelId] || [];
        state.messagesByChannel[channelId] = [
          ...state.messagesByChannel[channelId],
          message,
        ];
        state.messagesOfCurrentChannel = [
          ...state.messagesByChannel[channelId],
        ];
      }
    },
    setCurrentChannel: (state, action) => {
      const channel = action.payload;
      state.currentChannel = channel;
      state.currentChannelId = channel?.id || null;

      if (channel?.id) {
        state.messagesByChannel[channel.id] =
          state.messagesByChannel[channel.id] || [];

        if (Array.isArray(channel?.messages)) {
          const existingLocalMessages = state.messagesByChannel[channel.id] || [];
          state.messagesByChannel[channel.id] = [
            ...channel.messages,
            ...existingLocalMessages.filter(
              (msg) =>
                !channel.messages.some((m) => m.key?.messageId === msg.key?.messageId)
            ),
          ];
        }

        state.messagesOfCurrentChannel = [...state.messagesByChannel[channel.id]];
      } else {
        state.messagesOfCurrentChannel = [];
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCreateChannel.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCreateChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.channels.push(action.payload);
        state.currentChannel = action.payload;
        state.currentChannelId = action.payload?.id || null;
        state.messagesOfCurrentChannel = action.payload?.messages || [];
      })
      .addCase(fetchCreateChannel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(fetchAllChannels.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllChannels.fulfilled, (state, action) => {
        state.loading = false;
        console.log("action.payload: ", action.payload);
        
        state.channels = action.payload.data;
        // action.payload.forEach((channel) => {
        //   if (channel.id && Array.isArray(channel.messages)) {
        //     state.messagesByChannel[channel.id] =
        //       state.messagesByChannel[channel.id] || [];
        //     state.messagesByChannel[channel.id] = [
        //       ...channel.messages,
        //       ...state.messagesByChannel[channel.id].filter(
        //         (msg) => !channel.messages.some((m) => m.id === msg.id)
        //       ),
        //     ];
        //   }
        // });
        // if (state.currentChannelId) {
        //   state.messagesOfCurrentChannel =
        //     state.messagesByChannel[state.currentChannelId] || [];
        // }
      })
      .addCase(fetchAllChannels.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(fetchAllMembersOfChannel.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllMembersOfChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.joinedChannels = action.payload;
      })
      .addCase(fetchAllMembersOfChannel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(addMembersToChannel.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addMembersToChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.joinedChannels = action.payload;
      })
      .addCase(addMembersToChannel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
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
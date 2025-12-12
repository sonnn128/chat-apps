import { createAsyncThunk } from "@reduxjs/toolkit";
import channelService from "@/services/channelService";
import chatService from "@/services/chatService";

export const fetchCreateChannel = createAsyncThunk(
  "channels/createChannel",
  async (form) => {
    const res = await channelService.createChannel(form)
    return res.data
  }
);

export const fetchAllChannels = createAsyncThunk( 
  "channels/fetchAllChannels",
  async (_, { rejectWithValue }) => {
    try {
      const res = await channelService.getChannels();
      
      if (res.success) {
        return res.data;
      } else {
        return rejectWithValue(res.message);
      }
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const addMembersToChannel = createAsyncThunk(
  "channels/addMembersToChannel",
  async ({ channelId, userIds }) =>
    await channelService.addMembersToChannel(channelId, userIds)
);

export const addPeopleToChannel = createAsyncThunk(
  "channels/addPeopleToChannel",
  async ({ channelId, memberIds }) =>
    await channelService.addPeopleToChannel(channelId, memberIds)
);

export const sendChannelMessage = createAsyncThunk(
  "chat/sendChannelMessage",
  async (form) => await chatService.sendChannelMessage(form)
);

export const fetchDeleteChannel = createAsyncThunk(
  "channels/deleteChannel",
  async (channelId) => await channelService.deleteChannel(channelId)
);

export const fetchGetOrCreateDirectChannel = createAsyncThunk(
  "channels/fetchGetOrCreateDirectChannel",
  async (friendId) => {
    const res = await channelService.getOrCreateDirectChannel(friendId);
    return res.data;
  }
);
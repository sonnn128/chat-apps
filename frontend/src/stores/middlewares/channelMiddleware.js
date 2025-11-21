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
      console.log("📋 ChannelMiddleware: Fetching all channels...");
      const res = await channelService.getChannels();
      
      if (res.success) {
        console.log("✅ ChannelMiddleware: Channels fetched successfully:", res.data?.length || 0, "channels");
        return res.data;
      } else {
        console.error("❌ ChannelMiddleware: Failed to fetch channels:", res.message);
        return rejectWithValue(res.message);
      }
    } catch (error) {
      console.error("❌ ChannelMiddleware: Error fetching channels:", error);
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
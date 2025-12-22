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

export const fetchChannelById = createAsyncThunk(
  "channels/fetchChannelById",
  async (channelId) => {
    console.log("🔍 channelMiddleware: fetchChannelById called for:", channelId);
    try {
      const res = await channelService.getChannelById(channelId);
      console.log("✅ channelMiddleware: fetchChannelById response:", res);
      return res.data;
    } catch (error) {
      console.error("❌ channelMiddleware: fetchChannelById error:", error);
      throw error;
    }
  }
);

export const fetchGetOrCreateDirectChannel = createAsyncThunk(
  "channels/fetchGetOrCreateDirectChannel",
  async (friendId) => {
    const res = await channelService.getOrCreateDirectChannel(friendId);
    return res.data;
  }
);

export const updateChannelTheme = createAsyncThunk(
  "channels/updateChannelTheme",
  async ({ channelId, themeColor, themeGradient }) => {
    const res = await channelService.updateChannelTheme(channelId, themeColor, themeGradient);
    return res.data;
  }
);

export const removeMemberFromChannel = createAsyncThunk(
  "channels/removeMemberFromChannel",
  async ({ channelId, memberId }) => {
    await channelService.removeMember(channelId, memberId);
    return { channelId, memberId };
  }
);

export const markChannelAsRead = createAsyncThunk(
  "channels/markChannelAsRead",
  async (channelId) => await channelService.markAsRead(channelId)
);
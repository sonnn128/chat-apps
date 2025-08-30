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
  async () => {
    const res = await channelService.getChannels();
    return res.data;
  }
);

export const fetchAllMembersOfChannel = createAsyncThunk(
  "channels/getAllMembersOfChannel",
  async (channelId) => await channelService.getAllMembersOfChannel(channelId)
);
export const addMembersToChannel = createAsyncThunk(
  "channels/addMembersToChannel",
  async ({ channelId, userIds }) =>
    await channelService.addMembersToChannel(channelId, userIds)
);

export const sendChannelMessage = createAsyncThunk(
  "chat/sendChannelMessage",
  async (form) => await chatService.sendChannelMessage(form)
);

import { createAsyncThunk } from "@reduxjs/toolkit";
import friendshipService from "@/services/friendshipService";

export const fetchFriendList = createAsyncThunk(
  "friendship/fetchFriendList",
  async () => await friendshipService.getFriendList()
);

export const fetchFriendSuggestions = createAsyncThunk(
  "friendship/fetchFriendSuggestions",
  async () => {
    const res = await friendshipService.getFriendSuggestions();
    return res.data;
  }
);

export const fetchPendingRequests = createAsyncThunk(
  "friendship/fetchPendingRequests",
  async () => {
    const res = await friendshipService.getPendingRequests();
    return res.data;
  }
);

export const sendFriendRequest = createAsyncThunk(
  "friendship/sendFriendRequest",
  async (friendId) => await friendshipService.sendFriendRequest(friendId)
);

export const acceptFriendRequest = createAsyncThunk(
  "friendship/acceptFriendRequest",
  async (friendId) => await friendshipService.acceptFriendRequest(friendId)
);

export const removeFriend = createAsyncThunk(
  "friendship/removeFriend",
  async (friendId) => {
    await friendshipService.unfriendUser(friendId);
    return { friendId };
  }
);

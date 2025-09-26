import { createAsyncThunk } from "@reduxjs/toolkit";
import friendshipService from "@/services/friendshipService";

export const fetchFriendList = createAsyncThunk(
  "friendship/fetchFriendList",
  async () => await friendshipService.getFriendList()
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

export const rejectFriendRequest = createAsyncThunk(
  "friendship/rejectFriendRequest",
  async (friendId) => await friendshipService.rejectFriendRequest(friendId)
);

export const cancelFriendRequest = createAsyncThunk(
  "friendship/cancelFriendRequest",
  async (friendId) => await friendshipService.cancelFriendRequest(friendId)
);

export const unfriendUser = createAsyncThunk(
  "friendship/unfriendUser",
  async (friendId) => await friendshipService.unfriendUser(friendId)
);


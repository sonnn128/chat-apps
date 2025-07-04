// friendshipSlice.js
import { createSlice } from "@reduxjs/toolkit";
import {
  fetchFriendList,
  fetchFriendSuggestions,
  fetchPendingRequests,
  sendFriendRequest,
  acceptFriendRequest,
  removeFriend,
} from "@/stores/middlewares/friendShipMiddleware";

const initialState = {
  currentFriend: null,
  friends: [],
  friendSuggestions: [],
  pendingRequests: [],
  loading: false,
  error: null,
};

const handlePending = (state) => {
  state.loading = true;
  state.error = null;
};

const handleRejected = (state, action) => {
  state.loading = false;
  state.error = action.payload;
};

const friendshipSlice = createSlice({
  name: "friendship",
  initialState,
  reducers: {
    setCurrentFriend: (state, action) => {
      state.currentFriend = action.payload;
    },
    removeCurrentFriend: (state) => {
      state.currentFriend = null;
    },
    clearFriendSuggestions: (state) => {
      state.friendSuggestions = [];
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Friend List
      .addCase(fetchFriendList.pending, handlePending)
      .addCase(fetchFriendList.fulfilled, (state, action) => {
        state.loading = false;
        state.friends = action.payload;
      })
      .addCase(fetchFriendList.rejected, handleRejected)

      // Fetch Friend Suggestions
      .addCase(fetchFriendSuggestions.pending, handlePending)
      .addCase(fetchFriendSuggestions.fulfilled, (state, action) => {
        state.loading = false;
        state.friendSuggestions = action.payload;
      })
      .addCase(fetchFriendSuggestions.rejected, handleRejected)

      // Fetch Pending Requests
      .addCase(fetchPendingRequests.pending, handlePending)
      .addCase(fetchPendingRequests.fulfilled, (state, action) => {
        state.loading = false;
        state.pendingRequests = action.payload;
      })
      .addCase(fetchPendingRequests.rejected, handleRejected)

      // Send Friend Request
      .addCase(sendFriendRequest.pending, handlePending)
      .addCase(sendFriendRequest.fulfilled, (state, action) => {
        state.loading = false;
        // Remove the user from suggestions if they were there
        state.friendSuggestions = state.friendSuggestions.filter(
          (suggestion) => suggestion.id !== action.payload.friendId
        );
        state.pendingRequests.push(action.payload);
      })
      .addCase(sendFriendRequest.rejected, handleRejected)

      // Accept Friend Request
      .addCase(acceptFriendRequest.pending, handlePending)
      .addCase(acceptFriendRequest.fulfilled, (state, action) => {
        state.loading = false;
        const acceptedRequest = state.pendingRequests.find(
          (req) => req.id.friendId === action.payload.id.friendId
        );
        state.pendingRequests = state.pendingRequests.filter(
          (req) => req.id.friendId !== action.payload.id.friendId
        );
        if (acceptedRequest) {
          state.friends.push(action.payload);
        }
      })
      .addCase(acceptFriendRequest.rejected, handleRejected)

      // Remove Friend
      .addCase(removeFriend.pending, handlePending)
      .addCase(removeFriend.fulfilled, (state, action) => {
        state.loading = false;
        state.friends = state.friends.filter(
          (friend) => friend.id.friendId !== action.payload.friendId
        );
      })
      .addCase(removeFriend.rejected, handleRejected);
  },
});

export const { 
  setCurrentFriend, 
  removeCurrentFriend,
  clearFriendSuggestions 
} = friendshipSlice.actions;
export default friendshipSlice.reducer;
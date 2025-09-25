// friendshipSlice.js
import { createSlice } from "@reduxjs/toolkit";
import {
  fetchFriendList,
  fetchPendingRequests,
  sendFriendRequest,
  acceptFriendRequest,
  removeFriend,
} from "@/stores/middlewares/friendShipMiddleware";

const initialState = {
  currentFriend: null,
  friends: [],
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
    receiveFriendRequest: (state, action) => {
      const event = action.payload;
      console.log("📨 FriendshipSlice: Received friend request event:", event);
      
      // Add to pending requests if not already there
      const existingRequest = state.pendingRequests.find(
        req => req.friendshipKey?.requesterId === event.requesterId && 
               req.friendshipKey?.friendId === event.friendId
      );
      
      if (!existingRequest) {
        const newRequest = {
          friendshipKey: {
            requesterId: event.requesterId,
            friendId: event.friendId
          },
          status: "PENDING",
          createdAt: event.createdAt
        };
        state.pendingRequests.push(newRequest);
        console.log("✅ FriendshipSlice: Added friend request to pending requests");
      }
    },
    receiveFriendRequestAccepted: (state, action) => {
      const event = action.payload;
      console.log("📨 FriendshipSlice: Received friend request accepted event:", event);
      
      // Remove from pending requests
      state.pendingRequests = state.pendingRequests.filter(
        req => !(req.friendshipKey?.requesterId === event.requesterId && 
                 req.friendshipKey?.friendId === event.accepterId)
      );
      
      // Add to friends list
      const newFriend = {
        friendshipKey: {
          requesterId: event.requesterId,
          friendId: event.accepterId
        },
        status: "ACCEPTED",
        acceptedAt: event.acceptedAt
      };
      state.friends.push(newFriend);
      console.log("✅ FriendshipSlice: Added friend to friends list");
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
  receiveFriendRequest,
  receiveFriendRequestAccepted
} = friendshipSlice.actions;
export default friendshipSlice.reducer;
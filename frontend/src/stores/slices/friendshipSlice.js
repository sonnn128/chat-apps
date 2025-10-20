// friendshipSlice.js
import { createSlice } from "@reduxjs/toolkit";
import {
  fetchFriendList,
  fetchPendingRequests,
  sendFriendRequest,
  acceptFriendRequest,
  rejectFriendRequest,
  cancelFriendRequest,
  unfriendUser,
} from "@/stores/middlewares/friendShipMiddleware";

const initialState = {
  currentFriend: null,
  friends: [],
  pendingRequests: [],
  sentRequests: [], // Track requests sent by current user
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
        req => req.requesterId === event.requesterId && 
               req.friendId === event.friendId
      );
      
      if (!existingRequest) {
        const newRequest = {
          requesterId: event.requesterId,
          friendId: event.friendId,
          requesterFirstname: "Unknown", // Will be populated by API call
          requesterLastname: "User",
          requesterEmail: "unknown@example.com",
          requesterAvatar: null,
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
      
      // Remove from pending requests (for the person who accepted)
      state.pendingRequests = state.pendingRequests.filter(
        req => !(req.requesterId === event.requesterId && req.friendId === event.accepterId)
      );
      
      // Remove from sent requests (for the person who sent the request)
      state.sentRequests = state.sentRequests.filter(
        req => req.friendId !== event.accepterId
      );
      
      // Add to friends list
      const newFriend = {
        friendId: event.accepterId,
        firstname: "Unknown", // Will be populated by API call
        lastname: "User",
        email: "unknown@example.com",
        avatar: null,
        status: "ACCEPTED",
        acceptedAt: event.acceptedAt
      };
      state.friends.push(newFriend);
      console.log("✅ FriendshipSlice: Added friend to friends list and removed from pending/sent requests");
    },
    receiveFriendRequestRejected: (state, action) => {
      const event = action.payload;
      console.log("📨 FriendshipSlice: Received friend request rejected event:", event);
      
      // Remove from pending requests - the requesterId is who sent the request
      state.pendingRequests = state.pendingRequests.filter(
        req => req.requesterId !== event.requesterId
      );
      
      // Remove from sent requests (for the person who sent the request)
      state.sentRequests = state.sentRequests.filter(
        req => req.friendId !== event.rejecterId
      );
      
      console.log("✅ FriendshipSlice: Removed rejected friend request from pending and sent requests");
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
        // Add to sentRequests to track what we've sent
        const friendId = action.meta.arg;
        const sentRequest = {
          friendId: friendId,
          status: "PENDING",
          sentAt: new Date().toISOString()
        };
        state.sentRequests.push(sentRequest);
      })
      .addCase(sendFriendRequest.rejected, handleRejected)

      // Accept Friend Request
      .addCase(acceptFriendRequest.pending, handlePending)
      .addCase(acceptFriendRequest.fulfilled, (state, action) => {
        state.loading = false;
        // Remove from pending requests using requesterId from action meta
        const requesterId = action.meta.arg;
        state.pendingRequests = state.pendingRequests.filter(
          (req) => req.requesterId !== requesterId
        );
        // Add to friends list
        state.friends.push(action.payload);
        console.log("✅ FriendshipSlice: Accepted friend request and updated UI");
      })
      .addCase(acceptFriendRequest.rejected, handleRejected)

      // Reject Friend Request
      .addCase(rejectFriendRequest.pending, handlePending)
      .addCase(rejectFriendRequest.fulfilled, (state, action) => {
        state.loading = false;
        // Remove from pending requests using requesterId from action meta
        const requesterId = action.meta.arg;
        state.pendingRequests = state.pendingRequests.filter(
          (req) => req.requesterId !== requesterId
        );
        console.log("✅ FriendshipSlice: Rejected friend request and updated UI");
      })
      .addCase(rejectFriendRequest.rejected, handleRejected)

      // Cancel Friend Request
      .addCase(cancelFriendRequest.pending, handlePending)
      .addCase(cancelFriendRequest.fulfilled, (state, action) => {
        state.loading = false;
        // Remove from sent requests - use the friendId from the action meta
        const friendId = action.meta.arg;
        state.sentRequests = state.sentRequests.filter(
          (req) => req.friendId !== friendId
        );
      })
      .addCase(cancelFriendRequest.rejected, handleRejected)

      // Unfriend User
      .addCase(unfriendUser.pending, handlePending)
      .addCase(unfriendUser.fulfilled, (state, action) => {
        state.loading = false;
        // Remove from friends list - use the friendId from the action meta
        const friendId = action.meta.arg;
        state.friends = state.friends.filter(
          (friend) => friend.friendId !== friendId
        );
      })
      .addCase(unfriendUser.rejected, handleRejected);
  },
});

export const { 
  setCurrentFriend, 
  removeCurrentFriend,
  receiveFriendRequest,
  receiveFriendRequestAccepted,
  receiveFriendRequestRejected
} = friendshipSlice.actions;

export { rejectFriendRequest, cancelFriendRequest, unfriendUser };
export default friendshipSlice.reducer;
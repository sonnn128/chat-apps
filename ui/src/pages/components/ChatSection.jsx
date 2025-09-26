import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import ChatHeader from "@/components/chat/ChatHeader";
import ChatMessages from "@/components/chat/ChatMessages";
import ChatInput from "@/components/chat/ChatInput";
import AddMemberModal from "@/components/modals/AddMemberModal";
import ChatInfoSidebar from "@/components/chat/ChatInfoSidebar";
import { websocketService } from "@/utils/ws";
import { receiveMessage, addChannel } from "@/stores/slices/channelSlice";
import { receiveFriendRequest, receiveFriendRequestAccepted, receiveFriendRequestRejected } from "@/stores/slices/friendshipSlice";
import { fetchPendingRequests, fetchFriendList } from "@/stores/middlewares/friendShipMiddleware";
import { successToast } from "@/utils/toast";

const ChatSection = () => {
  const { channels, currentChannelId } = useSelector((state) => state.channel);

  const currentChannel = channels.find((x) => x.id === currentChannelId);
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  
  console.log("🔍 ChatSection: Component mounted, user:", user);
  console.log("🔍 ChatSection: User ID:", user?.data?.id);
  console.log("🔍 ChatSection: WebSocket connected:", websocketService.isConnected());
  
  useEffect(() => {
    const userId = user?.data?.id;
    if (!userId) {
      console.warn("⚠️ ChatSection: User ID not available, skipping WebSocket subscription");
      return;
    }

    const destination = `/user/${userId}/queue/notifications`;
    console.log("🔔 ChatSection: Setting up WebSocket subscription for user:", userId);
    console.log("🔔 ChatSection: WebSocket destination:", destination);
    console.log("🔔 ChatSection: WebSocket connected:", websocketService.isConnected());
    
    websocketService.subscribe(destination, (message) => {
      console.log("📨 ChatSection: WebSocket message received:", message);
      console.log("📨 ChatSection: Message type:", typeof message);
      console.log("📨 ChatSection: Message keys:", Object.keys(message || {}));
      
      try {
        // Handle different types of real-time events
        if (message.eventType === "FRIEND_REQUEST_SENT") {
          // Only show notification to the person who received the request
          if (message.friendId === user?.data?.id) {
            dispatch(receiveFriendRequest(message));
            // Refresh pending requests to get full user info
            dispatch(fetchPendingRequests());
            successToast("You have a new friend request! 🎉");
            console.log("✅ ChatSection: Friend request event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request event received but not for current user");
          }
        } else if (message.eventType === "FRIEND_REQUEST_ACCEPTED") {
          // Show notification to both users involved
          if (message.requesterId === user?.data?.id || message.accepterId === user?.data?.id) {
            dispatch(receiveFriendRequestAccepted(message));
            // Refresh friend list to get full user info
            dispatch(fetchFriendList());
            
            if (message.accepterId === user?.data?.id) {
              // Person who accepted the request
              successToast("Friend request accepted! 🎉");
            } else if (message.requesterId === user?.data?.id) {
              // Person who sent the request
              successToast("Your friend request was accepted! 🎉");
            }
            
            console.log("✅ ChatSection: Friend request accepted event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request accepted event received but not for current user");
          }
        } else if (message.eventType === "FRIEND_REQUEST_REJECTED") {
          // Show notification to both users involved
          if (message.requesterId === user?.data?.id || message.rejecterId === user?.data?.id) {
            dispatch(receiveFriendRequestRejected(message));
            // Refresh pending requests to update UI
            dispatch(fetchPendingRequests());
            
            if (message.requesterId === user?.data?.id) {
              // Person who sent the request
              successToast("Your friend request was rejected");
            } else if (message.rejecterId === user?.data?.id) {
              // Person who rejected the request
              successToast("Friend request rejected");
            }
            
            console.log("✅ ChatSection: Friend request rejected event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request rejected event received but not for current user");
          }
        } else if (message.eventType === "MESSAGE_SENT") {
          dispatch(receiveMessage(message));
          console.log("✅ ChatSection: Real-time message dispatched to Redux store");
        } else if (message.eventType === "CHANNEL_CREATED") {
          dispatch(addChannel(message));
          console.log("✅ ChatSection: Channel created event dispatched to Redux store");
        } else if (message.key && message.key.channelId) {
          // Handle direct message objects (without eventType wrapper)
          dispatch(receiveMessage(message));
          console.log("✅ ChatSection: Direct message dispatched to Redux store");
        } else if (message.requesterId && message.friendId && !message.eventType) {
          // Handle friend request events without eventType wrapper
          if (message.friendId === user?.data?.id) {
            dispatch(receiveFriendRequest(message));
            dispatch(fetchPendingRequests());
            successToast("You have a new friend request! 🎉");
            console.log("✅ ChatSection: Friend request event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request event received but not for current user");
          }
        } else if (message.requesterId && message.rejecterId && !message.eventType) {
          // Handle friend request rejected events without eventType wrapper
          if (message.requesterId === user?.data?.id) {
            dispatch(receiveFriendRequestRejected(message));
            dispatch(fetchPendingRequests());
            successToast("Friend request rejected");
            console.log("✅ ChatSection: Friend request rejected event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request rejected event received but not for current user");
          }
        } else if (message.requesterId && message.accepterId && !message.eventType) {
          // Handle friend request accepted events without eventType wrapper
          if (message.requesterId === user?.data?.id || message.accepterId === user?.data?.id) {
            dispatch(receiveFriendRequestAccepted(message));
            dispatch(fetchFriendList());
            
            if (message.accepterId === user?.data?.id) {
              // Person who accepted the request
              successToast("Friend request accepted! 🎉");
            } else if (message.requesterId === user?.data?.id) {
              // Person who sent the request
              successToast("Your friend request was accepted! 🎉");
            }
            
            console.log("✅ ChatSection: Friend request accepted event dispatched to Redux store");
          } else {
            console.log("📨 ChatSection: Friend request accepted event received but not for current user");
          }
        } else {
          console.log("📨 ChatSection: Unknown event type:", message.eventType);
        }
      } catch (error) {
        console.error("❌ ChatSection: Error dispatching message:", error);
      }
    });

    // Cleanup function
    return () => {
      console.log("🧹 ChatSection: Cleaning up WebSocket subscription");
    };
  }, [dispatch, user?.data?.id]);

  return (
    <div className="flex-1 flex flex-row bg-white overflow-hidden">
      <div className="flex-1 flex flex-col relative">
        {currentChannelId ? (
          <>
            <ChatHeader title={currentChannel?.name || "Channel"} />
            <ChatMessages />
            <ChatInput />
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-500">
            Select a conversation to start chatting
          </div>
        )}
      </div>

      {currentChannelId && <ChatInfoSidebar />}

      <AddMemberModal open={false} onClose={() => {}} channelId={null} />
    </div>
  );
};

export default ChatSection;

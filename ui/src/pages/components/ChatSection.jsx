import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import ChatHeader from "@/components/chat/ChatHeader";
import ChatMessages from "@/components/chat/ChatMessages";
import ChatInput from "@/components/chat/ChatInput";
import AddMemberModal from "@/components/modals/AddMemberModal";
import ChatInfoSidebar from "@/components/chat/ChatInfoSidebar";
import { websocketService } from "@/utils/ws";
import { receiveMessage } from "@/stores/slices/channelSlice";
import { receiveFriendRequest, receiveFriendRequestAccepted } from "@/stores/slices/friendshipSlice";

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
          dispatch(receiveFriendRequest(message));
          console.log("✅ ChatSection: Friend request event dispatched to Redux store");
        } else if (message.eventType === "FRIEND_REQUEST_ACCEPTED") {
          dispatch(receiveFriendRequestAccepted(message));
          console.log("✅ ChatSection: Friend request accepted event dispatched to Redux store");
        } else if (message.eventType === "MESSAGE_SENT") {
          dispatch(receiveMessage(message));
          console.log("✅ ChatSection: Real-time message dispatched to Redux store");
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

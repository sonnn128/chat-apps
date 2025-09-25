import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import ChatHeader from "@/components/chat/ChatHeader";
import ChatMessages from "@/components/chat/ChatMessages";
import ChatInput from "@/components/chat/ChatInput";
import AddMemberModal from "@/components/modals/AddMemberModal";
import ChatInfoSidebar from "@/components/chat/ChatInfoSidebar";
import { websocketService } from "@/utils/ws";
import { receiveMessage } from "@/stores/slices/channelSlice";

const ChatSection = () => {
  const { channels, currentChannelId } = useSelector((state) => state.channel);

  const currentChannel = channels.find((x) => x.id === currentChannelId);
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  
  useEffect(() => {
    if (!user?.id) {
      console.warn("⚠️ ChatSection: User ID not available, skipping WebSocket subscription");
      return;
    }

    const destination = `/user/${user.id}/queue/notifications`;
    console.log("🔔 ChatSection: Setting up WebSocket subscription for user:", user.id);
    
    websocketService.subscribe(destination, (message) => {
      console.log("📨 ChatSection: WebSocket message received:", message);
      
      try {
        // Dispatch to Redux store for real-time messages
        dispatch(receiveMessage(message));
        console.log("✅ ChatSection: Real-time message dispatched to Redux store");
      } catch (error) {
        console.error("❌ ChatSection: Error dispatching message:", error);
      }
    });

    // Cleanup function
    return () => {
      console.log("🧹 ChatSection: Cleaning up WebSocket subscription");
    };
  }, [dispatch, user?.id]);

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

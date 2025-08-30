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
    const destination = `/user/${user.id}/queue/notifications`;
    websocketService.subscribe(destination, (message) => {
      console.log("message: ", message);
      
      dispatch(receiveMessage(message));
    });
  }, [dispatch, user.id, channels]);

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

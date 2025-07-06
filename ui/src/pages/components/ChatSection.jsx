import React from "react";
import ChatHeader from "@/components/chat/ChatHeader";
import ChatMessages from "@/components/chat/ChatMessages";
import ChatInput from "@/components/chat/ChatInput";
import AddMemberModal from "@/components/modals/AddMemberModal";
import { useSelector } from "react-redux";

const ChatSection = () => {
  const currentChannelId = useSelector(
    (state) => state.channel.currentChannelId
  );
  console.log("currentChannelId: ", currentChannelId);

  return (
    <div className="flex-1 flex flex-col bg-white relative">
      {/* Chat Header */}
      {currentChannelId && (
        <ChatHeader title="Channel or Friend Name" onAddMember={() => {}} />
      )}

      {/* Chat Messages */}
      {currentChannelId && <ChatMessages />}

      {/* Chat Input */}
      {currentChannelId && <ChatInput />}

      {/* Add Member Modal */}
      <AddMemberModal open={false} onClose={() => {}} channelId={null} />
    </div>
  );
};

export default ChatSection;

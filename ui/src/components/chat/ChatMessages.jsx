import React, { useEffect, useRef } from "react";
import { useSelector } from "react-redux";
import UserMessage from "./messageType/UserMessage";
import NoticeMessage from "./messageType/NoticeMessage";

const ChatMessages = () => {
  const user = useSelector((state) => state.auth.user);
  const { channels, currentChannelId } = useSelector((state) => state.channel);
  const currentChannel = channels.find((x) => x.id === currentChannelId);
  const messagesOfCurrentChannel = currentChannel
    ? currentChannel.messages
    : [];

  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (messagesEndRef.current) {
      requestAnimationFrame(() => {
        messagesEndRef.current.scrollIntoView();
      });
    }
  }, [messagesOfCurrentChannel]);

  return (
    <div className="flex-1 p-4 overflow-y-auto bg-gray-50">
      <div className="flex flex-col gap-2">
        {messagesOfCurrentChannel.map((message) =>
          message.type === "CHAT" ? (
            <UserMessage
              key={message.key.messageId}
              content={message.content}
              isCurrentUser={user.id === message.userId}
            />
          ) : (
            <NoticeMessage
              key={message.key.messageId}
              content={message.content}
            />
          )
        )}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};

export default ChatMessages;

import React, { useEffect, useRef } from "react";
import { useSelector } from "react-redux";
import UserMessage from "./messageType/UserMessage";
import NoticeMessage from "./messageType/NoticeMessage";
import EmojiMessage from "./messageType/EmojiMessage";

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

  const renderMessage = (message) => {
    const isCurrentUser = user.id === message.userId;

    switch (message.type) {
      case "CHAT":
        return (
          <UserMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
          />
        );
      case "EMOJI":
        return (
          <EmojiMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
          />
        );
      case "NOTICE":
        return (
          <NoticeMessage
            key={message.key.messageId}
            content={message.content}
          />
        );
      default:
        return (
          <UserMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
          />
        );
    }
  };

  return (
    <div className="flex-1 p-4 overflow-y-auto bg-gray-50">
      <div className="flex flex-col gap-2">
        {messagesOfCurrentChannel.map((message) => renderMessage(message))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};

export default ChatMessages;

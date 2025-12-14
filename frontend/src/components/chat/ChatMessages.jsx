import React, { useEffect, useRef, useCallback } from "react";
import { useSelector } from "react-redux";
import UserMessage from "./messageType/UserMessage";
import NoticeMessage from "./messageType/NoticeMessage";
import EmojiMessage from "./messageType/EmojiMessage";
import { useHybridMessageStrategy } from "@/hooks/useHybridMessageStrategy";

const ChatMessages = () => {
  const user = useSelector((state) => state.auth.user);
  const { currentChannelId } = useSelector((state) => state.channel);

  const {
    messages: messagesOfCurrentChannel,
    isLoading,
    loadMoreMessages
  } = useHybridMessageStrategy(currentChannelId);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const prevMessagesLengthRef = useRef(messagesOfCurrentChannel.length);
  const prevLastMessageIdRef = useRef(null);
  const isFirstLoadRef = useRef(true);

  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container || !messagesEndRef.current) return;

    const currentLength = messagesOfCurrentChannel.length;
    const prevLength = prevMessagesLengthRef.current;

    const lastMessage = messagesOfCurrentChannel[currentLength - 1];
    const lastMessageId = lastMessage?.key?.messageId || lastMessage?.id;

    if (isFirstLoadRef.current && currentLength > 0) {
      messagesEndRef.current.scrollIntoView({ behavior: "auto" });
      isFirstLoadRef.current = false;
      prevLastMessageIdRef.current = lastMessageId;
    }
    else if (currentLength > prevLength) {
      const isMessageAddedAtBottom = lastMessageId !== prevLastMessageIdRef.current;

      if (isMessageAddedAtBottom) {
        const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 150;
        const isOwnMessage = lastMessage?.userId === user?.data?.id;

        if (isNearBottom || isOwnMessage) {
          messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }
    }

    prevMessagesLengthRef.current = currentLength;
    prevLastMessageIdRef.current = lastMessageId;
  }, [messagesOfCurrentChannel, user?.data?.id]);

  const handleScroll = useCallback((e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;

    if (scrollTop === 0 && !isLoading) {
      loadMoreMessages();
    }
  }, [isLoading, loadMoreMessages]);

  const renderMessage = (message, isLastInSequence, isFirstInSequence) => {
    const isCurrentUser = user?.data?.id === message.userId;

    const commonProps = {
      messageId: message.key.messageId,
      content: message.content,
      isCurrentUser,
      userId: message.userId,
      senderName: message.senderName,
      senderAvatar: message.senderAvatar,
      timestamp: message.timestamp || message.key.timestamp,
      isLastMessage: isLastInSequence,
      showName: isFirstInSequence,
      showAvatar: isLastInSequence,
    };

    switch (message.type) {
      case "CHAT":
      case "IMAGE":
      case "VIDEO":
      case "FILE":
      case "DELETED":
        return <UserMessage key={message.key.messageId} {...commonProps} type={message.type} status={message.status} />;
      case "EMOJI":
        return (
          <EmojiMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            userId={message.userId}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
            timestamp={message.timestamp || message.key.timestamp}
          />
        );
      case "NOTICE":
        return (
          <NoticeMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            userId={message.userId}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
            type={message.type}
            timestamp={message.timestamp || message.key.timestamp}
          />
        );
      default:
        return (
          <UserMessage
            key={message.key.messageId}
            {...commonProps}
            status={message.status}
          />
        );
    }
  };

  return (
    <div
      ref={messagesContainerRef}
      className="flex-1 p-4 overflow-y-auto bg-gray-50"
      onScroll={handleScroll}
    >
      {isLoading && (
        <div className="flex justify-center py-2">
          <div className="text-sm text-gray-500">
            {messagesOfCurrentChannel.length === 0
              ? "Loading messages..."
              : "Loading more messages..."
            }
          </div>
        </div>
      )}

      {!isLoading && messagesOfCurrentChannel.length === 0 && (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center text-gray-500">
            <div className="text-lg mb-2">💬</div>
            <div>No messages yet</div>
            <div className="text-sm">Start the conversation!</div>
          </div>
        </div>
      )}

      <div className="flex flex-col gap-1 px-2">
        {messagesOfCurrentChannel.map((message, index) => {
          const isFirstInSequence = index === 0 || messagesOfCurrentChannel[index - 1].userId !== message.userId;
          const isLastInSequence = index === messagesOfCurrentChannel.length - 1 || messagesOfCurrentChannel[index + 1].userId !== message.userId;

          return renderMessage(message, isLastInSequence, isFirstInSequence);
        })}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};

export default ChatMessages;

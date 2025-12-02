import React, { useEffect, useRef, useCallback } from "react";
import { useSelector } from "react-redux";
import UserMessage from "./messageType/UserMessage";
import NoticeMessage from "./messageType/NoticeMessage";
import EmojiMessage from "./messageType/EmojiMessage";
import { useHybridMessageStrategy } from "@/hooks/useHybridMessageStrategy";

const ChatMessages = () => {
  const user = useSelector((state) => state.auth.user);
  const { currentChannelId } = useSelector((state) => state.channel);

  // Use hybrid message strategy (WebSocket + Historical)
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

  // Handle scroll positioning
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container || !messagesEndRef.current) return;

    const currentLength = messagesOfCurrentChannel.length;
    const prevLength = prevMessagesLengthRef.current;

    const lastMessage = messagesOfCurrentChannel[currentLength - 1];
    const lastMessageId = lastMessage?.key?.messageId || lastMessage?.id;

    // 1. Initial Load: Scroll to bottom
    if (isFirstLoadRef.current && currentLength > 0) {
      messagesEndRef.current.scrollIntoView({ behavior: "auto" });
      isFirstLoadRef.current = false;
      prevLastMessageIdRef.current = lastMessageId;
    }
    // 2. Updates
    else if (currentLength > prevLength) {
      // Check if the last message ID has changed
      const isMessageAddedAtBottom = lastMessageId !== prevLastMessageIdRef.current;

      if (isMessageAddedAtBottom) {
        // Scroll to bottom if user is near bottom OR it's their own message
        const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 150;
        const isOwnMessage = lastMessage?.userId === user?.data?.id;

        if (isNearBottom || isOwnMessage) {
          messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }
      // If message added at top (history), do nothing (browser maintains scroll position relative to content usually, or we might need to adjust if it jumps)
    }

    prevMessagesLengthRef.current = currentLength;
    prevLastMessageIdRef.current = lastMessageId;
  }, [messagesOfCurrentChannel, user?.data?.id]);

  // Infinite scroll handler
  const handleScroll = useCallback((e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;

    // Load more messages when user scrolls to top
    if (scrollTop === 0 && !isLoading) {
      console.log("📜 User scrolled to top, loading more messages...");
      loadMoreMessages();
    }
  }, [isLoading, loadMoreMessages]);

  const renderMessage = (message, isLastMessage) => {
    const isCurrentUser = user?.data?.id === message.userId;

    switch (message.type) {
      case "CHAT":
      case "IMAGE":
      case "VIDEO":
      case "FILE":
        return (
          <UserMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            userId={message.userId}
            senderName={message.senderName}
            senderAvatar={message.senderAvatar}
            type={message.type}
            status={message.status}
            timestamp={message.timestamp || message.key.timestamp}
            isLastMessage={isLastMessage}
          />
        );
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
          />
        );
      default:
        return (
          <UserMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            userId={message.userId}
            status={message.status}
            timestamp={message.timestamp || message.key.timestamp}
            isLastMessage={isLastMessage}
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
      {/* Loading indicator at top */}
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

      {/* Empty state */}
      {!isLoading && messagesOfCurrentChannel.length === 0 && (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center text-gray-500">
            <div className="text-lg mb-2">💬</div>
            <div>No messages yet</div>
            <div className="text-sm">Start the conversation!</div>
          </div>
        </div>
      )}

      <div className="flex flex-col gap-2">
        {messagesOfCurrentChannel.map((message, index) => renderMessage(message, index === messagesOfCurrentChannel.length - 1))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};

export default ChatMessages;

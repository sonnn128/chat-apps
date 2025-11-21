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

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (messagesEndRef.current) {
      requestAnimationFrame(() => {
        messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
      });
    }
  }, [messagesOfCurrentChannel]);

  // Infinite scroll handler
  const handleScroll = useCallback((e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;

    // Load more messages when user scrolls to top
    if (scrollTop === 0 && !isLoading) {
      console.log("📜 User scrolled to top, loading more messages...");
      loadMoreMessages();
    }
  }, [isLoading, loadMoreMessages]);

  const renderMessage = (message) => {
    // Debug: Log message structure and user info
    console.log("🔍 ChatMessages: Rendering message:", message);
    console.log("🔍 ChatMessages: Message keys:", Object.keys(message || {}));
    console.log("🔍 ChatMessages: Message type:", message.type);
    console.log("🔍 ChatMessages: Message userId:", message.userId);
    console.log("🔍 ChatMessages: User data:", user);
    console.log("🔍 ChatMessages: User data.id:", user?.data?.id);

    const isCurrentUser = user?.data?.id === message.userId;
    console.log("🔍 ChatMessages: isCurrentUser:", isCurrentUser);

    switch (message.type) {
      case "CHAT":
        return (
          <UserMessage
            key={message.key.messageId}
            content={message.content}
            isCurrentUser={isCurrentUser}
            userId={message.userId}
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
            userId={message.userId}
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
            userId={message.userId}
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
        {messagesOfCurrentChannel.map((message) => renderMessage(message))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};

export default ChatMessages;

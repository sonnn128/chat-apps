import React, { useRef, useEffect } from "react";
import { Avatar } from "antd";
import { useSelector } from "react-redux";

const ChatMessages = ({ getFullName }) => {
  const messagesEndRef = useRef(null);
  const { messagesOfCurrentChannel, currentChannel } = useSelector((state) => state.channel);
  const currentUserId = useSelector((state) => state.auth.user.id);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messagesOfCurrentChannel]);

  const NoticeMessage = ({ message }) => (
    <div className="flex justify-center my-2">
      <div className="bg-gray-200 text-gray-700 text-xs px-3 py-1 rounded-full">
        {message.content}
      </div>
    </div>
  );

  const UserMessage = ({ message, isCurrentUser }) => (
    <div className={`flex items-start ${isCurrentUser ? "justify-end" : ""}`}>
      {!isCurrentUser && (
        <Avatar
          size={32}
          style={{ marginRight: 8 }}
        >
          {message.user?.firstname?.charAt(0).toUpperCase() || "U"}
        </Avatar>
      )}
      <div
        className={`${
          isCurrentUser
            ? "bg-blue-500 text-white"
            : "bg-gray-200 text-gray-900"
        } p-2 rounded-lg max-w-xs`}
      >
        <p className="text-sm">{message.content}</p>
      </div>
    </div>
  );

  return (
    <div className="flex-1 p-4 overflow-y-auto bg-gray-50">
      {currentChannel ? (
        <div className="flex flex-col gap-2">
          {messagesOfCurrentChannel && messagesOfCurrentChannel.length > 0 ? (
            messagesOfCurrentChannel.map((message) => (
              <React.Fragment key={message.timestamp}>
                {message.type === "NOTICE" ? (
                  <NoticeMessage message={message} />
                ) : (
                  <UserMessage
                    message={message}
                    isCurrentUser={message.userId === currentUserId}
                  />
                )}
              </React.Fragment>
            ))
          ) : (
            <p className="text-gray-500 text-center">No messages yet</p>
          )}
        </div>
      ) : (
        <p className="text-gray-500 text-center">Select a channel to view messages</p>
      )}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default ChatMessages;

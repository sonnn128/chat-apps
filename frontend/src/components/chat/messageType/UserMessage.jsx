import React, { useState } from "react";
import { Button, Tooltip } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined, FileOutlined, DownloadOutlined } from "@ant-design/icons";
import Avatar from "antd/es/avatar/Avatar";
import ReactionPicker from "../ReactionPicker";
import PropTypes from "prop-types";
import { useUserInfo } from "@/hooks/useUserInfo";

const UserMessage = ({ content, isCurrentUser, userId, senderName: propSenderName, senderAvatar: propSenderAvatar, type = "CHAT", status = "sent" }) => {
  const [showReactionPicker, setShowReactionPicker] = useState(false);
  const [reactionPickerPosition, setReactionPickerPosition] = useState({ x: 0, y: 0 });

  // Get real-time user info - but only if props not provided
  const { userInfo, loading } = useUserInfo(userId && !propSenderName ? userId : null);
  const senderName = propSenderName || (userInfo ? `${userInfo.firstname} ${userInfo.lastname}` : 'Loading...');
  const senderAvatar = propSenderAvatar || userInfo?.avatarUrl || null;

  const handleReactionClick = (event) => {
    const rect = event.currentTarget.getBoundingClientRect();
    setReactionPickerPosition({
      x: rect.left,
      y: rect.top - 320, // Position above the button
    });
    setShowReactionPicker(true);
  };

  const handleReactionSelect = (emoji) => {
    console.log("Reaction selected:", emoji);
    // Reaction functionality will be implemented in future updates
    setShowReactionPicker(false);
  };

  const renderContent = () => {
    switch (type) {
      case "IMAGE":
        return (
          <img
            src={content}
            alt="Sent image"
            className="max-w-xs rounded-lg cursor-pointer"
            onClick={() => window.open(content, "_blank")}
          />
        );
      case "VIDEO":
        return (
          <video
            src={content}
            controls
            className="max-w-xs rounded-lg"
          />
        );
      case "FILE":
        try {
          const fileData = JSON.parse(content);
          return (
            <div className="flex items-center gap-3 p-2 min-w-[200px]">
              <div className="bg-white/20 p-2 rounded-lg">
                <FileOutlined style={{ fontSize: '24px' }} />
              </div>
              <div className="flex-1 overflow-hidden">
                <div className="truncate font-medium">{fileData.name}</div>
                <div className="text-xs opacity-80">{(fileData.size / 1024).toFixed(1)} KB</div>
              </div>
              <a
                href={fileData.url}
                download
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 hover:bg-white/10 rounded-full transition-colors"
              >
                <DownloadOutlined />
              </a>
            </div>
          );
        } catch (e) {
          return <span>Invalid file data</span>;
        }
      default:
        return <p className="text-sm mb-0">{content}</p>;
    }
  };

  return (
    <div className={`flex items-start ${isCurrentUser ? "justify-end" : ""}`}>
      {!isCurrentUser && (
        <Avatar
          size={32}
          style={{ marginRight: 8 }}
          src={senderAvatar}
        >
          {senderName[0] || "U"}
        </Avatar>
      )}
      <div className={isCurrentUser ? "flex flex-col items-end" : ""}>
        {!isCurrentUser && (
          <div className="text-xs font-semibold text-gray-700 mb-1" style={{ marginLeft: 4 }}>
            {senderName}
          </div>
        )}
        <div className={`flex items-end gap-2 group ${isCurrentUser ? "flex-row-reverse" : ""}`}>
          <div
            className={`${isCurrentUser
                ? "bg-blue-500 text-white"
                : "bg-[#8e5cff] text-white"
              } p-2 rounded-2xl max-w-xs user-message`}
            style={{
              borderTopLeftRadius: isCurrentUser ? 16 : 4,
              borderTopRightRadius: isCurrentUser ? 4 : 16,
              marginLeft: !isCurrentUser ? 4 : 0,
              marginRight: 0,
              opacity: status === "pending" ? 0.7 : 1,
            }}
          >
            {renderContent()}
          </div>
          {status === "failed" && (
            <div className="text-red-500 text-xs mt-1 mr-1">Failed to send</div>
          )}

          <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex gap-1 items-center">
            <Tooltip title="More options">
              <Button type="text" shape="circle" size="small" icon={<MoreOutlined />} />
            </Tooltip>
            <Tooltip title="Reply">
              <Button type="text" shape="circle" size="small" icon={<RollbackOutlined />} />
            </Tooltip>
            <Tooltip title="Add reaction">
              <Button
                type="text"
                shape="circle"
                size="small"
                icon={<SmileOutlined />}
                onClick={handleReactionClick}
              />
            </Tooltip>
          </div>
        </div>
      </div>

      <ReactionPicker
        isVisible={showReactionPicker}
        onClose={() => setShowReactionPicker(false)}
        onReactionSelect={handleReactionSelect}
        position={reactionPickerPosition}
      />
    </div>
  );
};

UserMessage.propTypes = {
  content: PropTypes.string.isRequired,
  isCurrentUser: PropTypes.bool.isRequired,
  userId: PropTypes.string.isRequired,
  senderName: PropTypes.string,
  senderAvatar: PropTypes.string,
  type: PropTypes.string,
  status: PropTypes.string,
};

export default UserMessage;
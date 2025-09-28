import React, { useState } from "react";
import { Button, Tooltip } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined } from "@ant-design/icons";
import Avatar from "antd/es/avatar/Avatar";
import ReactionPicker from "../ReactionPicker";
import PropTypes from "prop-types";
import { useUserInfo } from "@/hooks/useUserInfo";

const UserMessage = ({ content, isCurrentUser, userId }) => {
  const [showReactionPicker, setShowReactionPicker] = useState(false);
  const [reactionPickerPosition, setReactionPickerPosition] = useState({ x: 0, y: 0 });
  
  // Get real-time user info
  const { userInfo, loading } = useUserInfo(userId);
  const senderName = userInfo ? `${userInfo.firstname} ${userInfo.lastname}` : 'Loading...';
  const senderAvatar = userInfo?.avatarUrl || null;

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
          <div className="text-xs font-semibold text-white mb-1" style={{ marginLeft: 4 }}>
            {senderName}
          </div>
        )}
        <div className={`flex items-end gap-2 group ${isCurrentUser ? "flex-row-reverse" : ""}`}>
          <div
            className={`${
              isCurrentUser
                ? "bg-blue-500 text-white"
                : "bg-[#8e5cff] text-white"
            } p-2 rounded-2xl max-w-xs user-message`}
            style={{
              borderTopLeftRadius: isCurrentUser ? 16 : 4,
              borderTopRightRadius: isCurrentUser ? 4 : 16,
              marginLeft: !isCurrentUser ? 4 : 0,
              marginRight: 0,
            }}
          >
            <p className="text-sm mb-0">{content}</p>
          </div>

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
};

export default UserMessage;
import React from "react";
import { Button, Tooltip, Avatar } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined } from "@ant-design/icons";
import PropTypes from "prop-types";

function EmojiMessage({ content, isCurrentUser, senderName = "User", senderAvatar }) {
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
            className="p-3 rounded-2xl flex items-center justify-center shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer transform hover:scale-105"
            style={{
              borderTopLeftRadius: isCurrentUser ? 16 : 4,
              borderTopRightRadius: isCurrentUser ? 4 : 16,
              marginLeft: !isCurrentUser ? 4 : 0,
            }}
          >
            <span className="text-2xl" aria-label="emoji">
              {content}
            </span>
          </div>

          <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex gap-1 items-center">
            <Tooltip title="More options">
              <Button type="text" shape="circle" size="small" icon={<MoreOutlined />} />
            </Tooltip>
            <Tooltip title="Reply">
              <Button type="text" shape="circle" size="small" icon={<RollbackOutlined />} />
            </Tooltip>
            <Tooltip title="Choose an emoji">
              <Button type="text" shape="circle" size="small" icon={<SmileOutlined />} />
            </Tooltip>
          </div>
        </div>
      </div>
    </div>
  );
}

EmojiMessage.propTypes = {
  content: PropTypes.string.isRequired,
  isCurrentUser: PropTypes.bool.isRequired,
  senderName: PropTypes.string,
  senderAvatar: PropTypes.string,
};

export default EmojiMessage;

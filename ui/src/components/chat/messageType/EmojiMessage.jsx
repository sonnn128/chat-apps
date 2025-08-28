import React from "react";
import { Button, Tooltip } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined } from "@ant-design/icons";

function EmojiMessage({ content, isCurrentUser, senderName = "User", senderAvatar }) {
  return (
    <div className={`flex items-start gap-2 ${isCurrentUser ? "justify-end" : ""}`}>
      {!isCurrentUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-pink-400 flex items-center justify-center text-white font-semibold text-sm shadow-sm">
            {senderName[0] || "U"}
          </div>
        </div>
      )}
      <div className="flex flex-col max-w-xs">
        {!isCurrentUser && (
          <div className="text-xs font-medium text-gray-500 mb-1 ml-1">{senderName}</div>
        )}
        <div className="flex items-end gap-2 group">
          <div
            className={`
              emoji-message-container
              ${isCurrentUser ? "emoji-current-user" : "emoji-other-user"}
              flex items-center justify-center shadow-sm hover:shadow-md transition-all duration-200
              cursor-pointer transform hover:scale-105
            `}
          >
            <span className="text-3xl filter drop-shadow-sm" role="img" aria-label="emoji" style={{ textShadow: "0 1px 2px rgba(0,0,0,0.1)" }}>
              {content}
            </span>
          </div>

          <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex gap-1 items-center">
            <Tooltip title="More options" overlayClassName="custom-tooltip">
              <Button type="text" shape="circle" size="small" icon={<MoreOutlined />} />
            </Tooltip>
            <Tooltip title="Reply" overlayClassName="custom-tooltip">
              <Button type="text" shape="circle" size="small" icon={<RollbackOutlined />} />
            </Tooltip>
            <Tooltip title="Choose an emoji" overlayClassName="custom-tooltip">
              <Button type="text" shape="circle" size="small" icon={<SmileOutlined />} />
            </Tooltip>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EmojiMessage;

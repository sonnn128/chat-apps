import React, { useState } from "react";
import { Button, Tooltip, Dropdown, Modal, Radio } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined, FileOutlined, DownloadOutlined, DeleteOutlined, ShareAltOutlined, PushpinOutlined, FlagOutlined } from "@ant-design/icons";
import Avatar from "antd/es/avatar/Avatar";
import ReactionPicker from "../ReactionPicker";
import PropTypes from "prop-types";
import { useUserInfo } from "@/hooks/useUserInfo";
import { DEFAULT_AVATAR } from "@/utils/constants";
import chatService from "@/services/chatService";
import { useSelector } from "react-redux";

const UserMessage = ({ messageId, content, isCurrentUser, userId, senderName: propSenderName, senderAvatar: propSenderAvatar, type = "CHAT", status = "sent", timestamp, isLastMessage }) => {
  // DEBUG LOG
  if (content === "") {
    console.log("UserMessage DEBUG:", { messageId, type, content, isCurrentUser });
  }

  const [showReactionPicker, setShowReactionPicker] = useState(false);
  const [reactionPickerPosition, setReactionPickerPosition] = useState({ x: 0, y: 0 });
  const { currentChannelId } = useSelector((state) => state.channel);

  // Unsend Modal state
  const [isUnsendModalOpen, setIsUnsendModalOpen] = useState(false);
  const [unsendType, setUnsendType] = useState('everyone');

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
    setShowReactionPicker(false);
  };

  const handleMenuClick = (e) => {
    if (e.key === 'unsend') {
      setIsUnsendModalOpen(true);
    }
    // Handle other actions here if needed
  };

  const handleUnsendCancel = () => {
    setIsUnsendModalOpen(false);
    setUnsendType('everyone'); // Reset to default
  };

  const handleUnsendRemove = async () => {
    // console.log("Unsend action:", unsendType);
    if (unsendType === 'everyone') {
      try {
        await chatService.deleteMessage(currentChannelId, messageId);
        // Optimistic update could be done here, or wait for socket event
      } catch (error) {
        console.error("Failed to unsend message", error);
        // Show error toast?
      }
    } else {
      // "For you" logic (local delete) - TO BE IMPLEMENTED
      console.log("Unsend for you not implemented yet");
    }
    setIsUnsendModalOpen(false);
  };

  const menuItems = [
    {
      key: 'unsend',
      label: 'Unsend',
      icon: <DeleteOutlined />,
      danger: true,
    },
    {
      key: 'forward',
      label: 'Forward',
      icon: <ShareAltOutlined />,
    },
    {
      key: 'pin',
      label: 'Pin',
      icon: <PushpinOutlined />,
    },
    {
      key: 'report',
      label: 'Report',
      icon: <FlagOutlined />,
      danger: true,
    },
  ];

  const renderContent = () => {
    switch (type) {
      case "DELETED":
        const displayName = isCurrentUser ? "You" : (senderName ? senderName.split(' ').pop() : 'User');
        return (
          <p className="text-sm mb-0 italic text-gray-500 bg-transparent">
            {isCurrentUser ? "You deleted a message" : `${displayName} deleted a message`}
          </p>
        );
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
            className={`${["IMAGE", "VIDEO", "DELETED"].includes(type)
              ? ""
              : isCurrentUser
                ? "bg-[#066CF6] text-white p-2"
                : "bg-[#F0F0F0] text-black p-2"
              } rounded-2xl max-w-xs user-message`}
            style={{
              borderTopLeftRadius: isCurrentUser ? 16 : 4,
              borderTopRightRadius: isCurrentUser ? 4 : 16,
              marginLeft: !isCurrentUser ? 4 : 0,
              marginRight: 0,
              opacity: status === "pending" ? 0.7 : 1,
              backgroundColor: type === "DELETED" ? "transparent" : undefined,
              border: type === "DELETED" ? "1px solid #ccc" : undefined,
              padding: type === "DELETED" ? "8px 12px" : undefined,
            }}
          >
            {renderContent()}
          </div>
          {status === "failed" && (
            <div className="text-red-500 text-xs mt-1 mr-1">Failed to send</div>
          )}

          {type !== "DELETED" && (
            <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex gap-1 items-center">
              <Dropdown menu={{ items: menuItems, onClick: handleMenuClick }} trigger={['click']} placement="bottomRight">
                <Tooltip title="More options">
                  <Button type="text" shape="circle" size="small" icon={<MoreOutlined />} />
                </Tooltip>
              </Dropdown>
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
          )}
        </div>
        {/* Status Text */}
        {isCurrentUser && (status === "pending" || (status === "sent" && isLastMessage)) && (
          <div className="text-xs text-gray-400 mt-1 mr-1">
            {status === "pending" ? "Sending..." : "Sent"}
          </div>
        )}
      </div>

      <ReactionPicker
        isVisible={showReactionPicker}
        onClose={() => setShowReactionPicker(false)}
        onReactionSelect={handleReactionSelect}
        position={reactionPickerPosition}
      />

      <Modal
        title="Who do you want to unsend this message for?"
        open={isUnsendModalOpen}
        onCancel={handleUnsendCancel}
        footer={[
          <Button key="cancel" onClick={handleUnsendCancel} style={{ width: '45%' }}>
            Cancel
          </Button>,
          <Button key="remove" type="primary" onClick={handleUnsendRemove} style={{ width: '45%', backgroundColor: '#066CF6' }}>
            Remove
          </Button>,
        ]}
        centered
        width={500}
      >
        <div className="mt-4 mb-2">
          <Radio.Group onChange={(e) => setUnsendType(e.target.value)} value={unsendType}>
            <div className="flex flex-col gap-4">
              <Radio value="everyone" className="items-start">
                <div className="flex flex-col gap-1 ml-2">
                  <span className="font-semibold text-base">Unsend for everyone</span>
                  <span className="text-gray-500 text-sm whitespace-normal">
                    This message will be unsent for everyone in the chat. Others may have already seen or forwarded it. Unsent messages can still be included in reports.
                  </span>
                </div>
              </Radio>
              <Radio value="you" className="items-start">
                <div className="flex flex-col gap-1 ml-2">
                  <span className="font-semibold text-base">Unsend for you</span>
                  <span className="text-gray-500 text-sm whitespace-normal">
                    This will remove the message from your devices. Other chat members will still be able to see it.
                  </span>
                </div>
              </Radio>
            </div>
          </Radio.Group>
        </div>
      </Modal>
    </div>
  );
};

UserMessage.propTypes = {
  messageId: PropTypes.string,
  content: PropTypes.string.isRequired,
  isCurrentUser: PropTypes.bool.isRequired,
  userId: PropTypes.string.isRequired,
  senderName: PropTypes.string,
  senderAvatar: PropTypes.string,
  type: PropTypes.string,
  status: PropTypes.string,
  timestamp: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  isLastMessage: PropTypes.bool,
};

export default UserMessage;
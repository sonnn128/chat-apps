import React, { useState } from "react";
import { Button, Tooltip, Dropdown, Modal, Radio, Image } from "antd";
import { MoreOutlined, RollbackOutlined, SmileOutlined, FileOutlined, DownloadOutlined, DeleteOutlined, ShareAltOutlined, PushpinOutlined, FlagOutlined } from "@ant-design/icons";
import Avatar from "antd/es/avatar/Avatar";
import ReactionPicker from "../ReactionPicker";
import LinkPreview from "../LinkPreview";
import PropTypes from "prop-types";
import { useUserInfo } from "@/hooks/useUserInfo";
import { DEFAULT_AVATAR } from "@/utils/constants";
import chatService from "@/services/chatService";
import { useSelector } from "react-redux";
import { getChannelTheme, getThemeStyle } from "@/utils/channelThemes";

const UserMessage = ({
  messageId,
  content,
  isCurrentUser,
  userId,
  senderName: propSenderName,
  senderAvatar: propSenderAvatar,
  type = "CHAT",
  status = "sent",
  timestamp,
  isLastMessage,
  showName = true,
  showAvatar = true
}) => {


  const [showReactionPicker, setShowReactionPicker] = useState(false);
  const [reactionPickerPosition, setReactionPickerPosition] = useState({ x: 0, y: 0 });
  const { currentChannelId, currentChannel } = useSelector((state) => state.channel);

  // Get channel theme
  const channelTheme = getChannelTheme(currentChannel);
  const themeStyle = getThemeStyle(channelTheme);

  // Debug log for theme
  // if (isCurrentUser && type === "CHAT") {
  //   console.log(`[Msg ${messageId}] Theme Applied:`, { themeId: channelTheme.id, style: themeStyle });
  // }

  // Debug log for theme
  // if (isCurrentUser && type === "CHAT") {
  //   console.log(`[Msg ${messageId}] Theme Applied:`, themeStyle);
  // }

  // Unsend Modal state
  const [isUnsendModalOpen, setIsUnsendModalOpen] = useState(false);
  const [unsendType, setUnsendType] = useState('everyone');

  // Logic resolve sender name
  // 1. Try to find in channel participants (First priority - most up to date from channel context)
  const participant = currentChannel?.participants?.find(p => (p.userId || p.id) === userId);

  // 2. Check validity of propSenderName. Some backends return "Unknown User" as a string.
  const isPropNameValid = propSenderName && propSenderName !== "Unknown User" && propSenderName !== "Unknown";

  // 3. Determine if we need to fetch user info (if not found in participants and prop invalid)
  const skipFetch = !!participant || isPropNameValid;
  const { userInfo, loading } = useUserInfo(userId && !skipFetch ? userId : null);

  // 4. Final resolve
  let senderName = "Unknown User";

  if (isPropNameValid) {
    senderName = propSenderName;
  } else if (participant) {
    senderName = participant.name || `${participant.firstname || ''} ${participant.lastname || ''}`.trim();
  } else if (userInfo) {
    senderName = `${userInfo.firstname} ${userInfo.lastname}`;
  } else if (loading) {
    senderName = "Loading...";
  }

  // Avatar resolution
  let senderAvatar = propSenderAvatar;
  if (!senderAvatar && participant) senderAvatar = participant.avatar || participant.avatarUrl;
  if (!senderAvatar && userInfo) senderAvatar = userInfo.avatarUrl;

  const handleReactionClick = (event) => {
    const rect = event.currentTarget.getBoundingClientRect();
    setReactionPickerPosition({
      x: rect.left,
      y: rect.top - 320,
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
  };

  const handleUnsendCancel = () => {
    setIsUnsendModalOpen(false);
    setUnsendType('everyone');
  };

  const handleUnsendRemove = async () => {
    // console.log("Unsend action:", unsendType);
    if (unsendType === 'everyone') {
      try {
        await chatService.deleteMessage(currentChannelId, messageId);
      } catch (error) {
        console.error("Failed to unsend message", error);
      }
    } else {
      // "For you" logic (local delete) - TO BE IMPLEMENTED
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
          <Image
            src={content}
            alt="Sent image"
            className="max-w-xs rounded-lg cursor-pointer"
            preview={{
              mask: null
            }}
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
        // Check if content contains a URL for CHAT messages
        if (type === "CHAT" && content) {
          const urlRegex = /(https?:\/\/[^\s]+)/g;
          const urls = content.match(urlRegex);

          if (urls && urls.length > 0) {
            // If content is just a URL (with or without whitespace), show preview only
            const trimmedContent = content.trim();
            if (urls[0] === trimmedContent) {
              return <LinkPreview url={urls[0]} isCurrentUser={isCurrentUser} />;
            }

            // If content has text + URL, show text + preview
            return (
              <div className="space-y-2 max-w-sm">
                <p className="text-sm mb-0 leading-normal">{content}</p>
                <LinkPreview url={urls[0]} isCurrentUser={isCurrentUser} />
              </div>
            );
          }
        }
        return <p className="text-sm mb-0 leading-normal break-words">{content}</p>;
    }
  };

  return (
    <div className={`flex items-start ${isCurrentUser ? "justify-end" : ""}`}>
      {!isCurrentUser && (
        <div style={{ marginRight: 8, width: 32, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {showAvatar ? (
            <Avatar
              size={32}
              style={{ marginTop: showName ? 22 : 0 }}
              src={senderAvatar}
            >
              {senderName[0] || "U"}
            </Avatar>
          ) : (
            <div style={{ width: 32 }} />
          )}
        </div>
      )}
      <div className={isCurrentUser ? "flex flex-col items-end" : ""}>
        {!isCurrentUser && showName && (
          <div className="text-[11px] text-gray-500 mb-1" style={{ marginLeft: 4 }}>
            {senderName}
          </div>
        )}
        <div className={`flex items-end gap-2 group ${isCurrentUser ? "flex-row-reverse" : ""}`}>
          <div
            className={`${["IMAGE", "VIDEO", "DELETED"].includes(type)
              ? ""
              : isCurrentUser && type === "CHAT" && !(content?.match(/(https?:\/\/[^\s]+)/g))
                ? "text-white px-4 py-2"
                : isCurrentUser && type === "CHAT"
                  ? "bg-transparent p-0"
                  : type === "CHAT"
                    ? "bg-[#E4E6EB] text-black px-4 py-2"
                    : "bg-[#E4E6EB] text-black p-3"
              } rounded-2xl max-w-xs user-message`}
            style={{
              borderTopLeftRadius: isCurrentUser ? 18 : (showName ? 18 : 4),
              borderTopRightRadius: isCurrentUser ? (showName ? 18 : 4) : 18,
              borderBottomLeftRadius: isCurrentUser ? 18 : (showAvatar ? 18 : 4),
              borderBottomRightRadius: isCurrentUser ? (showAvatar ? 18 : 4) : 18,
              marginLeft: !isCurrentUser ? 4 : 0,
              marginRight: 0,
              opacity: status === "pending" ? 0.7 : 1,
              backgroundColor: type === "DELETED" ? "transparent" :
                (isCurrentUser && type === "CHAT" && !(content?.match(/(https?:\/\/[^\s]+)/g)) ? undefined : undefined),
              border: type === "DELETED" ? "1px solid #ccc" : undefined,
              padding: type === "DELETED" ? "8px 12px" : undefined,
              // Apply theme style for sent messages
              ...(isCurrentUser && type === "CHAT" && !(content?.match(/(https?:\/\/[^\s]+)/g)) ? themeStyle : {}),
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
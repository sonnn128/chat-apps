import React from "react";
import { Avatar, Tooltip, Modal, Typography, Button, Space } from "antd";
import {
  UserAddOutlined,
  PhoneOutlined,
  VideoCameraOutlined,
  UsergroupAddOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import { useSelector } from "react-redux";

const ChatHeader = ({ title: propTitle }) => {
  const currentChannel = useSelector((state) => state.channel.currentChannel);
  const currentFriend = useSelector((state) => state.friendship.currentFriend);
  const user = useSelector((state) => state.auth.user?.data);

  // Determine display name: propTitle > currentChannel name > other participant name > currentFriend name > fallback
  let displayName = propTitle || null;

  if (!displayName && currentChannel) {
    displayName = currentChannel.channelName || null;

    // If channel is a 1-1 and no explicit channelName, use the other participant's name
    if (!displayName && currentChannel.participants && currentChannel.participants.length === 2) {
      const other = currentChannel.participants.find((p) => (p.userId || p.id) !== (user?.id));
      displayName = other?.name || `${other?.firstname || ""} ${other?.lastname || ""}`.trim() || null;
    }
  }

  if (!displayName && currentFriend) {
    displayName = `${currentFriend.firstname || ""} ${currentFriend.lastname || ""}`.trim();
  }

  if (!displayName) displayName = "No Name";

  // Avatar logic: prefer avatar from friend or participant, else initials
  let avatarSrc = null;
  let avatarText = displayName[0] || "A";
  if (currentFriend && currentFriend.avatar) avatarSrc = currentFriend.avatar;
  else if (currentChannel && currentChannel.participants && currentChannel.participants.length === 2) {
    const other = currentChannel.participants.find((p) => (p.userId || p.id) !== (user?.id));
    if (other) {
      avatarSrc = other.avatar || other.avatarUrl || null;
      avatarText = (other.firstname || other.name || "").charAt(0) || avatarText;
    }
  } else if (currentChannel && currentChannel.channelName) {
    avatarText = currentChannel.channelName.charAt(0) || avatarText;
  }

  return (
    <>
      <div className="p-3 border-b flex items-center justify-between bg-white">
        <div className="flex items-center">
          <Avatar size={40} src={avatarSrc} style={{ cursor: "pointer" }}>
            {!avatarSrc && avatarText}
          </Avatar>
          <div className="ml-3">
            <Typography.Text strong className="text-lg text-gray-900">
              {displayName}
            </Typography.Text>
            <p className="text-sm text-gray-500">Active now</p>
          </div>
        </div>

        <Space>
          <Tooltip title="Call">
            <Button icon={<PhoneOutlined />} />
          </Tooltip>
          <Tooltip title="Video Call">
            <Button icon={<VideoCameraOutlined />} />
          </Tooltip>
        </Space>
      </div>

      <Modal title={`Channel Members (0)`} open={false} footer={null}>
        <Typography.Text>No members found</Typography.Text>
      </Modal>
    </>
  );
};

export default ChatHeader;

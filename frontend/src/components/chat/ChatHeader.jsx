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
import { DEFAULT_AVATAR } from "@/utils/constants";

import { useCall } from "@/context/CallContext";
import { message } from "antd";

const ChatHeader = ({ title: propTitle }) => {
  const { callUser } = useCall();
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

  if (currentChannel && currentChannel.avatar) {
    avatarSrc = currentChannel.avatar;
  } else if (currentFriend && currentFriend.avatar) {
    avatarSrc = currentFriend.avatar;
  } else if (currentChannel && currentChannel.participants && currentChannel.participants.length === 2) {
    const other = currentChannel.participants.find((p) => (p.userId || p.id) !== (user?.id));
    if (other) {
      avatarSrc = other.avatar || other.avatarUrl || null;
      avatarText = (other.firstname || other.name || "").charAt(0) || avatarText;
    }
  } else if (currentChannel && currentChannel.channelName) {
    avatarText = currentChannel.channelName.charAt(0) || avatarText;
  }

  const handleCall = (video) => {
    console.log("handleCall triggered. Video:", video);
    console.log("Current Channel:", currentChannel);
    console.log("Current Friend:", currentFriend);
    console.log("User:", user);

    let targetId = null;
    let channelId = currentChannel?.id;

    if (currentFriend) {
      targetId = currentFriend.id || currentFriend.friendId;
    } else if (currentChannel) {
      // Try participants first
      if (currentChannel.participants && currentChannel.participants.length === 2) {
        const other = currentChannel.participants.find((p) => (p.userId || p.id) !== user?.id);
        targetId = other?.userId || other?.id;
      }
      // Fallback to memberIds if participants not populated
      else if (currentChannel.memberIds && currentChannel.memberIds.length === 2) {
        const otherId = currentChannel.memberIds.find(id => id !== user?.id);
        targetId = otherId;
      }
    }

    console.log("Derived targetId:", targetId);

    if (targetId) {
      callUser(targetId, channelId, video);
    } else {
      if (currentChannel && currentChannel.participants && currentChannel.participants.length > 2) {
        message.warning("Group calling is not supported yet.");
      } else {
        message.error("Cannot call: User not found or invalid channel.");
      }
      console.warn("Cannot call: No target user found or group call not supported");
    }
  };

  return (
    <>
      <div className="p-3 flex items-center justify-between bg-white">
        {/* ... (left side content same as before) */}
        <div className="flex items-center">
          <div className="relative">
            <Avatar size={40} src={avatarSrc || DEFAULT_AVATAR} style={{ cursor: "pointer" }} />
            <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
          </div>
          <div className="ml-3">
            <Typography.Text strong className="text-lg text-gray-900">
              {displayName}
            </Typography.Text>
            <p className="text-sm text-gray-500">Active now</p>
          </div>
        </div>

        <Space>
          <Tooltip title="Call">
            <Button icon={<PhoneOutlined />} onClick={() => handleCall(false)} />
          </Tooltip>
          <Tooltip title="Video Call">
            <Button icon={<VideoCameraOutlined />} onClick={() => handleCall(true)} />
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

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

const ChatHeader = () => {
  const currentChannel = useSelector((state) => state.channel.currentChannel);
  return (
    <>
      <div className="p-3 border-b flex items-center justify-between bg-white">
        <div className="flex items-center">
          <Avatar size={40} style={{ cursor: "pointer" }}>
            A
          </Avatar>
          <div className="ml-3">
            <Typography.Text strong className="text-lg text-gray-900">
              {currentChannel.channelName || "No Name"}
            </Typography.Text>
            <p className="text-sm text-gray-500">Active now</p>
          </div>
        </div>

        <Space>
          <Tooltip title="Add Friend">
            <Button icon={<UserAddOutlined />} />
          </Tooltip>
          <Tooltip title="Add Member">
            <Button icon={<UsergroupAddOutlined />} />
          </Tooltip>
          <Tooltip title="View Members">
            <Button icon={<TeamOutlined />} />
          </Tooltip>
          <Tooltip title="Call">
            <Button icon={<PhoneOutlined />} />
          </Tooltip>
          <Tooltip title="Video Call">
            <Button icon={<VideoCameraOutlined />} />
          </Tooltip>
        </Space>
      </div>

      <Modal title="Channel Members (0)" open={false} footer={null}>
        <Typography.Text>No members found</Typography.Text>
      </Modal>
    </>
  );
};

export default ChatHeader;

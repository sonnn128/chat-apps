import React, { useState } from "react";
import { Avatar, Tooltip, Modal, Typography, List, Tag, Button, Space } from "antd";
import {
  UserAddOutlined,
  PhoneOutlined,
  VideoCameraOutlined,
  CloseCircleOutlined,
  UsergroupAddOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import { useSelector } from "react-redux";

const ChatHeader = ({ onAddMember }) => {
  const currentChannel = useSelector((state) => state.channel.currentChannel);
  const currentFriend = useSelector((state) => state.friendship.currentFriend);
  const joinedChannels = useSelector((state) => state.channel.joinedChannels);

  const [openMemberModal, setOpenMemberModal] = useState(false);

  const getFullName = () => {
    return currentFriend
      ? `${currentFriend.firstname} ${currentFriend.lastname}`
      : "";
  };

  const handleViewMember = () => {
    setOpenMemberModal(true);
  };

  const handleCloseModal = () => {
    setOpenMemberModal(false);
  };

  return (
    <>
      <div className="p-3 border-b flex items-center justify-between bg-white">
        <div className="flex items-center">
          <Avatar
            size={40}
            style={{ cursor: "pointer" }}
          >
            {(getFullName() || currentChannel?.name)?.[0]}
          </Avatar>
          <div className="ml-3">
            <Typography.Text strong className="text-lg text-gray-900">
              {getFullName() || currentChannel?.name}
            </Typography.Text>
            <p className="text-sm text-gray-500">Active now</p>
          </div>
        </div>

        <Space>
          {currentFriend ? (
            false ? (
              <Tooltip title="Cancel Request">
                <Button icon={<CloseCircleOutlined />} />
              </Tooltip>
            ) : (
              <Tooltip title="Add Friend">
                <Button icon={<UserAddOutlined />} />
              </Tooltip>
            )
          ) : (
            <>
              <Tooltip title="Add Member">
                <Button icon={<UsergroupAddOutlined />} onClick={onAddMember} />
              </Tooltip>
              <Tooltip title="View Members">
                <Button icon={<TeamOutlined />} onClick={handleViewMember} />
              </Tooltip>
            </>
          )}
          <Tooltip title="Call">
            <Button icon={<PhoneOutlined />} />
          </Tooltip>
          <Tooltip title="Video Call">
            <Button icon={<VideoCameraOutlined />} />
          </Tooltip>
        </Space>
      </div>

      {/* Modal hiển thị danh sách thành viên */}
      <Modal
        title={`Channel Members (${joinedChannels.length})`}
        open={openMemberModal}
        onCancel={handleCloseModal}
        footer={null}
      >
        {joinedChannels.length > 0 ? (
          <List
            dataSource={joinedChannels}
            renderItem={(member) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<Avatar>{member.firstname[0]}</Avatar>}
                  title={`${member.firstname} ${member.lastname}`}
                  description={
                    <div className="flex flex-col gap-1">
                      <span>{member.email}</span>
                      <Tag color={member.role === "ADMIN" ? "blue" : "default"}>
                        {member.role === "ADMIN" ? "Admin" : "Member"}
                      </Tag>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Typography.Text>No members found</Typography.Text>
        )}
      </Modal>
    </>
  );
};

export default ChatHeader;

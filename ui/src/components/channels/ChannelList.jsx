import React, { useEffect, useState } from "react";
import { Avatar, List, Dropdown, Menu, Button } from "antd";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";
import {
  MoreOutlined,
  MailOutlined,
  BellOutlined,
  UserOutlined,
  PhoneOutlined,
  VideoCameraOutlined,
  StopOutlined,
  FolderOutlined,
  DeleteOutlined,
  WarningOutlined,
  RightOutlined,
  LogoutOutlined,
} from "@ant-design/icons";

import { removeChannel, setCurrentChannel } from "@/stores/slices/channelSlice";
import { removeCurrentFriend } from "@/stores/slices/friendshipSlice";
import { fetchAllMembersOfChannel, fetchDeleteChannel } from "@/stores/middlewares/channelMiddleware";

// Import các modal mới
import DeleteChannelModal from "../modals/channeloptions/DeleteChannelModal";
import LeaveGroupModal from "../modals/channeloptions/LeaveGroupModal";

const StyledListItem = styled(List.Item)`
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #f0f0f0;
  }

  &.selected {
    background-color: #e6f7ff;
  }
`;

function ChannelList() {
  const dispatch = useDispatch();
  const { channels, currentChannelId } = useSelector((state) => state.channel);

  const [openDropdownId, setOpenDropdownId] = useState(null);

  // States mới cho các modal
  const [isDeleteChannelModalVisible, setIsDeleteChannelModalVisible] =
    useState(false);
  const [selectedChannelToDelete, setSelectedChannelToDelete] = useState(null);

  const [isLeaveGroupModalVisible, setIsLeaveGroupModalVisible] =
    useState(false);
  const [selectedChannelToLeave, setSelectedChannelToLeave] = useState(null);

  useEffect(() => {
    if (currentChannelId) {
      dispatch(fetchAllMembersOfChannel(currentChannelId));
    }
  }, [currentChannelId, dispatch]);

  const onSelectChannel = (channel) => {
    dispatch(setCurrentChannel(channel));
    dispatch(removeCurrentFriend());
  };

  const handleMenuClick = (e, channel) => {
    if (e.key === "delete_chat") {
      setSelectedChannelToDelete(channel);
      setIsDeleteChannelModalVisible(true);
    } else if (e.key === "leave_group") {
      setSelectedChannelToLeave(channel);
      setIsLeaveGroupModalVisible(true);
    } else {
      console.log(`Clicked ${e.key} for channel ${channel.channelName}`);
    }
    setOpenDropdownId(null);
  };

  // Logic cho Delete Channel Modal
  const handleDeleteChannelConfirm = () => {
    if (selectedChannelToDelete) {
      console.log(`Deleting channel: ${selectedChannelToDelete.id}`);
      
      dispatch(fetchDeleteChannel(selectedChannelToDelete.id));
    }
    setIsDeleteChannelModalVisible(false);
    setSelectedChannelToDelete(null);
  };

  const handleDeleteChannelCancel = () => {
    setIsDeleteChannelModalVisible(false);
    setSelectedChannelToDelete(null);
  };

  // Logic cho Leave Group Modal
  const handleLeaveGroupConfirm = () => {
    if (selectedChannelToLeave) {
      console.log(`Leaving group: ${selectedChannelToLeave.channelName}`);
      // TODO: Dispatch action rời nhóm tại đây
      // dispatch(leaveGroup(selectedChannelToLeave.id));
    }
    setIsLeaveGroupModalVisible(false);
    setSelectedChannelToLeave(null);
  };

  const handleLeaveGroupCancel = () => {
    setIsLeaveGroupModalVisible(false);
    setSelectedChannelToLeave(null);
  };

  const renderChannelMenu = (channel) => (
    <Menu onClick={(e) => handleMenuClick(e, channel)}>
      <Menu.Item key="mark_unread" icon={<MailOutlined />}>
        Mark as unread
      </Menu.Item>
      <Menu.Item key="unmute_notifications" icon={<BellOutlined />}>
        Unmute notifications
      </Menu.Item>
      <Menu.Item key="view_profile" icon={<UserOutlined />}>
        View profile
      </Menu.Item>
      <Menu.Item key="audio_call" icon={<PhoneOutlined />}>
        Audio call
      </Menu.Item>
      <Menu.Item key="video_chat" icon={<VideoCameraOutlined />}>
        Video chat
      </Menu.Item>
      <Menu.Item key="block" icon={<StopOutlined />}>
        Block
      </Menu.Item>
      <Menu.Item key="archive_chat" icon={<FolderOutlined />}>
        Archive chat
      </Menu.Item>
      <Menu.Item key="delete_chat" icon={<DeleteOutlined />}>
        Delete chat
      </Menu.Item>
      <Menu.Item key="report" icon={<WarningOutlined />}>
        Report
      </Menu.Item>
      <Menu.Item key="leave_group" icon={<LogoutOutlined />}>
        Leave Group
      </Menu.Item>
    </Menu>
  );

  return (
    <div style={{ padding: "10px" }}>
      <List
        dataSource={channels}
        renderItem={(channel) => (
          <StyledListItem
            key={channel.id}
            className={currentChannelId === channel.id ? "selected" : ""}
          >
            <div
              onClick={() => onSelectChannel(channel)}
              style={{ flexGrow: 1, display: "flex", alignItems: "center" }}
            >
              <List.Item.Meta
                avatar={<Avatar>{channel.channelName[0]}</Avatar>}
                title={channel.channelName}
                description={`Created: ${new Date(
                  channel.createdAt
                ).toLocaleDateString()}`}
              />
            </div>

            <Dropdown
              overlay={() => renderChannelMenu(channel)}
              trigger={["click"]}
              onOpenChange={(open) =>
                setOpenDropdownId(open ? channel.id : null)
              }
            >
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  cursor: "pointer",
                  padding: "5px",
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {openDropdownId === channel.id && (
                  <RightOutlined
                    style={{
                      marginRight: "5px",
                      fontSize: "14px",
                      color: "#1890ff",
                    }}
                  />
                )}
                <MoreOutlined style={{ fontSize: "18px" }} />
              </div>
            </Dropdown>
          </StyledListItem>
        )}
      />

      {/* Sử dụng DeleteChannelModal */}
      <DeleteChannelModal
        visible={isDeleteChannelModalVisible}
        onConfirm={handleDeleteChannelConfirm}
        onCancel={handleDeleteChannelCancel}
        channelName={selectedChannelToDelete?.channelName}
      />

      {/* Sử dụng LeaveGroupModal */}
      <LeaveGroupModal
        visible={isLeaveGroupModalVisible}
        onConfirm={handleLeaveGroupConfirm}
        onCancel={handleLeaveGroupCancel}
        groupName={selectedChannelToLeave?.channelName}
      />
    </div>
  );
}

export default ChannelList;

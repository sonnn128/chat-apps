import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Input, Button, Tooltip, Badge, Space, Typography } from "antd";
import {
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
  UserAddOutlined,
  PlusOutlined,
} from "@ant-design/icons";

import ChannelList from "@/components/channels/ChannelList";
import FriendList from "@/components/friends/FriendList";
import FriendsModal from "@/components/modals/FriendsModal";
import FriendRequestsModal from "@/components/modals/FriendRequestsModal";
import FriendSuggestionsModal from "@/components/modals/FriendSuggestionsModal";
import { successToast } from "@/utils/toast";

import {
  sendFriendRequest,
  acceptFriendRequest,
} from "@/stores/middlewares/friendShipMiddleware";
import { removeCurrentChannel } from "@/stores/slices/channelSlice";
import { fetchCreateChannel } from "@/stores/middlewares/channelMiddleware";

const { Title } = Typography;

const Sidebar = () => {
  const dispatch = useDispatch();
  const { friendSuggestions, friends, pendingRequests } = useSelector(
    (state) => state.friendship
  );

  const [isAddingChannel, setIsAddingChannel] = useState(false);
  const [newChannelName, setNewChannelName] = useState("");
  const [openModal, setOpenModal] = useState({
    friends: false,
    requests: false,
    suggestions: false,
  });

  const handleAddFriend = async (userId) => {
    const res = await dispatch(sendFriendRequest(userId)).unwrap();
    if (res) successToast("Friend request sent");
  };

  const handleAcceptRequest = (requestId) => {
    dispatch(acceptFriendRequest(requestId));
  };

  const toggleModal = (type) =>
    setOpenModal((prev) => ({ ...prev, [type]: !prev[type] }));

  const onSelectUser = () => {
    dispatch(removeCurrentChannel());
  };

  const handleChannelSubmit = async () => {
    // TODO: Thêm logic tạo channel
    const name = newChannelName.trim();
    if (!name) return;
    const form = {
      channelName: name,
      memberIds: []
    }
    await dispatch(fetchCreateChannel(form));
    setNewChannelName("");
    setIsAddingChannel(false);
  };

  const headerActions = [
    {
      title: "Friend Requests",
      icon: <TeamOutlined />,
      count: pendingRequests.length,
      onClick: () => toggleModal("requests"),
    },
    {
      title: "Friends",
      icon: <UserOutlined />,
      count: friends.length,
      onClick: () => toggleModal("friends"),
    },
    {
      title: "Friend Suggestions",
      icon: <UserAddOutlined />,
      count: friendSuggestions.length,
      onClick: () => toggleModal("suggestions"),
    },
    { title: "Settings", icon: <SettingOutlined />, to: "/settings" },
  ];

  return (
    <motion.div
      initial={{ x: -300 }}
      animate={{ x: 0 }}
      transition={{ type: "spring", stiffness: 100 }}
      className="w-80 bg-white border-r flex flex-col"
    >
      {/* HEADER */}
      <div className="flex items-center justify-between p-3 border-b bg-white">
        <Title level={4} className="!m-0 !text-[#050505]">
          Messenger
        </Title>
        <Space>
          {headerActions.map(({ title, icon, count, onClick, to }) => (
            <Tooltip key={title} title={title}>
              {to ? (
                <Link to={to}>
                  <Button type="text" icon={icon} className="text-[#65676b]" />
                </Link>
              ) : (
                <Button
                  type="text"
                  icon={
                    <Badge count={count} offset={[10, 0]}>
                      {icon}
                    </Badge>
                  }
                  onClick={onClick}
                  className="text-[#65676b]"
                />
              )}
            </Tooltip>
          ))}
        </Space>
      </div>

      {/* SEARCH */}
      <div className="p-3">
        <Input
          placeholder="Search Messenger"
          style={{
            borderRadius: 9999,
            backgroundColor: "#f0f2f5",
            border: "none",
            padding: "8px 16px",
          }}
        />
      </div>

      {/* MAIN LIST */}
      <div className="flex-1 overflow-y-auto p-3">
        <div className="flex items-center justify-between mb-2">
          <Title level={5} className="!m-0 !text-[#65676b]">
            Channels
          </Title>

          {isAddingChannel ? (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <Input
                autoFocus
                placeholder="Channel name"
                value={newChannelName}
                onChange={(e) => setNewChannelName(e.target.value)}
                onPressEnter={handleChannelSubmit}
                style={{
                  width: 150,
                  borderRadius: 9999,
                  backgroundColor: "#f0f2f5",
                  border: "none",
                  padding: "8px 16px",
                }}
              />
            </motion.div>
          ) : (
            <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setIsAddingChannel(true)}
                style={{
                  borderRadius: 20,
                  backgroundColor: "#2196F3",
                  border: "none",
                }}
              >
                Add
              </Button>
            </motion.div>
          )}
        </div>

        <ChannelList />
        <FriendList friends={friends} />
      </div>

      {/* MODALS */}
      <FriendsModal
        open={openModal.friends}
        onClose={() => toggleModal("friends")}
        friends={friends}
        onSelectUser={onSelectUser}
      />
      <FriendRequestsModal
        open={openModal.requests}
        onClose={() => toggleModal("requests")}
        requests={pendingRequests}
        onAccept={handleAcceptRequest}
      />
      <FriendSuggestionsModal
        open={openModal.suggestions}
        onClose={() => toggleModal("suggestions")}
        suggestions={friendSuggestions}
        onAddFriend={handleAddFriend}
      />
    </motion.div>
  );
};

export default Sidebar;

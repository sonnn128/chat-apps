import React, { useEffect, useState } from "react";
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
import { successToast, errorToast } from "@/utils/toast";
import { stompClient } from "@/utils/ws";
import { fetchCreateChannel } from "@/stores/middlewares/channelMiddleware";
import {
  sendFriendRequest,
  acceptFriendRequest,
} from "@/stores/middlewares/friendShipMiddleware";
import {
  // setCurrentFriend,
  receiveMessage,
  removeCurrentChannel,
  setCurrentChannel,
} from "@/stores/slices/channelSlice";

const { Title } = Typography;

const Sidebar = () => {
  const dispatch = useDispatch();
  const { friendSuggestions, friends, pendingRequests } = useSelector(
    (state) => state.friendship
  );
  const { channels, currentChannelId } = useSelector((state) => state.channel);
  const {
    firstname: userFirstname,
    lastname: userLastname,
    id: userId,
  } = useSelector((state) => state.auth.user);
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

  const handleAcceptRequest = (requestId) =>
    dispatch(acceptFriendRequest(requestId));

  const handleChannelSubmit = async (e) => {
    if (e.key === "Enter" && newChannelName.trim()) {
      try {
        const res = await dispatch(fetchCreateChannel(newChannelName)).unwrap();
        stompClient.subscribe(`/channels/${res.id}`, (msg) => {
          dispatch(setCurrentChannel(res));
          dispatch(receiveMessage(JSON.parse(msg.body)));
        });
        stompClient.publish({
          destination: `/app/channels/${res.id}`,
          body: JSON.stringify({
            key: { channelId: res.id },
            userId,
            content: `${userFirstname} ${userLastname} created channel!`,
            type: "NOTICE",
            timestamp: Date.now(),
          }),
        });

        setNewChannelName("");
        setIsAddingChannel(false);
        successToast("Channel added");
      } catch (error) {
        errorToast("Channel creation failed");
        console.error(error);
      }
    }
  };

  const toggleModal = (type) =>
    setOpenModal((prev) => ({ ...prev, [type]: !prev[type] }));

  const onSelectUser = () => {
    dispatch(setCurrentFriend());
    dispatch(removeCurrentChannel());
  };

  useEffect(() => {
    stompClient.activate();
    stompClient.onConnect = () => {
      console.log("WebSocket connected");
      channels.forEach((channel) =>
        stompClient.subscribe(`/channels/${channel.id}`, (msg) => {
          dispatch(receiveMessage(JSON.parse(msg.body)));
        })
      );
    };
    return () => stompClient.deactivate();
  }, [channels, dispatch]);

  return (
    <motion.div
      initial={{ x: -300 }}
      animate={{ x: 0 }}
      transition={{ type: "spring", stiffness: 100 }}
      style={{
        width: 320,
        backgroundColor: "white",
        borderRight: "1px solid #f0f0f0",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <div
        style={{
          padding: 12,
          backgroundColor: "white",
          borderBottom: "1px solid #f0f0f0",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Title level={4} style={{ margin: 0, color: "#050505" }}>
          Messenger
        </Title>
        <Space>
          {[
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
          ].map(({ title, icon, count, onClick, to }) => (
            <Tooltip key={title} title={title}>
              {to ? (
                <Link to={to}>
                  <Button type="text" icon={icon} style={{ color: "#65676b" }} />
                </Link>
              ) : (
                <Button
                  type="text"
                  icon={
                    <Badge count={count || 0} offset={[10, 0]}>
                      {icon}
                    </Badge>
                  }
                  onClick={onClick}
                  style={{ color: "#65676b" }}
                />
              )}
            </Tooltip>
          ))}
        </Space>
      </div>

      <div style={{ padding: 12 }}>
        <Input
          placeholder="Search Messenger"
          style={{
            borderRadius: "9999px",
            backgroundColor: "#f0f2f5",
            border: "none",
            padding: "8px 16px",
          }}
        />
      </div>

      <div style={{ flex: 1, overflowY: "auto", padding: 12 }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 8,
          }}
        >
          <Title level={5} style={{ color: "#65676b", margin: 0 }}>
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
                  borderRadius: "9999px",
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
import React, { useState, useMemo, useCallback } from "react";
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
  PhoneOutlined, // Import icon PhoneOutlined cho trường hợp tìm kiếm SĐT
} from "@ant-design/icons";
import debounce from "lodash.debounce";

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
  const dataFakeForSearch = {
    firstname: "Dương",
    lastname: "Hoàng",
    email: "sonvipkl0423@gmail.com",
    password: "sonvipkl042@gmail.com",
    phone: "0799199917",
    avatar: "https://static.vecteezy.com/system/resources/previews/024/183/535/original/male-avatar-portrait-of-a-young-man-with-glasses-illustration-of-male-character-in-modern-color-style-vector.jpg",
  };

  const dispatch = useDispatch();
  const { friendSuggestions, friends, pendingRequests } = useSelector(
    (state) => state.friendship
  );
  const { channels } = useSelector((state) => state.channel);

  const [isAddingChannel, setIsAddingChannel] = useState(false);
  const [newChannelName, setNewChannelName] = useState("");
  const [openModal, setOpenModal] = useState({
    friends: false,
    requests: false,
    suggestions: false,
  });
  const [searchTerm, setSearchTerm] = useState("");
  const [displayedSearchTerm, setDisplayedSearchTerm] = useState(""); // State để lưu giá trị hiển thị trên Input

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
    const name = newChannelName.trim();
    if (!name) {
      console.warn("⚠️ Sidebar: Channel name is empty");
      return;
    }
    
    console.log("📝 Sidebar: Creating channel:", name);
    const form = {
      channelName: name,
    };
    
    try {
      const result = await dispatch(fetchCreateChannel(form));
      if (result.type.endsWith('fulfilled')) {
        console.log("✅ Sidebar: Channel created successfully");
        setNewChannelName("");
        setIsAddingChannel(false);
      } else {
        console.error("❌ Sidebar: Failed to create channel");
      }
    } catch (error) {
      console.error("❌ Sidebar: Error creating channel:", error);
    }
  };

  /**
   * SEARCH OPTIMIZATION
   * - debounce để tránh spam filter/API call
   */
  const debouncedSearch = useCallback(
    debounce((value) => {
      setSearchTerm(value.trim()); // Cập nhật searchTerm cho logic hiển thị
    }, 200),
    []
  );

  const handleSearchChange = (e) => {
    const value = e.target.value;
    setDisplayedSearchTerm(value); // Cập nhật giá trị hiển thị ngay lập tức
    debouncedSearch(value);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    debouncedSearch.flush(); // chạy ngay lập tức khi nhấn Enter
  };

  // Filter local list (nếu chưa dùng backend)
  const filteredChannels = useMemo(() => {
    if (!searchTerm) return channels;
    return channels.filter((channel) =>
      channel.channelName.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [channels, searchTerm]);

  const filteredFriends = useMemo(() => {
    if (!searchTerm) return friends;
    return friends.filter((friend) =>
      friend.userName.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [friends, searchTerm]);

  // Kiểm tra nếu có dữ liệu tìm kiếm để quyết định hiển thị
  const hasSearchInput = useMemo(() => {
    return displayedSearchTerm.trim() !== "";
  }, [displayedSearchTerm]);

  // Kiểm tra nếu số điện thoại trùng khớp với dataFakeForSearch
  const isMatchingFakePhone = useMemo(() => {
    return hasSearchInput && searchTerm === dataFakeForSearch.phone;
  }, [hasSearchInput, searchTerm, dataFakeForSearch.phone]);

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
      className="w-[420px] bg-white border-r flex flex-col"
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
      <div className="p-3 border-b bg-white">
        <form onSubmit={handleSearchSubmit}>
          <Input
            placeholder="Tìm kiếm bằng số điện thoại"
            prefix={<UserOutlined />}
            style={{
              borderRadius: 9999,
              backgroundColor: "#f0f2f5",
              border: "none",
              padding: "8px 16px",
            }}
            onChange={handleSearchChange}
            value={displayedSearchTerm} // Sử dụng displayedSearchTerm để kiểm soát giá trị của input
            allowClear
          />
        </form>
      </div>

      {/* MAIN LIST */}
      <div className="flex-1 overflow-y-auto p-3">
        {hasSearchInput && !isMatchingFakePhone && (
          // Hiển thị như ảnh nếu có input search và không trùng SĐT fake
          <div className="flex flex-col items-center justify-center p-4">
            <PhoneOutlined
              style={{ fontSize: "48px", color: "#1890ff", marginBottom: "16px" }}
            />
            <Title level={5} className="!m-0 !text-[#65676b] text-center">
              Số điện thoại chưa đăng ký tài khoản hoặc không cho phép tìm kiếm.
            </Title>
          </div>
        )}

        {isMatchingFakePhone && (
          // Hiển thị dataFakeForSearch nếu trùng SĐT fake
          <div className="flex items-center gap-3 p-2 bg-gray-100 rounded-lg">
            <img
              src={dataFakeForSearch.avatar} // Sử dụng avatar của dataFakeForSearch
              alt="Avatar"
              className="w-10 h-10 rounded-full object-cover"
            />
            <div>
              <p className="font-semibold m-0">
                {dataFakeForSearch.firstname} {dataFakeForSearch.lastname}
              </p>
              <p className="text-sm text-gray-600 m-0">
                {dataFakeForSearch.phone}
              </p>
            </div>
          </div>
        )}

        {!hasSearchInput && (
          // Hiển thị Channels và Friends nếu không có input search
          <>
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

            <ChannelList channels={filteredChannels} />
            <FriendList friends={filteredFriends} />
          </>
        )}
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
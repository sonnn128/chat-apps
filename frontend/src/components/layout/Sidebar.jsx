import React, { useState, useMemo, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Input, Button, Tooltip, Badge, Space, Typography } from "antd";
import {
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
  PlusOutlined,
  PhoneOutlined,
  LoadingOutlined,
} from "@ant-design/icons";
import debounce from "lodash.debounce";

import ConversationList from "@/components/conversations/ConversationList";
import FriendsModal from "@/components/modals/FriendsModal";
import FriendRequestsModal from "@/components/modals/FriendRequestsModal";
import { successToast } from "@/utils/toast";
import userService from "@/services/userService";

import {
  sendFriendRequest,
  acceptFriendRequest,
  rejectFriendRequest,
  cancelFriendRequest,
} from "@/stores/middlewares/friendShipMiddleware";
import { removeCurrentChannel } from "@/stores/slices/channelSlice";
import { fetchCreateChannel } from "@/stores/middlewares/channelMiddleware";

import { DEFAULT_AVATAR } from "@/utils/constants";

const { Title } = Typography;

const Sidebar = () => {
  const dispatch = useDispatch();
  const { friends, pendingRequests, sentRequests } = useSelector(
    (state) => state.friendship
  );
  const { channels } = useSelector((state) => state.channel);
  const { user } = useSelector((state) => state.auth); // eslint-disable-line no-unused-vars

  const [isAddingChannel, setIsAddingChannel] = useState(false);
  const [newChannelName, setNewChannelName] = useState("");
  const [openModal, setOpenModal] = useState({
    friends: false,
    requests: false,
  });
  const [searchTerm, setSearchTerm] = useState("");
  const [displayedSearchTerm, setDisplayedSearchTerm] = useState(""); // State để lưu giá trị hiển thị trên Input
  const [searchResult, setSearchResult] = useState(null); // State để lưu kết quả tìm kiếm
  const [isSearching, setIsSearching] = useState(false); // State để hiển thị loading
  const [isCreatingChannel, setIsCreatingChannel] = useState(false);

  const handleAddFriend = async (userId) => {
    const res = await dispatch(sendFriendRequest(userId)).unwrap();
    if (res) successToast("Friend request sent");
  };

  const handleAcceptRequest = (requesterId) => {
    dispatch(acceptFriendRequest(requesterId));
  };

  const handleRejectRequest = (requesterId) => {
    dispatch(rejectFriendRequest(requesterId));
  };

  const handleCancelRequest = (friendId) => {
    dispatch(cancelFriendRequest(friendId));
  };

  const toggleModal = (type) =>
    setOpenModal((prev) => ({ ...prev, [type]: !prev[type] }));

  const onSelectUser = () => {
    dispatch(removeCurrentChannel());
  };

  const handleChannelSubmit = async () => {
    const name = newChannelName.trim();
    if (!name) {

      return;
    }

    if (isCreatingChannel) return;


    setIsCreatingChannel(true);
    const form = {
      channelName: name,
    };

    try {
      const result = await dispatch(fetchCreateChannel(form));
      if (result.type.endsWith('fulfilled')) {

        setNewChannelName("");
        setIsAddingChannel(false);
      } else {

      }
    } catch (error) {

    } finally {
      setIsCreatingChannel(false);
    }
  };

  /**
   * SEARCH OPTIMIZATION
   * - debounce để tránh spam filter/API call
   */
  const debouncedSearch = useCallback(
    debounce(async (value) => {
      const trimmedValue = value.trim();
      setSearchTerm(trimmedValue);

      // Nếu là số điện thoại (chỉ chứa số và có độ dài hợp lý)
      if (trimmedValue && /^\d{10,11}$/.test(trimmedValue)) {
        setIsSearching(true);
        try {
          const response = await userService.searchUserByPhone(trimmedValue);
          if (response.success) {
            setSearchResult(response.data);
          } else {
            setSearchResult(null);
          }
        } catch (error) {

          setSearchResult(null);
        } finally {
          setIsSearching(false);
        }
      } else {
        setSearchResult(null);
      }
    }, 500),
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
    return channels.filter((channel) => {
      // For group channels, filter by channelName
      if (channel.channelType !== 'DIRECT_MESSAGE' && channel.channelName) {
        return channel.channelName.toLowerCase().includes(searchTerm.toLowerCase());
      }
      // For direct channels, filter by participant names
      if (channel.channelType === 'DIRECT_MESSAGE' && channel.participants && channel.participants.length === 2) {
        const otherParticipant = channel.participants.find(p => p.userId !== user?.id);
        if (otherParticipant) {
          const fullName = `${otherParticipant.firstname || ""} ${otherParticipant.lastname || ""}`.toLowerCase();
          return fullName.includes(searchTerm.toLowerCase());
        }
      }
      return false;
    });
  }, [channels, searchTerm, user?.id]);

  const filteredFriends = useMemo(() => {
    if (!searchTerm) return friends;
    return friends.filter((friend) => {
      const fullName = `${friend.firstname || ""} ${friend.lastname || ""}`.toLowerCase();
      const email = (friend.email || "").toLowerCase();
      const searchLower = searchTerm.toLowerCase();
      return fullName.includes(searchLower) || email.includes(searchLower);
    });
  }, [friends, searchTerm]);

  // Kiểm tra nếu có dữ liệu tìm kiếm để quyết định hiển thị
  const hasSearchInput = useMemo(() => {
    return displayedSearchTerm.trim() !== "";
  }, [displayedSearchTerm]);

  // Kiểm tra nếu có kết quả tìm kiếm số điện thoại
  const hasPhoneSearchResult = useMemo(() => {
    return hasSearchInput && searchResult && /^\d{10,11}$/.test(searchTerm);
  }, [hasSearchInput, searchResult, searchTerm]);

  const headerActions = [
    {
      title: "Friend Requests",
      icon: <TeamOutlined />,
      count: pendingRequests?.length || 0,
      onClick: () => toggleModal("requests"),
    },
    {
      title: "Friends",
      icon: <UserOutlined />,
      count: friends?.length || 0,
      onClick: () => toggleModal("friends"),
    },
    { title: "Settings", icon: <SettingOutlined />, to: "/settings" },
  ];

  return (
    <motion.div
      initial={{ x: -300 }}
      animate={{ x: 0 }}
      transition={{ type: "spring", stiffness: 100 }}
      className="w-[420px] bg-white flex flex-col"
    >
      {/* HEADER */}
      <div className="flex items-center justify-between p-3 bg-white">
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
      <div className="p-3 bg-white">
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
        {hasSearchInput && !hasPhoneSearchResult && !isSearching && (
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

        {isSearching && (
          <div className="flex items-center gap-3 p-2 bg-gray-100 rounded-lg">
            <div className="w-10 h-10 bg-gray-200 rounded-full animate-pulse"></div>
            <div>
              <p className="font-semibold m-0 text-gray-500">Đang tìm kiếm...</p>
            </div>
          </div>
        )}

        {hasPhoneSearchResult && searchResult.id !== user?.data?.id && (
          // Hiển thị kết quả tìm kiếm thực từ API (loại bỏ chính mình)
          <div className="flex items-center gap-3 p-2 bg-gray-100 rounded-lg">
            <img
              src={searchResult.avatar || "https://via.placeholder.com/40x40?text=U"}
              alt="Avatar"
              className="w-10 h-10 rounded-full object-cover"
            />
            <div className="flex-1">
              <p className="font-semibold m-0">
                {searchResult.firstname} {searchResult.lastname}
              </p>
              <p className="text-sm text-gray-600 m-0">
                {searchResult.phone}
              </p>
              <p className="text-xs text-gray-500 m-0">
                {searchResult.email}
              </p>
            </div>
            {(() => {
              const isFriend = friends.some(friend => friend.friendId === searchResult.id);
              const hasSentRequest = sentRequests.some(req => req.friendId === searchResult.id);

              if (isFriend) {
                return (
                  <Button
                    type="default"
                    size="small"
                    disabled
                    style={{
                      backgroundColor: '#4a4a4a',
                      borderColor: '#4a4a4a',
                      color: '#ffffff',
                      cursor: 'not-allowed',
                      opacity: 1
                    }}
                    icon={<UserOutlined style={{ color: '#ffffff' }} />}
                  >
                    Bạn bè
                  </Button>
                );
              }

              if (hasSentRequest) {
                return (
                  <Button
                    type="default"
                    size="small"
                    danger
                    onClick={() => handleCancelRequest(searchResult.id)}
                  >
                    Hủy yêu cầu
                  </Button>
                );
              }

              return (
                <Button
                  type="primary"
                  size="small"
                  onClick={() => handleAddFriend(searchResult.id)}
                >
                  Thêm bạn bè
                </Button>
              );
            })()}
          </div>
        )}

        {hasPhoneSearchResult && searchResult.id === user?.data?.id && (
          // Hiển thị thông báo khi tìm thấy chính mình
          <div className="flex items-center gap-3 p-2 bg-blue-50 rounded-lg border border-blue-200">
            <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
              <UserOutlined className="text-blue-500" />
            </div>
            <div className="flex-1">
              <p className="font-semibold m-0 text-blue-700">
                Đây là số điện thoại của bạn
              </p>
              <p className="text-sm text-blue-600 m-0">
                {searchResult.phone}
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
                    disabled={isCreatingChannel}
                    suffix={isCreatingChannel ? <LoadingOutlined /> : null}
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

            <ConversationList channels={filteredChannels} />
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
        onReject={handleRejectRequest}
      />
    </motion.div>
  );
};

export default Sidebar;
import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Modal, Button, Typography, List, Avatar, Checkbox, message } from "antd";
import PropTypes from "prop-types";
import { fetchFriendList } from "@/stores/middlewares/friendShipMiddleware";
import { addPeopleToChannel } from "@/stores/middlewares/channelMiddleware";
import { DEFAULT_AVATAR } from "@/utils/constants";

const { Title, Text } = Typography;

const AddPeopleModal = ({ open, onClose, channelId, channelName, currentMembers = [] }) => {
  const dispatch = useDispatch();
  const { friends, loading } = useSelector((state) => state.friendship);
  const { channels } = useSelector((state) => state.channel);
  const [selectedFriends, setSelectedFriends] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Get current channel data to display current members
  const currentChannel = channels.find(ch => ch.id === channelId);
  const currentChannelMembers = currentChannel?.participants || [];

  // Fetch friends when modal opens
  useEffect(() => {
    if (open) {
      dispatch(fetchFriendList());
      setSelectedFriends([]);
    }
  }, [open, dispatch]);

  const handleFriendToggle = (friendId) => {
    setSelectedFriends(prev => {
      if (prev.includes(friendId)) {
        return prev.filter(id => id !== friendId);
      } else {
        return [...prev, friendId];
      }
    });
  };

  const handleSubmit = async () => {
    if (selectedFriends.length === 0) {
      message.warning("Vui lòng chọn ít nhất một người để thêm vào kênh");
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await dispatch(addPeopleToChannel({
        channelId,
        memberIds: selectedFriends
      })).unwrap();

      if (result) {
        message.success(`Đã thêm ${selectedFriends.length} người vào kênh`);
        setSelectedFriends([]);
        onClose();
      }
    } catch (error) {
      message.error("Có lỗi xảy ra khi thêm người vào kênh");
      console.error("Error adding people to channel:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const isAlreadyMember = (friendId) => {
    // Check both currentMembers prop and currentChannelMembers from Redux
    return currentMembers.includes(friendId) ||
      currentChannelMembers.some(member => member.userId === friendId);
  };

  // Get member details for display
  const getMemberDisplayName = (member) => {
    if (member.firstname && member.lastname) {
      return `${member.firstname} ${member.lastname}`;
    }
    if (member.name && member.name !== `User ${member.userId.substring(0, 8)}`) {
      return member.name;
    }
    return `User ${member.userId.substring(0, 8)}`;
  };

  // Get member role display
  const getMemberRoleDisplay = (member) => {
    // Only show email if it's a real email (not generated)
    const isRealEmail = member.email &&
      member.email !== `${member.userId?.substring(0, 8) || 'unknown'}@example.com` &&
      member.email.includes('@') &&
      !member.email.includes('example.com');

    if (isRealEmail && member.role) {
      return `${member.email} · Role: ${member.role}`;
    }
    if (member.role) {
      return `Role: ${member.role}`;
    }
    if (isRealEmail) {
      return member.email;
    }
    return '';
  };

  const renderFriendsList = () => {
    if (loading) {
      return (
        <div className="text-center py-4">
          <Text type="secondary">Đang tải danh sách bạn bè...</Text>
        </div>
      );
    }

    if (friends?.length > 0) {
      return (
        <List
          itemLayout="horizontal"
          dataSource={friends}
          renderItem={(friend) => {
            const isMember = isAlreadyMember(friend.friendId);
            const isSelected = selectedFriends.includes(friend.friendId);

            return (
              <List.Item
                key={friend.friendId}
                className={`p-2 rounded-lg ${isMember ? 'bg-gray-100 opacity-60' : 'hover:bg-gray-50'}`}
              >
                <div className="flex items-center w-full">
                  <Avatar
                    src={friend.avatar || DEFAULT_AVATAR}
                    className="mr-3"
                  />

                  <div className="flex-1">
                    <Text strong className={isMember ? 'text-gray-500' : ''}>
                      {friend.firstname} {friend.lastname}
                    </Text>
                    <br />
                    <Text type="secondary" className="text-sm">
                      {friend.email}
                    </Text>
                    {isMember && (
                      <Text type="secondary" className="text-xs block">
                        (Đã có trong kênh)
                      </Text>
                    )}
                  </div>

                  <Checkbox
                    checked={isSelected}
                    disabled={isMember}
                    onChange={() => handleFriendToggle(friend.friendId)}
                  />
                </div>
              </List.Item>
            );
          }}
        />
      );
    }

    return (
      <div className="text-center py-8">
        <Text type="secondary">Bạn chưa có bạn bè nào</Text>
      </div>
    );
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      title={<Title level={5}>Thêm người vào kênh "{channelName}"</Title>}
      footer={[
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button
          key="submit"
          type="primary"
          loading={isSubmitting}
          onClick={handleSubmit}
          disabled={selectedFriends.length === 0}
        >
          Thêm {selectedFriends.length > 0 ? `(${selectedFriends.length})` : ''}
        </Button>,
      ]}
      styles={{ body: { maxHeight: "500px", overflowY: "auto", paddingTop: 0 } }}
      width={600}
    >
      {/* Current Members Section */}
      {currentChannelMembers.length > 0 && (
        <div className="mb-4">
          <Title level={5} className="mb-3">Thành viên hiện tại ({currentChannelMembers.length})</Title>
          <div className="max-h-32 overflow-y-auto border rounded-lg p-2 bg-gray-50">
            {currentChannelMembers.map((member) => (
              <div key={member.userId} className="flex items-center gap-2 py-1 px-2 hover:bg-gray-100 rounded">
                <Avatar size="small" src={member.avatar || DEFAULT_AVATAR} />
                <div className="flex-1">
                  <Text className="text-sm font-medium">{getMemberDisplayName(member)}</Text>
                  {getMemberRoleDisplay(member) && (
                    <Text type="secondary" className="text-xs block">
                      {getMemberRoleDisplay(member)}
                    </Text>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Friends to Add Section */}
      <div>
        <Title level={5} className="mb-3">Thêm bạn bè vào kênh</Title>
        {renderFriendsList()}
      </div>
    </Modal>
  );
};

AddPeopleModal.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  channelId: PropTypes.string,
  channelName: PropTypes.string,
  currentMembers: PropTypes.arrayOf(PropTypes.string),
};

export default AddPeopleModal;

import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Modal, Typography, List, Checkbox, Button, Space } from "antd";
import { addMembersToChannel } from "@/stores/middlewares/channelMiddleware";

const { Title, Text } = Typography;

const AddMemberModal = ({ open, onClose }) => {
  const dispatch = useDispatch();
  const { friends } = useSelector((state) => state.friendship);
  const channelId = useSelector((state) => state.channel.currentChannelId);
  const [selectedFriends, setSelectedFriends] = useState([]);

  // Handlers
  const toggleFriend = (friendId) =>
    setSelectedFriends((prev) =>
      prev.includes(friendId)
        ? prev.filter((id) => id !== friendId)
        : [...prev, friendId]
    );

  const getName = (friend) =>
    `${friend?.firstname || ""} ${friend?.lastname || ""}`.trim() ||
    "Unknown User";

  const handleAddMembers = () => {
    dispatch(addMembersToChannel({ userIds: selectedFriends, channelId }));
    setSelectedFriends([]); // Reset danh sách
    onClose(); // Đóng modal
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      title={
        <Title level={4} style={{ margin: 0, color: "#333" }}>
          Add Members
        </Title>
      }
      closeIcon={<span style={{ color: "#666", fontSize: 16 }}>✕</span>}
      styles={{
        content: {
          padding: 24,
          borderRadius: 8,
          backgroundColor: "#fff",
          boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
        },
      }}
    >
      <div style={{ maxHeight: 300, overflowY: "auto", marginTop: 16 }}>
        {friends?.length ? (
          <List
            dataSource={friends}
            renderItem={(friend) => (
              <List.Item key={friend.id} style={{ padding: "8px 0" }}>
                <Space>
                  <Checkbox
                    checked={selectedFriends.includes(friend.id)}
                    onChange={() => toggleFriend(friend.id)}
                    style={{ color: "#666" }}
                  />
                  <Text style={{ fontSize: 16, color: "#333" }}>
                    {getName(friend)}
                  </Text>
                </Space>
              </List.Item>
            )}
          />
        ) : (
          <Text
            style={{
              textAlign: "center",
              padding: 16,
              display: "block",
              fontSize: 16,
              color: "#666",
            }}
          >
            No friends available
          </Text>
        )}
      </div>

      {!!selectedFriends.length && (
        <Button
          type="primary"
          block
          onClick={handleAddMembers}
          style={{
            marginTop: 24,
            height: 40,
            fontSize: 16,
            backgroundColor: "#1976d2",
            borderColor: "#1976d2",
          }}
        >
          Add
        </Button>
      )}
    </Modal>
  );
};

export default AddMemberModal;
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Modal, Typography, Button, Avatar, List } from "antd";
import { setCurrentFriend } from "@/stores/slices/friendshipSlice";
import { removeCurrentChannel } from "@/stores/slices/channelSlice";

const { Title, Text } = Typography;

const FriendsModal = ({ open, onClose }) => {
  const dispatch = useDispatch();
  const friends = useSelector((state) => state.friendship.friends);

  const handleSelectFriend = (friend) => {
    dispatch(setCurrentFriend(friend));
    dispatch(removeCurrentChannel());
    onClose();
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={[
        <Button key="close" type="primary" onClick={onClose} block>
          Close
        </Button>,
      ]}
      title={<Title level={5}>Your Friends</Title>}
      bodyStyle={{ maxHeight: "400px", overflowY: "auto", paddingTop: 0 }}
    >
      {friends?.length > 0 ? (
        <List
          itemLayout="horizontal"
          dataSource={friends}
          renderItem={(friend) => (
            <List.Item
              key={friend.id}
              onClick={() => handleSelectFriend(friend)}
              className="cursor-pointer hover:bg-gray-100 rounded-lg px-2"
            >
              <List.Item.Meta
                avatar={<Avatar src={friend.avatar || ""} />}
                title={
                  <Text strong>
                    {friend.firstname + " " + friend.lastname}
                  </Text>
                }
                description={friend.email || "email"}
              />
            </List.Item>
          )}
        />
      ) : (
        <Text type="secondary" style={{ textAlign: "center", display: "block", padding: "16px 0" }}>
          No friends to display
        </Text>
      )}
    </Modal>
  );
};

export default FriendsModal;

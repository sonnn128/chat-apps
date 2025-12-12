import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Modal, Typography, Button, Avatar, List, Popconfirm } from "antd";
import { setCurrentFriend, unfriendUser } from "@/stores/slices/friendshipSlice";
import { setCurrentChannel } from "@/stores/slices/channelSlice";
import { fetchGetOrCreateDirectChannel } from "@/stores/middlewares/channelMiddleware";
import { DEFAULT_AVATAR } from "@/utils/constants";

const { Title, Text } = Typography;

const FriendsModal = ({ open, onClose }) => {
  const dispatch = useDispatch();
  const friends = useSelector((state) => state.friendship.friends);

  const handleSelectFriend = async (friend) => {
    dispatch(setCurrentFriend(friend));

    // Fetch or create direct channel with this friend
    try {
      const result = await dispatch(fetchGetOrCreateDirectChannel(friend.friendId)).unwrap();
      dispatch(setCurrentChannel(result));
      onClose();
    } catch (error) {
      console.error("Failed to get or create direct channel:", error);
    }
  };

  const handleUnfriend = (friendId, e) => {
    e.stopPropagation(); // Prevent triggering handleSelectFriend
    dispatch(unfriendUser(friendId));
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
      styles={{ body: { maxHeight: "400px", overflowY: "auto", paddingTop: 0 } }}
    >
      {friends?.length > 0 ? (
        <List
          itemLayout="horizontal"
          dataSource={friends}
          renderItem={(friend) => (
            <List.Item
              key={friend.friendId}
              onClick={() => handleSelectFriend(friend)}
              className="cursor-pointer hover:bg-gray-100 rounded-lg px-2"
              actions={[
                <Popconfirm
                  key="unfriend"
                  title="Xóa bạn bè"
                  description="Bạn có chắc chắn muốn xóa bạn bè này không?"
                  onConfirm={(e) => handleUnfriend(friend.friendId, e)}
                  okText="Xóa"
                  cancelText="Hủy"
                  okButtonProps={{ danger: true }}
                >
                  <Button
                    type="text"
                    danger
                    size="small"
                    onClick={(e) => e.stopPropagation()}
                  >
                    Xóa bạn
                  </Button>
                </Popconfirm>
              ]}
            >
              <List.Item.Meta
                avatar={<Avatar src={friend.avatar || DEFAULT_AVATAR} />}
                title={
                  <Text strong>
                    {`${friend.firstname || ""} ${friend.lastname || ""}`}
                  </Text>
                }
                description={friend.email || "No email"}
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

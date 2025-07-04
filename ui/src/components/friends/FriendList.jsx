import React from "react";
import { List, Avatar, Typography } from "antd";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import { setCurrentFriend } from "@/stores/slices/friendshipSlice";
import { removeCurrentChannel } from "@/stores/slices/channelSlice";

const { Text, Paragraph } = Typography;

const FriendList = () => {
  const dispatch = useDispatch();
  const friends = useSelector((state) => state.friendship.friends);

  const handleSelectFriend = (friend) => {
    dispatch(setCurrentFriend(friend));
    dispatch(removeCurrentChannel());
  };

  return (
    <div className="mt-4">
      <Text strong className="text-sm text-gray-600 mb-2 block">
        Friends
      </Text>
      {friends && friends.length > 0 ? (
        <List
          itemLayout="horizontal"
          dataSource={friends}
          renderItem={(friend) => (
            <motion.div
              key={friend.id}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              <List.Item
                onClick={() => handleSelectFriend(friend)}
                className="rounded-lg px-2 py-2 hover:bg-gray-100 cursor-pointer"
              >
                <List.Item.Meta
                  avatar={
                    <Avatar size={36}>
                      {friend.firstname.charAt(0).toUpperCase()}
                    </Avatar>
                  }
                  title={
                    <Text style={{ fontWeight: 500, color: "#050505" }}>
                      {`${friend.firstname} ${friend.lastname}`}
                    </Text>
                  }
                  description={
                    <Text type="secondary" ellipsis>
                      {friend.email}
                    </Text>
                  }
                />
              </List.Item>
            </motion.div>
          )}
        />
      ) : (
        <Typography.Text type="secondary">No friends to display</Typography.Text>
      )}
    </div>
  );
};

export default FriendList;

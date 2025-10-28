import React from "react";
import { List, Avatar, Typography } from "antd";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import { setCurrentChannel } from "@/stores/slices/channelSlice";
import { setCurrentFriend, removeCurrentFriend } from "@/stores/slices/friendshipSlice";

const { Text } = Typography;

// A combined list of channels and friends. Clicking a friend opens/creates a DM channel.
const ConversationList = ({ channels = [], friends = [] }) => {
  const dispatch = useDispatch();
  const userId = useSelector((state) => state.auth.user?.data?.id);

  const onSelectChannel = (channel) => {
    dispatch(setCurrentChannel(channel));
    dispatch(removeCurrentFriend());
  };

  const handleSelectFriend = (friend) => {
    // Do NOT create channel here. Server will create channel when friend request is accepted.
    // Selecting a friend opens a 1-1 conversation view (virtual) until the real channel exists.
    dispatch(setCurrentFriend(friend));
    dispatch(setCurrentChannel(null));
  };

  // merge lists: channels first, then friends (friends appear as pseudo-channels)
  const items = [
    ...(channels || []).map((ch) => ({ type: "channel", key: ch.id, data: ch })),
    ...(friends || []).map((f) => ({ type: "friend", key: f.friendId, data: f })),
  ];

  return (
    <div>
      <List
        dataSource={items}
        renderItem={(item) => {
          if (item.type === "channel") {
            const ch = item.data;
            return (
              <motion.div key={item.key} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <List.Item onClick={() => onSelectChannel(ch)} className="rounded-lg px-2 py-2 hover:bg-gray-100 cursor-pointer">
                  <List.Item.Meta
                    avatar={<Avatar size={36}>{(ch.channelName || "#")[0]}</Avatar>}
                    title={<Text style={{ fontWeight: 500 }}>{ch.channelName || "Channel"}</Text>}
                    description={<Text type="secondary">{ch.memberIds ? ch.memberIds.length + " members" : "Group"}</Text>}
                  />
                </List.Item>
              </motion.div>
            );
          }

          const f = item.data;
          return (
            <motion.div key={item.key} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
              <List.Item onClick={() => handleSelectFriend(f)} className="rounded-lg px-2 py-2 hover:bg-gray-100 cursor-pointer">
                <List.Item.Meta
                  avatar={<Avatar size={36} src={f.avatar}>{f.firstname?.charAt(0)?.toUpperCase() || "U"}</Avatar>}
                  title={<Text style={{ fontWeight: 500 }}>{`${f.firstname || ""} ${f.lastname || ""}`}</Text>}
                  description={<Text type="secondary">{f.email || "Friend"}</Text>}
                />
              </List.Item>
            </motion.div>
          );
        }}
      />
    </div>
  );
};

export default ConversationList;

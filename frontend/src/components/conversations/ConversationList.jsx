import React from "react";
import { List, Avatar, Typography, Dropdown, Button } from "antd";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import { setCurrentChannel } from "@/stores/slices/channelSlice";
import { setCurrentFriend, removeCurrentFriend } from "@/stores/slices/friendshipSlice";
import { fetchGetOrCreateDirectChannel, fetchDeleteChannel } from "@/stores/middlewares/channelMiddleware";
import {
  MoreOutlined,
  MailOutlined,
  BellOutlined,
  UserOutlined,
  PhoneOutlined,
  VideoCameraOutlined,
  StopOutlined,
  InboxOutlined,
  DeleteOutlined,
  WarningOutlined
} from "@ant-design/icons";

const { Text } = Typography;

// A combined list of channels and friends. Clicking a friend opens/creates a DM channel.
const ConversationList = ({ channels = [], friends = [] }) => {
  const dispatch = useDispatch();
  const userId = useSelector((state) => state.auth.user?.data?.id);

  const onSelectChannel = (channel) => {
    dispatch(setCurrentChannel(channel));
    dispatch(removeCurrentFriend());
  };

  const handleSelectFriend = async (friend) => {
    dispatch(setCurrentFriend(friend));

    // Fetch or create direct channel with this friend
    try {
      const result = await dispatch(fetchGetOrCreateDirectChannel(friend.friendId)).unwrap();
      dispatch(setCurrentChannel(result));
    } catch (error) {
      console.error("Failed to get or create direct channel:", error);
    }
  };

  // Menu items for channel options
  const getChannelMenuItems = (channel) => {
    const items = [
      {
        key: 'mark_unread',
        label: 'Mark as unread',
        icon: <MailOutlined />,
        onClick: () => console.log('Mark as unread', channel.id),
      },
      {
        key: 'mute',
        label: 'Mute notifications',
        icon: <BellOutlined />,
        onClick: () => console.log('Mute notifications', channel.id),
      },
      {
        key: 'view_profile',
        label: 'View profile',
        icon: <UserOutlined />,
        onClick: () => console.log('View profile', channel.id),
      },
      { type: 'divider' },
      {
        key: 'audio_call',
        label: 'Audio call',
        icon: <PhoneOutlined />,
        onClick: () => console.log('Audio call', channel.id),
      },
      {
        key: 'video_chat',
        label: 'Video chat',
        icon: <VideoCameraOutlined />,
        onClick: () => console.log('Video chat', channel.id),
      },
      { type: 'divider' },
      {
        key: 'block',
        label: 'Block',
        icon: <StopOutlined />,
        onClick: () => console.log('Block', channel.id),
      },
      {
        key: 'archive',
        label: 'Archive chat',
        icon: <InboxOutlined />,
        onClick: () => console.log('Archive chat', channel.id),
      },
      {
        key: 'report',
        label: 'Report',
        icon: <WarningOutlined />,
        onClick: () => console.log('Report', channel.id),
      },
    ];

    // Only show delete option for ADMIN
    if (channel.role === 'ADMIN') {
      items.splice(items.length - 1, 0, {
        key: 'delete',
        label: 'Delete chat',
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => {
          if (window.confirm("Are you sure you want to delete this channel?")) {
            dispatch(fetchDeleteChannel(channel.id));
          }
        },
      });
    }

    return items;
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
            // Get member count - prefer memberIds, fallback to participants
            const memberCount = ch.memberIds?.length || ch.participants?.length || 0;
            const memberText = memberCount === 1 ? "1 member" : memberCount + " members";

            return (
              <motion.div key={item.key} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <div
                  className="group relative rounded-lg px-2 py-2 hover:bg-gray-100 cursor-pointer flex items-center justify-between"
                  onClick={() => onSelectChannel(ch)}
                >
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <Avatar size={36}>{(ch.channelName || "#")[0]}</Avatar>
                    <div className="flex flex-col overflow-hidden">
                      <Text style={{ fontWeight: 500 }} ellipsis>{ch.channelName || "Channel"}</Text>
                      <Text type="secondary" style={{ fontSize: '12px' }} ellipsis>{memberText}</Text>
                    </div>
                  </div>

                  {/* Options Menu Trigger - Visible on Hover */}
                  <div
                    className="opacity-0 group-hover:opacity-100 transition-opacity absolute right-2 bg-gray-100 rounded-full shadow-sm"
                    onClick={(e) => e.stopPropagation()} // Prevent triggering item click
                  >
                    <Dropdown
                      menu={{ items: getChannelMenuItems(ch) }}
                      trigger={['click']}
                      placement="bottomRight"
                    >
                      <Button
                        type="text"
                        shape="circle"
                        icon={<MoreOutlined rotate={90} />}
                        size="small"
                        className="flex items-center justify-center bg-white hover:bg-gray-200 border border-gray-200"
                      />
                    </Dropdown>
                  </div>
                </div>
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

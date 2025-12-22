import React from "react";
import { List, Avatar, Typography, Dropdown, Button } from "antd";
import { motion } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import { setCurrentChannel, markChannelAsReadLocal } from "@/stores/slices/channelSlice";
import { fetchDeleteChannel, markChannelAsRead } from "@/stores/middlewares/channelMiddleware";
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
import { DEFAULT_AVATAR } from "@/utils/constants";

// Display only direct message channels from friends
const ConversationList = ({ channels = [] }) => {
  const dispatch = useDispatch();
  const userId = useSelector((state) => state.auth.user?.data?.id);

  const onSelectChannel = (channel) => {
    dispatch(setCurrentChannel(channel));
    if (channel.hasUnread) {
      dispatch(markChannelAsReadLocal(channel.id));
      dispatch(markChannelAsRead(channel.id));
    }
  };

  // Menu items for channel options
  const getChannelMenuItems = (channel) => {
    const items = [
      {
        key: 'mark_unread',
        label: 'Mark as unread',
        icon: <MailOutlined />,

      },
      {
        key: 'mute',
        label: 'Mute notifications',
        icon: <BellOutlined />,

      },
      {
        key: 'view_profile',
        label: 'View profile',
        icon: <UserOutlined />,

      },
      { type: 'divider' },
      {
        key: 'audio_call',
        label: 'Audio call',
        icon: <PhoneOutlined />,

      },
      {
        key: 'video_chat',
        label: 'Video chat',
        icon: <VideoCameraOutlined />,

      },
      { type: 'divider' },
      {
        key: 'block',
        label: 'Block',
        icon: <StopOutlined />,

      },
      {
        key: 'archive',
        label: 'Archive chat',
        icon: <InboxOutlined />,

      },
      {
        key: 'report',
        label: 'Report',
        icon: <WarningOutlined />,

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

  // Show all channels (both GROUP and DIRECT_MESSAGE)
  const items = (channels || []).map((ch) => ({ type: "channel", key: ch.id, data: ch }));

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
                    <div className="relative">
                      <Avatar size={36} src={(() => {
                        // For direct channels, show other participant's avatar
                        if (ch.channelType === 'DIRECT_MESSAGE' && ch.participants && ch.participants.length === 2) {
                          const otherParticipant = ch.participants.find(p => (p.userId || p.id) !== userId);
                          if (otherParticipant) {
                            return otherParticipant.avatar || otherParticipant.avatarUrl || DEFAULT_AVATAR;
                          }
                        }
                        // For group channels, show channel avatar
                        return ch.avatar || DEFAULT_AVATAR;
                      })()}>
                        {(() => {
                          if (ch.channelType === 'DIRECT_MESSAGE' && ch.participants && ch.participants.length === 2) {
                            const otherParticipant = ch.participants.find(p => (p.userId || p.id) !== userId);
                            if (otherParticipant) {
                              return (otherParticipant.firstname || "U").charAt(0);
                            }
                          }
                          return (ch.channelName || "C").charAt(0);
                        })()}
                      </Avatar>
                      <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
                    </div>
                    <div className="flex flex-col overflow-hidden">
                      <Text style={{ fontWeight: ch.hasUnread ? 700 : 500 }} ellipsis>
                        {(() => {
                          // For direct channels, show other participant's name
                          if (ch.channelType === 'DIRECT_MESSAGE' && ch.participants && ch.participants.length === 2) {
                            const otherParticipant = ch.participants.find(p => (p.userId || p.id) !== userId);
                            if (otherParticipant) {
                              return `${otherParticipant.firstname || ""} ${otherParticipant.lastname || ""}`.trim();
                            }
                          }
                          // For group channels, show channel name
                          return ch.channelName || "Channel";
                        })()}
                      </Text>
                      <Text
                        type={ch.hasUnread ? undefined : "secondary"}
                        style={{
                          fontSize: '12px',
                          fontWeight: ch.hasUnread ? 600 : 400,
                          color: ch.hasUnread ? '#1890ff' : undefined
                        }}
                        ellipsis
                      >
                        {(() => {
                          if (!ch.messages || ch.messages.length === 0) return "No messages";
                          const lastMsg = ch.messages[ch.messages.length - 1];

                          if (lastMsg.type === 'DELETED') {
                            if (lastMsg.userId === userId) return "You deleted a message";

                            let name = lastMsg.senderName;
                            if (!name && ch.participants) {
                              const p = ch.participants.find(p => p.userId === lastMsg.userId);
                              if (p) name = p.firstname || p.name;
                            }
                            const displayName = name ? name.split(' ').pop() : 'User';
                            return `${displayName} deleted a message`;
                          }

                          let prefix = "";
                          const isDirectChannel = ch.channelType === 'DIRECT_MESSAGE';

                          // Only add prefix if NOT a notice
                          if (lastMsg.type !== 'NOTICE') {
                            if (lastMsg.userId === userId) {
                              prefix = "You: ";
                            } else if (!isDirectChannel) {
                              // Only show sender name in GROUP channels, not in DIRECT channels
                              let name = lastMsg.senderName;
                              // Fallback to finding name in participants if senderName is missing
                              if (!name && ch.participants) {
                                const p = ch.participants.find(p => p.userId === lastMsg.userId);
                                if (p) name = p.firstname || p.name;
                              }
                              if (name) {
                                // Use full name
                                prefix = `${name}: `;
                              }
                            }
                          }

                          // Handle different message types
                          if (lastMsg.type === 'IMAGE') return `${prefix}Sent an image`;
                          if (lastMsg.type === 'FILE') return `${prefix}Sent a file`;

                          // Truncate long messages
                          const content = lastMsg.content || "";
                          const truncated = content.length > 25 ? content.substring(0, 25) + "..." : content;
                          return `${prefix}${truncated}`;
                        })()}
                      </Text>
                    </div>
                  </div>

                  {/* Unread Indicator */}
                  {ch.hasUnread && (
                    <div className="flex-shrink-0 ml-2">
                      <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                    </div>
                  )}

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
        }}
      />
    </div >
  );
};

export default ConversationList;

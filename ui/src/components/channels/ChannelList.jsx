import React, { useEffect } from "react";
import { Avatar, List } from "antd";
import { useDispatch, useSelector } from "react-redux";
import { setCurrentChannel } from "@/stores/slices/channelSlice";
import { removeCurrentFriend } from "@/stores/slices/friendshipSlice";
import { fetchAllMembersOfChannel } from "@/stores/middlewares/channelMiddleware";
import { websocketService } from "@/utils/ws";

function ChannelList() {
  const dispatch = useDispatch();
  const { channels, currentChannelId } = useSelector((state) => {
    return state.channel
  });

  useEffect(() => {
    if (currentChannelId) {
      dispatch(fetchAllMembersOfChannel(currentChannelId));
    }
  }, [currentChannelId, dispatch]);


  const onSelectChannel = (channel) => {
    dispatch(setCurrentChannel(channel));
    dispatch(removeCurrentFriend());
  };

  return (
    <div style={{ padding: "10px" }}>
      <List
        dataSource={channels}
        renderItem={(item) => (
          <List.Item
            key={item.id}
            onClick={() => onSelectChannel(item)}
            style={{
              cursor: "pointer",
              backgroundColor:
                currentChannelId === item.id ? "#e6f7ff" : "transparent",
              padding: "8px",
              borderRadius: "4px",
            }}
          >
            <List.Item.Meta
              avatar={<Avatar>{item.channelName[0]}</Avatar>}
              title={item.channelName}
              description={`Created: ${new Date(
                item.createdAt
              ).toLocaleDateString()}`}
            />
          </List.Item>
        )}
      />
    </div>
  );
}

export default ChannelList;

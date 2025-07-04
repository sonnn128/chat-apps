import React, { useState } from "react";
import { Input, Button, Tooltip } from "antd";
import { SendOutlined } from "@ant-design/icons";
import { useSelector } from "react-redux";
import { stompClient } from "@/utils/ws";
import { errorToast } from "@/utils/toast";

const ChatInput = () => {
  const [message, setMessage] = useState("");
  const { currentChannelId } = useSelector((state) => state.channel);
  const { user } = useSelector((state) => state.auth);

  const handleSendMessage = () => {
    if (!currentChannelId) {
      errorToast("Please select a channel first");
      return;
    }
    if (message.trim()) {
      const messageSend = {
        key: { channelId: currentChannelId },
        userId: user.id,
        content: message,
        type: "CHAT",
        timestamp: Date.now(),
      };
      try {
        stompClient.publish({
          destination: `/app/channels/${currentChannelId}`,
          body: JSON.stringify(messageSend),
        });
        setMessage("");
      } catch (error) {
        console.error("Error sending message: ", error);
        errorToast("Failed to send message");
      }
    }
  };

  return (
    <div className="p-3 border-t flex items-center bg-white">
      <Input
        placeholder="Aa"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onPressEnter={handleSendMessage}
        style={{
          borderRadius: "9999px",
          backgroundColor: "#f0f2f5",
          border: "none",
          padding: "8px 16px",
        }}
      />
      <Tooltip title="Send">
        <Button
          type="primary"
          shape="circle"
          icon={<SendOutlined />}
          onClick={handleSendMessage}
          style={{ marginLeft: 8 }}
        />
      </Tooltip>
    </div>
  );
};

export default ChatInput;
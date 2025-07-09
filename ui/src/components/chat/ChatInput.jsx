import React, { useState } from "react";
import { Input, Button, Tooltip } from "antd";
import { SendOutlined } from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { sendChannelMessage } from "@/stores/middlewares/channelMiddleware";

const ChatInput = () => {
  const [message, setMessage] = useState("");
  const { channels, currentChannelId } = useSelector((state) => state.channel);
  const dispatch = useDispatch();
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (message.trim() === "") return;
    const form = {
      channelId: currentChannelId,
      content: message,
      type: "CHAT",
    };
    // push to topic particapants subsribes
    await dispatch(sendChannelMessage(form));
    setMessage("");
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="p-3 border-t flex items-center bg-white"
    >
      <Input
        placeholder="Aa"
        value={message}
        style={{
          borderRadius: "9999px",
          backgroundColor: "#f0f2f5",
          border: "none",
          padding: "8px 16px",
        }}
        onChange={(e) => setMessage(e.target.value)}
      />
      <Tooltip title="Send">
        <Button
          type="primary"
          shape="circle"
          icon={<SendOutlined />}
          style={{ marginLeft: 8 }}
          htmlType="submit"
        />
      </Tooltip>
    </form>
  );
};

export default ChatInput;

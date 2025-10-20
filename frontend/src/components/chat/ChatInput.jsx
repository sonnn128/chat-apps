import React, { useState, useRef, useEffect } from "react";
import { Input, Button, Tooltip } from "antd";
import {
  SendOutlined,
  SmileOutlined,
  PictureOutlined,
  AudioOutlined,
  PlayCircleOutlined,
  FileOutlined,
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { sendChannelMessage } from "@/stores/middlewares/channelMiddleware";
import data from "@emoji-mart/data";
import Picker from "@emoji-mart/react";

const ChatInput = () => {
  const [message, setMessage] = useState("");
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const { currentChannelId } = useSelector((state) => state.channel);
  const dispatch = useDispatch();
  const emojiPickerRef = useRef(null);
  const fileInputRef = useRef(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (message.trim() === "") return;

    // Check emoji-only
    const isEmojiOnly = (text) => {
      const cleanText = text.trim();
      if (cleanText === "") return false;
      const emojiOnlyText = cleanText.replace(
        /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F900}-\u{1F9FF}\u{1F018}-\u{1F270}\u{238C}-\u{2454}\u{20D0}-\u{20FF}\u{FE0F}\u{200D}\u{1F3FB}-\u{1F3FF}\u{1F9B0}-\u{1F9B3}]/gu,
        ""
      );
      return emojiOnlyText.trim() === "";
    };

    let messageType = "CHAT";
    if (isEmojiOnly(message)) {
      messageType = "EMOJI";
      console.log("Emoji-only message detected:", message);
    }

    const form = {
      channelId: currentChannelId,
      content: message,
      type: messageType,
    };

    console.log("Sending message:", form);
    await dispatch(sendChannelMessage(form));
    setMessage("");
  };

  const handleEmojiSelect = (emoji) => {
    setMessage((prev) => prev + emoji.native);
    setShowEmojiPicker(false);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      console.log("File selected:", file);
    }
  };

  const handleImageUpload = () => fileInputRef.current?.click();
  const handleGIFClick = () => console.log("GIF picker clicked");
  const handleVoiceMessage = () => console.log("Voice message clicked");

  // Close emoji picker when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        emojiPickerRef.current &&
        !emojiPickerRef.current.contains(event.target)
      ) {
        setShowEmojiPicker(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative">
      <form
        onSubmit={handleSubmit}
        className="p-3 border-t flex items-center gap-2 bg-white"
      >
        {/* Left side icons */}
        <div className="flex items-center gap-2">
          <Tooltip title="Voice message">
            <Button
              type="text"
              shape="circle"
              icon={<AudioOutlined />}
              onClick={handleVoiceMessage}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          <Tooltip title="Send photo">
            <Button
              type="text"
              shape="circle"
              icon={<PictureOutlined />}
              onClick={handleImageUpload}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          <Tooltip title="Send GIF">
            <Button
              type="text"
              shape="circle"
              icon={<PlayCircleOutlined />}
              onClick={handleGIFClick}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          <Tooltip title="Send file">
            <Button
              type="text"
              shape="circle"
              icon={<FileOutlined />}
              onClick={() => fileInputRef.current?.click()}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept="image/*,video/*,.pdf,.doc,.docx,.txt"
          onChange={handleFileUpload}
          className="hidden"
        />

        <Input
          placeholder="Aa"
          value={message}
          style={{
            borderRadius: "9999px",
            backgroundColor: "#f0f2f5",
            border: "none",
            padding: "12px 20px",
            fontSize: "15px",
            flex: 1,
          }}
          onChange={(e) => setMessage(e.target.value)}
        />

        {/* Right side icons */}
        <div className="flex items-center gap-2">
          <Tooltip title="Choose an emoji">
            <Button
              type="text"
              shape="circle"
              icon={<SmileOutlined />}
              onClick={() => setShowEmojiPicker(!showEmojiPicker)}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          <Tooltip title="Send">
            <Button
              type="primary"
              shape="circle"
              icon={<SendOutlined />}
              htmlType="submit"
              className="bg-blue-500 hover:bg-blue-600 transition-colors"
              size="large"
            />
          </Tooltip>
        </div>
      </form>

      {showEmojiPicker && (
        <div
          ref={emojiPickerRef}
          className="absolute bottom-full right-0 mb-2 z-50"
        >
          <div className="bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden">
            <Picker data={data} onEmojiSelect={handleEmojiSelect} theme="light" />
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatInput;

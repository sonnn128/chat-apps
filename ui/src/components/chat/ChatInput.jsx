import React, { useState, useRef } from "react";
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
import EmojiPicker from "emoji-picker-react";

const ChatInput = () => {
  const [message, setMessage] = useState("");
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const { channels, currentChannelId } = useSelector((state) => state.channel);
  const dispatch = useDispatch();
  const emojiPickerRef = useRef(null);
  const fileInputRef = useRef(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (message.trim() === "") return;
    
    // Function to check if message contains only emojis (1 or multiple)
    const isEmojiOnly = (text) => {
      const cleanText = text.trim();
      if (cleanText === "") return false;
      const emojiOnlyText = cleanText.replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F900}-\u{1F9FF}\u{1F018}-\u{1F270}\u{238C}-\u{2454}\u{20D0}-\u{20FF}\u{FE0F}\u{200D}\u{FE0F}\u{1F3FB}-\u{1F3FF}\u{1F9B0}-\u{1F9B3}]/gu, '');
      return emojiOnlyText.trim() === "";
    };

    // Determine message type based on content
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
    
    console.log("Sending message with type:", messageType, "Content:", message);
    
    await dispatch(sendChannelMessage(form));
    setMessage("");
  };

  const handleEmojiClick = (emojiObject) => {
    console.log("Emoji clicked:", emojiObject); // Thêm log này
    setMessage((prevMessage) => prevMessage + emojiObject.emoji);
    setShowEmojiPicker(false);
  };

  const handleEmojiButtonClick = () => {
    console.log("Emoji button clicked, current state:", showEmojiPicker); // Thêm log này
    setShowEmojiPicker(!showEmojiPicker);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Handle file upload logic here
      console.log("File selected:", file);
    }
  };

  const handleImageUpload = () => {
    fileInputRef.current?.click();
  };

  const handleGIFClick = () => {
    // Handle GIF picker logic here
    console.log("GIF picker clicked");
  };

  const handleVoiceMessage = () => {
    // Handle voice message logic here
    console.log("Voice message clicked");
  };

  // Close emoji picker when clicking outside
  React.useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        emojiPickerRef.current &&
        !emojiPickerRef.current.contains(event.target)
      ) {
        setShowEmojiPicker(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className="relative">
      <form
        onSubmit={handleSubmit}
        className="p-3 border-t flex items-center gap-2 bg-white"
      >
        {/* Left side icons */}
        <div className="flex items-center gap-2">
          {/* Microphone icon */}
          <Tooltip title="Voice message" overlayClassName="custom-tooltip">
            <Button
              type="text"
              shape="circle"
              icon={<AudioOutlined />}
              onClick={handleVoiceMessage}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          {/* Picture/Gallery icon */}
          <Tooltip title="Send photo" overlayClassName="custom-tooltip">
            <Button
              type="text"
              shape="circle"
              icon={<PictureOutlined />}
              onClick={handleImageUpload}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          {/* GIF icon */}
          <Tooltip title="Send GIF" overlayClassName="custom-tooltip">
            <Button
              type="text"
              shape="circle"
              icon={<PlayCircleOutlined />}
              onClick={handleGIFClick}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          {/* File attachment icon */}
          <Tooltip title="Send file" overlayClassName="custom-tooltip">
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

        {/* Hidden file input */}
        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept="image/*,video/*,.pdf,.doc,.docx,.txt"
          onChange={handleFileUpload}
          className="hidden"
        />

        {/* Message input */}
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
          {/* Emoji button */}
          <Tooltip title="Choose an emoji" overlayClassName="custom-tooltip">
            <Button
              type="text"
              shape="circle"
              icon={<SmileOutlined />}
              onClick={handleEmojiButtonClick}
              className="text-blue-500 hover:bg-blue-50 transition-colors"
              size="large"
            />
          </Tooltip>

          {/* Send button */}
          <Tooltip title="Send" overlayClassName="custom-tooltip">
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

      {/* Emoji Picker */}
      {showEmojiPicker && (
        <div
          ref={emojiPickerRef}
          className="absolute bottom-full right-0 mb-2 z-50"
        >
          <div className="bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden">
            <EmojiPicker
              onEmojiClick={handleEmojiClick}
              autoFocusSearch={false}
              searchDisabled={false}
              width={350}
              height={400}
              lazyLoadEmojis={true}
              searchPlaceholder="Search emoji..."
              previewConfig={{
                showPreview: false,
              }}
              suggestedEmojisMode="recent"
              skinTonePickerLocation="SEARCH"
              emojiStyle="native"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatInput;

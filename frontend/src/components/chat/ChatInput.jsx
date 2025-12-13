import React, { useState, useRef, useEffect } from "react";
import { Input, Button, Tooltip } from "antd";
import {
  SendOutlined,
  SmileOutlined,
  PictureOutlined,
  AudioOutlined,
  PlayCircleOutlined,
  FileOutlined,
  CloseOutlined,
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { sendChannelMessage } from "@/stores/middlewares/channelMiddleware";
import { addPendingMessage } from "@/stores/slices/channelSlice";
import data from "@emoji-mart/data";
import Picker from "@emoji-mart/react";

const ChatInput = () => {
  const [message, setMessage] = useState("");
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const { currentChannelId } = useSelector((state) => state.channel);
  const dispatch = useDispatch();
  const emojiPickerRef = useRef(null);
  const fileInputRef = useRef(null);

  const { user } = useSelector((state) => state.auth);
  const userId = user?.data?.id;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (message.trim() === "" && selectedFiles.length === 0) return;

    // 1. Send text message if exists
    if (message.trim() !== "") {
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
      }

      const tempId = crypto.randomUUID();
      const form = {
        channelId: currentChannelId,
        content: message,
        type: messageType,
        tempId,
        userId
      };
      dispatch(sendChannelMessage(form));
      setMessage("");
    }

    // 2. Upload and send files
    if (selectedFiles.length > 0) {
      const filesToProcess = [...selectedFiles];
      setSelectedFiles([]); // Clear UI immediately

      const mediaService = (await import("@/services/mediaService")).default;

      // Process files sequentially or parallel
      for (const fileObj of filesToProcess) {
        try {
          // Create a tempId for the file message
          const tempId = crypto.randomUUID();

          // Optimistically add file message with local preview
          const pendingContent = fileObj.type === "FILE"
            ? JSON.stringify({ url: "#", name: fileObj.file.name, size: fileObj.file.size })
            : fileObj.previewUrl;

          const pendingMessage = {
            key: {
              channelId: currentChannelId,
              messageId: tempId,
              timestamp: new Date().toISOString()
            },
            userId: userId || "current-user",
            content: pendingContent,
            type: fileObj.type,
            status: "pending",
            senderName: user?.data?.firstname + " " + user?.data?.lastname,
            senderAvatar: user?.data?.avatarUrl
          };

          dispatch(addPendingMessage(pendingMessage));

          const res = await mediaService.uploadFile(fileObj.file);
          if (res.success) {
            const { secureUrl, originalFileName, fileSize } = res.data;

            let content = JSON.stringify({ url: secureUrl, name: originalFileName, size: fileSize });
            if (fileObj.type === "IMAGE" || fileObj.type === "VIDEO") {
              content = secureUrl;
            }

            const form = {
              channelId: currentChannelId,
              content: content,
              type: fileObj.type,
              tempId,
              userId
            };
            dispatch(sendChannelMessage(form));
          } else {
            // Handle upload failure - mark message as failed
            // We can dispatch a rejected action or manually update status
            // For now, let's just log it. Ideally we should update the message status to failed.
            console.error("Upload failed for file:", fileObj.file.name);
          }
        } catch (error) {
          console.error("Failed to upload file:", fileObj.file.name, error);
        }
      }
    }
  };

  const handleEmojiSelect = (emoji) => {
    setMessage((prev) => prev + emoji.native);
    setShowEmojiPicker(false);
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;

    const newFiles = files.map((file) => ({
      file,
      previewUrl: URL.createObjectURL(file),
      type: file.type.startsWith("image/")
        ? "IMAGE"
        : file.type.startsWith("video/")
          ? "VIDEO"
          : "FILE",
    }));

    setSelectedFiles((prev) => [...prev, ...newFiles]);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const removeFile = (index) => {
    setSelectedFiles((prev) => {
      const newFiles = [...prev];
      URL.revokeObjectURL(newFiles[index].previewUrl);
      newFiles.splice(index, 1);
      return newFiles;
    });
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
    <div className="relative flex flex-col">
      {/* File Preview Area */}
      {selectedFiles.length > 0 && (
        <div className="flex gap-2 p-2 bg-gray-50 overflow-x-auto">
          {selectedFiles.map((file, index) => (
            <div key={index} className="relative group flex-shrink-0">
              {file.type === "IMAGE" ? (
                <img
                  src={file.previewUrl}
                  alt="preview"
                  className="h-20 w-20 object-cover rounded-lg border border-gray-200"
                />
              ) : file.type === "VIDEO" ? (
                <video
                  src={file.previewUrl}
                  className="h-20 w-20 object-cover rounded-lg border border-gray-200"
                />
              ) : (
                <div className="h-20 w-20 flex flex-col items-center justify-center bg-white rounded-lg border border-gray-200 p-1">
                  <FileOutlined style={{ fontSize: "24px", color: "#1890ff" }} />
                  <span className="text-xs text-gray-500 truncate w-full text-center mt-1">
                    {file.file.name}
                  </span>
                </div>
              )}
              <button
                onClick={() => removeFile(index)}
                className="absolute -top-2 -right-2 bg-gray-200 hover:bg-gray-300 text-gray-600 rounded-full p-1 shadow-sm transition-colors"
                type="button"
              >
                <CloseOutlined style={{ fontSize: "12px" }} />
              </button>
            </div>
          ))}
        </div>
      )}
      <form
        onSubmit={handleSubmit}
        className="p-3 flex items-center gap-2 bg-white"
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
          onChange={handleFileSelect}
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

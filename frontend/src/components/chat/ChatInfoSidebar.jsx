import { useDispatch, useSelector } from "react-redux";
import ChannelMembers from "../ChannelMembers";
import React, { useState, useRef } from "react";
import { Avatar, message, Spin } from "antd";
import { User, Bell, Search, ChevronRight, Lock, UserPlus, Edit3, Image as ImageIcon, Palette, Smile, Type } from "lucide-react";
import channelService from "@/services/channelService";
import { updateChannel } from "@/stores/slices/channelSlice";

const ChatInfoSidebar = () => {
  const { currentChannel, channels } = useSelector((state) => state.channel);
  const [showMembers, setShowMembers] = useState(false);
  const [showCustomizeChat, setShowCustomizeChat] = useState(false);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);
  const dispatch = useDispatch();

  const menuItems = [
    { label: "Chat info", key: "chatInfo" },
    { label: "Customize chat", key: "customizeChat" },
    { label: "Chat members", key: "chatMembers" },
    { label: "Media, files and links", key: "mediaFiles" },
    { label: "Privacy & support", key: "privacySupport" },
  ];

  const actionButtons = [
    { icon: <User size={20} />, label: "Profile" },
    { icon: <Bell size={20} />, label: "Mute" },
    { icon: <Search size={20} />, label: "Search" },
  ];

  const handleMenuItemClick = (key) => {
    if (key === "chatMembers") {
      setShowMembers(!showMembers);
    } else if (key === "customizeChat") {
      setShowCustomizeChat(!showCustomizeChat);
    } else {
      console.log(`Clicked on: ${key}`);
    }
  };

  const handlePhotoChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setLoading(true);
    try {
      // 1. Upload image
      const mediaService = (await import("@/services/mediaService")).default;
      const uploadRes = await mediaService.uploadFile(file);

      if (uploadRes.success) {
        const avatarUrl = uploadRes.data.secureUrl;

        // 2. Update channel
        const updateRes = await channelService.updateChannelAvatar(currentChannel.id, avatarUrl);

        if (updateRes.success) {
          // 3. Update Redux state
          dispatch(updateChannel(updateRes.data));
          message.success("Channel photo updated successfully");
        } else {
          message.error("Failed to update channel photo");
        }
      } else {
        message.error("Failed to upload image");
      }
    } catch (error) {
      console.error("Error updating channel photo:", error);
      message.error("An error occurred while updating channel photo");
    } finally {
      setLoading(false);
    }
  };

  if (!currentChannel) {
    return <div className="w-[420px] flex-shrink-0 bg-white border-l border-gray-200 h-full flex items-center justify-center">Select a channel</div>;
  }

  return (
    <div className="w-[420px] flex-shrink-0 bg-white text-gray-800 border-l border-gray-200 flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-4">
        <div className="flex flex-col items-center text-center">
          {loading ? (
            <Spin size="large">
              <Avatar size={80} src={currentChannel.avatar}>
                {currentChannel.channelName ? currentChannel.channelName[0] : "C"}
              </Avatar>
            </Spin>
          ) : (
            <Avatar size={80} src={currentChannel.avatar}>
              {currentChannel.channelName ? currentChannel.channelName[0] : "C"}
            </Avatar>
          )}
          <h2 className="mt-4 text-xl font-bold text-gray-900">
            {currentChannel.channelName || "Channel Name"}
          </h2>
          <p className="text-gray-500 text-sm">@channel_tag</p>

          <div className="mt-2 flex items-center gap-2 bg-gray-100 text-gray-600 px-3 py-1 rounded-full text-xs">
            <Lock size={12} />
            <span>End-to-end encrypted</span>
          </div>
        </div>

        <div className="mt-6 flex justify-center gap-4">
          {actionButtons.map((action) => (
            <div
              key={action.label}
              className="flex flex-col items-center gap-1"
            >
              <button className="w-10 h-10 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full flex items-center justify-center">
                {action.icon}
              </button>
              <span className="text-xs text-gray-500">{action.label}</span>
            </div>
          ))}
        </div>

        <div className="mt-8">
          {menuItems.map((item) => (
            <div key={item.key}>
              <button
                onClick={() => handleMenuItemClick(item.key)}
                className="w-full flex justify-between items-center p-3 hover:bg-gray-100 rounded-lg"
              >
                <h2 className="text-sm font-semibold">{item.label}</h2>
                {item.key === "chatMembers" || item.key === "customizeChat" ? (
                  <ChevronRight
                    size={20}
                    className={`text-gray-400 transform transition-transform ${(item.key === "chatMembers" && showMembers) ||
                      (item.key === "customizeChat" && showCustomizeChat)
                      ? "rotate-90"
                      : ""
                      }`}
                  />
                ) : (
                  <ChevronRight size={20} className="text-gray-400" />
                )}
              </button>

              {item.key === "chatMembers" && showMembers && <ChannelMembers />}

              {item.key === "customizeChat" && showCustomizeChat && (
                <div className="pl-4 pr-2 pb-2">
                  <button className="w-full flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg text-sm text-gray-700">
                    <Edit3 size={18} className="text-gray-500" />
                    <span>Change chat name</span>
                  </button>
                  <button
                    className={`w-full flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg text-sm text-gray-700 ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                    onClick={() => !loading && fileInputRef.current?.click()}
                    disabled={loading}
                  >
                    <ImageIcon size={18} className="text-gray-500" />
                    <span>Change photo</span>
                  </button>
                  <input
                    type="file"
                    ref={fileInputRef}
                    className="hidden"
                    accept="image/*"
                    onChange={handlePhotoChange}
                  />
                  <button className="w-full flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg text-sm text-gray-700">
                    <Palette size={18} className="text-gray-500" />
                    <span>Change theme</span>
                  </button>
                  <button className="w-full flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg text-sm text-gray-700">
                    <Smile size={18} className="text-gray-500" />
                    <span>Change emoji</span>
                  </button>
                  <button className="w-full flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg text-sm text-gray-700">
                    <Type size={18} className="text-gray-500" />
                    <span>Edit nicknames</span>
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ChatInfoSidebar;

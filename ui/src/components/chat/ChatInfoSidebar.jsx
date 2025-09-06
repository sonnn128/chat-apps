import { useSelector } from "react-redux";
import ChannelMembers from "../ChannelMembers";
import React, { useState } from "react";
import { Avatar } from "antd";
import { User, Bell, Search, ChevronRight, Lock, UserPlus } from "lucide-react";

const ChatInfoSidebar = () => {
  const { currentChannel, channels } = useSelector((state) => state.channel);
  const [showMembers, setShowMembers] = useState(false);

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
    } else {
      console.log(`Clicked on: ${key}`);
    }
  };

  return (
    <div className="w-[420px] flex-shrink-0 bg-white text-gray-800 border-l border-gray-200 flex flex-col">
      <div className="flex-1 overflow-y-auto p-4">
        <div className="flex flex-col items-center text-center">
          <Avatar size={80}>A</Avatar>
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
                {item.key === "chatMembers" ? (
                  <ChevronRight
                    size={20}
                    className={`text-gray-400 transform transition-transform ${
                      showMembers ? "rotate-90" : ""
                    }`}
                  />
                ) : (
                  <ChevronRight size={20} className="text-gray-400" />
                )}
              </button>

              {item.key === "chatMembers" && showMembers && <ChannelMembers />}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ChatInfoSidebar;

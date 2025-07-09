import React from "react";
import { Avatar } from "antd";
import { User, Bell, Search, ChevronRight, Lock } from "lucide-react";
import { useSelector } from "react-redux";

const ChatInfoSidebar = () => {
  const { currentChannel } = useSelector((state) => state.channel);

  const menuItems = [
    { label: "Chat info" },
    { label: "Customize chat" },
    { label: "Media & files" },
    { label: "Privacy & support" },
  ];

  const actionButtons = [
    { icon: <User size={20} />, label: "Profile" },
    { icon: <Bell size={20} />, label: "Mute" },
    { icon: <Search size={20} />, label: "Search" },
  ];

  return (
    <div className="w-80 flex-shrink-0 bg-white text-gray-800 border-l border-gray-200 flex flex-col">
      <div className="flex-1 overflow-y-auto p-4">
        <div className="flex flex-col items-center text-center">
          <Avatar size={80}>A</Avatar>
          <h2 className="mt-4 text-xl font-bold text-gray-900">
            {currentChannel.channelName}
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
            <button
              key={item.label}
              className="w-full flex justify-between items-center p-3 hover:bg-gray-100 rounded-lg"
            >
              <span>{item.label}</span>
              <ChevronRight size={20} className="text-gray-400" />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ChatInfoSidebar;

import React, { useState } from "react";
import { Avatar } from "antd";
import { UserPlus } from "lucide-react";
import { useSelector } from "react-redux";
import AddPeopleModal from "./modals/AddPeopleModal";

const ChannelMembers = () => {
  const { currentChannel } = useSelector((state) => state.channel);
  const [isAddPeopleModalOpen, setIsAddPeopleModalOpen] = useState(false);
  
  const channelParticipants = currentChannel?.participants || [];

  return (
    <div className="pl-4 pr-2 pb-2">
      {channelParticipants.map((member) => {
        // Format ngày tháng năm
        const formattedDate = member.joinedAt
          ? new Date(member.joinedAt).toLocaleDateString("vi-VN")
          : null;

        // Get display name - use firstname + lastname if available, otherwise use generated name
        const getDisplayName = (member) => {
          if (member.firstname && member.lastname) {
            return `${member.firstname} ${member.lastname}`;
          }
          if (member.name && member.name !== `User ${member.userId.substring(0, 8)}`) {
            return member.name;
          }
          return `User ${member.userId.substring(0, 8)}`;
        };

        // Get role display with email
        const getRoleDisplay = (member) => {
          if (member.email && member.role) {
            return `${member.email} · Role: ${member.role}`;
          }
          if (member.role) {
            return `Role: ${member.role}`;
          }
          return '';
        };

        const displayName = getDisplayName(member);
        const roleDisplay = getRoleDisplay(member);

        return (
          <div
            key={member.userId}
            className="flex items-center gap-3 py-2 hover:bg-gray-50 rounded-lg px-2"
          >
            <Avatar src={member.avatar}>{displayName?.[0] || '?'}</Avatar>
            <div className="flex-1">
              <h3 className="text-sm font-medium text-gray-900">{displayName}</h3>
              {/* Hàng thông tin */}
              {roleDisplay && (
                <p className="text-xs text-gray-500">
                  {roleDisplay}
                </p>
              )}
              {/* Hàng join date */}
              {formattedDate && (
                <p className="text-xs text-gray-400 mt-0.5">
                  Joined at: {formattedDate}
                </p>
              )}
            </div>
            <button className="text-gray-400 hover:text-gray-600">
              &#8226;&#8226;&#8226;
            </button>
          </div>
        );
      })}

      <button 
        onClick={() => setIsAddPeopleModalOpen(true)}
        className="w-full flex items-center gap-3 py-3 hover:bg-gray-50 rounded-lg px-2 mt-2"
      >
        <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
          <UserPlus size={20} className="text-gray-600" />
        </div>
        <span className="text-sm font-medium text-blue-600">Add people</span>
      </button>

      {/* Add People Modal */}
      <AddPeopleModal
        open={isAddPeopleModalOpen}
        onClose={() => setIsAddPeopleModalOpen(false)}
        channelId={currentChannel?.id}
        channelName={currentChannel?.channelName}
        currentMembers={channelParticipants.map(member => member.userId)}
      />
    </div>
  );
};

export default ChannelMembers;

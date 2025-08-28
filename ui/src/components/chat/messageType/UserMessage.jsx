import Avatar from "antd/es/avatar/Avatar";

const UserMessage = ({ content, isCurrentUser, senderName = "Default", senderAvatar }) => (
  <div className={`flex items-start ${isCurrentUser ? "justify-end" : ""}`}>
    {!isCurrentUser && (
      <Avatar
        size={32}
        style={{ marginRight: 8 }}
        src={senderAvatar}
      >
        {senderName[0] || "U"}
      </Avatar>
    )}
    <div>
      {!isCurrentUser && (
        <div className="text-xs font-semibold text-white mb-1" style={{ marginLeft: 4 }}>
          {senderName}
        </div>
      )}
      <div
        className={`${
          isCurrentUser
            ? "bg-blue-500 text-white"
            : "bg-[#8e5cff] text-white"
        } p-2 rounded-2xl max-w-xs user-message`}
        style={{
          borderTopLeftRadius: isCurrentUser ? 16 : 4,
          borderTopRightRadius: isCurrentUser ? 4 : 16,
          marginLeft: !isCurrentUser ? 4 : 0,
        }}
      >
        <p className="text-sm mb-0">{content}</p>
      </div>
    </div>
  </div>
);

export default UserMessage;
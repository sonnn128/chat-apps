const UserMessage = ({ content, isCurrentUser }) => (
  <div className={`flex items-start ${isCurrentUser ? "justify-end" : ""}`}>
    {!isCurrentUser && (
      <Avatar size={32} style={{ marginRight: 8 }}>
        U
      </Avatar>
    )}
    <div
      className={`${
        isCurrentUser ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-900"
      } p-2 rounded-lg max-w-xs`}
    >
      <p className="text-sm">{content}</p>
    </div>
  </div>
);
export default UserMessage;

import React from "react";
import { Modal, Button, Avatar, Typography } from "antd";

const { Text, Title } = Typography;

const FriendSuggestionsModal = ({ open, onClose, suggestions, onAddFriend }) => {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={[
        <Button key="close" type="primary" onClick={onClose} block>
          Close
        </Button>,
      ]}
      title={<Title level={5}>Friend Suggestions</Title>}
      bodyStyle={{ maxHeight: "400px", overflowY: "auto", paddingTop: 0 }}
    >
      {suggestions?.length > 0 ? (
        suggestions.map((user) => (
          <div
            key={user.id}
            className="flex items-center p-3 rounded-lg cursor-pointer hover:bg-gray-100"
          >
            <Avatar
              size={40}
              src={user.avatar || ""}
              alt={user.username}
            />
            <div className="ml-3 flex-1 min-w-0">
              <p className="font-semibold text-gray-900 truncate">
                {user.firstname + " " + user.lastname}
              </p>
              <p className="text-sm text-gray-600 truncate">
                {user.email || "No email"}
              </p>
            </div>
            <Button
              type="default"
              size="small"
              onClick={() => onAddFriend(user.id)}
              className="ml-2"
            >
              Add Friend
            </Button>
          </div>
        ))
      ) : (
        <Text type="secondary" style={{ textAlign: "center", display: "block", padding: "16px 0" }}>
          No suggestions
        </Text>
      )}
    </Modal>
  );
};

export default FriendSuggestionsModal;

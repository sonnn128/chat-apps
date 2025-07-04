import React from "react";
import { Modal, Button, Typography, List, Avatar } from "antd";

const { Title, Text } = Typography;

const FriendRequestsModal = ({ open, onClose, requests, onAccept }) => {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      title={<Title level={5}>Friend Requests</Title>}
      footer={[
        <Button key="close" type="primary" onClick={onClose} block>
          Close
        </Button>,
      ]}
      bodyStyle={{ maxHeight: "400px", overflowY: "auto", paddingTop: 0 }}
    >
      {requests?.length ? (
        <List
          itemLayout="horizontal"
          dataSource={requests}
          renderItem={(request) => (
            <List.Item
              key={request.id}
              actions={[
                <Button
                  type="default"
                  size="small"
                  onClick={() => onAccept(request.id)}
                >
                  Accept
                </Button>,
              ]}
            >
              <List.Item.Meta
                avatar={
                  <Avatar>
                    {request.firstname?.charAt(0).toUpperCase() || "U"}
                  </Avatar>
                }
                title={`${request.firstname} ${request.lastname}`}
                description={request.email}
              />
            </List.Item>
          )}
        />
      ) : (
        <Text type="secondary" style={{ textAlign: "center", display: "block", padding: "16px 0" }}>
          No pending requests
        </Text>
      )}
    </Modal>
  );
};

export default FriendRequestsModal;

import React from "react";
import { Modal, Button } from "antd";
import { LogoutOutlined, ExclamationCircleOutlined } from "@ant-design/icons";

function LeaveGroupModal({ visible, onConfirm, onCancel, groupName }) {
  return (
    <Modal
      title={
        <span style={{ color: "#faad14" }}>
          <ExclamationCircleOutlined style={{ marginRight: 8 }} /> Leave Group
        </span>
      }
      open={visible}
      onCancel={onCancel}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          Cancel
        </Button>,
        <Button key="leave" type="primary" danger onClick={onConfirm}>
          <LogoutOutlined style={{ marginRight: 8 }} /> Leave Group
        </Button>,
      ]}
      maskClosable={false}
      closable={false}
      width={400}
      styles={{ body: { textAlign: "center", padding: "30px 24px" } }}
    >
      <p style={{ fontSize: "16px", marginBottom: "20px" }}>
        Are you sure you want to leave the group "<strong>{groupName}</strong>"?
      </p>
      <p style={{ color: "#888" }}>
        You will no longer receive messages from this group.
      </p>
    </Modal>
  );
}

export default LeaveGroupModal;

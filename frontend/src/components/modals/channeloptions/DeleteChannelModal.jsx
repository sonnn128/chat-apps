import React from "react";
import { Modal, Button } from "antd";
import { DeleteOutlined } from "@ant-design/icons";

function DeleteChannelModal({ visible, onConfirm, onCancel, channelName }) {
  return (
    <Modal
      title={
        <span style={{ color: "#ff4d4f" }}>
          <DeleteOutlined style={{ marginRight: 8 }} /> Delete Channel
        </span>
      }
      open={visible}
      onCancel={onCancel}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          Cancel
        </Button>,
        <Button key="delete" type="primary" danger onClick={onConfirm}>
          Delete Channel
        </Button>,
      ]}
      maskClosable={false}
      closable={false}
      width={400}
      bodyStyle={{ textAlign: "center", padding: "30px 24px" }}
    >
      <p style={{ fontSize: "16px", marginBottom: "20px" }}>
        Are you sure you want to delete the channel "
        <strong>{channelName}</strong>"?
      </p>
      <p style={{ color: "#888" }}>
        Once you delete this channel, it cannot be undone.
      </p>
    </Modal>
  );
}

export default DeleteChannelModal;

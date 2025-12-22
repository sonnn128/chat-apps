import React, { useState, useEffect } from "react";
import { Modal, Button, Input } from "antd";
import { EditOutlined } from "@ant-design/icons";

function ChangeChatNameModal({ open, onConfirm, onCancel, currentName, loading }) {
    const [name, setName] = useState(currentName);

    useEffect(() => {
        setName(currentName);
    }, [currentName, open]);

    return (
        <Modal
            title={
                <span>
                    <EditOutlined style={{ marginRight: 8 }} /> Change Chat Name
                </span>
            }
            open={open}
            onCancel={onCancel}
            footer={[
                <Button key="cancel" onClick={onCancel}>
                    Cancel
                </Button>,
                <Button
                    key="save"
                    type="primary"
                    onClick={() => onConfirm(name)}
                    loading={loading}
                    disabled={!name || name.trim() === ""}
                >
                    Save
                </Button>,
            ]}
            maskClosable={false}
            width={400}
            styles={{ body: { padding: "24px" } }}
        >
            <p style={{ marginBottom: "8px", fontWeight: 500 }}>Chat Name</p>
            <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter new chat name"
                maxLength={50}
                showCount
            />
            <p style={{ color: "#888", fontSize: "12px", marginTop: "12px" }}>
                Changing the name of a group chat changes it for everyone.
            </p>
        </Modal>
    );
}

export default ChangeChatNameModal;

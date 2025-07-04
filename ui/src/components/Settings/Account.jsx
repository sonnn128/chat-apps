import React, { useState } from "react";
import { motion } from "framer-motion";
import { Typography, Avatar, Button, Input, Switch, Divider, List, Modal } from "antd";
import { LogoutOutlined, SaveOutlined, CloseOutlined } from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "@/stores/slices/authSlice";

const { Title, Text } = Typography;

const Account = () => {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const [isEditing, setIsEditing] = useState(false);

  const handleLogout = () => {
    dispatch(logout());
  };

  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
    >
      <div style={{ padding: 32 }}>
        <Title level={4} style={{ marginBottom: 24 }}>
          Account Settings
        </Title>

        <div style={{ display: "flex", alignItems: "center", gap: 24, marginBottom: 32 }}>
          <Avatar size={80} style={{ backgroundColor: "#FF6F61" }}>
            {user?.firstname?.[0]}
            {user?.lastname?.[0]}
          </Avatar>

          <div style={{ flex: 1 }}>
            {isEditing ? (
              <>
                <Input placeholder="First Name" name="firstname" style={{ marginBottom: 12 }} />
                <Input placeholder="Last Name" name="lastname" />
              </>
            ) : (
              <>
                <Text strong style={{ display: "block", fontSize: 16, marginBottom: 8 }}>
                  {user?.firstname} {user?.lastname}
                </Text>
                <Text type="secondary" style={{ display: "block" }}>
                  <strong>Email:</strong> {user?.email}
                </Text>
                <Text type="secondary" style={{ display: "block", marginTop: 4 }}>
                  <strong>Username:</strong> {user?.username || "N/A"}
                </Text>
                <Text type="secondary" style={{ display: "block", marginTop: 4 }}>
                  <strong>Roles:</strong> {user?.roles?.join(", ") || "No roles assigned"}
                </Text>
              </>
            )}
          </div>
        </div>

        <div style={{ display: "flex", gap: 16 }}>
          {isEditing ? (
            <>
              <Button type="primary" icon={<SaveOutlined />}>Save</Button>
              <Button onClick={() => setIsEditing(false)} icon={<CloseOutlined />}>Cancel</Button>
            </>
          ) : (
            <>
              <Button type="primary" onClick={() => setIsEditing(true)}>Edit Profile</Button>
              <Button danger icon={<LogoutOutlined />} onClick={handleLogout}>Log Out</Button>
            </>
          )}
        </div>
      </div>
    </motion.div>
  );
};

export default Account;
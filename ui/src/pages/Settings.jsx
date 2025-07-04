import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  Button,
  Typography,
  Menu,
  Layout,
  Space,
} from "antd";
import { UserOutlined, NotificationOutlined, LeftOutlined } from "@ant-design/icons";
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "@/stores/slices/authSlice";
import { successToast } from "@/utils/toast";
import Account from "@/components/Settings/Account";
import NotificationsSection from "@/components/Settings/Notifications";

const { Sider, Content } = Layout;
const { Title } = Typography;

const sections = [
  { name: "Account", icon: <UserOutlined /> },
  { name: "Notifications", icon: <NotificationOutlined /> },
];

function Settings() {
  const [selectedSection, setSelectedSection] = useState("Account");
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
    successToast("Log out successfully");
  };

  const renderContent = () => {
    switch (selectedSection) {
      case "Account":
        return <Account user={user} handleLogout={handleLogout} />;
      case "Notifications":
        return <NotificationsSection />;
      case "Privacy":
        return <Privacy />;
      default:
        return null;
    }
  };

  return (
    <Layout style={{ minHeight: "100vh", backgroundColor: "#f0f2f5" }}>
      <motion.div
        initial={{ x: -500 }}
        animate={{ x: 0 }}
        transition={{ type: "spring", stiffness: 100 }}
      >
        <Sider
          width={360}
          style={{
            backgroundColor: "white",
            borderRight: "1px solid #f0f0f0",
            boxShadow: "0 1px 2px rgba(0,0,0,0.1)",
          }}
        >
          <div
            style={{
              padding: 16,
              borderBottom: "1px solid #f0f0f0",
              display: "flex",
              alignItems: "center",
              gap: 8,
            }}
          >
            <Link to="/">
              <Button
                type="text"
                icon={<LeftOutlined />}
                style={{ color: "#050505", padding: 4 }}
              />
            </Link>
            <Title level={4} style={{ margin: 0, color: "#050505" }}>
              Settings
            </Title>
          </div>

          <Menu
            mode="vertical"
            selectedKeys={[selectedSection]}
            onClick={({ key }) => setSelectedSection(key)}
            style={{ border: "none", backgroundColor: "transparent" }}
          >
            {sections.map((section) => (
              <Menu.Item
                key={section.name}
                icon={section.icon}
                style={{
                  padding: "12px 24px",
                  backgroundColor:
                    selectedSection === section.name ? "#e7f3ff" : "transparent",
                  color: "#050505",
                  fontWeight: selectedSection === section.name ? "bold" : "normal",
                  transition: "background-color 0.2s",
                }}
              >
                {section.name}
              </Menu.Item>
            ))}
          </Menu>
        </Sider>
      </motion.div>

      <Content
        style={{
          backgroundColor: "white",
          padding: 24,
          borderLeft: "1px solid #f0f0f0",
          overflowY: "auto",
        }}
      >
        {renderContent()}
      </Content>
    </Layout>
  );
}

export default Settings;

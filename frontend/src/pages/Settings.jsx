import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  Button,
  Typography,
  Menu,
  Layout,
  Space,
  theme,
} from "antd";
import { UserOutlined, NotificationOutlined, LeftOutlined, SecurityScanOutlined, BgColorsOutlined } from "@ant-design/icons";
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "@/stores/slices/authSlice";
import { successToast } from "@/utils/toast";
import Account from "@/components/Settings/Account";
import NotificationsSection from "@/components/Settings/Notifications";
import Appearance from "@/components/Settings/Appearance";

const { Sider, Content } = Layout;
const { Title } = Typography;

const sections = [
  { name: "Account", icon: <UserOutlined /> },
  { name: "Appearance", icon: <BgColorsOutlined /> },
  { name: "Notifications", icon: <NotificationOutlined /> },
  { name: "Privacy", icon: <SecurityScanOutlined /> },
];

function Settings() {
  const [selectedSection, setSelectedSection] = useState("Account");
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const { token } = theme.useToken();

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
    successToast("Log out successfully");
  };

  const renderContent = () => {
    switch (selectedSection) {
      case "Account":
        return <Account user={user} handleLogout={handleLogout} />;
      case "Appearance":
        return <Appearance />;
      case "Notifications":
        return <NotificationsSection />;
      case "Privacy":
        return (
          <div style={{ padding: 24 }}>
            <Title level={2}>Privacy Settings</Title>
            <p>Content for privacy settings goes here.</p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <Layout style={{ minHeight: "100vh", backgroundColor: token.colorBgLayout }}>
      <motion.div
        initial={{ x: -500 }}
        animate={{ x: 0 }}
        transition={{ type: "spring", stiffness: 100, damping: 20 }} // Điều chỉnh transition mượt mà hơn
      >
        <Sider
          width={280}
          style={{
            backgroundColor: token.colorBgContainer,
            borderRight: "none",
            boxShadow: token.boxShadowSecondary,
            position: 'relative',
            zIndex: 1,
          }}
        >
          <div
            style={{
              padding: "20px 24px",
              borderBottom: `1px solid ${token.colorBorderSecondary}`,
              display: "flex",
              alignItems: "center",
              gap: 12, // Tăng khoảng cách
            }}
          >
            <Link to="/">
              <Button
                type="text"
                icon={<LeftOutlined style={{ fontSize: '18px', color: '#595959' }} />} // Kích thước và màu sắc icon
                style={{
                  padding: "8px 10px", // Padding cho nút back
                  height: 'auto',
                  borderRadius: '8px', // Bo tròn nhẹ
                  "&:hover": {
                    backgroundColor: '#f0f2f5', // Hiệu ứng hover nhẹ
                  }
                }}
              />
            </Link>
            <Title level={3} style={{ margin: 0, color: token.colorText, fontWeight: 600 }}>
              Settings
            </Title>
          </div>

          <Menu
            mode="vertical"
            selectedKeys={[selectedSection]}
            onClick={({ key }) => setSelectedSection(key)}
            style={{
              border: "none",
              backgroundColor: "transparent",
              padding: "16px 0", // Padding cho Menu
            }}
          >
            {sections.map((section) => (
              <Menu.Item
                key={section.name}
                icon={React.cloneElement(section.icon, {
                  style: { fontSize: '18px', marginRight: '10px' } // Kích thước icon trong menu
                })}
                style={{
                  padding: "14px 24px",
                  margin: "4px 16px",
                  borderRadius: "8px",
                  fontWeight: selectedSection === section.name ? 600 : 500,
                  transition: "all 0.3s ease",
                  display: 'flex',
                  alignItems: 'center',
                }}
                className="settings-menu-item" // Thêm className để dễ dàng custom CSS nếu cần
              >
                {section.name}
              </Menu.Item>
            ))}
          </Menu>
        </Sider>
      </motion.div>

      <Content
        style={{
          backgroundColor: token.colorBgContainer,
          padding: 32, // Tăng padding cho phần nội dung
          borderLeft: "none", // Bỏ đường viền
          overflowY: "auto",
          flex: 1, // Đảm bảo content chiếm hết phần còn lại
        }}
      >
        {renderContent()}
      </Content>
    </Layout>
  );
}

export default Settings;
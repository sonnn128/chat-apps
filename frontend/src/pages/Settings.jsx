import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  Button,
  Typography,
  Menu,
  Layout,
  Space,
} from "antd";
import { UserOutlined, NotificationOutlined, LeftOutlined, SecurityScanOutlined } from "@ant-design/icons"; // Thêm SecurityScanOutlined cho Privacy
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "@/stores/slices/authSlice";
import { successToast } from "@/utils/toast";
import Account from "@/components/Settings/Account";
import NotificationsSection from "@/components/Settings/Notifications";
// Import Privacy component nếu có
// import Privacy from "@/components/Settings/Privacy";

const { Sider, Content } = Layout;
const { Title } = Typography;

const sections = [
  { name: "Account", icon: <UserOutlined /> },
  { name: "Notifications", icon: <NotificationOutlined /> },
  { name: "Privacy", icon: <SecurityScanOutlined /> }, // Thêm mục Privacy
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
        // Bạn cần tạo component Privacy riêng nếu muốn sử dụng
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
    <Layout style={{ minHeight: "100vh", backgroundColor: "#f9f9f9" }}> {/* Màu nền tổng thể nhẹ nhàng hơn */}
      <motion.div
        initial={{ x: -500 }}
        animate={{ x: 0 }}
        transition={{ type: "spring", stiffness: 100, damping: 20 }} // Điều chỉnh transition mượt mà hơn
      >
        <Sider
          width={280} // Chiều rộng sidebar vừa phải hơn
          style={{
            backgroundColor: "#ffffff", // Nền trắng tinh khôi
            borderRight: "none", // Bỏ đường viền để tạo cảm giác tối giản
            boxShadow: "0 4px 12px rgba(0,0,0,0.08)", // Bóng đổ tinh tế hơn
            position: 'relative', // Để bóng đổ hiển thị tốt
            zIndex: 1, // Đảm bảo sidebar nằm trên
          }}
        >
          <div
            style={{
              padding: "20px 24px", // Tăng padding
              borderBottom: "1px solid #e8e8e8", // Đường phân cách nhẹ nhàng
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
            <Title level={3} style={{ margin: 0, color: "#262626", fontWeight: 600 }}> {/* Kích thước và độ đậm của Title */}
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
                  padding: "14px 24px", // Tăng padding cho mỗi mục
                  margin: "4px 16px", // Tạo khoảng cách giữa các mục và từ biên
                  borderRadius: "8px", // Bo tròn các mục
                  backgroundColor:
                    selectedSection === section.name ? "#e6f7ff" : "transparent", // Màu nền xanh nhẹ khi được chọn
                  color: selectedSection === section.name ? "#1890ff" : "#595959", // Màu chữ xanh khi được chọn
                  fontWeight: selectedSection === section.name ? 600 : 500, // Độ đậm của chữ
                  transition: "all 0.3s ease", // Hiệu ứng chuyển động mượt mà
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
          backgroundColor: "#ffffff",
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
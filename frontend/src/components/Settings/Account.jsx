import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { 
  Typography, 
  Button, 
  Input, 
  Form, 
  Card, 
  Row, 
  Col, 
  message,
  Space,
  Tag
} from "antd";
import { 
  LogoutOutlined, 
  SaveOutlined, 
  CloseOutlined, 
  EditOutlined, 
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined,
  IdcardOutlined
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "@/stores/slices/authSlice";
import { updateProfile } from "@/stores/middlewares/profileMiddleware";
import { fetchUserProfile } from "@/stores/middlewares/authMiddleware";
import AvatarUpload from "@/components/AvatarUpload";

const { Title, Text } = Typography;

const Account = () => {
  const user = useSelector((state) => state.auth.user);
  const { loading } = useSelector((state) => state.profile);
  const dispatch = useDispatch();
  const [form] = Form.useForm();
  const [isEditing, setIsEditing] = useState(false);
  
  // Get avatar from user data instead of separate API call
  const avatarUrl = user?.data?.avatarUrl;

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        firstname: user.data?.firstname || "",
        lastname: user.data?.lastname || "",
        email: user.data?.email || "",
        phone: user.data?.phone || "",
      });
    }
  }, [user, form]);

  const handleLogout = () => {
    dispatch(logout());
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    // Reset form to original user data instead of empty values
    if (user) {
      form.setFieldsValue({
        firstname: user.data?.firstname || "",
        lastname: user.data?.lastname || "",
        email: user.data?.email || "",
        phone: user.data?.phone || "",
      });
    }
  };

  const handleSave = async (values) => {
    try {
      // Only send firstname and lastname for update
      const updateData = {
        firstname: values.firstname,
        lastname: values.lastname
      };
      await dispatch(updateProfile(updateData)).unwrap();
      message.success("Profile updated successfully!");
      setIsEditing(false);
    } catch (error) {
      message.error("Failed to update profile");
      console.error("Error updating profile:", error);
    }
  };

  const handleAvatarChange = async (newAvatarUrl) => {
    // Avatar has been updated successfully
    console.log('Avatar updated:', newAvatarUrl);
    
    // Refresh user data to get updated avatar URL
    try {
      await dispatch(fetchUserProfile()).unwrap();
      console.log('✅ User profile refreshed with new avatar');
    } catch (error) {
      console.error('❌ Failed to refresh user profile:', error);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
    >
      <div style={{ padding: 0 }}>
        <Title level={2} style={{ marginBottom: 32, color: "#262626" }}>
          Account Settings
        </Title>

        <Row gutter={[24, 24]}>
          {/* Profile Card */}
          <Col xs={24} lg={16}>
            <Card 
              title="Profile Information" 
              extra={
                !isEditing && (
                  <Button 
                    type="primary" 
                    icon={<EditOutlined />} 
                    onClick={handleEdit}
                  >
                    Edit Profile
                  </Button>
                )
              }
              style={{ marginBottom: 24 }}
            >
              <Form
                form={form}
                layout="vertical"
                onFinish={handleSave}
                disabled={!isEditing}
              >
                <Row gutter={16}>
                  <Col xs={24} sm={12}>
                    <Form.Item
                      label="First Name"
                      name="firstname"
                      rules={[{ required: true, message: 'Please input your first name!' }]}
                    >
                      <Input 
                        prefix={<UserOutlined />}
                        placeholder="Enter first name" 
                        size="large"
                      />
                    </Form.Item>
                  </Col>
                  <Col xs={24} sm={12}>
                    <Form.Item
                      label="Last Name"
                      name="lastname"
                      rules={[{ required: true, message: 'Please input your last name!' }]}
                    >
                      <Input 
                        prefix={<UserOutlined />}
                        placeholder="Enter last name" 
                        size="large"
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <Form.Item
                  label="Email Address"
                  name="email"
                  extra="Email cannot be changed"
                >
                  <Input 
                    prefix={<MailOutlined />}
                    placeholder="Enter email address" 
                    size="large"
                    disabled
                    style={{ backgroundColor: '#f5f5f5' }}
                  />
                </Form.Item>

                <Form.Item
                  label="Phone Number"
                  name="phone"
                  extra="Phone number cannot be changed"
                >
                  <Input 
                    prefix={<PhoneOutlined />}
                    placeholder="Enter phone number" 
                    size="large"
                    disabled
                    style={{ backgroundColor: '#f5f5f5' }}
                  />
                </Form.Item>

                {isEditing && (
                  <Space>
                    <Button 
                      type="primary" 
                      htmlType="submit" 
                      icon={<SaveOutlined />}
                      loading={loading}
                    >
                      Save Changes
                    </Button>
                    <Button onClick={handleCancel} icon={<CloseOutlined />}>
                      Cancel
                    </Button>
                  </Space>
                )}
              </Form>
            </Card>
          </Col>

          {/* Avatar & Account Info */}
          <Col xs={24} lg={8}>
            <Card title="Profile Picture" style={{ marginBottom: 24 }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
                  <AvatarUpload
                    userId={user?.data?.id}
                    currentAvatarUrl={avatarUrl}
                    onAvatarChange={handleAvatarChange}
                    size="large"
                    showDeleteButton={true}
                    disabled={!isEditing}
                  />
                </div>
                <div>
                  <Title level={4} style={{ margin: 0 }}>
                    {user?.data?.firstname} {user?.data?.lastname}
                  </Title>
                  <Text type="secondary">{user?.data?.email}</Text>
                </div>
              </div>
            </Card>

            <Card title="Account Information">
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <div>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    <IdcardOutlined /> USER ID
                  </Text>
                  <div style={{ 
                    fontFamily: 'monospace', 
                    fontSize: '11px', 
                    background: '#f5f5f5', 
                    padding: '8px', 
                    borderRadius: '4px',
                    marginTop: '4px',
                    wordBreak: 'break-all'
                  }}>
                    {user?.data?.id}
                  </div>
                </div>
                <div>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    STATUS
                  </Text>
                  <div style={{ marginTop: '4px' }}>
                    <Tag color="green">Active</Tag>
                  </div>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>

        {/* Action Buttons */}
        <Card style={{ marginTop: 24 }}>
          <div style={{ textAlign: 'right' }}>
            <Button 
              danger 
              icon={<LogoutOutlined />} 
              onClick={handleLogout}
              size="large"
            >
              Log Out
            </Button>
          </div>
        </Card>
      </div>
    </motion.div>
  );
};

export default Account;
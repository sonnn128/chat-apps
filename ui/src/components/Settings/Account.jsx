import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { 
  Typography, 
  Avatar, 
  Button, 
  Input, 
  Form, 
  Upload, 
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
  CameraOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined,
  IdcardOutlined
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "@/stores/slices/authSlice";
import { updateProfile, uploadAvatar } from "@/stores/middlewares/profileMiddleware";

const { Title, Text } = Typography;

const Account = () => {
  const user = useSelector((state) => state.auth.user);
  const { loading } = useSelector((state) => state.profile);
  const dispatch = useDispatch();
  const [form] = Form.useForm();
  const [isEditing, setIsEditing] = useState(false);
  const [avatarLoading, setAvatarLoading] = useState(false);

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
    form.resetFields();
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

  const handleAvatarChange = async (info) => {
    if (info.file.status === 'uploading') {
      setAvatarLoading(true);
      return;
    }
    
    if (info.file.status === 'done') {
      try {
        await dispatch(uploadAvatar(info.file.originFileObj)).unwrap();
        message.success('Avatar updated successfully');
      } catch (error) {
        message.error('Failed to update avatar');
        console.error("Error uploading avatar:", error);
      } finally {
        setAvatarLoading(false);
      }
    } else if (info.file.status === 'error') {
      message.error('Failed to update avatar');
      setAvatarLoading(false);
    }
  };

  const beforeUpload = (file) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
      message.error('You can only upload JPG/PNG file!');
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('Image must smaller than 2MB!');
    }
    return isJpgOrPng && isLt2M;
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
                <div style={{ position: 'relative', display: 'inline-block' }}>
                  <Avatar
                    size={120}
                    src={user?.data?.avatar}
                    icon={<UserOutlined />}
                    style={{ 
                      backgroundColor: "#1890ff",
                      border: "4px solid #f0f0f0"
                    }}
                  />
                  {isEditing && (
                    <Upload
                      name="avatar"
                      showUploadList={false}
                      beforeUpload={beforeUpload}
                      onChange={handleAvatarChange}
                      style={{ position: 'absolute', bottom: 0, right: 0 }}
                    >
                      <Button
                        type="primary"
                        shape="circle"
                        icon={<CameraOutlined />}
                        size="small"
                        loading={avatarLoading}
                        style={{ 
                          boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                          border: "2px solid white"
                        }}
                      />
                    </Upload>
                  )}
                </div>
                <div style={{ marginTop: 16 }}>
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
                    <CalendarOutlined /> MEMBER SINCE
                  </Text>
                  <div style={{ marginTop: '4px' }}>
                    {user?.data?.createdAt ? 
                      new Date(user.data.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      }) : 
                      'Unknown'
                    }
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
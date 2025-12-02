import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import {
  Form,
  Input,
  Button,
  Checkbox,
  Card,
  Typography,
  Row, // Keep for the remember/forgot section if desired, or remove
  Col, // Keep for the remember/forgot section if desired, or remove
} from "antd";
// EyeOutlined and EyeInvisibleOutlined are automatically handled by Input.Password
// so direct import might not be needed unless you customize iconRender heavily.
// For default behavior, they are not needed.
import { loginUser } from "@/stores/middlewares/authMiddleware";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";
import { successToast } from "@/utils/toast";

const { Title, Text, Link } = Typography;

function Login() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [isLoading, setIsLoading] = useState(false);
  const [form] = Form.useForm(); // Optional: if you need to interact with form instance

  const handleSubmit = async (values) => {
    setIsLoading(true);
    try {
      console.log("🔐 Login: Attempting to login...");
      const result = await dispatch(
        loginUser({ email: values.email.trim(), password: values.password })
      ).unwrap();

      if (result) {
        console.log("✅ Login: Login successful, loading channels...");
        successToast("Log in successfully");

        // Load channels after successful login
        await Promise.all([
          dispatch(fetchAllChannels()).unwrap(),
          dispatch(fetchFriendList()).unwrap(),
          dispatch(fetchPendingRequests()).unwrap()
        ]);
        console.log("✅ Login: Initial data loaded successfully");

        navigate("/");
      }
    } catch (error) {
      console.error("❌ Login: Login failed:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "calc(100vh - 64px)", // Assuming 64px is header height
        padding: "20px", // Add some padding for smaller screens
      }}
    >
      <Card style={{ width: "100%", maxWidth: 400 }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <Title level={3}>Log in to your account</Title>
          <Text>Welcome to ChatApps!</Text>
        </div>

        <Form
          form={form} // Optional
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ email: "", password: "", rememberMe: false }}
          name="login_form"
        >
          <Form.Item
            label="Email"
            name="email"
            rules={[
              { required: true, message: "Please input your email!" },
              { type: "email", message: "Please enter a valid email!" },
            ]}
          >
            <Input
              placeholder="Enter your email"
              disabled={isLoading}
              autoComplete="email"
            />
          </Form.Item>

          <Form.Item
            label="Password"
            name="password"
            rules={[{ required: true, message: "Please input your password!" }]}
          >
            <Input.Password
              placeholder="Enter your password"
              disabled={isLoading}
              autoComplete="current-password"
            // iconRender is handled by Input.Password by default
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: "12px" }}>
            {" "}
            {/* Reduced margin for this composite item */}
            <Row justify="space-between" align="middle">
              <Col>
                <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                  <Checkbox disabled={isLoading}>Remember for 30 days</Checkbox>
                </Form.Item>
              </Col>
              <Col>
                <Link href="/forgot-password" disabled={isLoading}>
                  Quên mật khẩu?
                </Link>
              </Col>
            </Row>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={isLoading}>
              Log in
            </Button>
          </Form.Item>

          <div style={{ textAlign: "center" }}>
            <Text>Don't have an account? </Text>
            <Link href="/register" strong disabled={isLoading}>
              Register now!
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  );
}

export default Login;

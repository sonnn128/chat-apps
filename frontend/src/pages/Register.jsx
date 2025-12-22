import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Form, Input, Button, Row, Col, Card, Typography, Steps, message } from "antd";
import { SafetyOutlined } from "@ant-design/icons";
import { useDispatch } from "react-redux";
import { registerUser } from "@/stores/middlewares/authMiddleware";
import { post } from "@/utils/httpRequest";
import { errorToast, successToast } from "@/utils/toast";

const { Title, Text, Link } = Typography;

function Register() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [isLoading, setIsLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0); // 0: Form, 1: OTP Verification
  const [formData, setFormData] = useState(null);
  const [form] = Form.useForm();
  const [otpForm] = Form.useForm();

  // Step 1: Submit registration form and send OTP
  const handleSubmitForm = async (values) => {
    setIsLoading(true);
    try {
      console.log("values: ", values);

      // Send OTP to email with validation
      await post("/auth/send-registration-otp", {
        email: values.email,
        phone: values.phone,
        firstname: values.firstname
      });

      setFormData(values);
      setCurrentStep(1);
      successToast("OTP sent to your email!");
    } catch (err) {
      console.error("Failed to send OTP:", err);
      // Error toast is already handled by httpRequest.js global handler
    } finally {
      setIsLoading(false);
    }
  };

  // Step 2: Verify OTP and complete registration
  const handleVerifyOtp = async (values) => {
    setIsLoading(true);
    try {
      // Register with OTP in one call
      await post(`/auth/register-with-otp?otp=${values.otp}`, formData);

      successToast("Registration successful! Please log in.");
      navigate("/login");
    } catch (err) {
      console.error("Failed to verify OTP or register:", err);
      errorToast(err.response?.data?.message || "Failed to verify OTP");
    } finally {
      setIsLoading(false);
    }
  };

  // Resend OTP
  const handleResendOtp = async () => {
    setIsLoading(true);
    try {
      await post("/auth/send-registration-otp", { email: formData.email });
      successToast("OTP resent to your email!");
    } catch (err) {
      console.error("Failed to resend OTP:", err);
      errorToast(err.response?.data?.message || "Failed to resend OTP");
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
        minHeight: "calc(100vh - 64px)",
        padding: "20px",
      }}
    >
      <Card style={{ width: "100%", maxWidth: 450 }}>
        <Title level={3} style={{ textAlign: "center", marginBottom: 24 }}>
          Create your account
        </Title>

        <Steps
          current={currentStep}
          items={[
            { title: 'Information' },
            { title: 'Verification' }
          ]}
          style={{ marginBottom: 24 }}
        />

        {currentStep === 0 ? (
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmitForm}
            initialValues={{
              email: "",
              password: "",
              lastname: "",
              firstname: "",
              phone: "",
              username: "",
            }}
            name="register_form"
          >
            <Row gutter={16}>
              <Col xs={24} sm={12}>
                <Form.Item
                  label="First Name"
                  name="firstname"
                  rules={[{ required: true, message: "Please input your first name!" }]}
                >
                  <Input placeholder="Enter your first name" disabled={isLoading} />
                </Form.Item>
              </Col>
              <Col xs={24} sm={12}>
                <Form.Item
                  label="Last Name"
                  name="lastname"
                  rules={[{ required: true, message: "Please input your last name!" }]}
                >
                  <Input placeholder="Enter your last name" disabled={isLoading} />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              label="Email"
              name="email"
              rules={[
                { required: true, message: "Please input your email!" },
                { type: "email", message: "Please enter a valid email!" },
              ]}
            >
              <Input placeholder="Enter your email" disabled={isLoading} autoComplete="email" />
            </Form.Item>

            <Form.Item
              label="Phone Number"
              name="phone"
              rules={[
                { required: true, message: "Please input your phone number!" },
                {
                  pattern: /^(0[3|5|7|8|9][0-9]{8}|\+84[3|5|7|8|9][0-9]{8})$/,
                  message:
                    "Phone must be a valid Vietnamese number (e.g. 0799199916 or +84815216193)",
                },
              ]}
            >
              <Input placeholder="Enter your phone number" disabled={isLoading} autoComplete="tel" />
            </Form.Item>

            <Form.Item
              label="Username"
              name="username"
              rules={[
                { required: true, message: "Please input your username!" },
              ]}
            >
              <Input placeholder="Enter your username" disabled={isLoading} autoComplete="username" />
            </Form.Item>

            <Form.Item
              label="Password"
              name="password"
              rules={[
                { required: true, message: "Please input your password!" },
                { min: 6, message: "Password must be at least 6 characters!" },
              ]}
              hasFeedback
            >
              <Input.Password placeholder="Enter your password" disabled={isLoading} autoComplete="new-password" />
            </Form.Item>

            <Form.Item style={{ marginTop: 24 }}>
              <Button type="primary" htmlType="submit" block loading={isLoading}>
                Continue
              </Button>
            </Form.Item>

            <div style={{ textAlign: "center" }}>
              <Text>Already have an account? </Text>
              <Link href="/login" strong disabled={isLoading}>
                Login here!
              </Link>
            </div>
          </Form>
        ) : (
          <Form
            form={otpForm}
            layout="vertical"
            onFinish={handleVerifyOtp}
            name="otp_verification_form"
          >
            <div style={{ textAlign: "center", marginBottom: 24 }}>
              <Text type="secondary">
                We've sent a 6-digit OTP to <strong>{formData?.email}</strong>
              </Text>
            </div>

            <Form.Item
              label="OTP Code"
              name="otp"
              rules={[
                { required: true, message: "Please input the OTP!" },
                { len: 6, message: "OTP must be 6 digits!" },
                { pattern: /^\d+$/, message: "OTP must contain only numbers!" }
              ]}
            >
              <Input
                size="large"
                prefix={<SafetyOutlined />}
                placeholder="Enter 6-digit OTP"
                maxLength={6}
                disabled={isLoading}
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={isLoading}>
                Verify & Register
              </Button>
            </Form.Item>

            <div style={{ textAlign: "center" }}>
              <Text type="secondary">Didn't receive the code? </Text>
              <Button type="link" onClick={handleResendOtp} loading={isLoading}>
                Resend OTP
              </Button>
            </div>

            <div style={{ textAlign: "center", marginTop: 16 }}>
              <Button type="link" onClick={() => setCurrentStep(0)} disabled={isLoading}>
                Back to form
              </Button>
            </div>
          </Form>
        )}
      </Card>
    </div>
  );
}

export default Register;

import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Form, Input, Button, Row, Col, Card, Typography } from "antd";
import { useDispatch } from "react-redux";
import { registerUser } from "@/stores/middlewares/authMiddleware";
import { errorToast, successToast } from "@/utils/toast";

const { Title, Text, Link } = Typography;

function Register() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [isLoading, setIsLoading] = useState(false);
  const [form] = Form.useForm(); // Optional: if you need to interact with form instance

  const handleSubmit = async (values) => {
    setIsLoading(true);
    try {
      await dispatch(registerUser({ ...values })).unwrap(); // Assuming your API takes all form values
      successToast("Registration successful! Please log in.");
      navigate("/login");
    } catch (err) {
      errorToast(err.message || "Registration failed. Please try again.");
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
        // paddingTop: 64, // User's original style, can be kept or adjusted
      }}
    >
      <Card style={{ width: "100%", maxWidth: 450 }}>
        {" "}
        {/* Slightly wider for more fields */}
        <Title level={3} style={{ textAlign: "center", marginBottom: 24 }}>
          Create your account
        </Title>
        <Form
          form={form} // Optional
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            email: "",
            password: "",
            lastname: "",
            firstname: "",
          }}
          name="register_form"
        >
          <Row gutter={16}>
            <Col xs={24} sm={12}>
              <Form.Item
                label="First Name"
                name="firstname" // Swapped with lastname to be more conventional
                rules={[
                  {
                    required: true,
                    message: "Please input your first name!",
                  },
                ]}
              >
                <Input
                  placeholder="Enter your first name"
                  disabled={isLoading}
                />
              </Form.Item>
            </Col>
            <Col xs={24} sm={12}>
              <Form.Item
                label="Last Name"
                name="lastname"
                rules={[
                  { required: true, message: "Please input your last name!" },
                ]}
              >
                <Input
                  placeholder="Enter your last name"
                  disabled={isLoading}
                />
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
            <Input
              placeholder="Enter your email"
              disabled={isLoading}
              autoComplete="email"
            />
          </Form.Item>

          <Form.Item
            label="Password"
            name="password"
            rules={[
              { required: true, message: "Please input your password!" },
              {
                min: 6,
                message: "Password must be at least 6 characters!",
              },
            ]}
            hasFeedback
          >
            <Input.Password
              placeholder="Enter your password"
              disabled={isLoading}
              autoComplete="new-password"
            />
          </Form.Item>
          <Form.Item style={{ marginTop: 24 }}>
            <Button type="primary" htmlType="submit" block loading={isLoading}>
              Register
            </Button>
          </Form.Item>

          <div style={{ textAlign: "center" }}>
            <Text>Already have an account? </Text>
            <Link href="/login" strong disabled={isLoading}>
              Login here!
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  );
}

export default Register;

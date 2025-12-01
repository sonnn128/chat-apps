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
  const [form] = Form.useForm();

  const handleSubmit = async (values) => {
    setIsLoading(true);
    try {
      console.log("values: ", values);

      await dispatch(registerUser({ ...values })).unwrap();
      successToast("Registration successful! Please log in.");
      navigate("/login");
    } catch (err) {
      // Error is handled globally by httpRequest.js
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
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            email: "",
            password: "",
            lastname: "",
            firstname: "",
            phone: "",
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

          {/* Phone field */}
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

          {/* Phone field */}
          <Form.Item
            label="Username"
            name="username"
            rules={[
              { required: true, message: "Please input your username number!" },
              {
                message:
                  "username must be a valid Vietnamese number (e.g. 0799199916 or +84815216193)",
              },
            ]}
          >
            <Input placeholder="Enter your username number" disabled={isLoading} autoComplete="tel" />
          </Form.Item>

          {/* Password field */}
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

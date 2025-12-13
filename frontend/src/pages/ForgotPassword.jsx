import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Form, Input, Button, Typography, message } from "antd";
import { MailOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { post } from "@/utils/httpRequest";

const { Title, Text } = Typography;

const ForgotPassword = () => {
    const [loading, setLoading] = useState(false);
    const [sent, setSent] = useState(false);

    const onFinish = async (values) => {
        setLoading(true);
        try {
            await post("/auth/forgot-password", { email: values.email });
            setSent(true);
            message.success("Reset link sent to your email!");
        } catch (error) {
            console.error(error);
            message.error(error.response?.data?.message || "Failed to send reset link.");
        } finally {
            setLoading(false);
        }
    };

    if (sent) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md text-center">
                    <MailOutlined className="text-6xl text-blue-500 mb-4" />
                    <Title level={3}>Check Your Email</Title>
                    <Text className="text-gray-500 mb-6 block">
                        We have sent a password reset link to your email address.
                    </Text>
                    <Link to="/login">
                        <Button type="primary" block>
                            Back to Login
                        </Button>
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md">
                <div className="mb-6">
                    <Link to="/login" className="text-gray-500 hover:text-gray-700 flex items-center gap-2 mb-4">
                        <ArrowLeftOutlined /> Back to Login
                    </Link>
                    <Title level={2}>Forgot Password?</Title>
                    <Text type="secondary">
                        Enter your email address and we'll send you a link to reset your password.
                    </Text>
                </div>

                <Form
                    name="forgot_password"
                    layout="vertical"
                    onFinish={onFinish}
                    requiredMark={false}
                >
                    <Form.Item
                        name="email"
                        label="Email Address"
                        rules={[
                            { required: true, message: "Please input your email!" },
                            { type: "email", message: "Please enter a valid email!" },
                        ]}
                    >
                        <Input size="large" prefix={<MailOutlined />} placeholder="Enter your email" />
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            size="large"
                            block
                            loading={loading}
                            className="bg-blue-600 hover:bg-blue-700 h-10"
                        >
                            Send Reset Link
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </div>
    );
};

export default ForgotPassword;

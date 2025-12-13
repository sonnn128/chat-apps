import React, { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Form, Input, Button, Typography, message } from "antd";
import { LockOutlined, CheckCircleOutlined } from "@ant-design/icons";
import { post } from "@/utils/httpRequest";

const { Title, Text } = Typography;

const ResetPassword = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const onFinish = async (values) => {
        if (!token) {
            message.error("Invalid token. Please request a new reset link.");
            return;
        }

        setLoading(true);
        try {
            await post("/auth/reset-password", {
                token,
                newPassword: values.newPassword
            });
            setSuccess(true);
            message.success("Password reset successfully!");
        } catch (error) {
            console.error(error);
            message.error(error.response?.data?.message || "Failed to reset password.");
        } finally {
            setLoading(false);
        }
    };

    if (!token) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="text-center">
                    <Title level={4} type="danger">Invalid or Missing Token</Title>
                    <Link to="/forgot-password">Request a new link</Link>
                </div>
            </div>
        );
    }

    if (success) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md text-center">
                    <CheckCircleOutlined className="text-6xl text-green-500 mb-4" />
                    <Title level={3}>Password Reset</Title>
                    <Text className="text-gray-500 mb-6 block">
                        Your password has been successfully reset. You can now login with your new password.
                    </Text>
                    <Link to="/login">
                        <Button type="primary" block size="large">
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
                <div className="mb-6 text-center">
                    <Title level={2}>Reset Password</Title>
                    <Text type="secondary">
                        Enter your new password below.
                    </Text>
                </div>

                <Form
                    name="reset_password"
                    layout="vertical"
                    onFinish={onFinish}
                    requiredMark={false}
                >
                    <Form.Item
                        name="newPassword"
                        label="New Password"
                        rules={[
                            { required: true, message: "Please input your new password!" },
                            { min: 6, message: "Password must be at least 6 characters!" },
                        ]}
                    >
                        <Input.Password
                            size="large"
                            prefix={<LockOutlined />}
                            placeholder="New Password"
                        />
                    </Form.Item>

                    <Form.Item
                        name="confirmPassword"
                        label="Confirm Password"
                        dependencies={['newPassword']}
                        rules={[
                            { required: true, message: "Please confirm your password!" },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('newPassword') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error('The two passwords that you entered do not match!'));
                                },
                            }),
                        ]}
                    >
                        <Input.Password
                            size="large"
                            prefix={<LockOutlined />}
                            placeholder="Confirm Password"
                        />
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
                            Reset Password
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </div>
    );
};

export default ResetPassword;

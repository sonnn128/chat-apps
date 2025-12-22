import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Form, Input, Button, Typography, message, Steps } from "antd";
import { MailOutlined, LockOutlined, ArrowLeftOutlined, SafetyOutlined } from "@ant-design/icons";
import { post } from "@/utils/httpRequest";

const { Title, Text } = Typography;

const ForgotPassword = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [currentStep, setCurrentStep] = useState(0); // 0: Email, 1: OTP + Password
    const [email, setEmail] = useState("");
    const [form] = Form.useForm();

    // Step 1: Send OTP
    const onSendOtp = async (values) => {
        setLoading(true);
        try {
            await post("/auth/forgot-password", { email: values.email });
            setEmail(values.email);
            setCurrentStep(1);
            message.success("OTP sent to your email!");
        } catch (error) {
            console.error(error);
            message.error(error.response?.data?.message || "Failed to send OTP.");
        } finally {
            setLoading(false);
        }
    };

    // Step 2: Verify OTP and Reset Password
    const onResetPassword = async (values) => {
        setLoading(true);
        try {
            await post("/auth/reset-password-otp", {
                email: email,
                otp: values.otp,
                newPassword: values.newPassword
            });
            message.success("Password reset successfully!");
            navigate("/login");
        } catch (error) {
            console.error(error);
            message.error(error.response?.data?.message || "Failed to reset password.");
        } finally {
            setLoading(false);
        }
    };

    // Resend OTP
    const onResendOtp = async () => {
        setLoading(true);
        try {
            await post("/auth/forgot-password", { email: email });
            message.success("OTP resent to your email!");
        } catch (error) {
            console.error(error);
            message.error(error.response?.data?.message || "Failed to resend OTP.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md">
                <div className="mb-6">
                    <Link to="/login" className="text-gray-500 hover:text-gray-700 flex items-center gap-2 mb-4">
                        <ArrowLeftOutlined /> Back to Login
                    </Link>
                    <Title level={2}>Forgot Password?</Title>
                    <Text type="secondary">
                        {currentStep === 0 
                            ? "Enter your email address and we'll send you an OTP to reset your password."
                            : "Enter the OTP sent to your email and your new password."}
                    </Text>
                </div>

                <Steps
                    current={currentStep}
                    items={[
                        { title: 'Email' },
                        { title: 'Reset' }
                    ]}
                    className="mb-6"
                />

                {currentStep === 0 ? (
                    <Form
                        form={form}
                        name="forgot_password_email"
                        layout="vertical"
                        onFinish={onSendOtp}
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
                                Send OTP
                            </Button>
                        </Form.Item>
                    </Form>
                ) : (
                    <Form
                        form={form}
                        name="reset_password_otp"
                        layout="vertical"
                        onFinish={onResetPassword}
                        requiredMark={false}
                    >
                        <Form.Item
                            name="otp"
                            label="OTP Code"
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
                            />
                        </Form.Item>

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
                                        return Promise.reject(new Error('Passwords do not match!'));
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

                        <div className="text-center">
                            <Text type="secondary">Didn't receive the code? </Text>
                            <Button type="link" onClick={onResendOtp} loading={loading}>
                                Resend OTP
                            </Button>
                        </div>
                    </Form>
                )}
            </div>
        </div>
    );
};

export default ForgotPassword;

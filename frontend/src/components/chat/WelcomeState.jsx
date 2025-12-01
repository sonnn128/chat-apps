import React from "react";
import { Typography } from "antd";
import { MessageOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

const WelcomeState = () => {
    return (
        <div className="flex flex-col items-center justify-center h-full w-full bg-gray-50 p-8 text-center">
            <div className="mb-6 p-6 bg-blue-50 rounded-full">
                <MessageOutlined style={{ fontSize: "48px", color: "#1890ff" }} />
            </div>
            <Title level={3} style={{ marginBottom: "16px", color: "#262626" }}>
                Welcome to ChatApps
            </Title>
            <Text type="secondary" style={{ fontSize: "16px", maxWidth: "400px" }}>
                Select a conversation from the sidebar or start a new one to begin messaging.
            </Text>
        </div>
    );
};

export default WelcomeState;

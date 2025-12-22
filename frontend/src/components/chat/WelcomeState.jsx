import React from "react";
import { Typography, theme } from "antd";
import { MessageOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

const WelcomeState = () => {
    const { token } = theme.useToken();

    return (
        <div
            className="flex flex-col items-center justify-center h-full w-full p-8 text-center"
            style={{ backgroundColor: token.colorBgLayout }}
        >
            <div
                className="mb-6 p-6 rounded-full"
                style={{ backgroundColor: token.colorPrimaryBg }}
            >
                <MessageOutlined style={{ fontSize: "48px", color: token.colorPrimary }} />
            </div>
            <Title level={3} style={{ marginBottom: "16px" }}>
                Welcome to ChatApps
            </Title>
            <Text type="secondary" style={{ fontSize: "16px", maxWidth: "400px" }}>
                Select a conversation from the sidebar or start a new one to begin messaging.
            </Text>
        </div>
    );
};

export default WelcomeState;

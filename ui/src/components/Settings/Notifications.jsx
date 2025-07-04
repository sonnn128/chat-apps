import React from "react";
import { motion } from "framer-motion";
import { Typography, List, Switch, Divider } from "antd";

const { Title, Text } = Typography;

const NotificationsSection = () => {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
    >
      <div style={{ padding: 32 }}>
        <Title level={4} style={{ fontWeight: "bold", marginBottom: 24 }}>
          Notifications
        </Title>
        <List
          split={false}
          dataSource={[
            {
              title: "Message Notifications",
              description: "Receive notifications for new messages",
              checked: true,
            },
            {
              title: "Sound",
              description: "Play sound for new messages",
              checked: true,
            },
          ]}
          renderItem={(item, index) => (
            <>
              <List.Item
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "12px 0",
                }}
              >
                <div>
                  <Text strong>{item.title}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: 13 }}>
                    {item.description}
                  </Text>
                </div>
                <Switch defaultChecked={item.checked} />
              </List.Item>
              {index !== 1 && <Divider style={{ margin: 0 }} />}
            </>
          )}
        />
      </div>
    </motion.div>
  );
};

export default NotificationsSection;

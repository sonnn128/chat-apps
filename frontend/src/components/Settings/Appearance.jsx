import React from 'react';
import { Typography, Card, Radio, Space } from 'antd';
import { SunOutlined, MoonOutlined, DesktopOutlined } from '@ant-design/icons';
import { useTheme } from '@/context/ThemeContext';

const { Title, Text } = Typography;

const Appearance = () => {
    const { themeMode, setTheme, actualTheme } = useTheme();

    const themeOptions = [
        {
            value: 'light',
            label: 'Light',
            icon: <SunOutlined style={{ fontSize: '24px' }} />,
            description: 'Always use light theme'
        },
        {
            value: 'dark',
            label: 'Dark',
            icon: <MoonOutlined style={{ fontSize: '24px' }} />,
            description: 'Always use dark theme'
        },
        {
            value: 'system',
            label: 'System',
            icon: <DesktopOutlined style={{ fontSize: '24px' }} />,
            description: 'Automatically match system theme'
        }
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Title level={2} style={{ marginBottom: '8px' }}>Appearance</Title>
            <Text type="secondary" style={{ fontSize: '14px', display: 'block', marginBottom: '24px' }}>
                Customize how ChatApps looks on your device
            </Text>

            <div style={{ marginBottom: '24px' }}>
                <Title level={4} style={{ marginBottom: '16px' }}>Theme</Title>
                <Radio.Group
                    value={themeMode}
                    onChange={(e) => setTheme(e.target.value)}
                    style={{ width: '100%' }}
                >
                    <Space direction="vertical" style={{ width: '100%' }} size={12}>
                        {themeOptions.map((option) => (
                            <Card
                                key={option.value}
                                hoverable
                                style={{
                                    borderColor: themeMode === option.value ? '#1890ff' : '#d9d9d9',
                                    borderWidth: themeMode === option.value ? '2px' : '1px',
                                    cursor: 'pointer',
                                    transition: 'all 0.3s ease'
                                }}
                                onClick={() => setTheme(option.value)}
                            >
                                <Radio value={option.value} style={{ width: '100%' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                                        <div style={{
                                            color: themeMode === option.value ? '#1890ff' : '#595959',
                                            transition: 'color 0.3s ease'
                                        }}>
                                            {option.icon}
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <div style={{
                                                fontWeight: themeMode === option.value ? 600 : 500,
                                                fontSize: '16px',
                                                marginBottom: '4px'
                                            }}>
                                                {option.label}
                                            </div>
                                            <Text type="secondary" style={{ fontSize: '13px' }}>
                                                {option.description}
                                            </Text>
                                        </div>
                                    </div>
                                </Radio>
                            </Card>
                        ))}
                    </Space>
                </Radio.Group>
            </div>

            {themeMode === 'system' && (
                <Card
                    style={{
                        backgroundColor: '#f0f2f5',
                        border: 'none'
                    }}
                >
                    <Space direction="vertical" size={4}>
                        <Text strong style={{ fontSize: '14px' }}>
                            Current system theme: {actualTheme === 'dark' ? 'Dark' : 'Light'}
                        </Text>
                        <Text type="secondary" style={{ fontSize: '13px' }}>
                            ChatApps will automatically switch between light and dark themes based on your system settings.
                        </Text>
                    </Space>
                </Card>
            )}
        </div>
    );
};

export default Appearance;

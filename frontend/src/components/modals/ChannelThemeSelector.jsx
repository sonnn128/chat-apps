import React, { useState } from 'react';
import { Modal, Typography, Row, Col, Tooltip } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import { CHANNEL_THEMES, getChannelTheme, getThemeStyle } from '@/utils/channelThemes';
import { useDispatch, useSelector } from 'react-redux';
import { updateChannelTheme, sendChannelMessage } from '@/stores/middlewares/channelMiddleware';

const { Title, Text } = Typography;

const ChannelThemeSelector = ({ open, onClose, channel }) => {
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user?.data);
    const currentTheme = getChannelTheme(channel);
    const [selectedTheme, setSelectedTheme] = useState(currentTheme);
    const [isUpdating, setIsUpdating] = useState(false);

    const handleThemeSelect = async (theme) => {
        setSelectedTheme(theme);
        setIsUpdating(true);

        try {
            await dispatch(updateChannelTheme({
                channelId: channel.id,
                themeColor: theme.color,
                themeGradient: theme.gradient,
            })).unwrap();

            // Send notice message
            const tempId = crypto.randomUUID();
            const senderName = user ? `${user.firstname} ${user.lastname}` : 'Someone';
            const noticeContent = `${senderName} changed the theme to ${theme.name}`;

            await dispatch(sendChannelMessage({
                channelId: channel.id,
                content: noticeContent,
                type: 'NOTICE',
                tempId,
                userId: user?.id
            }));

            // Close modal after successful update
            setTimeout(() => {
                onClose();
            }, 300);
        } catch (error) {
            console.error('Failed to update theme:', error);
        } finally {
            setIsUpdating(false);
        }
    };

    const themes = Object.values(CHANNEL_THEMES);
    const colorThemes = themes.filter(t => t.type === 'color');
    const gradientThemes = themes.filter(t => t.type === 'gradient');

    const ThemeCircle = ({ theme, size = 48 }) => {
        const isSelected = selectedTheme.id === theme.id;
        const style = getThemeStyle(theme);

        return (
            <Tooltip title={theme.name}>
                <div
                    onClick={() => handleThemeSelect(theme)}
                    style={{
                        width: size,
                        height: size,
                        borderRadius: '50%',
                        cursor: 'pointer',
                        border: isSelected ? '3px solid #1890ff' : '2px solid #e8e8e8',
                        boxShadow: isSelected ? '0 0 0 2px rgba(24, 144, 255, 0.2)' : 'none',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        transition: 'all 0.3s ease',
                        position: 'relative',
                        ...style,
                    }}
                    className="hover:scale-110 transition-transform"
                >
                    {isSelected && (
                        <CheckOutlined
                            style={{
                                color: 'white',
                                fontSize: '20px',
                                filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.3))'
                            }}
                        />
                    )}
                </div>
            </Tooltip>
        );
    };

    return (
        <Modal
            title={
                <div>
                    <Title level={4} style={{ margin: 0 }}>Change Theme</Title>
                    <Text type="secondary" style={{ fontSize: '14px' }}>
                        Choose a color or gradient for this conversation
                    </Text>
                </div>
            }
            open={open}
            onCancel={onClose}
            footer={null}
            width={600}
        >
            <div style={{ padding: '16px 0' }}>
                {/* Solid Colors */}
                <div style={{ marginBottom: '32px' }}>
                    <Title level={5} style={{ marginBottom: '16px' }}>Colors</Title>
                    <Row gutter={[16, 16]}>
                        {colorThemes.map((theme) => (
                            <Col key={theme.id} span={4}>
                                <div style={{ display: 'flex', justifyContent: 'center' }}>
                                    <ThemeCircle theme={theme} />
                                </div>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* Gradients */}
                <div>
                    <Title level={5} style={{ marginBottom: '16px' }}>Gradients</Title>
                    <Row gutter={[16, 16]}>
                        {gradientThemes.map((theme) => (
                            <Col key={theme.id} span={4}>
                                <div style={{ display: 'flex', justifyContent: 'center' }}>
                                    <ThemeCircle theme={theme} />
                                </div>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* Preview */}
                {selectedTheme && (
                    <div style={{ marginTop: '32px', padding: '16px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                        <Text type="secondary" style={{ fontSize: '12px', display: 'block', marginBottom: '8px' }}>
                            Preview
                        </Text>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-end' }}>
                            {/* Sent message preview */}
                            <div
                                style={{
                                    padding: '10px 16px',
                                    borderRadius: '18px',
                                    maxWidth: '60%',
                                    color: 'white',
                                    fontSize: '14px',
                                    ...getThemeStyle(selectedTheme),
                                }}
                            >
                                Hello! This is how your messages will look.
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Modal>
    );
};

export default ChannelThemeSelector;

import React from 'react';
import { Switch } from 'antd';
import { BulbOutlined, BulbFilled } from '@ant-design/icons';
import { useTheme } from '@/context/ThemeContext';

const ThemeToggle = ({ style, className }) => {
    const { theme, toggleTheme, isDark } = useTheme();

    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', ...style }} className={className}>
            <Switch
                checked={isDark}
                onChange={toggleTheme}
                checkedChildren={<BulbFilled style={{ color: '#ffd700' }} />}
                unCheckedChildren={<BulbOutlined />}
                style={{
                    backgroundColor: isDark ? '#1890ff' : '#d9d9d9'
                }}
            />
            <span style={{ fontSize: '14px', color: isDark ? '#fff' : '#000' }}>
                {isDark ? 'Dark' : 'Light'}
            </span>
        </div>
    );
};

export default ThemeToggle;

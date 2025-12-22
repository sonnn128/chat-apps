import React, { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext();

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within ThemeProvider');
    }
    return context;
};

export const ThemeProvider = ({ children }) => {
    // Get initial theme mode from localStorage or default to 'light'
    const [themeMode, setThemeMode] = useState(() => {
        const savedMode = localStorage.getItem('themeMode');
        return savedMode || 'light';
    });

    // Get system preference
    const getSystemTheme = () => {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    };

    // Calculate actual theme based on mode
    const getActualTheme = () => {
        if (themeMode === 'system') {
            return getSystemTheme();
        }
        return themeMode;
    };

    const [actualTheme, setActualTheme] = useState(getActualTheme());

    // Listen to system theme changes when mode is 'system'
    useEffect(() => {
        if (themeMode === 'system') {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

            const handleChange = (e) => {
                setActualTheme(e.matches ? 'dark' : 'light');
            };

            mediaQuery.addEventListener('change', handleChange);
            setActualTheme(getSystemTheme());

            return () => mediaQuery.removeEventListener('change', handleChange);
        } else {
            setActualTheme(themeMode);
        }
    }, [themeMode]);

    // Update localStorage and document class when theme changes
    useEffect(() => {
        localStorage.setItem('themeMode', themeMode);

        // Update document class for CSS-based theming
        if (actualTheme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
    }, [themeMode, actualTheme]);

    const setTheme = (mode) => {
        if (['light', 'dark', 'system'].includes(mode)) {
            setThemeMode(mode);
        }
    };

    const value = {
        themeMode,      // 'light' | 'dark' | 'system'
        actualTheme,    // 'light' | 'dark' (resolved)
        setTheme,
        isDark: actualTheme === 'dark',
        isSystem: themeMode === 'system'
    };

    return (
        <ThemeContext.Provider value={value}>
            {children}
        </ThemeContext.Provider>
    );
};

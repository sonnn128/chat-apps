import React from "react";
import "./assets/css/index.css";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { ConfigProvider, theme } from "antd";
import store from "@/stores/store.jsx";
import App from "@/App.jsx";
import { Toaster } from "react-hot-toast";
import { ThemeProvider, useTheme } from "@/context/ThemeContext";

// Wrapper component to access theme context
const ThemedApp = () => {
  const { isDark } = useTheme();

  return (
    <ConfigProvider
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1890ff',
          borderRadius: 8,
        },
      }}
    >
      <App />
    </ConfigProvider>
  );
};

createRoot(document.getElementById("root")).render(
  <>
    <Toaster position="top-right" />
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Provider store={store}>
        <ThemeProvider>
          <ThemedApp />
        </ThemeProvider>
      </Provider>
    </BrowserRouter>
  </>
);

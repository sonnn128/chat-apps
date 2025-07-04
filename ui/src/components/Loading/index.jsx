import React from "react";
import { Spin } from "antd";

function Loading() {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        backgroundColor: "#fff",
      }}
    >
      <Spin
        size="large"
        style={{
          color: "#1877f2",
        }}
      />
    </div>
  );
}

export default Loading;
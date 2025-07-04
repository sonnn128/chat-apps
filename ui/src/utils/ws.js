import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { errorToast } from "./toast";

const chatServerUrl = import.meta.env.VITE_REACT_CHAT_SERVER_URL || "http://localhost:8888";

const stompClient = new Client({
  webSocketFactory: () => new SockJS(`${chatServerUrl}/ws`),
  reconnectDelay: 5000, 
});

stompClient.onStompError = (error) => {
  console.error("WebSocket Error:", error);
  errorToast("WebSocket connection failed: " + JSON.stringify(error));
};

export { stompClient };
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { errorToast } from "./toast";

const notificationServer = import.meta.env.VITE_REACT_CHAT_SERVER_URL || 'http://localhost:8888/ws';


const stompClient = new Client({
  webSocketFactory: () => new SockJS(notificationServer),
  reconnectDelay: 5000, 
});

stompClient.onStompError = (error) => {
  console.error("WebSocket Error:", error);
  errorToast("WebSocket connection failed: " + JSON.stringify(error));
};

export { stompClient };
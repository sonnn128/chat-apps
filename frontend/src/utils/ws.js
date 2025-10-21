import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAuthHeaders } from "./authUtils";

const WEBSOCKET_URL = `${import.meta.env.VITE_REACT_APP_BASE_URL}/ws`;

let stompClient = null;
const subscriptions = new Map();
const pendingSubscriptions = [];

const connect = (onConnectedCallback) => {
  if (stompClient?.active) {
    console.log("WebSocket: Already connecting/connected, skipping...");
    return;
  }

  const headers = getAuthHeaders();
  if (!headers.Authorization || headers.Authorization === "Bearer null") {
    console.error(
      "WebSocket connection failed: No token found in localStorage."
    );
    return;
  }

  console.log("WebSocket: Attempting to connect to", WEBSOCKET_URL);

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WEBSOCKET_URL),
    connectHeaders: headers,
    reconnectDelay: 5000,
    debug: (str) => {
      console.log("STOMP Debug:", str);
    },
  });

  stompClient.onConnect = (frame) => {
    console.log("✅ WebSocket: Connected successfully!", frame);
    console.log(
      "WebSocket: Processing",
      pendingSubscriptions.length,
      "pending subscriptions"
    );

    // Process pending subscriptions
    pendingSubscriptions.forEach(({ destination, callback }) => {
      subscribe(destination, callback);
    });
    pendingSubscriptions.length = 0;

    if (onConnectedCallback) {
      console.log("WebSocket: Executing connection callback");
      onConnectedCallback();
    }
  };

  stompClient.onStompError = (frame) => {
    console.error("❌ WebSocket: STOMP error:", frame.headers["message"]);
    console.error("WebSocket: Additional details:", frame.body);
  };

  stompClient.onWebSocketError = (error) => {
    console.error("❌ WebSocket: Transport error:", error);
  };

  stompClient.onDisconnect = () => {
    console.log("⚠️ WebSocket: Disconnected");
  };

  stompClient.activate();
};

const disconnect = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    subscriptions.clear();
  }
};

const subscribe = (destination, callback) => {
  if (!stompClient?.connected) {
    console.log(
      "WebSocket: Client not connected, adding to pending subscriptions:",
      destination
    );
    pendingSubscriptions.push({ destination, callback });
    if (!stompClient?.active) {
      connect();
    }
    return;
  }

  if (subscriptions.has(destination)) {
    console.log("WebSocket: Already subscribed to:", destination);
    return;
  }

  console.log("✅ WebSocket: Subscribing to:", destination);
  const subscription = stompClient.subscribe(destination, (message) => {
    try {
      const parsedMessage = JSON.parse(message.body);
      console.log(
        "📨 WebSocket: Message received from",
        destination,
        ":",
        parsedMessage
      );
      callback(parsedMessage);
    } catch (error) {
      console.error(
        "❌ WebSocket: Error parsing message from",
        destination,
        ":",
        error
      );
      console.error("Raw message:", message.body);
    }
  });

  subscriptions.set(destination, subscription);
  console.log("WebSocket: Total active subscriptions:", subscriptions.size);
};

const sendMessage = (destination, body) => {
  if (!stompClient?.connected) {
    console.error(
      "❌ WebSocket: Cannot send message, STOMP client is not connected."
    );
    return;
  }

  console.log("📤 WebSocket: Sending message to", destination, ":", body);
  stompClient.publish({
    destination: destination,
    body: JSON.stringify(body),
  });
};

const isConnected = () => {
  return stompClient?.connected === true;
};

export const websocketService = {
  connect,
  disconnect,
  subscribe,
  sendMessage,
  isConnected,
};

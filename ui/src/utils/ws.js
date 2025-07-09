import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAuthHeaders } from "./authUtils";

const WEBSOCKET_URL =
  import.meta.env.VITE_REACT_APP_WEBSOCKET_URL || "http://localhost:8889/ws";

let stompClient = null;
const subscriptions = new Map();
const pendingSubscriptions = [];

const connect = (onConnectedCallback) => {
  if (stompClient?.active) {
    return;
  }

  const headers = getAuthHeaders();
  if (!headers.Authorization || headers.Authorization === "Bearer null") {
    console.error(
      "WebSocket connection failed: No token found in localStorage."
    );
    return;
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WEBSOCKET_URL),
    connectHeaders: headers,
    reconnectDelay: 5000,
  });

  stompClient.onConnect = (frame) => {
    console.log("Connect successfully: ", frame);
    // Process pending subscriptions
    pendingSubscriptions.forEach(({ destination, callback }) => {
      subscribe(destination, callback);
    });
    pendingSubscriptions.length = 0;
    if (onConnectedCallback) onConnectedCallback();
  };

  stompClient.onStompError = (frame) => {
    console.error("Broker reported STOMP error: " + frame.headers["message"]);
    console.error("Additional details: " + frame.body);
  };

  stompClient.onWebSocketError = (error) => {
    console.error("WebSocket transport error: ", error);
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
  if (!stompClient || !stompClient.connected) {
    pendingSubscriptions.push({ destination, callback });
    if (!stompClient?.active) {
      connect();
    }
    return;
  }

  if (subscriptions.has(destination)) {
    return;
  }

  const subscription = stompClient.subscribe(destination, (message) => {
    const parsedMessage = JSON.parse(message.body);
    callback(parsedMessage);
  });

  subscriptions.set(destination, subscription);
};

const sendMessage = (destination, body) => {
  if (!stompClient?.connected) {
    console.error("Cannot send message, STOMP client is not connected.");
    return;
  }

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

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient = null;

export const connectWebSocket = (username, onMessageReceived) => {
  // Nếu đã có kết nối đang hoạt động, không làm gì cả
  if (stompClient?.active) return;

  // Tùy chọn cho SockJS để ưu tiên các phương thức kết nối hiện đại
  // và tránh các lỗi 404 không cần thiết từ các fallback cũ.
  const options = {
    transports: ["websocket", "xhr-streaming", "xhr-polling"],
  };

  // Hàm factory để tạo kết nối SockJS, Vite proxy sẽ xử lý đường dẫn '/ws'
  const socketFactory = () => new SockJS("/ws", null, options);

  stompClient = new Client({
    webSocketFactory: socketFactory,

    // Tăng thời gian chờ kết nối để ổn định hơn
    connectTimeout: 5000,
    reconnectDelay: 5000, // Tự động kết nối lại sau 5 giây nếu bị ngắt

    onConnect: () => {
      console.log("WebSocket Connected!");
      // Lắng nghe trên kênh riêng của người dùng
      stompClient.subscribe(`/user/${username}/queue/notifications`, (msg) =>
        onMessageReceived(msg.body)
      );
    },
    onStompError: (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    },
    onWebSocketError: (error) => {
      // Bắt các lỗi ở tầng WebSocket (như lỗi kết nối ban đầu)
      console.error("WebSocket error", error);
    },
  });

  stompClient.activate();
};

export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    console.log("WebSocket disconnected.");
  }
};

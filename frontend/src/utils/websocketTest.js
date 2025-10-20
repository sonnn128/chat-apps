// WebSocket Test Utility
// Sử dụng file này để test WebSocket connection và logging

import { websocketService } from './ws';

export const testWebSocketConnection = () => {
  console.log("🧪 Testing WebSocket connection...");
  
  // Test connection
  websocketService.connect(() => {
    console.log("✅ Test: WebSocket connected successfully");
    
    // Test subscription
    const testDestination = "/test/destination";
    websocketService.subscribe(testDestination, (message) => {
      console.log("📨 Test: Message received:", message);
    });
    
    // Test sending message
    setTimeout(() => {
      websocketService.sendMessage("/test/send", { test: "Hello WebSocket!" });
    }, 1000);
  });
};

export const logWebSocketStatus = () => {
  console.log("📊 WebSocket Status:");
  console.log("- Connected:", websocketService.isConnected());
  console.log("- Service:", websocketService);
};

// Auto-run test in development
if (import.meta.env.DEV) {
  console.log("🔧 Development mode: WebSocket test utilities loaded");
}

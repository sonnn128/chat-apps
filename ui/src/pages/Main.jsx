import { useEffect } from "react";
import { useDispatch } from "react-redux";
import Sidebar from "@/pages/components/Sidebar";
import ChatSection from "@/pages/components/ChatSection";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";
import { websocketService } from "@/utils/ws";
function Main() {
  const dispatch = useDispatch();
  useEffect(() => {
    console.log("🚀 Main: Application initialized");
    // Channels are now loaded after login, no need to load here
  }, [dispatch]);

  useEffect(() => {
    console.log("🔌 Main: Connecting to WebSocket...");
    websocketService.connect(() => {
      console.log("✅ Main: WebSocket connection established successfully");
    });
  }, []);

  return (
    <div className="flex h-screen bg-gray-100 font-sans antialiased overflow-hidden">
      <Sidebar />
      <ChatSection />
    </div>
  );
}

export default Main;

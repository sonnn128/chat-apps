import { useEffect } from "react";
import { useDispatch } from "react-redux";
import Sidebar from "@/components/layout/Sidebar";
import ChatSection from "@/components/layout/ChatSection";
import { websocketService } from "@/utils/ws";

function Main() {
  const dispatch = useDispatch();

  useEffect(() => {
    console.log("🚀 Main: Application initialized");
    // Channels are now loaded after login, no need to load here
  }, [dispatch]);

  useEffect(() => {
    websocketService.connect(() => {
    });
  }, []);

  return (
    <div className="flex h-screen bg-gray-100 font-sans antialiased overflow-hidden">
      <Sidebar />
      <div className="flex-1 flex overflow-hidden">
        <ChatSection />
      </div>
    </div>
  );
}

export default Main;

import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import Sidebar from "@/pages/components/Sidebar";
import ChatSection from "@/pages/components/ChatSection";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";
import { websocketService } from "@/utils/ws";
function Main() {
  const channels = useSelector((state) => state.channel.channels);

  const dispatch = useDispatch();
  useEffect(() => {
    dispatch(fetchAllChannels());
    console.log("Fetch success");
  }, [dispatch]);
  useEffect(() => {
    websocketService.connect();
    return () => {
      websocketService.disconnect();
    };
  }, [dispatch, localStorage.getItem("token")]);

  return (
    <div className="flex h-screen bg-gray-100 font-sans antialiased overflow-hidden">
      {channels.length > 0 && <Sidebar />}
      <ChatSection />
    </div>
  );
}

export default Main;

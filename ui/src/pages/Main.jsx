import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import Sidebar from "@/pages/components/Sidebar";
import ChatSection from "@/pages/components/ChatSection";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";
import {
  fetchFriendList,
  fetchFriendSuggestions,
  fetchPendingRequests,
} from "@/stores/middlewares/friendShipMiddleware";
function Main() {
  const dispatch = useDispatch();
  const messagesOfCurrentChannel = useSelector(
    (state) => state.channel.messagesOfCurrentChannel
  );
  console.log("messagesOfCurrentChannel: ", messagesOfCurrentChannel);
  
  useEffect(() => {
    // dispatch(fetchFriendList());
    // dispatch(fetchPendingRequests());
    // dispatch(fetchFriendSuggestions());
    dispatch(fetchAllChannels());
    // dispatch(fetchFriendList());
    console.log("Fetch success");
  }, [dispatch]);

  return (
    <div className="flex h-screen bg-gray-100 font-sans antialiased overflow-hidden">
      <Sidebar />
      <ChatSection />
    </div>
  );
}

export default Main;

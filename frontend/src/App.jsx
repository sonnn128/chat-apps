import React, { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";

import { fetchUserProfile } from "@/stores/middlewares/authMiddleware";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";
import {
  fetchPendingRequests,
  fetchFriendList,
} from "@/stores/middlewares/friendShipMiddleware";
import Loading from "@/components/Loading";
import Login from "@/pages/Login";
import Main from "@/pages/Main";
import Register from "@/pages/Register";
import Settings from "@/pages/Settings";
import AvatarDebug from "@/components/AvatarDebug";

function App() {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const token = useSelector((state) => state.auth.token);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      if (token) {
        setIsLoading(true);
        try {
          await dispatch(fetchUserProfile()).unwrap();
          await Promise.all([
            dispatch(fetchAllChannels()).unwrap(),
            dispatch(fetchFriendList()).unwrap(),
            dispatch(fetchPendingRequests()).unwrap()
          ]);
        } catch (error) {
          console.error("Failed to load initial data:", error);
        } finally {
          setIsLoading(false);
        }
      }
    };
    fetchProfile();
  }, [token, dispatch]);

  // Auto-fetch channels and friends when user is loaded (for both login and reload)


  if (isLoading) {
    return <Loading />;
  }

  return (
    <Routes>
      <Route path="/" element={user ? <Main /> : <Navigate to="/login" />} />
      <Route path="/login" element={!user ? <Login /> : <Navigate to="/" />} />
      <Route
        path="/register"
        element={!user ? <Register /> : <Navigate to="/login" />}
      />
      <Route
        path="/settings"
        element={user ? <Settings /> : <Navigate to="/login" />}
      />
      <Route path="/debug/avatar" element={<AvatarDebug />} />
    </Routes>
  );
}

export default App;

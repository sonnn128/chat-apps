import React, { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";

import { fetchUserProfile } from "@/stores/middlewares/authMiddleware";
import Loading from "@/components/Loading";
import Login from "@/pages/Login";
import Main from "@/pages/Main";
import Register from "@/pages/Register";
import Settings from "@/pages/Settings";

function App() {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const token = useSelector((state) => state.auth.token);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      if (token) {
        setIsLoading(true);
        dispatch(fetchUserProfile());
        setIsLoading(false);
      }
    };
    fetchProfile();
  }, [token, dispatch]);

  if (isLoading) {
    return <Loading />;
  }

  return (
    <>
      <Routes>
        <Route path="/" element={user ? <Main /> : <Navigate to="/login" />} />
        <Route
          path="/login"
          element={!user ? <Login /> : <Navigate to="/" />}
        />
        <Route
          path="/register"
          element={!user ? <Register /> : <Navigate to="/" />}
        />
        <Route
          path="/settings"
          element={user ? <Settings /> : <Navigate to="/login" />}
        />
      </Routes>
    </>
  );
}

export default App;

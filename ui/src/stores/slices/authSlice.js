import { createSlice } from "@reduxjs/toolkit";
import {
  fetchUserProfile,
  loginUser,
} from "@/stores/middlewares/authMiddleware";
import { fetchAllChannels } from "@/stores/middlewares/channelMiddleware";

const initialState = {
  user: null,
  token: localStorage.getItem("token") || null,
  status: "idle",
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.status = "idle";
      localStorage.removeItem("token");
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserProfile.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload;
        state.isLogin = true;
        console.log("✅ Auth: User profile loaded, triggering channel fetch...");
        // Note: We can't dispatch here directly, so we'll handle this in App.jsx
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload.user;
        state.token = action.payload.accessToken;
        state.isLogin = true;
        localStorage.setItem("token", action.payload.accessToken);
        console.log("✅ Auth: Login successful, triggering channel fetch...");
        // Note: We can't dispatch here directly, so we'll handle this in App.jsx
      });
  },
});

export const { logout } = authSlice.actions;

export default authSlice.reducer;

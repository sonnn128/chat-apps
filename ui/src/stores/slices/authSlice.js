import { createSlice } from "@reduxjs/toolkit";
import {
  fetchUserProfile,
  loginUser,
} from "@/stores/middlewares/authMiddleware";

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
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload;
        state.token = action.payload.token;
        state.isLogin = true;
        localStorage.setItem("token", action.payload.token);
      });
  },
});

export const { logout } = authSlice.actions;

export default authSlice.reducer;

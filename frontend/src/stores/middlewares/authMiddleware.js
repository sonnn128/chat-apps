import { createAsyncThunk } from "@reduxjs/toolkit";
import authService from "../../services/authService";

export const fetchUserProfile = createAsyncThunk(
  "auth/fetchUserProfile",
  async () => await authService.getUserProfile()
);

export const loginUser = createAsyncThunk(
  "auth/loginUser",
  async ({ email, password }) => {
    const res = await authService.login({ email, password })
    return res.data;
  }
);

export const registerUser = createAsyncThunk(
  "auth/registerUser",
  async (registerData) => await authService.register(registerData)
);

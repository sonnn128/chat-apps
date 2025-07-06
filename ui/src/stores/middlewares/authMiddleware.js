import { createAsyncThunk } from "@reduxjs/toolkit";
import authServices from "@/services/authServices";

export const fetchUserProfile = createAsyncThunk(
  "auth/fetchUserProfile",
  async () => await authServices.getUserProfile()
);

export const loginUser = createAsyncThunk(
  "auth/loginUser",
  async ({ email, password }) => {
    const res = await authServices.login({ email, password })
    return res.data
  }
);

export const registerUser = createAsyncThunk(
  "auth/registerUser",
  async (registerData) => await authServices.register(registerData)
);


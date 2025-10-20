import { createAsyncThunk } from "@reduxjs/toolkit";
import profileService from "@/services/profileService";

export const fetchProfile = createAsyncThunk(
  "profile/fetchProfile",
  async () => await profileService.getProfile()
);

export const updateProfile = createAsyncThunk(
  "profile/updateProfile",
  async (profileData) => await profileService.updateProfile(profileData)
);

export const uploadAvatar = createAsyncThunk(
  "profile/uploadAvatar",
  async (file) => await profileService.uploadAvatar(file)
);

// app/store.js
import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import channelReducer from "./slices/channelSlice";
import friendshipReducer from "./slices/friendshipSlice";
import profileReducer from "./slices/profileSlice";

const store = configureStore({
  reducer: {
    auth: authReducer,
    channel: channelReducer,
    friendship: friendshipReducer,
    profile: profileReducer,
  },
});

export default store;

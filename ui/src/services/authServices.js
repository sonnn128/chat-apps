import { getAuthHeaders } from "@/utils/authUtils";
import { get, post } from "@/utils/httpRequest";
import { successToast } from "@/utils/toast";

const AUTH_API = "auth"; 

const login = async (credentials) => {
  const response = await post(`${AUTH_API}/login`, credentials);
  if (response.token) {
    localStorage.setItem("token", response.token);
  }
  return response;
};

const register = async (userData) => post(`${AUTH_API}/register`, userData);

const refreshToken = async () => {
  const token = localStorage.getItem("token");
  if (!token) return null;
  
  try {
    const response = await post(`${AUTH_API}/refresh`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (response.token) {
      localStorage.setItem("token", response.token);
    }
    return response;
  } catch (error) {
    localStorage.removeItem("token");
    return null;
  }
};

const logout = async () => {
  const token = localStorage.getItem("token");
  if (token) {
    try {
      await post(`${AUTH_API}/logout`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
    } catch (error) {
      console.error("Logout error:", error);
    }
  }
  localStorage.removeItem("token");
  successToast("Log out success");
};

const getUserProfile = async () =>
  get(`users/me`, { headers: getAuthHeaders() });

const authServices = {
  login,
  register,
  refreshToken,
  logout,
  getUserProfile,
};

export default authServices;

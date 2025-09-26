import { getAuthHeaders } from "@/utils/authUtils";
import { get, put } from "@/utils/httpRequest";

const PROFILE_API = "users";

const getProfile = async () => {
  const response = await get(`${PROFILE_API}/me`, { 
    headers: getAuthHeaders() 
  });
  return response;
};

const updateProfile = async (profileData) => {
  const response = await put(`${PROFILE_API}/me`, profileData, { 
    headers: getAuthHeaders() 
  });
  return response;
};

const uploadAvatar = async (file) => {
  const formData = new FormData();
  formData.append('avatar', file);
  
  const response = await put(`${PROFILE_API}/me/avatar`, formData, { 
    headers: {
      ...getAuthHeaders(),
      'Content-Type': 'multipart/form-data'
    }
  });
  return response;
};

const profileService = {
  getProfile,
  updateProfile,
  uploadAvatar
};

export default profileService;

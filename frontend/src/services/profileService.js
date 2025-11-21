import { getAuthHeaders } from "@/utils/authUtils";
import { get, put } from "@/utils/httpRequest";

const PROFILE_API = "me";
// ...
const getProfile = async () => {
  const response = await get(`${PROFILE_API}`, { 
    headers: getAuthHeaders() 
  });
  return response;
};

const updateProfile = async (profileData) => {
  const response = await put(`${PROFILE_API}`, profileData, { 
    headers: getAuthHeaders() 
  });
  return response;
};

const uploadAvatar = async (file) => {
  const formData = new FormData();
  formData.append('avatar', file);
  
  const response = await put(`${PROFILE_API}/avatar`, formData, { 
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

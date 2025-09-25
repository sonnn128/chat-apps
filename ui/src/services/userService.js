import { getAuthHeaders } from "@/utils/authUtils";
import { get } from "@/utils/httpRequest";

const USER_API = "/users";

const searchUserByPhone = async (phone) =>
  get(`${USER_API}/search/phone?phone=${encodeURIComponent(phone)}`, { 
    headers: getAuthHeaders() 
  });

const getUserById = async (userId) =>
  get(`${USER_API}/${userId}`, { 
    headers: getAuthHeaders() 
  });

const userService = {
  searchUserByPhone,
  getUserById
};

export default userService;

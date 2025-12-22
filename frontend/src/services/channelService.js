import { getAuthHeaders } from "@/utils/authUtils";
import { get, post, del, patch } from "@/utils/httpRequest";

const CHANNEL_API = "/channels";

const createChannel = async (form) =>
  post(CHANNEL_API, form , { headers: getAuthHeaders() });

const getChannels = async () => {
  const response = await get(CHANNEL_API, { headers: getAuthHeaders() });
  return response;
};

const deleteChannel = async (channelId) => del(`${CHANNEL_API}/${channelId}`, { headers: getAuthHeaders() });

const getChannelById = async (channelId) =>
  get(`${CHANNEL_API}/${channelId}`, { headers: getAuthHeaders() });

const addMembersToChannel = async (channelId, userIds) =>
  post(`${CHANNEL_API}/${channelId}/members`, {userIds}, { headers: getAuthHeaders() });

const addPeopleToChannel = async (channelId, memberIds) =>
  post(`${CHANNEL_API}/${channelId}/add-people`, {memberIds}, { headers: getAuthHeaders() });

const getOrCreateDirectChannel = async (friendId) => {
  // Note: Sorting of UUIDs is handled on the backend
  // The backend will ensure consistent channel creation regardless of parameter order
  return post(`${CHANNEL_API}/direct`, { friendId }, { headers: getAuthHeaders() });
};

const updateChannelAvatar = async (channelId, avatarUrl) =>
  patch(`${CHANNEL_API}/${channelId}/avatar`, { avatarUrl }, { headers: getAuthHeaders() });

const updateChannelName = async (channelId, channelName) =>
  patch(`${CHANNEL_API}/${channelId}/name`, { channelName }, { headers: getAuthHeaders() });

const updateChannelTheme = async (channelId, themeColor, themeGradient) =>
  patch(`${CHANNEL_API}/${channelId}/theme`, { themeColor, themeGradient }, { headers: getAuthHeaders() });

const removeMember = async (channelId, memberId) =>
  del(`${CHANNEL_API}/${channelId}/members/${memberId}`, { headers: getAuthHeaders() });

const markAsRead = async (channelId) =>
  post(`${CHANNEL_API}/${channelId}/read`, {}, { headers: getAuthHeaders() });

const channelService = {
  createChannel,
  getChannels,
  getChannelById,
  addMembersToChannel,
  addPeopleToChannel,
  deleteChannel,
  getOrCreateDirectChannel,
  updateChannelAvatar,
  updateChannelName,
  updateChannelTheme,
  removeMember,
  markAsRead
};

export default channelService;

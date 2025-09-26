import { getAuthHeaders } from "@/utils/authUtils";
import { get, post, del } from "@/utils/httpRequest";

const CHANNEL_API = "/channels";

const createChannel = async (form) =>
  post(CHANNEL_API, form , { headers: getAuthHeaders() });

const getChannels = async () => {
  console.log("📋 ChannelService: Making API call to get channels...");
  const response = await get(CHANNEL_API, { headers: getAuthHeaders() });
  console.log("📋 ChannelService: Full API response:", response);
  console.log("📋 ChannelService: Response type:", typeof response);
  console.log("📋 ChannelService: Response keys:", Object.keys(response || {}));
  console.log("📋 ChannelService: Response.success:", response?.success);
  console.log("📋 ChannelService: Response.data:", response?.data);
  console.log("📋 ChannelService: Response.data length:", response?.data?.length);
  return response;
};

const deleteChannel = async (channelId) => del(`${CHANNEL_API}/${channelId}`, { headers: getAuthHeaders() });

const getChannelById = async (channelId) =>
  get(`${CHANNEL_API}/${channelId}`, { headers: getAuthHeaders() });

const addMembersToChannel = async (channelId, userIds) =>
  post(`${CHANNEL_API}/${channelId}/members`, {userIds}, { headers: getAuthHeaders() });

const addPeopleToChannel = async (channelId, memberIds) =>
  post(`${CHANNEL_API}/${channelId}/add-people`, {memberIds}, { headers: getAuthHeaders() });

const channelService = {
  createChannel,
  getChannels,
  getChannelById,
  addMembersToChannel,
  addPeopleToChannel,
  deleteChannel
};

export default channelService;

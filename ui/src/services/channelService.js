import { getAuthHeaders } from "@/utils/authUtils";
import { get, post } from "@/utils/httpRequest";

const CHANNEL_API = "/channels";

const createChannel = async (name) =>
  post(CHANNEL_API, { name }, { headers: getAuthHeaders() });

const getChannels = async () => get(CHANNEL_API, { headers: getAuthHeaders() });

const getChannelById = async (channelId) =>
  get(`${CHANNEL_API}/${channelId}`, { headers: getAuthHeaders() });

const getAllMembersOfChannel = async (channelId) =>
  get(`${CHANNEL_API}/${channelId}/members`, { headers: getAuthHeaders() });

const addMembersToChannel = async (channelId, userIds) =>
  post(`${CHANNEL_API}/${channelId}/members`, {userIds}, { headers: getAuthHeaders() });

const channelService = {
  createChannel,
  getChannels,
  getChannelById,
  getAllMembersOfChannel,
  addMembersToChannel
};

export default channelService;

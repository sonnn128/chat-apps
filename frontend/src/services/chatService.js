import { getAuthHeaders } from "@/utils/authUtils";
import { post, get, del } from "@/utils/httpRequest";

const CHAT_API = "/messages";

const sendChannelMessage = async (form) =>
  post(CHAT_API, form, { headers: getAuthHeaders() });

const getChannelMessages = async (channelId, params = {}) => {
  const queryParams = new URLSearchParams({
    page: params.page || 0,
    pageSize: params.pageSize || 20,
    ...(params.before && { before: params.before }),
  });
  
  const url = `${CHAT_API}/${channelId}?${queryParams}`;
  return get(url, { headers: getAuthHeaders() });
};

const deleteMessage = async (channelId, messageId) =>
  del(`${CHAT_API}/${channelId}/${messageId}`, { headers: getAuthHeaders() });

const chatService = {
  sendChannelMessage,
  getChannelMessages,
  deleteMessage,
};

export default chatService;

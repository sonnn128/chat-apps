import { getAuthHeaders } from "@/utils/authUtils";
import { post } from "@/utils/httpRequest";

const CHAT_API = "/messages";

const sendChannelMessage = async (form) =>
  post(CHAT_API, form, { headers: getAuthHeaders() });

const chatService = {
  sendChannelMessage,
};

export default chatService;

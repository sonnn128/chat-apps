import { getAuthHeaders } from "@/utils/authUtils";
import { get, post, put, del } from "@/utils/httpRequest";

const FRIENDSHIP_API = "/friendships";

const friendshipService = {
  sendFriendRequest: async (friendId) => {
    const res = await post(
      `${FRIENDSHIP_API}/request/${friendId}`,
      {},
      { headers: getAuthHeaders() }
    );
    return res.data;
  },

  acceptFriendRequest: async (friendId) => {
    const res = await put(
      `${FRIENDSHIP_API}/accept/${friendId}`,
      {},
      { headers: getAuthHeaders() }
    );
    return res.data;
  },

  rejectFriendRequest: async (friendId) => {
    const res = await del(
      `${FRIENDSHIP_API}/reject/${friendId}`,
      { headers: getAuthHeaders() }
    );
    return res.data;
  },

  cancelFriendRequest: async (friendId) => {
    const res = await del(
      `${FRIENDSHIP_API}/cancel/${friendId}`,
      { headers: getAuthHeaders() }
    );
    return res.data;
  },

  unfriendUser: async (friendId) => {
    const res = await del(
      `${FRIENDSHIP_API}/unfriend/${friendId}`,
      { headers: getAuthHeaders() }
    );
    return res.data;
  },


  getFriendList: async () => {
    const res = await get(`${FRIENDSHIP_API}`, { headers: getAuthHeaders() });
    return res.data;
  },

  getPendingRequests: async () =>
    get(`${FRIENDSHIP_API}/pending`, { headers: getAuthHeaders() }),

};

export default friendshipService;
ư
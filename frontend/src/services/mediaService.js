import { getAuthHeaders } from "@/utils/authUtils";
import { post, del, get } from "@/utils/httpRequest";

const MEDIA_API = "/media";

const uploadFile = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return post(`${MEDIA_API}/upload`, formData, {
    headers: {
      ...getAuthHeaders(),
      "Content-Type": "multipart/form-data",
    },
  });
};

const getFileUrl = async (publicId) =>
  get(`${MEDIA_API}/url/${publicId}`, { headers: getAuthHeaders() });

const deleteFile = async (publicId) =>
  del(`${MEDIA_API}/${publicId}`, { headers: getAuthHeaders() });

const mediaService = {
  uploadFile,
  getFileUrl,
  deleteFile,
};

export default mediaService;

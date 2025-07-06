import axios from "axios";
import { errorToast } from "./toast";

const httpRequest = axios.create({
  baseURL: import.meta.env.VITE_REACT_APP_BASE_URL,
  timeout: 5000,
});

const handleRequest = async (callback) => {
  try {
    const response = await callback();
    return response.data;
  } catch (error) {
    console.error("API Error:", error);
    errorToast(error.response?.data?.message || "Something went wrong");
    throw error;
  }
};

export const get = (url, options = {}) => handleRequest(() => httpRequest.get(url, options));
export const post = (url, data, options = {}) => handleRequest(() => httpRequest.post(url, data, options));
export const put = (url, data, options = {}) => handleRequest(() => httpRequest.put(url, data, options));
export const del = (url, options = {}) => handleRequest(() => httpRequest.delete(url, options));

export default httpRequest;

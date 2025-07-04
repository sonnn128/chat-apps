const getAuthHeaders = () => {
  const token = localStorage.getItem("jwt_token");
  return { Authorization: `Bearer ${token}` };
};
export { getAuthHeaders };

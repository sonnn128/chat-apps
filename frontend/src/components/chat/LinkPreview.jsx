import React, { useState, useEffect } from "react";
import { Skeleton } from "antd";
import { LinkOutlined } from "@ant-design/icons";
import { post } from "@/utils/httpRequest";
import { getAuthHeaders } from "@/utils/authUtils";

const LinkPreview = ({ url, isCurrentUser = false }) => {
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPreview = async () => {
      try {
        setLoading(true);

        const data = await post("/messages/link-preview", { url }, { headers: getAuthHeaders() });
        setPreview(data);
      } catch (err) {
        console.error("Failed to fetch link preview:", err);
      } finally {
        setLoading(false);
      }
    };

    if (url) {
      fetchPreview();
    }
  }, [url]);

  if (loading) {
    return <Skeleton.Input active size="small" style={{ width: '200px' }} />;
  }

  const domain = new URL(url).hostname.replace('www.', '');
  const title = preview?.title || domain;

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className="flex items-center gap-2 w-full"
    >
      <LinkOutlined className={`flex-shrink-0 text-sm ${isCurrentUser ? 'text-white' : 'text-gray-600'}`} />
      <div className="flex-1 min-w-0">
        <div className={`font-medium text-xs ${isCurrentUser ? 'text-white' : 'text-gray-900'}`}>
          {domain}
        </div>
        <div className={`text-xs truncate ${isCurrentUser ? 'text-gray-200' : 'text-gray-500'}`}>
          {url}
        </div>
      </div>
    </a>
  );
};

export default LinkPreview;

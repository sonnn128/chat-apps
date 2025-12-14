import React, { useState, useEffect } from "react";
import { Skeleton, Button } from "antd";
import { CloseOutlined, GlobalOutlined } from "@ant-design/icons";

const LinkPreviewComposer = ({ url, onRemove }) => {
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    const fetchPreview = async () => {
      try {
        setLoading(true);
        setError(false);
        setImageError(false);

        const response = await fetch("/api/link-preview", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ url }),
          timeout: 10000,
        });

        if (response.ok) {
          const data = await response.json();
          if (Object.keys(data).length === 0) {
            setError(true);
          } else {
            setPreview(data);
          }
        } else {
          setError(true);
        }
      } catch (err) {
        console.error("Failed to fetch link preview:", err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    if (url) {
      fetchPreview();
    }
  }, [url]);

  if (loading) {
    return (
      <div className="relative bg-white rounded-lg border border-gray-200 overflow-hidden">
        <Skeleton.Image active style={{ width: '100%', height: '140px' }} />
        <div className="p-2">
          <Skeleton paragraph={{ rows: 1 }} />
        </div>
      </div>
    );
  }

  if (error || !preview) {
    return (
      <div className="relative bg-white rounded-lg border border-gray-200 p-3 flex items-center justify-between">
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <GlobalOutlined className="text-gray-500 flex-shrink-0" />
          <div className="truncate text-sm text-gray-600">{url}</div>
        </div>
        <Button
          type="text"
          size="small"
          icon={<CloseOutlined />}
          onClick={onRemove}
          className="flex-shrink-0"
        />
      </div>
    );
  }

  const domain = new URL(url).hostname.replace('www.', '');
  const hasImage = preview.image && !imageError;

  return (
    <div className="relative bg-white rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
      {/* Image Section */}
      {hasImage && (
        <div className="relative bg-gray-200 h-40 overflow-hidden group">
          <img
            src={preview.image}
            alt={preview.title || "preview"}
            className="w-full h-full object-cover"
            onError={() => setImageError(true)}
          />
          {/* URL Button Overlay */}
          <div className="absolute inset-0 flex items-end justify-center pb-3">
            <div className="bg-blue-500 text-white px-3 py-1.5 rounded-full text-xs font-semibold truncate max-w-[90%]">
              {url}
            </div>
          </div>
        </div>
      )}

      {/* Content Section */}
      <div className="p-2">
        {preview.title && (
          <div className="font-semibold text-gray-900 text-sm line-clamp-2">
            {preview.title}
          </div>
        )}
        {preview.description && (
          <div className="text-gray-600 text-xs mt-1 line-clamp-1">
            {preview.description}
          </div>
        )}
        <div className="text-gray-500 text-xs mt-2 flex items-center gap-1">
          <GlobalOutlined style={{ fontSize: '10px' }} />
          {domain}
        </div>
      </div>

      {/* Remove Button */}
      <Button
        type="text"
        size="small"
        icon={<CloseOutlined />}
        onClick={onRemove}
        className="absolute top-2 right-2 bg-white/80 hover:bg-white shadow-sm"
      />
    </div>
  );
};

export default LinkPreviewComposer;

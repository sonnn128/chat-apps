import React, { useState, useEffect } from "react";
import { Modal, Tabs, Empty, Spin, Image } from "antd";
import { FileOutlined, LinkOutlined, PlayCircleOutlined, FileTextOutlined } from "@ant-design/icons";
import { useSelector } from "react-redux";

const MediaFilesLinksModal = ({ open: visible, onClose }) => {
  const { channels, currentChannelId } = useSelector((state) => state.channel);
  const currentChannel = channels.find((ch) => ch.id === currentChannelId);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("media");

  // Extract media, files, and links from channel messages
  const extractContent = () => {
    const messages = currentChannel?.messages || [];
    const media = [];
    const files = [];
    const links = [];

    messages.forEach((msg) => {
      try {
        if (msg.type === "IMAGE") {
          media.push({
            id: msg.key?.messageId || msg.id,
            url: msg.content,
            type: "image",
            timestamp: msg.key?.timestamp || msg.timestamp,
            senderName: msg.senderName,
          });
        } else if (msg.type === "VIDEO") {
          media.push({
            id: msg.key?.messageId || msg.id,
            url: msg.content,
            type: "video",
            timestamp: msg.key?.timestamp || msg.timestamp,
            senderName: msg.senderName,
          });
        } else if (msg.type === "FILE") {
          try {
            const fileData = JSON.parse(msg.content);
            files.push({
              id: msg.key?.messageId || msg.id,
              name: fileData.name,
              url: fileData.url,
              size: fileData.size,
              timestamp: msg.key?.timestamp || msg.timestamp,
              senderName: msg.senderName,
            });
          } catch (e) {
            console.error("Error parsing file data:", e);
          }
        } else if (msg.type === "CHAT" && msg.content) {
          // Extract URLs from chat messages
          const urlRegex = /(https?:\/\/[^\s]+)/g;
          const matches = msg.content.match(urlRegex);
          if (matches) {
            matches.forEach((url) => {
              links.push({
                id: `${msg.key?.messageId || msg.id}-${url}`,
                url,
                title: url,
                timestamp: msg.key?.timestamp || msg.timestamp,
                senderName: msg.senderName,
              });
            });
          }
        }
      } catch (e) {
        console.error("Error extracting content:", e);
      }
    });

    return { media, files, links };
  };

  const { media, files, links } = extractContent();

  const formatFileSize = (bytes) => {
    if (!bytes) return "Unknown";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return "";
    try {
      return new Date(parseInt(timestamp)).toLocaleDateString();
    } catch {
      return "";
    }
  };

  return (
    <Modal
      title="Media, Files and Links"
      open={visible}
      onCancel={onClose}
      footer={null}
      width={800}
      styles={{ body: { maxHeight: "600px", overflowY: "auto" } }}
    >
      <Spin spinning={loading}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: "media",
              label: (
                <span>
                  <PlayCircleOutlined className="mr-2" />
                  Media ({media.length})
                </span>
              ),
              children:
                media.length === 0 ? (
                  <Empty description="No media found" />
                ) : (
                  <div className="grid grid-cols-3 gap-4">
                    {media.map((item) => (
                      <a
                        key={item.id}
                        href={item.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="group relative overflow-hidden rounded-lg"
                      >
                        {item.type === "image" ? (
                          <img
                            src={item.url}
                            alt="media"
                            className="w-full h-32 object-cover hover:opacity-80 transition-opacity"
                          />
                        ) : (
                          <div className="w-full h-32 bg-gray-200 flex items-center justify-center hover:bg-gray-300 transition-colors">
                            <PlayCircleOutlined style={{ fontSize: "24px" }} />
                          </div>
                        )}
                        <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity bg-black bg-opacity-20 flex items-center justify-center">
                          <span className="text-white text-xs">{item.senderName}</span>
                        </div>
                      </a>
                    ))}
                  </div>
                ),
            },
            {
              key: "files",
              label: (
                <span>
                  <FileOutlined className="mr-2" />
                  Files ({files.length})
                </span>
              ),
              children:
                files.length === 0 ? (
                  <Empty description="No files found" />
                ) : (
                  <div className="space-y-2">
                    {files.map((file) => (
                      <a
                        key={file.id}
                        href={file.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-3 p-3 hover:bg-gray-50 rounded-lg border border-gray-200 transition-colors"
                      >
                        <FileTextOutlined style={{ fontSize: "20px", color: "#1890ff" }} />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold truncate text-gray-900">
                            {file.name}
                          </p>
                          <p className="text-xs text-gray-500">
                            {formatFileSize(file.size)} • {file.senderName}
                          </p>
                        </div>
                      </a>
                    ))}
                  </div>
                ),
            },
            {
              key: "links",
              label: (
                <span>
                  <LinkOutlined className="mr-2" />
                  Links ({links.length})
                </span>
              ),
              children:
                links.length === 0 ? (
                  <Empty description="No links found" />
                ) : (
                  <div className="space-y-2">
                    {links.map((link) => (
                      <a
                        key={link.id}
                        href={link.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-3 p-3 hover:bg-gray-50 rounded-lg border border-gray-200 transition-colors"
                      >
                        <LinkOutlined style={{ fontSize: "16px", color: "#1890ff" }} />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold truncate text-blue-500 hover:underline">
                            {link.url.substring(0, 50)}...
                          </p>
                          <p className="text-xs text-gray-500">
                            {link.senderName}
                          </p>
                        </div>
                      </a>
                    ))}
                  </div>
                ),
            },
          ]}
        />
      </Spin>
    </Modal>
  );
};

export default MediaFilesLinksModal;

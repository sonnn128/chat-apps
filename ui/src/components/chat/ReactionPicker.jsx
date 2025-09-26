import React, { useRef, useEffect } from "react";
import { Button, Tooltip } from "antd";
import { SmileOutlined } from "@ant-design/icons";
import EmojiPicker from "emoji-picker-react";
import PropTypes from "prop-types";

const ReactionPicker = ({ 
  isVisible, 
  onClose, 
  onReactionSelect, 
  position = { x: 0, y: 0 } 
}) => {
  const pickerRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (pickerRef.current && !pickerRef.current.contains(event.target)) {
        onClose();
      }
    };

    if (isVisible) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isVisible, onClose]);

  const handleEmojiClick = (emojiObject) => {
    console.log("Reaction emoji clicked:", emojiObject);
    onReactionSelect(emojiObject.emoji);
    onClose();
  };

  if (!isVisible) return null;

  return (
    <div
      ref={pickerRef}
      className="absolute z-50"
      style={{
        left: position.x,
        top: position.y,
      }}
    >
      <div className="bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden">
        <EmojiPicker
          onEmojiClick={handleEmojiClick}
          autoFocusSearch={false}
          searchDisabled={false}
          width={300}
          height={300}
          lazyLoadEmojis={true}
          searchPlaceholder="Tìm emoji..."
          previewConfig={{
            showPreview: false,
          }}
          suggestedEmojisMode="recent"
          skinTonePickerLocation="SEARCH"
          emojiStyle="native"
          theme="light"
        />
      </div>
    </div>
  );
};

ReactionPicker.propTypes = {
  isVisible: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onReactionSelect: PropTypes.func.isRequired,
  position: PropTypes.shape({
    x: PropTypes.number,
    y: PropTypes.number,
  }),
};

export default ReactionPicker;

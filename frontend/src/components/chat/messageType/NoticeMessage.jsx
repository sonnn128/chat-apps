import React from "react";

const NoticeMessage = ({ content }) => (
  <div className="flex justify-center my-2">
    <div className="bg-gray-200 text-gray-700 text-xs px-3 py-1 rounded-full">
      {content}
    </div>
  </div>
);
export default NoticeMessage;

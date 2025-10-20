import React from 'react';

/**
 * Test component để kiểm tra add people functionality
 * Có thể xóa sau khi test xong
 */
const AddPeopleTest = () => {
  const mockAddPeopleResponse = {
    success: true,
    message: "People added to channel successfully",
    data: {
      channelId: "test-channel-id",
      channelName: "Test Channel",
      newMembers: [
        {
          id: "user-1",
          firstname: "John",
          lastname: "Doe",
          email: "john.doe@example.com",
          avatarUrl: "https://via.placeholder.com/40x40?text=JD"
        },
        {
          id: "user-2", 
          firstname: "Jane",
          lastname: "Smith",
          email: "jane.smith@example.com",
          avatarUrl: null
        }
      ],
      message: "People have been added to the channel successfully"
    }
  };

  return (
    <div className="p-4 border rounded-lg">
      <h3 className="text-lg font-semibold mb-4">Add People Test</h3>
      
      <div className="space-y-4">
        <div>
          <h4 className="font-medium mb-2">Mock API Response:</h4>
          <pre className="bg-gray-100 p-2 rounded text-xs overflow-auto">
            {JSON.stringify(mockAddPeopleResponse, null, 2)}
          </pre>
        </div>
        
        <div>
          <h4 className="font-medium mb-2">Expected UI Display:</h4>
          <div className="space-y-2">
            {mockAddPeopleResponse.data.newMembers.map((member, index) => (
              <div key={index} className="flex items-center gap-3 p-2 bg-gray-50 rounded">
                <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
                  {member.avatarUrl ? (
                    <img src={member.avatarUrl} alt="Avatar" className="w-8 h-8 rounded-full" />
                  ) : (
                    <span className="text-sm font-medium">
                      {member.firstname?.[0] || '?'}
                    </span>
                  )}
                </div>
                <div>
                  <div className="text-sm font-medium">
                    {member.firstname} {member.lastname}
                  </div>
                  <div className="text-xs text-gray-500">
                    {member.email}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddPeopleTest;

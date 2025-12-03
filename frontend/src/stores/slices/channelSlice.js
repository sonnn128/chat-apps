import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  addMembersToChannel,
  addPeopleToChannel,
  sendChannelMessage,
  fetchDeleteChannel,
} from "@/stores/middlewares/channelMiddleware";

const initialState = {
  channels: [],
  currentChannelId: null,
  currentChannel: null,
  error: null,
  status: "idle",
  // Message cache for each channel
  messageCache: {}, // Cache messages for each channel
  preloadedChannels: {}, // Track which channels have been preloaded (channelId: true)
};

const channelSlice = createSlice({
  name: "channels",
  initialState,
  reducers: {
    createChannel: (state, action) => {
      const channelData = action.payload;
      const newChannel = {
        id: channelData.id,
        channelName: channelData.channelName,
        createdAt: channelData.createdAt,
        messages: channelData.message ? [channelData.message] : [],
        memberIds: channelData.memberIds || [],
        participants: channelData.participants || []
      };
      state.channels.push(newChannel);
    },
    addChannel: (state, action) => {
      const channelData = action.payload;
      const newChannel = {
        id: channelData.channelId,
        channelName: channelData.channelName,
        createdAt: channelData.createdAt,
        messages: [],
        memberIds: channelData.memberIds || [],
        participants: channelData.participants || (channelData.memberIds ? 
          channelData.memberIds.map(memberId => ({
            userId: memberId,
            firstname: `User`,
            lastname: memberId.substring(0, 8),
            name: `User ${memberId.substring(0, 8)}`, // Fallback for backward compatibility
            email: `${memberId.substring(0, 8)}@example.com`, // Temporary email
            avatar: null,
            role: 'MEMBER' // Default role in uppercase
          })) : [])
      };
      
      // Check if channel already exists to avoid duplicates
      const existingChannel = state.channels.find(ch => ch.id === channelData.channelId);
      if (!existingChannel) {
        state.channels.push(newChannel);

      } else {

      }
    },
    updateChannel: (state, action) => {
      const updatedChannel = action.payload;
      const index = state.channels.findIndex(c => c.id === updatedChannel.id);
      if (index !== -1) {
        // Merge updates
        state.channels[index] = { ...state.channels[index], ...updatedChannel };
        
        // Update current channel if it matches
        if (state.currentChannel && state.currentChannel.id === updatedChannel.id) {
          state.currentChannel = { ...state.currentChannel, ...updatedChannel };
        }
      }
    },
    setChannels: (state, action) => {
      state.channels = action.payload;
    },
    selectChannel: (state, action) => {
      state.currentChannel = action.payload;
    },
    removeChannel: (state, action) => {
      state.channels = state.channels.filter(
        (channel) => channel.id !== action.payload
      );
      if (state.currentChannel?.id === action.payload) {
        state.currentChannel = null;
      }
    },
    removeCurrentChannel: (state) => {
      state.currentChannel = null;
      state.currentChannelId = null;
      state.messagesOfCurrentChannel = [];
    },

    setCurrentChannel: (state, action) => {
      const channel = action.payload;
      state.currentChannel = channel;
      state.currentChannelId = channel?.id || null;
    },
    receiveMessage: (state, action) => {
      const channelId = action.payload.key.channelId;
      const messageId = action.payload.key.messageId;
      const channelIndex = state.channels.findIndex((item) => item.id == channelId);
      let channelFind = channelIndex !== -1 ? state.channels[channelIndex] : null;
      
      // Add senderName for real-time messages
      const messageWithSender = {
        ...action.payload,
        senderName: action.payload.senderName || "Unknown User",
        senderAvatar: action.payload.senderAvatar || null
      };
      
      if (channelFind) {
        if (!channelFind.messages) {
          channelFind.messages = [];
        }
        // Check if message already exists (deduplication)
        const messageExists = channelFind.messages.some(msg => {
            const msgId = msg.key?.messageId || msg.id;
            return msgId === messageId;
        });
        if (!messageExists) {
          channelFind.messages.push(messageWithSender);

        } else {

        }
        
        // Move channel to top
        if (channelIndex > 0) {
            const [movedChannel] = state.channels.splice(channelIndex, 1);
            state.channels.unshift(movedChannel);
        }
      } else {

        
        // If this is a notice message (server-created notice such as friend-connect), create a basic channel
        // This covers different notice text/locales (e.g., "You are connected on messenger")
        if (action.payload.type === "NOTICE") {

          const newChannel = {
            id: channelId,
            channelName: "New Channel", // Will be updated when user clicks on it or when channel info arrives
            createdAt: new Date().toISOString(),
            messages: [messageWithSender],
            memberIds: [],
            participants: [],
            isNewChannel: true
          };
          state.channels.unshift(newChannel);

        }
      }
      
      // Also update messageCache for real-time UI updates
      if (!state.messageCache[channelId]) {
        state.messageCache[channelId] = [];
      }
      
      // Check if message already exists in cache (deduplication)
      const cacheMessageExists = state.messageCache[channelId].some(msg => msg.key.messageId === messageId);
      if (!cacheMessageExists) {
        state.messageCache[channelId].push(messageWithSender);
      }
      
      // Sort messages by timestamp (oldest first)
      state.messageCache[channelId].sort((a, b) => {
        const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
        const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
        return timestampA - timestampB;
      });
      

    },
    
    // Handle notification when user is added to a channel
    receiveChannelAddedNotification: (state, action) => {
      const event = action.payload;

      
      // Create new channel object for the added user
      const newChannelData = {
        id: event.channelId,
        channelName: event.channelName,
        createdAt: event.addedAt,
        messages: [],
        memberIds: event.newMemberIds,
        participants: event.newMemberNames?.map((name, index) => ({
          userId: event.newMemberIds[index],
          name: name,
          firstname: name.split(' ')[0] || 'User',
          lastname: name.split(' ').slice(1).join(' ') || 'Unknown',
          email: `${event.newMemberIds[index]?.substring(0, 8) || 'unknown'}@example.com`,
          avatar: null,
          avatarUrl: null,
          role: 'MEMBER'
        })) || [],
        isNewChannel: true // Flag to show notification
      };
      
      // Add to channels list if not already there, OR update if it exists (placeholder)
      const existingChannelIndex = state.channels.findIndex(ch => ch.id === event.channelId);
      
      if (existingChannelIndex === -1) {
        state.channels.push(newChannelData);

      } else {

        const existingChannel = state.channels[existingChannelIndex];
        
        // Update properties
        existingChannel.channelName = newChannelData.channelName;
        existingChannel.createdAt = newChannelData.createdAt;
        existingChannel.memberIds = newChannelData.memberIds;
        existingChannel.participants = newChannelData.participants;
        // Keep existing messages (e.g. the notice message that arrived earlier)
      }
    },
    
    // Message cache actions
    cacheChannelMessages: (state, action) => {
      const { channelId, messages } = action.payload;
      state.messageCache[channelId] = messages;
      state.preloadedChannels[channelId] = true;

    },
    
    addPendingMessage: (state, action) => {
       const message = action.payload;
       const channelId = message.key.channelId;
       
       if (!state.messageCache[channelId]) {
         state.messageCache[channelId] = [];
       }
       state.messageCache[channelId].push(message);
       
       const channel = state.channels.find(c => c.id === channelId);
       if (channel) {
          if (!channel.messages) channel.messages = [];
          channel.messages.push(message);
       }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCreateChannel.fulfilled, (state, action) => {
        state.loading = false;


        const channelCreate = {
          ...action.payload,
          participants: action.payload.participants || [],
          messages: action.payload.message ? [action.payload.message] : [],
        };


        // Check if channel already exists
        const existingChannel = state.channels.find(ch => ch.id === channelCreate.id);
        if (!existingChannel) {
            state.channels.push(channelCreate);

        } else {

            // Update properties that might have been placeholder values
            existingChannel.channelName = channelCreate.channelName;
            existingChannel.participants = channelCreate.participants;
            existingChannel.memberIds = channelCreate.memberIds;
            existingChannel.createdAt = channelCreate.createdAt;
            // We don't overwrite messages here to preserve any real-time messages received
        }

        state.currentChannel = channelCreate;
        state.currentChannelId = channelCreate?.id || null;
        
        // Cache notice message for the new channel
        if (action.payload.message) {
          state.messageCache[action.payload.id] = [action.payload.message];
          state.preloadedChannels[action.payload.id] = true;

        }
        

      })
      .addCase(fetchCreateChannel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;

      })
      .addCase(fetchAllChannels.fulfilled, (state, action) => {
        state.loading = false;



        
        const channels = action.payload || [];
        state.channels = channels;
        
        // Cache messages and participants for each channel if they exist
        channels.forEach(channel => {
          // Cache messages
          if (channel.messages && channel.messages.length > 0) {
            // Debug: Log message structure from API



            
            // Sort messages by timestamp (oldest first)
            const sortedMessages = [...channel.messages].sort((a, b) => {
              const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
              const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
              return timestampA - timestampB;
            });
            
            state.messageCache[channel.id] = sortedMessages;
            state.preloadedChannels[channel.id] = true;

          } else {
            // Initialize empty message cache for channels without messages
            if (!state.messageCache[channel.id]) {
              state.messageCache[channel.id] = [];
            }

          }
          
          // Handle participants data - check if we have detailed member info or just memberIds
          if (channel.participants && channel.participants.length > 0) {
            // If participants already have detailed info, use them

          } else if (channel.memberIds && channel.memberIds.length > 0) {
            // If we only have memberIds, convert them to participant objects
            channel.participants = channel.memberIds.map(memberId => ({
              userId: memberId,
              firstname: `User`,
              lastname: memberId.substring(0, 8),
              name: `User ${memberId.substring(0, 8)}`, // Fallback for backward compatibility
              email: `${memberId.substring(0, 8)}@example.com`, // Temporary email
              avatar: null,
              role: 'MEMBER' // Default role in uppercase
            }));

          } else {
            // Initialize empty participants if not provided
            if (!channel.participants) {
              channel.participants = [];
            }

          }
        });
        
        // Sort channels by latest message timestamp or createdAt
        state.channels.sort((a, b) => {
            const getLastTime = (ch) => {
                if (ch.messages && ch.messages.length > 0) {
                    const lastMsg = ch.messages[ch.messages.length - 1];
                    return new Date(lastMsg.timestamp || lastMsg.key?.timestamp || 0).getTime();
                }
                return new Date(ch.createdAt || 0).getTime();
            };
            return getLastTime(b) - getLastTime(a);
        });



      })
      .addCase(fetchAllChannels.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;

      })
      .addCase(addMembersToChannel.fulfilled, (state, action) => {
        state.loading = false;
        state.joinedChannels = action.payload;
      })
      .addCase(addPeopleToChannel.fulfilled, (state, action) => {
        state.loading = false;

        
        const responseData = action.payload.data;
        const noticeMessage = responseData.message;
        
        // Update participants in current channel if it matches
        if (state.currentChannel && responseData.channelId === state.currentChannel.id) {
          // Add new members to participants
          if (responseData.newMembers && responseData.newMembers.length > 0) {
            const newParticipants = responseData.newMembers.map(member => ({
              userId: member.id,
              firstname: member.firstname || 'User',
              lastname: member.lastname || 'Unknown',
              name: `${member.firstname || 'User'} ${member.lastname || 'Unknown'}`,
              email: member.email || `${member.id?.substring(0, 8) || 'unknown'}@example.com`,
              avatar: member.avatarUrl || null,
              avatarUrl: member.avatarUrl || null,
              role: 'MEMBER'
            }));
            
            state.currentChannel.participants = [...(state.currentChannel.participants || []), ...newParticipants];

          }
          
          // Add notice message to current channel messages
          if (noticeMessage) {
            if (!state.currentChannel.messages) {
              state.currentChannel.messages = [];
            }
            
            // Check for duplicates
            const messageExists = state.currentChannel.messages.some(
                msg => {
                    const msgId = msg.key?.messageId || msg.id;
                    const noticeId = noticeMessage.key?.messageId || noticeMessage.id;
                    return msgId === noticeId;
                }
            );
            
            if (!messageExists) {
                state.currentChannel.messages.push(noticeMessage);

            } else {

            }
            
            // Also cache notice message for real-time UI updates
            if (!state.messageCache[responseData.channelId]) {
              state.messageCache[responseData.channelId] = [];
            }
            
            const cacheExists = state.messageCache[responseData.channelId].some(
                msg => msg.key?.messageId === noticeMessage.key?.messageId
            );
            
            if (!cacheExists) {
                state.messageCache[responseData.channelId].push(noticeMessage);
                
                // Sort messages by timestamp (oldest first)
                state.messageCache[responseData.channelId].sort((a, b) => {
                  const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
                  const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
                  return timestampA - timestampB;
                });
                

            } else {

            }
          }
        }
        
        // Also update in channels list
        const channelIndex = state.channels.findIndex(ch => ch.id === responseData.channelId);
        if (channelIndex !== -1) {
          if (responseData.newMembers && responseData.newMembers.length > 0) {
            const newParticipants = responseData.newMembers.map(member => ({
              userId: member.id,
              firstname: member.firstname || 'User',
              lastname: member.lastname || 'Unknown',
              name: `${member.firstname || 'User'} ${member.lastname || 'Unknown'}`,
              email: member.email || `${member.id?.substring(0, 8) || 'unknown'}@example.com`,
              avatar: member.avatarUrl || null,
              avatarUrl: member.avatarUrl || null,
              role: 'MEMBER'
            }));
            
            state.channels[channelIndex].participants = [...(state.channels[channelIndex].participants || []), ...newParticipants];

          }
          
          // Add notice message to channel messages
          if (noticeMessage) {
            if (!state.channels[channelIndex].messages) {
              state.channels[channelIndex].messages = [];
            }
            
            const listMessageExists = state.channels[channelIndex].messages.some(
                msg => msg.key?.messageId === noticeMessage.key?.messageId
            );
            
            if (!listMessageExists) {
                state.channels[channelIndex].messages.push(noticeMessage);

            }
          }
        }
      })
      .addCase(sendChannelMessage.pending, (state, action) => {
        const { channelId, content, type, tempId, userId } = action.meta.arg;
        
        // Create temporary message
        const tempMessage = {
          key: {
            channelId,
            messageId: tempId, // Use tempId as messageId initially
            timestamp: new Date().toISOString()
          },
          userId: userId || "current-user", 
          content,
          type: type || "CHAT",
          status: "pending",
          timestamp: new Date().toISOString()
        };

        // Add to cache immediately if not exists
        if (!state.messageCache[channelId]) {
          state.messageCache[channelId] = [];
        }
        
        const existsInCache = state.messageCache[channelId].some(m => m.key.messageId === tempId);
        if (!existsInCache) {
            state.messageCache[channelId].push(tempMessage);
        }
        
        // Add to channel messages if loaded and not exists
        const channelIndex = state.channels.findIndex(c => c.id === channelId);
        if (channelIndex !== -1) {
           const channel = state.channels[channelIndex];
           if (channel && channel.messages) {
               const existsInChannel = channel.messages.some(m => m.key.messageId === tempId);
               if (!existsInChannel) {
                   channel.messages.push(tempMessage);
               }
           }
           
           // Move to top
           if (channelIndex > 0) {
               const [movedChannel] = state.channels.splice(channelIndex, 1);
               state.channels.unshift(movedChannel);
           }
        }
      })
      .addCase(sendChannelMessage.fulfilled, (state, action) => {
        const message = action.payload;
        const channelId = message.key.channelId;
        const { tempId } = action.meta.arg;
        
        // Update cache: Find pending message by tempId and replace/update it
        if (state.messageCache[channelId]) {
          const index = state.messageCache[channelId].findIndex(
            m => m.key.messageId === tempId
          );
          
          if (index !== -1) {
            // Replace pending message with real message
            state.messageCache[channelId][index] = {
              ...message,
              status: "sent"
            };
          } else {
            // Fallback if not found (shouldn't happen usually)
            state.messageCache[channelId].push({ ...message, status: "sent" });
          }
           // Sort messages by timestamp (oldest first)
          state.messageCache[channelId].sort((a, b) => {
            const timestampA = new Date(a.timestamp || a.key?.timestamp || 0).getTime();
            const timestampB = new Date(b.timestamp || b.key?.timestamp || 0).getTime();
            return timestampA - timestampB;
          });
        }

        // Update channel messages if loaded
        const channel = state.channels.find(c => c.id === channelId);
        if (channel && channel.messages) {
           const index = channel.messages.findIndex(m => m.key.messageId === tempId);
           if (index !== -1) {
             channel.messages[index] = { ...message, status: "sent" };
           } else {
             channel.messages.push({ ...message, status: "sent" });
           }
        }
        

      })
      .addCase(sendChannelMessage.rejected, (state, action) => {

        const { channelId, tempId } = action.meta.arg;
        
        // Mark message as failed in cache
        if (state.messageCache[channelId]) {
          const index = state.messageCache[channelId].findIndex(
            m => m.key.messageId === tempId
          );
          if (index !== -1) {
            state.messageCache[channelId][index].status = "failed";
          }
        }
        
        // Mark message as failed in channel messages
        const channel = state.channels.find(c => c.id === channelId);
        if (channel && channel.messages) {
           const index = channel.messages.findIndex(m => m.key.messageId === tempId);
           if (index !== -1) {
             channel.messages[index].status = "failed";
           }
        }
      })
      .addCase(fetchDeleteChannel.fulfilled, (state, action) => {
        state.loading = false;
        const deletedChannelId = action.meta.arg; // channelId passed to the thunk
        state.channels = state.channels.filter(
          (channel) => channel.id !== deletedChannelId
        );
        if (state.currentChannelId === deletedChannelId) {
          state.currentChannelId = null;
          state.currentChannel = null;
          state.messagesOfCurrentChannel = [];
        }

      })
      ;
  },
});

export const {
  createChannel,
  addChannel,
  updateChannel,
  setChannels,
  selectChannel,
  removeChannel,
  removeCurrentChannel,
  setCurrentChannel,
  receiveMessage,
  receiveChannelAddedNotification,
  cacheChannelMessages,
  addPendingMessage,
} = channelSlice.actions;
export default channelSlice.reducer;

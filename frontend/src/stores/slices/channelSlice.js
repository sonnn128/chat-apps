import { createSlice } from "@reduxjs/toolkit";
import {
  fetchCreateChannel,
  fetchAllChannels,
  addMembersToChannel,
  addPeopleToChannel,
  sendChannelMessage,
  fetchDeleteChannel,
  fetchChannelById,
  updateChannelTheme,
  removeMemberFromChannel
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
    markChannelAsReadLocal: (state, action) => {
        const channelId = action.payload;
        const index = state.channels.findIndex(c => c.id === channelId);
        if (index !== -1) {
            state.channels[index].hasUnread = false;
        }
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
      
      if (action.payload.type === "DELETED") {
          console.log("REDUX DEBUG: Receiving DELETED message", { 
              payloadType: action.payload.type, 
              mergedType: messageWithSender.type,
              payloadContent: action.payload.content,
              key: action.payload.key
          });
      }
      
      if (channelFind) {
        if (!channelFind.messages) {
          channelFind.messages = [];
        }
        // Check if message already exists (deduplication or update)
        const messageIndex = channelFind.messages.findIndex(msg => {
            const msgId = msg.key?.messageId || msg.id;
            return msgId === messageId;
        });

        if (messageIndex === -1) {
          channelFind.messages.push(messageWithSender);
        } else {
          // Update existing message (e.g. for DELETED status)
          channelFind.messages[messageIndex] = { ...channelFind.messages[messageIndex], ...messageWithSender };
        }
        
        // Mark as unread if not current channel
        if (state.currentChannelId !== channelId) {
            channelFind.hasUnread = true;
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
      
      // Update currentChannel if it matches the message channel
      if (state.currentChannel && state.currentChannel.id === channelId) {
          if (!state.currentChannel.messages) {
              state.currentChannel.messages = [];
          }
          const currentMsgIndex = state.currentChannel.messages.findIndex(msg => {
              const msgId = msg.key?.messageId || msg.id;
              return msgId === messageId;
          });
          
          if (currentMsgIndex === -1) {
              state.currentChannel.messages.push(messageWithSender);
          } else {
              // Update existing message
              state.currentChannel.messages[currentMsgIndex] = { ...state.currentChannel.messages[currentMsgIndex], ...messageWithSender };
          }
      }
      
      // Also update messageCache for real-time UI updates
      if (!state.messageCache[channelId]) {
        state.messageCache[channelId] = [];
      }
      
      // Check if message already exists in cache (deduplication or update)
      const cacheMessageIndex = state.messageCache[channelId].findIndex(msg => msg.key.messageId === messageId);
      
      if (cacheMessageIndex === -1) {
        state.messageCache[channelId].push(messageWithSender);
      } else {
        // Update existing message in cache
        state.messageCache[channelId][cacheMessageIndex] = { ...state.messageCache[channelId][cacheMessageIndex], ...messageWithSender };
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
    
    // Handle notification when channel details are updated (e.g. name change)
    receiveChannelUpdatedNotification: (state, action) => {
        const event = action.payload;
        // event has channelId, newChannelName, updaterId, updatedAt, and possibly other fields
        console.log("🔔 receiveChannelUpdatedNotification:", event);
        console.log("🎨 Theme in event:", { color: event.themeColor, gradient: event.themeGradient });
        
        // Update in channels list
        const channelIndex = state.channels.findIndex(ch => ch.id === event.channelId);
        if (channelIndex !== -1) {
            // Update only the fields that are present in the event
            const updates = {};
            if (event.newChannelName) updates.channelName = event.newChannelName;
            if (event.channelName) updates.channelName = event.channelName;
            if (event.avatar) updates.avatar = event.avatar;
            if (event.avatarUrl) updates.avatar = event.avatarUrl;
            if (event.themeColor !== undefined) updates.themeColor = event.themeColor;
            if (event.themeGradient !== undefined) updates.themeGradient = event.themeGradient;
            
            state.channels[channelIndex] = {
                ...state.channels[channelIndex],
                ...updates
            };
        }
        
        // Update current channel if matches
        if (state.currentChannel && state.currentChannel.id === event.channelId) {
            const updates = {};
            if (event.newChannelName) updates.channelName = event.newChannelName;
            if (event.channelName) updates.channelName = event.channelName;
            if (event.avatar) updates.avatar = event.avatar;
            if (event.avatarUrl) updates.avatar = event.avatarUrl;
            if (event.themeColor !== undefined) updates.themeColor = event.themeColor;
            if (event.themeGradient !== undefined) updates.themeGradient = event.themeGradient;
            
            state.currentChannel = {
                ...state.currentChannel,
                ...updates
            };
        }
    },

    receiveMemberRemovedNotification: (state, action) => {
        const { channelId, removedMemberIds } = action.payload;
        
        const removeMembersFromChannel = (channel) => {
            if (channel.memberIds) {
                channel.memberIds = channel.memberIds.filter(id => !removedMemberIds.includes(id));
            }
            if (channel.participants) {
                channel.participants = channel.participants.filter(p => !removedMemberIds.includes(p.userId || p.id));
            }
        };

        const channelIndex = state.channels.findIndex(c => c.id === channelId);
        if (channelIndex !== -1) {
             removeMembersFromChannel(state.channels[channelIndex]);
        }
        
        if (state.currentChannel && state.currentChannel.id === channelId) {
            removeMembersFromChannel(state.currentChannel);
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
            if (responseData.newMembers[0]) {
                console.log("🔍 First new member structure:", JSON.stringify(responseData.newMembers[0], null, 2));
            }
            const newParticipants = responseData.newMembers.map(member => {
              const mapped = {
                  userId: member.userId || member.id || member.user_id,
                  firstname: member.firstname || 'User',
                  lastname: member.lastname || 'Unknown',
                  name: `${member.firstname || 'User'} ${member.lastname || 'Unknown'}`,
                  email: member.email || `${(member.userId || member.id || member.user_id)?.substring(0, 8) || 'unknown'}@example.com`,
                  avatar: member.avatarUrl || null,
                  avatarUrl: member.avatarUrl || null,
                  role: 'MEMBER'
              };
              console.log("🧬 Mapped new member:", mapped);
              return mapped;
            });
            
            // Update in channels list
            if (state.channels[channelIndex].participants) {
                const newIds = new Set(newParticipants.map(p => p.userId).filter(Boolean));
                const newEmails = new Set(newParticipants.map(p => p.email).filter(Boolean));
                
                const existing = state.channels[channelIndex].participants.filter(p => {
                    const pId = p.userId || p.id;
                    if (pId && newIds.has(pId)) return false;
                    if (p.email && newEmails.has(p.email)) return false;
                    return true;
                });
                state.channels[channelIndex].participants = [...existing, ...newParticipants];
            } else {
                state.channels[channelIndex].participants = newParticipants;
            }
            
            // Update in currentChannel if matched
            if (state.currentChannel && state.currentChannel.id === responseData.channelId) {
                console.log("🔄 Syncing new members to currentChannel with deduplication");
                const newIds = new Set(newParticipants.map(p => p.userId).filter(Boolean));
                const newEmails = new Set(newParticipants.map(p => p.email).filter(Boolean));

                const existing = (state.currentChannel.participants || []).filter(p => {
                    const pId = p.userId || p.id;
                    if (pId && newIds.has(pId)) return false;
                    if (p.email && newEmails.has(p.email)) return false;
                    // Also filter out broken ghosts with no ID if we want to be aggressive, 
                    // but safety first: let email match handle it.
                    return true;
                });
                state.currentChannel.participants = [...existing, ...newParticipants];
            }

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
      .addCase(fetchChannelById.fulfilled, (state, action) => {
        // Update channel in list with fresh data from API
        const updatedChannel = action.payload;
        console.log("📥 channelSlice: fetchChannelById.fulfilled - Updated channel:", updatedChannel);
        const channelIndex = state.channels.findIndex(ch => ch.id === updatedChannel.id);
        
        if (channelIndex !== -1) {
          console.log("✅ channelSlice: Updating channel in list at index:", channelIndex);
          // Update channel in list
          state.channels[channelIndex] = {
            ...state.channels[channelIndex],
            ...updatedChannel
          };
        } else {
          console.warn("⚠️ channelSlice: Channel not found in list for ID:", updatedChannel.id);
        }
        
        // Update current channel if it matches
        if (state.currentChannel && state.currentChannel.id === updatedChannel.id) {
          console.log("✅ channelSlice: Updating currentChannel");
          state.currentChannel = {
            ...state.currentChannel,
            ...updatedChannel
          };
        } else {
          console.warn("⚠️ channelSlice: currentChannel doesn't match or not set");
        }
      })
      .addCase(updateChannelTheme.fulfilled, (state, action) => {
        const updatedChannel = action.payload;
        const channelIndex = state.channels.findIndex(ch => ch.id === updatedChannel.id);
        
        if (channelIndex !== -1) {
          // Update theme in channel list
          state.channels[channelIndex] = {
            ...state.channels[channelIndex],
            themeColor: updatedChannel.themeColor,
            themeGradient: updatedChannel.themeGradient,
          };
        }
        
        // Update current channel if it matches
        if (state.currentChannel && state.currentChannel.id === updatedChannel.id) {
          state.currentChannel = {
            ...state.currentChannel,
            themeColor: updatedChannel.themeColor,
            themeGradient: updatedChannel.themeGradient,
          };
        }
      })
      .addCase(removeMemberFromChannel.fulfilled, (state, action) => {
          const { channelId, memberId } = action.payload;
          const removedMemberIds = [memberId];
          
          const removeMembersFromChannel = (channel) => {
            if (channel.memberIds) {
                channel.memberIds = channel.memberIds.filter(id => !removedMemberIds.includes(id));
            }
            if (channel.participants) {
                channel.participants = channel.participants.filter(p => !removedMemberIds.includes(p.userId || p.id));
            }
        };

        const channelIndex = state.channels.findIndex(c => c.id === channelId);
        if (channelIndex !== -1) {
             removeMembersFromChannel(state.channels[channelIndex]);
        }
        
        if (state.currentChannel && state.currentChannel.id === channelId) {
            removeMembersFromChannel(state.currentChannel);
        }
      });
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
  receiveChannelUpdatedNotification,
  receiveMemberRemovedNotification,
  cacheChannelMessages,
  addPendingMessage,
  markChannelAsReadLocal,
} = channelSlice.actions;

export default channelSlice.reducer;

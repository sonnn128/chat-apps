import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import ChatHeader from "@/components/chat/ChatHeader";
import ChatMessages from "@/components/chat/ChatMessages";
import ChatInput from "@/components/chat/ChatInput";
import AddMemberModal from "@/components/modals/AddMemberModal";
import ChatInfoSidebar from "@/components/chat/ChatInfoSidebar";
import WelcomeState from "@/components/chat/WelcomeState";
import { websocketService } from "@/utils/ws";
import { receiveMessage, addChannel, receiveChannelAddedNotification } from "@/stores/slices/channelSlice";
import { receiveFriendRequest, receiveFriendRequestAccepted, receiveFriendRequestRejected } from "@/stores/slices/friendshipSlice";
import { fetchPendingRequests, fetchFriendList } from "@/stores/middlewares/friendShipMiddleware";
import { successToast } from "@/utils/toast";

const ChatSection = () => {
    const { channels, currentChannelId } = useSelector((state) => state.channel);
    const currentFriend = useSelector((state) => state.friendship.currentFriend);

    const currentChannel = channels.find((x) => x.id === currentChannelId);
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user);

    useEffect(() => {
        const userId = user?.data?.id;
        if (!userId) {
            return;
        }

        const destination = `/user/${userId}/queue/notifications`;

        websocketService.subscribe(destination, (message) => {

            try {
                if (message.eventType === "FRIEND_REQUEST_SENT") {
                    if (message.recipientId === user?.data?.id) {
                        dispatch(receiveFriendRequest(message));
                        dispatch(fetchPendingRequests());
                        successToast("You have a new friend request! 🎉");
                    } else {
                    }
                } else if (message.eventType === "FRIEND_REQUEST_ACCEPTED") {
                    if (message.friend1Id === user?.data?.id || message.friend2Id === user?.data?.id) {
                        dispatch(receiveFriendRequestAccepted(message));
                        dispatch(fetchFriendList());

                        if (message.friend2Id === user?.data?.id) {
                            successToast("Friend request accepted! 🎉");
                        } else if (message.friend1Id === user?.data?.id) {
                            successToast("Your friend request was accepted! 🎉");
                        }

                    } else {
                    }
                } else if (message.eventType === "FRIEND_REQUEST_REJECTED") {
                    if (message.senderId === user?.data?.id || message.recipientId === user?.data?.id) {
                        dispatch(receiveFriendRequestRejected(message));
                        dispatch(fetchPendingRequests());

                        if (message.senderId === user?.data?.id) {
                            successToast("Your friend request was rejected");
                        } else if (message.recipientId === user?.data?.id) {
                            successToast("Friend request rejected");
                        }

                    } else {
                    }
                } else if (message.eventType === "MESSAGE_SENT") {
                    if (message.type === "NOTICE" && message.content && message.content.includes("đã thêm")) {
                        console.log("📨 ChatSection: Notice message about adding people to channel:", message.content);
                        dispatch(receiveMessage(message));
                    } else {
                        dispatch(receiveMessage(message));
                    }
                } else if (message.eventType === "CHANNEL_CREATED") {
                    dispatch(addChannel(message));
                } else if (message.eventType === "MEMBERS_ADDED_TO_CHANNEL") {
                    const isNewMember = message.newMemberIds && message.newMemberIds.includes(user?.data?.id);
                    if (isNewMember) {
                        dispatch(receiveChannelAddedNotification(message));
                        successToast(`You've been added to channel "${message.channelName}"! 🎉`);
                    } else {
                    }
                } else if (message.key && message.key.channelId) {
                    dispatch(receiveMessage(message));
                } else if (message.requesterId && message.friendId && !message.eventType) {
                    if (message.friendId === user?.data?.id) {
                        dispatch(receiveFriendRequest(message));
                        dispatch(fetchPendingRequests());
                        successToast("You have a new friend request! 🎉");
                    } else {
                    }
                } else if (message.requesterId && message.rejecterId && !message.eventType) {
                    if (message.requesterId === user?.data?.id) {
                        dispatch(receiveFriendRequestRejected(message));
                        dispatch(fetchPendingRequests());
                        successToast("Friend request rejected");
                    } else {
                    }
                } else if (message.requesterId && message.accepterId && !message.eventType) {
                    if (message.requesterId === user?.data?.id || message.accepterId === user?.data?.id) {
                        dispatch(receiveFriendRequestAccepted(message));
                        dispatch(fetchFriendList());

                        if (message.accepterId === user?.data?.id) {
                            // Person who accepted the request
                            successToast("Friend request accepted! 🎉");
                        } else if (message.requesterId === user?.data?.id) {
                            // Person who sent the request
                            successToast("Your friend request was accepted! 🎉");
                        }
                    } else {
                    }
                } else {
                }
            } catch (error) {
                console.error("❌ ChatSection: Error dispatching message:", error);
            }
        });

        // Cleanup function
        return () => {
        };
    }, [dispatch, user?.data?.id]);

    if (!currentChannelId && !currentFriend) {
        return (
            <div className="flex h-full w-full">
                <WelcomeState />
                <AddMemberModal open={false} onClose={() => { }} channelId={null} />
            </div>
        );
    }

    return (
        <div className="flex h-full w-full">
            <div className="flex flex-col flex-1 min-w-0">
                <ChatHeader title={
                    // If a friend is selected, show their name
                    currentFriend ? `${currentFriend.firstname || ''} ${currentFriend.lastname || ''}` : (
                        currentChannel && currentChannel.participants && currentChannel.participants.length === 2
                            ? (currentChannel.participants.find(p => p.userId !== user?.data?.id)?.name || currentChannel.channelName || "Conversation")
                            : (currentChannel?.channelName || "Channel")
                    )
                } />
                <div className="flex-1 overflow-hidden flex flex-col">
                    {currentChannelId ? (
                        <ChatMessages />
                    ) : (
                        <div className="flex items-center justify-center h-full text-gray-500">
                            Select a chat to start messaging
                        </div>
                    )}
                </div>
                <ChatInput />
            </div>

            {!(currentFriend) && currentChannelId && <ChatInfoSidebar />}

            <AddMemberModal open={false} onClose={() => { }} channelId={null} />
        </div>
    );
};

export default ChatSection;

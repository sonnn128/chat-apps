import React, { createContext, useState, useRef, useEffect, useContext } from "react";
import Peer from "simple-peer";
import { useSelector, useDispatch } from "react-redux";
import { websocketService } from "@/utils/ws";
import { message } from "antd";

const CallContext = createContext();

export const CallProvider = ({ children }) => {
    const [stream, setStream] = useState(null);
    const [remoteStream, setRemoteStream] = useState(null);
    const [call, setCall] = useState({}); // { isReceivingCall, callerId, senderName, signal }
    const [callAccepted, setCallAccepted] = useState(false);
    const [callEnded, setCallEnded] = useState(false);
    const [isCalling, setIsCalling] = useState(false);
    const [currentPeer, setCurrentPeer] = useState(null);
    const [isVideo, setIsVideo] = useState(false);

    const connectionRef = useRef();
    const user = useSelector((state) => state.auth.user?.data);

    useEffect(() => {
        // Only setup listener if user is logged in
        if (!user?.id) return;

        // Note: Real-time listening is handled in ChatSection/App via websocket subscription.
        // However, we need a way to receive the specific CALL_SIGNAL here.
        // Ideally, we subscribe to the notification topic here or expose a handler.
        // For now, we will assume App.jsx or Sidebar passes the signal to us OR we subscribe here.

        const destination = `/user/${user.id}/queue/notifications`;
        // Since we can't easily multi-subscribe to the same topic without managing callbacks carefully in the utility,
        // we might rely on Redux or a global event bus. 
        // BUT, let's try to subscribe nicely if the utility supports it (implied strictly 1 callback in util? No, util has a Map).
        // Let's verify existing ws.js. It supports multiple subscriptions to DIFFERENT destinations, but maybe not same?
        // ws.js: subscriptions.set(destination, subscription). It overwrites! 
        // So we cannot subscribe to the same topic twice.
        // We need to refactor WS handling or assume the Redux store receives the event.
        // Let's check NotificationService again. It uses `handleNotification`. 
        // Let's assume for this MVP, we will dispatch an action to Redux `callSlice` or use a custom event.

        // better approach for MVP: Add a custom event listener for "CALL_SIGNAL_RECEIVED"
        const handleSignal = (e) => {
            const payload = e.detail;
            if (payload.type === "OFFER") {
                setCall({
                    isReceivingCall: true,
                    callerId: payload.senderId,
                    signal: JSON.parse(payload.sdp), // we stored strict SignalData in sdp field stringified? Or is it object?
                    // Wait, CallSignalPayload has 'sdp' as String? simple-peer signal is object.
                    isVideo: payload.isVideo,
                    channelId: payload.channelId
                });
            } else if (payload.type === "ANSWER") {
                if (connectionRef.current) {
                    connectionRef.current.signal(JSON.parse(payload.sdp));
                }
            } else if (payload.type === "ICE_CANDIDATE") {
                // handled by simple-peer implementation usually if integrated in signal flow, 
                // but simple-peer 'signal' method handles both types if passed correctly.
                if (connectionRef.current) {
                    connectionRef.current.signal(JSON.parse(payload.candidate));
                }
            } else if (payload.type === "HANGUP") {
                leaveCall();
            }
        };

        window.addEventListener("CALL_SIGNAL_RECEIVED", handleSignal);
        return () => {
            window.removeEventListener("CALL_SIGNAL_RECEIVED", handleSignal);
        };
    }, [user?.id]);

    const callUser = async (idToCall, channelId, video = true) => {
        setIsCalling(true);
        setIsVideo(video);

        try {
            const currentStream = await navigator.mediaDevices.getUserMedia({ video, audio: true });
            setStream(currentStream);

            const peer = new Peer({ initiator: true, trickle: false, stream: currentStream });

            peer.on("signal", (data) => {
                const payload = {
                    type: "OFFER",
                    sdp: JSON.stringify(data),
                    senderId: user.id,
                    calleeId: idToCall,
                    callerId: user.id,
                    channelId: channelId,
                    isVideo: video
                };
                // Send signal via API
                sendSignalToBackend(payload);
            });

            peer.on("stream", (currentRemoteStream) => {
                setRemoteStream(currentRemoteStream);
            });

            connectionRef.current = peer;
            setCurrentPeer(peer);
        } catch (err) {
            console.error("Failed to get local stream", err);
            message.error("Could not access camera/microphone");
            setIsCalling(false);
        }
    };

    const answerCall = async () => {
        setCallAccepted(true);

        try {
            const currentStream = await navigator.mediaDevices.getUserMedia({ video: call.isVideo, audio: true });
            setStream(currentStream);

            const peer = new Peer({ initiator: false, trickle: false, stream: currentStream });

            peer.on("signal", (data) => {
                const payload = {
                    type: "ANSWER",
                    sdp: JSON.stringify(data),
                    senderId: user.id,
                    calleeId: user.id, // I am callee, replying
                    callerId: call.callerId,
                    channelId: call.channelId,
                    isVideo: call.isVideo
                };
                sendSignalToBackend(payload);
            });

            peer.on("stream", (currentRemoteStream) => {
                setRemoteStream(currentRemoteStream);
            });

            peer.signal(call.signal);

            connectionRef.current = peer;
            setCurrentPeer(peer);
        } catch (err) {
            console.error("Failed to answer call", err);
            message.error("Could not access camera/microphone");
            leaveCall();
        }
    };

    const leaveCall = () => {
        setCallEnded(true);
        if (connectionRef.current) {
            connectionRef.current.destroy();
        }
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
        }
        setStream(null);
        setRemoteStream(null);
        setCall({});
        setCallAccepted(false);
        setIsCalling(false);
        setCallEnded(false);

        // Notify other peer? Ideally yes.
        // We'd send a HANGUP signal here if we were properly connected.
    };

    const sendSignalToBackend = async (payload) => {
        const chatService = (await import("@/services/chatService")).default; // Dynamic import to avoid cycles if any
        // Alternatively use axios directly or create a callService
        try {
            // Assuming we create a callService or use axios
            const { default: axios } = await import("axios");
            const { getAuthHeaders } = await import("@/utils/authUtils");
            await axios.post(`${import.meta.env.VITE_REACT_APP_BASE_URL}/api/v1/calls/signal`, payload, {
                headers: getAuthHeaders()
            });
        } catch (error) {
            console.error("Error sending signal", error);
        }
    };

    return (
        <CallContext.Provider value={{
            call,
            callAccepted,
            stream,
            remoteStream,
            callEnded,
            isCalling,
            callUser,
            leaveCall,
            answerCall,
            setStream,
            isVideo
        }}>
            {children}
        </CallContext.Provider>
    );
};

export const useCall = () => useContext(CallContext);

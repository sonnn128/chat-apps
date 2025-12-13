import React, { useEffect, useRef, useState } from 'react';
import { useCall } from '@/context/CallContext';
import { Button, Modal, Avatar } from 'antd';
import { PhoneOff, Mic, MicOff, Video, VideoOff, PhoneIncoming, Maximize2, Minimize2 } from 'lucide-react';
import { UserOutlined } from '@ant-design/icons'; // Correct import for Ant Design's UserOutlined
import { createPortal } from 'react-dom';
import Draggable from 'react-draggable';

const CallModal = () => {
    const {
        call,
        callAccepted,
        stream,
        remoteStream,
        callEnded,
        isCalling,
        leaveCall,
        answerCall,
        isVideo
    } = useCall();

    const myVideo = useRef();
    const userVideo = useRef();
    const [isMuted, setIsMuted] = useState(false);
    const [isCamOff, setIsCamOff] = useState(false);
    const [isMinimized, setIsMinimized] = useState(false);
    const nodeRef = useRef(null); // For Draggable to avoid strict mode warnings

    useEffect(() => {
        if (stream && myVideo.current) {
            myVideo.current.srcObject = stream;
        }
    }, [stream, isCamOff]); // Re-attach stream if cam toggled? No, stream stays same usually for tracks.

    useEffect(() => {
        if (remoteStream && userVideo.current) {
            userVideo.current.srcObject = remoteStream;
        }
    }, [remoteStream, callAccepted, callEnded]);

    // Handle Mute/Unmute
    const toggleMute = () => {
        if (stream) {
            stream.getAudioTracks().forEach(track => track.enabled = !track.enabled);
            setIsMuted(!isMuted);
        }
    };

    // Handle Camera On/Off
    const toggleCamera = () => {
        if (stream) {
            stream.getVideoTracks().forEach(track => track.enabled = !track.enabled);
            setIsCamOff(!isCamOff);
        }
    };

    if ((!isCalling && !call.isReceivingCall) || callEnded) return null;

    // Incoming Call Modal (Stays centered, not draggable for now or can be)
    if (call.isReceivingCall && !callAccepted) {
        return (
            <Modal
                title="Incoming Call"
                open={true}
                footer={null}
                closable={false}
                maskClosable={false}
                centered
                width={320}
                className="incoming-call-modal"
            >
                <div className="flex flex-col items-center gap-6 py-4">
                    <div className="relative">
                        <Avatar size={80} icon={<UserOutlined />} src={null /* Todo: Add avatar to signal payload */} className="bg-blue-500" />
                        <div className="absolute -bottom-1 -right-1 bg-green-500 w-5 h-5 rounded-full border-2 border-white animate-pulse"></div>
                    </div>

                    <div className="text-center">
                        <h3 className="font-bold text-xl text-gray-800">{call.senderName || "Unknown Caller"}</h3>
                        <p className="text-gray-500 mt-1">{call.isVideo ? "Incoming Video Call..." : "Incoming Voice Call..."}</p>
                    </div>

                    <div className="flex gap-8 w-full justify-center mt-2">
                        <div className="flex flex-col items-center gap-2">
                            <Button danger shape="circle" size="large" onClick={leaveCall} className="w-14 h-14 !bg-red-500 border-none hover:!bg-red-600 shadow-lg flex items-center justify-center">
                                <PhoneOff className="text-white w-6 h-6" fill="currentColor" />
                            </Button>
                            <span className="text-xs text-gray-500 font-medium">Decline</span>
                        </div>

                        <div className="flex flex-col items-center gap-2">
                            <Button type="primary" shape="circle" size="large" onClick={answerCall} className="w-14 h-14 !bg-green-500 border-none hover:!bg-green-600 shadow-lg flex items-center justify-center">
                                <PhoneIncoming className="text-white w-6 h-6" fill="currentColor" />
                            </Button>
                            <span className="text-xs text-gray-500 font-medium">Accept</span>
                        </div>
                    </div>
                </div>
            </Modal>
        );
    }

    // Active Call UI - Draggable Window
    return createPortal(
        <Draggable nodeRef={nodeRef} handle=".drag-handle" bounds="body">
            <div ref={nodeRef} className={`fixed z-[9999] bg-gray-900 shadow-2xl overflow-hidden transition-all duration-200 border border-gray-800
                ${isMinimized
                    ? 'w-48 h-32 rounded-lg'
                    : 'w-[800px] h-[600px] rounded-xl' // Default large size like Messenger
                } top-20 left-20 flex flex-col`}
            >
                {/* Header / Drag Handle */}
                <div className="drag-handle absolute top-0 left-0 right-0 h-10 z-20 hover:bg-white/5 cursor-move flex items-center justify-between px-3 group">
                    {/* Window Controls */}
                    <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button onClick={() => setIsMinimized(!isMinimized)} className="w-3 h-3 rounded-full bg-yellow-500 hover:bg-yellow-400" title="Minimize" />
                        <button onClick={() => setIsMinimized(false)} className="w-3 h-3 rounded-full bg-green-500 hover:bg-green-400" title="Maximize" />
                    </div>
                </div>

                {/* Main Video Area */}
                <div className="relative flex-1 bg-black flex items-center justify-center h-full w-full">
                    {/* Remote Video */}
                    {remoteStream ? (
                        <video
                            ref={userVideo}
                            playsInline
                            autoPlay
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <div className="flex flex-col items-center gap-4 animate-pulse">
                            <Avatar size={96} icon={<UserOutlined />} className="bg-gray-700" />
                            <span className="text-white text-lg font-medium">Connecting...</span>
                        </div>
                    )}

                    {/* Local Video (PiP) */}
                    {stream && !isMinimized && (
                        <Draggable bounds="parent">
                            <div className="absolute bottom-4 right-4 w-40 h-56 rounded-lg bg-gray-800 shadow-xl overflow-hidden border border-white/10 cursor-move z-30 group">
                                <video ref={myVideo} playsInline autoPlay muted className="w-full h-full object-cover transform scale-x-[-1]" />
                                {isCamOff && (
                                    <div className="absolute inset-0 flex flex-col items-center justify-center bg-gray-800 text-white/70">
                                        <VideoOff className="w-8 h-8 mb-2 opacity-50" />
                                        <span className="text-xs">Camera Off</span>
                                    </div>
                                )}
                            </div>
                        </Draggable>
                    )}
                </div>

                {/* Controls Bar (Hidden when minimized) */}
                {!isMinimized && (
                    <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex items-center gap-6 px-8 py-3 bg-gray-900/90 backdrop-blur-md rounded-full border border-white/10 shadow-2xl z-20">
                        {/* Mute Toggle */}
                        <button
                            onClick={toggleMute}
                            className={`p-3 rounded-full transition-all duration-200 ${!isMuted ? 'bg-gray-700 hover:bg-gray-600 text-white' : 'bg-white text-gray-900'}`}
                        >
                            {isMuted ? <MicOff size={24} /> : <Mic size={24} />}
                        </button>

                        {/* Camera Toggle */}
                        {isVideo && (
                            <button
                                onClick={toggleCamera}
                                className={`p-3 rounded-full transition-all duration-200 ${!isCamOff ? 'bg-gray-700 hover:bg-gray-600 text-white' : 'bg-white text-gray-900'}`}
                            >
                                {isCamOff ? <VideoOff size={24} /> : <Video size={24} />}
                            </button>
                        )}

                        {/* End Call */}
                        <button
                            onClick={leaveCall}
                            className="p-3 px-6 rounded-full bg-red-500 hover:bg-red-600 text-white transition-colors flex items-center gap-2"
                        >
                            <PhoneOff size={24} fill="currentColor" />
                        </button>
                    </div>
                )}
            </div>
        </Draggable>,
        document.body
    );
};

export default CallModal;

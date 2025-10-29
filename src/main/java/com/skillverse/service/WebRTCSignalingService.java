package com.skillverse.service;

import com.skillverse.dto.WebRTCSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebRTCSignalingService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Room management: roomId -> Set of participant peer IDs
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    
    // Peer to room mapping: peerId -> roomId
    private final Map<String, String> peerToRoom = new ConcurrentHashMap<>();
    
    // WebSocket session to peer mapping: sessionId -> peerId
    private final Map<String, String> sessionToPeer = new ConcurrentHashMap<>();

    public void processSignal(WebRTCSignalDTO signal, String sessionId) {
        String roomId = signal.getRoomId();
        String peerId = signal.getPeerId();

        switch (signal.getType()) {
            case "offer":
            case "answer":
            case "ice-candidate":
                // Forward signaling messages to target peer or broadcast to room
                if (signal.getTargetPeerId() != null) {
                    sendToUser(signal.getTargetPeerId(), signal);
                } else {
                    broadcastToRoom(roomId, signal, peerId);
                }
                break;

            case "start-screen-share":
            case "stop-screen-share":
                broadcastToRoom(roomId, signal, peerId);
                log.info("Screen share {} by peer {} in room {}", 
                        signal.getType().equals("start-screen-share") ? "started" : "stopped",
                        peerId, roomId);
                break;

            default:
                log.warn("Unknown signal type: {}", signal.getType());
        }
    }

    public void handleJoin(WebRTCSignalDTO signal, String sessionId) {
        String roomId = signal.getRoomId();
        String peerId = signal.getPeerId();

        // Add peer to room
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(peerId);
        peerToRoom.put(peerId, roomId);
        sessionToPeer.put(sessionId, peerId);

        // Notify existing participants about new peer
        Set<String> existingPeers = new HashSet<>(rooms.get(roomId));
        existingPeers.remove(peerId); // Don't notify the joining peer

        // Send existing peers list to the new peer
        WebRTCSignalDTO peersListSignal = WebRTCSignalDTO.builder()
                .type("peers-list")
                .roomId(roomId)
                .data(new ArrayList<>(existingPeers))
                .build();
        sendToUser(peerId, peersListSignal);

        // Notify existing peers about new peer
        WebRTCSignalDTO newPeerSignal = WebRTCSignalDTO.builder()
                .type("peer-joined")
                .roomId(roomId)
                .peerId(peerId)
                .userId(signal.getUserId())
                .userName(signal.getUserName())
                .isCreator(signal.getIsCreator())
                .build();
        broadcastToRoom(roomId, newPeerSignal, peerId);

        log.info("Peer {} joined room {}. Total peers: {}", peerId, roomId, rooms.get(roomId).size());
    }

    public void handleLeave(WebRTCSignalDTO signal, String sessionId) {
        String peerId = sessionToPeer.get(sessionId);
        if (peerId == null) {
            peerId = signal.getPeerId();
        }

        String roomId = peerToRoom.get(peerId);
        if (roomId == null) {
            roomId = signal.getRoomId();
        }

        if (roomId != null && peerId != null) {
            // Remove peer from room
            Set<String> roomPeers = rooms.get(roomId);
            if (roomPeers != null) {
                roomPeers.remove(peerId);
                if (roomPeers.isEmpty()) {
                    rooms.remove(roomId);
                }
            }

            peerToRoom.remove(peerId);
            sessionToPeer.remove(sessionId);

            // Notify remaining peers
            WebRTCSignalDTO peerLeftSignal = WebRTCSignalDTO.builder()
                    .type("peer-left")
                    .roomId(roomId)
                    .peerId(peerId)
                    .userId(signal.getUserId())
                    .build();
            broadcastToRoom(roomId, peerLeftSignal, null);

            log.info("Peer {} left room {}. Remaining peers: {}", 
                    peerId, roomId, roomPeers != null ? roomPeers.size() : 0);
        }
    }

    public void broadcastMediaState(WebRTCSignalDTO signal) {
        String roomId = signal.getRoomId();
        String peerId = signal.getPeerId();

        WebRTCSignalDTO mediaStateSignal = WebRTCSignalDTO.builder()
                .type("media-state-changed")
                .roomId(roomId)
                .peerId(peerId)
                .userId(signal.getUserId())
                .audioEnabled(signal.getAudioEnabled())
                .videoEnabled(signal.getVideoEnabled())
                .screenSharing(signal.getScreenSharing())
                .build();

        broadcastToRoom(roomId, mediaStateSignal, peerId);
    }

    private void broadcastToRoom(String roomId, WebRTCSignalDTO signal, String excludePeerId) {
        Set<String> roomPeers = rooms.get(roomId);
        if (roomPeers != null) {
            roomPeers.forEach(peerId -> {
                if (excludePeerId == null || !peerId.equals(excludePeerId)) {
                    sendToUser(peerId, signal);
                }
            });
        }
    }

    private void sendToUser(String peerId, WebRTCSignalDTO signal) {
        messagingTemplate.convertAndSendToUser(peerId, "/queue/signals", signal);
    }

    // Cleanup method for disconnected sessions
    public void handleDisconnect(String sessionId) {
        String peerId = sessionToPeer.get(sessionId);
        if (peerId != null) {
            WebRTCSignalDTO leaveSignal = WebRTCSignalDTO.builder()
                    .peerId(peerId)
                    .build();
            handleLeave(leaveSignal, sessionId);
        }
    }

    public Set<String> getRoomPeers(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }
}
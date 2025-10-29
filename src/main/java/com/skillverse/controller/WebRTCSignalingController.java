package com.skillverse.controller;

import com.skillverse.dto.WebRTCSignalDTO;
import com.skillverse.service.WebRTCSignalingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WebRTCSignalingController {

    @Autowired
    private WebRTCSignalingService signalingService;

    @MessageMapping("/signal/{roomId}")
    public void handleSignal(@DestinationVariable String roomId,
                            @Payload WebRTCSignalDTO signal,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            signal.setRoomId(roomId);
            
            log.debug("Received signal type: {} for room: {} from peer: {}", 
                     signal.getType(), roomId, signal.getPeerId());
            
            signalingService.processSignal(signal, sessionId);
        } catch (Exception e) {
            log.error("Error processing signal for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    

    @MessageMapping("/join/{roomId}")
    public void handleJoin(@DestinationVariable String roomId,
                          @Payload WebRTCSignalDTO signal,
                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            signal.setRoomId(roomId);
            signal.setType("join");
            
            log.info("User {} joining room: {}", signal.getUserId(), roomId);
            
            signalingService.handleJoin(signal, sessionId);
        } catch (Exception e) {
            log.error("Error handling join for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/leave/{roomId}")
    public void handleLeave(@DestinationVariable String roomId,
                           @Payload WebRTCSignalDTO signal,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            signal.setRoomId(roomId);
            signal.setType("leave");
            
            log.info("User {} leaving room: {}", signal.getUserId(), roomId);
            
            signalingService.handleLeave(signal, sessionId);
        } catch (Exception e) {
            log.error("Error handling leave for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/media-state/{roomId}")
    public void handleMediaStateChange(@DestinationVariable String roomId,
                                      @Payload WebRTCSignalDTO signal,
                                      SimpMessageHeaderAccessor headerAccessor) {
        try {
            signal.setRoomId(roomId);
            
            log.debug("Media state change for user {} in room {}: audio={}, video={}, screen={}", 
                     signal.getUserId(), roomId, signal.getAudioEnabled(), 
                     signal.getVideoEnabled(), signal.getScreenSharing());
            
            signalingService.broadcastMediaState(signal);
        } catch (Exception e) {
            log.error("Error handling media state change for room {}: {}", roomId, e.getMessage(), e);
        }
    }
}
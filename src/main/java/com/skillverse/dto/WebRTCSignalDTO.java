package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCSignalDTO {
    private String type; // "offer", "answer", "ice-candidate", "join", "leave", "mute", "unmute", "start-screen-share", "stop-screen-share"
    private String roomId;
    private String peerId;
    private String targetPeerId; // For directed messages
    private Object data; // SDP offer/answer or ICE candidate
    private Integer userId;
    private String userName;
    private Boolean isCreator;
    private String sessionId;
    
    // Media state
    private Boolean audioEnabled;
    private Boolean videoEnabled;
    private Boolean screenSharing;
}